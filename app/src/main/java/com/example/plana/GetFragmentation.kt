package com.example.plana

import android.Manifest
import android.accounts.AccountManager
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.plana.databinding.FragmentGetEventBinding
import com.example.plana.model.GetEventModel
import com.example.plana.utils.Constants.PREF_ACCOUNT_NAME
import com.example.plana.utils.Constants.REQUEST_ACCOUNT_PICKER
import com.example.plana.utils.Constants.REQUEST_AUTHORIZATION
import com.example.plana.utils.Constants.REQUEST_GOOGLE_PLAY_SERVICES
import com.example.plana.utils.Constants.REQUEST_PERMISSION_GET_ACCOUNTS
import com.example.plana.utils.executeAsyncTask
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.api.client.extensions.android.http.AndroidHttp
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.googleapis.extensions.android.gms.auth.GooglePlayServicesAvailabilityIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.UserRecoverableAuthIOException
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAuthIOException
import com.google.api.client.http.HttpRequestInitializer
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.client.util.DateTime
import com.google.api.client.util.ExponentialBackOff
import com.google.api.services.calendar.Calendar
import com.google.api.services.calendar.CalendarScopes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.EasyPermissions
import java.io.IOException

class GetEventFragment : Fragment() {

    private var _binding: FragmentGetEventBinding? = null
    private val binding get() = _binding!!


    private var mCredential: GoogleAccountCredential? = null
    private var mService: Calendar? = null

    var mProgress: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initCredentials()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentGetEventBinding.inflate(inflater, container, false)

