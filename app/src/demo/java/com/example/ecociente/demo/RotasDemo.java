package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

// Um grupo de rotas da API falsa (conta, feed, quiz...). Devolve null quando o pedido não é dele, e o ApiDemo tenta o próximo grupo. Chamado sob `synchronized (estado)`.
interface RotasDemo {

    @Nullable
    RespostaDemo tratar(@NonNull RequisicaoDemo requisicao);
}
