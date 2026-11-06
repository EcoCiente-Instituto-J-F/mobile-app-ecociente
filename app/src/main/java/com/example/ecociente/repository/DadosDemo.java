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
