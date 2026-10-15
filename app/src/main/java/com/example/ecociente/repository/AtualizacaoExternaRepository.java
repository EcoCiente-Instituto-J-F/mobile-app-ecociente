package com.example.ecociente.repository;

import android.content.Context;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.PerfilUsuario;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;
import org.json.JSONObject;

// PATCH /api/usuarios/me e /api/usuarios/me/endereco da ds-cadastro-api (nome, telefone, foto, endereço). O Firestore continua
// sendo a fonte do app: aqui só se espelha o que a API sabe guardar, em segundo plano e sem
// avisar o usuário se falhar (contas sem JWT, como Google/Facebook, simplesmente não sincronizam).
// E-mail fica de fora de propósito: a API não troca o e-mail no Firebase, e os dois divergiriam.
public class AtualizacaoExternaRepository {

    private static final String TAG = "AtualizacaoExterna";

    private static final String URL_USUARIO = "https://ds-cadastro-api.onrender.com/api/usuarios/me";

    private static final Pattern CEP = Pattern.compile("^\\d{5}-?\\d{3}$");

    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();

    private final AutenticacaoExternaRepository autenticacao = new AutenticacaoExternaRepository();

    // `perfil` já deve trazer o valor novo, porque o endereço da API exige CEP e número juntos.
    public void sincronizarCampo(
            @NonNull Context contexto,
            @NonNull String campo,
            @NonNull String valor,
            @Nullable PerfilUsuario perfil) {

        try {
            switch (campo) {
                case PerfilRepository.CAMPO_NOME:
                    if (!valor.trim().isEmpty()) {
                        enviar(contexto, URL_USUARIO, new JSONObject().put("nome", valor.trim()));
                    }
                    break;

                case PerfilRepository.CAMPO_FOTO_URL:
                    // A API só aceita http/https com até 500 caracteres (urlAvatar).
                    if ((valor.startsWith("http://") || valor.startsWith("https://"))
                            && valor.trim().equals(valor)
                            && valor.length() <= 500) {
                        enviar(contexto, URL_USUARIO, new JSONObject().put("urlAvatar", valor));
                    }
                    break;

                case PerfilRepository.CAMPO_TELEFONE:
                    String digitos = valor.replaceAll("\\D", "");

                    if (digitos.length() == 10 || digitos.length() == 11) {
                        enviar(contexto, URL_USUARIO, new JSONObject().put("telefone", digitos));
                    }
                    break;

                case PerfilRepository.CAMPO_CEP:
                case PerfilRepository.CAMPO_NUMERO:
                case PerfilRepository.CAMPO_COMPLEMENTO:
                    sincronizarEndereco(contexto, perfil);
                    break;

                default:
                    // A API não tem onde guardar os demais campos (CPF, nome da cooperativa...).
                    break;
            }
        } catch (Exception erro) {
            Log.w(TAG, "Não foi possível montar a atualização de " + campo, erro);
        }
    }

    private void sincronizarEndereco(@NonNull Context contexto, @Nullable PerfilUsuario perfil)
            throws Exception {

        if (perfil == null) {
            return;
        }

        String cep = perfil.getCep().trim();
        String numero = perfil.getNumero().trim();

        if (!CEP.matcher(cep).matches() || numero.isEmpty()) {
            return;
        }

        enviar(
                contexto,
                URL_USUARIO + "/endereco",
                new JSONObject()
                        .put("cep", cep)
                        .put("numero", numero)
                        .put("complemento", perfil.getComplemento().trim()));
    }

    private void enviar(
            @NonNull Context contexto, @NonNull String endpoint, @NonNull JSONObject corpo) {

        Context contextoApp = contexto.getApplicationContext();

        EXECUTOR.execute(
                () -> {
                    try {
                        String token = autenticacao.tokenParaUso(contextoApp);

                        if (token == null) {
                            Log.d(TAG, "Sem credenciais para a API; atualização fica só no app");
                            return;
                        }

                        int codigo = patch(endpoint, corpo, token);

                        // O JWT dura minutos: o salvo costuma estar vencido.
                        if (codigo == 401 || codigo == 403) {
                            String renovado = autenticacao.renovarToken(contextoApp);

                            if (renovado != null) {
                                codigo = patch(endpoint, corpo, renovado);
                            }
                        }

                        Log.d(TAG, "PATCH " + endpoint + " -> " + codigo);

                    } catch (Exception erro) {
                        Log.w(TAG, "Falha ao atualizar na API", erro);
                    }
                });
    }

    private int patch(@NonNull String endpoint, @NonNull JSONObject corpo, @NonNull String token)
            throws IOException {

        HttpURLConnection conexao = (HttpURLConnection) new URL(endpoint).openConnection();

        try {
            conexao.setRequestMethod("PATCH");
            conexao.setRequestProperty("Content-Type", "application/json");
            conexao.setRequestProperty("Accept", "application/json");
            conexao.setRequestProperty("Authorization", "Bearer " + token);
            conexao.setDoOutput(true);
            conexao.setUseCaches(false);
            // Render dorme a API após inatividade (cold start de 70 a 150 s).
            conexao.setConnectTimeout(180_000);
            conexao.setReadTimeout(180_000);

            try (OutputStream saida = conexao.getOutputStream()) {
                saida.write(corpo.toString().getBytes(StandardCharsets.UTF_8));
            }

            return conexao.getResponseCode();

        } finally {
            conexao.disconnect();
        }
    }
}
