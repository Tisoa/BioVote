package diploma.pr.biovote

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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import diploma.pr.biovote.ui.auth.AuthViewModel
import diploma.pr.biovote.ui.auth.UiState
import diploma.pr.biovote.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@Composable
fun RegistrationScreen(
    onSuccess: () -> Unit
) {
    /* ---------- state ---------- */
    val ctx          = LocalContext.current
    val vm : AuthViewModel = viewModel()
    val ui           by vm.state.collectAsState()

    var email     by remember { mutableStateOf("") }
    var fullName  by remember { mutableStateOf("") }
    var imageCap  by remember { mutableStateOf<ImageCapture?>(null) }
    var provider  by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var err       by remember { mutableStateOf<String?>(null) }

    /* ---------- permission ---------- */
    val camPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) err = "Не надано дозвіл на камеру"
        else ProcessCameraProvider.getInstance(ctx).also {
            it.addListener({ provider = it.get() }, ContextCompat.getMainExecutor(ctx))
        }
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) camPermLauncher.launch(Manifest.permission.CAMERA)
        else ProcessCameraProvider.getInstance(ctx).also {
            it.addListener({ provider = it.get() }, ContextCompat.getMainExecutor(ctx))
        }
    }

    /* ---------- UI ---------- */
    Column(
        Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Реєстрація", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("E-mail") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fullName, onValueChange = { fullName = it },
            label = { Text("Повне ім’я") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        /* камера */
        provider?.let { pr ->
            AndroidView(
                factory = {
                    val pv = PreviewView(it)
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(pv.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder().build()
                    imageCap = capture

                    pr.unbindAll()
                    pr.bindToLifecycle(
                        it as androidx.lifecycle.LifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview, capture
                    )
                    pv
                },
                modifier = Modifier.fillMaxWidth().height(280.dp)
            )
        }

        /* кнопка */
        Button(
            onClick = {
                val cap = imageCap ?: return@Button.also { err = "Камера не готова" }
                cap.takePicture(
                    ContextCompat.getMainExecutor(ctx),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(img: ImageProxy) {
                            val bmp = CameraUtils.imageProxyToBitmap(img)
                            img.close()

                            /* multipart */
                            val jpg = ByteArrayOutputStream().apply {
                                bmp.compress(Bitmap.CompressFormat.JPEG, 90, this)
                            }.toByteArray()
                            val face = MultipartBody.Part.createFormData(
                                "faceImage", "face.jpg",
                                jpg.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            )
                            val mail = email.toRequestBody("text/plain".toMediaTypeOrNull())
                            val name = fullName.toRequestBody("text/plain".toMediaTypeOrNull())

                            vm.register(mail, name, face)
                        }
                        override fun onError(e: ImageCaptureException) {
                            err = "Фото не зроблено: ${e.message}"
                        }
                    }
                )
            },
            enabled = ui !is UiState.Loading,
            modifier = Modifier.fillMaxWidth()
        ) { Text("Зареєструватися") }

        /* статус */
        when (ui) {
            is UiState.Error   -> err = (ui as UiState.Error).msg
            is UiState.Success -> LaunchedEffect(Unit) { onSuccess() }
            else               -> {}
        }
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}