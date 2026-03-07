package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.ui.viewmodels.FusionViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FusionScreen(
    seriesId: String,
    gameId: String,
    dataPath: String,
    onBack: () -> Unit,
    viewModel: FusionViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(dataPath) {
        viewModel.loadData(context, seriesId, gameId, dataPath)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Fusion Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when {
                state.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                state.errorMessage != null -> {
                    Text(
                        text = state.errorMessage ?: "",
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(16.dp),
                        color = MaterialTheme.colorScheme.error
                    )
                }
                state.selectedPersona != null -> {
                    FusionResultsView(
                        viewModel = viewModel,
                        state = state
                    )
                }
                else -> {
                    PersonaSelectionView(
                        personas = state.personas,
                        onPersonaSelected = { viewModel.selectPersona(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun PersonaSelectionView(
    personas: List<com.persona.companion.models.Persona>,
    onPersonaSelected: (com.persona.companion.models.Persona) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    val filteredPersonas = remember(personas, searchQuery) {
        if (searchQuery.isBlank()) {
            personas
        } else {
            personas.filter { 
                it.name.contains(searchQuery, ignoreCase = true) ||
                it.arcana?.contains(searchQuery, ignoreCase = true) == true
            }
        }
    }
    
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Select a Persona to see fusion recipes",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.titleMedium
        )
        
        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            placeholder = { Text("Search by name or arcana...") },
            singleLine = true,
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Clear"
                        )
                    }
                }
            }
        )
        
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredPersonas) { persona ->
                PersonaListItem(
                    persona = persona,
                    onClick = { onPersonaSelected(persona) }
                )
            }
        }
    }
}

@Composable
fun PersonaListItem(
    persona: com.persona.companion.models.Persona,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = persona.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${persona.arcana ?: "Unknown"} • Lv. ${persona.level ?: "?"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun FusionResultsView(
    viewModel: FusionViewModel,
    state: com.persona.companion.ui.viewmodels.FusionState
) {
    Column(modifier = Modifier.fillMaxSize()) {
        // Header with selected persona
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = state.selectedPersona?.name ?: "",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${state.selectedPersona?.arcana} • Lv. ${state.selectedPersona?.level}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                IconButton(onClick = { viewModel.clearSelection() }) {
                    Icon(Icons.Default.Close, "Clear")
                }
            }
        }
        
        // Fusion recipes
        if (state.fusionRecipes.isEmpty()) {
            Text(
                text = "No fusion recipes found for this persona",
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                text = "${state.fusionRecipes.size} fusion recipe(s) found",
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.fusionRecipes) { recipe ->
                    FusionRecipeCard(recipe = recipe)
                }
            }
        }
    }
}

@Composable
fun FusionRecipeCard(recipe: com.persona.companion.fusion.FusionRecipe) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First persona
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.persona1.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recipe.persona1.arcana} • Lv. ${recipe.persona1.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // Plus icon
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = "Plus",
                modifier = Modifier.padding(horizontal = 8.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            
            // Second persona
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = recipe.persona2.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${recipe.persona2.arcana} • Lv. ${recipe.persona2.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
