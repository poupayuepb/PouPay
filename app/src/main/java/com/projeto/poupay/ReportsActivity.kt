package com.projeto.poupay

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.core.content.res.ResourcesCompat
import androidx.core.util.component1
import androidx.core.util.component2
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.ColorTemplate
import com.github.mikephil.charting.utils.MPPointF
import com.google.android.material.datepicker.MaterialDatePicker
import com.projeto.poupay.alerts.MessageAlert
import com.projeto.poupay.sql.SqlQueries
import com.projeto.poupay.view.ListContentAdapter
import com.projeto.poupay.view.MonthPicker
import com.projeto.poupay.view.SwitchSelector
import jxl.Workbook
import jxl.WorkbookSettings
import jxl.format.Alignment
import jxl.format.Border
import jxl.format.BorderLineStyle
import jxl.format.Colour
import jxl.format.UnderlineStyle
import jxl.format.VerticalAlignment
import jxl.write.Label
import jxl.write.WritableCellFormat
import jxl.write.WritableFont
import org.apache.commons.lang3.StringUtils
import java.io.IOException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

class ReportsActivity : AppCompatActivity() {

    private lateinit var adapter: ListContentAdapter
    private lateinit var reportType: SwitchSelector
    private lateinit var categoryType: SwitchSelector
    private lateinit var chart: PieChart
    private var year: Int = 0
    private var month: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reports)

        chart = findViewById(R.id.Reports_Chart)

        findViewById<View>(R.id.Reports_Close).setOnClickListener { onBackPressed() }
        findViewById<View>(R.id.Reports_Download).setOnClickListener { downloadLog() }
        findViewById<View>(R.id.Reports_DatePicker).setOnClickListener { showDatePickerDialog() }
        findViewById<View>(R.id.Reports_Legends).setOnClickListener {
            chart.setDrawEntryLabels(!chart.isDrawEntryLabelsEnabled)
            chart.invalidate()
        }

        val date = Calendar.getInstance().time
        month = SimpleDateFormat("MM", Locale.getDefault()).format(date).toInt()
        year = SimpleDateFormat("yyyy", Locale.getDefault()).format(date).toInt()

        val list = findViewById<RecyclerView>(R.id.Reports_List)
        list.layoutManager = LinearLayoutManager(this)
        adapter = ListContentAdapter()
        list.adapter = adapter

        categoryType = findViewById(R.id.Reports_Category)
        categoryType.onSelectChangeListener = { _, _ -> updateAll() }


        reportType = findViewById(R.id.Reports_Type)
        reportType.onSelectChangeListener = { _, _ -> updateAll() }
        reportType.setSelectedButton(0)

        Locale.setDefault(Locale("pt", "BR"))

        initChart()
    }

    private fun showDatePickerDialog() {
        val picker = MonthPicker(this)
        picker.onMonthPickerListener = { month, year ->
            this.month = month
            this.year = year
            updateAll()
        }
        picker.show()
    }

    private fun initChart() {
        chart.setUsePercentValues(true)
        chart.description.isEnabled = false
        chart.setExtraOffsets(3F, 3F, 3F, 3F)
        chart.setNoDataText("Carregando...")
        chart.setNoDataTextColor(ResourcesCompat.getColor(resources, R.color.background, null))
        chart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD)

        chart.dragDecelerationFrictionCoef = 0.95f

        chart.setDrawEntryLabels(false)
        chart.setDrawCenterText(true)

        chart.setTransparentCircleColor(Color.WHITE)
        chart.setTransparentCircleAlpha(110)

        chart.holeRadius = 55f
        chart.transparentCircleRadius = 60f

        chart.rotationAngle = 0F
        chart.isRotationEnabled = true

        chart.isHighlightPerTapEnabled = true

        chart.legend.isEnabled = false

        chart.setHoleColor(Color.TRANSPARENT)
        chart.setEntryLabelColor(Color.WHITE)
        chart.setEntryLabelTextSize(8F)

        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                val title = (e as PieEntry).label
                val value = adapter.getValueByContentTitle(title)

                if (value != null) {
                    chart.setCenterTextSize(14f)
                    val color = if (reportType.selectedId == 0) R.color.red else R.color.green
                    chart.setCenterTextColor(ResourcesCompat.getColor(resources, color, null))
                    chart.setCenterTextTypeface(Typeface.DEFAULT)
                    chart.centerText = "$title\n${NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)}"
                } else {
                    updateCenterText()
                }
            }

            override fun onNothingSelected() {
                updateCenterText()
            }
        })
    }

    private fun updateAll() {
        setLoadingMode(true)
        val date = Calendar.getInstance().time
        val dateString = SimpleDateFormat("dd", Locale.getDefault()).format(date) + " de " + SimpleDateFormat("MMMM", Locale.getDefault()).format(date)
        findViewById<TextView>(R.id.Reports_Date).text = dateString

        adapter.clear()
        chart.clear()
        updateCenterText()

        SqlQueries.getReportItems(this, reportType.selectedId, categoryType.selectedId, year, month, {
            adapter.add(it)

            val entries = mutableListOf<PieEntry>()
            var total = 0.0
            var others = 0.0F

            for (item in it) total += item.value

            for (item in it) {
                val percentage = (item.value / total).toFloat()
                if (entries.size >= 8 || percentage <= 0.05) others += percentage
                else entries.add(PieEntry(percentage, item.title))
            }

            if (others > 0) entries.add(PieEntry(others, "Outros"))

            val dataSet = PieDataSet(entries, "Legenda")
            dataSet.setDrawIcons(false)
            dataSet.sliceSpace = 3f
            dataSet.iconsOffset = MPPointF(0F, 40F)
            dataSet.selectionShift = 5f

            val colors = mutableListOf<Int>()
            colors.add(ResourcesCompat.getColor(resources, R.color.background, null))
            colors.add(ResourcesCompat.getColor(resources, R.color.green, null))
            colors.add(ResourcesCompat.getColor(resources, R.color.red, null))
            colors.add(ResourcesCompat.getColor(resources, R.color.blue, null))
            colors.add(ResourcesCompat.getColor(resources, R.color.orange, null))
            colors.add(ResourcesCompat.getColor(resources, R.color.yellow, null))
            for (color in ColorTemplate.PASTEL_COLORS) colors.add(color)
            dataSet.colors = colors

            val data = PieData(dataSet)
            val formatter = PercentFormatter(chart)
            data.setValueFormatter(formatter)
            data.setValueTypeface(Typeface.DEFAULT_BOLD)
            data.setValueTextSize(12f)
            data.setValueTextColor(Color.WHITE)
            data.isHighlightEnabled = true

            chart.data = data
            chart.highlightValues(null)
            chart.invalidate()
            chart.animateY(1400, Easing.EaseInOutQuad)

            setLoadingMode(false)
        }, {
            showErroMessage(R.string.sqlerror)
            setLoadingMode(false)
        })
    }

    private fun updateCenterText() {
        val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        val format = SimpleDateFormat("MMMM", Locale.getDefault())
        format.timeZone = TimeZone.getTimeZone("UTC")
        calendar.set(Calendar.MONTH, month - 1)
        val monString = StringUtils.capitalize(format.format(calendar.time))

        chart.setCenterTextColor(ResourcesCompat.getColor(resources, R.color.background, null))
        chart.setCenterTextTypeface(Typeface.DEFAULT_BOLD)
        chart.setCenterTextSize(18F)
        chart.centerText = "$monString \nde $year"
    }

    private fun setLoadingMode(active: Boolean) {
        categoryType.isEnabled = !active
        reportType.isEnabled = !active
        findViewById<View>(R.id.Reports_Loading).visibility = if (active) View.VISIBLE else View.GONE
    }

    private fun showErroMessage(stringId: Int) {
        MessageAlert.create(this, MessageAlert.Type.ERROR, getString(stringId))
        setLoadingMode(false)
    }

    private fun downloadLog() {
        val fileName = "relatorio_poupay_${Calendar.getInstance().time.time.toString().replace(" ", "_")}.xls"
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        intent.type = "application/xls"
        intent.putExtra(Intent.EXTRA_TITLE, fileName)
        startActivityForResult(intent, 2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (data != null) {
                try {
                    data.data?.let { writeToExcelFile(it) }
                } catch (exception: IOException) {
                    exception.printStackTrace()
                }
            }
        }
    }

    private fun writeToExcelFile(uri: Uri) {
        val outputStream = contentResolver.openOutputStream(uri)
        val wbSettings = WorkbookSettings()
        wbSettings.locale = Locale(Locale.getDefault().language, Locale.getDefault().country)

        try {
            val workbook = Workbook.createWorkbook(outputStream, wbSettings)
            val sheet = workbook.createSheet("Relatório", 0)
            val materialDatePicker = MaterialDatePicker.Builder.dateRangePicker()
                .setTitleText("SELECIONE UM INTERVALO")
                .setPositiveButtonText("Baixar")
                .build()

            materialDatePicker.addOnPositiveButtonClickListener {
                val (initDate, finalDate) = it
                val calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                format.timeZone = TimeZone.getTimeZone("UTC")

                calendar.timeInMillis = initDate
                val textInitDate = format.format(calendar.time)

                calendar.timeInMillis = finalDate
                val textFinalDate = format.format(calendar.time)

                SqlQueries.getEntry(this, initDate, finalDate, { list ->
                    try {
                        val formatTitle = WritableCellFormat()
                        formatTitle.setBackground(Colour.BLUE)
                        formatTitle.wrap = true
                        formatTitle.isShrinkToFit = true
                        formatTitle.verticalAlignment = VerticalAlignment.CENTRE
                        formatTitle.setBorder(Border.ALL, BorderLineStyle.MEDIUM, Colour.BLACK)
                        formatTitle.alignment = Alignment.CENTRE
                        formatTitle.setFont(WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE))

                        val formatData = WritableCellFormat()
                        formatData.setBackground(Colour.VERY_LIGHT_YELLOW)
                        formatData.wrap = true
                        formatData.isShrinkToFit = true
                        formatData.verticalAlignment = VerticalAlignment.CENTRE
                        formatData.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK)
                        formatData.alignment = Alignment.CENTRE
                        formatData.setFont(WritableFont(WritableFont.ARIAL, 8, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK))

                        val formatData2 = WritableCellFormat()
                        formatData2.setBackground(Colour.LIGHT_ORANGE)
                        formatData2.wrap = true
                        formatData2.isShrinkToFit = true
                        formatData2.verticalAlignment = VerticalAlignment.CENTRE
                        formatData2.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK)
                        formatData2.alignment = Alignment.CENTRE
                        formatData2.setFont(WritableFont(WritableFont.ARIAL, 8, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK))

                        sheet.mergeCells(0, 0, 5, 0)
                        sheet.addCell(Label(0, 0, "Relatório do Usuário ${SqlQueries.username} (de $textInitDate a $textFinalDate)"))

                        sheet.addCell(Label(0, 1, "ID", formatTitle))
                        sheet.addCell(Label(1, 1, "VALOR", formatTitle))
                        sheet.addCell(Label(2, 1, "DATA", formatTitle))
                        sheet.addCell(Label(3, 1, "DESCR.", formatTitle))
                        sheet.addCell(Label(4, 1, "CATEGORIA", formatTitle))
                        sheet.addCell(Label(5, 1, "PARCELAS", formatTitle))

                        var row = 2
                        var formatFlag = true

                        for (item in list) {
                            sheet.addCell(Label(0, row, item.contentId.toString(), if (formatFlag) formatData else formatData2))
                            sheet.addCell(Label(1, row, NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.value), if (formatFlag) formatData else formatData2))
                            sheet.addCell(Label(2, row, item.date, if (formatFlag) formatData else formatData2))
                            sheet.addCell(Label(3, row, item.title, if (formatFlag) formatData else formatData2))
                            sheet.addCell(Label(4, row, item.subtitle, if (formatFlag) formatData else formatData2))
                            sheet.addCell(Label(5, row, item.parcels.toString(), if (formatFlag) formatData else formatData2))
                            formatFlag = !formatFlag
                            row++
                        }

                        workbook.write()
                        workbook.close()

                        MessageAlert.create(this, MessageAlert.Type.SUCCESS, getString(R.string.export_xls_sucess))
                        setLoadingMode(false)
                    } catch (exception: Exception) {
                        exception.printStackTrace()
                        MessageAlert.create(this, MessageAlert.Type.SUCCESS, getString(R.string.export_xls_error))
                        setLoadingMode(false)
                    }
                }, { showErroMessage(R.string.sqlerror) })

            }
            materialDatePicker.show(supportFragmentManager, "MATERIAL_DATE_PICKER")

        } catch (exception: IOException) {
            exception.printStackTrace()
            MessageAlert.create(this, MessageAlert.Type.ERROR, getString(R.string.export_xls_error))
            setLoadingMode(false)
        }
    }
}