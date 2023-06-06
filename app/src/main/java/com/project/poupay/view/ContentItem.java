package com.project.poupay.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.google.android.material.imageview.ShapeableImageView;
import com.project.poupay.R;

import java.text.NumberFormat;
import java.util.Locale;

@SuppressLint("ViewConstructor")
public class ContentItem extends ConstraintLayout {
    public static final int CONTENT_ITEM_TYPE_MONEY = 0;
    public static final int CONTENT_ITEM_TYPE_CARD = 1;
    public static final int CONTENT_ITEM_TYPE_PIX = 2;
    public static final int CONTENT_ITEM_TYPE_OTHER = 3;

    private final double mValue;
    private final int mId;
    private final String mDate;

    public ContentItem(Context context, int id, String date, String title, String subtitle, int type, double value) {
        super(context);
        inflate(context, R.layout.view_contentitem, this);

        mValue = value;
        mId = id;
        mDate = date;

        ShapeableImageView mImage = findViewById(R.id.ContentItem_Image);
        TextView mTitle = findViewById(R.id.ContentItem_Title);
        TextView mSubtitle = findViewById(R.id.ContentItem_Subtitle);
        TextView mValue = findViewById(R.id.ContentItem_Value);


        mSubtitle.setText(subtitle);
        mTitle.setText(title);
        mValue.setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value));
        switch (type) {
            case CONTENT_ITEM_TYPE_MONEY:
                mImage.setImageResource(R.drawable.ic_money_18);
                mImage.setBackgroundColor(getResources().getColor(R.color.green));
                break;
            case CONTENT_ITEM_TYPE_CARD:
                mImage.setImageResource(R.drawable.ic_card_18);
                mImage.setBackgroundColor(getResources().getColor(R.color.blue));
                break;
            case CONTENT_ITEM_TYPE_PIX:
                mImage.setImageResource(R.drawable.ic_pix_18);
                mImage.setBackgroundColor(getResources().getColor(R.color.pix));
                break;
            case CONTENT_ITEM_TYPE_OTHER:
                mImage.setImageResource(R.drawable.ic_other_18);
                mImage.setBackgroundColor(getResources().getColor(R.color.orange));
                break;
        }
        mValue.setTextColor(getResources().getColor(value < 0 ? R.color.red : R.color.green));
    }

    public double getValue() {
        return mValue;
    }

    public int getIdNumber() {
        return mId;
    }

    public String getDate() {
        return mDate;
    }

}
