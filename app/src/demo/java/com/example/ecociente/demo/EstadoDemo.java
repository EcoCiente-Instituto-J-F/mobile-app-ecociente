package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import java.util.function.LongSupplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONException;
import org.json.JSONObject;

// Todo o estado da demo num único JSON, gravado no armazém a cada alteração. Quem muda `raiz()` chama `salvar()` em seguida e, para a mudança ser atômica, faz as duas coisas dentro de `synchronized (estado)`: a ApiDemo e o ProvedorContaDemo usam este mesmo objeto como trava.
public final class EstadoDemo {

    private static final Logger LOG = Logger.getLogger("EcoCienteDemo");

    private final ArmazemDemo armazem;
    private final LongSupplier relogio;

    private JSONObject raiz;

    // Sobe a cada resetar(): quem guarda cópia do estado fora dele (avisos) sabe que a cópia ficou velha.
    private int geracao;

    // Carrega do armazém; ausente, corrompido ou de outra versão, começa da semente.
    public EstadoDemo(@NonNull ArmazemDemo armazem, @NonNull LongSupplier relogio) {
        this.armazem = armazem;
        this.relogio = relogio;
        this.raiz = carregar();
    }

    @NonNull
    public synchronized JSONObject raiz() {
        return raiz;
    }

    public synchronized int geracao() {
        return geracao;
    }

    public synchronized void salvar() {
        armazem.salvar(raiz.toString());
    }


