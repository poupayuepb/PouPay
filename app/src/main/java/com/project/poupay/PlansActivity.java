package com.project.poupay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.animation.AnimationUtils;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.project.poupay.alerts.MessageAlert;
import com.project.poupay.plans.ListPlansAdapter;
import com.project.poupay.plans.Plan;
import com.project.poupay.plans.PlansActivity_AddPlan;
import com.project.poupay.sql.User;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PlansActivity extends AppCompatActivity {

    private ListPlansAdapter adapter;
    private AppCompatButton mAddButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_plans);
        getWindow().setNavigationBarColor(Color.WHITE);

        mAddButton = findViewById(R.id.Plans_AddPlan);

        findViewById(R.id.Plans_Close).setOnClickListener(v -> onBackPressed());
        mAddButton.setOnClickListener(v -> {
            startActivity(new Intent(PlansActivity.this, PlansActivity_AddPlan.class));
            overridePendingTransition(R.anim.slide_up, R.anim.slide_in);
            finish();
        });

        RecyclerView mList = findViewById(R.id.Plans_List);
        mList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new ListPlansAdapter();
        mList.setAdapter(adapter);
        TextView title = findViewById(R.id.Plans_Title);
        final boolean[] isTitleVisible = {true};
        mList.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                if (!isTitleVisible[0] && !recyclerView.canScrollVertically(-1) && dy < 0) {
                    title.startAnimation(AnimationUtils.loadAnimation(PlansActivity.this, R.anim.alpha_in));
                    isTitleVisible[0] = true;
                }

                if (isTitleVisible[0] && recyclerView.canScrollVertically(-1)) {
                    title.startAnimation(AnimationUtils.loadAnimation(PlansActivity.this, R.anim.alpha_out));
                    isTitleVisible[0] = false;
                }
            }
        });

        adapter.setOnAddButtonClickListener((id, value, details) -> {
            setLoadingMode(true);
            User.addPlanValue(id, value, details, this, this::updateAll, exception -> showErroMessage(R.string.connection_error));
        });

        updateAll();
    }

    private void updateAll() {
        User.getHeader(this, (total, despesa, receita) -> {
            ((TextView) findViewById(R.id.Plans_Header_Out)).setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(despesa));
            ((TextView) findViewById(R.id.Plans_Header_In)).setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(receita));

            Date date = Calendar.getInstance().getTime();
            String dateString = new SimpleDateFormat("dd", Locale.getDefault()).format(date) + " de " + new SimpleDateFormat("MMMM", Locale.getDefault()).format(date);
            ((TextView) findViewById(R.id.Plans_Date)).setText(dateString);

            adapter.clear();

            User.getPlans(this, contents -> {
                for (Plan plan : contents) {
                    adapter.add(plan);
                }
            }, exception -> showErroMessage(R.string.connection_error));

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