package com.example.ecociente.demo;

import androidx.annotation.NonNull;
import com.example.ecociente.model.SemanaDeColetas;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

// Estado inicial da demo. Hoje cobre conta, perfil, condomínio, pontos e agendamentos; cada área nova (pontos, feed, quiz...) acrescenta o seu bloco aqui.
public final class SementeDemo {

    // Muda quando o formato do estado muda: um arquivo de outra versão é descartado e a semente volta.
    static final int VERSAO = 2;

    // Têm de ser iguais aos DEMO_EMAIL e DEMO_SENHA do build type `demo` em app/build.gradle.kts, que a tela de login preenche.
    public static final String EMAIL = "morador@ecociente.demo";
    public static final String SENHA = "demo1234";

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
            raiz.put("pontuacao", PontuacaoDemo.semente(agora));
            raiz.put("agendamentos", agendamentos(agora));

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

    // Tudo relativo a `agora`. A semana é a do card da home (segunda a sexta; sábado e domingo ainda contam para a que termina): cada dia da semana que ainda vem tem uma coleta, e a segunda-feira passada garante ao menos uma na semana. São 4 realizadas (a da segunda desta semana mais as das duas semanas anteriores) e 3 nas semanas seguintes, sempre no futuro.
    @NonNull
    private static JSONArray agendamentos(long agora) throws JSONException {
        Calendar hoje = Calendar.getInstance();
        hoje.setTimeInMillis(agora);

        Calendar inicioDeHoje = (Calendar) hoje.clone();
        inicioDeHoje.set(Calendar.HOUR_OF_DAY, 0);
        inicioDeHoje.set(Calendar.MINUTE, 0);
        inicioDeHoje.set(Calendar.SECOND, 0);
        inicioDeHoje.set(Calendar.MILLISECOND, 0);

        Calendar segunda = SemanaDeColetas.segundaDe(hoje);
        JSONArray lista = new JSONArray();

        // Passadas: [semanas antes, dias depois da segunda].
        int[][] passadas = {{0, 0}, {-1, 2}, {-1, 4}, {-2, 1}, {-2, 3}};
        int realizadas = 0;

        for (int i = 0; i < passadas.length && realizadas < 4; i++) {
            Calendar dia = diaDa(segunda, passadas[i][0] * 7 + passadas[i][1]);

            // Só entra o que já passou; com hoje na segunda, a quinta posição completa as quatro.
            if (dia.before(inicioDeHoje)) {
                lista.put(agendamento(lista.length() + 1, dia, "REALIZADO"));
                realizadas++;
            }
        }

        // Dias da semana que ainda vêm.
        for (int d = 1; d < SemanaDeColetas.DIAS_UTEIS; d++) {
            Calendar dia = diaDa(segunda, d);

            if (dia.after(inicioDeHoje)) {
                lista.put(agendamento(lista.length() + 1, dia, d % 2 == 0 ? "AGENDADO" : "CONFIRMADO"));
            }
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
