package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.example.ecociente.R;
import com.example.ecociente.ui.FieldFeedback;
import com.example.ecociente.ui.Motion;
import com.example.ecociente.model.ResultadoCadastro;
import com.example.ecociente.viewmodels.CadastroViewModel;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;

public class CadastroEtapa1Fragment extends Fragment {

    // Mesma regra exigida pela API de cadastro: maiúscula, minúscula, número,
    // caractere especial e pelo menos 8 caracteres.
    private static final Pattern PADRAO_SENHA_VALIDA =
            Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^\\sa-zA-Z\\d]).{8,}$");

    private EditText campoNome;
    private EditText campoDataNascimento;
    private EditText campoEmail;
    private EditText campoSenha;
    private EditText campoConfirmarSenha;
    private EditText campoCodigoCondominio;

    private ImageView iconeCalendario;
    private ImageView iconeOlhoSenhaCadastro;
    private ImageView iconeOlhoConfirmarSenha;

    private LinearLayout containerEtapas;
    private LinearLayout linhaPossuiCodigoCondominio;

    private MaterialCheckBox checkPossuiCodigoCondominio;

    private MaterialCardView containerNome;
    private MaterialCardView containerDataNascimento;
    private MaterialCardView containerEmailCadastro;
    private MaterialCardView containerSenhaCadastro;
    private MaterialCardView containerConfirmarSenha;
    private MaterialCardView containerCodigoCondominio;

    private MaterialButton botaoContinuar;

    private ProgressBar indicadorCarregamento;
    private View[] barrasForcaSenha;
    private TextView textoForcaSenha;
    private Motion motion;

    private static final int COR_BORDA_CAMPO = Color.rgb(53, 121, 103);
    private static final int COR_BORDA_CODIGO_ATIVO = Color.rgb(6, 78, 59);

    private CadastroViewModel viewModel;

    private boolean alterandoData = false;
    private boolean senhaVisivel = false;
    private boolean confirmarSenhaVisivel = false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle estadoSalvo) {

        return inflater.inflate(R.layout.fragment_cadastro_etapa1, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle estadoSalvo) {

        super.onViewCreated(view, estadoSalvo);

        inicializarComponentes(view);

        viewModel = new ViewModelProvider(requireActivity()).get(CadastroViewModel.class);

        viewModel.getCarregando().observe(getViewLifecycleOwner(), this::definirCarregando);

        configurarMascaraData();

        configurarCalendario();

        configurarVisibilidadeSenha();

        configurarForcaSenha();

        configurarFeedbackDeCampos();

        configurarCodigoCondominio();

        configurarContinuar();

        recuperarDadosPreenchidos();

        motion.staggerIn(
                view.findViewById(R.id.tituloCadastro),
                view.findViewById(R.id.containerEtapas),
                containerNome,
                containerDataNascimento,
                containerEmailCadastro,
                containerSenhaCadastro,
                containerConfirmarSenha,
                view.findViewById(R.id.cardOpcaoCondominio),
                containerCodigoCondominio,
                botaoContinuar
        );
    }

    private void inicializarComponentes(View view) {

        campoNome = view.findViewById(R.id.campoNome);

        campoDataNascimento = view.findViewById(R.id.campoDataNascimento);

        campoEmail = view.findViewById(R.id.campoEmailCadastro);

        campoSenha = view.findViewById(R.id.campoSenhaCadastro);

        campoConfirmarSenha = view.findViewById(R.id.campoConfirmarSenha);

        campoCodigoCondominio = view.findViewById(R.id.campoCodigoCondominio);

        iconeCalendario = view.findViewById(R.id.iconeCalendario);

        iconeOlhoSenhaCadastro = view.findViewById(R.id.iconeOlhoSenhaCadastro);

        iconeOlhoConfirmarSenha = view.findViewById(R.id.iconeOlhoConfirmarSenha);

        containerEtapas = view.findViewById(R.id.containerEtapas);

        linhaPossuiCodigoCondominio = view.findViewById(R.id.linhaPossuiCodigoCondominio);

        checkPossuiCodigoCondominio = view.findViewById(R.id.checkPossuiCodigoCondominio);

        containerNome = view.findViewById(R.id.containerNome);
        containerDataNascimento = view.findViewById(R.id.containerDataNascimento);
        containerEmailCadastro = view.findViewById(R.id.containerEmailCadastro);
        containerSenhaCadastro = view.findViewById(R.id.containerSenhaCadastro);
        containerConfirmarSenha = view.findViewById(R.id.containerConfirmarSenha);
        containerCodigoCondominio = view.findViewById(R.id.containerCodigoCondominio);

        botaoContinuar = view.findViewById(R.id.botaoContinuar);

        indicadorCarregamento = view.findViewById(R.id.indicadorCarregamentoCadastroEtapa1);

        barrasForcaSenha = new View[] {
                view.findViewById(R.id.forcaSenha1), view.findViewById(R.id.forcaSenha2),
                view.findViewById(R.id.forcaSenha3), view.findViewById(R.id.forcaSenha4)};
        textoForcaSenha = view.findViewById(R.id.textoForcaSenha);
        textoForcaSenha.setText("");
        textoForcaSenha.setVisibility(View.GONE);

        motion = new Motion();
    }

    private void configurarMascaraData() {

        campoDataNascimento.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence texto, int inicio, int quantidade, int depois) {}

                    @Override
                    public void onTextChanged(
                            CharSequence texto, int inicio, int antes, int quantidade) {}

                    @Override
                    public void afterTextChanged(Editable editable) {

                        if (alterandoData) {
                            return;
                        }

                        alterandoData = true;

                        String numeros = editable.toString().replaceAll("\\D", "");

                        if (numeros.length() > 8) {

                            numeros = numeros.substring(0, 8);
                        }

                        StringBuilder formatado = new StringBuilder();

                        if (numeros.length() <= 2) {

                            formatado.append(numeros);

                        } else {

                            formatado.append(numeros, 0, 2).append("/");

                            if (numeros.length() <= 4) {

                                formatado.append(numeros.substring(2));

                            } else {

                                formatado
                                        .append(numeros, 2, 4)
                                        .append("/")
                                        .append(numeros.substring(4));
                            }
                        }

                        campoDataNascimento.setText(formatado.toString());

                        campoDataNascimento.setSelection(campoDataNascimento.getText().length());

                        alterandoData = false;
                    }
                });
    }

    private void configurarCalendario() {

        iconeCalendario.setOnClickListener(view -> abrirCalendario());
    }

    private void configurarVisibilidadeSenha() {

        iconeOlhoSenhaCadastro.setOnClickListener(
                view ->
                        senhaVisivel =
                                alternarVisibilidadeSenha(
                                        campoSenha, iconeOlhoSenhaCadastro, senhaVisivel));

        iconeOlhoConfirmarSenha.setOnClickListener(
                view ->
                        confirmarSenhaVisivel =
                                alternarVisibilidadeSenha(
                                        campoConfirmarSenha,
                                        iconeOlhoConfirmarSenha,
                                        confirmarSenhaVisivel));
    }

    /**
     * O indicador informa a força da senha sem exibir a regra mínima antes da hora.
     * A validação de cadastro exige no mínimo 8 caracteres.
     */
    private void configurarForcaSenha() {

        campoSenha.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

                    @Override
                    public void onTextChanged(CharSequence s, int st, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable senha) {
                        atualizarForcaSenha(senha.toString());
                    }
                });

        campoConfirmarSenha.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int st, int c, int a) {}

                    @Override
                    public void onTextChanged(CharSequence s, int st, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable confirmacao) {
                        if (confirmacao.toString().equals(campoSenha.getText().toString())) {
                            FieldFeedback.clear(containerConfirmarSenha, COR_BORDA_CAMPO);
                        }
                    }
                });

        atualizarForcaSenha(campoSenha.getText().toString());
    }

    private void atualizarForcaSenha(String senha) {

        int corInativa = Color.rgb(217, 221, 219);

        if (senha == null || senha.isEmpty()) {

            for (View barra : barrasForcaSenha) {
                barra.setBackgroundColor(corInativa);
            }

            textoForcaSenha.setText("");
            textoForcaSenha.setVisibility(View.GONE);
            return;
        }

        boolean tamanhoMinimo = senha.length() >= 8;
        boolean temMaiuscula = senha.matches(".*[A-Z].*");
        boolean temMinuscula = senha.matches(".*[a-z].*");
        boolean temNumero = senha.matches(".*\\d.*");
        boolean temEspecial = senha.matches(".*[^A-Za-z0-9].*");

        /*
         * A primeira barra representa uma senha digitada.
         * Depois de atingir os 8 caracteres, os critérios extras aumentam a força.
         * Assim uma senha forte consegue preencher de verdade as 4 barras.
         */
        int nivel = 1;

        if (tamanhoMinimo && temMaiuscula && temMinuscula) {
            nivel++;
        }

        if (tamanhoMinimo && temNumero) {
            nivel++;
        }

        if (tamanhoMinimo && temEspecial) {
            nivel++;
        }

        nivel = Math.min(nivel, barrasForcaSenha.length);

        int cor;
        String texto;

        if (nivel == 1) {
            cor = Color.rgb(186, 26, 26);
            texto = "Senha fraca";

        } else if (nivel == 2) {
            cor = Color.rgb(190, 120, 0);
            texto = "Senha média";

        } else if (nivel == 3) {
            cor = Color.rgb(53, 121, 103);
            texto = "Senha forte";

        } else {
            cor = Color.rgb(6, 78, 59);
            texto = "Senha muito forte";
        }

        for (int i = 0; i < barrasForcaSenha.length; i++) {
            View barra = barrasForcaSenha[i];
            barra.setBackgroundColor(i < nivel ? cor : corInativa);
            barra.animate()
                    .alpha(i < nivel ? 1f : 0.65f)
                    .setDuration(Motion.MICRO_MS)
                    .start();
        }

        textoForcaSenha.setVisibility(View.VISIBLE);
        textoForcaSenha.setText(texto);
        textoForcaSenha.setTextColor(cor);
    }

    private void configurarFeedbackDeCampos() {
        campoNome.addTextChangedListener(limparErroAoDigitar(containerNome, COR_BORDA_CAMPO));
        campoDataNascimento.addTextChangedListener(limparErroAoDigitar(containerDataNascimento, COR_BORDA_CAMPO));
        campoEmail.addTextChangedListener(limparErroAoDigitar(containerEmailCadastro, COR_BORDA_CAMPO));
        campoSenha.addTextChangedListener(limparErroAoDigitar(containerSenhaCadastro, COR_BORDA_CAMPO));
        campoConfirmarSenha.addTextChangedListener(limparErroAoDigitar(containerConfirmarSenha, COR_BORDA_CAMPO));
        campoCodigoCondominio.addTextChangedListener(limparErroAoDigitar(containerCodigoCondominio, COR_BORDA_CODIGO_ATIVO));
    }

    private TextWatcher limparErroAoDigitar(MaterialCardView card, int normalColor) {
        return new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) { }
            @Override public void onTextChanged(CharSequence s, int st, int before, int count) { }
            @Override public void afterTextChanged(Editable s) {
                FieldFeedback.clear(card, normalColor);
            }
        };
    }

    private boolean alternarVisibilidadeSenha(
            EditText campo, ImageView icone, boolean visivelAtualmente) {

        if (visivelAtualmente) {

            campo.setTransformationMethod(PasswordTransformationMethod.getInstance());

            icone.setImageResource(R.drawable.icon_olho_fechado);

            icone.setContentDescription("Mostrar senha");

        } else {

            campo.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

            icone.setImageResource(R.drawable.icon_olho_aberto);

            icone.setContentDescription("Ocultar senha");
        }

        campo.setSelection(campo.getText().length());

        return !visivelAtualmente;
    }

    private void abrirCalendario() {

        long hojeUtc = MaterialDatePicker.todayInUtcMilliseconds();

        /*
         * Por padrão o calendário já abre
         * aproximadamente 18 anos atrás.
         *
         * Para data de nascimento isso fica
         * bem mais agradável do que abrir em hoje.
         */
        Calendar calendarioInicial = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendarioInicial.add(Calendar.YEAR, -18);

        long selecaoInicial =
                converterDataParaMillisUtc(campoDataNascimento.getText().toString().trim());

        if (selecaoInicial <= 0L) {

            selecaoInicial = calendarioInicial.getTimeInMillis();
        }

        /*
         * Não permite selecionar uma data futura.
         */
        CalendarConstraints restricoes =
                new CalendarConstraints.Builder().setEnd(hojeUtc).setOpenAt(selecaoInicial).build();

        MaterialDatePicker<Long> seletorData =
                MaterialDatePicker.Builder.datePicker()
                        .setTheme(R.style.ThemeOverlay_EcoCiente_MaterialDatePicker)
                        .setTitleText("Data de nascimento")
                        .setSelection(selecaoInicial)
                        .setCalendarConstraints(restricoes)
                        .build();

        seletorData.addOnPositiveButtonClickListener(
                selecao -> campoDataNascimento.setText(formatarMillisUtcComoData(selecao)));

        seletorData.show(getParentFragmentManager(), "calendario_data_nascimento");
    }

    private long converterDataParaMillisUtc(String data) {

        if (!dataValida(data)) {
            return -1L;
        }

        String[] partes = data.split("/");

        if (partes.length != 3) {
            return -1L;
        }

        try {

            int dia = Integer.parseInt(partes[0]);

            int mes = Integer.parseInt(partes[1]) - 1;

            int ano = Integer.parseInt(partes[2]);

            Calendar calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

            calendario.clear();

            calendario.set(ano, mes, dia);

            return calendario.getTimeInMillis();

        } catch (NumberFormatException erro) {

            return -1L;
        }
    }

    private String formatarMillisUtcComoData(long millis) {

        Calendar calendario = Calendar.getInstance(TimeZone.getTimeZone("UTC"));

        calendario.setTimeInMillis(millis);

        return String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                calendario.get(Calendar.DAY_OF_MONTH),
                calendario.get(Calendar.MONTH) + 1,
                calendario.get(Calendar.YEAR));
    }

    private void configurarCodigoCondominio() {

        checkPossuiCodigoCondominio.setOnCheckedChangeListener(
                (buttonView, marcado) -> {
                    atualizarModoCodigoCondominio(marcado);

                    salvarDadosCodigoCondominio();
                });

        linhaPossuiCodigoCondominio.setOnClickListener(
                view ->
                        checkPossuiCodigoCondominio.setChecked(
                                !checkPossuiCodigoCondominio.isChecked()));
    }

    private void atualizarModoCodigoCondominio(boolean possuiCodigo) {

        campoCodigoCondominio.setEnabled(possuiCodigo);

        if (possuiCodigo) {

            /*
             * Agora só existe uma etapa.
             */
            containerEtapas.setVisibility(View.GONE);

            containerCodigoCondominio.setCardBackgroundColor(Color.parseColor("#FAFCFB"));

            containerCodigoCondominio.setStrokeColor(Color.parseColor("#064E3B"));

            botaoContinuar.setText(R.string.cadastrar_se);

        } else {

            containerEtapas.setVisibility(View.VISIBLE);

            containerCodigoCondominio.setCardBackgroundColor(Color.parseColor("#E1E3E2"));

            containerCodigoCondominio.setStrokeColor(Color.parseColor("#B6BAB8"));

            botaoContinuar.setText("Continuar");
        }
    }

    private void configurarContinuar() {

        botaoContinuar.setOnClickListener(view -> validarEtapa1());
        Motion.pressFeedback(botaoContinuar);
    }

    private void validarEtapa1() {

        String nome = campoNome.getText().toString().trim();

        String dataNascimento = campoDataNascimento.getText().toString().trim();

        String email = campoEmail.getText().toString().trim();

        String senha = campoSenha.getText().toString();

        String confirmarSenha = campoConfirmarSenha.getText().toString();

        String codigoCondominio = campoCodigoCondominio.getText().toString().trim();

        if (nome.length() < 3) {
            mostrarErroCampo(containerNome, campoNome, "Digite seu nome e sobrenome.");
            return;
        }

        if (!dataValida(dataNascimento)) {
            mostrarErroCampo(containerDataNascimento, campoDataNascimento, "Digite uma data de nascimento válida.");
            return;
        }

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            mostrarErroCampo(containerEmailCadastro, campoEmail, "Digite um e-mail válido.");
            return;
        }

        if (!senhaValida(senha)) {

            mostrarErroCampo(
                    containerSenhaCadastro,
                    campoSenha,
                    "A senha precisa ter 8+ caracteres, com maiúscula, minúscula, número e"
                            + " caractere especial.");
            return;
        }

        if (!senha.equals(confirmarSenha)) {
            mostrarErroCampo(containerConfirmarSenha, campoConfirmarSenha, "As senhas não são iguais.");
            return;
        }

        boolean possuiCodigoCondominio = checkPossuiCodigoCondominio.isChecked();

        if (possuiCodigoCondominio && codigoCondominio.isEmpty()) {
            mostrarErroCampo(containerCodigoCondominio, campoCodigoCondominio, "Digite o código do condomínio.");
            return;
        }

        Login telaAutenticacao = (Login) requireActivity();

        telaAutenticacao.salvarDadosEtapa1(nome, dataNascimento, email, senha);

        telaAutenticacao.salvarDadosCodigoCondominio(possuiCodigoCondominio, codigoCondominio);

        if (possuiCodigoCondominio) {

            cadastrarUsuarioFirebase();

        } else {

            telaAutenticacao.abrirCadastroEtapa2();
        }
    }

    private boolean senhaValida(String senha) {

        return PADRAO_SENHA_VALIDA.matcher(senha).matches();
    }

    private boolean dataValida(String data) {

        if (data.length() != 10) {
            return false;
        }

        SimpleDateFormat formato = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());

        formato.setLenient(false);

        try {

            Date dataInformada = formato.parse(data);

            return dataInformada != null && !dataInformada.after(new Date());

        } catch (ParseException erro) {

            return false;
        }
    }

    private void cadastrarUsuarioFirebase() {

        Login telaAutenticacao = (Login) requireActivity();

        String email = telaAutenticacao.getEmail();

        String senha = telaAutenticacao.getSenha();

        if (senha.isEmpty()) {

            mostrarMensagem("A senha não foi encontrada. Preencha novamente.");

            return;
        }

        viewModel
                .cadastrar(
                        telaAutenticacao.getNome(), email, senha, telaAutenticacao.montarDadosCadastro())
                .observe(getViewLifecycleOwner(), this::tratarResultadoCadastro);
    }

    private void tratarResultadoCadastro(ResultadoCadastro resultado) {

        if (!isAdded()) {
            return;
        }

        if (!resultado.isSucesso()) {

            mostrarMensagem(resultado.getMensagemErro());

            return;
        }

        entrarNoEcoCiente();
    }

    private void definirCarregando(boolean carregando) {

        botaoContinuar.setEnabled(!carregando);

        campoNome.setEnabled(!carregando);

        campoDataNascimento.setEnabled(!carregando);

        campoEmail.setEnabled(!carregando);

        campoSenha.setEnabled(!carregando);

        campoConfirmarSenha.setEnabled(!carregando);

        iconeCalendario.setEnabled(!carregando);

        iconeOlhoSenhaCadastro.setEnabled(!carregando);

        iconeOlhoConfirmarSenha.setEnabled(!carregando);

        checkPossuiCodigoCondominio.setEnabled(!carregando);

        linhaPossuiCodigoCondominio.setEnabled(!carregando);

        boolean possuiCodigo = checkPossuiCodigoCondominio.isChecked();

        campoCodigoCondominio.setEnabled(!carregando && possuiCodigo);

        if (carregando) {

            indicadorCarregamento.setVisibility(View.VISIBLE);

            botaoContinuar.setText("");

        } else {

            indicadorCarregamento.setVisibility(View.GONE);

            atualizarModoCodigoCondominio(possuiCodigo);
        }

        if (getActivity() instanceof Login) {

            ((Login) getActivity()).definirNavegacaoHabilitada(!carregando);
        }
    }

    private void recuperarDadosPreenchidos() {

        Login telaAutenticacao = (Login) requireActivity();

        campoNome.setText(telaAutenticacao.getNome());

        campoDataNascimento.setText(telaAutenticacao.getDataNascimento());

        campoEmail.setText(telaAutenticacao.getEmail());

        campoSenha.setText(telaAutenticacao.getSenha());

        campoConfirmarSenha.setText(telaAutenticacao.getSenha());

        campoCodigoCondominio.setText(telaAutenticacao.getCodigoCondominio());

        checkPossuiCodigoCondominio.setChecked(telaAutenticacao.isPossuiCodigoCondominio());

        atualizarModoCodigoCondominio(telaAutenticacao.isPossuiCodigoCondominio());
    }

    private void salvarDadosCodigoCondominio() {

        if (!(getActivity() instanceof Login)
                || checkPossuiCodigoCondominio == null
                || campoCodigoCondominio == null) {

            return;
        }

        ((Login) getActivity())
                .salvarDadosCodigoCondominio(
                        checkPossuiCodigoCondominio.isChecked(),
                        campoCodigoCondominio.getText().toString().trim());
    }

    private void salvarDadosAtuais() {

        if (!(getActivity() instanceof Login)
                || campoNome == null
                || campoDataNascimento == null
                || campoEmail == null
                || campoSenha == null
                || checkPossuiCodigoCondominio == null
                || campoCodigoCondominio == null) {

            return;
        }

        Login telaAutenticacao = (Login) getActivity();

        telaAutenticacao.salvarDadosEtapa1(
                campoNome.getText().toString().trim(),
                campoDataNascimento.getText().toString().trim(),
                campoEmail.getText().toString().trim(),
                campoSenha.getText().toString());

        telaAutenticacao.salvarDadosCodigoCondominio(
                checkPossuiCodigoCondominio.isChecked(),
                campoCodigoCondominio.getText().toString().trim());
    }

    private void entrarNoEcoCiente() {

        if (!isAdded()) {
            return;
        }

        Intent rota = new Intent(requireContext(), MainActivity.class);

        rota.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        startActivity(rota);

        requireActivity().finish();
    }

    private void mostrarErroCampo(
            MaterialCardView card,
            EditText campo,
            String mensagem
    ) {
        FieldFeedback.error(card, mensagem, motion);
        FieldFeedback.showErrorSnackbar(requireView(), mensagem);
        campo.requestFocus();
    }

    private void mostrarMensagem(String mensagem) {

        if (!isAdded()) {
            return;
        }

        FieldFeedback.showErrorSnackbar(requireView(), mensagem);
    }

    @Override
    public void onDestroyView() {

        salvarDadosAtuais();

        if (getActivity() instanceof Login) {

            ((Login) getActivity()).definirNavegacaoHabilitada(true);
        }

        campoNome = null;

        campoDataNascimento = null;

        campoEmail = null;

        campoSenha = null;

        campoConfirmarSenha = null;

        campoCodigoCondominio = null;

        iconeCalendario = null;

        iconeOlhoSenhaCadastro = null;

        iconeOlhoConfirmarSenha = null;

        containerEtapas = null;

        linhaPossuiCodigoCondominio = null;

        checkPossuiCodigoCondominio = null;

        containerNome = null;
        containerDataNascimento = null;
        containerEmailCadastro = null;
        containerSenhaCadastro = null;
        containerConfirmarSenha = null;
        containerCodigoCondominio = null;

        botaoContinuar = null;

        indicadorCarregamento = null;

        if (motion != null) {
            motion.cancelAll();
            motion = null;
        }

        super.onDestroyView();
    }
}