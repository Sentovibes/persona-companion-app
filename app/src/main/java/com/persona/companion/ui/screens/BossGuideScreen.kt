package com.persona.companion.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.companion.data.SeriesData
import com.persona.companion.models.BossGuideItem
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.JsonLoader

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BossGuideScreen(
    seriesId: String,
    gameId: String,
    onBack: () -> Unit,
    onBossClick: (String) -> Unit
) {
    val context = LocalContext.current
    val series = remember(seriesId) { SeriesData.findSeries(seriesId) }
    val accentColor = series?.color ?: Persona3Blue

    val bossData = remember(gameId) {
        JsonLoader.loadBossGuides(context, "data/guides/boss_guides.json")
            .find { it.gameId.equals(gameId, ignoreCase = true) }
    }
    val bosses = bossData?.bosses ?: emptyList()

    var searchQuery by remember { mutableStateOf("") }
    val filteredBosses = remember(searchQuery, bosses) {
        bosses.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
                    it.location.contains(searchQuery, ignoreCase = true) ||
                    it.strategy.contains(searchQuery, ignoreCase = true)
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Boss Prep Guides", color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text(SeriesData.findGame(seriesId, gameId)?.title ?: "", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
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
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                placeholder = { Text("Search bosses, locations, elements...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Divider,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = accentColor
                )
            )

            if (filteredBosses.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No boss guides found for this search.",
                        color = TextSecondary,
                        fontSize = 16.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredBosses) { boss ->
                        BossGuideCard(
                            boss = boss,
                            onClick = { onBossClick(boss.name) }
                        )
                    }
                    item {
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun BossGuideCard(
    boss: BossGuideItem,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Boss Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = boss.name,
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )

                if (boss.level > 0) {
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = TagAlmighty.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "Lv ${boss.level}",
                            color = TagAlmighty,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Location
            Text(
                text = boss.location,
                color = TextSecondary,
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Weakness & Resistances brief
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Weaknesses", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = boss.weaknesses,
                        color = if (boss.weaknesses.contains("None", ignoreCase = true)) TextSecondary else TagIce,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "Resistances", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(
                        text = boss.resistances,
                        color = if (boss.resistances.contains("None", ignoreCase = true)) TextSecondary else TagFire,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

