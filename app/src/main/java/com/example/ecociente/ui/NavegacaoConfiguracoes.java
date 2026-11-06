package com.example.ecociente.ui;

import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.example.ecociente.repository.AutenticacaoExternaRepository;
import com.example.ecociente.views.ChatActivity;
import java.util.Locale;

/** Mantém as abas e a animação da navegação presentes nas telas do Figma. */
public final class NavegacaoConfiguracoes {
    private NavegacaoConfiguracoes() { }

    public static void instalar(@NonNull AppCompatActivity activity) {
        BarraNavegacaoView barra = activity.findViewById(com.example.ecociente.R.id.barraNavegacao);
        if (barra == null) return;
        String salvo = AutenticacaoExternaRepository.perfilSalvo(activity);
        String perfil = salvo == null ? "" : salvo.toUpperCase(Locale.ROOT);
        boolean sindico = perfil.contains("SINDICO") || perfil.contains("SÍNDICO");
        boolean cooperativa = perfil.contains("COOPERATIVA");
        boolean morador = perfil.contains("MORADOR");

        ItemBarra[] itens = sindico ? ItensBarra.sindico()
                : cooperativa ? ItensBarra.cooperativa()
                : morador ? ItensBarra.morador() : ItensBarra.usuario();
        barra.configurar(itens);
        int selecionado = sindico || cooperativa || morador
                ? ItensBarra.NENHUM : ItensBarra.QUARTO;
        Navegacao.marcarNaBarra(activity, barra, selecionado);
        barra.setOnAssistenteClickListener(v -> activity.startActivity(new Intent(activity, ChatActivity.class)));
        barra.setOnItemClickListener(indice -> {
            if (sindico) Navegacao.irParaItemDoSindico(activity, indice, selecionado);
            else if (cooperativa) Navegacao.irParaItemDaCooperativa(activity, indice, selecionado);
            else Navegacao.irParaItemDoUsuario(activity, indice, selecionado, morador);
        });
    }
}
