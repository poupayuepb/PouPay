package com.project.poupay.view;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.poupay.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ListContentAdapter extends RecyclerView.Adapter<ListContentAdapter.ViewHolder> {

    private final List<ContentReportItem> mItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView viewDrawable;
        private final TextView viewTitle, viewValue;

        public ViewHolder(View view) {
            super(view);
            viewDrawable = view.findViewById(R.id.ContentReportItem_Image);
            viewTitle = view.findViewById(R.id.ContentReportItem_Title);
            viewValue = view.findViewById(R.id.ContentReportItem_Value);
        }

        public void setup(ContentReportItem item) {
            double total = 0;
            viewDrawable.setImageResource(item.getImageID());
            viewTitle.setText(item.getTitle());
            viewValue.setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.getValue()));
            viewValue.setTextColor(item.getValue() < 0 ? Color.parseColor("#E57373") : Color.parseColor("#0FA958"));
        }
    }

    public ListContentAdapter() {
        mItems = new ArrayList<>();
    }

    public void add(ContentReportItem newItem) {
        mItems.add(newItem);
        notifyItemInserted(getItemCount() - 1);
    }

    public void add(List<ContentReportItem> newItems) {
        for (ContentReportItem item : newItems) add(item);
    }

    public void clear() {
        int count = getItemCount();
        mItems.clear();
        notifyItemRangeRemoved(0, count);
    }

    public Double getValueByContentTitle(String title) {
        Double value = null;
        for (ContentReportItem item : mItems) if (item.getTitle().equals(title)) return item.getValue();
        return value;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_contentreportitem, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        viewHolder.setup(mItems.get(position));
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}