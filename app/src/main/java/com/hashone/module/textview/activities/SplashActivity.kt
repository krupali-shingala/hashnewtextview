package com.hashone.module.textview.activities

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hashone.module.textview.databinding.ActivitySplashBinding

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        handler.postDelayed(runnable, 1500L)
    }

    private val handler = Handler()
    private val runnable = Runnable {
        nextScreen()
    }

    private fun nextScreen() {
        val newIntent = Intent(this, EditActivity::class.java)
        val bundle = intent.extras
        bundle?.let { newIntent.putExtras(it) }
        startActivity(newIntent)
        overridePendingTransition(0,0)
        finish()
    }
}