package com.example.ecociente.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import com.example.ecociente.R;
import com.example.ecociente.model.Solicitacao;
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

        holder.condominio.setText(
                contexto.getString(R.string.solicitacao_condominio, solicitacao.getCondominioId()));

        holder.status.setText(solicitacao.getStatus().getRotulo());

        holder.data.setText(
                contexto.getString(
                        R.string.solicitacao_data_horario,
                        FormatoDataApi.data(solicitacao.getDataInicio()),
                        FormatoDataApi.hora(solicitacao.getDataInicio())));

        holder.recorrencia.setVisibility(solicitacao.possuiRecorrencia() ? View.VISIBLE : View.GONE);

        holder.itemView.setOnClickListener(view -> ouvinte.aoClicar(solicitacao));
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
