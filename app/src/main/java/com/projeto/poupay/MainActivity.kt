package com.projeto.poupay

import android.animation.ValueAnimator
import android.content.Intent
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.animation.AnimationUtils
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.marcoscg.currencyedittext.CurrencyEditText
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.sql.Preferences
import com.projeto.poupay.sql.SqlQueries
import com.projeto.poupay.view.ListSegmentAdapter
import com.projeto.poupay.view.MainActivityCalculator
import com.projeto.poupay.view.SwitchSelector
import com.skydoves.powerspinner.DefaultSpinnerAdapter
import com.skydoves.powerspinner.PowerSpinnerView
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MainActivity : AppCompatActivity() {

    private lateinit var adapter: ListSegmentAdapter
    private lateinit var subtitle: TextView
    private lateinit var filterSelector: SwitchSelector
    private lateinit var calculatorDialog: MainActivityCalculator

    private var isAddVisible = false
    private var indexFilter = 0

    private var resultRemindsButton = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        if (it.resultCode == RemindersActivity.RESULT_CLICKED) showAddDialog(true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        subtitle = findViewById(R.id.Main_Subtitle)

        val list = findViewById<RecyclerView>(R.id.Main_List)
        list.layoutManager = LinearLayoutManager(applicationContext)
        adapter = ListSegmentAdapter()
        list.adapter = adapter

        filterSelector = findViewById(R.id.Main_FilterSelector)
        filterSelector.onSelectChangeListener = { id, _ -> update(id) }
        filterSelector.setSelectedButton(0)

        findViewById<View>(R.id.btn_add).setOnClickListener { showAddDialog(null) }
        findViewById<View>(R.id.Main_Logout).setOnClickListener { onBackPressed() }
        initAddView()

        calculatorDialog = MainActivityCalculator(this)
        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottomNavigation)
        bottomNavigationView.setOnItemSelectedListener {
            if (it.itemId == R.id.reminds) resultRemindsButton.launch(Intent(this@MainActivity, RemindersActivity::class.java))
            if (it.itemId == R.id.reports) startActivity(Intent(this@MainActivity, ReportsActivity::class.java))
            if (it.itemId == R.id.plans) startActivity(Intent(this@MainActivity, PlansActivity::class.java))
            if (it.itemId == R.id.calculator) calculatorDialog.show()
            return@setOnItemSelectedListener true
        }
    }

    override fun onRestart() {
        super.onRestart()
        filterSelector.setSelectedButton(0)
    }

    override fun onBackPressed() {
        val alertContent = layoutInflater.inflate(R.layout.dialog_exit, findViewById(R.id.Back_Main))
        val alert = AlertDialog.Builder(this)
        alert.setCancelable(false)
        alert.setView(alertContent)
        val dialog = alert.show()
        dialog.window?.setBackgroundDrawable(ColorDrawable(android.graphics.Color.TRANSPARENT))

        alertContent.apply {
            findViewById<View>(R.id.Back_Cancel).setOnClickListener { dialog.dismiss() }
            findViewById<View>(R.id.Back_Close).setOnClickListener { finishAndRemoveTask() }
            findViewById<View>(R.id.Back_LogOut).setOnClickListener {
                Preferences.set(Preferences.Entry.REMIND_LOGIN_ENABLED, false, this.context)
                startActivity(Intent(this.context, LoginActivity::class.java))
                finish()
            }
        }
    }

    private fun setLoadingMode(enable: Boolean) {
        filterSelector.isEnabled = !enable
        findViewById<View>(R.id.Main_ProgressBarCenter).visibility = if (enable) View.VISIBLE else View.GONE
    }

    private fun updateHeader() {
        SqlQueries.getHeader(this, { total, outcome, income ->
            findViewById<TextView>(R.id.txt_balance_value).text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total)
            findViewById<TextView>(R.id.Main_Header_Out).text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(outcome)
            findViewById<TextView>(R.id.Main_Header_In).text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(income)

            val date = Calendar.getInstance().time
            val dateString = SimpleDateFormat("dd", Locale.getDefault()).format(date) + " de " + SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
            findViewById<TextView>(R.id.Main_Date).text = dateString

            if (adapter.itemCount == 0) findViewById<TextView>(R.id.Main_Empty).isVisible = true
            setLoadingMode(false)
        }, {
            showErrorMessage(R.string.sqlerror)
        })
    }

    private fun update(indexFilter: Int) {
        this.indexFilter = indexFilter
        adapter.clear()
        findViewById<TextView>(R.id.Main_Empty).isVisible = false
        findViewById<TextView>(R.id.txt_balance_value).text = getString(R.string.null_value)
        findViewById<TextView>(R.id.Main_Header_In).text = getString(R.string.null_value)
        findViewById<TextView>(R.id.Main_Header_Out).text = getString(R.string.null_value)
        setLoadingMode(true)

        var initDate: Long
        var finalDate: Long
        when (indexFilter) {
            0 -> subtitle.text = getString(R.string.subtitle_days)
            1 -> subtitle.text = getString(R.string.subtitle_month)
            2 -> subtitle.text = getString(R.string.subtitle_year)
            else -> {
                val materialDatePicker = MaterialDatePicker.Builder
                    .dateRangePicker()
                    .setTitleText("SELECIONE UM INTERVALO")
                    .setPositiveButtonText("Filtrar")
                    .build()

                materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")
                materialDatePicker.addOnCancelListener { filterSelector.setSelectedButton(0) }
                materialDatePicker.addOnNegativeButtonClickListener { filterSelector.setSelectedButton(0) }
                materialDatePicker.addOnPositiveButtonClickListener {
                    initDate = it.first
                    finalDate = it.second
                    val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                    val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    format.timeZone = TimeZone.getTimeZone("UTC")

                    calendar.timeInMillis = initDate
                    val textInitDate = format.format(calendar.time)
                    calendar.timeInMillis = finalDate
                    val textFinalDate = format.format(calendar.time)

                    subtitle.text = String.format("%s até %s", textInitDate, textFinalDate)
                    SqlQueries.getEntry(this, initDate, finalDate, { newItems ->
                        adapter.add(newItems)
                        updateHeader()
                    }, { showErrorMessage(R.string.sqlerror) })
                }
            }
        }

        when (indexFilter) {
            0 -> {
                SqlQueries.getEntry(this, indexFilter, { items ->
                    for (item in items) item.onContentDeleteListener = { if (it) update(0) else showErrorMessage(R.string.deleteerro) }
                    adapter.add(items)
                    updateHeader()
                }, {
                    println(it.message)
                    showErrorMessage(R.string.sqlerror)
                })
            }

            1, 2 -> {
                SqlQueries.getEntry(this, indexFilter, { items ->
                    adapter.add(items)
                    updateHeader()
                }, {
                    println(it.message)
                    showErrorMessage(R.string.sqlerror)
                })
            }
        }
    }


    private fun initAddView() {
        val details = findViewById<EditText>(R.id.Add_Details)
        val moneyPortion = findViewById<PowerSpinnerView>(R.id.Add_CardOptions)
        val value = findViewById<CurrencyEditText>(R.id.Add_Value)
        val inputType = findViewById<SwitchSelector>(R.id.Add_InputType)
        val moneyType = findViewById<SwitchSelector>(R.id.Add_Type)
        val dateLabel = findViewById<TextView>(R.id.Add_LabelDate)
        val dateSwitch = findViewById<FloatingActionButton>(R.id.Add_DatePicker)

        value.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                updateCardSpinner()
                moneyPortion.visibility = if (moneyType.selectedId == 1 && inputType.selectedId == 0 && value.getNumericValue() >= 1) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        })

        inputType.onSelectChangeListener = { id, _ ->
            value.setTextColor(resources.getColor(if (id == 0) R.color.red else R.color.green, null))
            moneyPortion.isVisible = moneyType.selectedId == 1 && id == 0 && value.getNumericValue() >= 1
        }
        inputType.setSelectedButton(0)

        moneyType.setButtonIcon(0, R.drawable.ic_payments_money)
        moneyType.setButtonIcon(1, R.drawable.ic_baseline_credit_card_36)
        moneyType.setButtonIcon(2, R.drawable.ic_baseline_pix_36)
        moneyType.setButtonIcon(3, R.drawable.ic_payment_other_36)
        moneyType.onSelectChangeListener = { id, _ ->
            moneyPortion.isVisible = id == 1 && inputType.selectedId == 0 && value.getNumericValue() >= 1
            updateCardSpinner()
        }

        dateLabel.text = getString(R.string.now)
        var dateString = "now()"
        dateSwitch.setOnClickListener {
            val materialDatePicker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("SELECIONE UMA DATA")
                .setPositiveButtonText("Selecionar")
                .build()

            materialDatePicker.show(supportFragmentManager, "MATERAL_DATE_PICKER")
            materialDatePicker.addOnPositiveButtonClickListener {
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")
                calendar.timeInMillis = it
                val textDate = format.format(calendar.time)
                dateLabel.text = textDate

                val format2 = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                format2.timeZone = TimeZone.getTimeZone("UTC")
                dateString = format2.format(calendar.time)
            }
        }


        findViewById<View>(R.id.Add_Confirm).setOnClickListener {
            if (validateAddField()) {
                val sqlValue = value.getNumericValue() * (if (inputType.selectedId == 0) -1 else 1)
                val sqlDesc = details.text.toString()
                val sqlCategory = moneyType.selectedId
                val sqlPortion: Int = if (moneyType.selectedId == 1) moneyPortion.selectedIndex + 1 else 0

                SqlQueries.addEntry(sqlValue, sqlDesc, sqlCategory, sqlPortion, this, dateString, {
                    update(indexFilter)
                }, { showErrorMessage(R.string.sqlerror) })

                showAddDialog(false)
                inputType.setSelectedButton(0)
                moneyType.setSelectedButton(0)
                value.setText("0")
                details.setText("")
                dateString = "now()"
                dateLabel.text = getString(R.string.now)
                hideKeyboard()
            }
        }
    }

    private fun showAddDialog(visibility: Boolean?) {
        hideKeyboard()
        isAddVisible = visibility ?: !isAddVisible

        val fab = findViewById<FloatingActionButton>(R.id.btn_add)
        val addLayout = findViewById<ConstraintLayout>(R.id.Main_AddLayout)
        val value = findViewById<CurrencyEditText>(R.id.Add_Value)
        val details = findViewById<EditText>(R.id.Add_Details)

        fab.startAnimation(AnimationUtils.loadAnimation(this, if (isAddVisible) R.anim.rotate45hour else R.anim.rotate45antihour))

        val endValue: Float = if (isAddVisible) 0.0F else resources.getDimension(R.dimen.bottom_add_layout_height)
        val anim = ValueAnimator.ofFloat(addLayout.translationY, endValue)
        anim.addUpdateListener {
            addLayout.translationY = it.animatedValue as Float
            val params = fab.layoutParams as CoordinatorLayout.LayoutParams
            if (it.animatedFraction > 0.85 && !isAddVisible) {
                params.anchorId = R.id.bottomAppBar
                fab.layoutParams = params
            }
            if (it.animatedFraction > 0.15 && isAddVisible) {
                params.anchorId = R.id.Main_AddLayout
                fab.layoutParams = params
            }
        }
        anim.duration = 400
        anim.start()

        val ripple = findViewById<ImageView>(R.id.Main_Ripple)
        val maxSize = resources.displayMetrics.heightPixels
        val animRipple = ValueAnimator.ofInt(if (isAddVisible) 0 else maxSize, if (isAddVisible) maxSize else 0)
        animRipple.addUpdateListener {
            val params = ripple.layoutParams
            params.height = it.animatedValue as Int
            ripple.layoutParams = params
        }
        animRipple.duration = 400
        animRipple.start()

        value.clearFocus()
        details.clearFocus()
    }

    private fun hideKeyboard() {
        val currentFocusView = currentFocus
        if (currentFocusView != null) {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(currentFocusView.windowToken, 0)
        }
    }

    private fun validateAddField(): Boolean {
        var isValid = true
        val value = findViewById<CurrencyEditText>(R.id.Add_Value)
        val details = findViewById<EditText>(R.id.Add_Details)
        if (value.getNumericValue() == 0.0 || value.text?.isEmpty() == true) {
            value.error = "Esse campo deve ser preenchido."
            isValid = false
        }
        if (details.text.isEmpty()) {
            details.error = "Esse campo deve ser preenchido."
            isValid = false
        }
        return isValid
    }

    private fun updateCardSpinner() {
        val spinnerCard = findViewById<PowerSpinnerView>(R.id.Add_CardOptions)
        val value = findViewById<CurrencyEditText>(R.id.Add_Value)
        val spinnerArray = mutableListOf<String>()

        for (i in 1..48)
            if (value.getNumericValue() / i >= 1)
                spinnerArray.add("${i}x de ${NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value.getNumericValue() / i)}")

        val spinnerAdapter = DefaultSpinnerAdapter(spinnerCard)
        spinnerAdapter.setItems(spinnerArray)
        spinnerCard.setSpinnerAdapter(spinnerAdapter)

        if (spinnerArray.size > 0) spinnerCard.selectItemByIndex(0)
    }

    private fun showErrorMessage(messageId: Int) {
        MessageAlert.create(this, MessageAlert.Type.ERROR, getString(messageId))
        setLoadingMode(false)
    }
}