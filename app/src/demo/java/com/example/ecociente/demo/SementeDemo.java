package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import com.example.ecociente.model.SemanaDeColetas;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Estado inicial da demo. Hoje cobre conta, perfil, condomínio, pontos, feed, tentativas de quiz e agendamentos; cada área nova acrescenta o seu bloco aqui.
public final class SementeDemo {

    // Muda quando o formato do estado muda: um arquivo de outra versão é descartado e a semente volta.
    static final int VERSAO = 7;

    // Têm de ser iguais aos DEMO_EMAIL e DEMO_SENHA do build type `demo` em app/build.gradle.kts, que a tela de login preenche.
    public static final String EMAIL = "morador@ecociente.com.br";
    public static final String SENHA = "Ecociente2026.";

    public static final String UID = "demo-morador-42";
    public static final int USUARIO_ID = 42;
    public static final String NOME = "Marina Alves";
    public static final int CONDOMINIO_ID = 7;
    public static final String CONDOMINIO_NOME = "Condomínio Jardim das Palmeiras";

    private SementeDemo() {}

    @NonNull
    public static JSONObject criar(long agora) {
        try {
            JSONObject raiz = new JSONObject();

            raiz.put("versao", VERSAO);
            raiz.put("criadoEm", agora);
            raiz.put("conta", conta());
            raiz.put("me", me());
            raiz.put("preferencias", new JSONObject());
            raiz.put("avisosLidos", new JSONArray());
            raiz.put("pontuacao", PontuacaoDemo.semente(agora));
            raiz.put("feed", FeedDemo.semente(agora));
            raiz.put("educacao", RotasQuiz.semente());
            raiz.put("agendamentos", agendamentos(agora));
            raiz.put("agendamentosSemana", semanaDe(agora));

            UsuariosMultiperfilDemo.configurar(raiz);
            return raiz;

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Data como a API manda: hora local, sem fuso.
    @NonNull
    static String formatarData(long instante) {
        return new SimpleDateFormat(PontuacaoDemo.FORMATO_DATA, Locale.US).format(new Date(instante));
    }

    private static final int COOPERATIVA_ID = 3;
    private static final String COOPERATIVA_NOME = "Cooperativa Folha Nova";

    // Segunda-feira (texto) da semana do relógio: marca de qual semana o bloco `agendamentos` foi gerado.
    @NonNull
    static String semanaDe(long agora) {
        Calendar hoje = Calendar.getInstance();
        hoje.setTimeInMillis(agora);

        return formatarData(SemanaDeColetas.segundaDe(hoje).getTimeInMillis());
    }

    // Refaz só os agendamentos a partir do relógio, quando o estado guardado é de outra semana. Os ids seguem a posição (1, 2, 3...), então continuam os mesmos.
    static void renovarAgendamentos(@NonNull JSONObject raiz, long agora) {
        try {
            raiz.put("agendamentos", agendamentos(agora));
            raiz.put("agendamentosSemana", semanaDe(agora));

        } catch (JSONException erro) {
            throw new IllegalStateException(erro);
        }
    }

    // Tudo relativo a `agora`. A semana é a do card da home (segunda a sexta; sábado e domingo ainda contam para a que termina). Cada dia útil dela vira uma coleta: as que já passaram (às 9h) são REALIZADAS, as que ainda vêm ficam AGENDADA/CONFIRMADA. Ficam 4 REALIZADAS no total (as mais recentes da semana, completadas com as das semanas anteriores) e 3 nas semanas seguintes, a primeira na terça seguinte, sempre no futuro.
    @NonNull
    private static JSONArray agendamentos(long agora) throws JSONException {
        Calendar hoje = Calendar.getInstance();
        hoje.setTimeInMillis(agora);

        Calendar segunda = SemanaDeColetas.segundaDe(hoje);
        JSONArray lista = new JSONArray();

        List<Calendar> realizadas = new ArrayList<>();
        List<Calendar> vindouras = new ArrayList<>();

        for (int d = 0; d < SemanaDeColetas.DIAS_UTEIS; d++) {
            Calendar dia = diaDa(segunda, d);

            if (dia.getTimeInMillis() > agora) {
                vindouras.add(dia);
            } else {
                realizadas.add(dia);
            }
        }

        // Só as 4 mais recentes da semana; as demais vagas vêm das semanas anteriores (da mais próxima para a mais antiga).
        while (realizadas.size() > 4) {
            realizadas.remove(0);
        }

        int[] anteriores = {-7 + 4, -7 + 2, -14 + 3, -14 + 1};

        for (int i = 0; realizadas.size() < 4 && i < anteriores.length; i++) {
            realizadas.add(0, diaDa(segunda, anteriores[i]));
        }

        for (Calendar dia : realizadas) {
            lista.put(agendamento(lista.length() + 1, dia, "REALIZADO"));
        }

        for (int i = 0; i < vindouras.size(); i++) {
            lista.put(agendamento(lista.length() + 1, vindouras.get(i), i % 2 == 0 ? "CONFIRMADO" : "AGENDADO"));
        }

        // Semanas seguintes (a partir da segunda da semana do card, então são sempre futuras).
        int[] seguintes = {7 + 1, 14 + 3, 21};
        String[] status = {"AGENDADO", "CONFIRMADO", "AGENDADO"};

        for (int i = 0; i < seguintes.length; i++) {
            lista.put(agendamento(lista.length() + 1, diaDa(segunda, seguintes[i]), status[i]));
        }

        return lista;
    }

    // Dia `dias` depois da segunda, às 9h (Calendar.add cuida de virada de mês, ano e bissexto).
    @NonNull
    private static Calendar diaDa(@NonNull Calendar segunda, int dias) {
        Calendar dia = (Calendar) segunda.clone();
        dia.add(Calendar.DAY_OF_MONTH, dias);
        dia.set(Calendar.HOUR_OF_DAY, 9);

        return dia;
    }

    @NonNull
    private static JSONObject agendamento(int id, @NonNull Calendar inicio, @NonNull String status)
            throws JSONException {
        Calendar fim = (Calendar) inicio.clone();
        fim.add(Calendar.HOUR_OF_DAY, 2);

        return new JSONObject()
                .put("id", id)
                .put("condominioId", CONDOMINIO_ID)
                .put("condominioNome", CONDOMINIO_NOME)
                .put("cooperativaId", COOPERATIVA_ID)
                .put("cooperativaNome", COOPERATIVA_NOME)
                .put("dataInicio", formatarData(inicio.getTimeInMillis()))
                .put("dataFim", formatarData(fim.getTimeInMillis()))
                .put("statusAgendamento", status)
                .put("possuiRecorrencia", false)
                .put("origemSolicitacao", "COOPERATIVA");
    }

    // Credenciais e sessão. `logado` é o que ContaAtual enxerga; `token` é o JWT que o /auth/login entrega (vazio até o primeiro login).
    @NonNull
    private static JSONObject conta() throws JSONException {
        return new JSONObject()
                .put("uid", UID)
                .put("email", EMAIL)
                .put("senha", SENHA)
                .put("logado", false)
                .put("token", "");
    }

    // Forma do GET /api/usuarios/me, como o PerfilApiJson lê.
    @NonNull
    private static JSONObject me() throws JSONException {
        JSONObject endereco =
                new JSONObject()
                        .put("cep", "01310100")
                        .put("estado", "SP")
                        .put("cidade", "São Paulo")
                        .put("logradouro", "Avenida Paulista")
                        .put("numero", "1000")
                        .put("complemento", "Torre B, apto 42");

        JSONObject telefone = new JSONObject().put("numero", "11987654321").put("ativo", true);

        JSONObject condominio =
                new JSONObject()
                        .put("id", CONDOMINIO_ID)
                        .put("nome", CONDOMINIO_NOME)
                        .put("ativo", true)
                        .put("sindicoResponsavel", false)
                        .put("tipoCondominio", "Residencial")
                        .put("urlFoto", "")
                        .put("endereco", endereco)
                        .put("torres", new JSONArray().put(new JSONObject().put("id", 2).put("nome", "Torre B")))
                        .put("vinculo", new JSONObject().put("aprovado", true));

        return new JSONObject()
                .put("id", USUARIO_ID)
                .put("nome", NOME)
                .put("email", EMAIL)
                .put("cpf", "111.444.777-35")
                .put("urlAvatar", "")
                .put("perfil", "MORADOR")
                .put("telefones", new JSONArray().put(telefone))
                .put("endereco", endereco)
                .put("condominios", new JSONArray().put(condominio));
    }
}