        initView()

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_GOOGLE_PLAY_SERVICES -> if (resultCode != Activity.RESULT_OK) {
                binding.txtOut.text =
                    "This app requires Google Play Services. Please install " + "Google Play Services on your device and relaunch this app."
            } else {
                getResultsFromApi()
            }
            REQUEST_ACCOUNT_PICKER -> if (resultCode == Activity.RESULT_OK && data != null &&
                data.extras != null
            ) {
                val accountName = data.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
                if (accountName != null) {
                    val settings = this.activity?.getPreferences(Context.MODE_PRIVATE)
                    val editor = settings?.edit()
                    editor?.putString(PREF_ACCOUNT_NAME, accountName)
                    editor?.apply()
                    mCredential!!.selectedAccountName = accountName
                    getResultsFromApi()
                }
            }
            REQUEST_AUTHORIZATION -> if (resultCode == Activity.RESULT_OK) {
                getResultsFromApi()
            }
        }
    }

    private fun initView() {
        mProgress = ProgressDialog(requireContext())
        mProgress!!.setMessage("Loading...")

        with(binding) {
            btnCalendar.setOnClickListener {
                btnCalendar.isEnabled = false
                txtOut.text = ""
                getResultsFromApi()
                btnCalendar.isEnabled = true
            }
        }
    }

    // HERE
    private fun initCredentials() {
        mCredential = GoogleAccountCredential.usingOAuth2(
            requireContext(),
            arrayListOf(CalendarScopes.CALENDAR)
        )
            .setBackOff(ExponentialBackOff())

        initCalendarBuild(mCredential)
    }

    private fun initCalendarBuild(credential: GoogleAccountCredential?) {
        val transport = AndroidHttp.newCompatibleTransport()
        val jsonFactory = JacksonFactory.getDefaultInstance()
        val httpRequestInitializer = HttpRequestInitializer { httpRequest ->
            // Log the HTTP request
            Log.d("GoogleAPI", "Request: ${httpRequest.requestMethod} ${httpRequest.url}")
        }
        mService = Calendar.Builder(
            transport, jsonFactory, credential
        )   .setHttpRequestInitializer(httpRequestInitializer)
            .setApplicationName("Plana")
            .build()
    }

    private fun getResultsFromApi() {
        if (!isGooglePlayServicesAvailable()) {
            acquireGooglePlayServices()
        } else if (mCredential!!.selectedAccountName == null) {
            chooseAccount()
        } else if (!isDeviceOnline()) {
            binding.txtOut.text = "No network connection available."
        } else {
            makeRequestTask()
        }
    }

    private fun chooseAccount() {
        if (EasyPermissions.hasPermissions(
                requireContext(), Manifest.permission.GET_ACCOUNTS
            )
        ) {
            val accountName = this.activity?.getPreferences(Context.MODE_PRIVATE)
                ?.getString(PREF_ACCOUNT_NAME, null)
            if (accountName != null) {
                mCredential!!.selectedAccountName = accountName
                getResultsFromApi()
                Log.d("AccountName", "$accountName")
            } else {
                // Start a dialog from which the user can choose an account
                startActivityForResult(
                    mCredential!!.newChooseAccountIntent(),
                    REQUEST_ACCOUNT_PICKER
                )
            }
        } else {
            // Request the GET_ACCOUNTS permission via a user dialog
            EasyPermissions.requestPermissions(
                this,
                "This app needs to access your Google account (via Contacts).",
                REQUEST_PERMISSION_GET_ACCOUNTS,
                Manifest.permission.GET_ACCOUNTS
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }

    //Google consolea erişim izni olup olmadıgına bakıyoruz
    private fun isGooglePlayServicesAvailable(): Boolean {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
        return connectionStatusCode == ConnectionResult.SUCCESS
    }

    //Cihazın Google play servislerini destekleyip desteklemediğini kontrol ediyor
    private fun acquireGooglePlayServices() {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val connectionStatusCode = apiAvailability.isGooglePlayServicesAvailable(requireContext())
        if (apiAvailability.isUserResolvableError(connectionStatusCode)) {
            showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode)
        }
    }

    fun showGooglePlayServicesAvailabilityErrorDialog(connectionStatusCode: Int) {
        val apiAvailability = GoogleApiAvailability.getInstance()
        val dialog = apiAvailability.getErrorDialog(
            this,
            connectionStatusCode,
            REQUEST_GOOGLE_PLAY_SERVICES
        )
        dialog?.show()
    }

    private fun isDeviceOnline(): Boolean {
        val connMgr =
            this.activity?.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun makeRequestTask() {
        var mLastError: Exception? = null

        lifecycleScope.launch {

            mProgress!!.show()



            val job = async(Dispatchers.IO) {
                try {
                    Log.d("zzanzu", "makeRequestTask: doInBackground")
                    getDataFromCalendar()
                } catch (e: Exception) {
                    mLastError = e
                    Log.d("zzanzu", "makeRequestTask2: $e")
                    null
                }
            }


            val output = job.await()


            if (mLastError != null) {
                if (mLastError is GooglePlayServicesAvailabilityIOException) {
                    showGooglePlayServicesAvailabilityErrorDialog(
                        (mLastError as GooglePlayServicesAvailabilityIOException)
                            .connectionStatusCode
                    )
                } else if (mLastError is UserRecoverableAuthIOException) {
                    startActivityForResult(
                        (mLastError as UserRecoverableAuthIOException).intent,
                        REQUEST_AUTHORIZATION
                    )
                } else if (mLastError is GoogleAuthIOException) {
                    Log.e("GoogleAuthIOException", "Authentication failed: ${(mLastError as GoogleAuthIOException).message}")
                    binding.txtOut.text = "Authentication failed: " + mLastError
                } else {
                    binding.txtOut.text =
                        "The following error occurred: " + mLastError
                }
            } else {
                binding.txtOut.text = "Request cancelled."
            }

            mProgress!!.hide()
            if (output == null || output.size == 0) {
                Log.d("Google", "output is null or output size is null")
            } else {
                for (index in 0 until output.size) {
                    binding.txtOut.text = (TextUtils.join("\n", output))
                    Log.d(
                        "Google",
                        output[index].id.toString() + " " + output[index].summary + " " + output[index].startDate
                    )
                }
            }
        }
    }

    private fun getDataFromCalendar(): MutableList<GetEventModel> {


        val now = DateTime(System.currentTimeMillis())
        val eventStrings = ArrayList<GetEventModel>()

        val request = mService!!.events().list("primary")
            .setMaxResults(10)
            .setTimeMin(now)
            .setOrderBy("startTime")
            .setSingleEvents(true)
        //Log.d("Token", "${mCredential!!.token}")
        Log.d("API Request", "Request URL: ${request.buildHttpRequest().url}")
        val events = request.execute()
        val items = events.items


        for (event in items) {
            var start = event.start.dateTime
            if (start == null) {
                start = event.start.date
            }

            eventStrings.add(
                GetEventModel(
                    summary = event.summary,
                    startDate = start.toString()
                )
            )
        }
        return eventStrings
    }
}