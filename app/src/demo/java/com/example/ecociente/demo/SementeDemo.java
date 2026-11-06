package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Estado inicial da demo. Hoje cobre conta, perfil e condomínio; cada área nova (pontos, feed, quiz...) acrescenta o seu bloco aqui.
public final class SementeDemo {

    // Muda quando o formato do estado muda: um arquivo de outra versão é descartado e a semente volta.
    static final int VERSAO = 1;

    // Têm de ser iguais aos DEMO_EMAIL e DEMO_SENHA do build type `demo` em app/build.gradle.kts, que a tela de login preenche.
    public static final String EMAIL = "morador@ecociente.demo";
    public static final String SENHA = "Demo@2026";

    public static final String UID = "demo-morador-42";
    public static final int USUARIO_ID = 42;
    public static final String NOME = "Marina Alves";
    public static final int CONDOMINIO_ID = 7;
    public static final String CONDOMINIO_NOME = "Condomínio Jardim das Palmeiras";

    private SementeDemo() {}

    @NonNull
    public static JSONObject criar(long agora) {
        try {
            JSONObject raiz = new JSONObject();

            raiz.put("versao", VERSAO);
            raiz.put("criadoEm", agora);
            raiz.put("conta", conta());
            raiz.put("me", me());
            raiz.put("preferencias", new JSONObject());

            return raiz;

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Credenciais e sessão. `logado` é o que ContaAtual enxerga; `token` é o JWT que o /auth/login entrega (vazio até o primeiro login).
    @NonNull
    private static JSONObject conta() throws JSONException {
        return new JSONObject()
                .put("uid", UID)
                .put("email", EMAIL)
                .put("senha", SENHA)
                .put("logado", false)
                .put("token", "");
    }

    // Forma do GET /api/usuarios/me, como o PerfilApiJson lê.
    @NonNull
    private static JSONObject me() throws JSONException {
        JSONObject endereco =
                new JSONObject()
                        .put("cep", "01310100")
                        .put("estado", "SP")
                        .put("cidade", "São Paulo")
                        .put("logradouro", "Avenida Paulista")
                        .put("numero", "1000")
                        .put("complemento", "Torre B, apto 42");

        JSONObject telefone = new JSONObject().put("numero", "11987654321").put("ativo", true);

        JSONObject condominio =
                new JSONObject()
                        .put("id", CONDOMINIO_ID)
                        .put("nome", CONDOMINIO_NOME)
                        .put("ativo", true)
                        .put("sindicoResponsavel", false)
                        .put("tipoCondominio", "Residencial")
                        .put("urlFoto", "")
                        .put("endereco", endereco)
                        .put("torres", new JSONArray().put(new JSONObject().put("id", 2).put("nome", "Torre B")))
                        .put("vinculo", new JSONObject().put("aprovado", true));

        return new JSONObject()
                .put("id", USUARIO_ID)
                .put("nome", NOME)
                .put("email", EMAIL)
                .put("cpf", "111.444.777-35")
                .put("urlAvatar", "")
                .put("perfil", "MORADOR")
                .put("telefones", new JSONArray().put(telefone))
                .put("endereco", endereco)
                .put("condominios", new JSONArray().put(condominio));
    }
}
