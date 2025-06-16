package diploma.pr.biovote.ui.voting

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import diploma.pr.biovote.ui.auth.PollDetailViewModel
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollDetailScreen(
    pollId: Long,
    onBack: () -> Unit,
    vm: PollDetailViewModel = hiltViewModel()
) {
    val poll by vm.poll.collectAsState()
    val loading by vm.isLoading.collectAsState()
    val error by vm.error.collectAsState()

    var selected by remember { mutableStateOf<Long?>(null) }
    var photoFile by remember { mutableStateOf<File?>(null) }
    val ctx = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicturePreview()
    ) { bmp: Bitmap? ->
        bmp?.let {
            val f = File(ctx.cacheDir, "${UUID.randomUUID()}.jpg")
            FileOutputStream(f).use { out ->
                it.compress(Bitmap.CompressFormat.JPEG, 90, out)
            }
            photoFile = f
            vm.onPhotoReady(f)
        }
    }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(poll?.name.orEmpty()) },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                }
            }
        )
    }) { padding ->
        Box(
            Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                loading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                error != null -> Text(
                    error!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
                else -> poll?.questions?.let { qs ->
                    Column(
                        Modifier
                            .padding(16.dp)
                            .selectableGroup()
                    ) {
                        qs.forEach { q ->
                            Text(q.text, style = MaterialTheme.typography.titleMedium)
                            Spacer(Modifier.height(8.dp))
                            q.answers.forEach { a ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .selectable(
                                            selected = (selected == a.id),
                                            onClick = { selected = a.id },
                                            role = Role.RadioButton
                                        )
                                        .padding(vertical = 4.dp)
                                ) {
                                    RadioButton(selected = (selected == a.id), onClick = null)
                                    Spacer(Modifier.width(8.dp))
                                    Text(a.text)
                                }
                            }
                            Spacer(Modifier.height(24.dp))
                        }

                        Button(
                            onClick = { launcher.launch(null) },
                            enabled = selected != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(if (photoFile == null) "Proof by Face" else "Retake Photo")
                        }
                        Spacer(Modifier.height(12.dp))

                        Button(
                            onClick = {
                                selected?.let { ans ->
                                    photoFile?.let {
                                        vm.submitVoteWithProof(ans) { onBack() }
                                    }
                                }
                            },
                            enabled = selected != null && photoFile != null,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Confirm Vote")
                        }
                    }
                }
            }
        }
    }
}