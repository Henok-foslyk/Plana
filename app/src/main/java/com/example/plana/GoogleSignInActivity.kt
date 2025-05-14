package com.example.plana


import android.app.Activity
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.Credential
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential.Companion.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import com.example.plana.data.EventItem
import java.security.GeneralSecurityException

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
open class GoogleSignInActivity : ComponentActivity() {

    // [START declare_auth]
    private lateinit var auth: FirebaseAuth
    // [END declare_auth]


    // [START declare_credential_manager]
    private lateinit var credentialManager: CredentialManager
    // [END declare_credential_manager]

    private val consentLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
            if (res.resultCode == Activity.RESULT_OK) {
                lifecycleScope.launch {
                    listEvents(this@GoogleSignInActivity, auth.currentUser?.email ?: return@launch)
                }
            } else {
                Log.w(TAG, "User denied Calendar permission")
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // [START initialize_auth]
        // Initialize Firebase Auth
        auth = Firebase.auth
        // [END initialize_auth]

        // [START initialize_credential_manager]
        // Initialize Credential Manager
        credentialManager = CredentialManager.create(baseContext)

        // [END initialize_credential_manager]
        launchCredentialManager()
    }

    // [START on_start_check_user]
    override fun onStart() {
        super.onStart()

        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        updateUI(currentUser)
    }
    // [END on_start_check_user]
    @RequiresApi(Build.VERSION_CODES.O)
    protected open suspend fun listEvents(context: Context, email: String): List<EventItem> {
        var events: List<EventItem> = emptyList()
        try {
            val calendar: Calendar?


            val credential = GoogleAccountCredential.usingOAuth2(
                context,
                arrayListOf(CalendarScopes.CALENDAR_READONLY),
            )
                .setSelectedAccountName(email)
                .setBackOff(ExponentialBackOff())


            calendar =
                CalendarUtil.getCalendarService(credential)

            Log.d("calendar", calendar.toString())

            events = withContext(Dispatchers.IO) {           //  ← 💡 runs off‑main
                CalendarUtil.listUpcomingEvents(calendar)        //  ← does network + getToken
            }

                // Process and display the 'events' list in your UI
            Log.d("Calendar Util", "Successfully fetched ${events.size} upcoming events.")
            Log.d("EVENT", "$events")

        }
        catch (e: UserRecoverableAuthIOException) {
            withContext(Dispatchers.Main) {
                consentLauncher.launch(e.intent)   // ← just launch the intent
            }
        }
        catch (e: IOException) {
            Log.e("Calendar", "Error accessing Calendar API: ${e}")
            // Handle the error (e.g., show a message to the user)
        } catch (e: GeneralSecurityException) {
            Log.e("Calendar", "Security error with Calendar API transport: ${e}")
            // Handle the error
        }

        return events

    }

    private fun launchCredentialManager() {
        // [START create_credential_manager_request]
        // Instantiate a Google sign-in request
        val googleIdOption = GetGoogleIdOption.Builder()
            // Your server's client ID, not your Android client ID.
            .setServerClientId("296323113102-80b4af7ar2hgc6q2rivu0rp6h8af00m2.apps.googleusercontent.com")
            // Only show accounts previously used to sign in.
            .setAutoSelectEnabled(true)
            .setFilterByAuthorizedAccounts(true)
            .build()

        // Create the Credential Manager request
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        // [END create_credential_manager_request]

        lifecycleScope.launch {
            try {
                // Launch Credential Manager UI
                val result = credentialManager.getCredential(
                    context = baseContext,
                    request = request
                )
                // Extract credential from the result returned by Credential Manager
                handleSignIn(result.credential)


            } catch (e: GetCredentialException) {
                Log.e(TAG, "Couldn't retrieve user's credentials: ${e.localizedMessage}")
            }
        }
    }

    // [START handle_sign_in]
    private fun handleSignIn(credential: Credential) {
        // Check if credential is of type Google ID
        if (credential is CustomCredential && credential.type == TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            // Create Google ID Token
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            // Sign in to Firebase with using the token
            firebaseAuthWithGoogle(googleIdTokenCredential.idToken)
        } else {
            Log.w(TAG, "Credential is not of type Google ID!")
        }
    }
    // [END handle_sign_in]

    // [START auth_with_google]
    private fun firebaseAuthWithGoogle(idToken: String) {
        val credential = GoogleAuthProvider.getCredential(idToken, null)

        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")
                    val user = auth.currentUser
                    updateUI(user)
                } else {
                    // If sign in fails, display a message to the user
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    updateUI(null)
                }
            }
    }
    // [END auth_with_google]

    // [START sign_out]
    private fun signOut() {
        // Firebase sign out
        auth.signOut()

        // When a user signs out, clear the current user credential state from all credential providers.
        lifecycleScope.launch {
            try {
                val clearRequest = ClearCredentialStateRequest()
                credentialManager.clearCredentialState(clearRequest)
                updateUI(null)
            } catch (e: ClearCredentialException) {
                Log.e(TAG, "Couldn't clear user credentials: ${e.localizedMessage}")
            }
        }
    }
    // [END sign_out]

    protected open fun updateUI(user: FirebaseUser?) {
    }

    companion object {
        private const val TAG = "GoogleActivity"
    }
}