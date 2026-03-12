package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persona.companion.data.PersonaRepository
import com.persona.companion.data.SeriesData
import com.persona.companion.data.UserPreferences
import com.persona.companion.models.Enemy
import com.persona.companion.models.Persona
import com.persona.companion.navigation.Screen
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.JsonLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavoritesScreen(navController: NavController) {
    val context = LocalContext.current
    val userPrefs = remember { UserPreferences(context) }
    val personaRepo = remember { PersonaRepository(context) }
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Personas", "Enemies")
    
    // Load favorite personas
    val favoritePersonaIds = remember { userPrefs.getFavoritePersonas() }
    val favoritePersonas = remember(favoritePersonaIds) {
        favoritePersonaIds.mapNotNull { id ->
            // Parse ID format: "seriesId_gameId_personaName"
            val parts = id.split("_", limit = 3)
            if (parts.size == 3) {
                val (seriesId, gameId, name) = parts
                val game = SeriesData.findGame(seriesId, gameId)
                game?.let {
                    personaRepo.getPersonaByName(it.dataPath, name)?.let { persona ->
                        Triple(seriesId, gameId, persona)
                    }
                }
            } else null
        }
    }
    
    // Load favorite enemies
    val favoriteEnemyIds = remember { userPrefs.getFavoriteEnemies() }
    val favoriteEnemies = remember(favoriteEnemyIds) {
        favoriteEnemyIds.mapNotNull { id ->
            // Parse ID format: "seriesId_gameId_enemyName"
            val parts = id.split("_", limit = 3)
            if (parts.size == 3) {
                val (seriesId, gameId, name) = parts
                val game = SeriesData.findGame(seriesId, gameId)
                game?.enemyPath?.let { enemyPath ->
                    val enemies = JsonLoader.loadEnemies(context, enemyPath)
                    enemies.find { it.name == name }?.let { enemy ->
                        Triple(seriesId, gameId, enemy)
                    }
                }
            } else null
        }
    }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Favorites", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = Surface,
                contentColor = TextPrimary
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }
            
            when (selectedTab) {
                0 -> {
                    if (favoritePersonas.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No favorite personas yet",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favoritePersonas) { (seriesId, gameId, persona) ->
                                val series = SeriesData.findSeries(seriesId)
                                if (series != null) {
                                    PersonaRow(
                                        persona = persona,
                                        accentColor = series.color,
                                        onClick = {
                                            navController.navigate(
                                                Screen.PersonaDetail.createRoute(seriesId, gameId, persona.name)
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
                1 -> {
                    if (favoriteEnemies.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "No favorite enemies yet",
                                color = TextSecondary,
                                style = MaterialTheme.typography.bodyLarge
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(favoriteEnemies) { (seriesId, gameId, enemy) ->
                                EnemyRow(
                                    enemy = enemy,
                                    onClick = {
                                        navController.navigate(
                                            Screen.EnemyDetail.createRoute(seriesId, gameId, enemy.name)
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}


@Composable
private fun PersonaRow(persona: Persona, accentColor: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.15f)
        ) {
            Text(
                text = "${persona.level}",
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = persona.name,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Text(
                text = persona.arcana ?: "Unknown",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun EnemyRow(enemy: Enemy, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = androidx.compose.ui.graphics.Color(0xFFE91E63).copy(alpha = 0.15f)
        ) {
            Text(
                text = "${enemy.level}",
                style = MaterialTheme.typography.labelLarge,
                color = androidx.compose.ui.graphics.Color(0xFFE91E63),
                fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }
        Spacer(Modifier.width(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = enemy.name,
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimary,
                fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
            )
            Text(
                text = enemy.arcana,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}
