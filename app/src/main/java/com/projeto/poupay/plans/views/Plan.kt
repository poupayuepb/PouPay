package com.projeto.poupay.plans.views

data class Plan(
    val id: Int,
    val title: String,
    val subtitle: String,
    val date: String,
    val value: Double,
) {
    val items: MutableList<ContentPlanItem> = mutableListOf()
    var onContentPlanDelete: (isSucess: Boolean) -> Unit = {}
    fun add(item: ContentPlanItem) {
        item.onContentDeleteListener = {
            onContentPlanDelete.invoke(it)
        }
        this.items.add(item)
    }
}