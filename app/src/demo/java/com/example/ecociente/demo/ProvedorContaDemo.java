package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.example.ecociente.model.PerfilUsuario;
import com.example.ecociente.model.PreferenciasNotificacao;
import com.example.ecociente.model.ResultadoApi;
import com.example.ecociente.model.ResultadoLogin;
import com.example.ecociente.model.TipoPerfil;
import com.example.ecociente.model.UsuarioLogado;
import com.example.ecociente.repository.ContaAtual;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// A conta no lugar do Firebase: sessão, perfil e preferências ficam no EstadoDemo, o mesmo que a API falsa lê. Sair e excluir a conta recomeçam da semente.
public final class ProvedorContaDemo implements ContaAtual.Provedor {

    private static final String ERRO_SALVAR = "Não foi possível salvar. Tente novamente.";

    private final EstadoDemo estado;
    private final Runnable aoResetar;

    // `aoResetar` roda antes de o estado voltar à semente: é o momento de limpar o que a demo guardou fora dele (SharedPreferences, lembretes).
    public ProvedorContaDemo(@NonNull EstadoDemo estado, @NonNull Runnable aoResetar) {
        this.estado = estado;
        this.aoResetar = aoResetar;
    }

    @Nullable
    @Override
    public UsuarioLogado usuario() {
        synchronized (estado) {
            JSONObject raiz = estado.raiz();

            if (!ContaDemo.logado(raiz)) {
                return null;
            }

            JSONObject conta = ContaDemo.conta(raiz);

            return new UsuarioLogado(
                    conta.optString("uid", ""),
                    conta.optString("email", ""),
                    ContaDemo.me(raiz).optString("nome", ""));
        }
    }

    @NonNull
    @Override
    public ResultadoLogin entrar(@NonNull String email, @NonNull String senha) {
        synchronized (estado) {
            JSONObject raiz = estado.raiz();

            if (!ContaDemo.credenciaisConferem(raiz, email, senha)) {
                return ResultadoLogin.erro("E-mail ou senha incorretos");
            }

            try {
                ContaDemo.conta(raiz).put("logado", true);

            } catch (JSONException erro) {
                throw new IllegalStateException(erro);
            }

            estado.salvar();

            return ResultadoLogin.sucesso();
        }
    }

    @Override
    public void sair() {
        recomecar();
    }

    @Nullable
    @Override
    public PerfilUsuario perfil(@NonNull String uid) {
        synchronized (estado) {
            JSONObject raiz = estado.raiz();

            if (!donoDaSessao(raiz, uid)) {
                return null;
            }

            JSONObject me = ContaDemo.me(raiz);
            JSONObject endereco = me.optJSONObject("endereco");
            JSONArray condominios = me.optJSONArray("condominios");
            JSONObject condominio = condominios == null ? null : condominios.optJSONObject(0);

            PerfilUsuario perfil = new PerfilUsuario();

            perfil.setNome(me.optString("nome", ""));
            perfil.setEmail(ContaDemo.conta(raiz).optString("email", ""));
            perfil.setTelefone(ContaDemo.telefoneFormatado(ContaDemo.telefone(me)));
            perfil.setCpf(me.optString("cpf", ""));
            perfil.setFotoUrl(me.optString("urlAvatar", ""));
            perfil.setFotoCondominioUrl(condominio == null ? "" : condominio.optString("urlFoto", ""));
            perfil.setTipoPerfil(TipoPerfil.USUARIO);
            perfil.setPossuiCodigoCondominio(true);

            if (endereco != null) {
                perfil.setEndereco(endereco.optString("logradouro", ""));
                perfil.setNumero(endereco.optString("numero", ""));
                perfil.setComplemento(endereco.optString("complemento", ""));
                perfil.setCidade(endereco.optString("cidade", ""));
                perfil.setEstado(endereco.optString("estado", ""));
                perfil.setCep(endereco.optString("cep", ""));
            }

            return perfil;
        }
    }

    // Campos que o /me não guarda (tipoPerfil, tipoUsuario, possuiCodigoCondominio, que a home sincroniza) dão certo sem efeito: o perfil do morador é fixo.
    @NonNull
    @Override
    public ResultadoApi atualizarCampo(@NonNull String uid, @NonNull String campo, @NonNull Object valor) {
        synchronized (estado) {
            JSONObject raiz = estado.raiz();

            if (!donoDaSessao(raiz, uid)) {
                return ResultadoApi.erro(ERRO_SALVAR);
            }

            if (ContaDemo.aplicarCampo(ContaDemo.me(raiz), campo, valor)) {
                estado.salvar();
            }

            return ResultadoApi.sucesso();
        }
    }

