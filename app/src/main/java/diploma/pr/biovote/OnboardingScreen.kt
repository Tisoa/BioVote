// app/src/main/java/diploma/pr/biovote/OnboardingScreen.kt
package diploma.pr.biovote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun OnboardingScreen(onFinished: () -> Unit) {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("Ласкаво просимо до BioVote!", modifier = Modifier.padding(bottom = 16.dp))
        Text("Коротка інструкція або трохи тексту...", modifier = Modifier.padding(bottom = 24.dp))
        Button(onClick = onFinished, Modifier.fillMaxWidth()) {
            Text("Продовжити")
        }
    }
}