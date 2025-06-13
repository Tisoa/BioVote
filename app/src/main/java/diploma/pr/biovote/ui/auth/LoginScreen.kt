package diploma.pr.biovote.ui.auth

import android.Manifest
import android.graphics.Bitmap
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.repository.AuthRepository
import diploma.pr.biovote.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/* -------- маленька утиліта -------- */
private suspend fun <T> ListenableFuture<T>.await(): T =
    suspendCancellableCoroutine { cont ->
        addListener({
            try { cont.resume(get()) } catch (e: Exception) { cont.resumeWithException(e) }
        }, Runnable::run)
    }

/* -------- Composable -------- */
@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {

    val ctx           = LocalContext.current
    val lifecycle     = ctx as LifecycleOwner
    val scope         = rememberCoroutineScope()
    val repo          = remember { AuthRepository() }

    var email      by rememberSaveable { mutableStateOf("") }
    var errTxt     by remember { mutableStateOf<String?>(null) }
    var imageCap   by remember { mutableStateOf<ImageCapture?>(null) }
    val previewRef = remember { PreviewView(ctx) }

    /* ---------- permission ---------- */
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { if (!it) errTxt = "Не надано дозвіл на камеру" }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) permLauncher.launch(Manifest.permission.CAMERA)
        else {
            try {
                val provider = ProcessCameraProvider.getInstance(ctx).await()
                val preview  = Preview.Builder().build().apply {
                    setSurfaceProvider(previewRef.surfaceProvider)
                }
                val capture  = ImageCapture.Builder().build()
                imageCap = capture
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycle,
                    CameraSelector.DEFAULT_FRONT_CAMERA,
                    preview, capture
                )
            } catch (e: Exception) {
                errTxt = "Помилка камери: ${e.message}"
            }
        }
    }

    /* ---------- UI ---------- */
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))

        AndroidView(
            factory  = { previewRef },
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp)
        )

        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                if (email.isBlank()) { errTxt = "Введіть email"; return@Button }
                val cap = imageCap ?: return@Button.also { errTxt = "Камера не готова" }

                cap.takePicture(
                    ContextCompat.getMainExecutor(ctx),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(img: ImageProxy) {
                            val bmp = CameraUtils.imageProxyToBitmap(img).also { img.close() }

                            /* --- Multipart --- */
                            val jpg = ByteArrayOutputStream()
                                .apply { bmp.compress(Bitmap.CompressFormat.JPEG, 90, this) }
                                .toByteArray()

                            scope.launch(Dispatchers.IO) {
                                try {
                                    val facePart  = MultipartBody.Part.createFormData(
                                        "faceImage", "face.jpg",
                                        jpg.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                    )
                                    val resp = repo.login(email, facePart)

                                    if (resp.isSuccessful) {
                                        val body = resp.body()
                                        if (body?.success == true && body.message.isNotBlank()) {
                                            TokenManager(ctx).saveToken(body.message)  //  ⬅️  JWT
                                            launch(Dispatchers.Main) { onLoggedIn() }
                                        } else {
                                            errTxt = "Сервер не надав токен"
                                        }
                                    } else {
                                        errTxt = "HTTP ${resp.code()}"
                                    }
                                } catch (e: Exception) {
                                    errTxt = "Помилка: ${e.localizedMessage ?: e.message}"
                                }
                            }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            errTxt = "Помилка зйомки: ${exc.message}"
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) { Text("Увійти") }

        errTxt?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}