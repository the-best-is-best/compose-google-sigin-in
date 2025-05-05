package io.gituhb.demo

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
import io.github.sign_in_with_google.KGoogleSignIn

import io.gituhb.demo.theme.AppTheme
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
                    if (platform() == "Android") "204571788770-63q1akee6h2fgjdkafepa8jhouqh2csv.apps.googleusercontent.com" else "204571788770-rhck30jdb0321gosbi07h94s50midf52.apps.googleusercontent.com"
                val cred = googleSign.getCredential(clientId, false)
                cred.onSuccess {
                    println("id token is $it")
//                    Firebase.auth.signInWithCredential(
//                        authCredential = GoogleAuthProvider.credential(
//                            idToken = it.idToken, accessToken = it.accessToken
//                        )
//                    )
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
            }
        }) {
            Text("Logout")
        }
    }
}
