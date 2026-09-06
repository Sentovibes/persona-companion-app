package com.persona.companion.wear.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.persona.companion.wear.models.WEAR_SERIES
import com.persona.companion.wear.models.WearGame
import com.persona.companion.wear.models.WearSeries
import com.persona.companion.wear.ui.theme.WearDarkGray
import com.persona.companion.wear.ui.theme.WearLightGray
import com.persona.companion.wear.ui.theme.getGameAccentColor

@Composable
fun GameSelectScreen(
    currentGameId: String,
    onGameSelected: (WearGame) -> Unit
) {
    val listState = rememberScalingLazyListState()
    var selectedSeries by remember { mutableStateOf<WearSeries?>(null) }

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            if (selectedSeries == null) {
                // Step 1: Select Main Series (Persona 5, Persona 4, Persona 3)
                item {
                    ListHeader {
                        Text(
                            text = "Select Series",
                            color = WearLightGray,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                items(WEAR_SERIES) { series ->
                    val seriesColor = Color(series.colorHex)
                    val isCurrentSeries = series.games.any { it.id == currentGameId }

                    Chip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        onClick = { selectedSeries = series },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = if (isCurrentSeries) seriesColor.copy(alpha = 0.25f) else WearDarkGray,
                            contentColor = if (isCurrentSeries) seriesColor else Color.White
                        ),
                        label = {
                            Text(
                                text = series.title,
                                fontWeight = if (isCurrentSeries) FontWeight.Bold else FontWeight.SemiBold,
                                fontSize = 13.sp
                            )
                        },
                        secondaryLabel = {
                            Text(
                                text = series.games.joinToString(", ") { it.shortTitle },
                                fontSize = 10.sp,
                                color = if (isCurrentSeries) seriesColor.copy(alpha = 0.8f) else WearLightGray
                            )
                        }
                    )
                }
            } else {
                // Step 2: Select Game inside the chosen Series
                val series = selectedSeries!!
                val seriesColor = Color(series.colorHex)

                item {
                    ListHeader {
                        Text(
                            text = series.title,
                            color = seriesColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    CompactChip(
                        modifier = Modifier.padding(bottom = 4.dp),
                        onClick = { selectedSeries = null },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = WearDarkGray,
                            contentColor = WearLightGray
                        ),
                        label = {
                            Text(
                                text = "< Back to Series",
                                fontSize = 10.sp
                            )
                        }
                    )
                }

                items(series.games) { game ->
                    val isSelected = game.id == currentGameId
                    val gameColor = getGameAccentColor(game.id)

                    Chip(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp, vertical = 2.dp),
                        onClick = { onGameSelected(game) },
                        colors = ChipDefaults.chipColors(
                            backgroundColor = if (isSelected) gameColor.copy(alpha = 0.25f) else WearDarkGray,
                            contentColor = if (isSelected) gameColor else Color.White
                        ),
                        label = {
                            Text(
                                text = game.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                        },
                        secondaryLabel = if (isSelected) {
                            {
                                Text(
                                    text = "Active",
                                    fontSize = 10.sp,
                                    color = gameColor
                                )
                            }
                        } else null
                    )
                }
            }
        }
    }
}
