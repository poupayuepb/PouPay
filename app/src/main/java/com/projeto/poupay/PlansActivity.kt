package com.projeto.poupay

import android.content.Intent
import android.graphics.Color
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.TextView
import androidx.appcompat.widget.AppCompatButton
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.plans.subactivities.PlansActivityAddPlan
import com.projeto.poupay.plans.views.ListPlansAdapter
import com.projeto.poupay.sql.SqlQueries
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PlansActivity : AppCompatActivity() {

    private lateinit var adapter: ListPlansAdapter
    private lateinit var addButton: AppCompatButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_plans)
        window.navigationBarColor = Color.WHITE

        addButton = findViewById(R.id.Plans_AddPlan)
        addButton.setOnClickListener {
            startActivity(Intent(this, PlansActivityAddPlan::class.java))
            overridePendingTransition(R.anim.slide_up, R.anim.slide_in)
            finish()
        }

        findViewById<View>(R.id.Plans_Close).setOnClickListener { onBackPressed() }

        val list = findViewById<RecyclerView>(R.id.Plans_List)
        list.layoutManager = LinearLayoutManager(applicationContext)
        adapter = ListPlansAdapter()
        list.adapter = adapter
        val title = findViewById<TextView>(R.id.Plans_Title)
        var isTitleVisible = true
        list.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                if (!isTitleVisible && !recyclerView.canScrollVertically(-1) && dy < 0) {
                    title.startAnimation(AnimationUtils.loadAnimation(this@PlansActivity, R.anim.alpha_in))
                    isTitleVisible = true
                }
                if (isTitleVisible && recyclerView.canScrollVertically(-1)) {
                    title.startAnimation(AnimationUtils.loadAnimation(this@PlansActivity, R.anim.alpha_out))
                    isTitleVisible = false
                }
            }
        })

        adapter.setOnAddButtonClickListener { id, value, detail ->
            setLoadingMode(true)
            SqlQueries.addPlanValue(id, value, detail, this@PlansActivity, {
                updateAll()
            }, {
                println(it.message)
                showErroMessage(R.string.connection_error)
            })
        }

        adapter.setOnPlanDeleteListener {
            if (it) {
                updateAll()
            } else {
                showErroMessage(R.string.deleteerro)
            }
        }

        updateAll()
    }

    private fun updateAll() {
        setLoadingMode(true)
        findViewById<View>(R.id.Plans_Empty).isVisible = false
        adapter.clear()
        SqlQueries.getHeader(this, { _, outcome, income ->
            findViewById<TextView>(R.id.Plans_Header_Out).text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(outcome)
            findViewById<TextView>(R.id.Plans_Header_In).text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(income)

            val date = Calendar.getInstance().time
            val dateString = SimpleDateFormat("dd", Locale.getDefault()).format(date) + " de " + SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
            findViewById<TextView>(R.id.Plans_Date).text = dateString

            SqlQueries.getPlans(this, { contents ->
                for (plan in contents) {
                    plan.onContentPlanDelete = { if (it) updateAll() else showErroMessage(R.string.deleteerro) }
                    adapter.add(plan)
                }
                if (contents.isEmpty()) findViewById<View>(R.id.Plans_Empty).isVisible = true
                setLoadingMode(false)
            }, { showErroMessage(R.string.connection_error) })

        }, { showErroMessage(R.string.sqlerror) })
    }

    private fun showErroMessage(stringId: Int) {
        MessageAlert.create(this, MessageAlert.Type.ERROR, getString(stringId))
        setLoadingMode(false)
    }

    private fun setLoadingMode(active: Boolean) {
        addButton.visibility = if (active) View.GONE else View.VISIBLE
        findViewById<View>(R.id.Plans_ProgressBarCenter).visibility = if (active) View.VISIBLE else View.GONE
    }
}