package com.projeto.poupay.view

import android.annotation.SuppressLint
import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.TextView
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.projeto.poupay.R
import net.objecthunter.exp4j.ExpressionBuilder

@SuppressLint("ClickableViewAccessibility")
class MainActivityCalculator(context: Context) : BottomSheetDialog(context) {
    private var txtExpression: TextView
    private var txtResult: TextView
    private var isParenthesesOpen = false

    init {
        val view = LayoutInflater.from(context).inflate(R.layout.calculator, findViewById(R.id.Calculator_Main))
        setContentView(view)

        view.apply {
            txtExpression = findViewById(R.id.txt_expression)
            txtResult = findViewById(R.id.txt_result)

            findViewById<View>(R.id.num_zero).setOnClickListener { addExpression("0") }
            findViewById<View>(R.id.num_one).setOnClickListener { addExpression("1") }
            findViewById<View>(R.id.num_two).setOnClickListener { addExpression("2") }
            findViewById<View>(R.id.num_three).setOnClickListener { addExpression("3") }
            findViewById<View>(R.id.num_four).setOnClickListener { addExpression("4") }
            findViewById<View>(R.id.num_five).setOnClickListener { addExpression("5") }
            findViewById<View>(R.id.num_six).setOnClickListener { addExpression("6") }
            findViewById<View>(R.id.num_seven).setOnClickListener { addExpression("7") }
            findViewById<View>(R.id.num_eight).setOnClickListener { addExpression("8") }
            findViewById<View>(R.id.num_nine).setOnClickListener { addExpression("9") }
            findViewById<View>(R.id.point).setOnClickListener { addExpression(".") }
            findViewById<View>(R.id.sum).setOnClickListener { addExpression("+") }
            findViewById<View>(R.id.subtraction).setOnClickListener { addExpression("-") }
            findViewById<View>(R.id.multiplication).setOnClickListener { addExpression("×") }
            findViewById<View>(R.id.division).setOnClickListener { addExpression("÷") }
            findViewById<View>(R.id.percentage).setOnClickListener { addExpression("%") }
            findViewById<View>(R.id.parentheses).setOnClickListener {
                isParenthesesOpen = if (isParenthesesOpen) {
                    addExpression(")")
                    false
                } else {
                    addExpression("(")
                    true
                }
            }

            findViewById<View>(R.id.clean).setOnClickListener {
                txtExpression.text = ""
                txtResult.text = ""
                txtExpression.isSelected = false
                txtExpression.onCreateInputConnection(EditorInfo()).setSelection(txtExpression.text.length, txtExpression.text.length)
            }

            findViewById<View>(R.id.backspace).setOnClickListener {
                val ic = txtExpression.onCreateInputConnection(EditorInfo())
                if (ic.getSelectedText(0).isEmpty()) {
                    ic.deleteSurroundingText(1, 0)
                } else {
                    ic.commitText("", 1)
                }
            }

            findViewById<View>(R.id.equal).setOnClickListener {
                txtExpression.text = txtResult.text
                txtExpression.onCreateInputConnection(EditorInfo()).setSelection(txtExpression.text.length, txtExpression.text.length)
            }

            txtExpression.setRawInputType(InputType.TYPE_CLASS_TEXT)
            txtExpression.setTextIsSelectable(true)
            txtExpression.inputType = (txtExpression.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS)
            txtExpression.addTextChangedListener(afterTextChanged = { calculateResult() })
            txtExpression.setOnTouchListener { view, event ->
                val editText = view as EditText
                val inType = editText.inputType
                editText.inputType = InputType.TYPE_NULL
                editText.onTouchEvent(event)
                editText.inputType = inType
                return@setOnTouchListener true
            }
        }
    }

    private fun calculateResult() {
        try {
            var expressionString = txtExpression.text.toString()

            expressionString = expressionString.replace("%", "*0.01")
            expressionString = expressionString.replace("×", "*")
            expressionString = expressionString.replace("÷", "/")

            val expression = ExpressionBuilder(expressionString).build()
            val result = expression.evaluate()
            val longResult = result.toLong()

            if (result == longResult.toDouble()) {
                txtResult.text = longResult.toString()
            } else {
                txtResult.text = result.toString()
            }
        } catch (e: Exception) {
            txtResult.text = ""
        }
    }

    private fun addExpression(expression: String) {
        txtExpression.onCreateInputConnection(EditorInfo()).commitText(expression, 1)
        calculateResult()
    }
}