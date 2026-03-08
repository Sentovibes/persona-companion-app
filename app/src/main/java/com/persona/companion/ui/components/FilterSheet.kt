package com.persona.companion.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.persona.companion.models.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PersonaFilterSheet(
    filters: PersonaFilters,
    onFiltersChanged: (PersonaFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var currentFilters by remember { mutableStateOf(filters) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters & Sort", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sort options
            Text("Sort By", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            PersonaSortOption.values().forEach { option ->
                FilterChip(
                    selected = currentFilters.sortOption == option,
                    onClick = { currentFilters = currentFilters.copy(sortOption = option) },
                    label = { Text(option.name.replace("_", " ")) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Level range
            Text("Level Range", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = currentFilters.minLevel.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { level ->
                            currentFilters = currentFilters.copy(minLevel = level.coerceIn(1, 99))
                        }
                    },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = currentFilters.maxLevel.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { level ->
                            currentFilters = currentFilters.copy(maxLevel = level.coerceIn(1, 99))
                        }
                    },
                    label = { Text("Max") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Skill type filter
            Text("Skill Type", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            SkillType.values().forEach { type ->
                FilterChip(
                    selected = currentFilters.skillType == type,
                    onClick = { currentFilters = currentFilters.copy(skillType = type) },
                    label = { Text(type.name) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Game Exclusive Only")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = currentFilters.gameExclusive,
                    onCheckedChange = { currentFilters = currentFilters.copy(gameExclusive = it) }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("DLC Only")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = currentFilters.dlcOnly,
                    onCheckedChange = { currentFilters = currentFilters.copy(dlcOnly = it) }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Favorites Only")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = currentFilters.showFavoritesOnly,
                    onCheckedChange = { currentFilters = currentFilters.copy(showFavoritesOnly = it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Apply button
            Button(
                onClick = {
                    onFiltersChanged(currentFilters)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Reset button
            OutlinedButton(
                onClick = {
                    currentFilters = PersonaFilters()
                    onFiltersChanged(PersonaFilters())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnemyFilterSheet(
    filters: EnemyFilters,
    elements: List<String>,
    onFiltersChanged: (EnemyFilters) -> Unit,
    onDismiss: () -> Unit
) {
    var currentFilters by remember { mutableStateOf(filters) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Filters & Sort", style = MaterialTheme.typography.headlineSmall)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Sort options
            Text("Sort By", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            EnemySortOption.values().forEach { option ->
                FilterChip(
                    selected = currentFilters.sortOption == option,
                    onClick = { currentFilters = currentFilters.copy(sortOption = option) },
                    label = { Text(option.name.replace("_", " ")) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Level range
            Text("Level Range", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                OutlinedTextField(
                    value = currentFilters.minLevel.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { level ->
                            currentFilters = currentFilters.copy(minLevel = level.coerceIn(1, 99))
                        }
                    },
                    label = { Text("Min") },
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.width(8.dp))
                OutlinedTextField(
                    value = currentFilters.maxLevel.toString(),
                    onValueChange = { 
                        it.toIntOrNull()?.let { level ->
                            currentFilters = currentFilters.copy(maxLevel = level.coerceIn(1, 99))
                        }
                    },
                    label = { Text("Max") },
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Resistance filter
            Text("Resistance Type", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            
            FilterChip(
                selected = currentFilters.resistanceType == null,
                onClick = { currentFilters = currentFilters.copy(resistanceType = null) },
                label = { Text("All") },
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            
            elements.forEach { element ->
                FilterChip(
                    selected = currentFilters.resistanceType == element,
                    onClick = { currentFilters = currentFilters.copy(resistanceType = element) },
                    label = { Text(element) },
                    modifier = Modifier.padding(horizontal = 4.dp)
                )
            }
            
            if (currentFilters.resistanceType != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Filter By", style = MaterialTheme.typography.titleSmall)
                
                ResistanceFilter.values().forEach { filter ->
                    FilterChip(
                        selected = currentFilters.resistanceFilter == filter,
                        onClick = { currentFilters = currentFilters.copy(resistanceFilter = filter) },
                        label = { Text(filter.name) },
                        modifier = Modifier.padding(horizontal = 4.dp)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Toggle filters
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Game Exclusive Only")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = currentFilters.gameExclusive,
                    onCheckedChange = { currentFilters = currentFilters.copy(gameExclusive = it) }
                )
            }
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Favorites Only")
                Spacer(modifier = Modifier.weight(1f))
                Switch(
                    checked = currentFilters.showFavoritesOnly,
                    onCheckedChange = { currentFilters = currentFilters.copy(showFavoritesOnly = it) }
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Apply button
            Button(
                onClick = {
                    onFiltersChanged(currentFilters)
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Apply Filters")
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Reset button
            OutlinedButton(
                onClick = {
                    currentFilters = EnemyFilters()
                    onFiltersChanged(EnemyFilters())
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Reset")
            }
        }
    }
}
