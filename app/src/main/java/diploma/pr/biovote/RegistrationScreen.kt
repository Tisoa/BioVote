// app/src/main/java/diploma/pr/biovote/ui/auth/RegistrationScreen.kt
package diploma.pr.biovote.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import diploma.pr.biovote.utils.CameraUtils
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegistrationScreen(onSuccess: () -> Unit) {
    val ctx = LocalContext.current
    val vm: AuthViewModel = hiltViewModel()
    val uiState by vm.state.collectAsState()

    var email    by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var errorMsg by remember { mutableStateOf<String?>(null) }

    var camProv by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            errorMsg = "Нема дозволу на камеру"
        } else {
            ProcessCameraProvider.getInstance(ctx).also { f ->
                f.addListener({ camProv = f.get() }, ContextCompat.getMainExecutor(ctx))
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) permLauncher.launch(Manifest.permission.CAMERA)
        else ProcessCameraProvider.getInstance(ctx).also { f ->
            f.addListener({ camProv = f.get() }, ContextCompat.getMainExecutor(ctx))
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
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

        errorMsg?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        camProv?.let { provider ->
            AndroidView(
                factory = { ctxView ->
                    PreviewView(ctxView).apply {
                        val preview = Preview.Builder().build().also {
                            it.setSurfaceProvider(surfaceProvider)
                        }
                        val capture = ImageCapture.Builder().build()
                        imageCapture = capture
                        provider.unbindAll()
                        provider.bindToLifecycle(
                            ctxView as androidx.lifecycle.LifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            capture
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(500.dp)
            )
        }

        Spacer(Modifier.height(8.dp))

        Button(
            onClick = {
                if (email.isBlank() || fullName.isBlank()) {
                    errorMsg = "Заповніть усі поля"
                    return@Button
                }
                val cap = imageCapture ?: run {
                    errorMsg = "Камера не готова"; return@Button
                }
                cap.takePicture(
                    ContextCompat.getMainExecutor(ctx),
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            val bmp = CameraUtils.imageProxyToBitmap(image)
                            image.close()
                            val bytes = ByteArrayOutputStream().apply {
                                bmp.compress(Bitmap.CompressFormat.JPEG, 90, this)
                            }.toByteArray()
                            val part = MultipartBody.Part.createFormData(
                                "faceImage",
                                "face.jpg",
                                bytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                            )
                            vm.registerAndLogin(email.trim(), fullName.trim(), part)
                        }

                        override fun onError(exc: ImageCaptureException) {
                            errorMsg = "Не зроблено фото: ${exc.message}"
                        }
                    }
                )
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is UiState.Loading
        ) {
            Text(
                if (uiState is UiState.Loading) "Зачекайте…"
                else "Зареєструватися"
            )
        }

        LaunchedEffect(uiState) {
            when (uiState) {
                is UiState.Success -> onSuccess()
                is UiState.Error   -> errorMsg = (uiState as UiState.Error).msg
                else               -> { /* no-op */ }
            }
        }
    }
}