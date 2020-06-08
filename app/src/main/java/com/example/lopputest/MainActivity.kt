package com.example.lopputest

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.view.View

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun onStart(view: View) {
        // Do something in response to button
        val intent = Intent(this, ChoiceActivity::class.java)
        startActivity(intent)
    }
}
