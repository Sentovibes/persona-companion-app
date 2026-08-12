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
import com.persona.companion.ui.screens.SkillListScreen
import com.persona.companion.ui.screens.ItemListScreen
import com.persona.companion.ui.screens.GuidesHubScreen
import com.persona.companion.ui.screens.QuestGuideScreen
import com.persona.companion.ui.screens.BossGuideScreen
import com.persona.companion.ui.screens.BossDetailScreen
import com.persona.companion.ui.screens.DayByDayGuideScreen
import com.persona.companion.ui.screens.NewsUpdatesScreen

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
            val safeName = java.net.URLEncoder.encode(personaName, "UTF-8")
            return "persona_detail/$seriesId/$gameId/$safeName"
        }
    }
    
    object Fusion : Screen("fusion/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "fusion/$seriesId/$gameId"
    }
    
    object EnemyList : Screen("enemy_list/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "enemy_list/$seriesId/$gameId"
    }
    
    object EnemyDetail : Screen("enemy_detail/{seriesId}/{gameId}/{enemyName}/{enemyArea}") {
        fun createRoute(seriesId: String, gameId: String, name: String, area: String): String {
            val safeName = java.net.URLEncoder.encode(name, "UTF-8")
            val safeArea = java.net.URLEncoder.encode(area, "UTF-8")
            return "enemy_detail/$seriesId/$gameId/$safeName/$safeArea"
        }
    }
    
    object SocialLinks : Screen("social_links/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "social_links/$seriesId/$gameId"
    }
    
    object SocialLinkDetail : Screen("social_link_detail/{seriesId}/{gameId}/{arcana}") {
        fun createRoute(seriesId: String, gameId: String, arcana: String): String {
            val safeArcana = java.net.URLEncoder.encode(arcana, "UTF-8")
            return "social_link_detail/$seriesId/$gameId/$safeArcana"
        }
    }
    
    object ClassroomAnswers : Screen("classroom_answers/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "classroom_answers/$seriesId/$gameId"
    }

    object SkillList : Screen("skill_list/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "skill_list/$seriesId/$gameId"
    }

    object ItemList : Screen("item_list/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "item_list/$seriesId/$gameId"
    }

    object RequestList : Screen("request_list/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "request_list/$seriesId/$gameId"
    }

    object GuidesHub : Screen("guides_hub/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "guides_hub/$seriesId/$gameId"
    }

    object QuestGuide : Screen("quest_guide/{seriesId}/{gameId}/{giver}") {
        fun createRoute(seriesId: String, gameId: String, giver: String): String {
            val safeGiver = java.net.URLEncoder.encode(giver, "UTF-8")
            return "quest_guide/$seriesId/$gameId/$safeGiver"
        }
    }

    object BossGuide : Screen("boss_guide/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "boss_guide/$seriesId/$gameId"
    }

    object BossDetail : Screen("boss_detail/{seriesId}/{gameId}/{bossName}") {
        fun createRoute(seriesId: String, gameId: String, bossName: String): String {
            val safeName = java.net.URLEncoder.encode(bossName, "UTF-8")
            return "boss_detail/$seriesId/$gameId/$safeName"
        }
    }

    object DayGuide : Screen("day_guide/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "day_guide/$seriesId/$gameId"
    }

    object NewsUpdates : Screen("news_updates/{seriesId}/{gameId}") {
        fun createRoute(seriesId: String, gameId: String) = "news_updates/$seriesId/$gameId"
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
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
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
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
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
            val personaNameRaw = back.arguments?.getString("personaName") ?: return@composable
            val personaName = java.net.URLDecoder.decode(personaNameRaw, "UTF-8")
            PersonaDetailScreen(navController, seriesId, gameId, personaName)
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
                onBack = { navController.popBackStack() },
                onPersonaClick = { name ->
                    navController.navigate(Screen.PersonaDetail.createRoute(seriesId, gameId, name))
                }
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
                    navController.navigate(Screen.EnemyDetail.createRoute(seriesId, gameId, enemy.name, enemy.area))
                }
            )
        }
        
        composable(
            route = Screen.EnemyDetail.route,
            arguments = listOf(
                navArgument("seriesId")  { type = NavType.StringType },
                navArgument("gameId")    { type = NavType.StringType },
                navArgument("enemyName") { type = NavType.StringType },
                navArgument("enemyArea") { type = NavType.StringType }
            )
        ) { back ->
            val seriesId    = back.arguments?.getString("seriesId")  ?: return@composable
            val gameId      = back.arguments?.getString("gameId")    ?: return@composable
            val enemyNameRaw = back.arguments?.getString("enemyName") ?: return@composable
            val enemyAreaRaw = back.arguments?.getString("enemyArea") ?: return@composable
            
            val enemyName = java.net.URLDecoder.decode(enemyNameRaw, "UTF-8")
            val enemyArea = java.net.URLDecoder.decode(enemyAreaRaw, "UTF-8")

            EnemyDetailScreen(
                seriesId = seriesId,
                gameId = gameId,
                enemyName = enemyName,
                enemyArea = enemyArea,
                onBack = { navController.popBackStack() }
            )
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
            val arcanaRaw = back.arguments?.getString("arcana")   ?: return@composable
            val arcana = java.net.URLDecoder.decode(arcanaRaw, "UTF-8")
            
            SocialLinkDetailScreen(
                gameId = gameId,
                arcana = arcana,
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

        composable(
            route = Screen.SkillList.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            
            val game = com.persona.companion.data.SeriesData.findGame(seriesId, gameId)
            
            SkillListScreen(
                gameId = gameId,
                gameName = game?.title ?: "",
                skillPath = game?.skillPath,
                personaPath = game?.dataPath,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.ItemList.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            
            ItemListScreen(
                seriesId = seriesId,
                gameId = gameId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.RequestList.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")   ?: return@composable
            
            val game = com.persona.companion.data.SeriesData.findGame(seriesId, gameId)
            
            com.persona.companion.ui.screens.RequestListScreen(
                seriesId = seriesId,
                gameId = gameId,
                gameTitle = game?.title ?: "",
                requestPath = game?.requestPath,
                aigisRequestPath = game?.aigisRequestPath,
                onBack = { navController.popBackStack() },
                onEnemyClick = { enemyName ->
                    navController.navigate(Screen.EnemyDetail.createRoute(seriesId, gameId, enemyName, "Unknown"))
                }
            )
        }

        composable(
            route = Screen.GuidesHub.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
            GuidesHubScreen(
                seriesId = seriesId,
                gameId = gameId,
                onBack = { navController.popBackStack() },
                onNavigateToQuestGuide = { giver ->
                    navController.navigate(Screen.QuestGuide.createRoute(seriesId, gameId, giver))
                },
                onNavigateToBossGuide = {
                    navController.navigate(Screen.BossGuide.createRoute(seriesId, gameId))
                },
                onNavigateToDayGuide = {
                    navController.navigate(Screen.DayGuide.createRoute(seriesId, gameId))
                }
            )
        }

        composable(
            route = Screen.DayGuide.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
            DayByDayGuideScreen(
                seriesId = seriesId,
                gameId = gameId,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.QuestGuide.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType },
                navArgument("giver")    { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
            val giverRaw = back.arguments?.getString("giver")     ?: return@composable
            val giver    = java.net.URLDecoder.decode(giverRaw, "UTF-8")
            QuestGuideScreen(
                seriesId = seriesId,
                gameId = gameId,
                giver = giver,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.BossGuide.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
            BossGuideScreen(
                seriesId = seriesId,
                gameId = gameId,
                onBack = { navController.popBackStack() },
                onBossClick = { bossName ->
                    navController.navigate(Screen.BossDetail.createRoute(seriesId, gameId, bossName))
                }
            )
        }

        composable(
            route = Screen.BossDetail.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType },
                navArgument("bossName") { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
            val bossNameRaw = back.arguments?.getString("bossName") ?: return@composable
            val bossName = java.net.URLDecoder.decode(bossNameRaw, "UTF-8")
            BossDetailScreen(
                seriesId = seriesId,
                gameId = gameId,
                bossName = bossName,
                onBack = { navController.popBackStack() }
            )
        }

        composable(
            route = Screen.NewsUpdates.route,
            arguments = listOf(
                navArgument("seriesId") { type = NavType.StringType },
                navArgument("gameId")   { type = NavType.StringType }
            )
        ) { back ->
            val seriesId = back.arguments?.getString("seriesId") ?: return@composable
            val gameId   = back.arguments?.getString("gameId")    ?: return@composable
            NewsUpdatesScreen(
                seriesId = seriesId,
                gameId = gameId,
                onBack = { navController.popBackStack() }
            )
        }
    }
}
