package app.scatterto.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.scatterto.ui.about.AboutScreen
import app.scatterto.ui.display.DisplayScreen
import app.scatterto.ui.log.LogScreen
import app.scatterto.ui.main.MainScreen
import app.scatterto.ui.settings.AccountsScreen
import app.scatterto.ui.settings.MammouthScreen

/** App-Navigation: Hauptseite + Einstellungs-Unterseiten, erreichbar über das Slide-Panel (§3). */
@Composable
fun ScatterToApp(
    sharedText: String?,
    sharedSubject: String?,
    onSharedConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }

    NavHost(navController = navController, startDestination = Routes.MAIN) {
        composable(Routes.MAIN) {
            MainScreen(
                sharedText = sharedText,
                sharedSubject = sharedSubject,
                onSharedConsumed = onSharedConsumed,
                onOpen = { route -> navController.navigate(route) },
            )
        }
        composable(Routes.ACCOUNTS) { AccountsScreen(onBack = back) }
        composable(Routes.MAMMOUTH) { MammouthScreen(onBack = back) }
        composable(Routes.DISPLAY) { DisplayScreen(onBack = back) }
        composable(Routes.LOG) { LogScreen(onBack = back) }
        composable(Routes.ABOUT) { AboutScreen(onBack = back) }
    }
}

object Routes {
    const val MAIN = "main"
    const val ACCOUNTS = "accounts"
    const val MAMMOUTH = "mammouth"
    const val DISPLAY = "display"
    const val LOG = "log"
    const val ABOUT = "about"
}
