package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import org.json.JSONException;
import org.json.JSONObject;

// Upload de foto do Cloudinary: POST api.cloudinary.com/v1_1/{cloud}/image/upload, multipart com `upload_preset` e `file`. Sem JWT (o app não manda Authorization). Serve o avatar e a foto do descarte. Só os bytes da parte `file` vão para o armazém; a resposta traz a URL dele em `secure_url`.
final class RotasUpload implements RotasDemo {

    private static final String HOST = "api.cloudinary.com";

    private static final byte[] CRLF = {'\r', '\n'};
    private static final byte[] FIM_DOS_CABECALHOS = {'\r', '\n', '\r', '\n'};

    private final EstadoDemo estado;

    RotasUpload(@NonNull EstadoDemo estado) {
        this.estado = estado;
    }

    @Nullable
    @Override
    public RespostaDemo tratar(@NonNull RequisicaoDemo r) {
        if (!HOST.equals(r.host)
                || !"POST".equals(r.metodo)
                || !r.caminho.matches("/v1_1/[^/]+/image/upload")) {
            return null;
        }

        // O limite é a primeira linha do corpo ("--limite"); o Content-Type não chega até aqui.
        int fimDaPrimeiraLinha = indice(r.corpo, CRLF, 0);

        if (fimDaPrimeiraLinha < 3 || r.corpo[0] != '-' || r.corpo[1] != '-') {
            return erro("Corpo multipart inválido");
        }

        byte[] limite = Arrays.copyOfRange(r.corpo, 0, fimDaPrimeiraLinha);
        byte[] separador = concatenar(CRLF, limite);

        byte[] arquivo = null;
        boolean temPreset = false;

        // Cada parte começa depois de "limite\r\n"; a primeira não tem o CRLF antes do limite.
        int inicio = fimDaPrimeiraLinha + CRLF.length;

        while (inicio < r.corpo.length) {
            int fimDosCabecalhos = indice(r.corpo, FIM_DOS_CABECALHOS, inicio);
            int comecoDoConteudo = fimDosCabecalhos + FIM_DOS_CABECALHOS.length;
            int fimDoConteudo = fimDosCabecalhos < 0 ? -1 : indice(r.corpo, separador, comecoDoConteudo);

            if (fimDosCabecalhos < 0 || fimDoConteudo < 0) {
                break;
            }

            // Só os cabeçalhos (texto) viram String; o conteúdo da parte fica em bytes.
            String cabecalhos =
                    new String(r.corpo, inicio, fimDosCabecalhos - inicio, StandardCharsets.UTF_8);

            if (cabecalhos.contains("name=\"file\"")) {
                arquivo = Arrays.copyOfRange(r.corpo, comecoDoConteudo, fimDoConteudo);

            } else if (cabecalhos.contains("name=\"upload_preset\"") && fimDoConteudo > comecoDoConteudo) {
                temPreset = true;
            }

            // Depois do separador vem "\r\n" (próxima parte) ou "--" (fim).
            inicio = fimDoConteudo + separador.length;

            if (inicio + 1 < r.corpo.length && r.corpo[inicio] == '-' && r.corpo[inicio + 1] == '-') {
                break;
            }

            inicio += CRLF.length;
        }

        if (!temPreset) {
            return erro("Upload preset must be specified when using unsigned upload");
        }

        if (arquivo == null || arquivo.length == 0) {
            return erro("Missing required parameter - file");
        }

        String url = estado.gravarFoto(arquivo);

        try {
            return new RespostaDemo(
                    200,
                    new JSONObject()
                            .put("secure_url", url)
                            .put("url", url)
                            .put("format", "jpg")
                            .put("bytes", arquivo.length)
                            .toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Erro do Cloudinary: { "error": { "message": ... } }.
    @NonNull
    private static RespostaDemo erro(@NonNull String mensagem) {
        try {
            return new RespostaDemo(
                    400, new JSONObject().put("error", new JSONObject().put("message", mensagem)).toString());

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    @NonNull
    private static byte[] concatenar(@NonNull byte[] a, @NonNull byte[] b) {
        byte[] saida = Arrays.copyOf(a, a.length + b.length);

        System.arraycopy(b, 0, saida, a.length, b.length);

        return saida;
    }

    // Primeira posição de `procurado` em `dados` a partir de `de`, ou -1.
    private static int indice(@NonNull byte[] dados, @NonNull byte[] procurado, int de) {
        for (int i = Math.max(0, de); i <= dados.length - procurado.length; i++) {
            int j = 0;

            while (j < procurado.length && dados[i + j] == procurado[j]) {
                j++;
            }

            if (j == procurado.length) {
                return i;
            }
        }

        return -1;
    }
}
