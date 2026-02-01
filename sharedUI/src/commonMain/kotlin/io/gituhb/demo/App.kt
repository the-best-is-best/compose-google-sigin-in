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
import io.github.firebase_auth.AuthProvider
import io.github.firebase_auth.KAuthCredentials
import io.github.firebase_auth.KFirebaseAuth
import io.github.sign_in_with_google.KGoogleSignIn

import io.gituhb.demo.theme.AppTheme
import kotlinx.coroutines.launch

@Composable
fun App() = AppTheme {
    val googleSign = KGoogleSignIn()
    val scope = rememberCoroutineScope()
    val kFirebaseAuth = KFirebaseAuth()
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
                    kFirebaseAuth.getClient()
                val cred = googleSign.getCredential(clientId, false)
                cred.onSuccess {
                    println("id token is $it")
                    val user = kFirebaseAuth.signInWithCredential(
                        credential = KAuthCredentials(
                            idToken = it.idToken, accessToken = it.accessToken ?: "",
                            provider = AuthProvider.GOOGLE
                        )
                    )
                    user.onSuccess {
                        println("user is $it")
                    }.onFailure {
                        println("error is $it")
                    }
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
