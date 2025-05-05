package io.github.sign_in_with_google

import android.content.Context
import android.net.Uri
import androidx.core.content.edit
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import io.github.kmmcrypto.KMMCrypto
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException


actual class KGoogleSignIn {
    companion object {
        var cred: GoogleIdTokenCredential? = null
    }
    actual suspend fun getCredential(
        clientId: String,
        setFilterByAuthorizedAccounts: Boolean
    ): Result<GoogleCredential> = suspendCancellableCoroutine { cont ->
        val applicationContext = AndroidGoogleSignIn.getActivity()
        try {
            val credentialManager =
                CredentialManager.create(applicationContext) // Ensure this is a valid activity context

            val googleOption = GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setAutoSelectEnabled(true)
                .setFilterByAuthorizedAccounts(setFilterByAuthorizedAccounts)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val credentialResponse = credentialManager.getCredential(
                        context = applicationContext,
                        request = request
                    )
                    val credential = credentialResponse.credential
                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
                        cred = googleIdTokenCredential
                        saveCredential(cred!!)
                        cont.resume(
                            Result.success(
                                GoogleCredential(
                                    idToken = idToken,
                                    accessToken = null
                                )
                            )
                        )
                    } else {
                        cont.resume(Result.failure(Exception("Unexpected type of credential")))
                    }
                } catch (e: GetCredentialCancellationException) {
                    // Handle the cancellation gracefully
                    cont.resume(Result.failure(Exception("User canceled credential selection")))
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
        } catch (e: Exception) {
            cont.resumeWithException(e)
        }
    }

    actual suspend fun getUserData(): UserData? {
        if (cred == null) {
            cred = loadCredential()
        }

        if (cred != null) {
            return UserData(
                userId = cred!!.id,
                email = cred!!.id,
                idToken = cred!!.idToken,
                displayName = cred!!.displayName,
                profilePictureUrl = cred!!.profilePictureUri.toString(),
                phoneNumber = cred!!.phoneNumber,
                familyName = cred!!.familyName,
                givenName = cred!!.givenName
            )
        }
        return null
    }


    actual suspend fun signOut() {
        val context = AndroidGoogleSignIn.getActivity()

        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())

        context.getSharedPreferences("google_signin", Context.MODE_PRIVATE).edit { clear() }
        cred = null
        deleteCred()
    }

    private fun deleteCred() {
        val kmmCrypto = KMMCrypto()
        val group = "google_signin"

        try {
            kmmCrypto.deleteData("idToken", group)
            kmmCrypto.deleteData("id", group)
            kmmCrypto.deleteData("displayName", group)
            kmmCrypto.deleteData("profilePictureUrl", group)
            kmmCrypto.deleteData("phoneNumber", group)
            kmmCrypto.deleteData("familyName", group)
            kmmCrypto.deleteData("givenName", group)

            println("Credentials deleted successfully")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun saveCredential(cred: GoogleIdTokenCredential) {
        val kmmCrypto = KMMCrypto()
        val group = "google_signin"

        kmmCrypto.saveData("idToken", group, cred.idToken)
        kmmCrypto.saveData("id", group, cred.id)
        kmmCrypto.saveData("displayName", group, cred.displayName ?: "")
        kmmCrypto.saveData("profilePictureUrl", group, cred.profilePictureUri?.toString() ?: "")
        kmmCrypto.saveData("phoneNumber", group, cred.phoneNumber ?: "")
        kmmCrypto.saveData("familyName", group, cred.familyName ?: "")
        kmmCrypto.saveData("givenName", group, cred.givenName ?: "")
    }


    private suspend fun loadCredential(): GoogleIdTokenCredential? {
        val kmmCrypto = KMMCrypto()
        val group = "google_signin"

        val idToken = kmmCrypto.loadData("idToken", group)
        val id = kmmCrypto.loadData("id", group)

        if (idToken.isNullOrBlank() || id.isNullOrBlank()) return null

        return GoogleIdTokenCredential.Builder()
            .setId(id)
            .setIdToken(idToken)
            .setDisplayName(kmmCrypto.loadData("displayName", group))
            .setProfilePictureUri(
                kmmCrypto.loadData("profilePictureUrl", group)?.let { Uri.parse(it) }
            )
            .setPhoneNumber(kmmCrypto.loadData("phoneNumber", group))
            .setFamilyName(kmmCrypto.loadData("familyName", group))
            .setGivenName(kmmCrypto.loadData("givenName", group))
            .build()
    }


}