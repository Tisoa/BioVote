package diploma.pr.biovote

/* ---------- Android / Compose ---------- */
import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel

/* ---------- project ---------- */
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.ui.auth.AuthViewModel
import diploma.pr.biovote.ui.auth.UiState
import diploma.pr.biovote.utils.CameraUtils

/* ---------- network ---------- */
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

/* ---------- utils ---------- */
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream

@Composable
fun RegistrationScreen(
    onSuccess: () -> Unit          // колбек навігації ― коли все ок
) {
    /* ---------- DI → ViewModel з TokenManager ---------- */
    val ctx = LocalContext.current
    val vm: AuthViewModel = viewModel(
        factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(c: Class<T>): T =
                AuthViewModel(TokenManager(ctx)) as T
        }
    )
    val ui by vm.state.collectAsState()

    /* ---------- local-state ---------- */
    var email      by remember { mutableStateOf("") }
    var fullName   by remember { mutableStateOf("") }
    var err        by remember { mutableStateOf<String?>(null) }

    var imageCap   by remember { mutableStateOf<ImageCapture?>(null) }
    var camProvider by remember { mutableStateOf<ProcessCameraProvider?>(null) }

    /* ---------- permission flow ---------- */
    val camPermLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) err = "Потрібен дозвіл на камеру"
        else ProcessCameraProvider.getInstance(ctx).also { f ->
            f.addListener({ camProvider = f.get() }, ContextCompat.getMainExecutor(ctx))
        }
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) camPermLauncher.launch(Manifest.permission.CAMERA)
        else ProcessCameraProvider.getInstance(ctx).also { f ->
            f.addListener({ camProvider = f.get() }, ContextCompat.getMainExecutor(ctx))
        }
    }

    /* ---------- UI ---------- */
    val scroll = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scroll)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Реєстрація", style = MaterialTheme.typography.headlineSmall)

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Повне ім’я") },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        /* ---------- Camera preview (внизу форми) ---------- */
        camProvider?.let { provider ->
            AndroidView(
                factory = { context ->
                    val pv = PreviewView(context)
                    val preview = Preview.Builder().build().apply {
                        setSurfaceProvider(pv.surfaceProvider)
                    }
                    val capture = ImageCapture.Builder().build()
                    imageCap = capture

                    provider.unbindAll()
                    provider.bindToLifecycle(
                        context as androidx.lifecycle.LifecycleOwner,
                        CameraSelector.DEFAULT_FRONT_CAMERA,
                        preview,
                        capture
                    )
                    pv
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            )
        }

        /* ---------- submit ---------- */
        val scope = rememberCoroutineScope()
        Button(
            enabled = ui !is UiState.Loading,
            modifier = Modifier.fillMaxWidth(),
            onClick = {
                /* валідація */
                if (email.isBlank() || fullName.isBlank()) {
                    err = "Заповніть e-mail та ім’я"
                    return@Button
                }
                val cap = imageCap ?: return@Button.also {
                    err = "Камера ще не готова"
                }

                /* робимо фото */
                cap.takePicture(
                    ContextCompat.getMainExecutor(ctx),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            /* bitmap → jpeg → MultipartBody.Part */
                            val bmp = CameraUtils.imageProxyToBitmap(image)
                            image.close()

                            val jpgBytes = ByteArrayOutputStream().apply {
                                bmp.compress(Bitmap.CompressFormat.JPEG, 90, this)
                            }.toByteArray()

                            val facePart = MultipartBody.Part.createFormData(
                                "faceImage",
                                "face.jpg",
                                jpgBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            )

                            scope.launch {
                                /* AuthViewModel: реєструємося + логінимося */
                                vm.registerAndLogin(
                                    email.trim(),
                                    fullName.trim(),
                                    facePart
                                )
                            }
                        }

                        override fun onError(exc: ImageCaptureException) {
                            err = "Не вдалося зробити фото: ${exc.message}"
                        }
                    }
                )
            }
        ) { Text(if (ui is UiState.Loading) "Зачекайте…" else "Зареєструватися") }

        /* ---------- feedback ---------- */
        when (ui) {
            is UiState.Success -> LaunchedEffect(Unit) { onSuccess() }
            is UiState.Error   -> err = (ui as UiState.Error).msg
            else               -> {}
        }
        err?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}