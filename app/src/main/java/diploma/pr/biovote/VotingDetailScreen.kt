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
import diploma.pr.biovote.data.remote.ApiClient
import diploma.pr.biovote.data.remote.model.VoteRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun VotingDetailScreen(pollId: Int, token: String) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var voteSubmitted by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Голосування #$pollId", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder().build()
            imageCapture = capture
            val cameraSelector = androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                lifecycleOwner,
                cameraSelector,
                preview,
                capture
            )
            previewView
        }, modifier = Modifier
            .fillMaxWidth()
            .height(300.dp))

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                val fileName = "vote_face_${System.currentTimeMillis()}.jpg"
                imageCapture?.takePicture(ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = image.toBitmap()
                            image.close()
                            val bos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                            val imageBytes = bos.toByteArray()
                            val voteRequest = VoteRequest(pollId = pollId, answerIds = listOf(1)) // Replace with real answer

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val response = ApiClient.service.submitVote("Bearer $token", voteRequest)
                                    if (response.isSuccessful) {
                                        voteSubmitted = true
                                    } else {
                                        errorText = "Не вдалося надіслати голос"
                                    }
                                } catch (e: Exception) {
                                    errorText = "Помилка: ${e.message}"
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            errorText = "Camera error"
                        }
                    })
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

private fun Any.submitVote(token: String, voteRequest: VoteRequest): Any {
    TODO("Not yet implemented")
}
