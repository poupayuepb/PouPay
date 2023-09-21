package com.projeto.poupay.plans.views

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.imageview.ShapeableImageView
import com.projeto.poupay.R
import com.projeto.poupay.sql.SqlQueries
import java.text.NumberFormat
import java.util.Locale

@SuppressLint("ViewConstructor")
class ContentPlanItem(
    private val contentContext: Context,
    private val contentId: Int,
    val title: String,
    val date: String,
    val value: Double,
) : ConstraintLayout(contentContext) {

    var onContentDeleteListener: (hasSucess: Boolean) -> Unit = {}

    init {
        inflate(context, R.layout.view_contentitem, this)

        val mImage = findViewById<ShapeableImageView>(R.id.ContentItem_Image)
        val mTitle = findViewById<TextView>(R.id.ContentItem_Title)
        val mSubtitle = findViewById<TextView>(R.id.ContentItem_Subtitle)
        val mValue = findViewById<TextView>(R.id.ContentItem_Value)
        val mLayout = findViewById<ViewGroup>(R.id.ContentItem_Root)

        mSubtitle.text = date
        mTitle.text = title
        mValue.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)

        mImage.setImageResource(R.drawable.ic_baseline_savings_24)
        mImage.setBackgroundColor(resources.getColor(R.color.background, null))

        mLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_content_item, null)
        mLayout.isClickable = true
        mLayout.setOnClickListener { showDeleteDialog() }

        mValue.setTextColor(resources.getColor(if (value < 0) R.color.red else R.color.green, null))
    }

    private fun showDeleteDialog() {
        val alertContent = (contentContext as Activity).layoutInflater.inflate(R.layout.dialog_content_delete, findViewById(R.id.ContentDelete_Root))
        val alert = AlertDialog.Builder(contentContext)
        alert.setView(alertContent)
        val dialog = alert.show()

        alertContent.apply {
            findViewById<TextView>(R.id.ContentDelete_Details).text = buildString {
                append(title)
                append(": ")
                append(NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value))
            }
            findViewById<View>(R.id.ContentDelete_Cancel).setOnClickListener { dialog.dismiss() }
            findViewById<View>(R.id.ContentDelete_Confirm).setOnClickListener {
                SqlQueries.deletePlanContent(contentId, contentContext, {
                    onContentDeleteListener.invoke(true)
                }, {
                    onContentDeleteListener.invoke(false)
                })
                dialog.dismiss()
            }
        }
    }
}