package com.persona.companion.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.persona.companion.data.SeriesData
import com.persona.companion.models.RequestEntity
import com.persona.companion.ui.theme.*
import com.persona.companion.ui.viewmodels.RequestViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RequestListScreen(
    seriesId: String,
    gameId: String,
    gameTitle: String,
    requestPath: String?,
    aigisRequestPath: String? = null,
    onBack: () -> Unit,
    onEnemyClick: (String) -> Unit
) {
    val series = remember(seriesId) { SeriesData.findSeries(seriesId) }
    val game = remember(seriesId, gameId) { SeriesData.findGame(seriesId, gameId) }
    val accentColor = series?.color ?: AccentBlue
    
    val viewModel: RequestViewModel = viewModel()
    val requests by viewModel.requests.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(gameId, requestPath, aigisRequestPath) {
        viewModel.loadRequests(gameId, requestPath, aigisRequestPath)
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = { Text("Requests - $gameTitle", color = TextPrimary, style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Back", tint = TextPrimary)
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
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.onSearchQueryChanged(it) },
                placeholder = { Text("Search quests / rewards...", color = TextSecondary) },
                leadingIcon = { Icon(Icons.Default.Search, null, tint = TextSecondary) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
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

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = accentColor)
                }
            } else if (requests.isEmpty() && searchQuery.isNotEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No requests found matching \"$searchQuery\"", color = TextSecondary)
                }
            } else {
                Text(
                    text = "${requests.size} requests available",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(requests, key = { it.name }) { request ->
                        RequestCard(
                            request = request,
                            accentColor = accentColor,
                            onToggleCompletion = { completed ->
                                viewModel.toggleRequestCompletion(request.name, gameId, completed)
                            },
                            onEnemyClick = onEnemyClick
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RequestCard(
    request: RequestEntity,
    accentColor: Color,
    onToggleCompletion: (Boolean) -> Unit,
    onEnemyClick: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(12.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: ID + Name + Checkbox
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!request.id.isNullOrBlank()) {
                    Surface(
                        color = accentColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(4.dp),
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = request.id!!,
                            style = MaterialTheme.typography.labelMedium,
                            color = accentColor,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        )
                    }
                }
                Text(
                    text = request.name,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (request.isCompleted) TextSecondary else TextPrimary,
                    textDecoration = if (request.isCompleted) androidx.compose.ui.text.style.TextDecoration.LineThrough else null,
                    modifier = Modifier.weight(1f)
                )

                Checkbox(
                    checked = request.isCompleted,
                    onCheckedChange = { onToggleCompletion(it) },
                    colors = CheckboxDefaults.colors(
                        checkedColor = accentColor,
                        uncheckedColor = TextSecondary,
                        checkmarkColor = Color.White
                    )
                )

                Icon(
                    imageVector = if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = TextSecondary,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            // Subtitle: Available From / Intel Required
            Text(
                text = when {
                    !request.intel_required.isNullOrBlank() -> "Intel: ${request.intel_required}"
                    !request.available.isNullOrBlank() -> "Avail: ${request.available}"
                    else -> "Availability unknown"
                },
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = Divider.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(12.dp))

                // Rewards
                RequestInfoRow(Icons.Default.CardGiftcard, "REWARD", request.reward, accentColor)

                // Common Details
                val giver = request.giver ?: request.quest_giver
                if (!giver.isNullOrBlank()) {
                    RequestInfoRow(Icons.Default.Person, "GIVER", giver, accentColor)
                }

                if (!request.location.isNullOrBlank()) {
                    RequestInfoRow(Icons.Default.LocationOn, "LOCATION", request.location!!, accentColor)
                }

                if (!request.deadline.isNullOrBlank()) {
                    RequestInfoRow(Icons.Default.Event, "DEADLINE", request.deadline!!, accentColor)
                }

                if (!request.category.isNullOrBlank()) {
                    RequestInfoRow(Icons.Default.Category, "CATEGORY", request.category!!, accentColor)
                }

                if (!request.target_name.isNullOrBlank()) {
                    RequestInfoRow(Icons.Default.AdsClick, "TARGET", request.target_name!!, accentColor)
                }

                if (!request.target_enemy.isNullOrBlank()) {
                    RequestInfoRow(
                        Icons.Default.Shield, 
                        "ENEMY DATA", 
                        request.target_enemy!!, 
                        accentColor,
                        isClickable = true,
                        onClick = { onEnemyClick(request.target_enemy!!) }
                    )
                }

                if (!request.difficulty.isNullOrBlank()) {
                    RequestInfoRow(Icons.Default.Star, "DIFFICULTY", request.difficulty!!, accentColor)
                }

                if (!request.remarks.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Notes: ${request.remarks}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                    )
                }
            }
        }
    }
}

@Composable
private fun RequestInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    isClickable: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = isClickable, onClick = onClick)
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.Top
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = accentColor,
            modifier = Modifier.size(16.dp).padding(top = 2.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = if (isClickable) accentColor else TextPrimary,
                fontWeight = if (isClickable) FontWeight.Bold else FontWeight.Normal
            )
        }
    }
}
