package com.project.poupay;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.TextView;

import com.project.poupay.alerts.MessageAlert;
import com.project.poupay.planactivities.PlansActivity_AddPlan;
import com.project.poupay.sql.User;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlansActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        getWindow().setNavigationBarColor(Color.WHITE);

        findViewById(R.id.Plans_Close).setOnClickListener(v -> onBackPressed());
        findViewById(R.id.Plans_AddPlan).setOnClickListener(v -> {
            startActivity(new Intent(PlansActivity.this, PlansActivity_AddPlan.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_in);
            finish();
        });

        updateAll();
    }

    private void updateAll(){
        User.getHeader(this, (total, despesa, receita) -> {
            ((TextView) findViewById(R.id.Plans_Header_Out)).setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(despesa));
            ((TextView) findViewById(R.id.Plans_Header_In)).setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(receita));

            Date date = Calendar.getInstance().getTime();
            String dateString = new SimpleDateFormat("dd", Locale.getDefault()).format(date) + " de " + new SimpleDateFormat("MMMM", Locale.getDefault()).format(date);
            ((TextView) findViewById(R.id.Plans_Date)).setText(dateString);

            setLoadingMode(false);
        }, exception -> showErroMessage(R.string.sqlerror));
    }

    private void showErroMessage(int id) {
        MessageAlert.create(this, MessageAlert.TYPE_ERRO, getString(id));
        setLoadingMode(false);
    }

    private void setLoadingMode(boolean active) {

    }
}