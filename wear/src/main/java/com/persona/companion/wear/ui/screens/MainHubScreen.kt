package com.persona.companion.wear.ui.screens

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.ListHeader
import androidx.wear.compose.material.PositionIndicator
import androidx.wear.compose.material.Scaffold
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.compose.material.Vignette
import androidx.wear.compose.material.VignettePosition
import com.persona.companion.wear.models.WearGame
import com.persona.companion.wear.ui.theme.WearDarkGray
import com.persona.companion.wear.ui.theme.WearLightGray
import com.persona.companion.wear.ui.theme.getGameAccentColor

@Composable
fun MainHubScreen(
    selectedGame: WearGame,
    onNavigateToClassroom: () -> Unit,
    onNavigateToEnemies: () -> Unit,
    onNavigateToSocialLinks: () -> Unit,
    onNavigateToGameSelect: () -> Unit
) {
    val listState = rememberScalingLazyListState()
    val accent = getGameAccentColor(selectedGame.id)

    Scaffold(
        timeText = { TimeText() },
        vignette = { Vignette(vignettePosition = VignettePosition.TopAndBottom) },
        positionIndicator = { PositionIndicator(scalingLazyListState = listState) }
    ) {
        ScalingLazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = listState
        ) {
            item {
                ListHeader {
                    Text(
                        text = "Persona Companion",
                        color = WearLightGray,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // Current Game Selector Chip
            item {
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    onClick = onNavigateToGameSelect,
                    colors = ChipDefaults.chipColors(
                        backgroundColor = accent.copy(alpha = 0.2f),
                        contentColor = accent
                    ),
                    label = {
                        Text(
                            text = selectedGame.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "Switch Series / Game",
                            fontSize = 10.sp,
                            color = WearLightGray
                        )
                    }
                )
            }

            // Classroom & Exams Chip
            item {
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    onClick = onNavigateToClassroom,
                    colors = ChipDefaults.chipColors(
                        backgroundColor = WearDarkGray,
                        contentColor = Color.White
                    ),
                    label = {
                        Text(
                            text = "Classroom & Exams",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "Questions & Answers",
                            fontSize = 10.sp,
                            color = WearLightGray
                        )
                    }
                )
            }

            // Enemy Weaknesses Chip
            item {
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    onClick = onNavigateToEnemies,
                    colors = ChipDefaults.chipColors(
                        backgroundColor = WearDarkGray,
                        contentColor = Color.White
                    ),
                    label = {
                        Text(
                            text = "Enemy Calculator",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "Weaknesses & Resistances",
                            fontSize = 10.sp,
                            color = WearLightGray
                        )
                    }
                )
            }

            // Social Links Chip
            item {
                Chip(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 4.dp, vertical = 2.dp),
                    onClick = onNavigateToSocialLinks,
                    colors = ChipDefaults.chipColors(
                        backgroundColor = WearDarkGray,
                        contentColor = Color.White
                    ),
                    label = {
                        Text(
                            text = "Social Links",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 13.sp
                        )
                    },
                    secondaryLabel = {
                        Text(
                            text = "Best Dialogue Choices",
                            fontSize = 10.sp,
                            color = WearLightGray
                        )
                    }
                )
            }
        }
    }
}
