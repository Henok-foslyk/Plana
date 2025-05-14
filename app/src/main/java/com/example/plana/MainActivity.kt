package com.example.plana

import android.content.Context
import androidx.lifecycle.viewmodel.compose.viewModel
import android.os.Build
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.plana.data.EventItem
import dagger.hilt.android.AndroidEntryPoint
import com.example.plana.navigation.CalendarScreenRoute
import com.example.plana.navigation.TasksScreenRoute
import com.example.plana.ui.screen.CalendarViewModel
import com.example.plana.ui.screen.calendar.CalendarScreen
import com.example.plana.ui.screen.tasks.TasksScreen
import com.example.plana.ui.theme.PlanaTheme
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : GoogleSignInActivity(){
    private var contentDrawn = false

    @RequiresApi(Build.VERSION_CODES.O)
    override fun updateUI(user: FirebaseUser?) {
        if (user == null) {                // signed‑out → do nothing for now
            contentDrawn = false
            return
        }
        if (contentDrawn) return           // already showing UI

        contentDrawn = true
        enableEdgeToEdge()



        setContent {
            PlanaTheme {
                var events by remember { mutableStateOf<List<EventItem>?>(null) }

                RequireContactsPermission(
                    onGranted = { context ->
                        lifecycleScope.launch {
                            events = user.email?.let { listEvents(context, it) }
                        }
                    }
                )
                if (events != null) {
                    Scaffold(Modifier.fillMaxSize()) { innerPadding ->
                        MainNavigation(Modifier.padding(innerPadding), events = events)
                    }
                } else {
                    Box(
                        Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) { CircularProgressIndicator() }
                }



            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }
}


@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
    events: List<EventItem>?,
    viewModel: CalendarViewModel = viewModel()
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = CalendarScreenRoute

    )
    {
        composable<CalendarScreenRoute> {
            CalendarScreen(navController, viewModel, events = events)
        }
        composable<TasksScreenRoute> {
            TasksScreen(viewModel)
        }

    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun RequireContactsPermission(
    onGranted: (Context) -> Unit,          // run this once everything is allowed
    deniedContent: @Composable () -> Unit = {
        /* what to show when permanently denied */
        Text(
            "To connect your Google Calendar we need access to the account " +
                    "on this device. You can enable it in Settings ➜ Apps ➜ Permissions."
        )
    },
    rationaleContent: @Composable (/* retry */ () -> Unit) -> Unit = { retry ->
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                "Plana needs permission to read the Google account on your " +
                        "phone so it can talk to Google Calendar."
            )
            Spacer(Modifier.height(12.dp))
            Button(onClick = retry) { Text("Grant permission") }
        }
    }
) {
    val context = LocalContext.current
    val permissionsState = rememberMultiplePermissionsState(
        listOf(
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.GET_ACCOUNTS
        )
    )

    LaunchedEffect (Unit) {
        // shows the system dialog the first time
        permissionsState.launchMultiplePermissionRequest()
    }

    when {
        permissionsState.allPermissionsGranted -> onGranted(context)

        permissionsState.shouldShowRationale -> {
            rationaleContent { permissionsState.launchMultiplePermissionRequest() }
        }

    }
}