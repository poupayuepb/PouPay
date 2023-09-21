package com.projeto.poupay.alerts

import android.app.Activity
import android.view.Gravity
import com.projeto.poupay.R
import com.tapadoo.alerter.Alerter

class MessageAlert {
    enum class Type {
        ALERT, ERROR, SUCCESS
    }

    companion object {
        fun create(context: Activity, type: Type, message: String) {
            when (type) {
                Type.ALERT -> {
                    Alerter.create(context)
                        .setTitle("Alerta")
                        .setText(message)
                        .setIcon(R.drawable.ic_alert)
                        .setBackgroundColorRes(R.color.yellow)
                        .setDuration(8000)
                        .setLayoutGravity(Gravity.BOTTOM)
                        .show()
                }

                Type.ERROR -> {
                    Alerter.create(context)
                        .setTitle("Erro")
                        .setText(message)
                        .setIcon(R.drawable.ic_erro)
                        .setBackgroundColorRes(R.color.red)
                        .setDuration(8000)
                        .setLayoutGravity(Gravity.BOTTOM)
                        .show()
                }

                Type.SUCCESS -> {
                    Alerter.create(context)
                        .setTitle("Sucesso")
                        .setText(message)
                        .setIcon(R.drawable.ic_done)
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(8000)
                        .setLayoutGravity(Gravity.BOTTOM)
                        .show()
                }
            }
        }
    }
}