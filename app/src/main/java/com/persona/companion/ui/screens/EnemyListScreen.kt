package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.models.Enemy
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.EnemyListViewModel

enum class EnemyTab {
    ENEMIES, MINI_BOSSES, MAIN_BOSSES
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnemyListScreen(
    seriesId: String,
    gameId: String,
    gameTitle: String,
    enemyPath: String?,
    onBack: () -> Unit,
    onEnemyClick: (Enemy) -> Unit,
    viewModel: EnemyListViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    var selectedTab by remember { mutableStateOf(EnemyTab.ENEMIES) }
    var searchQuery by remember { mutableStateOf("") }
    
    LaunchedEffect(enemyPath) {
        viewModel.loadEnemies(context, enemyPath)
    }
    
    // Filter enemies by category and search
    val filteredEnemies = remember(state.enemies, selectedTab, searchQuery) {
        val byCategory = when (selectedTab) {
            EnemyTab.ENEMIES -> state.enemies.filter { !it.isMiniBoss && !it.isBoss }
            EnemyTab.MINI_BOSSES -> state.enemies.filter { it.isMiniBoss }
            EnemyTab.MAIN_BOSSES -> state.enemies.filter { it.isBoss }
        }
        
        if (searchQuery.isBlank()) {
            byCategory
        } else {
            byCategory.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.arcana.contains(searchQuery, ignoreCase = true) ||
                it.area.contains(searchQuery, ignoreCase = true)
            }
        }
    }
    
    val enemyCount = state.enemies.count { !it.isMiniBoss && !it.isBoss }
    val miniBossCount = state.enemies.count { it.isMiniBoss }
    val mainBossCount = state.enemies.count { it.isBoss }
    
    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("$gameTitle - Enemies", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        when {
            state.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            state.errorMessage != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = state.errorMessage ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
            state.enemies.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No enemies available",
                        color = TextSecondary
                    )
                }
            }
            else -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                ) {
                    // Search Bar
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        placeholder = { Text("Search enemies...", color = TextDisabled) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = "Search",
                                tint = TextSecondary
                            )
                        },
                        trailingIcon = {
                            if (searchQuery.isNotEmpty()) {
                                IconButton(onClick = { searchQuery = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear",
                                        tint = TextSecondary
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedBorderColor = TextSecondary,
                            unfocusedBorderColor = TextDisabled,
                            cursorColor = TextPrimary
                        ),
                        singleLine = true
                    )
                    
                    // Tabs
                    TabRow(
                        selectedTabIndex = selectedTab.ordinal,
                        containerColor = Surface,
                        contentColor = TextPrimary
                    ) {
                        Tab(
                            selected = selectedTab == EnemyTab.ENEMIES,
                            onClick = { selectedTab = EnemyTab.ENEMIES },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Enemies")
                                    Text(
                                        text = "($enemyCount)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == EnemyTab.MINI_BOSSES,
                            onClick = { selectedTab = EnemyTab.MINI_BOSSES },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Mini Bosses")
                                    Text(
                                        text = "($miniBossCount)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                        Tab(
                            selected = selectedTab == EnemyTab.MAIN_BOSSES,
                            onClick = { selectedTab = EnemyTab.MAIN_BOSSES },
                            text = {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text("Main Bosses")
                                    Text(
                                        text = "($mainBossCount)",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        )
                    }
                    
                    // Enemy List
                    if (filteredEnemies.isEmpty()) {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = if (searchQuery.isNotEmpty()) {
                                    "No enemies found matching \"$searchQuery\""
                                } else {
                                    when (selectedTab) {
                                        EnemyTab.ENEMIES -> "No enemies available"
                                        EnemyTab.MINI_BOSSES -> "No mini bosses available"
                                        EnemyTab.MAIN_BOSSES -> "No main bosses available"
                                    }
                                },
                                color = TextSecondary
                            )
                        }
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(filteredEnemies) { enemy ->
                                EnemyCard(
                                    enemy = enemy,
                                    onClick = { onEnemyClick(enemy) }
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
fun EnemyCard(
    enemy: Enemy,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = enemy.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = "${enemy.arcana} • Lv. ${enemy.level}",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            if (enemy.area.isNotEmpty() && enemy.area != "Unknown") {
                Spacer(Modifier.height(2.dp))
                Text(
                    text = enemy.area,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextDisabled
                )
            }
        }
        
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "${enemy.hp} HP",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
            Text(
                text = "${enemy.exp} EXP",
                style = MaterialTheme.typography.bodySmall,
                color = TextDisabled
            )
        }
    }
}
