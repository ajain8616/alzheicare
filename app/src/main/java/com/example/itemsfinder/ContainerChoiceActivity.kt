package com.example.itemsfinder

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class ContainerChoiceActivity : AppCompatActivity() {
    private lateinit var containerIntro:LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_choice)
       containerIntro=findViewById(R.id.containerIntro)
        containerIntro.playAnimation()
        Handler(Looper.getMainLooper()).postDelayed({
            containerIntro.visibility= View.VISIBLE
            val intent = Intent(this@ContainerChoiceActivity, MainActivity::class.java)
            startActivity(intent)
            finish()
        }, 2000)
    }

}
