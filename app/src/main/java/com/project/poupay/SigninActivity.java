
package com.project.poupay;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.project.poupay.alerts.MessageAlert;
import com.project.poupay.sql.Sql;
import com.project.poupay.tools.FieldValidator;

import java.util.Objects;

public class SigninActivity extends AppCompatActivity {

    private Button mFinishButton;
    private EditText mUsername, mPassword, mPasswordConfirm;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_cadastro);

        mFinishButton = findViewById(R.id.btn_concluir);
        mUsername = findViewById(R.id.edt_usuario);
        mPassword = findViewById(R.id.edt_senha);
        mPasswordConfirm = findViewById(R.id.edt_confirmarSenha);
        mProgressBar = findViewById(R.id.pgr_progressbar);

        ((TextView) findViewById(R.id.txt_versao)).setText(BuildConfig.VERSION_NAME);

        mFinishButton.setOnClickListener(v -> signUp());
    }

    private void signUp() {
        hideKeyboard();
        if (validateFields()) {
            setLoadingMode(true);
            String loginQuery = "INSERT INTO usuarios VALUES('" + mUsername.getText().toString() + "', '" + mPasswordConfirm.getText().toString() + "');";
            new Sql(this, loginQuery).setOnQueryCompleteListener((result, exception) -> {
                if (exception == null) {
                    showSucessDialog();
                } else {
                    if (Objects.requireNonNull(exception.getMessage()).contains("duplicate key value violates unique constraint")) {
                        mUsername.setError("Nome de usuário já em uso.");
                    } else {
                        MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Aconteceu um problema de comunicação com o servidor.");
                    }
                }
                setLoadingMode(false);
            }).start();
        }
    }

    private void showSucessDialog() {
        View alertContent = getLayoutInflater().inflate(R.layout.dialog_signup_sucess, findViewById(R.id.signup_sucess_main));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setView(alertContent);
        Dialog dialog = alert.show();

        alertContent.findViewById(R.id.signup_sucess_finish_button).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
    }

    public void setLoadingMode(boolean active) {
        mUsername.setEnabled(!active);
        mPassword.setEnabled(!active);
        mPasswordConfirm.setEnabled(!active);
        mFinishButton.setText(active ? "Cadastrando..." : "Concluir");
        mFinishButton.setEnabled(!active);
        mProgressBar.setVisibility(active ? View.VISIBLE : View.GONE);
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private boolean validateFields() {
        boolean pass = FieldValidator.validate(mUsername, FieldValidator.TYPE_USERNAME)
                && FieldValidator.validate(mPassword, FieldValidator.TYPE_PASSWORD);
        if (!mPassword.getText().toString().equals(mPasswordConfirm.getText().toString())) {
            pass = false;
            mPasswordConfirm.setError("As senhas não são íguais.");
        }
        return pass;
    }
}