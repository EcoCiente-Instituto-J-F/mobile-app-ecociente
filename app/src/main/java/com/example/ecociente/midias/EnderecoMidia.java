package com.example.ecociente.midias;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/** Somente entrega publica HTTPS. Nunca usa a Admin API dentro do Android. */
public final class EnderecoMidia {
    private EnderecoMidia() { }

    public static String cloudinary(Midia midia, boolean foto) {
        if (!"cloudinary".equals(midia.origem) || !midia.cloudName.matches("[A-Za-z0-9_-]+"))
            throw new IllegalArgumentException("Origem Cloudinary invalida");
        if (midia.caminho.startsWith("/") || midia.caminho.contains("..") || midia.caminho.contains(":"))
            throw new IllegalArgumentException("Public ID invalido");
        // O Public ID pode conter pontos e extensoes como parte literal do identificador.
        // No Cloudinary, se o Public ID for "imagem.png", a URL de entrega será
        // "imagem.png.png" quando o formato de entrega também for PNG.
        // Portanto, preservamos exatamente o valor cadastrado no catálogo.
        String caminhoPublico = midia.caminho;
        StringBuilder id = new StringBuilder();
        for (String parte : caminhoPublico.split("/")) {
            if (parte.isEmpty()) throw new IllegalArgumentException("Public ID invalido");
            if (id.length() > 0) id.append('/');
            try { id.append(URLEncoder.encode(parte, StandardCharsets.UTF_8.name()).replace("+", "%20")); }
            catch (java.io.UnsupportedEncodingException impossible) { throw new IllegalStateException(impossible); }
        }
        boolean gif = midia.animada && "gif".equalsIgnoreCase(midia.formato);
        String transformacao = gif ? "c_fit,w_280,h_280,f_gif"
                : foto ? "c_fill,g_auto,w_960,h_540,q_auto,f_webp" : "c_fit,w_640,h_640,q_auto,f_png";
        String extensao = gif ? ".gif" : foto ? ".webp" : ".png";
        String versao = midia.versao > 1 ? "v" + midia.versao + "/" : "";
        return "https://res.cloudinary.com/" + midia.cloudName + "/image/upload/"
                + transformacao + "/" + versao + id + extensao;
    }

    /** Remove apenas a extensao final conhecida, preservando pastas e pontos no nome. */
    private static String removerExtensao(String caminho) {
        int ultimaBarra = caminho.lastIndexOf('/');
        int ultimoPonto = caminho.lastIndexOf('.');
        if (ultimoPonto <= ultimaBarra) return caminho;

        String extensao = caminho.substring(ultimoPonto + 1).toLowerCase(java.util.Locale.ROOT);
        if ("png".equals(extensao) || "jpg".equals(extensao) || "jpeg".equals(extensao)
                || "webp".equals(extensao) || "gif".equals(extensao) || "svg".equals(extensao)) {
            return caminho.substring(0, ultimoPonto);
        }
        return caminho;
    }
}
