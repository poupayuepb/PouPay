package com.project.poupay;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.highlight.Highlight;
import com.github.mikephil.charting.listener.OnChartValueSelectedListener;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.project.poupay.alerts.MessageAlert;
import com.project.poupay.sql.User;
import com.project.poupay.view.ContentItem;
import com.project.poupay.view.ContentReportItem;
import com.project.poupay.view.ListContentAdapter;
import com.project.poupay.view.MonthPicker;
import com.project.poupay.view.SwitchSelector;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.io.OutputStream;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.format.Alignment;
import jxl.format.Border;
import jxl.format.BorderLineStyle;
import jxl.format.Colour;
import jxl.format.UnderlineStyle;
import jxl.format.VerticalAlignment;
import jxl.write.Label;
import jxl.write.WritableCellFormat;
import jxl.write.WritableFont;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;

public class ReportsActivity extends AppCompatActivity {

    private ListContentAdapter adapter;
    private SwitchSelector mReportType, mCategoryType;
    private PieChart mChart;
    private int mYear, mMonth;

    public final static int REPORTS_TYPE_EXPENSE = 0;
    public final static int REPORTS_TYPE_INCOMING = 1;

    public final static int REPORTS_CATEGORY_PER_DESCRIPTION = 0;
    public final static int REPORTS_CATEGORY_PER_PAYMENT_TYPE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reports);

        mChart = findViewById(R.id.Reports_Chart);

        findViewById(R.id.Reports_Close).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.Reports_Download).setOnClickListener(v -> downloadLog());
        findViewById(R.id.Reports_DatePicker).setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.Reports_Legends).setOnClickListener(v -> {
            mChart.setDrawEntryLabels(!mChart.isDrawEntryLabelsEnabled());
            mChart.invalidate();
        });

        Date date = Calendar.getInstance().getTime();
        mYear = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));
        mMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(date));

        RecyclerView mList = findViewById(R.id.Reports_List);
        mList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new ListContentAdapter();
        mList.setAdapter(adapter);

        mCategoryType = findViewById(R.id.Reports_Category);
        mCategoryType.setOnSelectChangeListener((id, text) -> updateAll());

        mReportType = findViewById(R.id.Reports_Type);
        mReportType.setOnSelectChangeListener((id, text) -> updateAll());
        mReportType.setSelectedButton(0);

        Locale.setDefault(new Locale("pt", "BR"));

        initChart();
    }

    private void showDatePickerDialog() {
        MonthPicker picker = new MonthPicker(this);
        picker.setOnSelectChangeListener((int month, int year) -> {
            mYear = year;
            mMonth = month;
            updateAll();
        });
        picker.show();


    }

    private void initChart() {
        mChart.setUsePercentValues(true);
        mChart.getDescription().setEnabled(false);
        mChart.setExtraOffsets(3, 3, 3, 3);
        mChart.setNoDataText("Carregando...");
        mChart.setNoDataTextColor(ResourcesCompat.getColor(getResources(), R.color.background, null));
        mChart.setNoDataTextTypeface(Typeface.DEFAULT_BOLD);

        mChart.setDragDecelerationFrictionCoef(0.95f);

        mChart.setDrawEntryLabels(false);
        mChart.setDrawCenterText(true);

        mChart.setTransparentCircleColor(Color.WHITE);
        mChart.setTransparentCircleAlpha(110);

        mChart.setHoleRadius(55f);
        mChart.setTransparentCircleRadius(60f);

        mChart.setRotationAngle(0);
        mChart.setRotationEnabled(true);

        mChart.setHighlightPerTapEnabled(true);

        mChart.getLegend().setEnabled(false);

        mChart.setHoleColor(Color.TRANSPARENT);
        mChart.setEntryLabelColor(Color.WHITE);
        mChart.setEntryLabelTextSize(8f);

        mChart.setOnChartValueSelectedListener(new OnChartValueSelectedListener() {
            @Override
            public void onValueSelected(Entry e, Highlight h) {
                String title = ((PieEntry) e).getLabel();
                Double value = adapter.getValueByContentTitle(title);

                if (value != null) {
                    mChart.setCenterTextSize(14f);
                    mChart.setCenterTextColor(ResourcesCompat.getColor(getResources(), mReportType.getSelectedId() == 0 ? R.color.red : R.color.green, null));
                    mChart.setCenterTextTypeface(Typeface.DEFAULT);
                    mChart.setCenterText(title + "\n" + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value));
                } else updateCenterText();

            }

            @Override
            public void onNothingSelected() {
                updateCenterText();
            }
        });

    }

    private void updateAll() {
        setLoadingMode(true);
        Date date = Calendar.getInstance().getTime();
        String dateString = new SimpleDateFormat("dd", Locale.getDefault()).format(date) + " de " + new SimpleDateFormat("MMMM", Locale.getDefault()).format(date);
        ((TextView) findViewById(R.id.Reports_Date)).setText(dateString);

        adapter.clear();
        mChart.clear();
        updateCenterText();

        User.getReportItems(this, mReportType.getSelectedId(), mCategoryType.getSelectedId(), mYear, mMonth, contents -> {
            adapter.add(contents);

            ArrayList<PieEntry> entries = new ArrayList<>();

            double total = 0;
            float others = 0;
            for (ContentReportItem item : contents) total += item.getValue();

            for (ContentReportItem item : contents) {
                float percentage = (float) (item.getValue() / total);
                if (entries.size() >= 8 || percentage <= 0.05) others += percentage;
                else entries.add(new PieEntry(percentage, item.getTitle()));
            }
            if (others > 0) entries.add(new PieEntry(others, "Outros"));


            PieDataSet dataSet = new PieDataSet(entries, "Legenda");
            dataSet.setDrawIcons(false);
            dataSet.setSliceSpace(3f);
            dataSet.setIconsOffset(new MPPointF(0, 40));
            dataSet.setSelectionShift(5f);

            ArrayList<Integer> colors = new ArrayList<>();
            colors.add(ResourcesCompat.getColor(getResources(), R.color.background, null));
            colors.add(ResourcesCompat.getColor(getResources(), R.color.green, null));
            colors.add(ResourcesCompat.getColor(getResources(), R.color.red, null));
            colors.add(ResourcesCompat.getColor(getResources(), R.color.blue, null));
            colors.add(ResourcesCompat.getColor(getResources(), R.color.orange, null));
            colors.add(ResourcesCompat.getColor(getResources(), R.color.yellow, null));
            for (int c : ColorTemplate.PASTEL_COLORS) colors.add(c);
            dataSet.setColors(colors);

            PieData data = new PieData(dataSet);
            ValueFormatter formater = new PercentFormatter(mChart);
            data.setValueFormatter(formater);

            data.setValueTypeface(Typeface.DEFAULT_BOLD);

            data.setValueTextSize(12f);
            data.setValueTextColor(Color.WHITE);
            data.setHighlightEnabled(true);

            mChart.setData(data);
            mChart.highlightValues(null);
            mChart.invalidate();
            mChart.animateY(1400, Easing.EaseInOutQuad);


            setLoadingMode(false);
        }, exception -> {
            showErroMessage(R.string.sqlerror);
            setLoadingMode(false);
        });

    }

    private void updateCenterText() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat format = new SimpleDateFormat("MMMM", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.MONTH, mMonth - 1);
        String monString = StringUtils.capitalize(format.format(calendar.getTime()));

        mChart.setCenterTextColor(ResourcesCompat.getColor(getResources(), R.color.background, null));
        mChart.setCenterTextTypeface(Typeface.DEFAULT_BOLD);
        mChart.setCenterTextSize(18f);
        mChart.setCenterText(monString + "\nde " + mYear);
    }

    private void setLoadingMode(boolean enable) {
        mCategoryType.setEnabled(!enable);
        mReportType.setEnabled(!enable);
        findViewById(R.id.Reports_Loading).setVisibility(enable ? View.VISIBLE : View.GONE);
    }

    private void showErroMessage(int id) {
        MessageAlert.create(this, MessageAlert.TYPE_ERRO, getString(id));
        setLoadingMode(false);
    }

    private void downloadLog() {
        String fileName = "relatorio_poupay_" + Calendar.getInstance().getTime().toString().replace(" ", "_") + ".xls";

        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/xls");
        intent.putExtra(Intent.EXTRA_TITLE, fileName);
        startActivityForResult(intent, 2);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        super.onActivityResult(requestCode, resultCode, resultData);
        if (requestCode == 2 && resultCode == Activity.RESULT_OK) {
            if (resultData != null) {
                try {
                    WriteToExcelFile(resultData.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    public void WriteToExcelFile(Uri uri) throws IOException {
        OutputStream os = getContentResolver().openOutputStream(uri);
        WorkbookSettings wbSettings = new WorkbookSettings();
        wbSettings.setLocale(new Locale(Locale.getDefault().getLanguage(), Locale.getDefault().getCountry()));
        WritableWorkbook workbook;

        try {
            workbook = Workbook.createWorkbook(os, wbSettings);
            WritableSheet sheet = workbook.createSheet("Relatorio", 0);

            final MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().setTitleText("SELECIONE UM INTERVALO").setPositiveButtonText("Baixar").build();
            materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            materialDatePicker.addOnPositiveButtonClickListener(selection -> {
                long initDate = selection.first;
                long finalDate = selection.second;
                Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
                SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                format.setTimeZone(TimeZone.getTimeZone("UTC"));
                calendar.setTimeInMillis(initDate);
                String textInitDate = format.format(calendar.getTime());
                calendar.setTimeInMillis(finalDate);
                String textFinalDate = format.format(calendar.getTime());
                User.getEntry(this, initDate, finalDate, newItems -> {
                    try {
                        int itemsCount = newItems.size();

                        WritableCellFormat formatTitle = new WritableCellFormat();
                        formatTitle.setBackground(Colour.BLUE);
                        formatTitle.setWrap(true);
                        formatTitle.setShrinkToFit(true);
                        formatTitle.setVerticalAlignment(VerticalAlignment.CENTRE);
                        formatTitle.setBorder(Border.ALL, BorderLineStyle.MEDIUM, Colour.BLACK);
                        formatTitle.setAlignment(Alignment.CENTRE);
                        formatTitle.setFont(new WritableFont(WritableFont.ARIAL, 8, WritableFont.BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.WHITE));

                        WritableCellFormat formatData = new WritableCellFormat();
                        formatData.setBackground(Colour.VERY_LIGHT_YELLOW);
                        formatData.setWrap(true);
                        formatData.setShrinkToFit(true);
                        formatData.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
                        formatData.setAlignment(Alignment.CENTRE);
                        formatData.setVerticalAlignment(VerticalAlignment.CENTRE);
                        formatData.setFont(new WritableFont(WritableFont.ARIAL, 8, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));

                        WritableCellFormat formatData2 = new WritableCellFormat();
                        formatData2.setBackground(Colour.LIGHT_ORANGE);
                        formatData2.setWrap(true);
                        formatData2.setShrinkToFit(true);
                        formatData2.setBorder(Border.ALL, BorderLineStyle.THIN, Colour.BLACK);
                        formatData2.setAlignment(Alignment.CENTRE);
                        formatData2.setVerticalAlignment(VerticalAlignment.CENTRE);
                        formatData2.setFont(new WritableFont(WritableFont.ARIAL, 8, WritableFont.NO_BOLD, false, UnderlineStyle.NO_UNDERLINE, Colour.BLACK));

                        sheet.mergeCells(0, 0, 5, 0);
                        sheet.addCell(new Label(0, 0, "Relatório do Usuário " + User.username + " (de " + textInitDate + " a " + textFinalDate + ")", formatTitle));

                        sheet.addCell(new Label(0, 1, "ID", formatTitle));
                        sheet.addCell(new Label(1, 1, "VALOR", formatTitle));
                        sheet.addCell(new Label(2, 1, "DATA", formatTitle));
                        sheet.addCell(new Label(3, 1, "DESCR.", formatTitle));
                        sheet.addCell(new Label(4, 1, "CATEGORIA", formatTitle));
                        sheet.addCell(new Label(5, 1, "PARCELAS", formatTitle));


                        int row = 2;
                        boolean formatFlag = true;
                        for (ContentItem item : newItems) {
                            sheet.addCell(new Label(0, row, String.valueOf(item.getIdNumber()), formatFlag ? formatData : formatData2));
                            sheet.addCell(new Label(1, row, NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.getValue()), formatFlag ? formatData : formatData2));
                            sheet.addCell(new Label(2, row, item.getDate(), formatFlag ? formatData : formatData2));
                            sheet.addCell(new Label(3, row, item.getTitle(), formatFlag ? formatData : formatData2));
                            sheet.addCell(new Label(4, row, item.getSubtitle(), formatFlag ? formatData : formatData2));
                            sheet.addCell(new Label(5, row, String.valueOf(item.getParcels()), formatFlag ? formatData : formatData2));

                            formatFlag = !formatFlag;
                            row++;
                        }

                        workbook.write();
                        workbook.close();

                        MessageAlert.create(this, MessageAlert.TYPE_SUCESS, getString(R.string.export_xls_sucess));
                        setLoadingMode(false);
                    } catch (IOException | WriteException e) {
                        e.printStackTrace();
                        MessageAlert.create(this, MessageAlert.TYPE_ERRO, getString(R.string.export_xls_error));
                        setLoadingMode(false);
                    }

                }, exception -> showErroMessage(R.string.sqlerror));
            });

        } catch (IOException e) {
            e.printStackTrace();
            MessageAlert.create(this, MessageAlert.TYPE_ERRO, getString(R.string.export_xls_error));
            setLoadingMode(false);
        }
    }
}