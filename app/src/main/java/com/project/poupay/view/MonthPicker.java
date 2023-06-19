package com.project.poupay.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;

import com.project.poupay.R;

import org.apache.commons.lang3.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class MonthPicker extends LinearLayout {

    private int mYear = 0;
    private int mMonth = 0;
    private View alertContent;
    private final Context mContext;
    private OnMonthPickerAcceptButtonClick onMonthPickerAcceptButtonClick = (int month, int year) -> {
    };

    public MonthPicker(Context context) {
        super(context);
        mContext = context;
        init();


    }

    private void init() {
        alertContent = ((AppCompatActivity) mContext).getLayoutInflater().inflate(R.layout.dialog_month_picker, findViewById(R.id.Month_Picker_Main));

        Drawable mDrawableUnselected = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.transparent, null);
        Drawable mDrawableSelected = ResourcesCompat.getDrawable(mContext.getResources(), R.drawable.month_picker_selected, null);

        Date date = Calendar.getInstance().getTime();
        mYear = Integer.parseInt(new SimpleDateFormat("yyyy", Locale.getDefault()).format(date));
        mMonth = Integer.parseInt(new SimpleDateFormat("MM", Locale.getDefault()).format(date));

        alertContent.findViewById(R.id.Month_Picker_Year_Minus).setOnClickListener(v -> {
            if (mYear > 2000) mYear--;
            update();
        });

        alertContent.findViewById(R.id.Month_Picker_Year_Plus).setOnClickListener(v -> {
            if (mYear < Integer.MAX_VALUE) mYear++;
            update();
        });

        GridLayout monthLayout = alertContent.findViewById(R.id.Month_Picker_Month_Layout);

        for (int i = 0; i < monthLayout.getChildCount(); i++) {
            TextView view = (TextView) monthLayout.getChildAt(i);
            int monthIndex = i;
            view.setOnClickListener(v -> {
                for (int j = 0; j < monthLayout.getChildCount(); j++) monthLayout.getChildAt(j).setBackground(mDrawableUnselected);
                v.setBackground(mDrawableSelected);
                mMonth = monthIndex + 1;
                update();
            });
        }
        update();
    }

    public void show() {
        AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
        alert.setCancelable(false);
        alert.setView(alertContent);
        Dialog dialog = alert.show();

        alertContent.findViewById(R.id.Month_Picker_Cancel).setOnClickListener(v -> dialog.dismiss());
        alertContent.findViewById(R.id.Month_Picker_Confirm).setOnClickListener(v -> {
            onMonthPickerAcceptButtonClick.onSelectChangeListener(mMonth, mYear);
            dialog.dismiss();
        });
    }

    public void update() {
        Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        SimpleDateFormat format = new SimpleDateFormat("MMMM", Locale.getDefault());
        format.setTimeZone(TimeZone.getTimeZone("UTC"));
        calendar.set(Calendar.MONTH, mMonth - 1);
        String monString = StringUtils.capitalize(format.format(calendar.getTime()));
        ((TextView) alertContent.findViewById(R.id.Month_Picker_Title)).setText(String.format(Locale.getDefault(), "%s de %d", monString, mYear));

        ((TextView) alertContent.findViewById(R.id.Month_Picker_Year)).setText(String.valueOf(mYear));
    }

    public void setOnSelectChangeListener(OnMonthPickerAcceptButtonClick onSelectChangeListener) {
        this.onMonthPickerAcceptButtonClick = onSelectChangeListener;
    }

    public interface OnMonthPickerAcceptButtonClick {
        void onSelectChangeListener(int month, int year);
    }

}
