package diploma.pr.biovote.ui.voting

/* ---------- Android & Compose ---------- */
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner

/* ---------- Data layer ---------- */
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.requests.VoteRequest
import diploma.pr.biovote.data.remote.model.responses.Poll
import diploma.pr.biovote.data.remote.model.responses.Question

/* ---------- Coroutines / utils ---------- */
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.UUID
import java.util.concurrent.Executor

@Composable
fun PollDetailScreen(pollId: Long) {

    /* ---------- basics ---------- */
    val ctx      = LocalContext.current
    val token    = remember { TokenManager(ctx).getToken().orEmpty() }
    val uiScope  = rememberCoroutineScope()
    val executor: Executor = remember { ContextCompat.getMainExecutor(ctx) }

    /* ---------- state ---------- */
    var poll       by remember { mutableStateOf<Poll?>(null) }
    var isLoading  by remember { mutableStateOf(true) }
    var uiError    by remember { mutableStateOf<String?>(null) }
    var voteHash   by remember { mutableStateOf<String?>(null) }

    var provider   by remember { mutableStateOf<ProcessCameraProvider?>(null) }
    var imageCap   by remember { mutableStateOf<ImageCapture?>(null) }
    var camError   by remember { mutableStateOf<String?>(null) }

    val chosenAns  = remember { mutableStateMapOf<Long, Long>() } // questionId -> answerId

    /* ---------- load poll once ---------- */
    LaunchedEffect(pollId) {
        try {
            val resp = ApiClient.service.getPoll(pollId, "Bearer $token")
            if (resp.isSuccessful) poll = resp.body()
            else                   uiError = "HTTP ${resp.code()}"
        } catch (e: Exception) {
            uiError = e.localizedMessage
        } finally {
            isLoading = false
        }
    }

    /* ---------- camera permission / provider ---------- */
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (!granted) {
            camError = "Дозвіл на камеру не надано"
        } else {
            ProcessCameraProvider.getInstance(ctx).also { f ->
                f.addListener({ provider = f.get() }, executor)
            }
        }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(ctx, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            ProcessCameraProvider.getInstance(ctx).also { f ->
                f.addListener({ provider = f.get() }, executor)
            }
        } else permLauncher.launch(Manifest.permission.CAMERA)
    }

    /* ---------- UI ---------- */
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        when {
            isLoading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                CircularProgressIndicator()
            }

            uiError != null -> {
                Text(uiError!!, color = MaterialTheme.colorScheme.error)
                return@Column
            }

            poll == null -> {
                Text("Опитування не знайдено", color = MaterialTheme.colorScheme.error)
                return@Column
            }
        }

        /* ---------- main content ---------- */
        val p = poll ?: return@Column

        Text(p.name ?: "-", style = MaterialTheme.typography.titleLarge)
        Spacer(Modifier.height(4.dp))
        Text(p.description ?: "-", style = MaterialTheme.typography.bodyMedium)
        Spacer(Modifier.height(12.dp))

        /* список питань */
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(p.questions) { q: Question ->
                Column {
                    Text(q.text ?: "-", style = MaterialTheme.typography.titleSmall)
                    Spacer(Modifier.height(4.dp))
                    q.answers.forEach { ans ->
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 8.dp)
                        ) {
                            RadioButton(
                                selected = chosenAns[q.id] == ans.id,
                                onClick  = { chosenAns[q.id] = ans.id }
                            )
                            Spacer(Modifier.width(8.dp))
                            Text(ans.text ?: "")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Divider()
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        /* camera preview */
        provider?.let { prov ->
            AndroidView(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                factory = { viewCtx ->
                    PreviewView(viewCtx).also { previewView ->
                        val preview = Preview.Builder().build().apply {
                            setSurfaceProvider(previewView.surfaceProvider)
                        }
                        val capture = ImageCapture.Builder().build()
                        imageCap = capture

                        prov.unbindAll()
                        prov.bindToLifecycle(
                            viewCtx as LifecycleOwner,
                            CameraSelector.DEFAULT_FRONT_CAMERA,
                            preview,
                            capture
                        )
                    }
                }
            )
        }

        camError?.let {
            Spacer(Modifier.height(4.dp))
            Text(it, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(16.dp))

        /* ---------- submit ---------- */
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = {

                /* validate camera & answers */
                val cap = imageCap
                if (cap == null) {
                    camError = "Камера не готова"; return@Button
                }
                if (chosenAns.size != p.questions.size) {
                    camError = "Заповніть усі відповіді"; return@Button
                }

                /* «селфі» – фото не передаємо, але чекаємо callback */
                camError = null
                cap.takePicture(
                    executor,
                    object : ImageCapture.OnImageCapturedCallback() {
                        override fun onCaptureSuccess(image: ImageProxy) {
                            image.close()

                            val req = VoteRequest(
                                pollId    = p.id,
                                answerIds = chosenAns.values.toList()
                            )

                            uiScope.launch(Dispatchers.IO) {
                                try {
                                    val resp = ApiClient.service.submitVote(
                                        "Bearer $token",
                                        req
                                    )
                                    withContext(Dispatchers.Main) {
                                        if (resp.isSuccessful) {
                                            voteHash = UUID.randomUUID().toString()
                                        } else {
                                            camError = "HTTP ${resp.code()}"
                                        }
                                    }
                                } catch (e: Exception) {
                                    withContext(Dispatchers.Main) {
                                        camError = e.localizedMessage
                                    }
                                }
                            }
                        }

                        override fun onError(ex: ImageCaptureException) {
                            camError = "Помилка камери: ${ex.message}"
                        }
                    }
                )
            }
        ) { Text("Підтвердити голос") }

        voteHash?.let {
            Spacer(Modifier.height(8.dp))
            Text(
                "Голос прийнято!\nКод підтвердження: $it",
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}