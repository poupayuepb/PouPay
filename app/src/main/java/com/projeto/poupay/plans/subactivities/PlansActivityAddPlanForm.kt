package com.projeto.poupay.plans.subactivities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.datepicker.MaterialDatePicker
import com.marcoscg.currencyedittext.CurrencyEditText
import com.projeto.poupay.PlansActivity
import com.projeto.poupay.R
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.sql.SqlQueries
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class PlansActivityAddPlanForm : AppCompatActivity() {

    private lateinit var value: CurrencyEditText
    private lateinit var goal: TextView
    private lateinit var instituition: EditText
    private lateinit var date: TextView
    private lateinit var confirmButton: Button

    private var isLoadingModeOn = false
    private var dateFormated = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plans_addplanform)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        value = findViewById(R.id.AddPlanForm_Value)
        goal = findViewById(R.id.AddPlanForm_Goal)
        instituition = findViewById(R.id.AddPlanForm_FinancialInstitution)
        date = findViewById(R.id.AddPlanForm_EstimatedDateText)
        confirmButton = findViewById(R.id.NewPlan_Confirm)

        goal.text = intent.extras?.getString("goal", "") ?: ""

        findViewById<View>(R.id.AddPlanForm_Back).setOnClickListener { onBackPressed() }
        findViewById<View>(R.id.AddPlanForm_EstimatedDatePicker).setOnClickListener { showDatePickerDialog() }
        confirmButton.setOnClickListener { confirm() }
    }

    override fun onBackPressed() {
        if (isLoadingModeOn) {
            startActivity(Intent(this, PlansActivity::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.slide_out)
            finish()
        }
    }

    private fun confirm() {
        if (!isLoadingModeOn && validateAddFields()) {
            setLoadingMode(true)
            SqlQueries.addPlan(goal.text.toString(), value.getNumericValue(), instituition.text.toString(), dateFormated, this, {
                startActivity(Intent(this, PlansActivity::class.java))
                overridePendingTransition(R.anim.slide_down, R.anim.slide_out)
                finish()
            }, {
                MessageAlert.create(this, MessageAlert.Type.ERROR, "Aconteceu um erro ao tentar adicionar este plano.")
                setLoadingMode(false)
            })
        }
    }

    private fun setLoadingMode(active: Boolean) {
        isLoadingModeOn = active
        value.isEnabled = !active
        instituition.isEnabled = !active
        findViewById<View>(R.id.AddPlanForm_Back).isEnabled = !active
        confirmButton.text = if (active) " " else getString(R.string.btn_confirm)
        findViewById<View>(R.id.AddPlanForm_Progress).visibility = if (active) View.VISIBLE else View.INVISIBLE
    }

    private fun showDatePickerDialog() {
        if (!isLoadingModeOn) {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECIONE UMA DATA")
                .setPositiveButtonText("Selecionar")
                .build()

            materialDatePicker.show(supportFragmentManager, "MATERAL_DATE_PICKER")
            materialDatePicker.addOnPositiveButtonClickListener {
                date.error = null
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                calendar.timeInMillis = it
                val textDate = format.format(calendar.time)
                date.text = textDate

                val format2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                dateFormated = format2.format(calendar.time)
            }

        }
    }

    private fun validateAddFields(): Boolean {
        var isValid = true
        if (value.getNumericValue() == 0.0 || value.text.toString().isEmpty()) {
            value.error = "Esse campo deve ser preenchido."
            isValid = false
        }
        if (value.getNumericValue() < 0) {
            value.error = "Esse campo deve conter apenas valor positivo."
            isValid = false
        }

        if (instituition.text.toString().isEmpty()) {
            value.error = "Esse campo deve ser preenchido"
            isValid = false
        }
        if (date.text.toString().isEmpty() || date.text.toString() == getString(R.string.NewPlan_estimatedDate)) {
            value.error = "Esse campo deve ser preenchido"
            isValid = false
        }
        return isValid
    }
}