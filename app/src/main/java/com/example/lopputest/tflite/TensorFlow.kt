package com.example.lopputest.tflite

import android.graphics.RectF
class TensorFlow(val id: String?, val title: String?, val confidence: Float?, private var location: RectF?) {
    override fun toString(): String {
        var resultString = ""
        if (title != null) resultString += title + ": "
        if (confidence != null) resultString += confidence.toString()
        return resultString
    }
}

object Keys {
        const val MODEL_PATH = "mobilenet_quant_v1_224.lite"
    const val LABEL_PATH = "labels.txt"
    const val INPUT_NAME = "input"
    const val OUTPUT_NAME = "output"
    const val IMAGE_MEAN: Float = 128f
    const val IMAGE_STD: Float = 128f
    private val PROBABILITY_MEAN = 0.0f

    private val PROBABILITY_STD = 1.0f
    const val INPUT_SIZE = 224
    const val MAX_RESULTS = 1
    const val DIM_BATCH_SIZE = 1
    const val DIM_PIXEL_SIZE = 3
    const val DIM_IMG_SIZE_X = 224
    const val DIM_IMG_SIZE_Y = 224
}