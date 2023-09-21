package com.projeto.poupay.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projeto.poupay.R
import java.text.NumberFormat
import java.util.Locale

class ListContentAdapter : RecyclerView.Adapter<ListContentAdapter.ViewHolder>() {

    private var mItens: MutableList<ContentReportItem> = mutableListOf()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val viewDrawable: ImageView
        private val viewTitle: TextView
        private val viewValue: TextView

        init {
            with(view) {
                viewDrawable = findViewById(R.id.ContentReportItem_Image)
                viewTitle = findViewById(R.id.ContentReportItem_Title)
                viewValue = findViewById(R.id.ContentReportItem_Value)
            }
        }

        fun setup(item: ContentReportItem) {
            viewDrawable.setImageResource(item.getImageID())
            viewTitle.text = item.title
            viewValue.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.value)
            viewValue.setTextColor(if (item.value < 0) Color.parseColor("#E57373") else Color.parseColor("#0FA958"))
        }

    }

    fun add(newItem: ContentReportItem) {
        mItens.add(newItem)
        notifyItemInserted(itemCount - 1)
    }

    fun add(newItems: List<ContentReportItem>) {
        for (item in newItems) {
            add(item)
        }
    }

    fun clear() {
        val count = itemCount
        mItens.clear()
        notifyItemRangeRemoved(0, count)
    }

    fun getValueByContentTitle(title: String): Double? {
        for (item in mItens) {
            if (item.title == title) {
                return item.value
            }
        }
        return null
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_contentreportitem, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItens.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.setup(mItens[position])
    }
}