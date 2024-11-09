package io.github.sign_in_with_google

import cocoapods.GoogleSignIn.GIDConfiguration
import cocoapods.GoogleSignIn.GIDGoogleUser
import cocoapods.GoogleSignIn.GIDSignIn
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

    actual suspend fun getCredential(
        clientId: String,
        setFilterByAuthorizedAccounts: Boolean
    ): Result<GoogleCredential> = suspendCancellableCoroutine { cont ->
        val config = GIDConfiguration(clientId)
        GIDSignIn.sharedInstance().configuration = config

        val windowScene: UIWindowScene? =
            UIApplication.sharedApplication.connectedScenes.first() as? UIWindowScene
        val window = windowScene?.windows?.first() as? UIWindow
        val rootViewController: UIViewController? = window?.rootViewController
        if (rootViewController == null) {
            cont.resumeWithException(Exception("There is no root view controller"))
        }

        try {
            GIDSignIn.sharedInstance.signInWithPresentingViewController(rootViewController!!) { result, error ->
                if (error != null) {
                    cont.resumeWithException(Exception(error.localizedDescription()))
                }
                userData = result?.user
                val idToken = result?.user?.idToken
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

//    actual fun getStoredCredential(): Result<GoogleCredential> {
//       val currentUser = GIDSignIn.sharedInstance.currentUser
//        userData = currentUser
//        println("current user data is ${currentUser}")
//        return if(userData == null){
//            Result.failure(Exception("No data saved"))
//        } else{
//
//            Result.success(
//                GoogleCredential(
//                    idToken = userData!!.idToken!!.tokenString,
//                    accessToken = userData!!.accessToken.tokenString
//                )
//            )
//        }
//    }

}