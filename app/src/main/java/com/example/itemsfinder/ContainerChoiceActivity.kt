package com.example.itemsfinder

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.airbnb.lottie.LottieAnimationView

class ContainerChoiceActivity : AppCompatActivity() {
    private lateinit var containerIntro:LottieAnimationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_container_choice)
       containerIntro=findViewById(R.id.containerIntro)
        containerIntro.playAnimation()
    }

}