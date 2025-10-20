package com.example.words.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.words.screens.GameScreen
import com.example.words.screens.StartScreen
import com.example.words.screens.WinScreen

@Composable
fun NavGraph() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "start") {
        composable("start") { StartScreen(navController) }
        composable("game") { GameScreen(navController) }
        composable("win") { WinScreen(navController) }
    }
}