package com.faikan.univcutoff

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity


class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        Handler(Looper.getMainLooper()).postDelayed({
            val intent = Intent(
                this@SplashActivity,
                HomeActivity::class.java
            )
            startActivity(intent)
            finish()
        }, SPLASH_DURATION.toLong())
    }

    companion object {
        private const val SPLASH_DURATION = 2000 // 2 seconds
    }
}
