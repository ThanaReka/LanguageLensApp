package com.example.languagelens

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.languagelens.data.GeminiRepository
import com.example.languagelens.data.RetrofitInstance
import com.example.languagelens.ui.theme.LanguageLensTheme
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        val apiService = RetrofitInstance.apiService // ✅ Get Retrofit instance
        val repository = GeminiRepository(apiService) // ✅ Pass it to the repository

        setContent {
            LanguageLensScreen(cameraProviderFuture, repository)
        }
    }
}


@Composable
fun LanguageLensScreen(
    cameraProviderFuture: ListenableFuture<ProcessCameraProvider>,
    repository: GeminiRepository // 🔹 Now we will use this!
) {
    val context = LocalContext.current
    var detectedText by remember { mutableStateOf("Tap to Detect") }
    var base64Image by remember { mutableStateOf("") }
    val imageCapture = remember { mutableStateOf<ImageCapture?>(null) }
    val coroutineScope = rememberCoroutineScope() // 🔹 Needed for API call

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Transparent)
    ) {
        CameraPreview(
            cameraProviderFuture = cameraProviderFuture,
            modifier = Modifier.fillMaxSize(),
            imageCaptureState = imageCapture
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Translation Output Box
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = detectedText,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    modifier = Modifier
                        .background(Color.Black.copy(alpha = 0.6f), shape = RoundedCornerShape(8.dp))
                        .padding(16.dp)
                )
            }

            // Capture & Translate Button
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {


                Button(
                    onClick = {
                        Log.d("CameraPreview", "Capture button clicked") // 🔍 Debug log
                        imageCapture.value?.let { captureImage(context, it) { imageData ->
                            base64Image = imageData
                            coroutineScope.launch {
                                detectedText = repository.detectTextFromImage(base64Image) // 🔹 Send image to Gemini
                            }
                        } } ?: Log.e("CameraPreview", "ImageCapture is null") // Prevent null errors
                    },
                    modifier = Modifier
                        .size(80.dp)
                        .clip(CircleShape),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                ) {
                    Icon(Icons.Filled.Camera, contentDescription = "Capture Image")
                }
            }
        }
    }
}
