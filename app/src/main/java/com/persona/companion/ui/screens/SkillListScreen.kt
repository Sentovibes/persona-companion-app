package com.persona.companion.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.models.Skill
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.SkillViewModel

private val elementColors = mapOf(
    "Fire" to Color(0xFFFF6B6B),
    "Ice" to Color(0xFF74B9FF),
    "Wind" to Color(0xFF55EFC4),
    "Elec" to Color(0xFFFDCB6E),
    "Light" to Color(0xFFFFF3B0),
    "Dark" to Color(0xFF9B59B6),
    "Almighty" to Color(0xFFF1C40F),
    "Psy" to Color(0xFFD980FA),
    "Nuclear" to Color(0xFF95A5A6),
    "Phys" to Color(0xFFBDC3C7),
    "Slash" to Color(0xFFBDC3C7),
    "Strike" to Color(0xFFBDC3C7),
    "Pierce" to Color(0xFFBDC3C7),
    "Recovery" to Color(0xFF2ECC71),
    "Support" to Color(0xFF3498DB),
    "Ailment" to Color(0xFF95A5A6)
)

private val allElements = listOf(
    "All", "Phys", "Fire", "Ice", "Elec", "Wind", "Light", "Dark", 
    "Almighty", "Psy", "Nuclear", "Recovery", "Support", "Ailment"
)

enum class SkillSortOption {
    NAME, COST_ASC, COST_DESC, ELEMENT
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SkillListScreen(
    gameId: String,
    gameName: String,
    skillPath: String?,
    personaPath: String?,
    onBack: () -> Unit
) {
    val viewModel: SkillViewModel = viewModel()
    val skills by viewModel.skills.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    
    var searchQuery by remember { mutableStateOf("") }
    var selectedElement by remember { mutableStateOf("All") }
    var sortOption by remember { mutableStateOf(SkillSortOption.NAME) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(gameId) {
        viewModel.loadSkills(skillPath)
    }

    val filteredAndSortedSkills = remember(skills, searchQuery, selectedElement, sortOption) {
        skills.filter {
            (selectedElement == "All" || it.element.contains(selectedElement, ignoreCase = true)) &&
            (searchQuery.isBlank() || 
             it.name.contains(searchQuery, ignoreCase = true) || 
             it.effect.contains(searchQuery, ignoreCase = true))
        }.sortedWith { a, b ->
            when (sortOption) {
                SkillSortOption.NAME -> a.name.compareTo(b.name)
                SkillSortOption.COST_ASC -> a.cost.compareTo(b.cost)
                SkillSortOption.COST_DESC -> b.cost.compareTo(a.cost)
                SkillSortOption.ELEMENT -> a.element.compareTo(b.element)
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Skills - $gameName", color = TextPrimary) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { showSortMenu = true }) {
                            Icon(Icons.Default.Sort, "Sort", tint = TextPrimary)
                        }
                        DropdownMenu(
                            expanded = showSortMenu,
                            onDismissRequest = { showSortMenu = false },
                            modifier = Modifier.background(SurfaceCard)
                        ) {
                            DropdownMenuItem(
                                text = { Text("Name (A-Z)", color = TextPrimary) },
                                onClick = { sortOption = SkillSortOption.NAME; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Cost (Low to High)", color = TextPrimary) },
                                onClick = { sortOption = SkillSortOption.COST_ASC; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Cost (High to Low)", color = TextPrimary) },
                                onClick = { sortOption = SkillSortOption.COST_DESC; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("By Element", color = TextPrimary) },
                                onClick = { sortOption = SkillSortOption.ELEMENT; showSortMenu = false }
                            )
                        }
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
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Search skills...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = Divider,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = AccentBlue
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            ScrollableTabRow(
                selectedTabIndex = allElements.indexOf(selectedElement).coerceAtLeast(0),
                containerColor = Background,
                contentColor = AccentBlue,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    if (allElements.indexOf(selectedElement) >= 0) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[allElements.indexOf(selectedElement)]),
                            color = AccentBlue
                        )
                    }
                }
            ) {
                allElements.forEach { element ->
                    Tab(
                        selected = selectedElement == element,
                        onClick = { selectedElement = element },
                        text = {
                            Text(
                                element,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedElement == element) AccentBlue else TextSecondary,
                                fontWeight = if (selectedElement == element) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AccentBlue)
                }
            } else {
                Text(
                    text = "${filteredAndSortedSkills.size} skills",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = filteredAndSortedSkills,
                        key = { it.name + it.element + it.cost }
                    ) { skill ->
                        SkillCard(
                            skill = skill,
                            viewModel = viewModel,
                            personaPath = personaPath
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SkillCard(
    skill: Skill,
    viewModel: SkillViewModel,
    personaPath: String?
) {
    var expanded by remember { mutableStateOf(false) }
    val elemColor = elementColors[skill.element] ?: AccentBlue
    val iconRes = when (skill.element) {
        "Fire" -> com.persona.companion.R.drawable.ic_fire
        "Ice" -> com.persona.companion.R.drawable.ic_ice
        "Elec" -> com.persona.companion.R.drawable.ic_elec
        "Wind", "Force" -> com.persona.companion.R.drawable.ic_wind
        "Light" -> com.persona.companion.R.drawable.ic_light
        "Dark" -> com.persona.companion.R.drawable.ic_dark
        "Almighty" -> com.persona.companion.R.drawable.ic_almighty
        "Psy" -> com.persona.companion.R.drawable.ic_psy
        "Nuclear" -> com.persona.companion.R.drawable.ic_nuke
        "Phys", "Slash", "Strike", "Pierce" -> com.persona.companion.R.drawable.ic_phys
        "Recovery" -> com.persona.companion.R.drawable.ic_recovery
        "Support" -> com.persona.companion.R.drawable.ic_support
        "Ailment" -> com.persona.companion.R.drawable.ic_ailment
        else -> null
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { expanded = !expanded },
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skill.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary,
                    modifier = Modifier.weight(1f)
                )

                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = elemColor.copy(alpha = 0.2f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        if (iconRes != null) {
                            Icon(
                                painter = androidx.compose.ui.res.painterResource(id = iconRes),
                                contentDescription = null,
                                tint = elemColor,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                        Text(
                            skill.element,
                            style = MaterialTheme.typography.labelSmall,
                            color = elemColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            if (skill.effect.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = skill.effect,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            if (skill.cost > 0 && skill.costType.isNotBlank()) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Cost: ${skill.cost} ${skill.costType}",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (skill.costType == "SP") Color(0xFF00CEC9) else Color(0xFFFF6B6B)
                )
            }

            androidx.compose.animation.AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Divider.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "LEARNED BY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = TextSecondary,
                        letterSpacing = 1.sp
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    val learners = remember(skill.name, personaPath) { 
                        viewModel.getLearners(personaPath, skill.name) 
                    }
                    
                    if (learners.isEmpty()) {
                        Text(
                            "Standard skill / Unknown learners",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.7f)
                        )
                    } else {
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            learners.forEach { learner ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(learner.name, fontSize = 10.sp) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = SuggestionChipDefaults.suggestionChipColors(
                                        containerColor = Surface.copy(alpha = 0.3f),
                                        labelColor = TextPrimary
                                    ),
                                    border = BorderStroke(1.dp, Divider.copy(alpha = 0.5f))
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
