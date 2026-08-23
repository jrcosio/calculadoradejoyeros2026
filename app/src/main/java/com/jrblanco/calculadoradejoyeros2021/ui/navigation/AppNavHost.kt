package com.jrblanco.calculadoradejoyeros2021.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jrblanco.calculadoradejoyeros2021.ui.home.HomeScreen
import com.jrblanco.calculadoradejoyeros2021.ui.welcome.WelcomeScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    NavHost(
        navController = navController,
        startDestination = Route.Welcome,
        modifier = modifier,
    ) {
        composable<Route.Welcome> {
            WelcomeScreen(
                onStart = {
                    navController.navigate(Route.Home) {
                        // La portada sale del historial: atrás desde la home cierra la
                        // app en vez de volver aquí.
                        popUpTo(Route.Welcome) { inclusive = true }
                        // Blinda contra la doble pulsación rápida, que si no encolaría
                        // dos navegaciones.
                        launchSingleTop = true
                    }
                },
            )
        }

        composable<Route.Home> {
            HomeScreen()
        }
    }
}
