package com.projeto.poupay.view

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.projeto.poupay.R
import java.text.NumberFormat
import java.util.Locale

class ListSegmentAdapter : RecyclerView.Adapter<ListSegmentAdapter.ViewHolder>() {
    private var mItems: LinkedHashMap<String, MutableList<ContentItem>> = LinkedHashMap()

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val titleText: TextView
        private val totalText: TextView
        private val content: LinearLayout

        init {
            with(view) {
                titleText = findViewById(R.id.Segment_Title)
                totalText = findViewById(R.id.Segment_Total)
                content = findViewById(R.id.Segment_Content)
            }
        }

        fun setup(title: String, items: List<ContentItem>) {
            var total = 0.0
            titleText.text = title
            content.removeAllViews()
            for (item in items) {
                content.addView(item)
                total += item.value
            }
            totalText.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(total)
        }
    }

    fun add(newItem: ContentItem) {
        val key = newItem.date
        if (mItems.containsKey(key)) {
            mItems[key]?.add(newItem)
        } else {
            val list = mutableListOf<ContentItem>()
            list.add(newItem)
            mItems[key] = list
        }
        notifyItemInserted(itemCount - 1)
    }

    fun add(newItems: List<ContentItem>) {
        for (newItem in newItems) {
            add(newItem)
        }
    }

    fun clear() {
        val count = itemCount
        mItems.clear()
        notifyItemRangeRemoved(0, count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.view_segment, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val title: String = mItems.keys.toList()[position]
        val items: List<ContentItem> = mItems[title] ?: listOf()
        holder.setup(title, items)
    }
}