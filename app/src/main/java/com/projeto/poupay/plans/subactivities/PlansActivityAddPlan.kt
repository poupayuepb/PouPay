package com.projeto.poupay.plans.subactivities

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.EditText
import com.projeto.poupay.PlansActivity
import com.projeto.poupay.R

class PlansActivityAddPlan : AppCompatActivity() {

    private lateinit var goalText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plans_addplan)
        window.statusBarColor = Color.WHITE
        window.navigationBarColor = Color.WHITE

        goalText = findViewById(R.id.AddPlan_OtherInput)

        findViewById<View>(R.id.AddPlan_Back).setOnClickListener { onBackPressed() }
        findViewById<View>(R.id.AddPlan_Investiment).setOnClickListener { startFormActivity("Investimento") }
        findViewById<View>(R.id.AddPlan_Travel).setOnClickListener { startFormActivity("Viagem") }
        findViewById<View>(R.id.AddPlan_Reserve).setOnClickListener { startFormActivity("Reserva") }
        findViewById<View>(R.id.AddPlan_Debt).setOnClickListener { startFormActivity("Dívida") }
        findViewById<View>(R.id.AddPlan_Confirm).setOnClickListener {
            if(validateGoalField()) startFormActivity(goalText.text.toString())
        }
    }

    override fun onBackPressed() {
        startActivity(Intent(this, PlansActivity::class.java))
        overridePendingTransition(R.anim.slide_down, R.anim.slide_out)
        finish()
    }


    private fun validateGoalField(): Boolean {
        var isValid = true
        if(goalText.text.isEmpty()){
            goalText.error = "Esse campo deve ser preenchido."
            isValid = false
        }
        return isValid
    }

    private fun startFormActivity(goal: String) {
        val intent = Intent(this, PlansActivityAddPlanForm::class.java)
        intent.putExtra("goal", goal)
        startActivity(intent)
        overridePendingTransition(R.anim.slide_up, R.anim.slide_in)
        finish()
    }
}