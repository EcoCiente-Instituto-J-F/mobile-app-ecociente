package com.example.ecociente.model;

// Id numérico do condomínio do usuário, que a ds-pontuacao-api exige no ranking.
// Nenhuma API entrega esse id ao app ainda (falta GET /api/usuarios/me ou o id no JWT).
// Enquanto isso, preencha com o id real do banco:
//   SELECT id_condominio FROM tb_condominios WHERE codigo_acesso = 'ECO0001';
// 0 = não configurado: o app não chama a API e segue com os dados que já tem.
public final class CondominioAtual {

    public static final int ID = 1;

    private CondominioAtual() {}
}
