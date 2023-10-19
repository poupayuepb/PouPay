package com.projeto.poupay.view

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.CheckBox
import android.widget.GridLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.projeto.poupay.R
import org.apache.commons.lang3.StringUtils
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class MonthPicker(context: Context) : LinearLayout(context) {
    private var year = 0
    private var month = 0
    private var alertContent: View
    private var yearOnly: CheckBox
    var onMonthPickerListener: (month: Int, year: Int) -> Unit = { _, _ -> }

    init {
        this.alertContent = (context as AppCompatActivity).layoutInflater.inflate(R.layout.dialog_month_picker, findViewById(R.id.Month_Picker_Main))
        val mDrawableUnselected = ResourcesCompat.getDrawable(context.resources, R.drawable.transparent, null)
        val mDrawableSelected = ResourcesCompat.getDrawable(context.resources, R.drawable.month_picker_selected, null)

        val date = Calendar.getInstance().time
        this.year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date).toInt()
        this.month = SimpleDateFormat("MM", Locale.getDefault()).format(date).toInt()

        alertContent.findViewById<View>(R.id.Month_Picker_Year_Minus).setOnClickListener {
            if (year > 2000) year--
            update()
        }

        alertContent.findViewById<View>(R.id.Month_Picker_Year_Plus).setOnClickListener {
            if (year < Int.MAX_VALUE) year++
            update()
        }

        val monthLayout = alertContent.findViewById<GridLayout>(R.id.Month_Picker_Month_Layout)

        for (i in 0..<monthLayout.childCount) {
            val view = monthLayout.getChildAt(i)
            view.setOnClickListener {
                for (j in 0..<monthLayout.childCount) {
                    monthLayout.getChildAt(j).background = mDrawableUnselected
                }
                it.background = mDrawableSelected
                month = i + 1
                update()
            }
        }

        yearOnly = alertContent.findViewById(R.id.Month_Picker_YearEntire)
        yearOnly.setOnCheckedChangeListener { _, isOn ->
            monthLayout.isVisible = !isOn
        }

        update()
    }

    private fun update() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val format = SimpleDateFormat("MMMM", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        calendar.set(Calendar.MONTH, month - 1)
        val monString = StringUtils.capitalize(format.format(calendar.time))

        with(alertContent) {
            findViewById<TextView>(R.id.Month_Picker_Title).text = String.format(Locale.getDefault(), "%s de %d", monString, year)
            findViewById<TextView>(R.id.Month_Picker_Year).text = year.toString()
        }
    }

    fun show() {
        val alert = AlertDialog.Builder(context)
        alert.setCancelable(false)
        alert.setView(alertContent)
        val dialog = alert.show()

        with(alertContent) {
            findViewById<View>(R.id.Month_Picker_Cancel).setOnClickListener {
                dialog.dismiss()
            }
            findViewById<View>(R.id.Month_Picker_Confirm).setOnClickListener {
                val monthOutput = if (yearOnly.isChecked) -1 else month
                onMonthPickerListener.invoke(monthOutput, year)
                dialog.dismiss()
            }
        }
    }
}