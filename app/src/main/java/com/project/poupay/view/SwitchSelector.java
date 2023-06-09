package com.project.poupay.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.res.ResourcesCompat;

import com.project.poupay.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class SwitchSelector extends LinearLayout {

    private final Context mContext;
    private List<TextView> mButtons;
    private int mSelectedId = 0;
    private OnSelectChangeListener onSelectChangeListener = (id, text) -> {
    };
    private boolean isEnabled = true;

    @SuppressWarnings("resource")
    public SwitchSelector(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        mContext = context;
        TypedArray attributes = context.obtainStyledAttributes(attrs, R.styleable.SwitchSelector);
        try {
            // Font_Size
            int fontSize = attributes.getDimensionPixelSize(R.styleable.SwitchSelector_text_size, 14);

            // TextViews
            CharSequence[] texts = attributes.getTextArray(R.styleable.SwitchSelector_options_list);
            if (texts != null) {
                mButtons = new ArrayList<>();
                for (CharSequence text : texts) {
                    TextView newTextView = new TextView(mContext);
                    LinearLayout.LayoutParams layParams = new LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT, 1.0f);
                    newTextView.setLayoutParams(layParams);
                    newTextView.setBackground(AppCompatResources.getDrawable(mContext, R.drawable.bg_middle_button));
                    newTextView.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(R.color.gray_light)));
                    newTextView.setGravity(Gravity.CENTER);
                    newTextView.setTextColor(Color.WHITE);
                    newTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize);
                    newTextView.setTypeface(null, Typeface.BOLD);
                    newTextView.setText(text);
                    newTextView.setOnClickListener(v -> {
                        if (this.isEnabled) setSelectedButton((TextView) v);
                    });
                    mButtons.add(newTextView);
                }
                mButtons.get(0).setBackground(AppCompatResources.getDrawable(mContext, R.drawable.bg_left_button));
                mButtons.get(mButtons.size() - 1).setBackground(AppCompatResources.getDrawable(mContext, R.drawable.bg_right_button));
            }

        } finally {
            attributes.recycle();
            init();
        }

    }

    private void init() {
        inflate(mContext, R.layout.view_switchselector, findViewById(R.id.SwitchSelector_Main));
        for (TextView button : mButtons) addView(button);
        setSelectedButton(0);

    }

    public void setSelectedButton(TextView selectedButton) {
        setSelectedButton(mButtons.indexOf(selectedButton));
    }

    public void setSelectedButton(int selectedId) {
        mSelectedId = selectedId;
        for (TextView button : mButtons) button.setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(R.color.gray_light)));
        mButtons.get(mSelectedId).setBackgroundTintList(ColorStateList.valueOf(mContext.getResources().getColor(R.color.background)));
        onSelectChangeListener.onSelectChangeListener(mSelectedId, mButtons.get(mSelectedId).getText().toString());
    }

    public String getSelectedText() {
        return mButtons.get(mSelectedId).getText().toString();
    }

    public int getSelectedId() {
        return mSelectedId;
    }

    public void setOnSelectChangeListener(OnSelectChangeListener onSelectChangeListener) {
        this.onSelectChangeListener = onSelectChangeListener;
    }

    public void setButtonIcon(int buttonId, int drawableId) {
        Drawable icon = ResourcesCompat.getDrawable(mContext.getResources(), drawableId, null);
        Objects.requireNonNull(icon).setTint(Color.WHITE);
        mButtons.get(buttonId).setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null);
        mButtons.get(buttonId).setCompoundDrawablePadding(-20);
        mButtons.get(buttonId).setPadding(0, 20, 0, 0);
    }

    public interface OnSelectChangeListener {
        void onSelectChangeListener(int id, String text);
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        isEnabled = enabled;
    }
}
