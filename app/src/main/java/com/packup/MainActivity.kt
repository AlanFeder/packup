package com.packup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.packup.ui.screens.FamilySetupScreen
import com.packup.ui.screens.PackingScreen
import com.packup.ui.theme.PackUpTheme
import com.packup.viewmodel.AppStartupState
import com.packup.viewmodel.StartupViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PackUpTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot(
    startupViewModel: StartupViewModel = hiltViewModel()
) {
    val state by startupViewModel.state.collectAsStateWithLifecycle()

    when (state) {
        is AppStartupState.Loading -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        }
        is AppStartupState.NeedsFamilySetup -> {
            FamilySetupScreen(
                onSetupComplete = { startupViewModel.onFamilySetupComplete() }
            )
        }
        is AppStartupState.Ready -> {
            PackingScreen()
        }
        is AppStartupState.Error -> {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(
                    text = (state as AppStartupState.Error).message,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
