package com.example.ecociente.views;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.ecociente.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.checkbox.MaterialCheckBox;
import com.google.android.material.datepicker.CalendarConstraints;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class CadastroEtapa1Fragment extends Fragment {

    private EditText campoNome;
    private EditText campoDataNascimento;
    private EditText campoEmail;
    private EditText campoSenha;
    private EditText campoConfirmarSenha;
    private EditText campoCodigoCondominio;

    private ImageView iconeCalendario;

    private LinearLayout containerEtapas;
    private LinearLayout linhaPossuiCodigoCondominio;

    private MaterialCheckBox checkPossuiCodigoCondominio;

    private MaterialCardView containerCodigoCondominio;

    private MaterialButton botaoContinuar;

    private ProgressBar indicadorCarregamento;

    private FirebaseAuth autenticacaoFirebase;

    private boolean alterandoData =
            false;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle estadoSalvo
    ) {

        return inflater.inflate(
                R.layout.fragment_cadastro_etapa1,
                container,
                false
        );
    }

    @Override
    public void onViewCreated(
            @NonNull View view,
            @Nullable Bundle estadoSalvo
    ) {

        super.onViewCreated(
                view,
                estadoSalvo
        );

        inicializarComponentes(
                view
        );

        autenticacaoFirebase =
                FirebaseAuth.getInstance();

        configurarMascaraData();

        configurarCalendario();

        configurarCodigoCondominio();

        configurarContinuar();

        recuperarDadosPreenchidos();
    }

    private void inicializarComponentes(
            View view
    ) {

        campoNome =
                view.findViewById(
                        R.id.campoNome
                );

        campoDataNascimento =
                view.findViewById(
                        R.id.campoDataNascimento
                );

        campoEmail =
                view.findViewById(
                        R.id.campoEmailCadastro
                );

        campoSenha =
                view.findViewById(
                        R.id.campoSenhaCadastro
                );

        campoConfirmarSenha =
                view.findViewById(
                        R.id.campoConfirmarSenha
                );

        campoCodigoCondominio =
                view.findViewById(
                        R.id.campoCodigoCondominio
                );

        iconeCalendario =
                view.findViewById(
                        R.id.iconeCalendario
                );

        containerEtapas =
                view.findViewById(
                        R.id.containerEtapas
                );

        linhaPossuiCodigoCondominio =
                view.findViewById(
                        R.id.linhaPossuiCodigoCondominio
                );

        checkPossuiCodigoCondominio =
                view.findViewById(
                        R.id.checkPossuiCodigoCondominio
                );

        containerCodigoCondominio =
                view.findViewById(
                        R.id.containerCodigoCondominio
                );

        botaoContinuar =
                view.findViewById(
                        R.id.botaoContinuar
                );

        indicadorCarregamento =
                view.findViewById(
                        R.id.indicadorCarregamentoCadastroEtapa1
                );
    }

    private void configurarMascaraData() {

        campoDataNascimento.addTextChangedListener(
                new TextWatcher() {

                    @Override
                    public void beforeTextChanged(
                            CharSequence texto,
                            int inicio,
                            int quantidade,
                            int depois
                    ) {
                    }

                    @Override
                    public void onTextChanged(
                            CharSequence texto,
                            int inicio,
                            int antes,
                            int quantidade
                    ) {
                    }

                    @Override
                    public void afterTextChanged(
                            Editable editable
                    ) {

                        if (
                                alterandoData
                        ) {
                            return;
                        }

                        alterandoData =
                                true;

                        String numeros =
                                editable
                                        .toString()
                                        .replaceAll(
                                                "\\D",
                                                ""
                                        );

                        if (
                                numeros.length() > 8
                        ) {

                            numeros =
                                    numeros.substring(
                                            0,
                                            8
                                    );
                        }

                        StringBuilder formatado =
                                new StringBuilder();

                        if (
                                numeros.length() <= 2
                        ) {

                            formatado.append(
                                    numeros
                            );

                        } else {

                            formatado
                                    .append(
                                            numeros,
                                            0,
                                            2
                                    )
                                    .append(
                                            "/"
                                    );

                            if (
                                    numeros.length() <= 4
                            ) {

                                formatado.append(
                                        numeros.substring(
                                                2
                                        )
                                );

                            } else {

                                formatado
                                        .append(
                                                numeros,
                                                2,
                                                4
                                        )
                                        .append(
                                                "/"
                                        )
                                        .append(
                                                numeros.substring(
                                                        4
                                                )
                                        );
                            }
                        }

                        campoDataNascimento.setText(
                                formatado.toString()
                        );

                        campoDataNascimento.setSelection(
                                campoDataNascimento
                                        .getText()
                                        .length()
                        );

                        alterandoData =
                                false;
                    }
                }
        );
    }

    private void configurarCalendario() {

        iconeCalendario.setOnClickListener(
                view ->
                        abrirCalendario()
        );
    }

    private void abrirCalendario() {

        long hojeUtc =
                MaterialDatePicker
                        .todayInUtcMilliseconds();

        /*
         * Por padrão o calendário já abre
         * aproximadamente 18 anos atrás.
         *
         * Para data de nascimento isso fica
         * bem mais agradável do que abrir em hoje.
         */
        Calendar calendarioInicial =
                Calendar.getInstance(
                        TimeZone.getTimeZone(
                                "UTC"
                        )
                );

        calendarioInicial.add(
                Calendar.YEAR,
                -18
        );

        long selecaoInicial =
                converterDataParaMillisUtc(
                        campoDataNascimento
                                .getText()
                                .toString()
                                .trim()
                );

        if (
                selecaoInicial <= 0L
        ) {

            selecaoInicial =
                    calendarioInicial
                            .getTimeInMillis();
        }

        /*
         * Não permite selecionar uma data futura.
         */
        CalendarConstraints restricoes =
                new CalendarConstraints
                        .Builder()
                        .setEnd(
                                hojeUtc
                        )
                        .setOpenAt(
                                selecaoInicial
                        )
                        .build();

        MaterialDatePicker<Long> seletorData =
                MaterialDatePicker
                        .Builder
                        .datePicker()
                        .setTheme(
                                R.style.ThemeOverlay_EcoCiente_MaterialDatePicker
                        )
                        .setTitleText(
                                "Data de nascimento"
                        )
                        .setSelection(
                                selecaoInicial
                        )
                        .setCalendarConstraints(
                                restricoes
                        )
                        .build();

        seletorData
                .addOnPositiveButtonClickListener(
                        selecao ->

                                campoDataNascimento.setText(
                                        formatarMillisUtcComoData(
                                                selecao
                                        )
                                )
                );

        seletorData.show(
                getParentFragmentManager(),
                "calendario_data_nascimento"
        );
    }

    private long converterDataParaMillisUtc(
            String data
    ) {

        if (
                !dataValida(
                        data
                )
        ) {
            return -1L;
        }

        String[] partes =
                data.split(
                        "/"
                );

        if (
                partes.length != 3
        ) {
            return -1L;
        }

        try {

            int dia =
                    Integer.parseInt(
                            partes[0]
                    );

            int mes =
                    Integer.parseInt(
                            partes[1]
                    ) - 1;

            int ano =
                    Integer.parseInt(
                            partes[2]
                    );

            Calendar calendario =
                    Calendar.getInstance(
                            TimeZone.getTimeZone(
                                    "UTC"
                            )
                    );

            calendario.clear();

            calendario.set(
                    ano,
                    mes,
                    dia
            );

            return calendario.getTimeInMillis();

        } catch (
                NumberFormatException erro
        ) {

            return -1L;
        }
    }

    private String formatarMillisUtcComoData(
            long millis
    ) {

        Calendar calendario =
                Calendar.getInstance(
                        TimeZone.getTimeZone(
                                "UTC"
                        )
                );

        calendario.setTimeInMillis(
                millis
        );

        return String.format(
                Locale.getDefault(),
                "%02d/%02d/%04d",
                calendario.get(
                        Calendar.DAY_OF_MONTH
                ),
                calendario.get(
                        Calendar.MONTH
                ) + 1,
                calendario.get(
                        Calendar.YEAR
                )
        );
    }

    private void configurarCodigoCondominio() {

        checkPossuiCodigoCondominio
                .setOnCheckedChangeListener(

                        (buttonView, marcado) -> {

                            atualizarModoCodigoCondominio(
                                    marcado
                            );

                            salvarDadosCodigoCondominio();
                        }
                );

        linhaPossuiCodigoCondominio
                .setOnClickListener(

                        view ->
                                checkPossuiCodigoCondominio
                                        .setChecked(
                                                !checkPossuiCodigoCondominio
                                                        .isChecked()
                                        )
                );
    }

    private void atualizarModoCodigoCondominio(
            boolean possuiCodigo
    ) {

        campoCodigoCondominio.setEnabled(
                possuiCodigo
        );

        if (
                possuiCodigo
        ) {

            /*
             * Agora só existe uma etapa.
             */
            containerEtapas.setVisibility(
                    View.GONE
            );

            containerCodigoCondominio
                    .setCardBackgroundColor(
                            Color.parseColor(
                                    "#FAFCFB"
                            )
                    );

            containerCodigoCondominio
                    .setStrokeColor(
                            Color.parseColor(
                                    "#064E3B"
                            )
                    );

            botaoContinuar.setText(
                    R.string.cadastrar_se
            );

        } else {

            containerEtapas.setVisibility(
                    View.VISIBLE
            );

            containerCodigoCondominio
                    .setCardBackgroundColor(
                            Color.parseColor(
                                    "#E1E3E2"
                            )
                    );

            containerCodigoCondominio
                    .setStrokeColor(
                            Color.parseColor(
                                    "#B6BAB8"
                            )
                    );

            botaoContinuar.setText(
                    "Continuar"
            );
        }
    }

    private void configurarContinuar() {

        botaoContinuar
                .setOnClickListener(
                        view ->
                                validarEtapa1()
                );
    }

    private void validarEtapa1() {

        String nome =
                campoNome
                        .getText()
                        .toString()
                        .trim();

        String dataNascimento =
                campoDataNascimento
                        .getText()
                        .toString()
                        .trim();

        String email =
                campoEmail
                        .getText()
                        .toString()
                        .trim();

        String senha =
                campoSenha
                        .getText()
                        .toString();

        String confirmarSenha =
                campoConfirmarSenha
                        .getText()
                        .toString();

        String codigoCondominio =
                campoCodigoCondominio
                        .getText()
                        .toString()
                        .trim();

        if (
                nome.length() < 3
        ) {

            mostrarMensagem(
                    "Digite seu nome e sobrenome."
            );

            campoNome.requestFocus();

            return;
        }

        if (
                !dataValida(
                        dataNascimento
                )
        ) {

            mostrarMensagem(
                    "Digite uma data de nascimento válida."
            );

            campoDataNascimento.requestFocus();

            return;
        }

        if (
                email.isEmpty()
                        ||
                        !Patterns.EMAIL_ADDRESS
                                .matcher(
                                        email
                                )
                                .matches()
        ) {

            mostrarMensagem(
                    "Digite um e-mail válido."
            );

            campoEmail.requestFocus();

            return;
        }

        if (
                senha.length() < 6
        ) {

            mostrarMensagem(
                    "A senha deve possuir pelo menos 6 caracteres."
            );

            campoSenha.requestFocus();

            return;
        }

        if (
                !senha.equals(
                        confirmarSenha
                )
        ) {

            mostrarMensagem(
                    "As senhas não são iguais."
            );

            campoConfirmarSenha.requestFocus();

            return;
        }

        boolean possuiCodigoCondominio =
                checkPossuiCodigoCondominio
                        .isChecked();

        if (
                possuiCodigoCondominio
                        &&
                        codigoCondominio.isEmpty()
        ) {

            mostrarMensagem(
                    "Digite o código do condomínio."
            );

            campoCodigoCondominio.requestFocus();

            return;
        }

        Login telaAutenticacao =
                (Login) requireActivity();

        telaAutenticacao
                .salvarDadosEtapa1(
                        nome,
                        dataNascimento,
                        email,
                        senha
                );

        telaAutenticacao
                .salvarDadosCodigoCondominio(
                        possuiCodigoCondominio,
                        codigoCondominio
                );

        if (
                possuiCodigoCondominio
        ) {

            cadastrarUsuarioFirebase();

        } else {

            telaAutenticacao
                    .abrirCadastroEtapa2();
        }
    }

    private boolean dataValida(
            String data
    ) {

        if (
                data.length() != 10
        ) {
            return false;
        }

        SimpleDateFormat formato =
                new SimpleDateFormat(
                        "dd/MM/yyyy",
                        Locale.getDefault()
                );

        formato.setLenient(
                false
        );

        try {

            Date dataInformada =
                    formato.parse(
                            data
                    );

            return dataInformada != null
                    &&
                    !dataInformada.after(
                            new Date()
                    );

        } catch (
                ParseException erro
        ) {

            return false;
        }
    }

    private void cadastrarUsuarioFirebase() {

        Login telaAutenticacao =
                (Login) requireActivity();

        String email =
                telaAutenticacao.getEmail();

        String senha =
                telaAutenticacao.getSenha();

        if (
                senha.isEmpty()
        ) {

            mostrarMensagem(
                    "A senha não foi encontrada. Preencha novamente."
            );

            return;
        }

        definirCarregando(
                true
        );

        autenticacaoFirebase
                .createUserWithEmailAndPassword(
                        email,
                        senha
                )
                .addOnCompleteListener(

                        requireActivity(),

                        tarefa -> {

                            if (
                                    !isAdded()
                            ) {
                                return;
                            }

                            if (
                                    tarefa.isSuccessful()
                            ) {

                                atualizarNomeUsuario();

                                return;
                            }

                            definirCarregando(
                                    false
                            );

                            tratarErroCadastro(
                                    tarefa.getException()
                            );
                        }
                );
    }

    private void atualizarNomeUsuario() {

        FirebaseUser usuario =
                autenticacaoFirebase
                        .getCurrentUser();

        if (
                usuario == null
        ) {

            definirCarregando(
                    false
            );

            mostrarMensagem(
                    "A conta foi criada, mas não foi possível carregar o usuário."
            );

            return;
        }

        String nome =
                ((Login) requireActivity())
                        .getNome();

        UserProfileChangeRequest perfil =
                new UserProfileChangeRequest
                        .Builder()
                        .setDisplayName(
                                nome
                        )
                        .build();

        usuario
                .updateProfile(
                        perfil
                )
                .addOnCompleteListener(

                        tarefa -> {

                            if (
                                    !isAdded()
                            ) {
                                return;
                            }

                            definirCarregando(
                                    false
                            );

                            if (
                                    !tarefa.isSuccessful()
                            ) {

                                mostrarMensagem(
                                        "Conta criada, mas não foi possível atualizar o nome do perfil."
                                );
                            }

                            entrarNoEcoCiente();
                        }
                );
    }

    private void tratarErroCadastro(
            Exception erro
    ) {

        if (
                erro
                        instanceof FirebaseAuthUserCollisionException
        ) {

            mostrarMensagem(
                    "Já existe uma conta cadastrada com este e-mail."
            );

            return;
        }

        if (
                erro
                        instanceof FirebaseAuthWeakPasswordException
        ) {

            mostrarMensagem(
                    "A senha informada é muito fraca."
            );

            return;
        }

        if (
                erro
                        instanceof FirebaseAuthInvalidCredentialsException
        ) {

            mostrarMensagem(
                    "O e-mail informado é inválido."
            );

            return;
        }

        if (
                erro != null
                        &&
                        erro.getLocalizedMessage()
                                != null
        ) {

            mostrarMensagem(
                    erro.getLocalizedMessage()
            );

            return;
        }

        mostrarMensagem(
                "Não foi possível criar sua conta."
        );
    }

    private void definirCarregando(
            boolean carregando
    ) {

        botaoContinuar.setEnabled(
                !carregando
        );

        campoNome.setEnabled(
                !carregando
        );

        campoDataNascimento.setEnabled(
                !carregando
        );

        campoEmail.setEnabled(
                !carregando
        );

        campoSenha.setEnabled(
                !carregando
        );

        campoConfirmarSenha.setEnabled(
                !carregando
        );

        iconeCalendario.setEnabled(
                !carregando
        );

        checkPossuiCodigoCondominio
                .setEnabled(
                        !carregando
                );

        linhaPossuiCodigoCondominio
                .setEnabled(
                        !carregando
                );

        boolean possuiCodigo =
                checkPossuiCodigoCondominio
                        .isChecked();

        campoCodigoCondominio
                .setEnabled(
                        !carregando
                                &&
                                possuiCodigo
                );

        if (
                carregando
        ) {

            indicadorCarregamento.setVisibility(
                    View.VISIBLE
            );

            botaoContinuar.setText(
                    ""
            );

        } else {

            indicadorCarregamento.setVisibility(
                    View.GONE
            );

            atualizarModoCodigoCondominio(
                    possuiCodigo
            );
        }

        if (
                getActivity()
                        instanceof Login
        ) {

            ((Login) getActivity())
                    .definirNavegacaoHabilitada(
                            !carregando
                    );
        }
    }

    private void recuperarDadosPreenchidos() {

        Login telaAutenticacao =
                (Login) requireActivity();

        campoNome.setText(
                telaAutenticacao.getNome()
        );

        campoDataNascimento.setText(
                telaAutenticacao
                        .getDataNascimento()
        );

        campoEmail.setText(
                telaAutenticacao.getEmail()
        );

        campoSenha.setText(
                telaAutenticacao.getSenha()
        );

        campoConfirmarSenha.setText(
                telaAutenticacao.getSenha()
        );

        campoCodigoCondominio.setText(
                telaAutenticacao
                        .getCodigoCondominio()
        );

        checkPossuiCodigoCondominio
                .setChecked(
                        telaAutenticacao
                                .isPossuiCodigoCondominio()
                );

        atualizarModoCodigoCondominio(
                telaAutenticacao
                        .isPossuiCodigoCondominio()
        );
    }

    private void salvarDadosCodigoCondominio() {

        if (
                !(getActivity()
                        instanceof Login)
                        ||
                        checkPossuiCodigoCondominio
                                == null
                        ||
                        campoCodigoCondominio
                                == null
        ) {

            return;
        }

        ((Login) getActivity())
                .salvarDadosCodigoCondominio(

                        checkPossuiCodigoCondominio
                                .isChecked(),

                        campoCodigoCondominio
                                .getText()
                                .toString()
                                .trim()
                );
    }

    private void salvarDadosAtuais() {

        if (
                !(getActivity()
                        instanceof Login)
                        ||
                        campoNome == null
                        ||
                        campoDataNascimento == null
                        ||
                        campoEmail == null
                        ||
                        campoSenha == null
                        ||
                        checkPossuiCodigoCondominio
                                == null
                        ||
                        campoCodigoCondominio
                                == null
        ) {

            return;
        }

        Login telaAutenticacao =
                (Login) getActivity();

        telaAutenticacao
                .salvarDadosEtapa1(

                        campoNome
                                .getText()
                                .toString()
                                .trim(),

                        campoDataNascimento
                                .getText()
                                .toString()
                                .trim(),

                        campoEmail
                                .getText()
                                .toString()
                                .trim(),

                        campoSenha
                                .getText()
                                .toString()
                );

        telaAutenticacao
                .salvarDadosCodigoCondominio(

                        checkPossuiCodigoCondominio
                                .isChecked(),

                        campoCodigoCondominio
                                .getText()
                                .toString()
                                .trim()
                );
    }

    private void entrarNoEcoCiente() {

        if (
                !isAdded()
        ) {
            return;
        }

        Intent rota =
                new Intent(
                        requireContext(),
                        MainActivity.class
                );

        rota.addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK
                        |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK
        );

        startActivity(
                rota
        );

        requireActivity()
                .finish();
    }

    private void mostrarMensagem(
            String mensagem
    ) {

        if (
                !isAdded()
        ) {
            return;
        }

        Toast.makeText(
                requireContext(),
                mensagem,
                Toast.LENGTH_SHORT
        ).show();
    }

    @Override
    public void onDestroyView() {

        salvarDadosAtuais();

        if (
                getActivity()
                        instanceof Login
        ) {

            ((Login) getActivity())
                    .definirNavegacaoHabilitada(
                            true
                    );
        }

        campoNome =
                null;

        campoDataNascimento =
                null;

        campoEmail =
                null;

        campoSenha =
                null;

        campoConfirmarSenha =
                null;

        campoCodigoCondominio =
                null;

        iconeCalendario =
                null;

        containerEtapas =
                null;

        linhaPossuiCodigoCondominio =
                null;

        checkPossuiCodigoCondominio =
                null;

        containerCodigoCondominio =
                null;

        botaoContinuar =
                null;

        indicadorCarregamento =
                null;

        super.onDestroyView();
    }
}