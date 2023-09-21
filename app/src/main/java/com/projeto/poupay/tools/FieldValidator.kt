package com.projeto.poupay.tools

import android.widget.TextView
import java.util.regex.Pattern

class FieldValidator {
    enum class Type {
        USERNAME, PASSWORD
    }

    companion object {
        fun validate(field: TextView, type: Type): Boolean {
            val pattern = if (type == Type.USERNAME) {
                Pattern.compile("^(?=.{8,20}\$)(?![_.])(?!.*[_.]{2})[a-zA-Z0-9._]+(?<![_.])\$")
            } else {
                Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#&()–{}:;',?/*~\$^+=<>]).{8,20}\$")
            }

            var pass = true

            if(!pattern.matcher(field.text).matches()){
                pass = false
                field.error = "A senha deve conter pelo menos um carácter minúsculo, um maiúsculo, um número e um carácter especial."
            }

            if(field.text.isEmpty()){
                pass = false
                field.error = "Campo obrigatório."
            }

            return pass
        }
    }
}