    @NonNull
    @Override
    public PreferenciasNotificacao preferencias(@NonNull String uid) {
        synchronized (estado) {
            JSONObject guardadas = estado.raiz().optJSONObject("preferencias");
            PreferenciasNotificacao preferencias = new PreferenciasNotificacao();

            if (guardadas == null) {
                return preferencias;
            }

            if (guardadas.has(PreferenciasNotificacao.CHAVE_NOTIFICACOES_GERAIS)) {
                preferencias.setNotificacoesGerais(
                        guardadas.optBoolean(PreferenciasNotificacao.CHAVE_NOTIFICACOES_GERAIS, true));
            }

            if (guardadas.has(PreferenciasNotificacao.CHAVE_LEMBRETES_AVISOS)) {
                preferencias.setLembretesAvisos(
                        guardadas.optBoolean(PreferenciasNotificacao.CHAVE_LEMBRETES_AVISOS, true));
            }

            if (guardadas.has(PreferenciasNotificacao.CHAVE_NOVAS_ATIVIDADES)) {
                preferencias.setNovasAtividades(
                        guardadas.optBoolean(PreferenciasNotificacao.CHAVE_NOVAS_ATIVIDADES, true));
            }

            if (guardadas.has(PreferenciasNotificacao.CHAVE_DICAS_SUSTENTAVEIS)) {
                preferencias.setDicasSustentaveis(
                        guardadas.optBoolean(PreferenciasNotificacao.CHAVE_DICAS_SUSTENTAVEIS, true));
            }

            return preferencias;
        }
    }

    @NonNull
    @Override
    public ResultadoApi atualizarPreferencia(@NonNull String uid, @NonNull String chave, boolean valor) {
        synchronized (estado) {
            JSONObject raiz = estado.raiz();

            if (!donoDaSessao(raiz, uid)) {
                return ResultadoApi.erro(ERRO_SALVAR);
            }

            try {
                JSONObject guardadas = raiz.optJSONObject("preferencias");

                if (guardadas == null) {
                    guardadas = new JSONObject();
                    raiz.put("preferencias", guardadas);
                }

                guardadas.put(chave, valor);

            } catch (JSONException erro) {
                throw new IllegalStateException(erro);
            }

            estado.salvar();

            return ResultadoApi.sucesso();
        }
    }

    @NonNull
    @Override
    public ResultadoApi alterarSenha(@NonNull String senhaAtual, @NonNull String novaSenha) {
        synchronized (estado) {
            JSONObject raiz = estado.raiz();

            if (!ContaDemo.logado(raiz)) {
                return ResultadoApi.erro("Não foi possível identificar o usuário.");
            }

            JSONObject conta = ContaDemo.conta(raiz);

            if (!senhaAtual.equals(conta.optString("senha", ""))) {
                return ResultadoApi.erro("Senha atual incorreta.");
            }

            if (novaSenha.isEmpty()) {
                return ResultadoApi.erro("Não foi possível atualizar a senha.");
            }

            try {
                conta.put("senha", novaSenha);

            } catch (JSONException erro) {
                throw new IllegalStateException(erro);
            }

            estado.salvar();

            return ResultadoApi.sucesso();
        }
    }

    @NonNull
    @Override
    public ResultadoApi excluirConta(@NonNull String senhaAtual) {
        synchronized (estado) {
            JSONObject raiz = estado.raiz();

            if (!ContaDemo.logado(raiz)) {
                return ResultadoApi.erro("Não foi possível identificar o usuário.");
            }

            if (!senhaAtual.equals(ContaDemo.conta(raiz).optString("senha", ""))) {
                return ResultadoApi.erro("Senha incorreta.");
            }

            recomecar();

            return ResultadoApi.sucesso();
        }
    }

    // O estado volta à semente mesmo que a limpeza do que está fora dele falhe.
    private void recomecar() {
        synchronized (estado) {
            try {
                aoResetar.run();

            } finally {
                estado.resetar();
            }
        }
    }

    private static boolean donoDaSessao(@NonNull JSONObject raiz, @NonNull String uid) {
        return ContaDemo.logado(raiz) && uid.equals(ContaDemo.conta(raiz).optString("uid", ""));
    }
}
