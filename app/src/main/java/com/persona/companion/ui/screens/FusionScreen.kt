package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import com.persona.companion.fusion.ForwardFusionOption
import com.persona.companion.fusion.FusionRecipe
import com.persona.companion.ui.viewmodels.ForwardSubTab
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
fun ForwardFusionView(
    state: com.persona.companion.ui.viewmodels.FusionState,
    viewModel: FusionViewModel,
    onPersonaDetailClick: (String) -> Unit
) {
    var activePickerSlot by remember { mutableStateOf<Int?>(null) }
    var showSourcePicker by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Sub-mode tabs
        TabRow(
            selectedTabIndex = state.forwardSubTab.ordinal,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = state.forwardSubTab == com.persona.companion.ui.viewmodels.ForwardSubTab.CALCULATOR,
                onClick = { viewModel.setForwardSubTab(com.persona.companion.ui.viewmodels.ForwardSubTab.CALCULATOR) },
                text = { Text("Fusion Chamber", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = state.forwardSubTab == com.persona.companion.ui.viewmodels.ForwardSubTab.FROM_PERSONA,
                onClick = { viewModel.setForwardSubTab(com.persona.companion.ui.viewmodels.ForwardSubTab.FROM_PERSONA) },
                text = { Text("Recipes from Persona", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.forwardSubTab) {
            com.persona.companion.ui.viewmodels.ForwardSubTab.CALCULATOR -> {
                ForwardCalculatorTab(
                    state = state,
                    viewModel = viewModel,
                    onOpenPicker = { slotIndex -> activePickerSlot = slotIndex },
                    onPersonaDetailClick = onPersonaDetailClick
                )
            }
            com.persona.companion.ui.viewmodels.ForwardSubTab.FROM_PERSONA -> {
                ForwardFromPersonaTab(
                    state = state,
                    viewModel = viewModel,
                    onOpenPicker = { showSourcePicker = true },
                    onPersonaDetailClick = onPersonaDetailClick
                )
            }
        }
    }

    // Persona picker for Chamber slots (0, 1, 2)
    activePickerSlot?.let { slotIndex ->
        PersonaPickerDialog(
            title = when (slotIndex) {
                0 -> "Select 1st Ingredient"
                1 -> "Select 2nd Ingredient"
                else -> "Select 3rd Ingredient"
            },
            personas = state.personas,
            onDismiss = { activePickerSlot = null },
            onPersonaSelected = { persona ->
                viewModel.setIngredient(slotIndex, persona)
                activePickerSlot = null
            }
        )
    }

    // Persona picker for "From Persona" tab
    if (showSourcePicker) {
        PersonaPickerDialog(
            title = "Select Source Persona",
            personas = state.personas,
            onDismiss = { showSourcePicker = false },
            onPersonaSelected = { persona ->
                viewModel.setForwardSourcePersona(persona)
                showSourcePicker = false
            }
        )
    }
}

@Composable
fun ForwardCalculatorTab(
    state: com.persona.companion.ui.viewmodels.FusionState,
    viewModel: FusionViewModel,
    onOpenPicker: (Int) -> Unit,
    onPersonaDetailClick: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select Ingredients:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(
                        onClick = { viewModel.swapIngredients() },
                        enabled = state.selectedIngredients[0] != null || state.selectedIngredients[1] != null
                    ) {
                        Text("Swap")
                    }
                    TextButton(
                        onClick = { viewModel.clearIngredients() },
                        enabled = state.selectedIngredients.any { it != null }
                    ) {
                        Text("Clear All")
                    }
                }
            }
        }

        // Slot 1
        item {
            IngredientSlotCard(
                label = "1st Ingredient",
                persona = state.selectedIngredients.getOrNull(0),
                isRequired = true,
                onSelectClick = { onOpenPicker(0) },
                onClearClick = { viewModel.setIngredient(0, null) }
            )
        }

        // Slot 2
        item {
            IngredientSlotCard(
                label = "2nd Ingredient",
                persona = state.selectedIngredients.getOrNull(1),
                isRequired = true,
                onSelectClick = { onOpenPicker(1) },
                onClearClick = { viewModel.setIngredient(1, null) }
            )
        }

        // Slot 3 (Optional)
        item {
            IngredientSlotCard(
                label = "3rd Ingredient (Optional / Triangle)",
                persona = state.selectedIngredients.getOrNull(2),
                isRequired = false,
                onSelectClick = { onOpenPicker(2) },
                onClearClick = { viewModel.setIngredient(2, null) }
            )
        }

        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Fusion Result",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            val activeCount = state.selectedIngredients.filterNotNull().size
            if (activeCount < 2) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(28.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Select at least 2 ingredients to see the fused Persona",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
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
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.Top
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    if (viewModel.isSpecialFusion(result.name)) {
                                        Surface(
                                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                            shape = RoundedCornerShape(4.dp),
                                            modifier = Modifier.padding(bottom = 4.dp)
                                        ) {
                                            Text(
                                                text = "★ SPECIAL FUSION",
                                                style = MaterialTheme.typography.labelSmall,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.tertiary,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                    }
                                    Text(
                                        text = result.name,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${result.arcana} • Lv. ${result.level}",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "Lv. ${result.level ?: 0}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                    )
                                }
                            }

                            // Weakness / Resistances Row
                            Spacer(modifier = Modifier.height(12.dp))
                            PersonaResistancesRow(persona = result)

                            // Stats Preview
                            if (result.stats != null && result.stats.size >= 5) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    val statLabels = listOf("St", "Ma", "En", "Ag", "Lu")
                                    statLabels.forEachIndexed { i, label ->
                                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                            Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                            Text(
                                                text = "${result.stats.getOrNull(i) ?: 0}",
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = TextPrimary
                                            )
                                        }
                                    }
                                }
                            }

                            // Estimated Summon Cost
                            val cost = viewModel.getPersonaCost(result)
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Estimated Summon Cost:",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                                Text(
                                    text = "¥ " + String.format("%,d", cost),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFFFD700)
                                )
                            }

                            // Unlock requirements
                            if (!result.unlock.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Surface(
                                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.3f),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "Unlock: ${result.unlock}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onErrorContainer,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = { onPersonaDetailClick(result.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("View Persona Details")
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(Icons.Default.ChevronRight, contentDescription = null, modifier = Modifier.size(18.dp))
                            }
                        }
                    }
                } else {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp),
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
}

