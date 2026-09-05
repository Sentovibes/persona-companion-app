package com.persona.companion.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ChevronRight
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
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.fusion.ForwardFusionOption
import com.persona.companion.fusion.FusionRecipe
import com.persona.companion.models.Persona
import com.persona.companion.ui.theme.*
import androidx.compose.foundation.lazy.LazyRow
import com.persona.companion.ui.viewmodels.CalculatorMode
import com.persona.companion.ui.viewmodels.ForwardSubTab
import com.persona.companion.ui.viewmodels.FusionType
import com.persona.companion.ui.viewmodels.FusionViewModel
import com.persona.companion.ui.viewmodels.RecipeSortOrder

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
        containerColor = Background,
        topBar = {
            Column {
                TopAppBar(
                    title = { Text("Fusion Calculator", color = TextPrimary) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back", tint = TextPrimary)
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
                )

                TabRow(
                    selectedTabIndex = state.calculatorMode.ordinal,
                    containerColor = Surface,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Tab(
                        selected = state.calculatorMode == CalculatorMode.REVERSE,
                        onClick = { viewModel.setCalculatorMode(CalculatorMode.REVERSE) },
                        text = { Text("Reverse Lookup", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.calculatorMode == CalculatorMode.FORWARD,
                        onClick = { viewModel.setCalculatorMode(CalculatorMode.FORWARD) },
                        text = { Text("Forward Fusion", fontWeight = FontWeight.SemiBold) }
                    )
                    Tab(
                        selected = state.calculatorMode == CalculatorMode.SKILL_ROUTES,
                        onClick = { viewModel.setCalculatorMode(CalculatorMode.SKILL_ROUTES) },
                        text = { Text("Skill Routes", fontWeight = FontWeight.SemiBold) }
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
                state.calculatorMode == CalculatorMode.SKILL_ROUTES -> {
                    SkillRoutesView(
                        state = state,
                        viewModel = viewModel,
                        onPersonaDetailClick = onPersonaClick
                    )
                }
                state.calculatorMode == CalculatorMode.FORWARD -> {
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

// ---------------------------------------------------------------------------
// Skill Routes View
// ---------------------------------------------------------------------------

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SkillRoutesView(
    state: com.persona.companion.ui.viewmodels.FusionState,
    viewModel: FusionViewModel,
    onPersonaDetailClick: (String) -> Unit
) {
    var showTargetPicker by remember { mutableStateOf(false) }
    var skillSearchQuery by remember { mutableStateOf("") }
    var showSkillSuggestions by remember { mutableStateOf(false) }

    val popularSkills = listOf(
        "Spell Master", "Victory Cry", "Megidolaon", "Debilitate",
        "Heat Riser", "Ali Dance", "Drain Phys", "Charge",
        "Concentrate", "Enduring Soul", "Insta-Heal", "Arms Master"
    )

    val matchingSkills = remember(skillSearchQuery, state.allAvailableSkills) {
        if (skillSearchQuery.isBlank()) emptyList()
        else state.allAvailableSkills.filter {
            it.contains(skillSearchQuery, ignoreCase = true) && !state.skillRouteSkills.contains(it)
        }.take(8)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Target Persona (Optional)
        item {
            Text(
                text = "Target Persona (Optional):",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        item {
            val target = state.skillRouteTarget
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showTargetPicker = true },
                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(
                    1.dp,
                    if (target != null) MaterialTheme.colorScheme.primary else Hairline
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (target != null) {
                        Column {
                            Text(target.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${target.arcana} • Lv. ${target.level}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            TextButton(onClick = { showTargetPicker = true }) {
                                Text("Change")
                            }
                            IconButton(onClick = { viewModel.setSkillRouteTarget(null) }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    } else {
                        Text("+ Select Target Persona (or leave empty)", style = MaterialTheme.typography.bodyMedium, color = TextSecondary)
                        TextButton(onClick = { showTargetPicker = true }) {
                            Text("Browse")
                        }
                    }
                }
            }
        }

        // 2. Target Skills Selection
        item {
            Text(
                text = "Target Skills:",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        // Selected Skills Chips
        if (state.skillRouteSkills.isNotEmpty()) {
            item {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    state.skillRouteSkills.forEach { skill ->
                        InputChip(
                            selected = true,
                            onClick = { viewModel.removeSkillRouteSkill(skill) },
                            label = { Text(skill, fontWeight = FontWeight.Bold) },
                            trailingIcon = {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(16.dp))
                            },
                            colors = InputChipDefaults.inputChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                    TextButton(onClick = { viewModel.clearSkillRouteSkills() }) {
                        Text("Clear All", fontSize = 12.sp)
                    }
                }
            }
        }

        // Search Skill Input
        item {
            OutlinedTextField(
                value = skillSearchQuery,
                onValueChange = {
                    skillSearchQuery = it
                    showSkillSuggestions = it.isNotBlank()
                },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Search & add a skill...", color = TextSecondary.copy(alpha = 0.6f)) },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                trailingIcon = {
                    if (skillSearchQuery.isNotEmpty()) {
                        IconButton(onClick = {
                            skillSearchQuery = ""
                            showSkillSuggestions = false
                        }) {
                            Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                        }
                    }
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = SurfaceCard,
                    unfocusedContainerColor = SurfaceCard,
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = HairlineStrong
                ),
                shape = RoundedCornerShape(10.dp),
                singleLine = true
            )
        }

        // Skill Autocomplete Suggestions
        if (showSkillSuggestions && matchingSkills.isNotEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceRaised),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        matchingSkills.forEach { sk ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.addSkillRouteSkill(sk)
                                        skillSearchQuery = ""
                                        showSkillSuggestions = false
                                    }
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(sk, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                Text("+ Add", color = MaterialTheme.colorScheme.primary, fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Quick Suggestions Bar
        item {
            Column {
                Text("Popular Skills:", style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.padding(bottom = 6.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    popularSkills.forEach { sk ->
                        val isAdded = state.skillRouteSkills.contains(sk)
                        AssistChip(
                            onClick = {
                                if (isAdded) viewModel.removeSkillRouteSkill(sk)
                                else viewModel.addSkillRouteSkill(sk)
                            },
                            label = { Text(sk, fontSize = 12.sp) },
                            leadingIcon = if (isAdded) {
                                { Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(14.dp)) }
                            } else null,
                            colors = AssistChipDefaults.assistChipColors(
                                containerColor = if (isAdded) MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) else SurfaceCard,
                                labelColor = if (isAdded) MaterialTheme.colorScheme.primary else TextSecondary
                            )
                        )
                    }
                }
            }
        }

        // 3. Results Section
        if (state.skillRouteSkills.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
                        Text(
                            "Select 1 or more target skills above to compute inheritance fusion routes and direct learners.",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            // Section A: Direct Learners
            item {
                Text(
                    text = "Direct Learners (${state.skillLearners.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            if (state.skillLearners.isEmpty()) {
                item {
                    Text("No personas naturally learn these skills.", color = TextSecondary, fontSize = 13.sp)
                }
            } else {
                items(state.skillLearners) { learner ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPersonaDetailClick(learner.personaName) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(learner.personaName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${learner.arcana} • Base Lv. ${learner.level}", color = TextSecondary, fontSize = 12.sp)
                            }
                            Surface(
                                color = Color(0xFF81C784).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = if (learner.skillLevel < 1) "${learner.skillName} (Innate)" else "${learner.skillName} (Lv. ${learner.skillLevel})",
                                    color = Color(0xFF81C784),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section B: Itemization / Skill Cards
            if (state.skillItemizers.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Electric Chair / Itemization (${state.skillItemizers.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(state.skillItemizers) { itemizer ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPersonaDetailClick(itemizer.personaName) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(itemizer.personaName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                Text("${itemizer.arcana} • Base Lv. ${itemizer.level}", color = TextSecondary, fontSize = 12.sp)
                            }
                            Surface(
                                color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                shape = RoundedCornerShape(6.dp)
                            ) {
                                Text(
                                    text = itemizer.itemDescription,
                                    color = Color(0xFFFFD700),
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Section C: Multi-Tree Fusion Recipes
            if (state.skillMultiTrees.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Multi-Skill Fusion Trees (${state.skillMultiTrees.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }

                items(state.skillMultiTrees) { tree ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPersonaDetailClick(tree.targetPersona) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text(
                                text = "Branch A: ${tree.parentA} [learns ${tree.parentASkill}]",
                                color = Color(0xFF81C784),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Text(
                                text = "+ Branch B: ${tree.parentB} [learns ${tree.parentBSkill}]",
                                color = Color(0xFF64B5F6),
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "=> Result: ${tree.targetPersona} (Inherits both skills!)",
                                color = MaterialTheme.colorScheme.primary,
                                fontWeight = FontWeight.ExtraBold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }

            // Section D: Single Direct Routes
            if (state.singleDirectRoutes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Direct Inheritance Recipes (${state.singleDirectRoutes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                items(state.singleDirectRoutes) { route ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPersonaDetailClick(route.targetName) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Surface(
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                shape = RoundedCornerShape(4.dp)
                            ) {
                                Text(
                                    text = if (route.type == "special_direct") "Special Recipe" else "2-Persona Recipe",
                                    color = MaterialTheme.colorScheme.primary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            if (route.type == "special_direct") {
                                Text("Ingredient: ${route.sourcePersona} [learns ${route.skill}]", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("With: ${route.allIngredients?.filter { it != route.sourcePersona }?.joinToString(", ")}", color = TextSecondary, fontSize = 12.sp)
                            } else {
                                Text("Ingredient: ${route.sourcePersona} [learns ${route.skill}]", color = Color(0xFF81C784), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                Text("+ Partner: ${route.partner}", color = Color(0xFF64B5F6), fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("=> Result: ${route.targetName} (Inherits skill!)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.ExtraBold, fontSize = 14.sp)
                        }
                    }
                }
            }

            // Section E: Single Two-Step Routes
            if (state.singleTwoStepRoutes.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Multi-Step Fusion Pathways (${state.singleTwoStepRoutes.size})",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                }
                itemsIndexed(state.singleTwoStepRoutes) { index, route ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onPersonaDetailClick(route.step2Result) },
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Surface(
                                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text("Pathway #${index + 1}", color = MaterialTheme.colorScheme.primary, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                                }
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("2 Steps", color = TextSecondary, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            
                            // Step 1
                            Surface(color = Background, shape = RoundedCornerShape(6.dp)) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                                    Text("Step 1: Learn ${route.skill}", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text("${route.step1P1} [learns] + ${route.step1P2}", color = Color(0xFF81C784), fontSize = 12.sp)
                                    Text("=> Result: ${route.step1Result}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                            
                            Icon(imageVector = Icons.Default.ArrowDownward, contentDescription = "Next Step", tint = TextSecondary, modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 4.dp).size(16.dp))
                            
                            // Step 2
                            Surface(color = Background, shape = RoundedCornerShape(6.dp)) {
                                Column(modifier = Modifier.padding(8.dp).fillMaxWidth()) {
                                    Text("Step 2: Final Fusion", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                    Spacer(modifier = Modifier.height(4.dp))
                                    if (route.type == "2step_special") {
                                        Text("Special Recipe: ${route.step2SpecialRecipe?.joinToString(", ")}", color = Color(0xFF64B5F6), fontSize = 12.sp)
                                    } else {
                                        Text("${route.step2P1} [from Step 1] + ${route.step2P2}", color = Color(0xFF64B5F6), fontSize = 12.sp)
                                    }
                                    Text("=> Target: ${route.step2Result} (Inherits!)", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Persona picker for Target Persona
    if (showTargetPicker) {
        PersonaPickerDialog(
            title = "Select Target Persona",
            personas = state.personas,
            onDismiss = { showTargetPicker = false },
            onPersonaSelected = { persona ->
                viewModel.setSkillRouteTarget(persona)
                showTargetPicker = false
            }
        )
    }
}

// ---------------------------------------------------------------------------
// Forward Fusion View
// ---------------------------------------------------------------------------

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
            containerColor = SurfaceCard,
            contentColor = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
        ) {
            Tab(
                selected = state.forwardSubTab == ForwardSubTab.CALCULATOR,
                onClick = { viewModel.setForwardSubTab(ForwardSubTab.CALCULATOR) },
                text = { Text("Fusion Chamber", fontWeight = FontWeight.SemiBold) }
            )
            Tab(
                selected = state.forwardSubTab == ForwardSubTab.FROM_PERSONA,
                onClick = { viewModel.setForwardSubTab(ForwardSubTab.FROM_PERSONA) },
                text = { Text("Recipes from Persona", fontWeight = FontWeight.SemiBold) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        when (state.forwardSubTab) {
            ForwardSubTab.CALCULATOR -> {
                ForwardCalculatorTab(
                    state = state,
                    viewModel = viewModel,
                    onOpenPicker = { slotIndex -> activePickerSlot = slotIndex },
                    onPersonaDetailClick = onPersonaDetailClick
                )
            }
            ForwardSubTab.FROM_PERSONA -> {
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

@OptIn(ExperimentalLayoutApi::class)
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
                text = "Fusion Result (Compendium Profile)",
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
                            text = "Select at least 2 ingredients above to fuse",
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
                            containerColor = SurfaceCard
                        ),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(
                            1.5.dp,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(18.dp)) {
                            // 1. Hero Header
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
                                        text = "${result.arcana ?: "Unknown"} Arcana",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                    if (!result.trait.isNullOrBlank()) {
                                        Text(
                                            text = "Trait: ${result.trait}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            modifier = Modifier.padding(top = 2.dp)
                                        )
                                    }
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

                            // 2. Lore / Description
                            if (!result.description.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Text(
                                    text = result.description,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }

                            // 3. Itemization / Transmutes
                            if (!result.item.isNullOrBlank() || !result.itemr.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    if (!result.item.isNullOrBlank()) {
                                        Row {
                                            Text("Electric Chair: ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                            Text(result.item, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFD700), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (!result.itemr.isNullOrBlank()) {
                                        Row {
                                            Text("Fusion Alarm: ", style = MaterialTheme.typography.labelSmall, color = TextSecondary)
                                            Text(result.itemr, style = MaterialTheme.typography.labelSmall, color = Color(0xFFFFB74D), fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 4. Base Stats Bars
                            if (result.stats != null && result.stats.size >= 5) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Base Stats", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                val statLabels = listOf("STR", "MAG", "END", "AGI", "LUK")
                                val maxStat = result.stats.maxOrNull()?.coerceAtLeast(1) ?: 1
                                statLabels.forEachIndexed { i, label ->
                                    val v = result.stats[i]
                                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(vertical = 2.dp)) {
                                        Text(label, style = MaterialTheme.typography.labelSmall, color = TextSecondary, modifier = Modifier.width(32.dp))
                                        LinearProgressIndicator(
                                            progress = { v.toFloat() / maxStat.toFloat() },
                                            modifier = Modifier
                                                .weight(1f)
                                                .height(6.dp)
                                                .clip(RoundedCornerShape(3.dp)),
                                            color = MaterialTheme.colorScheme.primary,
                                            trackColor = SurfaceRaised
                                        )
                                        Text("$v", style = MaterialTheme.typography.labelSmall, color = TextPrimary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 8.dp).width(20.dp))
                                    }
                                }
                            }

                            // 5. Affinities
                            Spacer(modifier = Modifier.height(12.dp))
                            Text("Affinities", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Spacer(modifier = Modifier.height(6.dp))
                            PersonaResistancesRow(persona = result)

                            // 6. Skills Table
                            if (!result.skills.isNullOrEmpty()) {
                                Spacer(modifier = Modifier.height(12.dp))
                                Text("Skills (${result.skills.size})", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                                Spacer(modifier = Modifier.height(6.dp))
                                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                    result.skills.forEach { (skName, skLvl) ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(SurfaceRaised)
                                                .padding(horizontal = 10.dp, vertical = 6.dp),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(skName, style = MaterialTheme.typography.bodySmall, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                                            val badge = when {
                                                skLvl < 1.0 -> "Innate"
                                                skLvl >= 100.0 -> "Special"
                                                else -> "Lv. ${skLvl.toInt()}"
                                            }
                                            val badgeColor = when {
                                                skLvl < 1.0 -> Color(0xFF81C784)
                                                skLvl >= 100.0 -> Color(0xFFFFD700)
                                                else -> TextSecondary
                                            }
                                            Text(badge, style = MaterialTheme.typography.labelSmall, color = badgeColor, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                            }

                            // 7. Estimated Summon Cost
                            val cost = viewModel.getPersonaCost(result)
                            Spacer(modifier = Modifier.height(14.dp))
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

                            // 8. Action button
                            Spacer(modifier = Modifier.height(14.dp))
                            Button(
                                onClick = { onPersonaDetailClick(result.name) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text("Open Full Compendium Entry")
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
                        Icon(Icons.Default.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = source.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Text(
                                text = "${source.arcana} • Lv. ${source.level}",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
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
                    placeholder = { Text("Filter results by name or arcana...", color = TextSecondary.copy(alpha = 0.6f)) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = SurfaceCard,
                        unfocusedContainerColor = SurfaceCard,
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = HairlineStrong
                    ),
                    shape = RoundedCornerShape(10.dp),
                    singleLine = true
                )
            }

            item {
                Text(
                    text = "Possible Fusion Outputs (${filteredOptions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
            }

            items(filteredOptions) { option ->
                ForwardOptionCard(
                    option = option,
                    onPersonaClick = { onPersonaDetailClick(option.result.name) }
                )
            }
        }
    }
}

@Composable
fun ForwardOptionCard(
    option: ForwardFusionOption,
    onPersonaClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onPersonaClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
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
                        text = option.result.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    if (option.isSpecial) {
                        Surface(
                            color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(4.dp),
                            modifier = Modifier.padding(start = 6.dp)
                        ) {
                            Text(
                                text = "SPECIAL",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.tertiary,
                                modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
                            )
                        }
                    }
                }
                Text(
                    text = "${option.result.arcana} • Lv. ${option.result.level}",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "+ Fuse with: ${option.otherIngredient.name} (${option.otherIngredient.arcana} Lv. ${option.otherIngredient.level})",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color(0xFF64B5F6)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondary)
        }
    }
}

@Composable
fun IngredientSlotCard(
    label: String,
    persona: Persona?,
    isRequired: Boolean,
    onSelectClick: () -> Unit,
    onClearClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onSelectClick() },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (persona != null) MaterialTheme.colorScheme.primary else Hairline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (persona != null) {
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = persona.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "${persona.arcana} • Lv. ${persona.level}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    TextButton(onClick = onSelectClick) {
                        Text("Change")
                    }
                    IconButton(onClick = onClearClick) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            } else {
                Column {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                    Text(
                        text = if (isRequired) "+ Select Ingredient" else "+ Select Ingredient (Optional)",
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (isRequired) MaterialTheme.colorScheme.primary else TextSecondary
                    )
                }
                TextButton(onClick = onSelectClick) {
                    Text("Browse")
                }
            }
        }
    }
}

@Composable
fun PersonaResistancesRow(persona: Persona) {
    val elements = listOf("Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark")
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        elements.forEach { elem ->
            val isWeak = persona.weaknesses.any { it.equals(elem, ignoreCase = true) }
            val isResist = persona.resistances.any { it.equals(elem, ignoreCase = true) }
            val isNull = persona.nullifies.any { it.equals(elem, ignoreCase = true) }
            val isRepel = persona.repels.any { it.equals(elem, ignoreCase = true) }
            val isDrain = persona.absorbs.any { it.equals(elem, ignoreCase = true) }

            val label = when {
                isWeak -> "Wk"
                isResist -> "Str"
                isNull -> "Nul"
                isRepel -> "Rpl"
                isDrain -> "Dr"
                else -> "-"
            }
            val color = when {
                isWeak -> Color(0xFFE57373)
                isResist -> Color(0xFF81C784)
                isNull -> Color(0xFFB0BEC5)
                isRepel -> Color(0xFF64B5F6)
                isDrain -> Color(0xFFFFD54F)
                else -> TextDisabled
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(elem, style = MaterialTheme.typography.labelSmall, color = TextSecondary, fontSize = 10.sp)
                Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Reverse Lookup Views
// ---------------------------------------------------------------------------

@Composable
fun PersonaSelectionView(
    personas: List<Persona>,
    onPersonaSelected: (Persona) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedArcana by remember { mutableStateOf<String?>(null) }

    val allArcana = remember(personas) {
        listOf("All") + personas.mapNotNull { it.arcana }.distinct().sorted()
    }

    val filteredPersonas = remember(personas, searchQuery, selectedArcana) {
        personas.filter { p ->
            val matchesQuery = searchQuery.isBlank() ||
                p.name.contains(searchQuery, ignoreCase = true) ||
                p.arcana?.contains(searchQuery, ignoreCase = true) == true
            val matchesArcana = selectedArcana == null || selectedArcana == "All" || p.arcana == selectedArcana
            matchesQuery && matchesArcana
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search Persona to find recipes...", color = TextSecondary.copy(alpha = 0.6f)) },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TextSecondary) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = SurfaceCard,
                unfocusedContainerColor = SurfaceCard,
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = HairlineStrong
            ),
            shape = RoundedCornerShape(10.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        androidx.compose.foundation.lazy.LazyRow(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.fillMaxWidth()
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

        Spacer(modifier = Modifier.height(12.dp))

        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.fillMaxSize()
        ) {
            items(filteredPersonas) { persona ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onPersonaSelected(persona) },
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(persona.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                            Text("${persona.arcana}", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
                        }
                        Surface(
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(6.dp)
                        ) {
                            Text(
                                "Lv. ${persona.level ?: 0}",
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

@Composable
fun FusionTypeSelectionView(
    persona: Persona,
    onTypeSelected: (FusionType) -> Unit,
    onBack: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text("Fusion Recipes for ${persona.name}", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = TextPrimary)
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTypeSelected(FusionType.NORMAL) },
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.primary.copy(alpha = 0.4f))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Normal 2-Way Fusion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Find all 2-Persona recipe combinations to fuse ${persona.name}.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTypeSelected(FusionType.TRIPLE) },
            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Hairline)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text("Triangle / Triple Fusion", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TextPrimary)
                Text("Find 3-Persona triangle fusion recipes.", style = MaterialTheme.typography.bodySmall, color = TextSecondary)
            }
        }
    }
}

@Composable
fun FusionResultsView(
    viewModel: FusionViewModel,
    state: com.persona.companion.ui.viewmodels.FusionState
) {
    val target = state.selectedPersona ?: return
    val totalCount = state.fusionRecipes.size
    val displayedRecipes = viewModel.getFilteredAndSortedRecipes()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Recipes for ${target.name}",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = if (displayedRecipes.size == totalCount) {
                            "$totalCount combinations found"
                        } else {
                            "Showing ${displayedRecipes.size} of $totalCount combinations"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                TextButton(onClick = { viewModel.clearSelection() }) {
                    Text("Change Persona")
                }
            }
        }

        if (totalCount > 0) {
            item {
                OutlinedTextField(
                    value = state.recipeSearchQuery,
                    onValueChange = { viewModel.setRecipeSearchQuery(it) },
                    placeholder = { Text("Filter by ingredient...", color = TextSecondary) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search", tint = TextSecondary) },
                    trailingIcon = {
                        if (state.recipeSearchQuery.isNotEmpty()) {
                            IconButton(onClick = { viewModel.setRecipeSearchQuery("") }) {
                                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TextSecondary)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = Hairline,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    )
                )
            }

            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    items(RecipeSortOrder.values()) { order ->
                        val selected = state.recipeSortOrder == order
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.setRecipeSortOrder(order) },
                            label = {
                                Text(
                                    order.label,
                                    fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                                    fontSize = 12.sp
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f),
                                selectedLabelColor = MaterialTheme.colorScheme.primary
                            )
                        )
                    }
                }
            }
        }

        if (displayedRecipes.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Box(modifier = Modifier.padding(28.dp), contentAlignment = Alignment.Center) {
                        Text(
                            text = if (totalCount == 0) "No fusion combinations available." else "No recipes match \"${state.recipeSearchQuery}\".",
                            color = TextSecondary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        } else {
            itemsIndexed(displayedRecipes) { index, recipe ->
                val isCheapest = (index == 0 && state.recipeSortOrder == RecipeSortOrder.CHEAPEST)
                FusionRecipeCard(recipe = recipe, viewModel = viewModel, isCheapest = isCheapest)
            }
        }
    }
}

@Composable
fun FusionRecipeCard(
    recipe: FusionRecipe,
    viewModel: FusionViewModel,
    isCheapest: Boolean = false
) {
    val cost = viewModel.getRecipeCost(recipe)
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(10.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isCheapest) Color(0xFF4CAF50).copy(alpha = 0.7f) else Hairline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            if (isCheapest) {
                Surface(
                    color = Color(0xFF4CAF50).copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp),
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    Text(
                        text = "★ CHEAPEST OPTION",
                        color = Color(0xFF4CAF50),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
            recipe.personas.forEachIndexed { i, p ->
                if (i > 0) {
                    Text("+", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 2.dp))
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(p.name, fontWeight = FontWeight.Bold, color = TextPrimary)
                    Text("${p.arcana} Lv. ${p.level}", color = TextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = "Total Cost: ¥ " + String.format("%,d", cost),
                    color = if (isCheapest) Color(0xFF4CAF50) else Color(0xFFFFD700),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

// ---------------------------------------------------------------------------
// Persona Picker Dialog
// ---------------------------------------------------------------------------

@Composable
fun PersonaPickerDialog(
    title: String,
    personas: List<Persona>,
    onDismiss: () -> Unit,
    onPersonaSelected: (Persona) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filtered = remember(personas, searchQuery) {
        if (searchQuery.isBlank()) personas
        else personas.filter {
            it.name.contains(searchQuery, ignoreCase = true) ||
            it.arcana?.contains(searchQuery, ignoreCase = true) == true
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title, color = TextPrimary, fontWeight = FontWeight.Bold) },
        text = {
            Column(modifier = Modifier.height(400.dp)) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Search by name or arcana...") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
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

                Spacer(modifier = Modifier.height(10.dp))

                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
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
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(persona.name, color = TextPrimary, fontWeight = FontWeight.Bold)
                                    Text("${persona.arcana}", color = TextSecondary, fontSize = 12.sp)
                                }
                                Text("Lv. ${persona.level ?: 0}", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
