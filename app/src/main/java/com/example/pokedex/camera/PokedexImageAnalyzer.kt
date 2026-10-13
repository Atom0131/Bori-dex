package com.example.pokedex.camera

import android.graphics.Bitmap
import android.graphics.Matrix
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy

/**
 * Captures a single, upright frame once the camera has had ~1 s to settle focus/exposure, then
 * hands it to [onFrame]. Inference happens in the ViewModel so the "Who's That Pokémon?"
 * animation can run while the model thinks.
 */
class PokedexImageAnalyzer(
    private val onFrame: (Bitmap) -> Unit
) : ImageAnalysis.Analyzer {

    private var frameCount = 0

    override fun analyze(image: ImageProxy) {
        // use{} closes the frame immediately so CameraX is never stalled waiting on inference.
        image.use {
            frameCount++
            // Wait ~15 frames for auto-focus/exposure to settle initially
            if (frameCount < 15) return
            
            // Only emit 1 out of every 4 frames (so we don't overwhelm the classifier)
            if (frameCount % 4 != 0) return

            val raw = it.toBitmap()
            val rotation = it.imageInfo.rotationDegrees
            val upright = if (rotation != 0) {
                val matrix = Matrix().apply { postRotate(rotation.toFloat()) }
                Bitmap.createBitmap(raw, 0, 0, raw.width, raw.height, matrix, true)
            } else raw
            
            // Center-crop to 1:1 square matching the camera targeting reticle
            val minDim = minOf(upright.width, upright.height)
            val cropX = (upright.width - minDim) / 2
            val cropY = (upright.height - minDim) / 2
            val centeredSquare = Bitmap.createBitmap(upright, cropX, cropY, minDim, minDim)
            onFrame(centeredSquare)
        }
    }
}
