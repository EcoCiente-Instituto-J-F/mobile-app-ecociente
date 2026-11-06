package com.example.ecociente.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.json.JSONArray;
import org.json.JSONObject;

public final class DadosDemo {
    public interface Fonte {
        String token();
        int usuarioId();
        int condominioId();
        int cooperativaId();
        String perfil();
        JSONObject perfilCompleto();
        com.example.ecociente.model.ResultadoCadastro cadastrar(String nome, String email, String senha, java.util.Map<String, Object> dados);
    }
    @Nullable private static android.content.Context contexto;
    public static void instalarContexto(@NonNull android.content.Context app) { contexto = app.getApplicationContext(); }
    @NonNull public static android.content.Context contexto() {
        if (contexto == null) throw new IllegalStateException("Contexto demo ainda nao instalado");
        return contexto;
    }
    @NonNull public static com.example.ecociente.model.ResultadoCadastro cadastrar(
            @NonNull String nome, @NonNull String email, @NonNull String senha,
            @NonNull java.util.Map<String, Object> dados) {
        Fonte f = fonte;
        return f == null
                ? com.example.ecociente.model.ResultadoCadastro.erro("Demo indisponivel")
                : f.cadastrar(nome, email, senha, dados);
    }
    private static volatile Fonte fonte;
    private DadosDemo() {}
    public static void instalar(@NonNull Fonte nova) { fonte = nova; }
    @Nullable public static String token() {
        Fonte f = fonte;
        String t = f == null ? "" : f.token();
        return t == null || t.isEmpty() ? null : t;
    }
    public static int usuarioId() { Fonte f = fonte; return f == null ? 0 : f.usuarioId(); }
    public static int condominioId() { Fonte f = fonte; return f == null ? 0 : f.condominioId(); }
    public static int cooperativaId() { Fonte f = fonte; return f == null ? 0 : f.cooperativaId(); }
    @Nullable public static String perfil() { Fonte f = fonte; return f == null ? null : f.perfil(); }
    @Nullable public static String perfilJson() { Fonte f = fonte; return f == null ? null : f.perfilCompleto().toString(); }
    @Nullable public static String condominioNome() {
        Fonte f = fonte;
        if (f == null) return null;
        JSONArray lista = f.perfilCompleto().optJSONArray("condominios");
        JSONObject c = lista == null ? null : lista.optJSONObject(0);
        return c == null ? null : c.optString("nome", "");
    }
}
