package com.example.ecociente.demo;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
final class UsuariosMultiperfilDemo {
    static final String SENHA = "Ecociente2026.";
    static final String MORADOR = "morador@ecociente.com.br";
    static final String COMUM = "comum@ecociente.com.br";
    static final String SINDICO = "sindico@ecociente.com.br";
    static final String COOPERATIVA = "cooperativa@ecociente.com.br";
    static final String[] CAMPOS = {"conta", "me", "preferencias", "avisosLidos", "pontuacao", "educacao"};
    private UsuariosMultiperfilDemo() {}
    static void configurar(JSONObject raiz) throws JSONException {
        JSONObject perfis = new JSONObject();
        perfis.put(MORADOR, registro(raiz, MORADOR, 42, "MORADOR", "Marina Alves"));
        perfis.put(COMUM, registro(raiz, COMUM, 43, "USUARIO_COMUM", "Lucas Ferreira"));
        perfis.put(SINDICO, registro(raiz, SINDICO, 44, "SINDICO", "Ana Ribeiro"));
        perfis.put(COOPERATIVA, registro(raiz, COOPERATIVA, 45, "COOPERATIVA", "Paulo Nascimento"));
        raiz.put("perfisDemo", perfis);
        raiz.put("usuarioAtivoDemo", MORADOR);
        aplicar(raiz, perfis.getJSONObject(MORADOR));
    }
    private static JSONObject registro(JSONObject raiz, String email, int id, String tipo, String nome) throws JSONException {
        JSONObject item = snapshot(raiz);
        item.getJSONObject("conta").put("email", email).put("uid", "demo-" + id).put("senha", SENHA).put("token", "").put("logado", false);
        JSONObject me = item.getJSONObject("me");
        me.put("id", id).put("nome", nome).put("email", email).put("perfil", tipo);
        JSONArray condominios = me.optJSONArray("condominios");
        if ("USUARIO_COMUM".equals(tipo) || "COOPERATIVA".equals(tipo)) {
            me.put("condominios", new JSONArray());
        } else if (condominios != null && condominios.length() > 0) {
            condominios.getJSONObject(0).put("sindicoResponsavel", "SINDICO".equals(tipo));
        }
        if ("COOPERATIVA".equals(tipo)) {
            me.put("cooperativa", new JSONObject().put("id", 3).put("nome", "Cooperativa EcoCiente").put("email", email));
        } else {
            me.remove("cooperativa");
        }
        if (!"MORADOR".equals(tipo)) {
            JSONArray participantes = item.getJSONObject("pontuacao").getJSONArray("participantes");
            for (int i = 0; i < participantes.length(); i++) {
                JSONObject p = participantes.getJSONObject(i);
                if (p.optInt("usuarioId") == 42) p.put("usuarioId", id);
            }
        }
        return item;
    }
    static JSONObject snapshot(JSONObject raiz) throws JSONException {
        JSONObject dados = new JSONObject();
        for (String chave : CAMPOS) {
            Object valor = raiz.get(chave);
            if (valor instanceof JSONObject) valor = new JSONObject(valor.toString());
            else if (valor instanceof JSONArray) valor = new JSONArray(valor.toString());
            dados.put(chave, valor);
        }
        return dados;
    }
    static void aplicar(JSONObject destino, JSONObject origem) throws JSONException {
        JSONObject dados = snapshot(origem);
        for (String chave : CAMPOS) destino.put(chave, dados.get(chave));
    }
    static int usuarioId(JSONObject raiz) { return ContaDemo.me(raiz).optInt("id", 0); }
    static int condominioId(JSONObject raiz) {
        JSONArray lista = ContaDemo.me(raiz).optJSONArray("condominios");
        JSONObject cond = lista == null ? null : lista.optJSONObject(0);
        return cond == null ? 0 : cond.optInt("id", 0);
    }
}
