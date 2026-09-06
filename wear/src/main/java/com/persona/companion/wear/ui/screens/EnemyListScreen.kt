package com.persona.companion.wear.ui.screens

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
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.persona.companion.wear.data.WearDataLoader
import com.persona.companion.wear.models.WearEnemyItem
import com.persona.companion.wear.ui.theme.DrainColor
import com.persona.companion.wear.ui.theme.NullColor
import com.persona.companion.wear.ui.theme.P4Gold
import com.persona.companion.wear.ui.theme.RepelColor
import com.persona.companion.wear.ui.theme.ResistColor
import com.persona.companion.wear.ui.theme.WeakColor
import com.persona.companion.wear.ui.theme.WearDarkGray
import com.persona.companion.wear.ui.theme.WearLightGray
import com.persona.companion.wear.ui.theme.WearMediumGray

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EnemyListScreen(
    gameId: String
) {
    val context = LocalContext.current
    val listState = rememberScalingLazyListState()

    val enemies by produceState<List<WearEnemyItem>>(initialValue = emptyList(), key1 = gameId) {
        value = WearDataLoader.loadEnemies(context, gameId)
    }

    var selectedEnemy by remember { mutableStateOf<WearEnemyItem?>(null) }

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
                            text = "📍 ${enemy.area}",
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
                        text = "Tap anywhere to go back",
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
            ScalingLazyColumn(
                modifier = Modifier.fillMaxSize(),
                state = listState
            ) {
                item {
                    ListHeader {
                        Text(
                            text = "Enemies (${enemies.size})",
                            color = WearLightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                if (enemies.isEmpty()) {
                    item {
                        Text(
                            text = "Loading enemies...",
                            color = WearLightGray,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                items(enemies) { enemy ->
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
