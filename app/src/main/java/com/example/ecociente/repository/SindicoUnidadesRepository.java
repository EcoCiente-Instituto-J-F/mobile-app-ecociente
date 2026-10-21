package com.example.ecociente.repository;

import android.content.Context;
import android.content.SharedPreferences;
import com.example.ecociente.model.SindicoDadosCondominio;
import com.example.ecociente.model.SindicoApartamento;
import com.example.ecociente.model.SindicoItemUnidade;

import java.util.ArrayList;
import java.util.Collections;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Sem API de unidades: começa vazio e o que o síndico cadastra fica guardado no aparelho
 * (SharedPreferences, separado por usuário) até a API existir.
 */
public final class SindicoUnidadesRepository {
    private static final SindicoUnidadesRepository INSTANCIA = new SindicoUnidadesRepository();

    private SindicoDadosCondominio dados = new SindicoDadosCondominio(true, "", "", "", "", "");
    private String fotoUri;
    private final List<SindicoItemUnidade> blocos = new ArrayList<>();
    private final List<SindicoItemUnidade> areas = new ArrayList<>();
    private final List<SindicoApartamento> apartamentos = new ArrayList<>();
    private int proximoId = 1;
    private int proximoApartamentoId = 1;

    private static final String PREFERENCIAS = "sindico_unidades";

    private SharedPreferences preferencias;
    private String chaveDoUsuario;

    // Visível ao pacote para os testes criarem um repositório novo, sem o singleton.
    SindicoUnidadesRepository() { }

    // Lê o que o usuário atual já cadastrou. Trocar de conta troca também os dados.
    public synchronized void carregar(Context contexto) {

        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String chave = usuario != null ? usuario.getUid() : "sem_usuario";

        if (preferencias != null && chave.equals(chaveDoUsuario)) {
            return;
        }

        preferencias = contexto.getApplicationContext().getSharedPreferences(PREFERENCIAS, Context.MODE_PRIVATE);
        chaveDoUsuario = chave;

        deJson(preferencias.getString(chave, null));
    }

    private void persistir() {
        if (preferencias != null) {
            preferencias.edit().putString(chaveDoUsuario, paraJson()).apply();
        }
    }

