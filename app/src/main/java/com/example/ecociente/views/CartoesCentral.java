package com.example.ecociente.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import com.example.ecociente.R;
import com.example.ecociente.model.Aviso;
import com.example.ecociente.model.CategoriaAviso;
import com.example.ecociente.model.Notificacao;
import com.example.ecociente.model.SindicoAviso;
import com.example.ecociente.util.FormatoDataApi;

// Cartões da central: notificações e avisos (de qualquer perfil) vivem na mesma lista.
public final class CartoesCentral {

    private CartoesCentral() {}

    @NonNull
    public static View notificacao(
            @NonNull LayoutInflater inflador, @NonNull ViewGroup pai, @NonNull Notificacao notificacao) {

        View cartao = inflador.inflate(R.layout.item_central_notificacao, pai, false);

        ((TextView) cartao.findViewById(R.id.tituloCentralNotificacao)).setText(notificacao.titulo);
        ((TextView) cartao.findViewById(R.id.mensagemCentralNotificacao)).setText(notificacao.mensagem);
        ((TextView) cartao.findViewById(R.id.horaCentralNotificacao)).setText(notificacao.hora);

        return cartao;
    }

    @NonNull
    public static View aviso(
            @NonNull LayoutInflater inflador, @NonNull ViewGroup pai, @NonNull Aviso aviso) {

        View cartao =
                montarAviso(
                        inflador,
                        pai,
                        aviso.titulo,
                        aviso.mensagem,
                        aviso.remetente,
                        FormatoDataApi.relativa(aviso.enviadoEm, System.currentTimeMillis()),
                        aviso.deCooperativa,
                        !aviso.isLida());

        aplicarChip(cartao.findViewById(R.id.chipCategoriaAviso), aviso.categoria);

        return cartao;
    }

    @NonNull
    public static View avisoDoSindico(
            @NonNull LayoutInflater inflador, @NonNull ViewGroup pai, @NonNull SindicoAviso aviso) {

        View cartao =
                montarAviso(
                        inflador,
                        pai,
                        aviso.getTitulo(),
                        aviso.getMensagem(),
                        aviso.getAutor(),
                        aviso.getMomento(),
                        aviso.getAutor().startsWith("Cooperativa"),
                        aviso.isNovo());

        TextView chip = cartao.findViewById(R.id.chipCategoriaAviso);

        aplicarChip(chip, categoriaPorRotulo(chip.getContext(), aviso.getCategoria()));

        // Rascunho não é uma categoria: aparece só como etiqueta no cartão.
        if (aviso.isRascunho()) {
            chip.setText(aviso.getCategoria());
        }

        return cartao;
    }

    @NonNull
    private static View montarAviso(
            @NonNull LayoutInflater inflador,
            @NonNull ViewGroup pai,
            @NonNull String titulo,
            @NonNull String mensagem,
            @NonNull String remetente,
            @NonNull String tempo,
            boolean deCooperativa,
            boolean naoLido) {

        View cartao = inflador.inflate(R.layout.item_aviso, pai, false);

        ((ImageView) cartao.findViewById(R.id.iconeRemetenteAviso))
                .setImageResource(deCooperativa ? R.drawable.ic_reciclar : R.drawable.ic_perfil_cooperativa);

        ((TextView) cartao.findViewById(R.id.textoTituloAviso)).setText(titulo);
        ((TextView) cartao.findViewById(R.id.textoPreviaAviso)).setText(mensagem);
        ((TextView) cartao.findViewById(R.id.textoRemetenteAviso)).setText(remetente);
        ((TextView) cartao.findViewById(R.id.textoTempoAviso)).setText(tempo);

        cartao.findViewById(R.id.pontoAvisoNaoLido).setVisibility(naoLido ? View.VISIBLE : View.INVISIBLE);

        return cartao;
    }

    @NonNull
    private static CategoriaAviso categoriaPorRotulo(@NonNull Context contexto, @NonNull String rotulo) {

        for (CategoriaAviso categoria : CategoriaAviso.values()) {
            if (contexto.getString(categoria.rotulo).equalsIgnoreCase(rotulo)) {
                return categoria;
            }
        }

        return CategoriaAviso.AVISO_GERAL;
    }

    public static void aplicarChip(@NonNull TextView chip, @NonNull CategoriaAviso categoria) {

        chip.setText(categoria.rotulo);

        chip.setTextColor(ContextCompat.getColor(chip.getContext(), categoria.texto));

        ViewCompat.setBackgroundTintList(
                chip, ContextCompat.getColorStateList(chip.getContext(), categoria.fundo));
    }
}
