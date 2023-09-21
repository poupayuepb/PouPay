package com.projeto.poupay.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.res.ResourcesCompat
import com.projeto.poupay.R

class SwitchSelector(context: Context, attrs: AttributeSet?) : LinearLayout(context, attrs) {
    private val buttons = mutableListOf<TextView>()
    var selectedId = 0
    var onSelectChangeListener: (id: Int, text: String) -> Unit = { _, _ -> }

    init {
        val attributes = context.obtainStyledAttributes(attrs, R.styleable.SwitchSelector)
        try {
            val fontSize = attributes.getDimensionPixelSize(R.styleable.SwitchSelector_text_size, 14)
            val texts = attributes.getTextArray(R.styleable.SwitchSelector_options_list)

            if (texts != null) {
                for (text in texts) {
                    val newTextView = TextView(context)
                    val layParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT, 1.0f)

                    newTextView.layoutParams = layParams
                    newTextView.background = AppCompatResources.getDrawable(context, R.drawable.bg_middle_button)
                    newTextView.backgroundTintList = ColorStateList.valueOf(context.resources.getColor(R.color.gray_light, null))
                    newTextView.gravity = Gravity.CENTER
                    newTextView.setTextColor(Color.WHITE)
                    newTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, fontSize.toFloat())
                    newTextView.setTypeface(null, Typeface.BOLD)
                    newTextView.text = text
                    newTextView.setOnClickListener {
                        if (this.isEnabled) setSelectedButton(it as TextView)
                    }
                    buttons.add(newTextView)
                }
                buttons[0].background = AppCompatResources.getDrawable(context, R.drawable.bg_left_button)
                buttons[buttons.size - 1].background = AppCompatResources.getDrawable(context, R.drawable.bg_right_button)
            }
        } finally {
            attributes.recycle()
            inflate(context, R.layout.view_switchselector, findViewById(R.id.SwitchSelector_Main))
            for (button in buttons) {
                addView(button)
            }
            setSelectedButton(0)
        }
    }

    private fun setSelectedButton(button: TextView) {
        setSelectedButton(buttons.indexOf(button))
    }

    fun setSelectedButton(index: Int) {
        selectedId = index
        for (button in buttons) {
            button.backgroundTintList = ColorStateList.valueOf(context.resources.getColor(R.color.gray_light, null))
        }
        buttons[index].backgroundTintList = ColorStateList.valueOf(context.resources.getColor(R.color.background, null))
        onSelectChangeListener.invoke(index, buttons[index].text.toString())

    }

    fun setButtonIcon(buttonId: Int, drawableId: Int) {
        val icon = ResourcesCompat.getDrawable(context.resources, drawableId, null)
        icon?.setTint(Color.WHITE)
        buttons[buttonId].setCompoundDrawablesWithIntrinsicBounds(null, icon, null, null)
        buttons[buttonId].compoundDrawablePadding = -20
        buttons[buttonId].setPadding(0, 20, 0, 0)
    }
}