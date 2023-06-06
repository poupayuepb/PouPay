package com.project.poupay.tools;

import android.widget.TextView;

import java.util.regex.Pattern;

public class FieldValidator {

    public static final int TYPE_USERNAME = 0;
    public static final int TYPE_PASSWORD = 1;


    public static boolean validate(String field, int type) {
        Pattern pattern;
        if (type == TYPE_USERNAME) pattern = Pattern.compile("^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$");
        else pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–{}:;',?/*~$^+=<>]).{8,20}$");
        return pattern.matcher(field).matches() && field.length() != 0;
    }

    public static boolean validate(TextView field, int type) {
        Pattern pattern;
        if (type == TYPE_USERNAME) {
            pattern = Pattern.compile("^(?=.{8,20}$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])$");
        } else {
            pattern = Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–{}:;.',?/*~$^+=<>]).{8,20}$");
        }


        boolean pass = true;

        if (!pattern.matcher(field.getText()).matches()) {
            pass = false;
            field.setError("A senha deve conter pelo menos um carácter minúsculo, um maiúsculo, um número e um carácter especial.");
        }
        if (field.getText().length() == 0) {
            pass = false;
            field.setError("Campo obrigatório.");
        }
        return pass;
    }
}
