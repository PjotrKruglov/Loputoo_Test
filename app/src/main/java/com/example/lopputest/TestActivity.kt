package com.example.lopputest

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity

import android.os.Bundle
import android.view.View
import android.provider.MediaStore
import com.example.lopputest.tflite.Keys
import kotlinx.android.synthetic.main.activity_test.*
import java.io.BufferedReader
import java.io.InputStreamReader
import kotlin.random.Random


class TestActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        val bufReader = BufferedReader(InputStreamReader(assets.open("labels.txt")))
        val values = ArrayList<String>()
        var line = bufReader.readLine()
        while (line != null) {
            values.add(line)
            line = bufReader.readLine()
        }
        bufReader.close()
        val random = Random.nextInt(0, values.size-1)
        objectname.text = values[random]
    }

    fun onSelect(view: View) {
        // Do something in response to button
        dispatchTakePictureIntent()
    }

    fun onBack(view: View) {
        // Do something in response to button
        val intent = Intent(this, ChoiceActivity::class.java)
        startActivity(intent)
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }

    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            var imageBitmap = data?.extras?.get("data") as Bitmap
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, Keys.INPUT_SIZE, Keys.INPUT_SIZE, false)
            val intent = Intent(this, ResultActivity::class.java)
            intent.putExtra("BitmapImage", imageBitmap)
            intent.putExtra("item", objectname.text.toString())
            startActivity(intent)
        }
    }

}
