package com.project.poupay;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ImageView img_logo;
    TextView txt_namePouPay, txt_slogan;
    Animation top, bottom;

    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        img_logo = findViewById(R.id.img_logo);
        txt_namePouPay = findViewById(R.id.txt_namePouPay);
        txt_slogan = findViewById(R.id.txt_slogan);

        ((TextView)findViewById(R.id.txt_versao)).setText(BuildConfig.VERSION_NAME);

        top = AnimationUtils.loadAnimation(this, R.anim.top);
        bottom = AnimationUtils.loadAnimation(this, R.anim.bottom);

        img_logo.setAnimation(top);
        txt_namePouPay.setAnimation(bottom);
        txt_slogan.setAnimation(bottom);

        new Handler().postDelayed(() -> {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        },SPLASH_DURATION);
    }
}