package com.project.poupay.view;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.project.poupay.R;

public class MainActivity extends AppCompatActivity {

    FloatingActionButton btn_add;
    BottomSheetDialog sheetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar_header);
        setSupportActionBar(toolbar);

        btn_add = findViewById(R.id.btn_add);

        btn_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSheetDialog();
            }
        });
    }

    private void showSheetDialog() {
        sheetDialog = new BottomSheetDialog(MainActivity.this, R.style.BottomAddDialogStyle);

        View view = LayoutInflater.from(MainActivity.this).inflate(R.layout.add_value, null);
        sheetDialog.setContentView(view);

        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        RadioButton rb_expense = view.findViewById(R.id.rb_expense);
        RadioButton rb_revenue = view.findViewById(R.id.rb_revenue);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_expense) {
                    rb_expense.setBackgroundResource(R.drawable.radio_button_expense_check);
                    rb_revenue.setBackgroundResource(R.drawable.radio_button_revenue_uncheck);
                } else if (checkedId == R.id.rb_revenue) {
                    rb_revenue.setBackgroundResource(R.drawable.radio_button_revenue_check);
                    rb_expense.setBackgroundResource(R.drawable.radio_button_expense_uncheck);
                }
            }
        });

        sheetDialog.show();

    }

}