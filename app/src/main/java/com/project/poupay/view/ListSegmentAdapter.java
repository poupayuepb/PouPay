package com.project.poupay.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.poupay.R;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class ListSegmentAdapter extends RecyclerView.Adapter<ListSegmentAdapter.ViewHolder> {

    private final LinkedHashMap<String, List<ContentItem>> mItems;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleText, totalText;
        private final LinearLayout content;

        public ViewHolder(View view) {
            super(view);
            titleText = view.findViewById(R.id.Segment_Title);
            totalText = view.findViewById(R.id.Segment_Total);
            content = view.findViewById(R.id.Segment_Content);
        }

        public void setup(String title, List<ContentItem> items) {
            double total = 0;
            titleText.setText(title);
            content.removeAllViews();
            for (ContentItem item : items) {
                content.addView(item);
                total += item.getValue();
            }
            totalText.setText(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total));
        }
    }

    public ListSegmentAdapter() {
        mItems = new LinkedHashMap<>();
    }

    public void add(ContentItem newItem) {
        String key = newItem.getDate();
        if (mItems.containsKey(key)) {
            Objects.requireNonNull(mItems.get(key)).add(newItem);
        } else {
            List<ContentItem> list = new ArrayList<>();
            list.add(newItem);
            mItems.put(key, list);
        }
        notifyItemInserted(getItemCount() - 1);
    }

    public void add(List<ContentItem> newItems) {
        for (ContentItem item : newItems) add(item);
    }

    public void clear() {
        int count = getItemCount();
        mItems.clear();
        notifyItemRangeRemoved(0, count);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.view_segment, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {
        String title = (String) mItems.keySet().toArray()[position];
        List<ContentItem> items = new ArrayList<>(Objects.requireNonNull(mItems.get(title)));
        viewHolder.setup(title, items);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }



}
