package gonzalez.tomas.pdfreadertomas.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import gonzalez.tomas.pdfreadertomas.ui.screens.biblioteca.BibliotecaScreen
import gonzalez.tomas.pdfreadertomas.ui.screens.importar.ImportarPdfScreen
import gonzalez.tomas.pdfreadertomas.ui.screens.lector.LectorScreen

/**
 * Componente principal de navegación para la aplicación
 */
@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "biblioteca"
    ) {
        // Pantalla de Biblioteca (principal)
        composable("biblioteca") {
            BibliotecaScreen(
                onDocumentClick = { documentId ->
                    // Usar launchSingleTop para evitar múltiples instancias del lector
                    navController.navigate("lector/$documentId") {
                        launchSingleTop = true
                    }
                },
                onImportClick = {
                    navController.navigate("importar") {
                        launchSingleTop = true
                    }
                }
            )
        }

        // Pantalla de Lector PDF
        composable(
            route = "lector/{documentId}",
            arguments = listOf(
                navArgument("documentId") { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val documentId = backStackEntry.arguments?.getLong("documentId") ?: 0
            LectorScreen(
                documentId = documentId,
                onNavigateBack = {
                    // Navegar hacia atrás de forma segura
                    if (navController.previousBackStackEntry != null) {
                        navController.popBackStack()
                    } else {
                        // Si no hay entrada anterior, ir a biblioteca
                        navController.navigate("biblioteca") {
                            popUpTo("biblioteca") { inclusive = true }
                            launchSingleTop = true
                        }
                    }
                }
            )
        }

        // Pantalla de Importación
        composable("importar") {
            ImportarPdfScreen(
                onBackClick = {
                    navController.popBackStack()
                },
                onImportSuccess = {
                    navController.popBackStack()
                }
            )
        }
    }
}
