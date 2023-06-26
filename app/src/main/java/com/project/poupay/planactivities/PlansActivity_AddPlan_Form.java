package com.project.poupay.planactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.marcoscg.currencyedittext.CurrencyEditText;
import com.project.poupay.PlansActivity;
import com.project.poupay.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class PlansActivity_AddPlan_Form extends AppCompatActivity {

    private CurrencyEditText mValue;
    private TextView mGoal;
    private EditText mInstitution;
    private TextView mDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans_addplanform);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        mValue = findViewById(R.id.AddPlanForm_Value);
        mGoal = findViewById(R.id.AddPlanForm_Goal);
        mInstitution = findViewById(R.id.AddPlanForm_FinancialInstitution);
        mDate = findViewById(R.id.AddPlanForm_EstimatedDateText);

        mGoal.setText(getIntent().getExtras().getString("goal",""));

        findViewById(R.id.AddPlanForm_Back).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.AddPlanForm_EstimatedDatePicker).setOnClickListener(v -> showDatePickerDialog());
        findViewById(R.id.NewPlan_Confirm).setOnClickListener(v -> confirm());

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PlansActivity_AddPlan_Form.this, PlansActivity.class));
        overridePendingTransition(R.anim.slide_down, R.anim.slide_out);
        finish();
    }

    private void confirm() {
        if(validateAddFields()){
            // TODO: Fazer adicionar.


            startActivity(new Intent(PlansActivity_AddPlan_Form.this, PlansActivity.class));
            overridePendingTransition(R.anim.slide_down, R.anim.slide_out);
            finish();
        }
    }

    private void showDatePickerDialog() {
        final MaterialDatePicker<Long> materialDatePicker = MaterialDatePicker.Builder.datePicker().setTitleText("SELECIONE UMA DATA").setPositiveButtonText("Selecionar").build();
        materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
        materialDatePicker.addOnPositiveButtonClickListener(date -> {
            Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            calendar.setTimeInMillis(date);
            String textInitDate = format.format(calendar.getTime());
            mDate.setText(textInitDate);
        });
    }

    public boolean validateAddFields() {
        boolean isValid = true;
        if (mValue.getNumericValue() == 0 || Objects.requireNonNull(mValue.getText()).toString().isEmpty()) {
            mValue.setError("Esse campo deve ser preenchido.");
            isValid = false;
        }
        if (mValue.getNumericValue() <= 0) {
            mValue.setError("Esse campo deve conter um valor positivo.");
            isValid = false;
        }
        if (mInstitution.getText().toString().isEmpty()) {
            mInstitution.setError("Esse campo deve ser preenchido.");
            isValid = false;
        }
        if (mDate.getText().toString().isEmpty()|| mDate.getText().toString().equals(getString(R.string.NewPlan_estimatedDate))) {
            mDate.setError("Esse campo deve ser preenchido.");
            isValid = false;
        }
        return isValid;
    }


}