    String paraJson() {
        try {
            JSONObject raiz = new JSONObject();

            raiz.put("proximoId", proximoId);
            raiz.put("proximoApartamentoId", proximoApartamentoId);
            raiz.put("fotoUri", fotoUri == null ? JSONObject.NULL : fotoUri);
            raiz.put("dados", new JSONObject()
                    .put("residencial", dados.isResidencial())
                    .put("nome", dados.getNome())
                    .put("endereco", dados.getEndereco())
                    .put("cidade", dados.getCidade())
                    .put("estado", dados.getEstado())
                    .put("cep", dados.getCep()));
            raiz.put("blocos", itensParaJson(blocos));
            raiz.put("areas", itensParaJson(areas));

            JSONArray unidades = new JSONArray();
            for (SindicoApartamento a : apartamentos) {
                unidades.put(new JSONObject()
                        .put("id", a.getId())
                        .put("blocoId", a.getBlocoId())
                        .put("numero", a.getNumero())
                        .put("morador", a.getMorador())
                        .put("telefone", a.getTelefone()));
            }
            raiz.put("apartamentos", unidades);

            return raiz.toString();

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Texto vazio ou inválido deixa o repositório vazio, como na primeira vez.
    void deJson(String json) {

        blocos.clear();
        areas.clear();
        apartamentos.clear();
        dados = new SindicoDadosCondominio(true, "", "", "", "", "");
        fotoUri = null;
        proximoId = 1;
        proximoApartamentoId = 1;

        if (json == null || json.isEmpty()) {
            return;
        }

        try {
            JSONObject raiz = new JSONObject(json);

            proximoId = raiz.optInt("proximoId", 1);
            proximoApartamentoId = raiz.optInt("proximoApartamentoId", 1);
            fotoUri = raiz.isNull("fotoUri") ? null : raiz.optString("fotoUri", null);

            JSONObject d = raiz.optJSONObject("dados");
            if (d != null) {
                dados = new SindicoDadosCondominio(
                        d.optBoolean("residencial", true), d.optString("nome"), d.optString("endereco"),
                        d.optString("cidade"), d.optString("estado"), d.optString("cep"));
            }

            itensDeJson(raiz.optJSONArray("blocos"), blocos);
            itensDeJson(raiz.optJSONArray("areas"), areas);

            JSONArray unidades = raiz.optJSONArray("apartamentos");
            for (int i = 0; unidades != null && i < unidades.length(); i++) {
                JSONObject a = unidades.getJSONObject(i);
                apartamentos.add(new SindicoApartamento(
                        a.getInt("id"), a.getInt("blocoId"), a.optString("numero"),
                        a.optString("morador"), a.optString("telefone")));
            }

        } catch (JSONException erro) {
            blocos.clear();
            areas.clear();
            apartamentos.clear();
        }
    }

    private static JSONArray itensParaJson(List<SindicoItemUnidade> itens) throws JSONException {
        JSONArray lista = new JSONArray();
        for (SindicoItemUnidade item : itens) {
            lista.put(new JSONObject()
                    .put("id", item.getId())
                    .put("nome", item.getNome())
                    .put("detalhe", item.getDetalhe()));
        }
        return lista;
    }

    private static void itensDeJson(JSONArray lista, List<SindicoItemUnidade> destino) throws JSONException {
        for (int i = 0; lista != null && i < lista.length(); i++) {
            JSONObject item = lista.getJSONObject(i);
            destino.add(new SindicoItemUnidade(item.getInt("id"), item.optString("nome"), item.optString("detalhe")));
        }
    }

    public static SindicoUnidadesRepository getInstance() { return INSTANCIA; }
    public SindicoDadosCondominio getDados() { return dados; }
    public void salvarDados(SindicoDadosCondominio novosDados) { dados = novosDados; persistir(); }
    public String getFotoUri() { return fotoUri; }
    public void salvarFotoUri(String uri) { fotoUri = uri; persistir(); }
    public List<SindicoItemUnidade> getBlocos() {
        return Collections.unmodifiableList(new ArrayList<>(blocos));
    }
    public List<SindicoItemUnidade> getAreas() {
        return Collections.unmodifiableList(new ArrayList<>(areas));
    }

    public SindicoItemUnidade getBloco(int blocoId) {
        for (SindicoItemUnidade bloco : blocos) {
            if (bloco.getId() == blocoId) return bloco;
        }
        return null;
    }

    public List<SindicoApartamento> getApartamentos(int blocoId) {
        List<SindicoApartamento> resultado = new ArrayList<>();
        for (SindicoApartamento apartamento : apartamentos) {
            if (apartamento.getBlocoId() == blocoId) resultado.add(apartamento);
        }
        return Collections.unmodifiableList(resultado);
    }

    public SindicoApartamento getApartamento(int apartamentoId) {
        for (SindicoApartamento apartamento : apartamentos) {
            if (apartamento.getId() == apartamentoId) return apartamento;
        }
        return null;
    }

    public boolean existeNumero(int blocoId, int apartamentoId, String numero) {
        for (SindicoApartamento apartamento : apartamentos) {
            if (apartamento.getBlocoId() == blocoId && apartamento.getId() != apartamentoId
                    && apartamento.getNumero().equalsIgnoreCase(numero)) return true;
        }
        return false;
    }

    public void salvarApartamento(int apartamentoId, int blocoId, String numero,
                                 String morador, String telefone) {
        SindicoApartamento novo = new SindicoApartamento(
                apartamentoId < 0 ? proximoApartamentoId++ : apartamentoId,
                blocoId, numero, morador, telefone);
        if (apartamentoId >= 0) {
            for (int i = 0; i < apartamentos.size(); i++) {
                if (apartamentos.get(i).getId() == apartamentoId) {
                    apartamentos.set(i, novo);
                    persistir();
                    return;
                }
            }
            throw new IllegalArgumentException("Unidade não encontrada");
        }
        apartamentos.add(novo);
        persistir();
    }

    public void excluirApartamento(int apartamentoId) {
        apartamentos.removeIf(apartamento -> apartamento.getId() == apartamentoId);
        persistir();
    }

    public void salvarBloco(int id, String nome, int quantidade) {
        salvar(blocos, id, nome, quantidade + " unidades");
    }

    public void salvarArea(int id, String nome, String local) {
        salvar(areas, id, nome, local);
    }

    private void salvar(List<SindicoItemUnidade> itens, int id, String nome, String detalhe) {
        SindicoItemUnidade novo = new SindicoItemUnidade(id < 0 ? proximoId++ : id,
                nome, detalhe);
        if (id >= 0) {
            for (int i = 0; i < itens.size(); i++) {
                if (itens.get(i).getId() == id) {
                    itens.set(i, novo);
                    persistir();
                    return;
                }
            }
            throw new IllegalArgumentException("Item não encontrado");
        }
        itens.add(novo);
        persistir();
    }

    public void excluirBloco(int id) {
        excluir(blocos, id);
        apartamentos.removeIf(apartamento -> apartamento.getBlocoId() == id);
        persistir();
    }
    public void excluirArea(int id) { excluir(areas, id); }

    private void excluir(List<SindicoItemUnidade> itens, int id) {
        itens.removeIf(item -> item.getId() == id);
        persistir();
    }
}
