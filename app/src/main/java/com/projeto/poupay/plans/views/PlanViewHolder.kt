package com.projeto.poupay.plans.views

import android.animation.ValueAnimator
import android.app.Activity
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.marcoscg.currencyedittext.CurrencyEditText
import com.projeto.poupay.R
import com.projeto.poupay.sql.SqlQueries
import java.text.NumberFormat
import java.util.Locale

class PlanViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
    private var titleText: TextView
    private var subtitleText: TextView
    private var valueActual: TextView
    private var valueTotal: TextView
    private var titleDate: TextView
    private var content: LinearLayout
    private var addLayout: LinearLayout
    private var addButton: AppCompatButton
    private var addValue: CurrencyEditText
    private var addDetails: EditText

    private var planId = 0
    private var isExpanded = false
    private var onAddButtonClickListener: (id: Int, value: Double, details: String) -> Unit = { _, _, _ -> }
    private var onPlanDeleteListener: (isSucess: Boolean) -> Unit = {}

    init {
        view.apply {
            titleText = findViewById(R.id.Plan_Title)
            subtitleText = findViewById(R.id.Plan_Subtitle)
            valueActual = findViewById(R.id.Plan_Completed)
            valueTotal = findViewById(R.id.Plan_Total)
            content = findViewById(R.id.Plan_List)
            addLayout = findViewById(R.id.Plans_AddLayout)
            titleDate = findViewById(R.id.Plan_Date)
            addButton = findViewById(R.id.Plan_Add)
            addValue = findViewById(R.id.AddPlan_Value)
            addDetails = findViewById(R.id.AddPlan_Details)

            addButton.setOnClickListener {
                expandAddContribuitionToggle(this)
                onAddButtonClickListener.invoke(planId, addValue.getNumericValue(), addDetails.text.toString())
            }

            findViewById<View>(R.id.Plan_AddFab).setOnClickListener { expandAddContribuitionToggle(it) }
        }
    }

    private fun expandAddContribuitionToggle(view: View) {
        isExpanded = !isExpanded
        val anim = ValueAnimator.ofInt(addLayout.height, if (isExpanded) view.resources.getDimension(R.dimen._200dp).toInt() else 0)
        anim.addUpdateListener {
            val params = addLayout.layoutParams
            params.height = it.animatedValue as Int
            addLayout.layoutParams = params
        }
        anim.duration = 400
        anim.start()

        addButton.visibility = if (isExpanded) View.VISIBLE else View.INVISIBLE

        val addFab = view.findViewById<FloatingActionButton>(R.id.Plan_AddFab)
        addFab.startAnimation(AnimationUtils.loadAnimation(view.context, if (isExpanded) R.anim.rotate45hour else R.anim.rotate45antihour))
    }

    fun setup(id: Int, title: String, subtitle: String, date: String, value: Double, items: List<ContentPlanItem>) {
        var total = 0.0
        this.planId = id
        titleText.text = title
        subtitleText.text = subtitle
        titleDate.text = String.format("Previsão: %s", date)
        content.removeAllViews()
        for (item in items) {
            content.addView(item)
            total += item.value
        }
        valueActual.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)
        valueTotal.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total)
        view.findViewById<View>(R.id.Plan_DeletePlan).setOnClickListener {
            val alertContent = (view.context as Activity).layoutInflater.inflate(R.layout.dialog_content_delete, view.findViewById(R.id.ContentDelete_Root))
            val alert = AlertDialog.Builder(view.context)
            alert.setView(alertContent)
            val dialog = alert.show()

            alertContent.apply {
                findViewById<TextView>(R.id.ContentDelete_Details).text = buildString {
                    append("Plano: ")
                    append(title)
                }
                findViewById<View>(R.id.ContentDelete_Cancel).setOnClickListener { dialog.dismiss() }
                findViewById<View>(R.id.ContentDelete_Confirm).setOnClickListener {
                    SqlQueries.deletePlan(planId, view.context, {
                        onPlanDeleteListener.invoke(true)
                    }, {
                        onPlanDeleteListener.invoke(false)
                    })
                    dialog.dismiss()
                }
            }
        }

    }

    fun setOnPlanDeleteListener(listener: (isSucess: Boolean) -> Unit) {
        this.onPlanDeleteListener = listener
    }

    fun setOnAddButtonClickListener(listener: (id: Int, value: Double, details: String) -> Unit) {
        this.onAddButtonClickListener = listener
    }
}