package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.data.MyDatabaseProvider
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.ResumeViewModel
import com.example.ui.ResumeViewModelFactory
import com.example.ui.ScreenEditProfile
import com.example.ui.ScreenHome
import com.example.ui.ScreenPreviewCV

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        enableEdgeToEdge()

        // Safely retrieve the Room database repository
        val repository = MyDatabaseProvider.getRepository(applicationContext)
        val viewModelFactory = ResumeViewModelFactory(repository)
        val viewModel = ViewModelProvider(this, viewModelFactory)[ResumeViewModel::class.java]

        setContent {
            MyApplicationTheme(darkTheme = viewModel.isDarkMode) {
                val navController = rememberNavController()
                
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "home",
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding)
                    ) {
                        composable("home") {
                            ScreenHome(
                                viewModel = viewModel,
                                onNavigateToEdit = {
                                    navController.navigate("edit")
                                },
                                onNavigateToPreview = {
                                    navController.navigate("preview")
                                }
                            )
                        }

                        composable("edit") {
                            ScreenEditProfile(
                                viewModel = viewModel,
                                onNavigateToPreview = {
                                    navController.navigate("preview")
                                },
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }

                        composable("preview") {
                            ScreenPreviewCV(
                                viewModel = viewModel,
                                onBack = {
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
