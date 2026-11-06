package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Leitura e escrita da conta e do perfil dentro do JSON do estado. Quem chama já está sob `synchronized (estado)` e chama `estado.salvar()` depois.
final class ContaDemo {

    private ContaDemo() {}

    @NonNull
    static JSONObject conta(@NonNull JSONObject raiz) {
        JSONObject conta = raiz.optJSONObject("conta");

        if (conta == null) {
            throw new IllegalStateException("Estado da demo sem conta");
        }

        return conta;
    }

    // Corpo do GET /api/usuarios/me.
    @NonNull
    static JSONObject me(@NonNull JSONObject raiz) {
        JSONObject me = raiz.optJSONObject("me");

        if (me == null) {
            throw new IllegalStateException("Estado da demo sem perfil");
        }

        return me;
    }

    static boolean logado(@NonNull JSONObject raiz) {
        return conta(raiz).optBoolean("logado", false);
    }

    @NonNull
    static String normalizarEmail(@Nullable String email) {
        return email == null ? "" : email.trim().toLowerCase(java.util.Locale.ROOT);
    }

    static boolean credenciaisConferem(@NonNull JSONObject raiz, @Nullable String email, @Nullable String senha) {
        JSONObject conta = conta(raiz);

        return senha != null
                && normalizarEmail(email).equals(normalizarEmail(conta.optString("email", "")))
                && senha.equals(conta.optString("senha", ""));
    }

    @NonNull
    static String telefone(@NonNull JSONObject me) {
        JSONArray telefones = me.optJSONArray("telefones");
        JSONObject primeiro = telefones == null ? null : telefones.optJSONObject(0);

        return primeiro == null ? "" : primeiro.optString("numero", "");
    }

    // (11) 98765-4321 para 11 dígitos, (11) 8765-4321 para 10; qualquer outra coisa fica como veio.
    @NonNull
    static String telefoneFormatado(@NonNull String digitos) {
        if (digitos.length() == 11) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 7) + "-" + digitos.substring(7);
        }

        if (digitos.length() == 10) {
            return "(" + digitos.substring(0, 2) + ") " + digitos.substring(2, 6) + "-" + digitos.substring(6);
        }

        return digitos;
    }

    // Aplica um campo do perfil do app (as chaves de PerfilRepository.CAMPO_*) no formato do /me. false quando o /me não tem onde guardar o campo (CPF da cooperativa, tipoPerfil...).
    static boolean aplicarCampo(@NonNull JSONObject me, @NonNull String campo, @NonNull Object valor) {
        String texto = String.valueOf(valor);

        try {
            switch (campo) {
                case "nome":
                    me.put("nome", texto.trim());
                    return true;

                case "cpf":
                    me.put("cpf", texto);
                    return true;

                case "fotoUrl":
                    me.put("urlAvatar", texto);
                    return true;

                case "telefone":
                    definirTelefone(me, texto.replaceAll("\\D", ""));
                    return true;

                case "fotoCondominioUrl":
                    JSONArray condominios = me.optJSONArray("condominios");
                    JSONObject condominio = condominios == null ? null : condominios.optJSONObject(0);

                    if (condominio == null) {
                        return false;
                    }

                    condominio.put("urlFoto", texto);
                    return true;

                case "endereco":
                    enderecoDe(me).put("logradouro", texto);
                    return true;

                case "cep":
                case "numero":
                case "complemento":
                case "cidade":
                case "estado":
                    enderecoDe(me).put(campo, texto);
                    return true;

                default:
                    return false;
            }
        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    private static void definirTelefone(@NonNull JSONObject me, @NonNull String digitos) throws JSONException {
        JSONArray telefones = me.optJSONArray("telefones");

        if (telefones == null) {
            telefones = new JSONArray();
            me.put("telefones", telefones);
        }

        JSONObject primeiro = telefones.optJSONObject(0);

        if (primeiro == null) {
            primeiro = new JSONObject().put("ativo", true);
            telefones.put(0, primeiro);
        }

        primeiro.put("numero", digitos);
    }

    @NonNull
    static JSONObject enderecoDe(@NonNull JSONObject me) throws JSONException {
        JSONObject endereco = me.optJSONObject("endereco");

        if (endereco == null) {
            endereco = new JSONObject();
            me.put("endereco", endereco);
        }

        return endereco;
    }
}
