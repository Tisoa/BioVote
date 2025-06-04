package diploma.pr.biovote

import android.graphics.Bitmap
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.VoteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import diploma.pr.biovote.utils.CameraUtils

@Composable
fun VotingDetailScreen(pollId: Int, token: String) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val coroutineScope = rememberCoroutineScope()

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var voteSubmitted by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener({
                    cameraProvider = future.get()
                }, ContextCompat.getMainExecutor(context))
            }
        } else {
            errorText = "Дозвіл на камеру не надано"
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        } else {
            ProcessCameraProvider.getInstance(context).also { future ->
                future.addListener({
                    cameraProvider = future.get()
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Голосування #$pollId", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        cameraProvider?.let { provider ->
            AndroidView(factory = { ctx ->
                val previewView = PreviewView(ctx)
                val preview = Preview.Builder().build().apply {
                    setSurfaceProvider(previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder().build()
                imageCapture = capture

                val selector = CameraSelector.DEFAULT_FRONT_CAMERA
                provider.unbindAll()
                provider.bindToLifecycle(lifecycleOwner, selector, preview, capture)

                previewView
            }, modifier = Modifier
                .fillMaxWidth()
                .height(300.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val capture = imageCapture
                if (capture == null) {
                    errorText = "Камера не готова"
                    return@Button
                }

                capture.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = CameraUtils.imageProxyToBitmap(image)
                            image.close()

                            val bos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                            val imageBytes = bos.toByteArray()
                            val voteRequest = VoteRequest(
                                pollId = pollId,
                                answerIds = listOf(1) // Replace with actual answer logic
                            )

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val response = ApiClient.service.submitVote("Bearer $token", voteRequest)
                                    if (response.isSuccessful) {
                                        voteSubmitted = true
                                    } else {
                                        errorText = "Не вдалося надіслати голос: ${'$'}{response.code()}"
                                    }
                                } catch (e: Exception) {
                                    errorText = "Помилка: ${'$'}{e.message}"
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            errorText = "Помилка зйомки: ${'$'}{exception.message}"
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Підтвердити голосування")
        }

        if (voteSubmitted) {
            Text("Ваш голос успішно надіслано", color = MaterialTheme.colorScheme.primary)
        }

        errorText?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}