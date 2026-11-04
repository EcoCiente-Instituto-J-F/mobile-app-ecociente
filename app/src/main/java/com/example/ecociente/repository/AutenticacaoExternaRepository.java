package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.SessaoExterna;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONException;
import org.json.JSONObject;

// Fala com a ds-mobile-api (Java/Postgres, repositório separado deste app), POST /auth/login. Chamada em paralelo ao login do Firebase, sem travar o app se essa API estiver fora do ar. Contas criadas antes do cadastro integrado não existem no Postgres e falham aqui.
public class AutenticacaoExternaRepository {

    private static final String TAG = "AuthExternaEcoCiente";

    private static final String URL_LOGIN = ConfiguracaoServidores.URL_LOGIN;

    private static final String PREFERENCIAS = "sessao_externa";
    private static final String CHAVE_TOKEN = "token";
    private static final String CHAVE_PERFIL = "perfil";
    private static final String CHAVE_USUARIO_ID = "usuarioId";
    private static final String CHAVE_FIREBASE_UID = "firebaseUid";
    private static final String CHAVE_TOKEN_EXPIRA_EM_MS = "tokenExpiraEmMs";

    private static final Object TRAVA = new Object();
    private static final long INTERVALO_APOS_FALHA_MS = 10_000L;
    private static volatile long novaTentativaPermitidaEmMs;

    private static final Object ESPERA = new Object();
    private static final long ESPERA_MAXIMA_MS = 180_000;
    private static int loginsEmAndamento = 0;

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private static final ExecutorService EXECUTOR_ESPERA = Executors.newSingleThreadExecutor();

    private static final Handler PRINCIPAL = new Handler(Looper.getMainLooper());

    public interface Retorno {
        void aoConcluir(@Nullable SessaoExterna sessao);
    }

    // `credenciaisRecusadas` é true quando a API respondeu que a conta não serve (senha ou usuário); sessao nula com false significa API fora do ar ou lenta.
    public interface RetornoLogin {
        void aoConcluir(@Nullable SessaoExterna sessao, boolean credenciaisRecusadas);
    }

    public void autenticar(
            @NonNull Context contexto,
            @NonNull String email,
            @NonNull String senha,
            @NonNull Retorno retorno) {

        autenticar(contexto, email, senha, (sessao, recusada) -> retorno.aoConcluir(sessao));
    }

    public void autenticar(
            @NonNull Context contexto,
            @NonNull String email,
            @NonNull String senha,
            @NonNull RetornoLogin retorno) {

        Context contextoApp = contexto.getApplicationContext();

        synchronized (ESPERA) {
            loginsEmAndamento++;
        }

        EXECUTOR.execute(
                () -> {
                    SessaoExterna sessao = null;
                    boolean recusada = false;

                    try {
                        sessao = login(email, senha);

                        if (usuarioIdSalvo(contextoApp) != sessao.getUsuarioId()) {
                            PerfilApiRepository.limpar(contextoApp);
                        }

                        persistirToken(contextoApp, sessao.getToken(), sessao.getExpiraEmSegundos());
                        persistirPerfil(contextoApp, sessao.getPerfil());
                        persistirUsuarioId(contextoApp, sessao.getUsuarioId());

                        CredenciaisSeguras.salvar(contextoApp, email, senha);
                        novaTentativaPermitidaEmMs = 0L;

                    } catch (CredenciaisRecusadas erro) {
                        recusada = true;
                        Log.w(TAG, "A API externa recusou a conta", erro);

                    } catch (Exception erro) {
                        Log.w(TAG, "Não foi possível autenticar na API externa", erro);
                    }

                    synchronized (ESPERA) {
                        loginsEmAndamento--;
                        ESPERA.notifyAll();
                    }

                    SessaoExterna sessaoFinal = sessao;
                    boolean recusadaFinal = recusada;

                    PRINCIPAL.post(() -> retorno.aoConcluir(sessaoFinal, recusadaFinal));
                });
    }

    // Só chamar fora da thread principal: pode esperar o login terminar.
    @Nullable
    public String tokenParaUso(@NonNull Context contexto) {

        aguardarLoginEmAndamento();

        String salvo = obterTokenSalvo(contexto);
        if (salvo != null) {
            return salvo;
        }
        return renovarTokenInterno(contexto, false);
    }

