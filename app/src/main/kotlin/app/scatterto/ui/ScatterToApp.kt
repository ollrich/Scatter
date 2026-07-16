package app.scatterto.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.scatterto.ui.about.AboutScreen
import app.scatterto.ui.display.DisplayScreen
import app.scatterto.ui.log.LogScreen
import app.scatterto.ui.main.MainScreen
import app.scatterto.ui.settings.AccountsScreen
import app.scatterto.ui.settings.AiScreen

/** App-Navigation: Hauptseite + Einstellungs-Unterseiten, erreichbar über das Slide-Panel (§3). */
@Composable
fun ScatterToApp(
    sharedText: String?,
    sharedSubject: String?,
    onSharedConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    val back: () -> Unit = { navController.popBackStack() }

    // Ein neuer Share gehört IMMER auf die Einstiegsseite (§12.2 Nr. 8) — „Teilen → Scatter" ist
    // bereits die Anweisung, dafür braucht es keine Rückfrage. Der Sprung muss hier oben passieren:
    // MainScreen ist auf einer Unterseite gar nicht komponiert, sein Share-Effekt liefe also nie.
    LaunchedEffect(sharedText) {
        if (sharedText != null) navController.popBackStack(Routes.MAIN, inclusive = false)
    }

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
        composable(Routes.AI) { AiScreen(onBack = back) }
        composable(Routes.DISPLAY) { DisplayScreen(onBack = back) }
        composable(Routes.LOG) { LogScreen(onBack = back) }
        composable(Routes.ABOUT) { AboutScreen(onBack = back) }
    }
}

object Routes {
    const val MAIN = "main"
    const val ACCOUNTS = "accounts"
    const val AI = "ai"
    const val DISPLAY = "display"
    const val LOG = "log"
    const val ABOUT = "about"
}
