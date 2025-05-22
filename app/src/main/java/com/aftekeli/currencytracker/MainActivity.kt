package com.aftekeli.currencytracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aftekeli.currencytracker.data.repository.ThemeRepository
import com.aftekeli.currencytracker.ui.navigation.AppNavigation
import com.aftekeli.currencytracker.ui.theme.CurrencyTrackerTheme
import com.aftekeli.currencytracker.ui.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var themeRepository: ThemeRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeSettingFlow = themeRepository.themeSetting
            val themeSetting by themeSettingFlow.collectAsState(initial = com.aftekeli.currencytracker.data.repository.ThemeSetting.SYSTEM)

            CurrencyTrackerTheme(themeSetting = themeSetting) {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val authViewModel: AuthViewModel = hiltViewModel()
                    AppNavigation(viewModel = authViewModel)
                }
            }
        }
    }
}