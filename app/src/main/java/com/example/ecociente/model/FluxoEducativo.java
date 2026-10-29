package com.example.ecociente.model;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.ecociente.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Contrato local de navegacao/estado da experiencia educativa.
 * Os IDs de demonstracao sao negativos e NUNCA sao enviados ao backend.
 * A persistencia neste modulo e somente local; o backend atual nao expoe
 * um endpoint de conclusao de aula ou paginas de leitura.
 */
public final class FluxoEducativo {
    public static final String EXTRA_CURSO = "eco.curso.json";
    public static final String EXTRA_AULA_ID = "eco.aula.id";
    public static final int SEM_AULA = Integer.MIN_VALUE;

    private FluxoEducativo() { }

    @NonNull
    public static Intent comCurso(@NonNull Intent intent, @NonNull CursoEducativo curso) {
        try {
            JSONObject json = new JSONObject();
            json.put("id", curso.id);
            json.put("titulo", curso.titulo);
            json.put("descricao", curso.descricao);
            JSONArray aulas = new JSONArray();
            for (CursoEducativo.Aula aula : curso.aulas) {
                JSONObject item = new JSONObject();
                item.put("id", aula.id);
                item.put("titulo", aula.titulo);
                item.put("conteudo", aula.conteudo);
                item.put("ordem", aula.ordem);
                aulas.put(item);
            }
            json.put("aulas", aulas);
            intent.putExtra(EXTRA_CURSO, json.toString());
        } catch (Exception erro) {
            throw new IllegalArgumentException("Falha ao preparar o curso para navegacao", erro);
        }
        return intent;
    }

    @Nullable
    public static CursoEducativo lerCurso(@NonNull Intent intent) {
        String conteudo = intent.getStringExtra(EXTRA_CURSO);
        if (conteudo == null || conteudo.isEmpty()) return null;
        try {
            JSONObject json = new JSONObject(conteudo);
            JSONArray itens = json.optJSONArray("aulas");
            List<CursoEducativo.Aula> aulas = new ArrayList<>();
            for (int i = 0; itens != null && i < itens.length(); i++) {
                JSONObject item = itens.getJSONObject(i);
                aulas.add(new CursoEducativo.Aula(item.getInt("id"),
                        item.optString("titulo"), item.optString("conteudo"),
                        item.optInt("ordem", i + 1)));
            }
            return new CursoEducativo(json.getInt("id"), json.optString("titulo"),
                    json.optString("descricao"), aulas);
        } catch (Exception erro) {
            return null;
        }
    }

    @Nullable
    public static CursoEducativo.Aula lerAula(@NonNull Intent intent, @NonNull CursoEducativo curso) {
        int id = intent.getIntExtra(EXTRA_AULA_ID, SEM_AULA);
        if (id == SEM_AULA) return null;
        for (CursoEducativo.Aula aula : curso.aulas) {
            if (aula.id == id) return aula;
        }
        return null;
    }

    @NonNull
    public static String tipo(@NonNull CursoEducativo curso, @Nullable CursoEducativo.Aula aula) {
        if (aula != null) return "Aula";
        if (DadosDemonstracaoEducacao.ATIVO && curso.id < 0)
            return DadosDemonstracaoEducacao.tipoConteudo(curso.id);
        return "Guia";
    }

    @NonNull
    public static String nome(@NonNull CursoEducativo curso, @Nullable CursoEducativo.Aula aula) {
        return aula == null ? curso.titulo : aula.titulo;
    }

    @NonNull
    public static String descricao(@NonNull CursoEducativo curso, @Nullable CursoEducativo.Aula aula) {
        if (aula == null) return curso.descricao;
        // Texto de aula pode ser extenso. A intro mostra a descricao do curso,
        // enquanto as paginas de leitura usam o conteudo integral da aula.
        return curso.descricao.isEmpty()
                ? "Aprenda sobre este tema em pequenas etapas pr\u00e1ticas."
                : curso.descricao;
    }

    public static int imagem(@NonNull CursoEducativo curso) {
        switch (curso.id) {
            case -101: case -106: return R.drawable.img_greenrecyclecontainer;
            case -103: case -105: return R.drawable.img_greenplastic;
            case -102: case -104: return R.drawable.img_greenpapers;
            default: return R.drawable.ic_guia_verde;
        }
    }

    private static SharedPreferences preferencias(@NonNull Context contexto) {
        FirebaseUser usuario = FirebaseAuth.getInstance().getCurrentUser();
        String id = usuario == null ? "visitante" : usuario.getUid();
        String ambiente = DadosDemonstracaoEducacao.ATIVO ? "demo" : "local";
        return contexto.getSharedPreferences("ecociente_leitura_" + ambiente + "_v1_" + id,
                Context.MODE_PRIVATE);
    }

