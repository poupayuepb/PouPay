package com.projeto.poupay

import android.animation.ValueAnimator
import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.AuthenticationCallback
import androidx.core.content.ContextCompat
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.sql.Preferences
import com.projeto.poupay.sql.Sql
import com.projeto.poupay.sql.SqlQueries
import com.projeto.poupay.tools.FieldValidator

class LoginActivity : AppCompatActivity() {
    private lateinit var mSignUpButton: TextView
    private lateinit var mSignInButton: Button
    private lateinit var mUsername: EditText
    private lateinit var mPassword: EditText
    private lateinit var mSaveLogin: CheckBox

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_login)

        mSignUpButton = findViewById(R.id.txt_cadastrarse)
        mSignInButton = findViewById(R.id.btn_entrar)
        mUsername = findViewById(R.id.edt_usuario)
        mPassword = findViewById(R.id.edt_senha)
        mSaveLogin = findViewById(R.id.Login_Save)

        findViewById<TextView>(R.id.txt_versao).text = packageManager.getPackageInfo(packageName, 0).versionName

        mSignUpButton.setOnClickListener {
            startActivity(Intent(this, SigninActivity::class.java))
        }

        mSignInButton.setOnClickListener {
            mUsername.clearFocus()
            mPassword.clearFocus()
            hideKeyboard()
            if (validateFields()) {
                setLoadingMode(true)
                val query = "SELECT nome, senha FROM usuarios WHERE nome = '${mUsername.text}';"
                Sql(query, this) { result, exception ->
                    if (result == null || exception != null) {
                        MessageAlert.create(this, MessageAlert.Type.ERROR, getString(R.string.connection_error))
                    } else {
                        var hasUser = false
                        while (result.next()) {
                            hasUser = true
                            if (result.getString("senha") == mPassword.text.toString()) {
                                SqlQueries.username = mUsername.text.toString()
                                checkLoginSave()
                                startActivity(Intent(this, MainActivity::class.java))
                                finish()
                            } else {
                                mPassword.error = "Senha incorreta."
                            }
                        }
                        if (!hasUser) MessageAlert.create(this, MessageAlert.Type.ERROR, "Usuário não cadastrado.")
                    }
                    setLoadingMode(false)
                }.start()
            }
        }

        if (Preferences.getBoolean(Preferences.Entry.REMIND_LOGIN_ENABLED, false, this)) {
            val executor = ContextCompat.getMainExecutor(this)
            val biometricPrompt = BiometricPrompt(this, executor, object : AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    mPassword.setText(Preferences.getString(Preferences.Entry.REMIND_LOGIN_PASSWORD, "", applicationContext))
                    mSignInButton.performClick()
                }
            })

            val biometricManager = BiometricManager.from(this)
            if (biometricManager.canAuthenticate(BIOMETRIC_STRONG) == BiometricManager.BIOMETRIC_SUCCESS) {
                val promptInfo = BiometricPrompt.PromptInfo.Builder()
                    .setTitle("Use seu sensor biométrico")
                    .setSubtitle("Realize o login no PouPay automaticamente")
                    .setAllowedAuthenticators(BIOMETRIC_STRONG)
                    .setNegativeButtonText("Inserir Senha Manualmente")
                    .build()
                biometricPrompt.authenticate(promptInfo)
            } else {
                mPassword.requestFocus()
            }

            mUsername.setText(Preferences.getString(Preferences.Entry.REMIND_LOGIN_USERNAME, "", this))
            mSaveLogin.isChecked = Preferences.getBoolean(Preferences.Entry.REMIND_LOGIN_ENABLED, false, this)
        }
    }

    private fun checkLoginSave() {
        Preferences.set(Preferences.Entry.REMIND_LOGIN_ENABLED, mSaveLogin.isChecked, this)
        Preferences.set(Preferences.Entry.REMIND_LOGIN_USERNAME, if (mSaveLogin.isChecked) mUsername.text.toString() else "", this)
        Preferences.set(Preferences.Entry.REMIND_LOGIN_PASSWORD, if (mSaveLogin.isChecked) mPassword.text.toString() else "", this)
    }

    private fun setLoadingMode(active: Boolean) {
        mSignInButton.isEnabled = !active
        mSignUpButton.isEnabled = !active

        val ripple = findViewById<FrameLayout>(R.id.login_ripple)
        val maxSize = resources.displayMetrics.heightPixels * 2
        val anim = ValueAnimator.ofInt(if (active) 0 else maxSize, if (active) maxSize else 0)
        anim.addUpdateListener {
            val params = ripple.layoutParams
            params.height = it.animatedValue as Int
            params.width = it.animatedValue as Int
            ripple.layoutParams = params
        }
        anim.duration = 500
        anim.start()
    }

    private fun validateFields(): Boolean {
        return FieldValidator.validate(mUsername, FieldValidator.Type.USERNAME)
                && FieldValidator.validate(mPassword, FieldValidator.Type.PASSWORD)
    }

    private fun hideKeyboard() {
        if (currentFocus != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
        }
    }
}