package diploma.pr.biovote.ui.voting


import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
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
import diploma.pr.biovote.data.local.TokenManager
import diploma.pr.biovote.data.remote.model.ApiClient
import diploma.pr.biovote.data.remote.model.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Composable
fun VotingListScreen() {
    val coroutineScope = rememberCoroutineScope()
    var polls by remember { mutableStateOf<List<ApiService.PollResponse>>(emptyList()) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current
    val token = remember { TokenManager(context).getToken() ?: "" }

    LaunchedEffect(Unit) {
        coroutineScope.launch(Dispatchers.IO) {
            try {
                val response = ApiClient.service.getPolls("Bearer $token")
                if (response.isSuccessful && response.body() != null) {
                    polls = response.body()!!
                } else {
                    errorMessage = "Не вдалося завантажити список голосувань"
                }
            } catch (e: Exception) {
                errorMessage = "Помилка: ${e.message}"
            }
        }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text("Доступні голосування", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(16.dp))

        if (polls.isEmpty() && errorMessage == null) {
            CircularProgressIndicator()
        }

        errorMessage?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
        }

        polls.forEach { poll ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { /* Navigate to detail screen */ },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(poll.title, style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text("ID: ${poll.id}", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}