    public synchronized boolean selecionarConta(String email, String senha) {
        String chave = ContaDemo.normalizarEmail(email);
        JSONObject perfis = raiz.optJSONObject("perfisDemo");
        JSONObject escolhido = perfis == null ? null : perfis.optJSONObject(chave);
        if (escolhido == null || !ContaDemo.credenciaisConferem(escolhido, email, senha)) return false;
        try {
            String anterior = raiz.optString("usuarioAtivoDemo", "");
            if (anterior.equals(chave)) return true;
            if (!anterior.isEmpty()) perfis.put(anterior, UsuariosMultiperfilDemo.snapshot(raiz));
            UsuariosMultiperfilDemo.aplicar(raiz, escolhido);
            raiz.put("usuarioAtivoDemo", chave);
            salvar();
            return true;
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    @NonNull
    public synchronized com.example.ecociente.model.ResultadoCadastro cadastrar(
            @NonNull String nome, @NonNull String email, @NonNull String senha,
            @NonNull java.util.Map<String, Object> dados) {
        String chave = ContaDemo.normalizarEmail(email);
        if (chave.isEmpty() || !chave.contains("@") || senha.length() < 6 || nome.trim().isEmpty()) {
            return com.example.ecociente.model.ResultadoCadastro.erro("Informe nome, e-mail e senha valida.");
        }
        JSONObject perfis = raiz.optJSONObject("perfisDemo");
        if (perfis == null) return com.example.ecociente.model.ResultadoCadastro.erro("Demo nao inicializada.");
        if (perfis.has(chave)) {
            return com.example.ecociente.model.ResultadoCadastro.contaJaExistente("Este e-mail ja esta cadastrado.");
        }
        try {
            String tipo = String.valueOf(dados.getOrDefault("tipoUsuario", "comum")).toLowerCase(java.util.Locale.ROOT);
            boolean cooperativa = "cooperativa".equals(tipo);
            boolean morador = "morador".equals(tipo);
            int novoId = 100 + perfis.length();
            JSONObject base = perfis.getJSONObject(UsuariosMultiperfilDemo.MORADOR);
            JSONObject novo = new JSONObject(base.toString());
            JSONObject conta = novo.getJSONObject("conta");
            conta.put("email", chave).put("senha", senha).put("uid", "demo-" + novoId)
                    .put("logado", false).put("token", "");
            JSONObject me = novo.getJSONObject("me");
            me.put("id", novoId).put("nome", nome.trim()).put("email", chave)
                    .put("perfil", cooperativa ? "COOPERATIVA" : morador ? "MORADOR" : "USUARIO_COMUM")
                    .put("cpf", String.valueOf(dados.getOrDefault("cpf", "")));
            if (cooperativa || !morador) me.put("condominios", new org.json.JSONArray());
            if (cooperativa) {
                me.put("cooperativa", new JSONObject()
                        .put("id", novoId).put("nome", String.valueOf(dados.getOrDefault("nomeCooperativa", "Cooperativa Demo")))
                        .put("email", chave));
            } else {
                me.remove("cooperativa");
            }
            JSONObject endereco = me.optJSONObject("endereco");
            if (endereco != null) {
                for (String k : new String[]{"cep", "numero", "complemento", "logradouro", "cidade", "estado"}) {
                    if (dados.containsKey(k)) endereco.put(k, String.valueOf(dados.get(k)));
                }
            }
            JSONObject pontos = novo.getJSONObject("pontuacao");
            org.json.JSONArray participantes = pontos.optJSONArray("participantes");
            for (int i = 0; participantes != null && i < participantes.length(); i++) {
                JSONObject item = participantes.getJSONObject(i);
                if (item.optInt("usuarioId") == SementeDemo.USUARIO_ID) item.put("usuarioId", novoId);
            }
            perfis.put(chave, novo);
            salvar();
            return com.example.ecociente.model.ResultadoCadastro.sucesso("demo-" + novoId);
        } catch (org.json.JSONException erro) {
            return com.example.ecociente.model.ResultadoCadastro.erro("Nao foi possivel cadastrar o usuario de demonstracao.");
        }
    }

    public synchronized void encerrarSessao() {
        try {
            ContaDemo.conta(raiz).put("logado", false).put("token", "");
            salvar();
        } catch (JSONException e) {
            throw new IllegalStateException(e);
        }
    }

    // O instante atual segundo o relógio injetado.
    public long agora() {
        return relogio.getAsLong();
    }

    // Pontos do morador. Quem chama de fora da ApiDemo não precisa travar: os métodos já são synchronized.
    public synchronized int saldo() {
        return PontuacaoDemo.saldo(raiz);
    }

    // 1 para o primeiro do ranking; 0 se o morador não está nele.
    public synchronized int posicaoDoUsuario() {
        return PontuacaoDemo.posicaoDoUsuario(raiz);
    }

    // Soma pontos ao morador (origem POSTAGEM ou QUIZ), grava a movimentação e persiste.
    public synchronized void creditar(@NonNull String origemTipo, int pontos) {
        PontuacaoDemo.creditar(raiz, origemTipo, pontos, relogio.getAsLong());
        salvar();
    }

    // Grava o JPEG enviado (upload do avatar ou do descarte) e devolve a URL file:// dele.
    @NonNull
    public synchronized String gravarFoto(@NonNull byte[] jpeg) {
        return armazem.gravarFoto(jpeg);
    }

    // Apaga o arquivo e as fotos e recomeça da semente.
    public synchronized void resetar() {
        armazem.apagarTudo();
        raiz = SementeDemo.criar(relogio.getAsLong());
        geracao++;
        salvar();
    }

    @NonNull
    private JSONObject carregar() {
        String json = armazem.ler();

        if (json != null) {
            try {
                JSONObject lido = new JSONObject(json);

                if (lido.optInt("versao", 0) == SementeDemo.VERSAO) {
                    // A semente de agendamentos é relativa à semana em que nasceu: abrindo o app em outra semana, só ela é refeita.
                    long agora = relogio.getAsLong();

                    if (!SementeDemo.semanaDe(agora).equals(lido.optString("agendamentosSemana", ""))) {
                        SementeDemo.renovarAgendamentos(lido, agora);
                        armazem.salvar(lido.toString());
                    }

                    return lido;
                }

                LOG.warning("Estado da demo de outra versão; recomeçando da semente");

            } catch (JSONException erro) {
                LOG.log(Level.WARNING, "Estado da demo corrompido; recomeçando da semente", erro);
            }
        }

        JSONObject semente = SementeDemo.criar(relogio.getAsLong());

        armazem.salvar(semente.toString());

        return semente;
    }
}