@Composable
fun ForwardFromPersonaTab(
    state: com.persona.companion.ui.viewmodels.FusionState,
    viewModel: FusionViewModel,
    onOpenPicker: () -> Unit,
    onPersonaDetailClick: (String) -> Unit
) {
    val source = state.forwardSourcePersona
    var searchQuery by remember { mutableStateOf("") }

    val filteredOptions = remember(state.forwardOptions, searchQuery) {
        if (searchQuery.isBlank()) {
            state.forwardOptions
        } else {
            state.forwardOptions.filter {
                it.result.name.contains(searchQuery, ignoreCase = true) ||
                it.result.arcana?.contains(searchQuery, ignoreCase = true) == true ||
                it.otherIngredient.name.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = "Source Persona:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            if (source == null) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onOpenPicker() },
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(18.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "+ Select Source Persona",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "See every Persona you can fuse from your stock",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Icon(Icons.Default.Add, contentDescription = "Select", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
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
                                text = source.name,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${source.arcana} • Lv. ${source.level}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            TextButton(onClick = { onOpenPicker() }) {
                                Text("Change")
                            }
                            IconButton(onClick = { viewModel.setForwardSourcePersona(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    }
                }
            }
        }

        if (source != null) {
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Filter result name, arcana, or ingredient...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )
            }

            item {
                Text(
                    text = "${filteredOptions.size} Possible Fusions Found",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            items(filteredOptions) { option ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPersonaDetailClick(option.result.name) },
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+ ${option.otherIngredient.name}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextSecondary
                                )
                                Text(
                                    text = " (${option.otherIngredient.arcana} Lv.${option.otherIngredient.level})",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary.copy(alpha = 0.7f)
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "= ${option.result.name}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                if (option.isSpecial) {
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Surface(
                                        color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "★ Special",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "${option.result.arcana} • Lv. ${option.result.level}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }

                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "Details",
                            tint = TextSecondary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun IngredientSlotCard(
    label: String,
    persona: com.persona.companion.models.Persona?,
    isRequired: Boolean,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit
) {
    if (persona == null) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onSelectClick() },
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(
                1.dp,
                if (isRequired) MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f)
            )
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
                        text = "+ $label",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = if (isRequired) "Required ingredient" else "Optional for 3-way fusion",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Icon(Icons.Default.Add, contentDescription = "Add", tint = MaterialTheme.colorScheme.primary)
            }
        }
    } else {
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelectClick() }
                ) {
                    Text(
                        text = persona.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${persona.arcana} • Lv. ${persona.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onSelectClick) {
                        Text("Change")
                    }
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaPickerDialog(
    title: String,
    personas: List<com.persona.companion.models.Persona>,
    onDismiss: () -> Unit,
    onPersonaSelected: (com.persona.companion.models.Persona) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedArcana by remember { mutableStateOf<String?>(null) }

    val allArcana = remember(personas) {
        listOf("All") + personas.mapNotNull { it.arcana }.distinct().sorted()
    }

    val filtered = remember(personas, searchQuery, selectedArcana) {
        personas.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                    p.name.contains(searchQuery, ignoreCase = true) ||
                    p.arcana?.contains(searchQuery, ignoreCase = true) == true
            val matchesArcana = selectedArcana == null || selectedArcana == "All" || p.arcana == selectedArcana
            matchesQuery && matchesArcana
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.85f),
        properties = androidx.compose.ui.window.DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .fillMaxHeight(),
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                // Search Bar
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    placeholder = { Text("Search by name or arcana...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp)
                )

                // Arcana Filter Chips
                androidx.compose.foundation.lazy.LazyRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(allArcana) { arcana ->
                        val isSelected = (selectedArcana == null && arcana == "All") || selectedArcana == arcana
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedArcana = if (arcana == "All") null else arcana },
                            label = { Text(arcana, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

                // Persona List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    items(filtered) { persona ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPersonaSelected(persona) },
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(
                                        text = persona.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "${persona.arcana}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondary
                                    )
                                }
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        text = "Lv. ${persona.level ?: 0}",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
