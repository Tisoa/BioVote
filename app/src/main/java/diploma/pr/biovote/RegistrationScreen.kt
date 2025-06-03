package diploma.pr.biovote.ui.auth

import android.Manifest
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.core.content.PermissionChecker
import androidx.lifecycle.LifecycleOwner
import diploma.pr.biovote.data.remote.ApiClient
import diploma.pr.biovote.data.repository.AuthRepository
import diploma.pr.biovote.utils.CameraUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.ByteArrayOutputStream

// Registration screen for capturing user details and biometric data
@Composable
fun RegistrationScreen(onRegistered: () -> Unit) {
    val context = LocalContext.current
    val lifecycleOwner = context as LifecycleOwner
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var email by rememberSaveable { mutableStateOf("") }
    var fullName by rememberSaveable { mutableStateOf("") }
    var imageCapture: ImageCapture? by remember { mutableStateOf(null) }
    var errorText by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (!isGranted) {
            errorText = "Camera permission denied"
        }
    }

    // Dependency injection
    val authRepository = EntryPointAccessors.fromApplication(context, AuthRepository::class.java)
    LaunchedEffect(Unit) {
        if (PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA) != PermissionChecker.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text("Register", style = MaterialTheme.typography.headlineSmall)
        TextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        TextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )

        AndroidView(
            factory = { ctx ->
                val previewView = PreviewView(ctx)
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder().build()
                imageCapture = capture
                val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, capture)
                previewView
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
        )

        Button(
            onClick = {
                if (PermissionChecker.checkSelfPermission(context, Manifest.permission.CAMERA) == PermissionChecker.PERMISSION_GRANTED) {
                    val fileName = "face_${System.currentTimeMillis()}.jpg"
                    imageCapture?.takePicture(
                        ContextCompat.getMainExecutor(context),
                        object : ImageCapture.OnImageCapturedCallback() {
                            override fun onCaptureSuccess(image: ImageProxy) {
                                val bitmap = CameraUtils.imageProxyToBitmap(image)
                                image.close()
                                val bos = ByteArrayOutputStream()
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, bos)
                                val imageBytes = bos.toByteArray()
                                val imageBody = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())
                                val photoPart = MultipartBody.Part.createFormData("faceImage", fileName, imageBody)
                                val emailPart = email.toRequestBody("text/plain".toMediaTypeOrNull())
                                val fullNamePart = fullName.toRequestBody("text/plain".toMediaTypeOrNull())

                                coroutineScope.launch(Dispatchers.IO) {
                                    val result = ApiClient.service.registerUser(emailPart, fullNamePart, photoPart)
                                }
                            }

                            override fun onError(exception: ImageCaptureException) {
                                errorText = "Camera capture error: ${exception.message}"
                            }
                        }
                    )
                } else {
                    permissionLauncher.launch(Manifest.permission.CAMERA)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Register")
        }

        errorText?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
        }
    }
}

class AuthRepository {

}

class EntryPointAccessors {
    companion object {
        fun fromApplication(context: LifecycleOwner, java: Class<AuthRepository>): Any {
            TODO("Not yet implemented")
        }
    }

}
