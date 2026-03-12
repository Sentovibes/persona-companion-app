package com.persona.companion.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.persona.companion.ui.screens.CategoryScreen
import com.persona.companion.ui.screens.ClassroomAnswersScreen
import com.persona.companion.ui.screens.EnemyDetailScreen
import com.persona.companion.ui.screens.EnemyListScreen
import com.persona.companion.ui.screens.FavoritesScreen
import com.persona.companion.ui.screens.FusionScreen
import com.persona.companion.ui.screens.GameSelectionScreen
import com.persona.companion.ui.screens.HomeScreen
import com.persona.companion.ui.screens.PersonaDetailScreen
import com.persona.companion.ui.screens.PersonaListScreen
import com.persona.companion.ui.screens.RecentlyViewedScreen
import com.persona.companion.ui.screens.SettingsScreen
import com.persona.companion.ui.screens.SocialLinkDetailScreen
import com.persona.companion.ui.screens.SocialLinksScreen

// ---------------------------------------------------------------------------
// Route definitions
// ---------------------------------------------------------------------------

sealed class Screen(val route: String) {

    object Home : Screen("home")
    
    object Settings : Screen("settings")
    
    object Favorites : Screen("favorites")
    
    object RecentlyViewed : Screen("recently_viewed")

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
    
    object EnemyList : Screen("enemy_list/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "enemy_list/$seriesId/$gameId"
    }
    
    object EnemyDetail : Screen("enemy_detail/{seriesId}/{gameId}/{enemyName}") {
        fun createRoute(seriesId: String, gameId: String, enemyName: String): String {
            val safeName = enemyName.replace("/", "|")
            return "enemy_detail/$seriesId/$gameId/$safeName"
        }
    }
    
    object SocialLinks : Screen("social_links/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "social_links/$seriesId/$gameId"
    }
    
    object SocialLinkDetail : Screen("social_link_detail/{seriesId}/{gameId}/{arcana}") {
        fun createRoute(seriesId: String, gameId: String, arcana: String): String {
            val safeArcana = arcana.replace("/", "|")
            return "social_link_detail/$seriesId/$gameId/$safeArcana"
        }
    }
    
    object ClassroomAnswers : Screen("classroom_answers/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "classroom_answers/$seriesId/$gameId"
    }
}

// ---------------------------------------------------------------------------
// NavHost
// ---------------------------------------------------------------------------

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Home.route,
        enterTransition = { androidx.compose.animation.EnterTransition.None },
        exitTransition = { androidx.compose.animation.ExitTransition.None },
        popEnterTransition = { androidx.compose.animation.EnterTransition.None },
        popExitTransition = { androidx.compose.animation.ExitTransition.None }
    ) {

        composable(Screen.Home.route) {
            HomeScreen(navController)
        }
        
        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }
        
        composable(Screen.Favorites.route) {
            FavoritesScreen(navController)
        }
        
        composable(Screen.RecentlyViewed.route) {
            RecentlyViewedScreen(navController)
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
        
        composable(
            route = Screen.EnemyList.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            
            val game = com.persona.companion.data.SeriesData.findGame(seriesId, gameId)
            
            EnemyListScreen(
                seriesId = seriesId,
                gameId = gameId,
                gameTitle = game?.title ?: "",
                enemyPath = game?.enemyPath,
                aigisEnemyPath = game?.aigisEnemyPath,
                onBack = { navController.popBackStack() },
                onEnemyClick = { enemy ->
                    navController.navigate(Screen.EnemyDetail.createRoute(seriesId, gameId, enemy.name))
                }
            )
        }
        
        composable(
            route = Screen.EnemyDetail.route,
            arguments = listOf(
                navArgument("seriesId")  { type = NavType.StringType },
                navArgument("gameId")    { type = NavType.StringType },
                navArgument("enemyName") { type = NavType.StringType }
            )
        ) { back ->
            val seriesId  = back.arguments?.getString("seriesId")  ?: return@composable
            val gameId    = back.arguments?.getString("gameId")    ?: return@composable
            val enemyName = back.arguments?.getString("enemyName") ?: return@composable
            
            val game = com.persona.companion.data.SeriesData.findGame(seriesId, gameId)
            val enemies = if (game?.enemyPath != null) {
                com.persona.companion.utils.JsonLoader.loadEnemies(
                    androidx.compose.ui.platform.LocalContext.current,
                    game.enemyPath
                )
            } else emptyList()
            
            val enemy = enemies.find { it.name.replace("/", "|") == enemyName }
            
            if (enemy != null) {
                EnemyDetailScreen(
                    enemy = enemy,
                    gameId = gameId,
                    onBack = { navController.popBackStack() }
                )
            }
        }
        
        composable(
            route = Screen.SocialLinks.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            
            val game = com.persona.companion.data.SeriesData.findGame(seriesId, gameId)
            
            SocialLinksScreen(
                gameId = gameId,
                gameName = game?.title ?: "",
                onBack = { navController.popBackStack() },
                onSocialLinkClick = { arcana ->
                    navController.navigate(Screen.SocialLinkDetail.createRoute(seriesId, gameId, arcana))
                }
            )
        }
        
        composable(
            route = Screen.SocialLinkDetail.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType },
                navArgument("arcana")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            val arcana   = back.arguments?.getString("arcana")   ?: return@composable
            
            SocialLinkDetailScreen(
                gameId = gameId,
                arcana = arcana.replace("|", "/"),
                onBack = { navController.popBackStack() }
            )
        }
        
        composable(
            route = Screen.ClassroomAnswers.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            
            val game = com.persona.companion.data.SeriesData.findGame(seriesId, gameId)
            
            ClassroomAnswersScreen(
                gameId = gameId,
                gameName = game?.title ?: "",
                onBack = { navController.popBackStack() }
            )
        }
    }
}
