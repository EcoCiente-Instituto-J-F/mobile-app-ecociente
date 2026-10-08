package com.example.ecociente.model;

import androidx.annotation.NonNull;

public enum TipoDocumento {
    CPF("###.###.###-##", 11, new int[] {10, 9, 8, 7, 6, 5, 4, 3, 2}),
    CNPJ("##.###.###/####-##", 14, new int[] {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2});

    private final String mascara;
    private final int tamanho;
    private final int[] pesosPrimeiroDigito;
    private final int[] pesosSegundoDigito;

    TipoDocumento(String mascara, int tamanho, int[] pesosPrimeiroDigito) {
        this.mascara = mascara;
        this.tamanho = tamanho;
        this.pesosPrimeiroDigito = pesosPrimeiroDigito;
        this.pesosSegundoDigito = new int[pesosPrimeiroDigito.length + 1];
        this.pesosSegundoDigito[0] = pesosPrimeiroDigito[0] + 1;
        System.arraycopy(
                pesosPrimeiroDigito, 0, pesosSegundoDigito, 1, pesosPrimeiroDigito.length);
    }

    @NonNull
    public String getMascara() {
        return mascara;
    }

    @NonNull
    public static String apenasDigitos(String valor) {
        return valor == null ? "" : valor.replaceAll("\\D", "");
    }

    public boolean valido(String valor) {

        String numeros = apenasDigitos(valor);

        if (numeros.length() != tamanho || numeros.chars().distinct().count() == 1) {
            return false;
        }

        int primeiro = digitoVerificador(numeros, pesosPrimeiroDigito);

        if (primeiro != Character.getNumericValue(numeros.charAt(tamanho - 2))) {
            return false;
        }

        int segundo = digitoVerificador(numeros, pesosSegundoDigito);

        return segundo == Character.getNumericValue(numeros.charAt(tamanho - 1));
    }

    private static int digitoVerificador(String numeros, int[] pesos) {

        int soma = 0;

        for (int i = 0; i < pesos.length; i++) {
            soma += Character.getNumericValue(numeros.charAt(i)) * pesos[i];
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}
