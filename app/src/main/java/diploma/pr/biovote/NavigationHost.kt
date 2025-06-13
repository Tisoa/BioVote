package diploma.pr.biovote

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Rect
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.params.MeteringRectangle
import android.view.ViewGroup
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.camera.camera2.interop.Camera2Interop
import androidx.camera.camera2.interop.ExperimentalCamera2Interop
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.compose.*
import diploma.pr.biovote.ui.auth.LoginScreen
import diploma.pr.biovote.RegistrationScreen
import diploma.pr.biovote.ui.voting.VotingListScreen
import java.io.File

/* ---------- simple nav-DSL ---------- */
sealed class Screen(val route: String) {
    object Onboarding  : Screen("onboarding")
    object Register    : Screen("register")
    object Login       : Screen("login")
    object VotingList  : Screen("voting_list")
    object Camera      : Screen("camera")
}

@Composable
fun NavigationHost() {
    val nav = rememberNavController()

    NavHost(nav, startDestination = Screen.Onboarding.route) {

        composable(Screen.Onboarding.route) {
            OnboardingScreen { nav.navigate(Screen.Register.route) }
        }

        composable(Screen.Register.route) {
            RegistrationScreen { nav.navigate(Screen.Login.route) }
        }

        composable(Screen.Login.route) {
            LoginScreen { nav.navigate(Screen.VotingList.route) }
        }

        composable(Screen.VotingList.route) {
            VotingListScreen { nav.navigate(Screen.Camera.route) }
        }

        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { /* TODO use saved file */ },
                onError         = { /* TODO show snackbar */ }
            )
        }
    }
}

/* ---------- camera-demo screen ---------- */
@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraScreen(
    onImageCaptured: (ImageCapture.OutputFileResults) -> Unit,
    onError:        (Throwable)                       -> Unit
) {
    val ctx = LocalContext.current

    /* --------- permission -------- */
    var camGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { camGranted = it }

    LaunchedEffect(Unit) {
        if (!camGranted) permLauncher.launch(Manifest.permission.CAMERA)
    }

    if (!camGranted) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Потрібен дозвіл на камеру")
        }
        return
    }

    /* --------- CameraX binding -------- */
    var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }

    AndroidView(
        factory = { context ->
            PreviewView(context).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                bindCameraUseCases(
                    context       = context,
                    lifecycleOwner= context as LifecycleOwner,
                    previewView   = this
                ) { cap -> imageCapture = cap }
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    /* shutter button */
    Box(Modifier.fillMaxSize().padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        ElevatedButton(
            onClick = {
                val cap = imageCapture ?: return@ElevatedButton
                val file = File(ctx.cacheDir, "capture_${System.currentTimeMillis()}.jpg")
                val opts = ImageCapture.OutputFileOptions.Builder(file).build()
                cap.takePicture(
                    opts,
                    ContextCompat.getMainExecutor(ctx),
                    object : ImageCapture.OnImageSavedCallback {
                        override fun onError(ex: ImageCaptureException) = onError(ex)
                        override fun onImageSaved(out: ImageCapture.OutputFileResults) =
                            onImageCaptured(out)
                    }
                )
            },
            modifier = Modifier.width(160.dp)
        ) { Text("📸 Зняти") }
    }
}

/* helper: creates Preview + ImageCapture and binds them */
@OptIn(ExperimentalCamera2Interop::class)
private fun bindCameraUseCases(
    context: android.content.Context,
    lifecycleOwner: LifecycleOwner,
    previewView: PreviewView,
    onCaptureCreated: (ImageCapture) -> Unit
) {
    val provider = ProcessCameraProvider.getInstance(context).get()

    /* Preview */
    val previewBuilder = Preview.Builder()
    Camera2Interop.Extender(previewBuilder).setCaptureRequestOption(
        CaptureRequest.CONTROL_AWB_REGIONS,
        arrayOf(
            MeteringRectangle(Rect(0, 0, 1, 1), MeteringRectangle.METERING_WEIGHT_MAX)
        )
    )
    val preview = previewBuilder.build().apply {
        setSurfaceProvider(previewView.surfaceProvider)
    }

    /* Photo capture */
    val captureBuilder = ImageCapture.Builder()
    Camera2Interop.Extender(captureBuilder).setCaptureRequestOption(
        CaptureRequest.CONTROL_AWB_REGIONS,
        arrayOf(
            MeteringRectangle(Rect(0, 0, 1, 1), MeteringRectangle.METERING_WEIGHT_MAX)
        )
    )
    val capture = captureBuilder.build()
    onCaptureCreated(capture)

    provider.unbindAll()
    provider.bindToLifecycle(
        lifecycleOwner,
        CameraSelector.DEFAULT_FRONT_CAMERA,
        preview,
        capture
    )
}