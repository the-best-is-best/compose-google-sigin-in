package io.github.sign_in_with_google

import android.content.Context
import android.os.Bundle
import android.os.Parcel
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
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
        try {
            val credentialManager =
                CredentialManager.create(AndroidGoogleSignIn.getActivity()) // Ensure this is a valid activity context

            val googleOption = GetGoogleIdOption.Builder()
                .setServerClientId(clientId)
                .setFilterByAuthorizedAccounts(setFilterByAuthorizedAccounts)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleOption)
                .build()

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    // Run the suspend function getCredential within a coroutine
                    val credentialResponse = credentialManager.getCredential(
                        context = AndroidGoogleSignIn.getActivity(),
                        request = request
                    )
                    val credential = credentialResponse.credential
                    if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                        val googleIdTokenCredential =
                            GoogleIdTokenCredential.createFrom(credential.data)
                        val idToken = googleIdTokenCredential.idToken
//                        saveBundle(credential.data)
                        cred = googleIdTokenCredential
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
                } catch (e: Exception) {
                    cont.resumeWithException(e)
                }
            }
        } catch (e: Exception) {
            // Resume with failure if an exception occurred
            cont.resumeWithException(e)
        }
    }


    actual suspend fun getUserData(): UserData? {
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
        val credentialManager = CredentialManager.create(AndroidGoogleSignIn.getActivity())
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        deleteBundle()
        cred = null
    }

    private fun saveBundle(bundle: Bundle) {
        val `in`: Bundle = bundle
        val fos: FileOutputStream =
            AndroidGoogleSignIn.getActivity().openFileOutput("google_sign_in", Context.MODE_PRIVATE)
        val p = Parcel.obtain() //creating empty parcel object
        `in`.writeToParcel(p, 0) //saving bundle as parcel
        fos.write(p.marshall()) //writing parcel to file
        fos.flush()
        fos.close()

    }

    private fun loadBundle(): Bundle? {
        return try {
            val fis: FileInputStream =
                AndroidGoogleSignIn.getActivity().openFileInput("google_sign_in")
            val p = Parcel.obtain()
            val data = fis.readBytes() // Reading the Parcel from the file
            p.unmarshall(data, 0, data.size) // Unmarshalling the data into the Parcel object
            p.setDataPosition(0) // Reset the position to the start of the Parcel
            val bundle = Bundle.CREATOR.createFromParcel(p) // Creating the Bundle from the Parcel
            fis.close()
            bundle
        } catch (e: Exception) {
            e.printStackTrace() // Handle exception (e.g., file not found, etc.)
            null
        }
    }

    private fun deleteBundle() {
        try {
            val file = File(AndroidGoogleSignIn.getActivity().filesDir, "google_sign_in")
            if (file.exists()) {
                val deleted = file.delete() // Deletes the file
                if (deleted) {
                    println("File deleted successfully")
                } else {
                    println("Failed to delete the file")
                }
            } else {
                println("File not found")
            }
        } catch (e: Exception) {
            e.printStackTrace() // Handle any exception that might occur
        }
    }


//    actual fun getStoredCredential():Result<GoogleCredential> {
//       val bundle = loadBundle() ?: return Result.failure(Exception("No data saved"))
//        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(bundle)
//        cred = googleIdTokenCredential
//        return Result.success(
//            GoogleCredential(
//                idToken = googleIdTokenCredential.idToken,
//                null
//        ))
//    } actual fun getStoredCredential():Result<GoogleCredential> {
//       val bundle = loadBundle() ?: return Result.failure(Exception("No data saved"))
//        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(bundle)
//        cred = googleIdTokenCredential
//        return Result.success(
//            GoogleCredential(
//                idToken = googleIdTokenCredential.idToken,
//                null
//        ))
//    }

}
