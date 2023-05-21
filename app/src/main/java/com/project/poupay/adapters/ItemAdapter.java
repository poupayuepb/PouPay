package com.project.poupay.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.poupay.R;
import com.project.poupay.model.Item;

import java.util.List;

public class ItemAdapter extends RecyclerView.Adapter<ItemAdapter.ItemViewHolder> {

    Context context;
    static List<Item> itemList;
    Item item;

    public ItemAdapter(Context context, List<Item> itemList) {
        this.context = context;
        this.itemList = itemList;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v;
        v = LayoutInflater.from(context).inflate(R.layout.item_rv, parent, false);
        final ItemViewHolder viewHolder = new ItemViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {

        holder.img_pay.setImageResource(itemList.get(position).getIcon());
        holder.txt_description.setText(String.valueOf(itemList.get(position).getDescription()));
        holder.txt_payment_type.setText(String.valueOf(itemList.get(position).getPaymentType()));
        holder.txt_price.setText(String.valueOf(itemList.get(position).getPrice()));

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public static class ItemViewHolder extends RecyclerView.ViewHolder{

        ImageView img_pay;
        TextView txt_description, txt_payment_type, txt_price;

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);

            img_pay = itemView.findViewById(R.id.img_pay);
            txt_description = itemView.findViewById(R.id.txt_description);
            txt_payment_type = itemView.findViewById(R.id.txt_payment_type);
            txt_price = itemView.findViewById(R.id.txt_price);

        }
    }
}
