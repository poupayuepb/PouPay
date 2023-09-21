package com.projeto.poupay.plans.views

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.projeto.poupay.R

class ListPlansAdapter : RecyclerView.Adapter<PlanViewHolder>() {

    private val mItems = mutableListOf<Plan>()
    private var onPlanDeleteListener: (isSucess: Boolean) -> Unit = {}
    private var onAddButtonClickListener: (id: Int, value: Double, detail: String) -> Unit = { _, _, _ -> }

    fun add(id: Int, title: String, subtitle: String, date: String, value: Double, items: List<ContentPlanItem>) {
        val newPlan = Plan(id, title, subtitle, date, value)
        for (item in items) newPlan.add(item)
        mItems.add(newPlan)
        notifyItemInserted(mItems.size)
    }

    fun add(plan: Plan) {
        mItems.add(plan)
        notifyItemInserted(mItems.size)
    }

    fun clear() {
        val count = itemCount
        mItems.clear()
        notifyItemRangeRemoved(0, count)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlanViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.view_addplanvalue, parent, false)
        val planView = PlanViewHolder(view)
        planView.setOnAddButtonClickListener(this.onAddButtonClickListener)
        planView.setOnPlanDeleteListener(this.onPlanDeleteListener)
        return planView
    }

    override fun getItemCount(): Int {
        return mItems.size
    }

    override fun onBindViewHolder(holder: PlanViewHolder, position: Int) {
        val item = mItems[position]
        holder.setup(item.id, item.title, item.subtitle, item.date, item.value, item.items)
    }

    fun setOnPlanDeleteListener(listener: (isSucess: Boolean) -> Unit) {
        this.onPlanDeleteListener = listener
    }

    fun setOnAddButtonClickListener(listener: (id: Int, value: Double, detail: String) -> Unit) {
        this.onAddButtonClickListener = listener
    }

}