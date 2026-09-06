package com.persona.companion.wear.ui.screens

import android.app.Activity
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.items
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.persona.companion.wear.data.WearDataLoader
import com.persona.companion.wear.models.WearEnemyItem
import com.persona.companion.wear.ui.rotaryScroll
import com.persona.companion.wear.ui.theme.DrainColor
import com.persona.companion.wear.ui.theme.NullColor
import com.persona.companion.wear.ui.theme.P4Gold
import com.persona.companion.wear.ui.theme.RepelColor
import com.persona.companion.wear.ui.theme.ResistColor
import com.persona.companion.wear.ui.theme.WeakColor
import com.persona.companion.wear.ui.theme.WearDarkGray
import com.persona.companion.wear.ui.theme.WearLightGray
import com.persona.companion.wear.ui.theme.WearMediumGray
import com.persona.companion.wear.ui.theme.getGameAccentColor

enum class EnemyCategory {
    ENEMIES,
    MINI_BOSSES,
    MAIN_BOSSES
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnemyListScreen(
    gameId: String
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()
    val accent = getGameAccentColor(gameId)

    val allEnemies by produceState<List<WearEnemyItem>>(initialValue = emptyList(), key1 = gameId) {
        value = WearDataLoader.loadEnemies(context, gameId)
    }

    var selectedCategory by remember { mutableStateOf(EnemyCategory.ENEMIES) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedEnemy by remember { mutableStateOf<WearEnemyItem?>(null) }

    val voiceSearchLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val results = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val spokenText = results?.firstOrNull() ?: ""
            searchQuery = spokenText.trim()
        }
    }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        if (selectedEnemy != null) {
            val enemy = selectedEnemy!!
            val detailListState = rememberScalingLazyListState()

            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScroll(detailListState)
                    .clickable { selectedEnemy = null },
                state = detailListState
            ) {
                item {
                    ListHeader {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = enemy.name,
                                color = Color.White,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = "Lv. ${enemy.level} • ${enemy.arcana}",
                                color = WearLightGray,
                                fontSize = 10.sp
                            )
                        }
                    }
                }

                if (enemy.area.isNotEmpty()) {
                    item {
                        Text(
                            text = "Location: ${enemy.area}",
                            color = WearLightGray,
                            fontSize = 10.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Weaknesses Section
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(WearDarkGray)
                            .padding(8.dp)
                    ) {
                        Column {
                            Text(
                                text = "WEAKNESSES",
                                color = WeakColor,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (enemy.weaknesses.isEmpty()) {
                                Text(
                                    text = "None (No elemental weaknesses)",
                                    color = WearLightGray,
                                    fontSize = 11.sp,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    enemy.weaknesses.forEach { elem ->
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(WeakColor.copy(alpha = 0.25f))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = elem,
                                                color = WeakColor,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Other Resistances
                val otherResists = enemy.resistances.filter { it.second != "Weak" }
                if (otherResists.isNotEmpty()) {
                    item {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(WearDarkGray)
                                .padding(8.dp)
                        ) {
                            Column {
                                Text(
                                    text = "RESISTANCES",
                                    color = WearLightGray,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                FlowRow(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 4.dp),
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalArrangement = Arrangement.spacedBy(4.dp)
                                ) {
                                    otherResists.forEach { (elem, type) ->
                                        val color = when (type) {
                                            "Resist" -> ResistColor
                                            "Null" -> NullColor
                                            "Repel" -> RepelColor
                                            "Drain" -> DrainColor
                                            else -> WearLightGray
                                        }
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(color.copy(alpha = 0.2f))
                                                .padding(horizontal = 5.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = "$elem: $type",
                                                color = color,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        text = "Tap anywhere to return",
                        color = WearLightGray,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 6.dp)
                    )
                }
            }
        } else {
            // Category counts
            val regularEnemies = allEnemies.filter { !it.isBoss && !it.isMiniBoss }
            val miniBosses = allEnemies.filter { it.isMiniBoss }
            val mainBosses = allEnemies.filter { it.isBoss }

            val currentCategoryList = when (selectedCategory) {
                EnemyCategory.ENEMIES -> regularEnemies
                EnemyCategory.MINI_BOSSES -> miniBosses
                EnemyCategory.MAIN_BOSSES -> mainBosses
            }

            val displayList = if (searchQuery.isBlank()) {
                currentCategoryList
            } else {
                currentCategoryList.filter {
                    it.name.contains(searchQuery, ignoreCase = true) ||
                            it.arcana.contains(searchQuery, ignoreCase = true) ||
                            it.area.contains(searchQuery, ignoreCase = true)
                }
            }

            ScalingLazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .rotaryScroll(listState),
                state = listState
            ) {
                // Header
                item {
                    ListHeader {
                        Text(
                            text = "Enemy Calculator",
                            color = WearLightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Search Bar Chip
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Chip(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                try {
                                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Search Enemy Name")
                                    }
                                    voiceSearchLauncher.launch(intent)
                                } catch (e: Exception) {
                                    // Fallback if voice search not supported
                                }
                            },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (searchQuery.isNotEmpty()) accent.copy(alpha = 0.2f) else WearDarkGray,
                                contentColor = if (searchQuery.isNotEmpty()) accent else Color.White
                            ),
                            label = {
                                Text(
                                    text = if (searchQuery.isEmpty()) "Search..." else "Search: $searchQuery",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        )
                        if (searchQuery.isNotEmpty()) {
                            CompactChip(
                                modifier = Modifier.padding(start = 4.dp),
                                onClick = { searchQuery = "" },
                                colors = ChipDefaults.chipColors(
                                    backgroundColor = WearDarkGray,
                                    contentColor = Color.White
                                ),
                                label = { Text("X", fontSize = 10.sp, fontWeight = FontWeight.Bold) }
                            )
                        }
                    }
                }

                // 3 Category Tabs: Enemies | Mini-Bosses | Bosses
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 2.dp, vertical = 3.dp),
                        horizontalArrangement = Arrangement.spacedBy(3.dp)
                    ) {
                        val tabEnemies = selectedCategory == EnemyCategory.ENEMIES
                        val tabMini = selectedCategory == EnemyCategory.MINI_BOSSES
                        val tabBoss = selectedCategory == EnemyCategory.MAIN_BOSSES

                        CompactChip(
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = EnemyCategory.ENEMIES },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (tabEnemies) accent else WearDarkGray,
                                contentColor = if (tabEnemies) Color.Black else Color.White
                            ),
                            label = {
                                Text(
                                    text = "Normal (${regularEnemies.size})",
                                    fontSize = 8.sp,
                                    fontWeight = if (tabEnemies) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        )

                        CompactChip(
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = EnemyCategory.MINI_BOSSES },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (tabMini) accent else WearDarkGray,
                                contentColor = if (tabMini) Color.Black else Color.White
                            ),
                            label = {
                                Text(
                                    text = "Mini (${miniBosses.size})",
                                    fontSize = 8.sp,
                                    fontWeight = if (tabMini) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        )

                        CompactChip(
                            modifier = Modifier.weight(1f),
                            onClick = { selectedCategory = EnemyCategory.MAIN_BOSSES },
                            colors = ChipDefaults.chipColors(
                                backgroundColor = if (tabBoss) accent else WearDarkGray,
                                contentColor = if (tabBoss) Color.Black else Color.White
                            ),
                            label = {
                                Text(
                                    text = "Boss (${mainBosses.size})",
                                    fontSize = 8.sp,
                                    fontWeight = if (tabBoss) FontWeight.Bold else FontWeight.Normal,
                                    textAlign = TextAlign.Center,
                                    maxLines = 1
                                )
                            }
                        )
                    }
                }

                if (displayList.isEmpty()) {
                    item {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matches for \"$searchQuery\"" else "No enemies in this category",
                            color = WearLightGray,
                            fontSize = 11.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 12.dp)
                        )
                    }
                }

                items(displayList) { enemy ->
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(WearDarkGray)
                            .clickable { selectedEnemy = enemy }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                    ) {
                        Column {
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(WearMediumGray)
                                        .padding(horizontal = 4.dp, vertical = 1.dp)
                                ) {
                                    Text(
                                        text = "Lv ${enemy.level}",
                                        color = P4Gold,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Text(
                                    text = enemy.name,
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    modifier = Modifier.padding(start = 6.dp)
                                )
                            }

                            val weakText = if (enemy.weaknesses.isNotEmpty()) {
                                "Weak: " + enemy.weaknesses.joinToString(", ")
                            } else {
                                "No weaknesses"
                            }

                            Text(
                                text = weakText,
                                color = if (enemy.weaknesses.isNotEmpty()) WeakColor else WearLightGray,
                                fontSize = 10.sp,
                                fontWeight = if (enemy.weaknesses.isNotEmpty()) FontWeight.SemiBold else FontWeight.Normal,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
