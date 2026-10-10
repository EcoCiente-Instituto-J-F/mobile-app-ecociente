package com.example.ecociente.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecociente.R;
import com.example.ecociente.model.Solicitacao;
import com.example.ecociente.model.StatusAgendamento;
import com.example.ecociente.ui.FormatoDataApi;

public class SolicitacaoAdapter
        extends ListAdapter<Solicitacao, SolicitacaoAdapter.SolicitacaoHolder> {

    public interface OnSolicitacaoClickListener {
        void aoClicar(@NonNull Solicitacao solicitacao);
    }

    private static final DiffUtil.ItemCallback<Solicitacao> COMPARADOR =
            new DiffUtil.ItemCallback<Solicitacao>() {
                @Override
                public boolean areItemsTheSame(@NonNull Solicitacao a, @NonNull Solicitacao b) {
                    return a.getId() == b.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Solicitacao a, @NonNull Solicitacao b) {
                    return a.equals(b);
                }
            };

    private final OnSolicitacaoClickListener ouvinte;

    public SolicitacaoAdapter(@NonNull OnSolicitacaoClickListener ouvinte) {
        super(COMPARADOR);
        this.ouvinte = ouvinte;
    }

    @NonNull
    @Override
    public SolicitacaoHolder onCreateViewHolder(@NonNull ViewGroup pai, int tipo) {

        View item =
                LayoutInflater.from(pai.getContext()).inflate(R.layout.item_solicitacao, pai, false);

        return new SolicitacaoHolder(item);
    }

    @Override
    public void onBindViewHolder(@NonNull SolicitacaoHolder holder, int posicao) {

        Solicitacao solicitacao = getItem(posicao);

        Context contexto = holder.itemView.getContext();

        holder.condominio.setText(nomeExibido(contexto, solicitacao));

        StatusAgendamento status = solicitacao.getStatus();

        holder.status.setText(status.getEtiqueta());
        holder.status.setTextColor(ContextCompat.getColor(contexto, status.getTextoEtiqueta()));

        ViewCompat.setBackgroundTintList(
                holder.status, ContextCompat.getColorStateList(contexto, status.getFundoEtiqueta()));

        holder.data.setText(
                contexto.getString(
                        R.string.solicitacao_data_horario,
                        FormatoDataApi.data(solicitacao.getDataInicio()),
                        FormatoDataApi.hora(solicitacao.getDataInicio())));

        holder.recorrencia.setVisibility(solicitacao.possuiRecorrencia() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(view -> ouvinte.aoClicar(solicitacao));
    }

    @NonNull
    static String nomeExibido(@NonNull Context contexto, @NonNull Solicitacao solicitacao) {

        String nome = solicitacao.getNomeCondominio();

        return nome != null
                ? nome
                : contexto.getString(R.string.solicitacao_condominio, solicitacao.getCondominioId());
    }

    static final class SolicitacaoHolder extends RecyclerView.ViewHolder {

        final TextView condominio;
        final TextView status;
        final TextView data;
        final TextView recorrencia;

        SolicitacaoHolder(@NonNull View item) {
            super(item);

            condominio = item.findViewById(R.id.textoCondominioSolicitacao);
            status = item.findViewById(R.id.textoStatusSolicitacao);
            data = item.findViewById(R.id.textoDataSolicitacao);
            recorrencia = item.findViewById(R.id.textoRecorrenciaSolicitacao);
        }
    }
}
