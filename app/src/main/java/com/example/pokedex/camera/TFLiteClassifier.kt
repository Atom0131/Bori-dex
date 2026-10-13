package com.example.pokedex.camera

import android.content.Context
import android.graphics.Bitmap
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.common.FileUtil
import java.nio.ByteBuffer
import java.nio.ByteOrder

class TFLiteClassifier(context: Context) {
    private val interpreter: Interpreter
    private val labels: List<String>
    private val inputBuffer: ByteBuffer
    private val outputBuffer: ByteBuffer
    private val intValues = IntArray(224 * 224)
    
    init {
        val modelBuffer = FileUtil.loadMappedFile(context, "model.tflite")
        val options = Interpreter.Options().setNumThreads(4)
        interpreter = Interpreter(modelBuffer, options)
        labels = FileUtil.loadLabels(context, "labels.txt")
            .map { it.trim().removePrefix("\uFEFF") }
            .filter { it.isNotEmpty() }
            
        // Allocate direct native buffers matching the model's exact tensor shapes:
        // Input: [1, 224, 224, 3] FLOAT32 (602,112 bytes)
        inputBuffer = ByteBuffer.allocateDirect(1 * 224 * 224 * 3 * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        // Output: [1, 1025] FLOAT32 (4,100 bytes)
        outputBuffer = ByteBuffer.allocateDirect(1 * labels.size * 4).apply {
            order(ByteOrder.nativeOrder())
        }
        
        val inTensor = interpreter.getInputTensor(0)
        val outTensor = interpreter.getOutputTensor(0)
        android.util.Log.d("TFLiteClassifier", "Model init -> In: shape=${inTensor.shape().contentToString()}, type=${inTensor.dataType()} | Out: shape=${outTensor.shape().contentToString()}, type=${outTensor.dataType()} | labels=${labels.size}")
    }

    fun getLabels(): List<String> = labels

    fun getProbabilities(bitmap: Bitmap): FloatArray {
        val scaledBitmap = if (bitmap.width == 224 && bitmap.height == 224) {
            bitmap
        } else {
            Bitmap.createScaledBitmap(bitmap, 224, 224, true)
        }
        
        scaledBitmap.getPixels(intValues, 0, 224, 0, 0, 224, 224)
        
        inputBuffer.rewind()
        var pixel = 0
        for (i in 0 until 224) {
            for (j in 0 until 224) {
                val v = intValues[pixel++]
                // Extract R, G, B channels as Float32 (0.0f .. 255.0f)
                // The model's internal Rescaling layer converts to [-1, 1]
                inputBuffer.putFloat(((v shr 16) and 0xFF).toFloat())
                inputBuffer.putFloat(((v shr 8) and 0xFF).toFloat())
                inputBuffer.putFloat((v and 0xFF).toFloat())
            }
        }
        
        inputBuffer.rewind()
        outputBuffer.rewind()
        interpreter.run(inputBuffer, outputBuffer)
        outputBuffer.rewind()
        val floatBuf = outputBuffer.asFloatBuffer()
        val probabilities = FloatArray(labels.size)
        floatBuf.get(probabilities)
        return probabilities
    }

    fun classify(bitmap: Bitmap): String {
        val probabilities = getProbabilities(bitmap)

        val sumVal = probabilities.sum()
        val pMax = probabilities.maxOrNull() ?: 0f
        val sampleR = (intValues[0] shr 16) and 0xFF
        val sampleG = (intValues[0] shr 8) and 0xFF
        val sampleB = intValues[0] and 0xFF
        android.util.Log.d("TFLiteClassifier", "Input: rgb=($sampleR,$sampleG,$sampleB) | Output: sum=$sumVal, max=$pMax")

        var maxIdx = 0
        var maxVal = -1f
        for (i in probabilities.indices) {
            if (probabilities[i] > maxVal) {
                maxVal = probabilities[i]
                maxIdx = i
            }
        }
        
        // Log top 3 predictions
        val top3 = probabilities.withIndex().sortedByDescending { it.value }.take(3)
        var logStr = "Predictions: "
        for (p in top3) {
            logStr += "${labels[p.index]} (${"%.4f".format(p.value)}), "
        }
        android.util.Log.d("TFLiteClassifier", logStr)
        
        // With 1025 classes, random chance = 1/1025 = 0.00098 (0.098%).
        // A threshold of 0.15 (15%) is >150x above random baseline, easily
        // catching genuine 80%+ detections while rejecting room background clutter.
        if (maxVal < 0.15f) {
            return "UNKNOWN"
        }
        
        return labels[maxIdx]
    }
    
    fun release() {
        interpreter.close()
    }
}
