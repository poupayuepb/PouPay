package com.projeto.poupay.view

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.projeto.poupay.R
import java.text.NumberFormat
import java.util.Locale

class ListContentAdapter : RecyclerView.Adapter<ListContentAdapter.ViewHolder>() {

    private var mItens: MutableList<ContentReportItem> = mutableListOf()
    private var onItemClickListener: (item: ContentReportItem) -> Unit = {}

    class ViewHolder(val view: View) : RecyclerView.ViewHolder(view) {
        private val viewRootLayout: ConstraintLayout
        private val viewDrawable: ImageView
        private val viewTitle: TextView
        private val viewValue: TextView
        private var onItemClickListener: (item: ContentReportItem) -> Unit = {}

        init {
            with(view) {
                viewRootLayout = findViewById(R.id.ContentReportItem_Root)
                viewDrawable = findViewById(R.id.ContentReportItem_Image)
                viewTitle = findViewById(R.id.ContentReportItem_Title)
                viewValue = findViewById(R.id.ContentReportItem_Value)
            }
        }

        fun setup(item: ContentReportItem) {
            viewRootLayout.setOnClickListener { onItemClickListener.invoke(item) }
            viewDrawable.setImageResource(item.getImageID())
            viewTitle.text = item.subtitule.ifEmpty { item.title }
            viewValue.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(item.value)
            viewValue.setTextColor(if (item.value < 0) Color.parseColor("#E57373") else Color.parseColor("#0FA958"))
        }

        fun setOnItemClickListener(listener: (item: ContentReportItem) -> Unit) {
            this.onItemClickListener = listener
        }

    }

    fun setOnItemClickListener(listener: (item: ContentReportItem) -> Unit) {
        this.onItemClickListener = listener
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

    fun getByTitle(): List<ContentReportItem> {
        val filteredList = mutableListOf<ContentReportItem>()
        mItens.forEach { superItem ->
            var alreadyHasItem = false
            filteredList.forEach { item ->
                if (superItem.title == item.title) {
                    alreadyHasItem = true
                    item.value += superItem.value
                }
            }
            if(!alreadyHasItem){
                filteredList.add(superItem.copy())
            }
        }
        return filteredList
    }

    fun clear() {
        val count = itemCount
        mItens.clear()
        notifyItemRangeRemoved(0, count)
    }

    fun getValueByContentTitle(title: String): Double? {
        val internalItems = getByTitle()
        for (item in internalItems) {
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
        holder.setOnItemClickListener(this.onItemClickListener)

    }
}