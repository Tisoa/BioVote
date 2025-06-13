package diploma.pr.biovote.ui.voting

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import diploma.pr.biovote.data.remote.model.responses.Poll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.repository.PollRepository
import kotlinx.coroutines.launch

@Composable
fun VotingListScreen(
    onPollSelected: (Long) -> Unit
) {
    /* ---------- state ---------- */
    val ctx        = LocalContext.current
    val token      = remember { TokenManager(ctx).getToken().orEmpty() }
    val repo       = remember { PollRepository() }

    var polls      by remember { mutableStateOf<List<Poll>>(emptyList()) }
    var isLoading  by remember { mutableStateOf(true) }
    var error      by remember { mutableStateOf<String?>(null) }
    val scope      = rememberCoroutineScope()

    /* ---------- load ---------- */
    LaunchedEffect(token) {
        scope.launch {
            isLoading = true
            repo.polls(token)
                .onSuccess { polls = it }
                .onFailure { error = it.message }
            isLoading = false
        }
    }

    /* ---------- UI ---------- */
    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Доступні голосування", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        when {
            isLoading ->
                Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }

            error != null ->
                Text(error!!, color = MaterialTheme.colorScheme.error)

            polls.isEmpty() ->
                Text("Немає доступних голосувань")

            else ->
                LazyColumn(
                    Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(polls) { poll ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPollSelected(poll.id) },
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                /* якщо у вашій моделі поле `name`, замініть! */
                                Text(poll.name, style = MaterialTheme.typography.titleMedium)
                                Spacer(Modifier.height(4.dp))
                                Text("ID: ${poll.id}", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                    }
                }
        }
    }
}