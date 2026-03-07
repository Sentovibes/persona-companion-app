package com.persona.companion.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.persona.companion.ui.screens.CategoryScreen
import com.persona.companion.ui.screens.FusionScreen
import com.persona.companion.ui.screens.GameSelectionScreen
import com.persona.companion.ui.screens.HomeScreen
import com.persona.companion.ui.screens.PersonaDetailScreen
import com.persona.companion.ui.screens.PersonaListScreen
import com.persona.companion.ui.screens.SettingsScreen

// ---------------------------------------------------------------------------
// Route definitions
// ---------------------------------------------------------------------------

sealed class Screen(val route: String) {

    object Home : Screen("home")
    
    object Settings : Screen("settings")

    object GameSelection : Screen("game_selection/{seriesId}") {
        fun createRoute(seriesId: String) = "game_selection/$seriesId"
    }

    object Category : Screen("category/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "category/$seriesId/$gameId"
    }

    object PersonaList : Screen("persona_list/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "persona_list/$seriesId/$gameId"
    }

    object PersonaDetail : Screen("persona_detail/{seriesId}/{gameId}/{personaName}") {
        fun createRoute(seriesId: String, gameId: String, personaName: String): String {
            // Encode slashes so they don't break the route
            val safeName = personaName.replace("/", "|")
            return "persona_detail/$seriesId/$gameId/$safeName"
        }
    }
    
    object Fusion : Screen("fusion/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "fusion/$seriesId/$gameId"
    }
}

// ---------------------------------------------------------------------------
// NavHost
// ---------------------------------------------------------------------------

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route
    ) {

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(
            route = Screen.GameSelection.route,
            arguments = listOf(navArgument("seriesId") { type = NavType.StringType })
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            GameSelectionScreen(navController, seriesId)
        }

        composable(
            route = Screen.Category.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            CategoryScreen(navController, seriesId, gameId)
        }

        composable(
            route = Screen.PersonaList.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            PersonaListScreen(navController, seriesId, gameId)
        }

        composable(
            route = Screen.PersonaDetail.route,
            arguments = listOf(
                navArgument("seriesId")    { type = NavType.StringType },
                navArgument("gameId")      { type = NavType.StringType },
                navArgument("personaName") { type = NavType.StringType }
            )
        ) { back ->
            val seriesId    = back.arguments?.getString("seriesId")    ?: return@composable
            val gameId      = back.arguments?.getString("gameId")      ?: return@composable
            val personaName = back.arguments?.getString("personaName") ?: return@composable
            PersonaDetailScreen(navController, seriesId, gameId, personaName.replace("|", "/"))
        }
        
        composable(
            route = Screen.Fusion.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            
            // Get the data path for this game
            val game = com.persona.companion.data.SeriesData.findGame(seriesId, gameId)
            val dataPath = game?.dataPath ?: return@composable
            
            FusionScreen(
                seriesId = seriesId,
                gameId = gameId,
                dataPath = dataPath,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
