package com.persona.companion.debug

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.companion.BuildConfig
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DebugOverlay(
    visible: Boolean,
    onDismiss: () -> Unit
) {
    if (!BuildConfig.ENABLE_DEBUG_FEATURES || !visible) return
    
    var selectedTab by remember { mutableStateOf(0) }
    val logs by remember { derivedStateOf { DebugLogger.getLogs() } }
    val listState = rememberLazyListState()
    
    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.Black.copy(alpha = 0.95f)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "Debug Console",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer
                )
                Row {
                    TextButton(onClick = { DebugLogger.clearLogs() }) {
                        Text("Clear")
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, "Close")
                    }
                }
            }
            
            // Tabs
            TabRow(selectedTabIndex = selectedTab) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = { Text("Logs (${logs.size})") }
                )
                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = { Text("Errors") }
                )
                Tab(
                    selected = selectedTab == 2,
                    onClick = { selectedTab = 2 },
                    text = { Text("Stats") }
                )
            }
            
            // Content
            when (selectedTab) {
                0 -> LogsTab(logs, listState)
                1 -> ErrorsTab(logs.filter { it.level == DebugLogger.LogLevel.ERROR })
                2 -> StatsTab()
            }
        }
    }
}

@Composable
private fun LogsTab(logs: List<DebugLogger.LogEntry>, listState: LazyListState) {
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        items(logs) { log ->
            LogItem(log)
        }
    }
}

@Composable
private fun LogItem(log: DebugLogger.LogEntry) {
    val dateFormat = remember { SimpleDateFormat("HH:mm:ss.SSS", Locale.getDefault()) }
    val color = when (log.level) {
        DebugLogger.LogLevel.DEBUG -> Color.Gray
        DebugLogger.LogLevel.INFO -> Color.White
        DebugLogger.LogLevel.WARNING -> Color.Yellow
        DebugLogger.LogLevel.ERROR -> Color.Red
    }
    
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Row {
            Text(
                text = dateFormat.format(Date(log.timestamp)),
                fontSize = 10.sp,
                color = Color.Gray,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = log.level.name[0].toString(),
                fontSize = 10.sp,
                color = color,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = log.tag,
                fontSize = 10.sp,
                color = Color.Cyan,
                fontFamily = FontFamily.Monospace
            )
        }
        Text(
            text = log.message,
            fontSize = 11.sp,
            color = color,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.padding(start = 16.dp)
        )
        log.throwable?.let { throwable ->
            Text(
                text = throwable.stackTraceToString(),
                fontSize = 9.sp,
                color = Color.Red.copy(alpha = 0.8f),
                fontFamily = FontFamily.Monospace,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Composable
private fun ErrorsTab(errors: List<DebugLogger.LogEntry>) {
    if (errors.isEmpty()) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No errors logged", color = Color.Green)
        }
    } else {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(errors) { error ->
                LogItem(error)
                Divider(color = Color.Red.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 8.dp))
            }
        }
    }
}

@Composable
private fun StatsTab() {
    val runtime = Runtime.getRuntime()
    val usedMemory = (runtime.totalMemory() - runtime.freeMemory()) / 1024 / 1024
    val maxMemory = runtime.maxMemory() / 1024 / 1024
    val totalMemory = runtime.totalMemory() / 1024 / 1024
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        StatItem("Build Type", if (BuildConfig.DEBUG) "Debug" else "Release")
        StatItem("Version", BuildConfig.VERSION_NAME)
        StatItem("Version Code", BuildConfig.VERSION_CODE.toString())
        Divider(modifier = Modifier.padding(vertical = 8.dp))
        StatItem("Used Memory", "$usedMemory MB")
        StatItem("Total Memory", "$totalMemory MB")
        StatItem("Max Memory", "$maxMemory MB")
        StatItem("Memory Usage", "${(usedMemory * 100 / maxMemory)}%")
    }
}

@Composable
private fun StatItem(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = Color.Gray, fontSize = 14.sp)
        Text(value, color = Color.White, fontSize = 14.sp, fontFamily = FontFamily.Monospace)
    }
}
