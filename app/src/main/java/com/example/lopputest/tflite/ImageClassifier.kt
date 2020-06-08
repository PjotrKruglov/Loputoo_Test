@file:Suppress("SameParameterValue")

package com.example.lopputest.tflite


import android.content.res.AssetManager
import android.graphics.Bitmap
import com.example.lopputest.tflite.Keys.DIM_BATCH_SIZE
import com.example.lopputest.tflite.Keys.DIM_IMG_SIZE_X
import com.example.lopputest.tflite.Keys.DIM_IMG_SIZE_Y
import com.example.lopputest.tflite.Keys.DIM_PIXEL_SIZE
import com.example.lopputest.tflite.Keys.IMAGE_MEAN
import com.example.lopputest.tflite.Keys.IMAGE_STD
import com.example.lopputest.tflite.Keys.INPUT_SIZE
import com.example.lopputest.tflite.Keys.LABEL_PATH
import com.example.lopputest.tflite.Keys.MAX_RESULTS
import com.example.lopputest.tflite.Keys.MODEL_PATH
import io.reactivex.rxjava3.core.Single
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.io.FileInputStream
import java.io.IOException
import java.io.InputStreamReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList


@Suppress("NULLABILITY_MISMATCH_BASED_ON_JAVA_ANNOTATIONS")
class ImageClassifier constructor(private val assetManager: AssetManager) { // https://github.com/teresawu/random/tree/master/tensorflow

    private var interpreter: Interpreter? = null
    private var labelProb: Array<FloatArray>
    private val labels = Vector<String>()
    private val intValues by lazy { IntArray(INPUT_SIZE * INPUT_SIZE) }
    private val imgData: ByteBuffer

    init {
        try {
            val br = BufferedReader(InputStreamReader(assetManager.open(LABEL_PATH)))
            while (true) {
                val line = br.readLine() ?: break
                labels.add(line)
            }
            br.close()
        } catch (e: IOException) {
            throw RuntimeException("Problem reading label file!", e)
        }
        labelProb = Array(1) { FloatArray(labels.size) }
        imgData = ByteBuffer.allocateDirect(4*DIM_BATCH_SIZE * DIM_IMG_SIZE_X * DIM_IMG_SIZE_Y * DIM_PIXEL_SIZE)
        imgData.order(ByteOrder.nativeOrder())
        try {
            interpreter = Interpreter(loadModelFile(assetManager, MODEL_PATH))
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    private fun convertBitmapToByteBuffer(bitmap: Bitmap) {
        imgData.rewind()
        bitmap.getPixels(intValues, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
        var pixel = 0
        for (i in 0 until DIM_IMG_SIZE_X) {
            for (j in 0 until DIM_IMG_SIZE_Y) {
                val value = intValues!![pixel++]
                imgData!!.put((value shr 16 and 0xFF).toByte())
                imgData!!.put((value shr 8 and 0xFF).toByte())
                imgData!!.put((value and 0xFF).toByte())
            }
        }
    }

    private fun loadModelFile(assets: AssetManager, modelFilename: String): MappedByteBuffer {
        val fileDescriptor = assets.openFd(modelFilename)
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel
        val startOffset = fileDescriptor.startOffset
        val declaredLength = fileDescriptor.declaredLength
        return fileChannel.map(FileChannel.MapMode.READ_ONLY, startOffset, declaredLength)
    }

    fun recognizeImage(bitmap: Bitmap): Single<List<TensorFlow>> {
        return Single.just(bitmap).flatMap {
            convertBitmapToByteBuffer(it)
            interpreter?.run(imgData, labelProb)
            val pq = PriorityQueue<TensorFlow>(4,
                    Comparator<TensorFlow> { lhs, rhs ->
                        java.lang.Float.compare(rhs.confidence!!, lhs.confidence!!)
                    })
            for (i in labels.indices) {
                pq.add(TensorFlow("" + i, if (labels.size > i) labels[i] else "unknown", labelProb[0][i].toFloat(), null))
            }
            val recognitions = ArrayList<TensorFlow>()
            val recognitionsSize = Math.min(pq.size, MAX_RESULTS)
            for (i in 0 until recognitionsSize) recognitions.add(pq.poll())
            return@flatMap Single.just(recognitions)
        }
    }

    fun close() {
        interpreter?.close()
    }
}