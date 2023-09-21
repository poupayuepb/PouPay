package com.projeto.poupay

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.TextView

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    companion object {
        private const val SPLASH_DURATION: Long = 3000
    }

    private lateinit var mLogo: ImageView
    private lateinit var mLogoTitle: TextView
    private lateinit var mLogoSubtitle: TextView
    private lateinit var animTop: Animation
    private lateinit var animBottom: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash_screen)

        initViews()

        mLogo.animation = animTop
        mLogoTitle.animation = animBottom
        mLogoSubtitle.animation = animBottom

        findViewById<TextView>(R.id.txt_versao).text = packageManager.getPackageInfo(packageName, 0).versionName

        Handler(mainLooper).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, SPLASH_DURATION)
    }

    private fun initViews() {
        mLogo = findViewById(R.id.img_logo)
        mLogoTitle = findViewById(R.id.txt_namePouPay)
        mLogoSubtitle = findViewById(R.id.txt_slogan)

        animTop = AnimationUtils.loadAnimation(this, R.anim.top)
        animBottom = AnimationUtils.loadAnimation(this, R.anim.bottom)
    }

}