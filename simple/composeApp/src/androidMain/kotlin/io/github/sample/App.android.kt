package io.github.sample

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.github.compose_utils.AndroidUtils
import io.github.firebase_core.AndroidKFirebaseCore
import io.github.sign_in_with_google.AndroidGoogleSignIn

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AndroidKFirebaseCore.initialize(this)
        AndroidGoogleSignIn.initialization(this)
        AndroidUtils.initialization(this)

        setContent { App() }
    }
}

@Preview
@Composable
fun AppPreview() {
    App()
}
