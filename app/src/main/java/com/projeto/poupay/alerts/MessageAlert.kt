package com.projeto.poupay.alerts

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.projeto.poupay.R
import com.tapadoo.alerter.Alerter

class MessageAlert {
    enum class Type {
        ALERT, ERROR, SUCCESS
    }

    companion object {
        fun create(context: Activity, message: String, title: String, onConfirm: () -> Unit, onCancel: () -> Unit = {}) {
            val alertContent = context.layoutInflater.inflate(R.layout.dialog_confirm_message, context.findViewById(R.id.Confirm_Root))
            val alert = AlertDialog.Builder(context)
            alert.setCancelable(false)
            alert.setView(alertContent)
            val dialog = alert.show()
            dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

            alertContent.apply {
                findViewById<TextView>(R.id.Confirm_Title).text = title
                findViewById<TextView>(R.id.Confirm_Message).text = message
                findViewById<Button>(R.id.Confirm_Confirm).setOnClickListener {
                    onConfirm.invoke()
                    dialog.dismiss()
                }
                findViewById<Button>(R.id.Confirm_Cancel).setOnClickListener {
                    onCancel.invoke()
                    dialog.dismiss()
                }
            }
        }

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