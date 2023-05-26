package com.project.poupay.view;

import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.project.poupay.BuildConfig;
import com.project.poupay.R;
import com.project.poupay.alerts.MessageAlert;
import com.project.poupay.sql.Sql;
import com.project.poupay.validators.FieldValidator;

import java.sql.SQLException;
import java.util.Objects;
import java.util.regex.Pattern;

public class TelaLogin extends AppCompatActivity {

    private TextView mSignUpButton;
    private Button mSignInButton;
    private EditText mUsername, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_login);

        mSignUpButton = findViewById(R.id.txt_cadastrarse);
        mSignInButton = findViewById(R.id.btn_entrar);
        mUsername = findViewById(R.id.edt_usuario);
        mPassword = findViewById(R.id.edt_senha);

        ((TextView) findViewById(R.id.txt_versao)).setText(BuildConfig.VERSION_NAME);

        mSignUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(TelaLogin.this, TelaCadastro.class);
            startActivity(intent);
        });

        mSignInButton.setOnClickListener(v -> {
            mUsername.clearFocus();
            mPassword.clearFocus();
            hideKeyboard();
            if (validateFields()) {
                setLoadingMode(true);
                String loginQuery = "SELECT nome, senha FROM usuarios WHERE nome='" + mUsername.getText() + "'";
                new Sql(this, loginQuery).setOnQueryCompleteListener((result, exception) -> {
                    try {
                        while (result.next()) {
                            if (Objects.equals(result.getString("senha"), mPassword.getText().toString())) {
                                Intent intent = new Intent(TelaLogin.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                mPassword.setError("Senha incorreta.");
                            }
                        }
                    } catch (SQLException e) {
                        MessageAlert.create(this, MessageAlert.TYPE_ERRO, getString(R.string.connection_error));
                    }
                    setLoadingMode(false);
                }).start();
            }
        });
    }

    private void setLoadingMode(boolean active) {
        mSignInButton.setEnabled(!active);
        mSignUpButton.setEnabled(!active);

        FrameLayout ripple = findViewById(R.id.login_ripple);
        int maxSize = getResources().getDisplayMetrics().heightPixels * 2;
        ValueAnimator anim = ValueAnimator.ofInt(active ? 0 : maxSize, active ? maxSize : 0);
        anim.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = ripple.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            params.width = (int) animation.getAnimatedValue();
            ripple.setLayoutParams(params);
        });
        anim.setDuration(500);
        anim.start();
    }

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) getSystemService(Activity.INPUT_METHOD_SERVICE);
        imm.toggleSoftInput(InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
    }

    private boolean validateFields() {
        return FieldValidator.validate(mUsername, FieldValidator.TYPE_USERNAME)
                && FieldValidator.validate(mPassword, FieldValidator.TYPE_PASSWORD);
    }
}