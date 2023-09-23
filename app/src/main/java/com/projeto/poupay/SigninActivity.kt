package com.projeto.poupay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.sql.Sql
import com.projeto.poupay.tools.FieldValidator

class SigninActivity : AppCompatActivity() {

    private lateinit var mFinishButton: Button
    private lateinit var mUsername: TextView
    private lateinit var mPassword: TextView
    private lateinit var mPasswordConfirm: TextView
    private lateinit var mProgressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tela_cadastro)

        mFinishButton = findViewById(R.id.btn_concluir)
        mUsername = findViewById(R.id.edt_usuario)
        mPassword = findViewById(R.id.edt_senha)
        mPasswordConfirm = findViewById(R.id.edt_confirmarSenha)
        mProgressBar = findViewById(R.id.pgr_progressbar)

        findViewById<TextView>(R.id.txt_versao).text = packageManager.getPackageInfo(packageName, 0).versionName

        mFinishButton.setOnClickListener {
            signUp()
        }
    }

    private fun signUp() {
        hideKeyboard()
        if (validateFields()) {
            setLoadingMode(true)
            val loginQuery = "INSERT INTO usuarios VALUES('${mUsername.text}', '${mPasswordConfirm.text}');"
            Sql(loginQuery, this) { _, exception ->
                if (exception == null) {
                    showSucessDialog()
                } else {
                    if (exception.message?.contains("duplicate key value violates unique constraint") == true) {
                        mUsername.error = "Nome de usuário já em uso."
                    } else {
                        MessageAlert.create(this, MessageAlert.Type.ERROR, "Aconteceu um problema de comunicação com o servidor.")
                    }
                }
                setLoadingMode(false)
            }.start()
        }
    }

    private fun showSucessDialog() {
        val alertContent = layoutInflater.inflate(R.layout.dialog_signup_sucess, findViewById(R.id.signup_sucess_main))
        val alert = AlertDialog.Builder(this)
        alert.setCancelable(false)
        alert.setView(alertContent)
        val dialog = alert.show()

        alertContent.findViewById<View>(R.id.signup_sucess_finish_button).setOnClickListener {
            dialog.dismiss()
            finish()
        }
    }

    private fun setLoadingMode(active: Boolean) {
        mUsername.isEnabled = !active
        mPassword.isEnabled = !active
        mPasswordConfirm.isEnabled = !active
        mFinishButton.text = if (active) "Cadastrando..." else "Concluir"
        mFinishButton.isEnabled = !active
        mProgressBar.visibility = if (active) View.VISIBLE else View.GONE
    }

    private fun validateFields(): Boolean {
        var pass = FieldValidator.validate(mUsername, FieldValidator.Type.USERNAME) &&
                FieldValidator.validate(mPassword, FieldValidator.Type.PASSWORD)
        if(mPassword.text.toString() != mPasswordConfirm.text.toString()){
            pass = false
            mPasswordConfirm.error = "As senhas não são iguais."
        }
        return pass
    }

    private fun hideKeyboard() {
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }
}