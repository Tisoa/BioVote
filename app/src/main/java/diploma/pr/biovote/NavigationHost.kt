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
import androidx.compose.material.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
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
import diploma.pr.biovote.ui.voting.VotingListScreen
import java.io.File

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
    NavHost(navController = nav, startDestination = Screen.Onboarding.route) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(onFinished = { nav.navigate(Screen.Register.route) })
        }
        composable(Screen.Register.route) {
            RegistrationScreen(onRegistered = { nav.navigate(Screen.Login.route) })
        }
        composable(Screen.Login.route) {
            LoginScreen(onLoggedIn  = { nav.navigate(Screen.VotingList.route) })
        }
        composable(Screen.VotingList.route) {
            VotingListScreen { nav.navigate(Screen.Camera.route) }
        }
        composable(Screen.Camera.route) {
            CameraScreen(
                onImageCaptured = { /* do something with it */ },
                onError         = { /* show a Snackbar, etc */ }
            )
        }
    }
}

@OptIn(ExperimentalCamera2Interop::class)
@Composable
fun CameraScreen(
    onImageCaptured: (ImageCapture.OutputFileResults) -> Unit,
    onError:        (Throwable)                -> Unit
) {
    val context = LocalContext.current

    // --- 1) Request camera permission ---
    var hasPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasPermission = granted }

    LaunchedEffect(Unit) {
        if (!hasPermission) launcher.launch(Manifest.permission.CAMERA)
    }

    if (!hasPermission) {
        // simple placeholder UI while we wait for permission
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Camera permission required")
        }
        return
    }

    // --- 2) Build PreviewView + bind CameraX use-cases ---
    var imageCaptureUseCase by remember { mutableStateOf<ImageCapture?>(null) }

    AndroidView(
        factory = { ctx ->
            PreviewView(ctx).apply {
                layoutParams = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                // bind in the background
                ProcessCameraProvider.getInstance(ctx)
                    .also { future ->
                        future.addListener({
                            try {
                                val provider = future.get()

                                // 2a) Preview with tiny AWB region to silence that emulator warning:
                                val previewBuilder = Preview.Builder()
                                Camera2Interop.Extender(previewBuilder).setCaptureRequestOption(
                                    CaptureRequest.CONTROL_AWB_REGIONS,
                                    arrayOf(
                                        MeteringRectangle(
                                            Rect(0,0,1,1),
                                            MeteringRectangle.METERING_WEIGHT_MAX
                                        )
                                    )
                                )
                                val preview = previewBuilder
                                    .build()
                                    .also { it.setSurfaceProvider(surfaceProvider) }

                                // 2b) ImageCapture also with AWB region override:
                                val captureBuilder = ImageCapture.Builder()
                                Camera2Interop.Extender(captureBuilder).setCaptureRequestOption(
                                    CaptureRequest.CONTROL_AWB_REGIONS,
                                    arrayOf(
                                        MeteringRectangle(
                                            Rect(0,0,1,1),
                                            MeteringRectangle.METERING_WEIGHT_MAX
                                        )
                                    )
                                )
                                val capture = captureBuilder.build()
                                imageCaptureUseCase = capture

                                // re-bind
                                provider.unbindAll()
                                provider.bindToLifecycle(
                                    ctx as LifecycleOwner,
                                    CameraSelector.DEFAULT_BACK_CAMERA,
                                    preview,
                                    capture
                                )

                            } catch(exc: Exception) {
                                onError(exc)
                            }
                        }, ContextCompat.getMainExecutor(ctx))
                    }
            }
        },
        modifier = Modifier.fillMaxSize()
    )

    // --- 3) A simple “Capture” button overlaid on the preview ---
    Box(
        Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(onClick = {
            val tmpFile = File(context.cacheDir, "camera_capture.jpg")
            val options = ImageCapture.OutputFileOptions.Builder(tmpFile).build()
            imageCaptureUseCase?.takePicture(
                options,
                ContextCompat.getMainExecutor(context),
                object : ImageCapture.OnImageSavedCallback {
                    override fun onError(exc: ImageCaptureException) {
                        onError(exc)
                    }
                    override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                        onImageCaptured(output)
                    }
                }
            )
        }) {
            Text("Capture")
        }
    }
}