    private static String chave(@NonNull CursoEducativo curso, @Nullable CursoEducativo.Aula aula) {
        return (aula == null ? "guia:" + curso.id : "aula:" + aula.id);
    }

    public static int paginaSalva(@NonNull Context contexto, @NonNull CursoEducativo curso,
                                  @Nullable CursoEducativo.Aula aula) {
        return preferencias(contexto).getInt("etapa:" + chave(curso, aula), 0);
    }

    public static void salvarPagina(@NonNull Context contexto, @NonNull CursoEducativo curso,
                                   @Nullable CursoEducativo.Aula aula, int indice) {
        preferencias(contexto).edit()
                .putInt("etapa:" + chave(curso, aula), Math.max(0, indice)).apply();
    }

    public static void concluir(@NonNull Context contexto, @NonNull CursoEducativo curso,
                                @Nullable CursoEducativo.Aula aula) {
        preferencias(contexto).edit().putBoolean("concluido:" + chave(curso, aula), true)
                .remove("etapa:" + chave(curso, aula)).apply();
    }

    public static boolean concluido(@NonNull Context contexto, @NonNull CursoEducativo curso,
                                    @Nullable CursoEducativo.Aula aula) {
        return preferencias(contexto).getBoolean("concluido:" + chave(curso, aula), false);
    }

    public static int totalAulasConcluidas(@NonNull Context contexto, @NonNull CursoEducativo curso) {
        int contador = 0;
        for (CursoEducativo.Aula aula : curso.aulas) {
            if (concluido(contexto, curso, aula)) contador++;
        }
        return contador;
    }

    /** Uma pagina corresponde a uma etapa visivel entre dois cliques em Continuar. */
    public static final class Pagina {
        @NonNull public final String titulo;
        @NonNull public final String subtitulo;
        @NonNull public final String texto;
        @NonNull public final String dica;
        public final int imagem;

        public Pagina(String titulo, String subtitulo, String texto, String dica, int imagem) {
            this.titulo = titulo;
            this.subtitulo = subtitulo;
            this.texto = texto;
            this.dica = dica;
            this.imagem = imagem;
        }
    }

    @NonNull
    public static List<Pagina> paginas(@NonNull CursoEducativo curso,
                                       @Nullable CursoEducativo.Aula aula) {
        if (DadosDemonstracaoEducacao.ATIVO && curso.id < 0) {
            return paginasDemo(curso, aula);
        }
        String texto = aula == null ? curso.descricao : aula.conteudo;
        if (texto == null || texto.trim().isEmpty()) return Collections.emptyList();
        // Nao altera o conteudo enviado pelo backend: apenas divide paragrafo/frases
        // em paginas menores, respeitando a ordem original das informacoes.
        String[] paragrafos = texto.trim().split("(?<=\\.)\\s+(?=[A-Z\\p{Lu}])|\\n\\s*\\n+");
        List<String> grupos = new ArrayList<>();
        StringBuilder bloco = new StringBuilder();
        for (String paragrafo : paragrafos) {
            if (bloco.length() > 0 && bloco.length() + paragrafo.length() > 320) {
                grupos.add(bloco.toString().trim());
                bloco.setLength(0);
            }
            if (bloco.length() > 0) bloco.append(' ');
            bloco.append(paragrafo.trim());
        }
        if (bloco.length() > 0) grupos.add(bloco.toString().trim());
        List<Pagina> paginas = new ArrayList<>();
        for (int i = 0; i < grupos.size(); i++) {
            paginas.add(new Pagina(i == 0 ? nome(curso, aula) :
                    "Vamos aprender mais", "CONTE\u00daDO EDUCATIVO", grupos.get(i),
                    "Pense em como aplicar esse aprendizado na sua rotina.", imagem(curso)));
        }
        return paginas;
    }

