package com.example.ecociente.repository;

import java.text.Normalizer;
import java.util.Locale;

public final class EkoRespostaLocal {
    private EkoRespostaLocal() {}

    public static String responder(String pergunta) {
        String texto = Normalizer.normalize(pergunta == null ? "" : pergunta, Normalizer.Form.NFD)
                .replaceAll("\\p{M}+", "").toLowerCase(Locale.ROOT);
        String resposta;
        if (texto.contains("compost") || texto.contains("organico") || texto.contains("casca")) {
            resposta = "Resíduos orgânicos podem ser destinados à compostagem, quando há estrutura adequada. Equilibre materiais úmidos e secos, mantenha a composteira arejada e evite misturar plástico ou vidro.";
        } else if (texto.contains("pilha") || texto.contains("bateria") || texto.contains("eletronico")) {
            resposta = "Pilhas, baterias e eletrônicos precisam de pontos específicos de recebimento. Não misture com recicláveis comuns nem com resíduos orgânicos. Consulte o serviço de coleta da sua cidade.";
        } else if (texto.contains("plastico") || texto.contains("garrafa") || texto.contains("pet")) {
            resposta = "Esvazie as embalagens plásticas e, quando possível, separe-as limpas e secas. A aceitação de cada tipo de plástico depende da coleta seletiva local.";
        } else if (texto.contains("vidro") || texto.contains("caco")) {
            resposta = "Separe o vidro conforme as regras da coleta local. Se estiver quebrado, proteja os cacos e identifique a embalagem para evitar acidentes.";
        } else if (texto.contains("papel") || texto.contains("papelao")) {
            resposta = "Papel e papelão limpos e secos costumam ser aceitos na coleta seletiva. Separe os materiais contaminados por alimentos, óleo ou produtos químicos.";
        } else if (texto.contains("metal") || texto.contains("lata") || texto.contains("aluminio")) {
            resposta = "Latas e embalagens metálicas vazias podem seguir para a coleta seletiva, conforme as regras locais. Evite deixar restos de alimentos dentro das embalagens.";
        } else if (texto.contains("cooperativa") || texto.contains("perto") || texto.contains("proxim")) {
            resposta = "Não consigo consultar endereços ou horários de cooperativas sem a API. Confira os pontos de coleta oficiais da sua cidade antes de se deslocar.";
        } else {
            resposta = "Posso oferecer orientações gerais sobre reciclagem, compostagem e descarte de resíduos. Para respostas personalizadas, informações da sua conta ou dados atualizados, precisamos restabelecer a conexão com a IA.";
        }
        return "Eko em modo offline — orientação local, não gerada pela IA.\n\n"
                + resposta + "\n\nQuando a conexão voltar, envie a pergunta novamente para receber uma resposta completa.";
    }
}
