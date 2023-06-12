package com.project.poupay;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.bottomsheet.BottomSheetDialog;

import net.objecthunter.exp4j.Expression;
import net.objecthunter.exp4j.ExpressionBuilder;

public class Calculator extends BottomSheetDialog implements View.OnClickListener{

    Button numZero, numOne, numTwo, numThree, numFour, numFive, numSix, numSeven, numEight, numNine,
            point, sum, subtraction, division, multiplication, percentage, equal, clean, parentheses;
    TextView txtExpression, txtResult;
    ImageView backspace;

    public Calculator(@NonNull Context context) {
        super(context, R.style.BottomAddDialogStyle);

        View view = LayoutInflater.from(context).inflate(R.layout.calculator, null);
        setContentView(view);

        initComponents();

        numZero.setOnClickListener(this);
        numOne.setOnClickListener(this);
        numTwo.setOnClickListener(this);
        numThree.setOnClickListener(this);
        numFour.setOnClickListener(this);
        numFive.setOnClickListener(this);
        numSix.setOnClickListener(this);
        numSeven.setOnClickListener(this);
        numEight.setOnClickListener(this);
        numNine.setOnClickListener(this);
        point.setOnClickListener(this);
        sum.setOnClickListener(this);
        subtraction.setOnClickListener(this);
        multiplication.setOnClickListener(this);
        division.setOnClickListener(this);
        percentage.setOnClickListener(this);
        parentheses.setOnClickListener(this);

        clean.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                txtExpression.setText("");
                txtResult.setText("");
            }
        });

        backspace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextView expression = findViewById(R.id.txt_expression);
                String string = expression.getText().toString();

                if (!string.isEmpty()) {

                    byte var0 = 0;
                    int var1 = string.length() - 1;
                    String txtExpression = string.substring(var0, var1);
                    expression.setText(txtExpression);
                }
                txtResult.setText("");
            }
        });

        equal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                txtExpression.setText(txtResult.getText());
            }
        });

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
}
    private void initComponents() {
        numZero = findViewById(R.id.num_zero);
        numOne = findViewById(R.id.num_one);
        numTwo = findViewById(R.id.num_two);
        numThree = findViewById(R.id.num_three);
        numFour = findViewById(R.id.num_four);
        numFive = findViewById(R.id.num_five);
        numSix = findViewById(R.id.num_six);
        numSeven = findViewById(R.id.num_seven);
        numEight = findViewById(R.id.num_eight);
        numNine = findViewById(R.id.num_nine);
        point = findViewById(R.id.point);
        sum = findViewById(R.id.sum);
        subtraction = findViewById(R.id.subtraction);
        multiplication = findViewById(R.id.multiplication);
        division = findViewById(R.id.division);
        equal = findViewById(R.id.equal);
        clean = findViewById(R.id.clean);
        txtExpression = findViewById(R.id.txt_expression);
        txtResult = findViewById(R.id.txt_result);
        backspace = findViewById(R.id.backspace);
        percentage = findViewById(R.id.percentage);
        parentheses = findViewById(R.id.parentheses);
    }

    public void addExpression(String string, boolean clearData) {
        String expression = txtExpression.getText().toString();

        if (clearData) {
            txtExpression.setText(expression + string);
        } else {
            txtExpression.append(string);
        }
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


    boolean isParenthesesOpen = false;
    @Override
    public void onClick(View view) {

        switch (view.getId()) {
            case R.id.num_zero:
                addExpression("0", true);
                break;

            case R.id.num_one:
                addExpression("1", true);
                break;

            case R.id.num_two:
                addExpression("2", true);
                break;

            case R.id.num_three:
                addExpression("3", true);
                break;

            case R.id.num_four:
                addExpression("4", true);
                break;

            case R.id.num_five:
                addExpression("5", true);
                break;

            case R.id.num_six:
                addExpression("6", true);
                break;

            case R.id.num_seven:
                addExpression("7", true);
                break;

            case R.id.num_eight:
                addExpression("8", true);
                break;

            case R.id.num_nine:
                addExpression("9", true);
                break;

            case R.id.point:
                addExpression(".", true);
                break;

            case R.id.sum:
                addExpression("+", false);
                break;

            case R.id.subtraction:
                addExpression("-", false);
                break;

            case R.id.multiplication:
                addExpression("×", false);
                break;

            case R.id.division:
                addExpression("÷", false);
                break;

            case R.id.percentage:
                addExpression("%", false);
                break;

            case R.id.parentheses:

                if (isParenthesesOpen){
                    addExpression(")", false);
                    isParenthesesOpen = false;
                }else{
                    addExpression("(", false);
                    isParenthesesOpen = true;
                }
                break;
        }
    }
}
