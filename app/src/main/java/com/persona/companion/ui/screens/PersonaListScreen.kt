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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.persona.companion.data.SeriesData
import com.persona.companion.models.Persona
import com.persona.companion.navigation.Screen
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.PersonaListViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaListScreen(
    navController: NavController,
    seriesId: String,
    gameId: String,
    vm: PersonaListViewModel = viewModel()
) {
    val context = LocalContext.current
    val series  = SeriesData.findSeries(seriesId) ?: return
    val game    = SeriesData.findGame(seriesId, gameId) ?: return
    val state   by vm.state.collectAsState()

    LaunchedEffect(game.dataPath) {
        vm.load(game.dataPath)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Personas — ${game.title}", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
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
            // Search bar
            SearchBar(
                query         = state.query,
                onQueryChange = vm::onQueryChange,
                accentColor   = series.color
            )

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = series.color)
                    }
                }
                state.filtered.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("No personas found", color = TextSecondary)
                    }
                }
                else -> {
                    // Group by arcana for easier browsing
                    val grouped = state.filtered.groupBy { it.arcana ?: "Unknown" }.entries.sortedBy { it.key }

                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        grouped.forEach { (arcana, personas) ->
                            item {
                                ArcanaHeader(arcana, series.color)
                            }
                            items(personas) { persona ->
                                PersonaRow(
                                    persona     = persona,
                                    accentColor = series.color,
                                    onClick     = {
                                        navController.navigate(
                                            Screen.PersonaDetail.createRoute(seriesId, gameId, persona.name)
                                        )
                                    }
                                )
                            }
                            item { Spacer(Modifier.height(4.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchBar(query: String, onQueryChange: (String) -> Unit, accentColor: Color) {
    OutlinedTextField(
        value         = query,
        onValueChange = onQueryChange,
        modifier      = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        placeholder   = { Text("Search by name or arcana…", color = TextDisabled) },
        leadingIcon   = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
        trailingIcon  = {
            if (query.isNotBlank()) {
                IconButton(onClick = { onQueryChange("") }) {
                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                }
            }
        },
        singleLine    = true,
        shape         = RoundedCornerShape(12.dp),
        colors        = OutlinedTextFieldDefaults.colors(
            focusedBorderColor   = accentColor,
            unfocusedBorderColor = Divider,
            focusedTextColor     = TextPrimary,
            unfocusedTextColor   = TextPrimary,
            cursorColor          = accentColor
        )
    )
}

@Composable
private fun ArcanaHeader(arcana: String, accentColor: Color) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(width = 3.dp, height = 16.dp)
                .background(accentColor)
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text  = arcana,
            style = MaterialTheme.typography.labelLarge,
            color = accentColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun PersonaRow(persona: Persona, accentColor: Color, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(SurfaceCard)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Level badge
        Surface(
            shape = RoundedCornerShape(8.dp),
            color = accentColor.copy(alpha = 0.15f)
        ) {
            Text(
                text  = "${persona.level}",
                style = MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
            )
        }

        Spacer(Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = persona.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Text(
                text  = persona.arcana ?: "Unknown",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
        }

        // Skill count hint
        val skillCount = persona.skills?.size ?: 0
        if (skillCount > 0) {
            Text(
                text  = "$skillCount skills",
                style = MaterialTheme.typography.labelSmall,
                color = TextDisabled
            )
        }
    }
}