    public static void prepararSessaoRestaurada(@NonNull Context contexto) {
        Context contextoApp = contexto.getApplicationContext();
        EXECUTOR_ESPERA.execute(() -> {
            if (FirebaseAuth.getInstance().getCurrentUser() == null) {
                return;
            }
            try {
                String token = new AutenticacaoExternaRepository().tokenParaUso(contextoApp);
                if (token == null) {
                    Log.w(TAG, "Sessao da API indisponivel na abertura automatica");
                } else {
                    Log.i(TAG, "Sessao da API pronta apos abertura automatica");
                }
            } catch (Exception erro) {
                Log.w(TAG, "Falha ao preparar sessao da API na abertura automatica", erro);
            }
        });
    }

    public static boolean loginEmAndamento() {

        synchronized (ESPERA) {
            return loginsEmAndamento > 0;
        }
    }

    // Sessão antiga do Firebase (app aberto sem passar pelo login): ainda não há perfil da API guardado. Se há credenciais guardadas, refaz o login em segundo plano para obtê-lo.
    public static void garantirPerfil(@NonNull Context contexto, @NonNull Runnable aoConcluir) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR_ESPERA.execute(
                () -> {
                    if (perfilSalvo(contextoApp) == null) {
                        new AutenticacaoExternaRepository().renovarToken(contextoApp);
                    }

                    PRINCIPAL.post(aoConcluir);
                });
    }

    // Espera, fora da thread principal, o login terminar (ou o limite) e avisa na principal.
    public static void aguardarLogin(long limiteMs, @NonNull Runnable aoConcluir) {

        EXECUTOR_ESPERA.execute(
                () -> {
                    aguardarLoginEmAndamento(limiteMs);

                    PRINCIPAL.post(aoConcluir);
                });
    }

    private void aguardarLoginEmAndamento() {
        aguardarLoginEmAndamento(ESPERA_MAXIMA_MS);
    }

    private static void aguardarLoginEmAndamento(long limiteMs) {

        long limite = SystemClock.elapsedRealtime() + limiteMs;

        synchronized (ESPERA) {
            while (loginsEmAndamento > 0) {
                long restante = limite - SystemClock.elapsedRealtime();

                if (restante <= 0) {
                    return;
                }

                try {
                    ESPERA.wait(restante);
                } catch (InterruptedException erro) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }
        }
    }

    @Nullable
    public String renovarToken(@NonNull Context contexto) {
        return renovarTokenInterno(contexto, true);
    }

    @Nullable
    private String renovarTokenInterno(@NonNull Context contexto, boolean forcarRenovacao) {
        Context contextoApp = contexto.getApplicationContext();

        synchronized (TRAVA) {
            if (!forcarRenovacao) {
                String tokenAtual = obterTokenSalvo(contextoApp);
                if (tokenAtual != null) {
                    return tokenAtual;
                }
            }

            if (SystemClock.elapsedRealtime() < novaTentativaPermitidaEmMs) {
                Log.w(TAG, "Renovacao JWT temporariamente suspensa apos falha de conexao");
                return null;
            }

            String[] credenciais = CredenciaisSeguras.ler(contextoApp);

            if (credenciais == null || credenciais.length < 2) {
                String token = renovarPeloFirebase(contextoApp);
                if (token == null) {
                    registrarFalhaDeRenovacao();
                } else {
                    novaTentativaPermitidaEmMs = 0L;
                }
                return token;
            }

            try {
                SessaoExterna sessao = login(credenciais[0], credenciais[1]);

                persistirToken(contextoApp, sessao.getToken(), sessao.getExpiraEmSegundos());
                persistirPerfil(contextoApp, sessao.getPerfil());
                persistirUsuarioId(contextoApp, sessao.getUsuarioId());

                novaTentativaPermitidaEmMs = 0L;
                Log.i(TAG, "JWT renovado pela API Mobile");
                return sessao.getToken();

            } catch (CredenciaisRecusadas erro) {
                Log.w(TAG, "Credenciais guardadas recusadas; tentando troca via Firebase");
                CredenciaisSeguras.limpar(contextoApp);
                String token = renovarPeloFirebase(contextoApp);
                if (token == null) {
                    registrarFalhaDeRenovacao();
                } else {
                    novaTentativaPermitidaEmMs = 0L;
                }
                return token;

            } catch (Exception erro) {
                Log.w(TAG, "Nao foi possivel renovar o JWT na API Mobile", erro);
                registrarFalhaDeRenovacao();
                return null;
            }
        }
    }

    private static void registrarFalhaDeRenovacao() {
        novaTentativaPermitidaEmMs = SystemClock.elapsedRealtime() + INTERVALO_APOS_FALHA_MS;
    }

    public static void atualizarSenhaSalva(
            @NonNull Context contexto, @NonNull String email, @NonNull String novaSenha) {
        CredenciaisSeguras.salvar(contexto, email, novaSenha);
    }

    public static boolean temCredenciaisSalvas(@NonNull Context contexto) {

        String[] credenciais = CredenciaisSeguras.ler(contexto.getApplicationContext());

        return credenciais != null && credenciais.length >= 2;
    }

    private static final class CredenciaisRecusadas extends IOException {

        CredenciaisRecusadas(String mensagem) {
            super(mensagem);
        }
    }

    public static void encerrarSessao(@NonNull Context contexto) {

        novaTentativaPermitidaEmMs = 0L;
        CredenciaisSeguras.limpar(contexto);

        contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .edit()
                .remove(CHAVE_TOKEN)
                .remove(CHAVE_PERFIL)
                .remove(CHAVE_USUARIO_ID)
                .remove(CHAVE_FIREBASE_UID)
                .remove(CHAVE_TOKEN_EXPIRA_EM_MS)
                .apply();

        PerfilApiRepository.limpar(contexto);
    }

    // Perfil que a API devolveu no último login (SINDICO, COOPERATIVA...): é o dado do Postgres, usado para escolher a home quando o Firestore está com outro tipoPerfil.
    @Nullable
    public static String perfilSalvo(@NonNull Context contexto) {

        return contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .getString(CHAVE_PERFIL, null);
    }

    // Id do usuário no Postgres (vem no login). Trocar de conta apaga o que era do usuário anterior.
    public static int usuarioIdSalvo(@NonNull Context contexto) {
        return contexto.getApplicationContext()
                .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .getInt(CHAVE_USUARIO_ID, 0);
    }

    private void persistirUsuarioId(@NonNull Context contextoApp, int usuarioId) {

        SharedPreferences preferencias = contextoApp.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);

        if (preferencias.getInt(CHAVE_USUARIO_ID, 0) != usuarioId) {
            PerfilApiRepository.limpar(contextoApp);
        }

        preferencias.edit().putInt(CHAVE_USUARIO_ID, usuarioId).apply();
    }

    private void persistirPerfil(@NonNull Context contextoApp, @NonNull String perfil) {

        contextoApp.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .edit()
                .putString(CHAVE_PERFIL, perfil)
                .apply();
    }

    @NonNull
    private SessaoExterna login(@NonNull String email, @NonNull String senha) throws IOException, JSONException {
        return loginNoServidor(ConfiguracaoServidores.URL_LOGIN, email, senha,
                ConfiguracaoServidores.TIMEOUT_RESPOSTA_LOGIN_MS);
    }

    @NonNull
    private SessaoExterna loginNoServidor(
            @NonNull String endereco,
            @NonNull String email,
            @NonNull String senha,
            int timeoutResposta) throws IOException, JSONException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(endereco).openConnection();
        try {
            conexao.setRequestMethod("POST");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setDoOutput(true);
            conexao.setUseCaches(false);
            conexao.setConnectTimeout(ConfiguracaoServidores.TIMEOUT_CONEXAO_MS);
            conexao.setReadTimeout(timeoutResposta);

            JSONObject corpo = new JSONObject();
            corpo.put("email", email.trim().toLowerCase(Locale.ROOT));
            corpo.put("senha", senha);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }

            int codigoResposta = conexao.getResponseCode();

            if (codigoResposta >= 400 && codigoResposta < 500) {
                String corpoDoErro = LeituraHttp.texto(conexao.getErrorStream());

                if (PoliticaErroLogin.credenciaisRecusadas(codigoResposta, corpoDoErro)) {
                    throw new CredenciaisRecusadas("Resposta HTTP " + codigoResposta);
                }
            }
            if (codigoResposta < 200 || codigoResposta >= 300) {
                throw new IOException("Resposta HTTP " + codigoResposta);
            }

            JSONObject resposta = new JSONObject(LeituraHttp.texto(conexao.getInputStream()));
            return new SessaoExterna(
                    resposta.getString("token"),
                    resposta.optString("tipoToken", "Bearer"),
                    resposta.optLong("expiraEmSegundos", 0),
                    resposta.optInt("usuarioId", 0),
                    resposta.optString("nome", ""),
                    resposta.optString("email", email),
                    resposta.optString("perfil", ""));

        } finally {
            conexao.disconnect();
        }
    }

    @Nullable
    private String renovarPeloFirebase(@NonNull Context contextoApp) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        if (usuario == null) {
            Log.w(TAG, "Sessao Firebase ausente");
            return null;
        }
        try {
            String idToken = Tasks.await(usuario.getIdToken(false), 15, TimeUnit.SECONDS).getToken();
            if (idToken == null || idToken.isBlank()) {
                Log.w(TAG, "Firebase ID token nao disponivel");
                return null;
            }
            HttpURLConnection conexao = (HttpURLConnection) new URL(
                    ConfiguracaoServidores.MOBILE_API + "/auth/firebase").openConnection();
            try {
                conexao.setRequestMethod("POST");
                conexao.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                conexao.setRequestProperty("Accept", "application/json");
                conexao.setConnectTimeout(ConfiguracaoServidores.TIMEOUT_CONEXAO_MS);
                conexao.setReadTimeout(ConfiguracaoServidores.TIMEOUT_RESPOSTA_LOGIN_MS);
                conexao.setDoOutput(true);
                JSONObject corpo = new JSONObject();
                corpo.put("idToken", idToken);
                try (OutputStream saida = conexao.getOutputStream()) {
                    saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
                }
                int codigo = conexao.getResponseCode();
                if (codigo < 200 || codigo >= 300) {
                    Log.w(TAG, "Troca de sessao Firebase recusada (HTTP " + codigo + ")");
                    return null;
                }
                JSONObject resposta = new JSONObject(LeituraHttp.texto(conexao.getInputStream()));
                String jwt = resposta.getString("token");
                String perfil = resposta.optString("perfil", "");
                int usuarioId = resposta.optInt("usuarioId", 0);
                if (jwt.isBlank() || usuarioId <= 0 || perfil.isBlank()) {
                    Log.w(TAG, "Sessao da API incompleta");
                    return null;
                }
                if (FirebaseAuth.getInstance().getCurrentUser() == null
                        || !usuario.getUid().equals(FirebaseAuth.getInstance().getCurrentUser().getUid())) {
                    return null;
                }
                persistirToken(contextoApp, jwt, resposta.optLong("expiraEmSegundos", 0));
                persistirPerfil(contextoApp, perfil);
                persistirUsuarioId(contextoApp, usuarioId);
                Log.i(TAG, "JWT da API renovado via Firebase (HTTP " + codigo + ")");
                return jwt;
            } finally {
                conexao.disconnect();
            }
        } catch (Exception erro) {
            Log.w(TAG, "Nao foi possivel obter JWT via sessao Firebase", erro);
            return null;
        }
    }

    @Nullable
    private static String uidSalvo(@NonNull Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE)
                .getString(CHAVE_FIREBASE_UID, null);
    }

    private void persistirToken(@NonNull Context contextoApp, @NonNull String token, long expiraEmSegundos) {

        SharedPreferences preferencias =
                contextoApp.getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);

        String cifrado = CredenciaisSeguras.cifrar(token);
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String uid = usuario == null ? null : usuario.getUid();

        // Sem Keystore não há como guardar com segurança: o token fica só na memória do login.
        if (cifrado != null) {
            long prazoMs = ValidadeTokenApi.calcularVencimento(System.currentTimeMillis(), expiraEmSegundos);
            preferencias.edit().putString(CHAVE_TOKEN, cifrado)
                    .putString(CHAVE_FIREBASE_UID, uid)
                    .putLong(CHAVE_TOKEN_EXPIRA_EM_MS, prazoMs)
                    .apply();
        } else {
            preferencias.edit().remove(CHAVE_TOKEN)
                    .remove(CHAVE_FIREBASE_UID)
                    .remove(CHAVE_TOKEN_EXPIRA_EM_MS)
                    .apply();
        }
    }

    @Nullable
    public static String obterTokenSalvo(@NonNull Context contexto) {

        SharedPreferences preferencias =
                contexto
                        .getApplicationContext()
                        .getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String uid = preferencias.getString(CHAVE_FIREBASE_UID, null);
        if (usuario == null || !usuario.getUid().equals(uid)) {
            return null;
        }
        long expiraEmMs = preferencias.getLong(CHAVE_TOKEN_EXPIRA_EM_MS, 0L);
        if (ValidadeTokenApi.deveRenovar(expiraEmMs, System.currentTimeMillis())) {
            return null;
        }
        String guardado = preferencias.getString(CHAVE_TOKEN, null);

        // Token de versões antigas (texto puro) ou ilegível: descarta e o app renova com o login.
        return guardado == null ? null : CredenciaisSeguras.decifrar(guardado);
    }
}
