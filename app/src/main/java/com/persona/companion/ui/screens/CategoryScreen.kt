package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Assignment
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Inventory2
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.ui.viewmodels.CategoryViewModel
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.persona.companion.data.SeriesData
import com.persona.companion.navigation.Screen
import com.persona.companion.ui.theme.*

data class CategoryItem(
    val label: String,
    val icon: ImageVector,
    val available: Boolean,
    val route: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CategoryScreen(navController: NavController, seriesId: String, gameId: String) {
    val series = SeriesData.findSeries(seriesId) ?: return
    val game   = SeriesData.findGame(seriesId, gameId) ?: return

    val viewModel: CategoryViewModel = viewModel()
    val completedCount by viewModel.completedCount.collectAsState()
    val totalCount by viewModel.totalCount.collectAsState()

    LaunchedEffect(gameId) {
        viewModel.loadGame(gameId)
    }

    val categories = mutableListOf<CategoryItem>()
    categories.addAll(listOf(
        CategoryItem(
            label     = "Personas",
            icon      = Icons.AutoMirrored.Filled.MenuBook,
            available = game.dataPath != null,
            route     = if (game.dataPath != null) Screen.PersonaList.createRoute(seriesId, gameId) else null
        ),
        CategoryItem(
            label     = "Fusion Calculator",
            icon      = Icons.Default.AutoAwesome,
            available = true,
            route     = Screen.Fusion.createRoute(seriesId, gameId)
        ),
        CategoryItem(
            label     = "Enemies",
            icon      = Icons.Default.Shield,
            available = game.enemyPath != null,
            route     = if (game.enemyPath != null) Screen.EnemyList.createRoute(seriesId, gameId) else null
        ),
        CategoryItem(
            label     = "Social Links / Confidants",
            icon      = Icons.Default.Groups,
            available = game.socialLinkPath != null,
            route     = if (game.socialLinkPath != null) Screen.SocialLinks.createRoute(seriesId, gameId) else null
        ),
        CategoryItem(
            label     = "Classroom Answers",
            icon      = Icons.Default.School,
            available = gameId in listOf("p3fes", "p3p", "p3r", "p4", "p4g", "p5", "p5r"),
            route     = if (gameId in listOf("p3fes", "p3p", "p3r", "p4", "p4g", "p5", "p5r")) Screen.ClassroomAnswers.createRoute(seriesId, gameId) else null
        ),
        CategoryItem(
            label     = "Skills",
            icon      = Icons.Default.AutoAwesome,
            available = game.skillPath != null,
            route     = if (game.skillPath != null) Screen.SkillList.createRoute(seriesId, gameId) else null
        ),
        CategoryItem(
            label     = "Items",
            icon      = Icons.Default.Inventory2,
            available = game.itemPath != null,
            route     = if (game.itemPath != null) Screen.ItemList.createRoute(seriesId, gameId) else null
        ),
        CategoryItem(
            label     = "Side-Quests / Requests",
            icon      = Icons.AutoMirrored.Filled.Assignment,
            available = game.requestPath != null,
            route     = if (game.requestPath != null) Screen.RequestList.createRoute(seriesId, gameId) else null
        ),
        CategoryItem(
            label     = "Guides & Walkthroughs",
            icon      = Icons.AutoMirrored.Filled.MenuBook,
            available = true,
            route     = Screen.GuidesHub.createRoute(seriesId, gameId)
        )
    ))

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(game.title, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            if (totalCount > 0) {
                item {
                    CampaignProgressCard(
                        completed = completedCount,
                        total = totalCount,
                        accentColor = series.color
                    )
                }
            }

            item {
                Text(
                    text = "What would you like to browse?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(categories) { category ->
                val badgeText = if (category.label == "Side-Quests / Requests" && totalCount > 0) {
                    "$completedCount/$totalCount"
                } else null

                CategoryRow(
                    item        = category,
                    accentColor = series.color,
                    badgeText   = badgeText,
                    onClick     = {
                        if (category.available && category.route != null) {
                            navController.navigate(category.route)
                        }
                    }
                )
            }
        }
    }
}

@Composable
private fun CategoryRow(
    item: CategoryItem,
    accentColor: Color,
    badgeText: String? = null,
    onClick: () -> Unit
) {
    val alpha = if (item.available) 1f else 0.45f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(SurfaceCard)
            .clickable(enabled = item.available, onClick = onClick)
            .alpha(alpha)
            .padding(horizontal = 20.dp, vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = item.icon,
            contentDescription = null,
            tint = if (item.available) accentColor else TextDisabled,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = item.label,
            style = MaterialTheme.typography.titleMedium,
            color = if (item.available) TextPrimary else TextSecondary,
            modifier = Modifier.weight(1f)
        )
        if (!item.available) {
            Surface(
                shape  = RoundedCornerShape(6.dp),
                color  = SurfaceRaised
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = TextDisabled,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text  = "Soon",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextDisabled
                    )
                }
            }
        } else {
            if (badgeText != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = accentColor.copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 8.dp)
                ) {
                    Text(
                        text = badgeText,
                        style = MaterialTheme.typography.labelMedium,
                        color = accentColor,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}

@Composable
fun CampaignProgressCard(
    completed: Int,
    total: Int,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    val progress = if (total > 0) completed.toFloat() / total else 0f
    val percentage = (progress * 100).toInt()

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 12.dp)
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Campaign Progress",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "$completed of $total Requests Completed",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                }
                
                Text(
                    text = "$percentage%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = accentColor
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = accentColor,
                trackColor = SurfaceRaised
            )
        }
    }
}
