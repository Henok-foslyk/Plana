package com.example.plana

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import com.example.plana.navigation.CalendarScreenRoute
import com.example.plana.navigation.TasksScreenRoute
import com.example.plana.ui.screen.calendar.CalendarScreen
import com.example.plana.ui.screen.tasks.TasksScreen
import com.example.plana.ui.theme.PlanaTheme
import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class MainActivity : GoogleSignInActivity(){
    private var contentDrawn = false

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
                Scaffold(Modifier.fillMaxSize()) { innerPadding ->
                    MainNavigation(Modifier.padding(innerPadding))
                }
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


//    override fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
//        super.onSignInResult(result)
//        val user = FirebaseAuth.getInstance().currentUser
//        if (user != null) {
//            // Re-launch content after successful sign-in
////            setContent {
////                ...
////            }
//        } else {
//            // Show error or retry
//        }
//    }
}


@Composable
fun MainNavigation(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = CalendarScreenRoute
    )
    {
        composable<CalendarScreenRoute> {
            CalendarScreen(
//                onMoneyAPISelected = {
//                    navController.navigate(MoneyScreenRoute)
//                },
//                onNasaMarsAPISelected = {
//                    navController.navigate(NasaScreenRoute)
//                }
            )
        }
        composable<TasksScreenRoute> {
            TasksScreen()
        }

    }
}