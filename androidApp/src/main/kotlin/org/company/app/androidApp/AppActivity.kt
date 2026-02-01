package org.company.app.androidApp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import io.github.firebase_core.AndroidKFirebaseCore
import io.github.kmmcrypto.AndroidKMMCrypto
import io.github.sign_in_with_google.AndroidGoogleSignIn
import io.gituhb.demo.App

class AppActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        AndroidKFirebaseCore.initialization(this)
        AndroidKMMCrypto.init("key0")

        AndroidGoogleSignIn.initialization(this)
        enableEdgeToEdge()
        setContent { App() }
    }
}
