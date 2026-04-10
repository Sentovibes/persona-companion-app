package com.persona.companion.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.BorderStroke
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.data.SeriesData
import com.persona.companion.models.Item
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.ItemViewModel

private val allCategories = listOf(
    "All", "Consumable", "Weapon", "Armor", "Accessory", "Skill Card", "Other"
)

enum class ItemSortOption {
    NAME, PRICE_ASC, PRICE_DESC, LOCATION
}

private fun parsePrice(price: String?): Int {
    if (price.isNullOrBlank() || price == "-") return 0
    // Handle "1,000 / 800" or "¥ 2,000 (Set)" by taking the first sequence of digits
    val regex = Regex("""[\d,]+""")
    val match = regex.find(price)
    return match?.value?.replace(",", "")?.toIntOrNull() ?: 0
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ItemListScreen(
    seriesId: String,
    gameId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val game = remember(seriesId, gameId) { SeriesData.findGame(seriesId, gameId) }
    val series = remember(seriesId) { SeriesData.findSeries(seriesId) }
    
    if (game == null || series == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Game data not found", color = TextSecondary)
        }
        return
    }

    val accentColor = series.color
    val viewModel: ItemViewModel = viewModel()
    val items by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("All") }
    var sortOption by remember { mutableStateOf(ItemSortOption.NAME) }
    var showSortMenu by remember { mutableStateOf(false) }

    LaunchedEffect(gameId, game.itemPath, game.aigisItemPath) {
        viewModel.loadItems(gameId, game.itemPath, game.aigisItemPath)
    }

    val filteredAndSortedItems = remember(items, searchQuery, selectedCategory, sortOption) {
        items.filter { item ->
            val matchesSearch = item.name.contains(searchQuery, ignoreCase = true) ||
                    (item.description?.contains(searchQuery, ignoreCase = true) ?: false)
            val matchesCategory = selectedCategory == "All" || item.category == selectedCategory
            matchesSearch && matchesCategory
        }.sortedWith { a, b ->
            when (sortOption) {
                ItemSortOption.NAME -> a.name.compareTo(b.name)
                ItemSortOption.PRICE_ASC -> {
                    val pA = parsePrice(a.price)
                    val pB = parsePrice(b.price)
                    pA.compareTo(pB)
                }
                ItemSortOption.PRICE_DESC -> {
                    val pA = parsePrice(a.price)
                    val pB = parsePrice(b.price)
                    pB.compareTo(pA)
                }
                ItemSortOption.LOCATION -> (a.location ?: "").compareTo(b.location ?: "")
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Items - ${game.title}", color = TextPrimary, style = MaterialTheme.typography.titleMedium) },
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
                                onClick = { sortOption = ItemSortOption.NAME; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Price (Low to High)", color = TextPrimary) },
                                onClick = { sortOption = ItemSortOption.PRICE_ASC; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("Price (High to Low)", color = TextPrimary) },
                                onClick = { sortOption = ItemSortOption.PRICE_DESC; showSortMenu = false }
                            )
                            DropdownMenuItem(
                                text = { Text("By Location/Origin", color = TextPrimary) },
                                onClick = { sortOption = ItemSortOption.LOCATION; showSortMenu = false }
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
                placeholder = { Text("Search items...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = accentColor,
                    unfocusedBorderColor = Divider,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    cursorColor = accentColor
                ),
                shape = RoundedCornerShape(12.dp),
                singleLine = true
            )

            ScrollableTabRow(
                selectedTabIndex = allCategories.indexOf(selectedCategory).coerceAtLeast(0),
                containerColor = Background,
                contentColor = accentColor,
                edgePadding = 16.dp,
                divider = {},
                indicator = { tabPositions ->
                    val index = allCategories.indexOf(selectedCategory)
                    if (index >= 0) {
                        TabRowDefaults.SecondaryIndicator(
                            Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = accentColor
                        )
                    }
                }
            ) {
                allCategories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = category },
                        text = {
                            Text(
                                category,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selectedCategory == category) accentColor else TextSecondary,
                                fontWeight = if (selectedCategory == category) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    )
                }
            }

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else {
                Text(
                    text = "${filteredAndSortedItems.size} items",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(
                        items = filteredAndSortedItems,
                        key = { index, item -> "${item.name}_${item.category}_${index}" }
                    ) { _, item ->
                        ItemCard(
                            item = item,
                            viewModel = viewModel,
                            personaPath = game.dataPath,
                            accentColor = accentColor
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ItemCard(
    item: Item,
    viewModel: ItemViewModel,
    personaPath: String?,
    accentColor: Color
) {
    var expanded by remember { mutableStateOf(false) }
    
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
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = item.category.orEmpty().uppercase(),
                        style = MaterialTheme.typography.labelSmall,
                        color = accentColor,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
                
                if (!item.price.isNullOrBlank() && item.price != "0") {
                    Surface(
                        color = Color(0xFF2D3436),
                        shape = RoundedCornerShape(8.dp),
                        border = BorderStroke(1.dp, Color(0xFFFFD700).copy(alpha = 0.5f))
                    ) {
                        Text(
                            text = if (item.price!!.contains("¥")) item.price!! else "${item.price!!} ¥",
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFFFFD700),
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }
            
            if (!item.effect.isNullOrBlank() || !item.description.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = if (!item.effect.isNullOrBlank()) item.effect!! else item.description.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            AnimatedVisibility(
                visible = expanded,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 16.dp)) {
                    HorizontalDivider(color = Divider.copy(alpha = 0.5f), thickness = 1.dp)
                    Spacer(modifier = Modifier.height(16.dp))

                    // Acquisition Methods
                    if (!item.location.isNullOrBlank()) {
                        SectionHeader(Icons.Default.LocationOn, "ACQUISITION / LOCATION")
                        val locations = item.location.split("|").map { it.trim() }.filter { it.isNotBlank() }
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            locations.forEach { loc ->
                                Row(verticalAlignment = Alignment.Top) {
                                    Text("• ", color = accentColor, fontWeight = FontWeight.Bold)
                                    Text(loc, style = MaterialTheme.typography.bodySmall, color = TextPrimary)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }
                    
                    // Stats Grid
                    if (item.attack != null || item.accuracy != null || !item.sellPrice.isNullOrBlank()) {
                        SectionHeader(Icons.Default.Info, "DETAILS & PRICING")
                        Row(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            if (item.attack != null) {
                                ItemInfoRow("Attack", item.attack.toString(), Modifier.weight(1f))
                            }
                            if (item.accuracy != null) {
                                ItemInfoRow("Accuracy", item.accuracy.toString(), Modifier.weight(1f))
                            }
                            if (!item.sellPrice.isNullOrBlank()) {
                                ItemInfoRow(
                                    label = "Resale Value",
                                    value = if (item.sellPrice!!.contains("¥")) item.sellPrice!! else "${item.sellPrice!!} ¥",
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                    }

                    // Obtained From (Persona Itemization) section
                    SectionHeader(Icons.Default.ShoppingCart, "PERSONA ITEMIZATION")
                    
                    val sources = remember(item.name, personaPath) { 
                        viewModel.getItemSources(personaPath, item.name) 
                    }
                    
                    if (sources.isEmpty()) {
                        Text(
                            "Standard item / No known persona source",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary.copy(alpha = 0.7f),
                            modifier = Modifier.padding(start = 4.dp)
                        )
                    } else {
                        FlowRow(
                            Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            sources.forEach { source ->
                                SuggestionChip(
                                    onClick = { },
                                    label = { Text(source.name, fontSize = 10.sp) },
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
                    
                    if (!item.description.isNullOrBlank() && item.description != item.effect) {
                        Spacer(modifier = Modifier.height(16.dp))
                        SectionHeader(null, "FLAVOR TEXT")
                        Text(
                            text = item.description!!,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionHeader(icon: ImageVector?, title: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(bottom = 8.dp)
    ) {
        if (icon != null) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TextSecondary,
                modifier = Modifier.size(14.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.ExtraBold,
            color = TextSecondary,
            letterSpacing = 1.sp
        )
    }
}

@Composable
private fun ItemInfoRow(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier.padding(vertical = 2.dp)) {
        Text(
            text = label.uppercase(),
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            fontWeight = FontWeight.Bold,
            fontSize = 9.sp
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Medium
        )
    }
}
