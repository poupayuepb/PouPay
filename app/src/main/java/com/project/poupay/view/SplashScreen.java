package com.project.poupay.view;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.project.poupay.BuildConfig;
import com.project.poupay.R;

public class SplashScreen extends AppCompatActivity {

    ImageView img_logo;
    TextView txt_namePouPay, txt_slogan;
    Animation top, bottom;

    private static final int SPLASH_DURATION = 3000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
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

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(SplashScreen.this, TelaLogin.class);
                startActivity(intent);
                finish();
            }
        },SPLASH_DURATION);
    }
}