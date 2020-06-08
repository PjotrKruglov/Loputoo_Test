package com.example.lopputest

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.View
import android.graphics.Bitmap
import android.os.Parcelable
import com.example.lopputest.tflite.ImageClassifier
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_result.*


class ResultActivity : AppCompatActivity() {

    private lateinit var classifier: ImageClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val intent = intent
        val bitmap = intent.getParcelableExtra<Parcelable>("BitmapImage") as Bitmap
        val name = intent.getStringExtra("item")
        classifier = ImageClassifier(getAssets())
        classifier.recognizeImage(bitmap).subscribeBy(
                onSuccess = {
                    val re = Regex("[^A-Za-z0-9 ]")
                    val value = re.replace(it.toString().substringBefore(" "), "")
                    if (value.toString().equals(name))
                        Result.text = "You are right!"
                    else
                        Result.text = "This is "+ value+ ", not "+name
                }
        )
    }

    fun onBack(view: View) {
        val intent = Intent(this, TestActivity::class.java)
        startActivity(intent)
    }
}
