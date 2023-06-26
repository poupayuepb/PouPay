package com.project.poupay;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Editable;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class MainActivity_Calculator extends BottomSheetDialog {

    private final TextView txtExpression;
    private final TextView txtResult;
    boolean isParenthesesOpen = false;

    @SuppressLint("ClickableViewAccessibility")
    public MainActivity_Calculator(@NonNull Context context) {
        super(context, R.style.BottomAddDialogStyle);
        View view = LayoutInflater.from(context).inflate(R.layout.calculator, findViewById(R.id.Calculator_Main));
        setContentView(view);

        txtExpression = view.findViewById(R.id.txt_expression);
        txtResult = view.findViewById(R.id.txt_result);
        view.findViewById(R.id.num_zero).setOnClickListener(v -> addExpression("0"));
        view.findViewById(R.id.num_one).setOnClickListener(v -> addExpression("1"));
        view.findViewById(R.id.num_two).setOnClickListener(v -> addExpression("2"));
        view.findViewById(R.id.num_three).setOnClickListener(v -> addExpression("3"));
        view.findViewById(R.id.num_four).setOnClickListener(v -> addExpression("4"));
        view.findViewById(R.id.num_five).setOnClickListener(v -> addExpression("5"));
        view.findViewById(R.id.num_six).setOnClickListener(v -> addExpression("6"));
        view.findViewById(R.id.num_seven).setOnClickListener(v -> addExpression("7"));
        view.findViewById(R.id.num_eight).setOnClickListener(v -> addExpression("8"));
        view.findViewById(R.id.num_nine).setOnClickListener(v -> addExpression("9"));
        view.findViewById(R.id.point).setOnClickListener(v -> addExpression("."));
        view.findViewById(R.id.sum).setOnClickListener(v -> addExpression("+"));
        view.findViewById(R.id.subtraction).setOnClickListener(v -> addExpression("-"));
        view.findViewById(R.id.multiplication).setOnClickListener(v -> addExpression("×"));
        view.findViewById(R.id.division).setOnClickListener(v -> addExpression("÷"));
        view.findViewById(R.id.percentage).setOnClickListener(v -> addExpression("%"));
        view.findViewById(R.id.parentheses).setOnClickListener(v -> {
            if (isParenthesesOpen) {
                addExpression(")");
                isParenthesesOpen = false;
            } else {
                addExpression("(");
                isParenthesesOpen = true;
            }
        });

        view.findViewById(R.id.clean).setOnClickListener(v -> {
            txtExpression.setText("");
            txtResult.setText("");
            txtExpression.setSelected(false);
            txtExpression.onCreateInputConnection(new EditorInfo()).setSelection(txtExpression.getText().length(), txtExpression.getText().length());
        });

        view.findViewById(R.id.backspace).setOnClickListener(v -> {
            InputConnection ic = txtExpression.onCreateInputConnection(new EditorInfo());
            if (TextUtils.isEmpty(ic.getSelectedText(0))) ic.deleteSurroundingText(1, 0);
            else ic.commitText("", 1);
        });

        view.findViewById(R.id.equal).setOnClickListener(v -> {
            txtExpression.setText(txtResult.getText());
            txtExpression.onCreateInputConnection(new EditorInfo()).setSelection(txtExpression.getText().length(), txtExpression.getText().length());
        });

        txtExpression.setRawInputType(InputType.TYPE_CLASS_TEXT);
        txtExpression.setTextIsSelectable(true);
        txtExpression.setInputType(txtExpression.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        txtExpression.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                calculateResult();
            }
        });
        txtExpression.setOnTouchListener((v, event) -> {
            EditText edittext = (EditText) v;
            int inType = edittext.getInputType();
            edittext.setInputType(InputType.TYPE_NULL);
            edittext.onTouchEvent(event);
            edittext.setInputType(inType);
            return true;
        });
    }


    public void addExpression(String string) {
        txtExpression.onCreateInputConnection(new EditorInfo()).commitText(string, 1);
        calculateResult();
    }

    private void calculateResult() {
        try {
            String expressionString = txtExpression.getText().toString();

            expressionString = expressionString.replaceAll("%", "*0.01");
            expressionString = expressionString.replaceAll("×", "*");
            expressionString = expressionString.replaceAll("÷", "/");

            Expression expression = new ExpressionBuilder(expressionString).build();
            double result = expression.evaluate();
            long longResult = (long) result;

            if (result == (double) longResult) {
                txtResult.setText(String.valueOf(longResult));
            } else {
                txtResult.setText(String.valueOf(result));
            }
        } catch (Exception e) {
            txtResult.setText("");
        }
    }


}
