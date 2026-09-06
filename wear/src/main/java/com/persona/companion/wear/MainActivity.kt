package com.persona.companion.wear

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.persona.companion.wear.models.WEAR_GAMES
import com.persona.companion.wear.models.WearGame
import com.persona.companion.wear.ui.screens.ClassroomScreen
import com.persona.companion.wear.ui.screens.EnemyListScreen
import com.persona.companion.wear.ui.screens.GameSelectScreen
import com.persona.companion.wear.ui.screens.MainHubScreen
import com.persona.companion.wear.ui.screens.SocialLinkListScreen
import com.persona.companion.wear.ui.theme.PersonaWearTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            PersonaWearTheme {
                val navController = rememberSwipeDismissableNavController()
                var selectedGame by remember { mutableStateOf<WearGame>(WEAR_GAMES.first()) }

                SwipeDismissableNavHost(
                    navController = navController,
                    startDestination = "hub"
                ) {
                    composable("hub") {
                        MainHubScreen(
                            selectedGame = selectedGame,
                            onNavigateToClassroom = { navController.navigate("classroom") },
                            onNavigateToEnemies = { navController.navigate("enemies") },
                            onNavigateToSocialLinks = { navController.navigate("social_links") },
                            onNavigateToGameSelect = { navController.navigate("game_select") }
                        )
                    }

                    composable("game_select") {
                        GameSelectScreen(
                            currentGameId = selectedGame.id,
                            onGameSelected = { game ->
                                selectedGame = game
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("classroom") {
                        ClassroomScreen(
                            gameId = selectedGame.id
                        )
                    }

                    composable("enemies") {
                        EnemyListScreen(
                            gameId = selectedGame.id
                        )
                    }

                    composable("social_links") {
                        SocialLinkListScreen(
                            gameId = selectedGame.id
                        )
                    }
                }
            }
        }
    }
}
