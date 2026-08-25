package com.jrblanco.calculadoradejoyeros2021.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jrblanco.calculadoradejoyeros2021.R
import com.jrblanco.calculadoradejoyeros2021.ui.components.JewelryBottomBar
import com.jrblanco.calculadoradejoyeros2021.ui.components.MainTab
import com.jrblanco.calculadoradejoyeros2021.ui.herramientas.HerramientasScreen
import com.jrblanco.calculadoradejoyeros2021.ui.home.HomeModule
import com.jrblanco.calculadoradejoyeros2021.ui.home.HomeScreen
import com.jrblanco.calculadoradejoyeros2021.ui.info.InfoScreen
import com.jrblanco.calculadoradejoyeros2021.ui.oro.OroScreen
import com.jrblanco.calculadoradejoyeros2021.ui.placeholder.PlaceholderScreen
import com.jrblanco.calculadoradejoyeros2021.ui.plata.PlataScreen
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.SoldaduraBaseScreen
import com.jrblanco.calculadoradejoyeros2021.ui.soldaduras.SoldadurasScreen
import com.jrblanco.calculadoradejoyeros2021.ui.welcome.WelcomeScreen

@Composable
fun AppNavHost(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController(),
) {
    // Saltar entre las tres zonas principales no debe apilar historial: se vuelve a
    // Home y se evita duplicar la zona si ya se está en ella.
    fun goToTab(tab: MainTab) {
        navController.navigate(tab.route) {
            popUpTo(Route.Home) { inclusive = tab == MainTab.HOME }
            launchSingleTop = true
        }
    }

    // launchSingleTop blinda la doble pulsación rápida sobre una tarjeta.
    fun goTo(route: Route) {
        navController.navigate(route) { launchSingleTop = true }
    }

    val onInfo = { goTo(Route.AcercaDe) }
    val onBack = { navController.popBackStack(); Unit }

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
                        launchSingleTop = true
                    }
                },
            )
        }

        // --- Zonas principales: llevan barra inferior ---

        composable<Route.Home> {
            HomeScreen(
                onModuleClick = { module -> goTo(module.route) },
                onTabSelect = ::goToTab,
                onInfo = onInfo,
            )
        }

        composable<Route.Favoritos> {
            PlaceholderScreen(
                title = stringResource(R.string.nav_favoritos),
                analyticsName = "favoritos",
                onInfo = onInfo,
                bottomBar = {
                    JewelryBottomBar(selected = MainTab.FAVORITOS, onSelect = ::goToTab)
                },
            )
        }

        composable<Route.Ajustes> {
            PlaceholderScreen(
                title = stringResource(R.string.nav_ajustes),
                analyticsName = "ajustes",
                onInfo = onInfo,
                bottomBar = {
                    JewelryBottomBar(selected = MainTab.AJUSTES, onSelect = ::goToTab)
                },
            )
        }

        // --- Secciones de módulo: pantalla completa, con flecha de retroceso ---

        composable<Route.Oro> {
            OroScreen(onInfo = onInfo, onBack = onBack)
        }
        composable<Route.Plata> {
            PlataScreen(onInfo = onInfo, onBack = onBack)
        }
        composable<Route.Soldaduras> {
            SoldadurasScreen(
                onInfo = onInfo,
                onBack = onBack,
                onSoldaduraBase = { goTo(Route.SoldaduraBase) },
            )
        }
        composable<Route.SoldaduraBase> {
            SoldaduraBaseScreen(onInfo = onInfo, onBack = onBack)
        }
        composable<Route.Herramientas> {
            HerramientasScreen(onInfo = onInfo, onBack = onBack)
        }

        // --- Otros ---

        composable<Route.AcercaDe> {
            InfoScreen(onBack = onBack)
        }
    }
}

/** Destino de cada pestaña de la barra inferior. */
private val MainTab.route: Route
    get() = when (this) {
        MainTab.HOME -> Route.Home
        MainTab.FAVORITOS -> Route.Favoritos
        MainTab.AJUSTES -> Route.Ajustes
    }

/** Destino de cada módulo del menú. */
private val HomeModule.route: Route
    get() = when (this) {
        HomeModule.ORO -> Route.Oro
        HomeModule.PLATA -> Route.Plata
        HomeModule.SOLDADURAS -> Route.Soldaduras
        HomeModule.HERRAMIENTAS -> Route.Herramientas
    }
