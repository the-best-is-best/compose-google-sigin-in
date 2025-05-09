package io.github.sign_in_with_google


import io.github.native.sign_in_with_google.GIDConfiguration
import io.github.native.sign_in_with_google.GIDGoogleUser
import io.github.native.sign_in_with_google.GIDSignIn
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.UIKit.UIApplication
import platform.UIKit.UIViewController
import platform.UIKit.UIWindow
import platform.UIKit.UIWindowScene
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

actual class KGoogleSignIn {
    companion object {
        var userData: GIDGoogleUser? = null
    }

    fun initialize(clientId: String) {
        val config = GIDConfiguration(clientId)
        GIDSignIn.sharedInstance.configuration = config

        GIDSignIn.sharedInstance.restorePreviousSignInWithCompletion { user, error ->
            if (error != null) {
                // Handle error
                println("Error restoring previous sign-in: ${error.localizedDescription()}")
            } else if (user == null) {
                // No previous sign-in
                println("No previous sign-in found.")
            } else {
                // Successfully restored previous sign-in
                println("Successfully restored previous sign-in.")
            }
            if (user != null) {
                userData = user
            }
        }
    }


    actual suspend fun getCredential(
        clientId: String,
        setFilterByAuthorizedAccounts: Boolean
    ): Result<GoogleCredential> = suspendCancellableCoroutine { cont ->
        try {
            val config = GIDConfiguration(clientId)
            GIDSignIn.sharedInstance().configuration = config

            val windowScene: UIWindowScene? =
                UIApplication.sharedApplication.connectedScenes.first() as? UIWindowScene
            val window = windowScene?.windows?.first() as? UIWindow
            val rootViewController: UIViewController? = window?.rootViewController
            if (rootViewController == null) {
                cont.resumeWithException(Exception("There is no root view controller"))
                return@suspendCancellableCoroutine
            }

            GIDSignIn.sharedInstance()
                .signInWithPresentingViewController(rootViewController) { result, error ->
                if (error != null) {
                    // Check if the error is due to user cancellation
                    if (error.localizedDescription()
                            .contains("The user canceled the sign-in flow", ignoreCase = true)
                    ) {
                        // Handle the cancellation gracefully without throwing an exception
                        cont.resume(Result.failure(Exception("The user canceled the sign-in flow")))
                    } else {
                        // Handle other errors
                        cont.resumeWithException(Exception(error.localizedDescription()))
                    }
                    return@signInWithPresentingViewController
                }

                    if (result == null) {
                        cont.resumeWithException(Exception("Sign-in failed or the result is null"))
                        return@signInWithPresentingViewController
                    }

                    userData = result.user
                    val idToken = result.user.idToken
                cont.resume(
                    Result.success(
                        GoogleCredential(
                            idToken!!.tokenString,
                            userData?.accessToken?.tokenString
                        )
                    )
                )
            }

        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }


    actual suspend fun getUserData(): UserData? {
        if (userData == null) {
            return null
        }
        return UserData(
            idToken = userData!!.idToken!!.tokenString,
            userId = userData!!.userID!!,
            email = userData!!.profile!!.email,
            profilePictureUrl = null,
            displayName = userData!!.profile()?.name(),
            phoneNumber = null,
            familyName = userData!!.profile()?.familyName,
            givenName = userData!!.profile()?.givenName
        )
    }

    actual suspend fun signOut() {
        GIDSignIn.sharedInstance.signOut()
        userData = null
    }


}