// app/src/main/java/diploma/pr/biovote/ui/auth/LoginScreen.kt
package diploma.pr.biovote.ui.auth

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.ImageCapture
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
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
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.repository.AuthRepository
import diploma.pr.biovote.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(onLoggedIn: () -> Unit) {
    val ctx       = LocalContext.current
    val scope     = rememberCoroutineScope()
    val repo      = remember { AuthRepository(ApiClient.service) }
    var email     by remember { mutableStateOf("") }
    var err       by remember { mutableStateOf<String?>(null) }
    var capture   by remember { mutableStateOf<ImageCapture?>(null) }
    val preview   = remember { PreviewView(ctx) }

    // request camera…
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) err = "Camera permission denied"
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED
        ) permLauncher.launch(Manifest.permission.CAMERA)
        else {
            val provider = ProcessCameraProvider.getInstance(ctx).get()
            val p = androidx.camera.core.Preview.Builder().build().apply {
                setSurfaceProvider(preview.surfaceProvider)
            }
            val c = ImageCapture.Builder().build().also { capture = it }
            provider.unbindAll()
            provider.bindToLifecycle(ctx as androidx.lifecycle.LifecycleOwner,
                androidx.camera.core.CameraSelector.DEFAULT_FRONT_CAMERA,
                p, c)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = email, onValueChange = { email = it },
            label = { Text("Email") }, singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        AndroidView({ preview }, Modifier.fillMaxWidth().height(300.dp))
        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            if (email.isBlank()) return@Button
            capture?.takePicture(
                ContextCompat.getMainExecutor(ctx),
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(img: androidx.camera.core.ImageProxy) {
                        val bmp = CameraUtils.imageProxyToBitmap(img).also { img.close() }
                        val baos = java.io.ByteArrayOutputStream().apply {
                            bmp.compress(Bitmap.CompressFormat.JPEG, 90, this)
                        }
                        val part = MultipartBody.Part.createFormData(
                            "face", "face.jpg",
                            baos.toByteArray()
                                .toRequestBody("image/jpeg".toMediaTypeOrNull())
                        )
                        scope.launch(Dispatchers.IO) {
                            val resp = repo.loginByFace(email, part)
                            if (resp.isSuccessful && resp.body()?.success == true) {
                                TokenManager(ctx).saveToken(resp.body()!!.message)
                                onLoggedIn()
                            } else {
                                err = "Login failed: ${resp.code()}"
                            }
                        }
                    }
                }
            )
        }, Modifier.fillMaxWidth()) {
            Text("Sign in by Face")
        }
        err?.let { Text(it, color = MaterialTheme.colorScheme.error) }
    }
}