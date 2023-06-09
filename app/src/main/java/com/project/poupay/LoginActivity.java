package com.project.poupay;

import static androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG;

import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.project.poupay.alerts.MessageAlert;
import com.project.poupay.sql.Preferences;
import com.project.poupay.sql.Sql;
import com.project.poupay.sql.User;
import com.project.poupay.tools.FieldValidator;

import java.sql.SQLException;
import java.util.Objects;
import java.util.concurrent.Executor;

public class LoginActivity extends AppCompatActivity {

    private TextView mSignUpButton;
    private Button mSignInButton;
    private EditText mUsername, mPassword;
    private CheckBox mSaveLogin;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tela_login);

        mSignUpButton = findViewById(R.id.txt_cadastrarse);
        mSignInButton = findViewById(R.id.btn_entrar);
        mUsername = findViewById(R.id.edt_usuario);
        mPassword = findViewById(R.id.edt_senha);
        mSaveLogin = findViewById(R.id.Login_Save);

        ((TextView) findViewById(R.id.txt_versao)).setText(BuildConfig.VERSION_NAME);

        mSignUpButton.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, SigninActivity.class);
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
                        boolean emptyUser = true;
                        while (result.next()) {
                            emptyUser = false;
                            if (Objects.equals(result.getString("senha"), mPassword.getText().toString())) {
                                User.username = mUsername.getText().toString();
                                checkLoginSave();
                                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                            } else {
                                mPassword.setError("Senha incorreta.");
                            }
                        }
                        if (emptyUser) MessageAlert.create(this, MessageAlert.TYPE_ERRO, "Usuário não cadastrado.");
                    } catch (SQLException e) {
                        MessageAlert.create(this, MessageAlert.TYPE_ERRO, getString(R.string.connection_error));
                    }
                    setLoadingMode(false);
                }).start();
            }
        });


        if (Preferences.getBool(Preferences.REMIND_LOGIN_ENABLED, false, this)) {
            Executor executor = ContextCompat.getMainExecutor(this);
            BiometricPrompt biometricPrompt = new BiometricPrompt(LoginActivity.this, executor, new BiometricPrompt.AuthenticationCallback() {
                @Override
                public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                    super.onAuthenticationError(errorCode, errString);
                }

                @Override
                public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                    super.onAuthenticationSucceeded(result);
                    mPassword.setText(Preferences.getString(Preferences.REMIND_LOGIN_PASSWORD, "", LoginActivity.this));
                    mSignInButton.performClick();
                }

                @Override
                public void onAuthenticationFailed() {
                    super.onAuthenticationFailed();
                    MessageAlert.create(LoginActivity.this, MessageAlert.TYPE_ERRO, "Não foi possível realizar sua verificação.");
                }
            });
            BiometricManager biometricManager = BiometricManager.from(this);
            if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                        .setTitle("Use seu sensor biométrico")
                        .setSubtitle("Realize o login no PouPay automaticamente")
                        .setAllowedAuthenticators(BIOMETRIC_STRONG)
                        .setNegativeButtonText("Inserir Senha Manualmente")
                        .build();
                biometricPrompt.authenticate(promptInfo);
            } else{
                mPassword.requestFocus();
            }

            mUsername.setText(Preferences.getString(Preferences.REMIND_LOGIN_USERNAME, "", LoginActivity.this));
            mSaveLogin.setChecked(Preferences.getBool(Preferences.REMIND_LOGIN_ENABLED, false, this));
        }
    }

    private void checkLoginSave() {
        Preferences.set(Preferences.REMIND_LOGIN_ENABLED, mSaveLogin.isChecked(), this);
        Preferences.set(Preferences.REMIND_LOGIN_USERNAME, mSaveLogin.isChecked() ? mUsername.getText().toString() : "", this);
        Preferences.set(Preferences.REMIND_LOGIN_PASSWORD, mSaveLogin.isChecked() ? mPassword.getText().toString() : "", this);
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
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    private boolean validateFields() {
        return FieldValidator.validate(mUsername, FieldValidator.TYPE_USERNAME)
                && FieldValidator.validate(mPassword, FieldValidator.TYPE_PASSWORD);
    }
}