package diploma.pr.biovote.ui.voting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.responses.Poll
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VotingListScreen(
    onPollSelected: (Long) -> Unit = {},
    onOpenCamera: () -> Unit
) {
    val context = LocalContext.current
    val token = remember { TokenManager(context).getToken() ?: "" }

    var polls by remember { mutableStateOf<List<Poll>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(token) {
        scope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.service.getPolls("Bearer $token")
                if (response.isSuccessful) {
                    polls = response.body() ?: emptyList()
                } else {
                    errorMessage = "HTTP ${response.code()}: не вдалося завантажити голосування"
                }
            } catch (e: Exception) {
                errorMessage = "Помилка: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Column(modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
        Text(
            text = "Доступні голосування",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(12.dp))

        when {
            isLoading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            errorMessage != null -> {
                Text(
                    text = errorMessage!!,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(8.dp)
                )
            }
            polls.isEmpty() -> {
                Text("Немає доступних голосувань", style = MaterialTheme.typography.bodyMedium)
            }
            else -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(polls) { poll ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPollSelected(poll.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = poll.title,
                                    style = MaterialTheme.typography.titleMedium
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "ID: ${poll.id}",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}