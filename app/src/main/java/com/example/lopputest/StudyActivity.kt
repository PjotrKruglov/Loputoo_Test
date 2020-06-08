package com.example.lopputest


import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.lopputest.tflite.ImageClassifier
import com.example.lopputest.tflite.Keys.INPUT_SIZE
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.android.synthetic.main.activity_study.*
import java.io.FileNotFoundException

class StudyActivity : AppCompatActivity() { // https://github.com/teresawu/random/tree/master/tensorflow // https://github.com/tensorflow/examples/blob/master/lite/examples/image_classification/android/EXPLORE_THE_CODE.md

    private val REQUEST_CODE_PERMISSIONS = 101
    private val REQUIRED_PERMISSIONS = arrayOf("android.permission.CAMERA")

    private val CHOOSE_IMAGE = 52
    private lateinit var photoImage: Bitmap
    private lateinit var classifier: ImageClassifier

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_study)
        classifier = ImageClassifier(getAssets())
        if (allPermissionsGranted()) {
            Valik.setOnClickListener {
                choosePicture()
            }
        } else {
            ActivityCompat.requestPermissions(this, REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS);
        }
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<String>,
            grantResults: IntArray
    ) {

        if (requestCode == REQUEST_CODE_PERMISSIONS) {
            if (allPermissionsGranted()) {


            } else {
                Toast.makeText(this, "Permissions not granted by the user.", Toast.LENGTH_SHORT)
                        .show()
                finish()
            }
        }
    }

    private fun allPermissionsGranted(): Boolean {

        for (permission in REQUIRED_PERMISSIONS) {
            if (ContextCompat.checkSelfPermission(
                            this,
                            permission
                    ) != PackageManager.PERMISSION_GRANTED
            ) {
                return false
            }
        }
        return true
    }

    private fun choosePicture() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, CHOOSE_IMAGE)
    }

    @SuppressLint("MissingSuperCall", "SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == CHOOSE_IMAGE && resultCode == Activity.RESULT_OK)
            try {
                val stream = data?.getData()?.let { contentResolver?.openInputStream(it) }
                if (::photoImage.isInitialized) photoImage.recycle()
                photoImage = BitmapFactory.decodeStream(stream)
                photoImage = Bitmap.createScaledBitmap(photoImage, INPUT_SIZE, INPUT_SIZE, false)
                Picture.setImageBitmap(photoImage)
                classifier.recognizeImage(photoImage).subscribeBy(
                        onSuccess = {
                            val re = Regex("[^A-Za-z0-9 ]")
                            val values = re.replace(it.toString(), "")
                            val arr = values.split(" ")
                            val prediction = """${arr[1].substring(1, 3)}.${arr[1].substring(3, 5)}"""
                            objectNameStudy.text = """${arr[0]} : ${prediction} %"""
                        }
                )
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            var imageBitmap = data?.extras?.get("data") as Bitmap
            imageBitmap = Bitmap.createScaledBitmap(imageBitmap, INPUT_SIZE, INPUT_SIZE, false)
            Picture.setImageBitmap(imageBitmap)
            classifier.recognizeImage(imageBitmap).subscribeBy(
                    onSuccess = {
                        val re = Regex("[^A-Za-z0-9 ]")
                        val values = re.replace(it.toString(), "")
                        val arr = values.split(" ")
                        val prediction = """${arr[1].substring(1, 3)}.${arr[1].substring(3, 5)}"""
                        objectNameStudy.text = """${arr[0]} : ${prediction} %"""
                        //objectNameStudy.text = """${arr[0]} : ${prediction} %"""
                    }
            )
        }
    }

    fun onBack(view: View) {
        // Do something in response to button
        val intent = Intent(this, ChoiceActivity::class.java)
        startActivity(intent)
    }

    fun onSelect(view: View) {
        // Do something in response to button
        dispatchTakePictureIntent()
    }

    val REQUEST_IMAGE_CAPTURE = 1

    private fun dispatchTakePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.resolveActivity(packageManager)?.also {
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE)
            }
        }
    }
}
