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
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.ui.viewmodels.FusionViewModel
import androidx.compose.material.icons.filled.ChevronRight
import com.persona.companion.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FusionScreen(
    seriesId: String,
    gameId: String,
    dataPath: String,
    onBack: () -> Unit,
    onPersonaClick: (String) -> Unit = {},
    viewModel: FusionViewModel = viewModel()
) {
    val context = LocalContext.current
    val state by viewModel.state.collectAsState()
    
    LaunchedEffect(dataPath) {
        viewModel.loadData(context, seriesId, gameId, dataPath)
    }
    
    Scaffold(
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Fusion Calculator") },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.Default.ArrowBack, "Back")
                        }
                    }
                )
                
                TabRow(
                    selectedTabIndex = state.calculatorMode.ordinal,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = state.calculatorMode == com.persona.companion.ui.viewmodels.CalculatorMode.REVERSE,
                        onClick = { viewModel.setCalculatorMode(com.persona.companion.ui.viewmodels.CalculatorMode.REVERSE) },
                        text = { Text("Reverse Lookup") }
                    )
                    Tab(
                        selected = state.calculatorMode == com.persona.companion.ui.viewmodels.CalculatorMode.FORWARD,
                        onClick = { viewModel.setCalculatorMode(com.persona.companion.ui.viewmodels.CalculatorMode.FORWARD) },
                        text = { Text("Forward Fusion") }
                    )
                }
            }
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
                state.calculatorMode == com.persona.companion.ui.viewmodels.CalculatorMode.FORWARD -> {
                    ForwardFusionView(
                        state = state,
                        viewModel = viewModel,
                        onPersonaDetailClick = onPersonaClick
                    )
                }
                state.selectedPersona != null -> {
                    if (state.fusionType == null) {
                        FusionTypeSelectionView(
                            persona = state.selectedPersona!!,
                            onTypeSelected = { viewModel.selectFusionType(it) },
                            onBack = { viewModel.clearSelection() }
                        )
                    } else {
                        FusionResultsView(
                            viewModel = viewModel,
                            state = state
                        )
                    }
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
fun FusionTypeSelectionView(
    persona: com.persona.companion.models.Persona,
    onTypeSelected: (com.persona.companion.ui.viewmodels.FusionType) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = persona.name,
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${persona.arcana} • Lv. ${persona.level}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }
        }

        Text(
            text = "Choose Fusion Type",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Button(
            onClick = { onTypeSelected(com.persona.companion.ui.viewmodels.FusionType.NORMAL) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
        ) {
            Text(
                text = "Normal Reverse Fissions (2-Persona)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = { onTypeSelected(com.persona.companion.ui.viewmodels.FusionType.TRIPLE) },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
        ) {
            Text(
                text = "Triple Reverse Fissions (3-Persona)",
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        TextButton(onClick = onBack) {
            Text("Back to Persona Selection")
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

enum class RecipeSortOption {
    DEFAULT, CHEAPEST, LOWEST_LEVEL, HIGHEST_LEVEL
}

@Composable
fun FusionResultsView(
    viewModel: FusionViewModel,
    state: com.persona.companion.ui.viewmodels.FusionState
) {
    var sortOption by remember { mutableStateOf(RecipeSortOption.DEFAULT) }

    val sortedRecipes = remember(state.fusionRecipes, sortOption) {
        when (sortOption) {
            RecipeSortOption.DEFAULT -> state.fusionRecipes
            RecipeSortOption.CHEAPEST -> state.fusionRecipes.sortedBy { viewModel.getRecipeCost(it) }
            RecipeSortOption.LOWEST_LEVEL -> state.fusionRecipes.sortedBy { recipe ->
                recipe.personas.maxOfOrNull { it.level ?: 0 } ?: 0
            }
            RecipeSortOption.HIGHEST_LEVEL -> state.fusionRecipes.sortedByDescending { recipe ->
                recipe.personas.maxOfOrNull { it.level ?: 0 } ?: 0
            }
        }
    }

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
                    val isSpecial = state.selectedPersona?.let { viewModel.isSpecialFusion(it.name) } == true
                    Text(
                        text = "${state.selectedPersona?.arcana} • Lv. ${state.selectedPersona?.level} • ${if (isSpecial) "Advanced" else if (state.fusionType == com.persona.companion.ui.viewmodels.FusionType.NORMAL) "Normal" else "Triple"}",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
                Row {
                    val isSpecial = state.selectedPersona?.let { viewModel.isSpecialFusion(it.name) } == true
                    if (!isSpecial) {
                        IconButton(onClick = { viewModel.selectPersona(state.selectedPersona!!) }) {
                            Icon(Icons.Default.ArrowBack, "Back to Type Selection")
                        }
                    }
                    IconButton(onClick = { viewModel.clearSelection() }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            }
            
            // Adding Resistances here
            state.selectedPersona?.let { persona ->
                Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                    Text(
                        text = "Resistances",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    PersonaResistancesRow(persona)
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
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${state.fusionRecipes.size} recipe(s) found",
                    style = MaterialTheme.typography.titleMedium
                )
                
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(
                        RecipeSortOption.DEFAULT to "Default",
                        RecipeSortOption.CHEAPEST to "Cheapest",
                        RecipeSortOption.LOWEST_LEVEL to "Lowest Lvl"
                    ).forEach { (opt, label) ->
                        val isSelected = sortOption == opt
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier
                                .clickable { sortOption = opt }
                        ) {
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelMedium,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(sortedRecipes) { recipe ->
                    FusionRecipeCard(
                        recipe = recipe,
                        estimatedCost = viewModel.getRecipeCost(recipe),
                        onPersonaClick = { persona ->
                            viewModel.selectPersona(persona)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun FusionRecipeCard(
    recipe: com.persona.companion.fusion.FusionRecipe,
    estimatedCost: Int,
    onPersonaClick: (com.persona.companion.models.Persona) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            if (estimatedCost > 0) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(start = 16.dp, end = 16.dp, top = 12.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    Text(
                        text = "Est. Cost: ¥${String.format("%,d", estimatedCost)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Use Column for 3+ personas to avoid squishing
            if (recipe.personas.size > 2) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    recipe.personas.forEachIndexed { index, persona ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Plus icon (except for first persona)
                            if (index > 0) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = "Plus",
                                    modifier = Modifier.padding(end = 12.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            } else {
                                Spacer(modifier = Modifier.width(36.dp))
                            }

                            // Persona info
                            Column(
                                modifier = Modifier
                                    .weight(1f)
                                    .clickable { onPersonaClick(persona) }
                                    .padding(8.dp)
                            ) {
                                Text(
                                    text = persona.name,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                                Text(
                                    text = "${persona.arcana} • Lv. ${persona.level}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            } else {
                // Original horizontal layout for 2-persona fusions
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    recipe.personas.forEachIndexed { index, persona ->
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { onPersonaClick(persona) }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = persona.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "${persona.arcana} • Lv. ${persona.level}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        if (index < recipe.personas.size - 1) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Plus",
                                modifier = Modifier.padding(horizontal = 4.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}
@Composable
fun PersonaResistancesRow(persona: com.persona.companion.models.Persona) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        val elementalAffinities = listOf(
            Triple("Phys", persona.weaknesses.contains("Phys"), persona.resistances.contains("Phys")),
            Triple("Fire", persona.weaknesses.contains("Fire"), persona.resistances.contains("Fire")),
            Triple("Ice", persona.weaknesses.contains("Ice"), persona.resistances.contains("Ice")),
            Triple("Elec", persona.weaknesses.contains("Elec"), persona.resistances.contains("Elec")),
            Triple("Wind", persona.weaknesses.contains("Wind"), persona.resistances.contains("Wind")),
            Triple("Light", persona.weaknesses.contains("Light"), persona.resistances.contains("Light")),
            Triple("Dark", persona.weaknesses.contains("Dark"), persona.resistances.contains("Dark"))
        )

        elementalAffinities.forEach { (name, isWeak, isResist) ->
            ElementChip(name, isWeak, isResist)
        }
    }
}

@Composable
fun ElementChip(name: String, isWeak: Boolean, isResist: Boolean) {
    val iconRes = when (name) {
        "Fire" -> com.persona.companion.R.drawable.ic_fire
        "Ice" -> com.persona.companion.R.drawable.ic_ice
        "Elec" -> com.persona.companion.R.drawable.ic_elec
        "Wind" -> com.persona.companion.R.drawable.ic_wind
        "Light" -> com.persona.companion.R.drawable.ic_light
        "Dark" -> com.persona.companion.R.drawable.ic_dark
        "Phys" -> com.persona.companion.R.drawable.ic_phys
        else -> null
    }

    val tint = when {
        isWeak -> Color(0xFFE57373)
        isResist -> Color(0xFF81C784)
        else -> MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
    }

    if (iconRes != null) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                contentDescription = name,
                modifier = Modifier.size(16.dp),
                tint = tint
            )
            if (isWeak) {
                Text("Wk", fontSize = 8.sp, color = tint)
            } else if (isResist) {
                Text("Str", fontSize = 8.sp, color = tint)
            }
        }
    }
}

@Composable
fun SearchablePersonaDropdown(
    label: String,
    personas: List<com.persona.companion.models.Persona>,
    selectedPersona: com.persona.companion.models.Persona?,
    onPersonaSelected: (com.persona.companion.models.Persona?) -> Unit
) {
    var searchQuery by remember(selectedPersona) { mutableStateOf(selectedPersona?.name ?: "") }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    val filtered = remember(personas, searchQuery) {
        if (searchQuery.isBlank()) {
            personas
        } else {
            personas.filter { it.name.contains(searchQuery, ignoreCase = true) }
        }
    }

    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
                searchQuery = it
                isDropdownExpanded = true
                if (it.isBlank()) {
                    onPersonaSelected(null)
                } else {
                    val match = personas.find { p -> p.name.equals(it, ignoreCase = true) }
                    if (match != null) onPersonaSelected(match)
                }
            },
            label = { Text(label) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            trailingIcon = {
                if (selectedPersona != null || searchQuery.isNotEmpty()) {
                    IconButton(onClick = {
                        searchQuery = ""
                        onPersonaSelected(null)
                        isDropdownExpanded = false
                    }) {
                        Icon(Icons.Default.Close, "Clear")
                    }
                }
            }
        )

        if (isDropdownExpanded && filtered.isNotEmpty() && selectedPersona?.name != searchQuery) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 200.dp)
                    .padding(top = 4.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                LazyColumn(modifier = Modifier.fillMaxWidth()) {
                    items(filtered) { persona ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    searchQuery = persona.name
                                    onPersonaSelected(persona)
                                    isDropdownExpanded = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(persona.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${persona.arcana} • Lv. ${persona.level}", color = TextSecondary)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ForwardFusionView(
    state: com.persona.companion.ui.viewmodels.FusionState,
    viewModel: FusionViewModel,
    onPersonaDetailClick: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Select 2 or 3 ingredients to fuse:",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 12.dp),
            color = TextPrimary
        )

        // Ingredient 1
        SearchablePersonaDropdown(
            label = "1st Ingredient",
            personas = state.personas,
            selectedPersona = state.selectedIngredients.getOrNull(0),
            onPersonaSelected = { viewModel.setIngredient(0, it) }
        )

        // Ingredient 2
        SearchablePersonaDropdown(
            label = "2nd Ingredient",
            personas = state.personas,
            selectedPersona = state.selectedIngredients.getOrNull(1),
            onPersonaSelected = { viewModel.setIngredient(1, it) }
        )

        // Ingredient 3 (Optional)
        SearchablePersonaDropdown(
            label = "3rd Ingredient (Optional)",
            personas = state.personas,
            selectedPersona = state.selectedIngredients.getOrNull(2),
            onPersonaSelected = { viewModel.setIngredient(2, it) }
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Result Card
        Text(
            text = "Fusion Result",
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        val activeIngredientsCount = state.selectedIngredients.filterNotNull().size
        if (activeIngredientsCount < 2) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = SurfaceCard)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Please select at least 2 ingredients",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            val result = state.forwardResult
            if (result != null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPersonaDetailClick(result.name) },
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = result.name,
                                style = MaterialTheme.typography.headlineSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                text = "${result.arcana} • Lv. ${result.level}",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                                modifier = Modifier.padding(top = 4.dp)
                            )
                        }
                        Icon(
                            imageVector = Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Invalid Fusion Combination",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
