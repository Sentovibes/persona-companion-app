package com.persona.companion.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.persona.companion.data.SeriesData
import com.persona.companion.models.Persona
import com.persona.companion.navigation.Screen
import com.persona.companion.ui.components.PersonaFilterSheet
import com.persona.companion.ui.components.WeaknessRow
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.PersonaListViewModel
import com.persona.companion.utils.personaImage
import com.persona.companion.utils.rememberShouldLoadImages

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaListScreen(
    navController: NavController,
    seriesId: String,
    gameId: String,
    vm: PersonaListViewModel = viewModel()
) {
    val context = LocalContext.current
    val userPrefs = remember { com.persona.companion.data.UserPreferences(context) }
    var isCompactView by remember { mutableStateOf(userPrefs.isCompactView()) }
    val series  = SeriesData.findSeries(seriesId) ?: return
    val game    = SeriesData.findGame(seriesId, gameId) ?: return
    val state   by vm.state.collectAsState()
    val ownedPersonas by userPrefs.ownedPersonasFlow.collectAsState(initial = userPrefs.getOwnedPersonas())
    var ownershipFilter by remember { mutableStateOf("ALL") } // "ALL", "OWNED", "MISSING"
    var showFilterSheet by remember { mutableStateOf(false) }

    LaunchedEffect(game.dataPath, game.aigisDataPath) {
        vm.load(game.dataPath, game.aigisDataPath, seriesId, gameId)
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
                actions = {
                    IconButton(onClick = { showFilterSheet = true }) {
                        Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = series.color)
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

            // Sort options
            SortBar(
                currentSort = state.sortBy,
                onSortChange = vm::setSortOption,
                accentColor = series.color
            )

            // Compendium registration progress & quick filter
            val totalCount = state.personas.size
            val ownedCount = remember(state.personas, ownedPersonas, seriesId, gameId) {
                state.personas.count { p ->
                    ownedPersonas.contains(com.persona.companion.utils.FilterUtils.getPersonaId(seriesId, gameId, p))
                }
            }
            val displayedPersonas = remember(state.filtered, ownershipFilter, ownedPersonas, seriesId, gameId) {
                when (ownershipFilter) {
                    "OWNED" -> state.filtered.filter { p ->
                        ownedPersonas.contains(com.persona.companion.utils.FilterUtils.getPersonaId(seriesId, gameId, p))
                    }
                    "MISSING" -> state.filtered.filter { p ->
                        !ownedPersonas.contains(com.persona.companion.utils.FilterUtils.getPersonaId(seriesId, gameId, p))
                    }
                    else -> state.filtered
                }
            }

            if (totalCount > 0 && !state.isLoading) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "$ownedCount / $totalCount Registered",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (ownedCount == totalCount) Color(0xFF4CAF50) else TextSecondary,
                        fontWeight = FontWeight.SemiBold
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        listOf("ALL" to "All", "OWNED" to "Owned", "MISSING" to "Missing").forEach { (key, label) ->
                            val selected = ownershipFilter == key
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selected) series.color.copy(alpha = 0.2f) else SurfaceCard,
                                border = BorderStroke(
                                    1.dp,
                                    if (selected) series.color.copy(alpha = 0.6f) else HairlineSoft
                                ),
                                modifier = Modifier.clickable { ownershipFilter = key }
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (selected) series.color else TextSecondary,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            when {
                state.isLoading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = series.color)
                    }
                }
                displayedPersonas.isEmpty() -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(16.dp)) {
                            Text("No personas found", color = TextSecondary)
                            val errorMsg = state.errorMessage
                            if (errorMsg != null) {
                                Spacer(Modifier.height(8.dp))
                                Text(errorMsg, color = Color.Red, style = MaterialTheme.typography.bodySmall)
                            }
                            Spacer(Modifier.height(16.dp))
                            Text(state.debugInfo, color = TextDisabled, style = MaterialTheme.typography.bodySmall)
                        }
                    }
                }
                else -> {
                    // Display personas based on sort option
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .imePadding()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = PaddingValues(vertical = 12.dp)
                    ) {
                        when (state.sortBy) {
                            com.persona.companion.ui.viewmodels.SortOption.ARCANA -> {
                                // Group by arcana
                                val grouped = displayedPersonas.groupBy { it.arcana ?: "Unknown" }.entries.sortedBy { it.key }
                                grouped.forEach { (arcana, personas) ->
                                    item {
                                        ArcanaHeader(arcana, series.color)
                                    }
                                    itemsIndexed(personas, key = { index, persona -> "${arcana}_${persona.name}_$index" }) { _, persona ->
                                        val personaId = com.persona.companion.utils.FilterUtils.getPersonaId(seriesId, gameId, persona)
                                        PersonaRow(
                                            persona     = persona,
                                            accentColor = series.color,
                                            gameId      = gameId,
                                            isOwned     = ownedPersonas.contains(personaId),
                                            onToggleOwned = { userPrefs.toggleOwnedPersona(personaId) },
                                            onClick     = {
                                                navController.navigate(
                                                    Screen.PersonaDetail.createRoute(seriesId, gameId, persona.name)
                                                )
                                            },
                                            isCompact = isCompactView
                                        )
                                    }
                                    item { Spacer(Modifier.height(4.dp)) }
                                }
                            }
                            else -> {
                                // Flat list for level/name sorting
                                itemsIndexed(displayedPersonas, key = { index, persona -> "${persona.name}_$index" }) { _, persona ->
                                    val personaId = com.persona.companion.utils.FilterUtils.getPersonaId(seriesId, gameId, persona)
                                    PersonaRow(
                                        persona     = persona,
                                        accentColor = series.color,
                                        gameId      = gameId,
                                        isOwned     = ownedPersonas.contains(personaId),
                                        onToggleOwned = { userPrefs.toggleOwnedPersona(personaId) },
                                        onClick     = {
                                            navController.navigate(
                                                Screen.PersonaDetail.createRoute(seriesId, gameId, persona.name)
                                            )
                                        },
                                        isCompact = isCompactView
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
        
        // Filter Sheet
        if (showFilterSheet) {
            PersonaFilterSheet(
                filters = state.filters,
                onFiltersChanged = { filters ->
                    vm.setFilters(filters)
                },
                onDismiss = { showFilterSheet = false }
            )
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
private fun PersonaRow(
    persona: Persona,
    accentColor: Color,
    gameId: String = "",
    isOwned: Boolean = false,
    onToggleOwned: () -> Unit = {},
    onClick: () -> Unit,
    isCompact: Boolean = false
) {
    val context = LocalContext.current
    val shouldLoadImages = rememberShouldLoadImages()
    
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(if (isCompact) 8.dp else 10.dp),
        color = SurfaceCard,
        border = BorderStroke(1.dp, HairlineSoft)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(onClick = onClick)
                .padding(horizontal = if (isCompact) 12.dp else 16.dp, vertical = if (isCompact) 10.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
        // Persona image thumbnail (only if images are available)
        if (shouldLoadImages) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .personaImage(context, gameId, persona.name)
                    .crossfade(true)
                    .build(),
                contentDescription = persona.name,
                modifier = Modifier
                    .size(if (isCompact) 36.dp else 48.dp)
                    .clip(CircleShape)
                    .background(Surface),
                contentScale = ContentScale.Crop
            )
            
            Spacer(Modifier.width(if (isCompact) 10.dp else 12.dp))
        }
        
        // Level badge
        Surface(
            shape = RoundedCornerShape(if (isCompact) 6.dp else 8.dp),
            color = accentColor.copy(alpha = 0.15f)
        ) {
            Text(
                text  = "${persona.level}",
                style = if (isCompact) MaterialTheme.typography.labelMedium else MaterialTheme.typography.labelLarge,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = if (isCompact) 8.dp else 10.dp, vertical = if (isCompact) 4.dp else 6.dp)
            )
        }

        Spacer(Modifier.width(if (isCompact) 10.dp else 14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text  = persona.name,
                style = if (isCompact) MaterialTheme.typography.bodyLarge else MaterialTheme.typography.titleMedium,
                color = TextPrimary
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isCompact) {
                    Text(
                        text  = persona.arcana ?: "Unknown",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.width(8.dp))
                }
                WeaknessRow(persona.weaknesses)
            }
        }

        // Skill count hint & Compendium check toggle
        if (!isCompact) {
            val skillCount = persona.skills?.size ?: 0
            if (skillCount > 0) {
                Text(
                    text  = "$skillCount skills",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextDisabled
                )
                Spacer(Modifier.width(4.dp))
            }
        }
        IconButton(
            onClick = onToggleOwned,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = if (isOwned) Icons.Default.CheckCircle else Icons.Outlined.CheckCircle,
                contentDescription = if (isOwned) "Registered in Compendium" else "Mark as Registered",
                tint = if (isOwned) Color(0xFF4CAF50) else TextDisabled.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
        }
    }
    }
}


@Composable
private fun SortBar(
    currentSort: com.persona.companion.ui.viewmodels.SortOption,
    onSortChange: (com.persona.companion.ui.viewmodels.SortOption) -> Unit,
    accentColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        com.persona.companion.ui.viewmodels.SortOption.values().forEach { option ->
            val isSelected = currentSort == option
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onSortChange(option) },
                color = if (isSelected) accentColor.copy(alpha = 0.2f) else SurfaceCard,
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    text = when (option) {
                        com.persona.companion.ui.viewmodels.SortOption.ARCANA -> "Arcana"
                        com.persona.companion.ui.viewmodels.SortOption.LEVEL -> "Level"
                        com.persona.companion.ui.viewmodels.SortOption.NAME -> "Name"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) accentColor else TextSecondary,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp)
                )
            }
        }
    }
}
