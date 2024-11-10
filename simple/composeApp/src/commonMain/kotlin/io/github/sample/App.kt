package io.github.sample

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.gitlive.firebase.Firebase
import dev.gitlive.firebase.auth.GoogleAuthProvider
import dev.gitlive.firebase.auth.auth
import io.github.compose_utils.PlatformData
import io.github.sample.theme.AppTheme
import io.github.sign_in_with_google.KGoogleSignIn
import kotlinx.coroutines.launch

@Composable
internal fun App() = AppTheme {
    val googleSign = KGoogleSignIn()
    val scope = rememberCoroutineScope()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(onClick = {
            scope.launch {
                val clientId: String =
                    if (PlatformData().name == "android") "608756724133-mj7pg7pcrlvjjdrib1g050vgap51a1s3.apps.googleusercontent.com" else "608756724133-omnidr2brkle3pp9d2s30rd87olvj7d9.apps.googleusercontent.com"
                val cred = googleSign.getCredential(clientId, true)
                cred.onSuccess {
                    Firebase.auth.signInWithCredential(
                        authCredential = GoogleAuthProvider.credential(
                            idToken = it.idToken, accessToken = it.accessToken
                        )
                    )
                }

            }
        }) {
            Text("Sign in with google")
        }
        Button(onClick = {
            scope.launch {
                println("user id is ${googleSign.getUserData()?.userId}")
            }
        }) {
            Text("Get user data")
        }
//        Button(onClick = {
//
//            scope.launch {
//               val cred  = googleSign.getStoredCredential()
//                cred.onSuccess {
//                    Firebase.auth.signInWithCredential(GoogleAuthProvider.credential(idToken = it.idToken, it.accessToken))
//                    println("user is ${Firebase.auth.currentUser}")
//                }
//            }
//        }){
//            Text("get stored cred")
//        }
        Button(onClick = {
            scope.launch {
                googleSign.signOut()
                Firebase.auth.signOut()
            }
        }) {
            Text("Logut")
        }
    }
}
