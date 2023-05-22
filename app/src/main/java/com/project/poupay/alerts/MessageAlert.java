package com.project.poupay.alerts;

import android.content.Context;
import android.view.Gravity;

import androidx.appcompat.app.AppCompatActivity;

import com.project.poupay.R;
import com.tapadoo.alerter.Alerter;


public class MessageAlert {
    public static final int TYPE_ALERT = 0;
    public static final int TYPE_ERRO = 1;
    public static final int TYPE_SUCESS = 2;

    public static void create(Context context, int messageType, String message) {
        switch (messageType) {
            case TYPE_ALERT:
                Alerter.create((AppCompatActivity) context)
                        .setTitle("Alerta")
                        .setText(message)
                        .setIcon(R.drawable.ic_alert)
                        .setBackgroundColorRes(R.color.yellow)
                        .setDuration(8000)
                        .setLayoutGravity(Gravity.BOTTOM)
                        .show();
                break;
            case TYPE_ERRO:
                Alerter.create((AppCompatActivity) context)
                        .setTitle("Erro")
                        .setText(message)
                        .setIcon(R.drawable.ic_erro)
                        .setBackgroundColorRes(R.color.red)
                        .setDuration(8000)
                        .setLayoutGravity(Gravity.BOTTOM)
                        .show();
                break;
            default:
                Alerter.create((AppCompatActivity) context)
                        .setTitle("Sucesso")
                        .setText(message)
                        .setLayoutGravity(Gravity.BOTTOM)
                        .setIcon(R.drawable.ic_done)
                        .setBackgroundColorRes(R.color.green)
                        .setDuration(8000)
                        .show();
                break;
        }

    }
}