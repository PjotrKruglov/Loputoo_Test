package com.example.lopputest

import androidx.appcompat.app.AppCompatActivity

import android.content.Intent
import android.os.Bundle
import android.view.View

class ChoiceActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choice)
    }

    fun onStudy(view: View) {
        // Do something in response to button
        val intent = Intent(this, StudyActivity::class.java)
        startActivity(intent)
    }

    fun onTest(view: View) {
        // Do something in response to button
        val intent = Intent(this, TestActivity::class.java)
        startActivity(intent)
    }
}
