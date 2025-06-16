package diploma.pr.biovote.ui.voting

// for `by … collectAsState(...)`
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import diploma.pr.biovote.data.remote.model.responses.Question
import diploma.pr.biovote.ui.auth.PollDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PollDetailScreen(
    pollId: Long,
    onBack: () -> Unit,
    viewModel: PollDetailViewModel = hiltViewModel()
) {
    val poll      by viewModel.poll.collectAsState(initial = null)
    val isLoading by viewModel.isLoading.collectAsState(initial = false)
    val questions = poll?.questions.orEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(poll?.name.orEmpty()) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center)
                )
            } else {
                Column(modifier = Modifier.padding(16.dp)) {
                    questions.forEach { question ->
                        QuestionSection(question) { answerId ->
                            // submitVote now only takes a List<Long>
                            viewModel.submitVote(listOf(answerId))
                            onBack()
                        }
                        Spacer(modifier = Modifier.height(24.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuestionSection(
    question: Question,
    onSubmit: (Long) -> Unit
) {
    var selectedAnswer by remember { mutableStateOf<Long?>(null) }

    Text(
        text = question.text,
        style = MaterialTheme.typography.titleMedium
    )
    Spacer(modifier = Modifier.height(8.dp))

    Column(Modifier.selectableGroup()) {
        question.answers.forEach { answer ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedAnswer == answer.id),
                        onClick = { selectedAnswer = answer.id }
                    )
                    .padding(vertical = 4.dp)
            ) {
                RadioButton(
                    selected = (selectedAnswer == answer.id),
                    onClick  = { selectedAnswer = answer.id }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = answer.text,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }

    Spacer(modifier = Modifier.height(16.dp))

    Button(
        onClick  = { selectedAnswer?.let(onSubmit) },
        enabled  = (selectedAnswer != null),
    ) {
        Text("Submit Vote")
    }
}