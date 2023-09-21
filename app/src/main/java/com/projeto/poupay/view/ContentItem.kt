package com.projeto.poupay.view

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
class ContentItem(
    private val contentContext: Context,
    val contentId: Int,
    val date: String,
    val title: String,
    val subtitle: String,
    val type: Int,
    val value: Double,
    val parcels: Int
) : ConstraintLayout(contentContext) {

    companion object {
        const val CONTENT_ITEM_TYPE_MONEY = 0
        const val CONTENT_ITEM_TYPE_CARD = 1
        const val CONTENT_ITEM_TYPE_PIX = 2
        const val CONTENT_ITEM_TYPE_OTHER = 3
    }

    var onContentDeleteListener: (hasSucess: Boolean) -> Unit = {}

    init {
        inflate(context, R.layout.view_contentitem, this)

        val mImage = findViewById<ShapeableImageView>(R.id.ContentItem_Image)
        val mTitle = findViewById<TextView>(R.id.ContentItem_Title)
        val mSubtitle = findViewById<TextView>(R.id.ContentItem_Subtitle)
        val mValue = findViewById<TextView>(R.id.ContentItem_Value)
        val mLayout = findViewById<ViewGroup>(R.id.ContentItem_Root)

        mSubtitle.text = subtitle
        mTitle.text = title
        mValue.text = NumberFormat.getCurrencyInstance(Locale.getDefault()).format(value)

        when (type) {
            CONTENT_ITEM_TYPE_MONEY -> {
                mImage.setImageResource(R.drawable.ic_money_18)
                mImage.setBackgroundColor(resources.getColor(R.color.green, null))
            }

            CONTENT_ITEM_TYPE_CARD -> {
                mImage.setImageResource(R.drawable.ic_card_18)
                mImage.setBackgroundColor(resources.getColor(R.color.blue, null))
            }

            CONTENT_ITEM_TYPE_PIX -> {
                mImage.setImageResource(R.drawable.ic_pix_18)
                mImage.setBackgroundColor(resources.getColor(R.color.pix, null))
            }

            CONTENT_ITEM_TYPE_OTHER -> {
                mImage.setImageResource(R.drawable.ic_other_18)
                mImage.setBackgroundColor(resources.getColor(R.color.orange, null))
            }

            else -> {
                mImage.setImageResource(R.drawable.ic_baseline_savings_24)
                mImage.setBackgroundColor(resources.getColor(R.color.background, null))
            }
        }

        if (contentId >= 0) {
            mLayout.background = ResourcesCompat.getDrawable(resources, R.drawable.bg_content_item, null)
            mLayout.isClickable = true
            mLayout.setOnClickListener {
                showDeleteDialog()
            }
        }

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
                SqlQueries.deleteEntry(contentId, contentContext, {
                    onContentDeleteListener.invoke(true)
                }, {
                    onContentDeleteListener.invoke(false)
                })
                dialog.dismiss()
            }
        }
    }

}