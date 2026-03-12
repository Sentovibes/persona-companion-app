package com.persona.themeduitest.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.themeduitest.ui.components.*
import com.persona.themeduitest.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ThemeShowcaseScreen() {
    var selectedGame by remember { mutableStateOf(PersonaGame.P3) }
    
    val backgroundColor = when (selectedGame) {
        PersonaGame.P3 -> P3Colors.Background
        PersonaGame.P4 -> P4Colors.Background
        PersonaGame.P5 -> P5Colors.Background
    }
    
    val textColor = when (selectedGame) {
        PersonaGame.P3 -> P3Colors.OnBackground
        PersonaGame.P4 -> P4Colors.OnBackground
        PersonaGame.P5 -> P5Colors.OnBackground
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        "Persona Themed UI Test",
                        fontWeight = FontWeight.Bold
                    ) 
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = backgroundColor,
                    titleContentColor = textColor
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(padding)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Game Selector
            Text(
                text = "Select Game Theme",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                FilterChip(
                    selected = selectedGame == PersonaGame.P3,
                    onClick = { selectedGame = PersonaGame.P3 },
                    label = { Text("P3") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = P3Colors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
                FilterChip(
                    selected = selectedGame == PersonaGame.P4,
                    onClick = { selectedGame = PersonaGame.P4 },
                    label = { Text("P4") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = P4Colors.Primary,
                        selectedLabelColor = Color.Black
                    )
                )
                FilterChip(
                    selected = selectedGame == PersonaGame.P5,
                    onClick = { selectedGame = PersonaGame.P5 },
                    label = { Text("P5") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = P5Colors.Primary,
                        selectedLabelColor = Color.White
                    )
                )
            }
            
            Divider(color = textColor.copy(alpha = 0.3f))
            
            // Button Showcase
            Text(
                text = when (selectedGame) {
                    PersonaGame.P3 -> "Persona 3 Style"
                    PersonaGame.P4 -> "Persona 4 Style"
                    PersonaGame.P5 -> "Persona 5 Style"
                },
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = textColor
            )
            
            when (selectedGame) {
                PersonaGame.P3 -> {
                    P3Button(text = "Compendium", onClick = {})
                    P3Button(text = "Enemies", onClick = {})
                    P3Button(text = "Settings", onClick = {})
                    P3Button(text = "About", onClick = {})
                }
                PersonaGame.P4 -> {
                    P4Button(text = "Compendium", onClick = {})
                    P4Button(text = "Enemies", onClick = {})
                    P4Button(text = "Settings", onClick = {})
                    P4Button(text = "About", onClick = {})
                }
                PersonaGame.P5 -> {
                    P5Button(text = "Compendium", onClick = {})
                    P5Button(text = "Enemies", onClick = {})
                    P5Button(text = "Settings", onClick = {})
                    P5Button(text = "About", onClick = {})
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Description
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = when (selectedGame) {
                        PersonaGame.P3 -> P3Colors.Surface
                        PersonaGame.P4 -> P4Colors.Surface
                        PersonaGame.P5 -> P5Colors.Surface
                    }
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Theme Characteristics:",
                        fontWeight = FontWeight.Bold,
                        color = textColor
                    )
                    Text(
                        text = when (selectedGame) {
                            PersonaGame.P3 -> "• Sharp rectangular edges\n• Blue tech aesthetic\n• Digital/futuristic feel\n• Thin borders with glow"
                            PersonaGame.P4 -> "• Rounded corners\n• Retro TV aesthetic\n• Warm yellow/orange tones\n• Thick borders"
                            PersonaGame.P5 -> "• Diagonal cuts\n• Bold red/black contrast\n• Stylish and sharp\n• High contrast design"
                        },
                        color = textColor.copy(alpha = 0.9f),
                        fontSize = 14.sp
                    )
                }
            }
        }
    }
}
