package diploma.pr.biovote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.core.view.WindowCompat
import dagger.hilt.android.AndroidEntryPoint            // ← import this
import diploma.pr.biovote.ui.theme.BioVoteTheme

@AndroidEntryPoint                                 // ← add this
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent {
            BioVoteTheme {
                NavigationHost()
            }
        }
    }
}