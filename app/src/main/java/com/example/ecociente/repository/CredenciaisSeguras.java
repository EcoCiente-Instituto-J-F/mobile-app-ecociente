package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;

final class CredenciaisSeguras {

    private static final String TAG = "CredenciaisSeguras";
    private static final String PROVEDOR = "AndroidKeyStore";
    private static final String ALIAS = "ecociente_credenciais";
    private static final String TRANSFORMACAO = "AES/GCM/NoPadding";
    private static final String PREFERENCIAS = "credenciais_seguras";
    private static final String CHAVE_DADOS = "dados";
    private static final String SEPARADOR_PARTES = ":";
    private static final String SEPARADOR_CAMPOS = "\n";
    private static final int BITS_TAG = 128;

    private CredenciaisSeguras() {}

    static void salvar(@NonNull Context contexto, @NonNull String email, @NonNull String senha) {

        String cifrado = cifrar(email + SEPARADOR_CAMPOS + senha);

        if (cifrado != null) {
            preferencias(contexto).edit().putString(CHAVE_DADOS, cifrado).apply();
        }
    }

    // Texto cifrado (AES-256-GCM, chave no Keystore) no formato "iv:dados"; null se não deu.
    @Nullable
    static String cifrar(@NonNull String texto) {

        try {
            Cipher cifra = Cipher.getInstance(TRANSFORMACAO);

            cifra.init(Cipher.ENCRYPT_MODE, chave());

            byte[] cifrado = cifra.doFinal(texto.getBytes(StandardCharsets.UTF_8));

            return codificar(cifra.getIV()) + SEPARADOR_PARTES + codificar(cifrado);

        } catch (GeneralSecurityException | IOException erro) {
            Log.w(TAG, "Não foi possível cifrar", erro);

            return null;
        }
    }

    // Texto original de um valor feito por `cifrar`; null se estiver fora do formato ou inválido.
    @Nullable
    static String decifrar(@NonNull String valor) {

        try {
            String[] partes = valor.split(SEPARADOR_PARTES);

            Cipher cifra = Cipher.getInstance(TRANSFORMACAO);

            cifra.init(
                    Cipher.DECRYPT_MODE,
                    chave(),
                    new GCMParameterSpec(BITS_TAG, Base64.decode(partes[0], Base64.NO_WRAP)));

            return new String(cifra.doFinal(Base64.decode(partes[1], Base64.NO_WRAP)), StandardCharsets.UTF_8);

        } catch (GeneralSecurityException | IOException | IllegalArgumentException | ArrayIndexOutOfBoundsException erro) {
            Log.w(TAG, "Valor cifrado inválido", erro);

            return null;
        }
    }

    @Nullable
    static String[] ler(@NonNull Context contexto) {

        String dados = preferencias(contexto).getString(CHAVE_DADOS, null);

        if (dados == null) {
            return null;
        }

        String texto = decifrar(dados);

        if (texto == null) {
            Log.w(TAG, "Credenciais guardadas inválidas; descartando");

            limpar(contexto);

            return null;
        }

        return texto.split(SEPARADOR_CAMPOS, 2);
    }

    static void limpar(@NonNull Context contexto) {
        preferencias(contexto).edit().remove(CHAVE_DADOS).apply();
    }

    @NonNull
    private static SharedPreferences preferencias(@NonNull Context contexto) {
        return contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
    }

    @NonNull
    private static String codificar(@NonNull byte[] bytes) {
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    @NonNull
    private static SecretKey chave() throws GeneralSecurityException, IOException {

        KeyStore repositorio = KeyStore.getInstance(PROVEDOR);

        repositorio.load(null);

        if (repositorio.containsAlias(ALIAS)) {
            return (SecretKey) repositorio.getKey(ALIAS, null);
        }

        KeyGenerator gerador = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, PROVEDOR);

        gerador.init(
                new KeyGenParameterSpec.Builder(
                                ALIAS, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                        .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                        .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                        .setKeySize(256)
                        .build());

        return gerador.generateKey();
    }
}
