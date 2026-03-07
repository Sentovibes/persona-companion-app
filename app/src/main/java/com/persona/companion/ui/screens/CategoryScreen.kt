package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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

    val categories = listOf(
        CategoryItem(
            label     = "Personas",
            icon      = Icons.Default.MenuBook,
            available = true,
            route     = Screen.PersonaList.createRoute(seriesId, gameId)
        ),
        CategoryItem(
            label     = "Fusion Calculator",
            icon      = Icons.Default.AutoAwesome,
            available = true,
            route     = Screen.Fusion.createRoute(seriesId, gameId)
        ),
        CategoryItem(
            label     = "Social Links / Confidants",
            icon      = Icons.Default.Groups,
            available = false
        ),
        CategoryItem(
            label     = "Classroom Answers",
            icon      = Icons.Default.School,
            available = false
        ),
        CategoryItem(
            label     = "Bosses",
            icon      = Icons.Default.Shield,
            available = false
        ),
        CategoryItem(
            label     = "Enemies",
            icon      = Icons.Default.Shield,
            available = false
        )
    )

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text(game.title, color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            item {
                Text(
                    text = "What would you like to browse?",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(bottom = 4.dp)
                )
            }
            items(categories) { category ->
                CategoryRow(
                    item        = category,
                    accentColor = series.color,
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
private fun CategoryRow(item: CategoryItem, accentColor: Color, onClick: () -> Unit) {
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
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondary
            )
        }
    }
}
