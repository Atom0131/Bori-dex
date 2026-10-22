package com.example.pokedex.camera

import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    onFrame: (android.graphics.Bitmap) -> Unit,
    controller: CameraXController? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    
    val previewView = remember { 
        PreviewView(context).apply {
            scaleType = PreviewView.ScaleType.FILL_CENTER
        } 
    }
    val cameraExecutor = remember { Executors.newSingleThreadExecutor() }

    DisposableEffect(Unit) {
        onDispose {
            controller?.setTorch(false)
            cameraExecutor.shutdown()
        }
    }

    LaunchedEffect(Unit) {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            
            val imageAnalyzer = ImageAnalysis.Builder()
                .setTargetAspectRatio(androidx.camera.core.AspectRatio.RATIO_4_3)
                .setTargetRotation(previewView.display.rotation)
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, PokedexImageAnalyzer(onFrame))
                }
                
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                val camera = cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageAnalyzer
                )
                
                // Attach camera & metering to controller
                controller?.attachCamera(camera, previewView.meteringPointFactory)
                
                // Observe live zoom state from CameraX
                camera.cameraInfo.zoomState.observe(lifecycleOwner) { zoomState ->
                    controller?.updateZoomState(
                        zoomState.zoomRatio,
                        zoomState.minZoomRatio,
                        zoomState.maxZoomRatio
                    )
                }
                
                // Observe live torch state from CameraX
                camera.cameraInfo.torchState.observe(lifecycleOwner) { torchState ->
                    controller?.updateTorchState(torchState)
                }

                // Fallback direct touch listener on PreviewView
                previewView.setOnTouchListener { view, event ->
                    if (event.action == android.view.MotionEvent.ACTION_UP) {
                        controller?.startFocusAndMetering(
                            androidx.compose.ui.geometry.Offset(event.x, event.y),
                            context
                        )
                        view.performClick()
                    }
                    true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }
    
    AndroidView(
        factory = { previewView },
        modifier = modifier.fillMaxSize()
    )
}
