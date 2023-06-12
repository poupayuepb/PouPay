package com.project.poupay;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.core.util.Pair;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.marcoscg.currencyedittext.CurrencyEditText;
import com.project.poupay.alerts.MessageAlert;
import com.project.poupay.sql.Preferences;
import com.project.poupay.sql.User;
import com.project.poupay.view.ListContentAdapter;
import com.project.poupay.view.SwitchSelector;
import com.skydoves.powerspinner.DefaultSpinnerAdapter;
import com.skydoves.powerspinner.PowerSpinnerView;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.TimeZone;

public class MainActivity extends AppCompatActivity {

    BottomNavigationView bottomNavigationView;

    private ListContentAdapter adapter;
    private TextView mSubtitle;
    private SwitchSelector mFilterSelector;

    private boolean isAddLayoutVisible = false;
    private int indexFilter = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mSubtitle = findViewById(R.id.Main_Subtitle);

        RecyclerView mList = findViewById(R.id.Main_List);
        mList.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        adapter = new ListContentAdapter();
        mList.setAdapter(adapter);

        mFilterSelector = findViewById(R.id.Main_FilterSelector);
        mFilterSelector.setOnSelectChangeListener((id, text) -> update(id));
        mFilterSelector.setSelectedButton(0);

        findViewById(R.id.btn_add).setOnClickListener(v -> showAddDialog(null));
        findViewById(R.id.Main_Logout).setOnClickListener(v -> onBackPressed());
        initAddView();

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnItemSelectedListener(item -> {
            switch (item.getItemId()){
                case R.id.reminders:
                    break;
                case R.id.graphic:
                    break;
                case R.id.plans:
                    break;
                case R.id.calculator:
                    Calculator dialog = new Calculator(MainActivity.this);
                    dialog.show();
                    break;
            }
            return true;
        });
    }

    @Override
    public void onBackPressed() {
        View alertContent = getLayoutInflater().inflate(R.layout.dialog_exit, findViewById(R.id.Back_Main));
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        alert.setCancelable(false);
        alert.setView(alertContent);
        Dialog dialog = alert.show();
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));

        alertContent.findViewById(R.id.Back_Cancel).setOnClickListener(v -> dialog.dismiss());
        alertContent.findViewById(R.id.Back_Close).setOnClickListener(v -> finishAndRemoveTask());
        alertContent.findViewById(R.id.Back_LogOut).setOnClickListener(v -> {
            Preferences.set(Preferences.REMIND_LOGIN_ENABLED, false, this);
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void setLoadingMode(boolean enabled) {
        mFilterSelector.setEnabled(!enabled);
        findViewById(R.id.Main_ProgressBarCenter).setVisibility(enabled ? View.VISIBLE : View.GONE);
    }

    private void updateHeader() {
        User.getHeader(this, (total, despesa, receita) -> {
            ((TextView) findViewById(R.id.txt_balance_value)).setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total));
            ((TextView) findViewById(R.id.Main_Header_Out)).setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(despesa));
            ((TextView) findViewById(R.id.Main_Header_In)).setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(receita));
            setLoadingMode(false);
        }, exception -> showErroMessage(R.string.sqlerror));
    }

    public void update(int indexFilter) {
        this.indexFilter = indexFilter;
        adapter.clear();
        ((TextView) findViewById(R.id.txt_balance_value)).setText("R$ -");
        ((TextView) findViewById(R.id.Main_Header_Out)).setText("R$ -");
        ((TextView) findViewById(R.id.Main_Header_In)).setText("R$ -");
        setLoadingMode(true);

        if (indexFilter == 3) {
            final MaterialDatePicker<Pair<Long, Long>> materialDatePicker = MaterialDatePicker.Builder.dateRangePicker().setTitleText("SELECIONE UM INTERVALO").setPositiveButtonText("Filtrar").build();
            materialDatePicker.show(getSupportFragmentManager(), "MATERIAL_DATE_PICKER");
            materialDatePicker.addOnCancelListener(dialog -> mFilterSelector.setSelectedButton(0));
            materialDatePicker.addOnNegativeButtonClickListener(dialog -> mFilterSelector.setSelectedButton(0));
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
                mSubtitle.setText(String.format("%s  até  %s", textInitDate, textFinalDate));
                User.getEntry(this, initDate, finalDate, newItems -> {
                    adapter.add(newItems);
                    updateHeader();
                }, exception -> showErroMessage(R.string.sqlerror));
            });
        } else {
            if (indexFilter == 0) mSubtitle.setText(R.string.subtitle_days);
            else if (indexFilter == 1) mSubtitle.setText(R.string.subtitle_month);
            else mSubtitle.setText(R.string.subtitle_year);
            User.getEntry(this, indexFilter, newItems -> {
                adapter.add(newItems);
                updateHeader();
            }, exception -> showErroMessage(R.string.sqlerror));
        }
    }

    private void initAddView() {
        EditText details = findViewById(R.id.Add_Details);
        PowerSpinnerView moneyPortion = findViewById(R.id.Add_CardOptions);
        CurrencyEditText value = findViewById(R.id.Add_Value);
        SwitchSelector inputType = findViewById(R.id.Add_InputType);
        SwitchSelector moneyType = findViewById(R.id.Add_Type);

        value.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                updateCardSpinner();
                moneyPortion.setVisibility(moneyType.getSelectedId() == 1 && inputType.getSelectedId() == 0 && value.getNumericValue() >= 1 ? View.VISIBLE : View.GONE);
            }
        });


        inputType.setOnSelectChangeListener((id, text) -> {
            value.setTextColor(getResources().getColor(id == 0 ? R.color.red : R.color.green));
            moneyPortion.setVisibility(moneyType.getSelectedId() == 1 && id == 0 && value.getNumericValue() >= 1 ? View.VISIBLE : View.GONE);
        });
        inputType.setSelectedButton(0);

        moneyType.setButtonIcon(0, R.drawable.ic_payments_money);
        moneyType.setButtonIcon(1, R.drawable.ic_baseline_credit_card_36);
        moneyType.setButtonIcon(2, R.drawable.ic_baseline_pix_36);
        moneyType.setButtonIcon(3, R.drawable.ic_payment_other_36);
        moneyType.setOnSelectChangeListener((id, text) -> {
            moneyPortion.setVisibility(id == 1 && inputType.getSelectedId() == 0 && value.getNumericValue() >= 1 ? View.VISIBLE : View.GONE);
            updateCardSpinner();
        });

        findViewById(R.id.Add_Confirm).setOnClickListener(v -> {
            if (validateAddFields()) {
                double sqlValue = value.getNumericValue() * (inputType.getSelectedId() == 0 ? -1 : 1);
                String sqlDesc = details.getText().toString();
                int sqlCategory = moneyType.getSelectedId();
                Integer sqlPortion = null;
                if (moneyType.getSelectedId() == 1) sqlPortion = moneyPortion.getSelectedIndex() + 1;
                User.addEntry(sqlValue, sqlDesc, sqlCategory, sqlPortion, this, () -> {
                    MessageAlert.create(MainActivity.this, MessageAlert.TYPE_SUCESS, "Lançamento inserido com sucesso.");
                    update(indexFilter);
                }, exception -> showErroMessage(R.string.sqlerror));
                showAddDialog(false);
                inputType.setSelectedButton(0);
                moneyType.setSelectedButton(0);
                value.setText("0");
                details.setText("");
                hideKeyboard();
            }
        });
    }

    private void showAddDialog(@Nullable Boolean visibility) {
        hideKeyboard();
        if (visibility == null) isAddLayoutVisible = !isAddLayoutVisible;
        else isAddLayoutVisible = visibility;

        FloatingActionButton fab = findViewById(R.id.btn_add);
        LinearLayout addLayout = findViewById(R.id.Main_AddLayout);
        CurrencyEditText value = findViewById(R.id.Add_Value);
        EditText details = findViewById(R.id.Add_Details);

        fab.startAnimation(AnimationUtils.loadAnimation(this, isAddLayoutVisible ? R.anim.rotate45hour : R.anim.rotate45antihour));

        float endValue = isAddLayoutVisible ? 0 : getResources().getDimension(R.dimen.bottom_add_layout_height);
        ValueAnimator anim = ValueAnimator.ofFloat(addLayout.getTranslationY(), endValue);
        anim.addUpdateListener(animation -> {
            addLayout.setTranslationY((Float) animation.getAnimatedValue());
            CoordinatorLayout.LayoutParams p = (CoordinatorLayout.LayoutParams) fab.getLayoutParams();
            if (animation.getAnimatedFraction() > 0.85 && !isAddLayoutVisible) {
                p.setAnchorId(R.id.bottomAppBar);
                fab.setLayoutParams(p);
            }
            if (animation.getAnimatedFraction() > 0.15 && isAddLayoutVisible) {
                p.setAnchorId(R.id.Main_AddLayout);
                fab.setLayoutParams(p);
            }
        });

        anim.setDuration(400);
        anim.start();
        ImageView ripple = findViewById(R.id.Main_Ripple);
        int maxSize = getResources().getDisplayMetrics().heightPixels;
        ValueAnimator animRipple = ValueAnimator.ofInt(isAddLayoutVisible ? 0 : maxSize, isAddLayoutVisible ? maxSize : 0);
        animRipple.addUpdateListener(animation -> {
            ViewGroup.LayoutParams params = ripple.getLayoutParams();
            params.height = (int) animation.getAnimatedValue();
            ripple.setLayoutParams(params);
        });
        animRipple.setDuration(400);
        animRipple.start();

        value.clearFocus();
        details.clearFocus();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(this.getCurrentFocus().getWindowToken(), 0);
        }
    }

    public boolean validateAddFields() {
        boolean isValid = true;
        CurrencyEditText value = findViewById(R.id.Add_Value);
        EditText details = findViewById(R.id.Add_Details);
        if (value.getNumericValue() == 0 || Objects.requireNonNull(value.getText()).toString().isEmpty()) {
            value.setError("Esse campo deve ser preenchido.");
            isValid = false;
        }
        if (details.getText().toString().isEmpty()) {
            details.setError("Esse campo deve ser preenchido.");
            isValid = false;
        }
        return isValid;
    }

    private void updateCardSpinner() {
        PowerSpinnerView spinnerCard = findViewById(R.id.Add_CardOptions);
        CurrencyEditText value = findViewById(R.id.Add_Value);
        List<String> spinnerArray = new ArrayList<>();

        for (int i = 1; i <= 48; i++) {
            if (value.getNumericValue() / i >= 1)
                spinnerArray.add(i + "x de " + NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value.getNumericValue() / i));
        }

        DefaultSpinnerAdapter spinnerAdapter = new DefaultSpinnerAdapter(spinnerCard);
        spinnerAdapter.setItems(spinnerArray);
        spinnerCard.setSpinnerAdapter(spinnerAdapter);

        if (spinnerArray.size() > 0) spinnerCard.selectItemByIndex(0);
    }

    private void showErroMessage(int id) {
        MessageAlert.create(MainActivity.this, MessageAlert.TYPE_ERRO, getString(id));
        setLoadingMode(false);
    }
}