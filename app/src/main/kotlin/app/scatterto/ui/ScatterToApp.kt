package app.scatterto.ui

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import app.scatterto.ui.main.MainScreen
import app.scatterto.ui.settings.SettingsScreen

/** App-Navigation: Hauptseite und Einstellungen (§3). */
@Composable
fun ScatterToApp(
    sharedText: String?,
    sharedSubject: String?,
    onSharedConsumed: () -> Unit,
) {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = "main") {
        composable("main") {
            MainScreen(
                sharedText = sharedText,
                sharedSubject = sharedSubject,
                onSharedConsumed = onSharedConsumed,
                onOpenSettings = { navController.navigate("settings") },
            )
        }
        composable("settings") {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
