package diploma.pr.biovote.ui.voting

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import diploma.pr.biovote.data.remote.model.responses.Poll
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun VotingDetailScreen(
    pollId: Int,
    token: String
) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val coroutineScope = rememberCoroutineScope()

    var cameraProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
    var voteSubmitted by remember { mutableStateOf(false) }
    var errorText by remember { mutableStateOf<String?>(null) }

    // Запит на дозвіл камери
    val permissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                ProcessCameraProvider.getInstance(context).also { f ->
                    f.addListener({
                        cameraProvider = f.get()
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
            ProcessCameraProvider.getInstance(context).also { f ->
                f.addListener({
                    cameraProvider = f.get()
                }, ContextCompat.getMainExecutor(context))
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Голосування #$pollId", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        cameraProvider?.let { provider ->
            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx)
                    val preview = Preview.Builder().build()
                        .apply { setSurfaceProvider(previewView.surfaceProvider) }
                    val capture = ImageCapture.Builder().build()
                    imageCapture = capture

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        capture
                    )
                    previewView
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
            )
        }

        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                val cap = imageCapture
                if (cap == null) {
                    errorText = "Камера не готова"
                    return@Button
                }
                cap.takePicture(ContextCompat.getMainExecutor(context),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bmp = CameraUtils.imageProxyToBitmap(image)
                            image.close()

                            // Ваше голосування — тут ви передаєте лише pollId та обрану відповідь
                            val voteReq = VoteRequest(
                                pollId = pollId.toLong(),
                                answerIds = listOf(/* тут ваші id варіантів */)
                            )

                            coroutineScope.launch(Dispatchers.IO) {
                                try {
                                    val resp = ApiClient.service.submitVote(
                                        "Bearer $token", voteReq
                                    )
                                    if (resp.isSuccessful) {
                                        voteSubmitted = true
                                    } else {
                                        errorText = "Не вдалося надіслати: ${resp.code()}"
                                    }
                                } catch (e: Exception) {
                                    errorText = "Помилка: ${e.message}"
                                }
                            }
                        }

                        override fun onError(ex: ImageCaptureException) {
                            errorText = "Помилка зйомки: ${ex.message}"
                        }
                    })
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Підтвердити голосування")
        }

        if (voteSubmitted) {
            Spacer(Modifier.height(8.dp))
            Text("Ваш голос успішно надіслано", color = MaterialTheme.colorScheme.primary)
        }
        errorText?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}