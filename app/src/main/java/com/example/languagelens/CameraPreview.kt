package com.example.languagelens

import android.Manifest
import android.content.Context
import android.util.Base64
import android.util.Log
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.google.common.util.concurrent.ListenableFuture
import java.io.File
import java.util.concurrent.Executors

@Composable
fun CameraPreview(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    modifier: Modifier = Modifier,
    imageCaptureState: MutableState<ImageCapture?> // 🔹 Only keeping necessary parameters
) {
    val context = LocalContext.current
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val executor = ContextCompat.getMainExecutor(context)

    LaunchedEffect(cameraProviderFuture) {
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            val newImageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()

            imageCaptureState.value = newImageCapture // 🔹 Store reference in state

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner, cameraSelector, preview, newImageCapture
                )
            } catch (e: Exception) {
                Log.e("Camera", "Use case binding failed", e)
            }
        }, executor)
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        AndroidView({ previewView }, modifier = Modifier.fillMaxSize())
    }
}

fun captureImage(context: Context, imageCapture: ImageCapture, onImageCaptured: (String) -> Unit) {
    val file = File(context.cacheDir, "captured_image.jpg")
    val outputOptions = ImageCapture.OutputFileOptions.Builder(file).build()

    imageCapture.takePicture(outputOptions, ContextCompat.getMainExecutor(context), object :
        ImageCapture.OnImageSavedCallback {
        override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
            Log.d("CameraPreview", "Image saved to: ${file.absolutePath}") // 🔍 Debug log

            val base64Image = convertImageToBase64(file)
            Log.d("CameraPreview", "Base64 image generated: ${base64Image.take(100)}") // 🔍 Log first 100 chars

            onImageCaptured(base64Image) // ✅ Should be triggered here
        }

        override fun onError(exception: ImageCaptureException) {
            Log.e("CameraPreview", "Image capture failed: ${exception.message}", exception)
        }
    })
}

fun convertImageToBase64(file: File): String {
    return try {
        val bytes = file.readBytes()
        val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
        Log.d("CameraPreview", "Base64 conversion successful, length: ${base64String.length}") // 🔍 Debug log
        base64String
    } catch (e: Exception) {
        Log.e("Base64", "Conversion failed: ${e.message}", e)
        ""
    }
}