    private static List<Pagina> paginasDemo(@NonNull CursoEducativo curso,
                                            @Nullable CursoEducativo.Aula aula) {
        List<Pagina> paginas = new ArrayList<>();
        int imagem = imagem(curso);
        String abertura = aula == null ? curso.descricao : aula.conteudo;
        paginas.add(new Pagina(nome(curso, aula), "VAMOS COME\u00c7AR!",
                abertura + "\n\nA ideia \u00e9 aprender um pouco de cada vez, com exemplos " +
                "que voc\u00ea pode aplicar de verdade.",
                "N\u00e3o precisa mudar tudo de uma vez. Comece com uma atitude.", imagem));
        String segundo;
        String terceiro;
        String desafio;
        switch (curso.id) {
            case -101:
                segundo = "Recicl\u00e1veis e org\u00e2nicos precisam de destinos diferentes. " +
                        "Embalagens vazias e sem restos de alimento facilitam o trabalho da coleta seletiva. " +
                        "As regras de aceita\u00e7\u00e3o variam conforme a cidade.";
                terceiro = "Escolha um recipiente para papel, pl\u00e1stico e metal. " +
                        "Separe os org\u00e2nicos, mantenha os recicl\u00e1veis secos e consulte os dias de coleta.";
                desafio = "Olhe uma embalagem da sua casa e descubra como ela deve ser descartada.";
                break;
            case -102:
                segundo = "Na compostagem, os microrganismos transformam parte dos " +
                        "res\u00edduos org\u00e2nicos. Materiais \u00famidos, como cascas, e materiais secos, " +
                        "como folhas, precisam estar equilibrados. Aera\u00e7\u00e3o tamb\u00e9m \u00e9 importante.";
                terceiro = "Comece com pequenas por\u00e7\u00f5es de res\u00edduos vegetais, " +
                        "adicione material seco e observe umidade e cheiro. Ajuste conforme o seu sistema.";
                desafio = "Separe hoje uma casca de fruta e imagine seu caminho at\u00e9 o composto.";
                break;
            case -103:
                segundo = "O s\u00edmbolo no pl\u00e1stico ajuda a identificar o tipo de material, " +
                        "mas n\u00e3o significa que ele ser\u00e1 reciclado em qualquer local. " +
                        "Consulte o que a coleta seletiva da regi\u00e3o realmente recebe.";
                terceiro = "Antes de descartar uma embalagem, esvazie-a. " +
                        "Quando poss\u00edvel, prefira objetos reutiliz\u00e1veis e evite embalagens desnecess\u00e1rias.";
                desafio = "Encontre dois tipos diferentes de embalagem pl\u00e1stica na sua rotina.";
                break;
            case -104:
                segundo = "Papel seco e limpo costuma ter melhor aproveitamento na reciclagem. " +
                        "Pap\u00e9is muito sujos de gordura e itens sanit\u00e1rios n\u00e3o devem ser misturados aos recicl\u00e1veis.";
                terceiro = "Guarde folhas e caixas secas separadas dos res\u00edduos org\u00e2nicos. " +
                        "Evite amassar papel ainda aproveit\u00e1vel para rascunho.";
                desafio = "Reaproveite o verso de uma folha antes de envi\u00e1-la \u00e0 reciclagem.";
                break;
            case -105:
                segundo = "O vidro pode ser aproveitado novamente, mas recipientes quebrados " +
                        "exigem cuidado para n\u00e3o ferir quem manuseia os res\u00edduos.";
                terceiro = "Mantenha os recipientes inteiros separados. " +
                        "Se houver cacos, proteja, identifique e siga a orienta\u00e7\u00e3o da coleta local.";
                desafio = "Descubra qual ponto de coleta da sua cidade recebe vidro.";
                break;
            case -106:
                segundo = "Caixas de papel\u00e3o ocupam muito espa\u00e7o quando n\u00e3o s\u00e3o dobradas. " +
                        "Reduzir o volume facilita o armazenamento e a coleta.";
                terceiro = "Retire restos de embalagens, achate as caixas e deixe-as protegidas da chuva.";
                desafio = "Experimente dobrar uma caixa e compare o espa\u00e7o ocupado.";
                break;
            default:
                segundo = "Sustentabilidade aparece em decis\u00f5es simples: aproveitar alimentos, " +
                        "reduzir desperd\u00edcios, consumir com aten\u00e7\u00e3o e reutilizar o que j\u00e1 temos.";
                terceiro = "Escolha um h\u00e1bito para esta semana. Pode ser levar uma garrafa, " +
                        "desligar aparelhos sem uso ou planejar as compras.";
                desafio = "Qual pequena mudan\u00e7a voc\u00ea consegue manter pelos pr\u00f3ximos sete dias?";
                break;
        }
        paginas.add(new Pagina("Entenda por que importa", "PEQUENAS A\u00c7\u00d5ES, GRANDE IMPACTO",
                segundo, "Observar seus h\u00e1bitos \u00e9 o primeiro passo para transform\u00e1-los.", imagem));
        paginas.add(new Pagina("Agora \u00e9 sua vez!", "COLOQUE EM PR\u00c1TICA",
                terceiro + "\n\nVoc\u00ea n\u00e3o precisa fazer tudo hoje. " +
                "O importante \u00e9 come\u00e7ar e continuar aprendendo.", desafio, imagem));
        return paginas;
    }
}
