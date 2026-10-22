package com.example.pokedex.camera

import android.content.Context
import androidx.camera.core.Camera
import androidx.camera.core.CameraControl
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.MeteringPointFactory
import androidx.camera.core.TorchState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Offset
import androidx.core.content.ContextCompat
import java.util.concurrent.TimeUnit

enum class FocusState {
    IDLE,
    FOCUSING,
    SUCCESS,
    FAILED
}

/**
 * Observable controller that exposes CameraX hardware capabilities
 * (Zoom, Torch/Flashlight, Tap-to-Focus) directly to Jetpack Compose UI.
 */
class CameraXController {
    var camera: Camera? = null
    var meteringPointFactory: MeteringPointFactory? = null

    // Zoom state
    var zoomRatio by mutableFloatStateOf(1.0f)
        private set
    var minZoomRatio by mutableFloatStateOf(1.0f)
        private set
    var maxZoomRatio by mutableFloatStateOf(8.0f)
        private set

    // Torch / Flashlight state
    var isTorchOn by mutableStateOf(false)
        private set
    var hasFlashUnit by mutableStateOf(false)
        internal set

    // Tap-to-Focus state
    var focusPoint by mutableStateOf<Offset?>(null)
        private set
    var focusState by mutableStateOf(FocusState.IDLE)
        private set

    /**
     * Updates internal references when CameraX binds the active camera.
     */
    fun attachCamera(activeCamera: Camera, factory: MeteringPointFactory) {
        this.camera = activeCamera
        this.meteringPointFactory = factory
        this.hasFlashUnit = activeCamera.cameraInfo.hasFlashUnit()
    }

    /**
     * Updates zoom limits from CameraX ZoomState.
     */
    fun updateZoomState(current: Float, min: Float, max: Float) {
        this.zoomRatio = current
        this.minZoomRatio = min
        // Cap UI max zoom to 8.0x for stable ergonomics, even if sensor supports 30x digital
        this.maxZoomRatio = minOf(max, 8.0f)
    }

    /**
     * Updates torch state from CameraX TorchState.
     */
    fun updateTorchState(state: Int) {
        this.isTorchOn = (state == TorchState.ON)
    }

    /**
     * Sets optical/digital zoom ratio within supported hardware bounds.
     */
    fun setZoom(ratio: Float) {
        val clamped = ratio.coerceIn(minZoomRatio, maxZoomRatio)
        zoomRatio = clamped
        camera?.cameraControl?.setZoomRatio(clamped)
    }

    /**
     * Smooth incremental zoom (used for pinch-to-zoom gestures).
     */
    fun applyZoomDelta(delta: Float) {
        setZoom(zoomRatio * delta)
    }

    /**
     * Toggles flashlight on or off.
     */
    fun toggleTorch() {
        if (!hasFlashUnit) return
        val target = !isTorchOn
        setTorch(target)
    }

    /**
     * Explicitly sets torch state (e.g. turning off upon scan lock or navigation).
     */
    fun setTorch(enabled: Boolean) {
        if (!hasFlashUnit) return
        isTorchOn = enabled
        camera?.cameraControl?.enableTorch(enabled)
    }

    /**
     * Triggers autofocus (AF) and autoexposure (AE) at the tapped screen offset.
     */
    fun startFocusAndMetering(offset: Offset, context: Context) {
        val factory = meteringPointFactory ?: return
        val cam = camera ?: return

        val point = factory.createPoint(offset.x, offset.y)
        val action = FocusMeteringAction.Builder(
            point,
            FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE
        ).setAutoCancelDuration(3, TimeUnit.SECONDS).build()

        focusPoint = offset
        focusState = FocusState.FOCUSING

        val future = cam.cameraControl.startFocusAndMetering(action)
        future.addListener({
            try {
                val result = future.get()
                focusState = if (result.isFocusSuccessful) FocusState.SUCCESS else FocusState.FAILED
            } catch (e: Exception) {
                focusState = FocusState.IDLE
            }
        }, ContextCompat.getMainExecutor(context))
    }

    /**
     * Clears visual focus indicator after animation completes.
     */
    fun clearFocusIndicator() {
        focusPoint = null
        focusState = FocusState.IDLE
    }

    /**
     * Resets controller and shuts off hardware torch.
     */
    fun reset() {
        setTorch(false)
        clearFocusIndicator()
        setZoom(1.0f)
    }
}
