package diploma.pr.biovote.ui.auth

import android.graphics.Bitmap
import androidx.camera.core.CameraSelector
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import diploma.pr.biovote.data.remote.ApiClient
import diploma.pr.biovote.data.remote.model.TokenManager
import diploma.pr.biovote.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var email by rememberSaveable { mutableStateOf("") }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    Column(modifier = Modifier.padding(16.dp)) {
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        AndroidView(factory = { ctx ->
            val previewView = PreviewView(ctx)
            val cameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().apply {
                setSurfaceProvider(previewView.surfaceProvider)
            }
            val capture = ImageCapture.Builder().build()
            imageCapture = capture
            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)

            previewView
        }, modifier = Modifier
            .fillMaxWidth()
            .height(300.dp))

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                imageCapture?.takePicture(
                    ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bitmap = CameraUtils.imageProxyToBitmap(image)
                            image.close()

                            val bos = ByteArrayOutputStream()
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos)
                            val imageBytes = bos.toByteArray()

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
                                    val imageRequestBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                    val imagePart = MultipartBody.Part.createFormData("faceImage", "face.jpg", imageRequestBody)

                                    val response = ApiClient.service.loginUserByFace(emailPart, imagePart)

                                    if (response.isSuccessful && response.body() != null) {
                                        val token = response.body()?.token
                                        if (token != null) {
                                            TokenManager(context).saveToken("Bearer $token")
                                            onLoggedIn()
                                        } else {
                                            errorText = "Сервер не повернув токен"
                                        }
                                    } else {
                                        errorText = "Логін не вдався: ${response.code()}"
                                    }
                                } catch (e: Exception) {
                                    errorText = "Помилка: ${e.message}"
                                }
                            }
                        }

                        override fun onError(exception: ImageCaptureException) {
                            errorText = "Помилка камери: ${exception.message}"
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Увійти")
        }

        errorText?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}