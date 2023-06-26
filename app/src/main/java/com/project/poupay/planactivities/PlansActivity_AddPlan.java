package com.project.poupay.planactivities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;

import com.project.poupay.PlansActivity;
import com.project.poupay.R;

public class PlansActivity_AddPlan extends AppCompatActivity {

    private EditText mGoalText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans_addplan);
        getWindow().setStatusBarColor(Color.WHITE);
        getWindow().setNavigationBarColor(Color.WHITE);

        mGoalText = findViewById(R.id.AddPlan_OtherInput);

        findViewById(R.id.AddPlan_Back).setOnClickListener(v -> onBackPressed());

        findViewById(R.id.AddPlan_Confirm).setOnClickListener(v -> {
            if (validateGoalFields()) startFormActivity(mGoalText.getText().toString());
        });

        findViewById(R.id.AddPlan_Investiment).setOnClickListener(v -> startFormActivity("Investimento"));
        findViewById(R.id.AddPlan_Travel).setOnClickListener(v -> startFormActivity("Viagem"));
        findViewById(R.id.AddPlan_Reserve).setOnClickListener(v -> startFormActivity("Reserva"));
        findViewById(R.id.AddPlan_Debt).setOnClickListener(v -> startFormActivity("Dívida"));

    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(PlansActivity_AddPlan.this, PlansActivity.class));
        overridePendingTransition(R.anim.slide_down, R.anim.slide_out);
        finish();
    }

    private void startFormActivity(String goal) {
        Intent intent = new Intent(PlansActivity_AddPlan.this, PlansActivity_AddPlan_Form.class);
        intent.putExtra("goal", goal);
        startActivity(intent);
        overridePendingTransition(R.anim.slide_up, R.anim.slide_in);
        finish();
    }

    public boolean validateGoalFields() {
        boolean isValid = true;
        if (mGoalText.getText().toString().length() == 0) {
            mGoalText.setError("Esse campo deve ser preenchido.");
            isValid = false;
        }
        return isValid;
    }

}