package com.persona.companion.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.persona.companion.data.SeriesData
import com.persona.companion.ui.theme.*
import com.persona.companion.utils.AppAnalytics
import org.json.JSONObject

data class ShadowEntry(
    val name: String,
    val personaName: String,
    val arcana: String,
    val personality: String,
    val best: String,
    val ok: String,
    val bad: String,
    val color: Color
)

data class SunPerk(
    val rank: Int,
    val name: String,
    val effect: String
)

data class ArcanaCard(
    val num: String,
    val name: String,
    val effect: String,
    val burst: String? = null
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NegotiationGuideScreen(
    seriesId: String,
    gameId: String,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val series = remember(seriesId) { SeriesData.findSeries(seriesId) }
    val game = remember(gameId) { SeriesData.findGame(seriesId, gameId) }
    val accentColor = series?.color ?: Persona5Red

    var searchQuery by remember { mutableStateOf("") }
    var shadows by remember { mutableStateOf<List<ShadowEntry>>(emptyList()) }
    var sunPerks by remember { mutableStateOf<List<SunPerk>>(emptyList()) }
    var majorCards by remember { mutableStateOf<List<ArcanaCard>>(emptyList()) }
    var sweepRules by remember { mutableStateOf<List<String>>(emptyList()) }

    LaunchedEffect(Unit) {
        AppAnalytics.trackScreen("negotiation_guide_$seriesId")
        try {
            val jsonStr = context.assets.open("data/negotiation/negotiation_data.json").bufferedReader().use { it.readText() }
            val root = JSONObject(jsonStr)

            if (seriesId == "p5" && root.has("p5")) {
                val p5 = root.getJSONObject("p5")
                
                // Shadows
                if (p5.has("shadows")) {
                    val sArray = p5.getJSONArray("shadows")
                    val sList = mutableListOf<ShadowEntry>()
                    for (i in 0 until sArray.length()) {
                        val sObj = sArray.getJSONObject(i)
                        val pType = sObj.optString("personality", "Unknown")
                        val pColor = when (pType) {
                            "Upbeat" -> Color(0xFFFFB74D)
                            "Timid" -> Color(0xFF81C784)
                            "Gloomy" -> Color(0xFF64B5F6)
                            "Irritable" -> Color(0xFFE57373)
                            else -> accentColor
                        }
                        sList.add(
                            ShadowEntry(
                                name = sObj.optString("name"),
                                personaName = sObj.optString("persona_name"),
                                arcana = sObj.optString("arcana"),
                                personality = pType,
                                best = sObj.optString("best"),
                                ok = sObj.optString("ok"),
                                bad = sObj.optString("bad"),
                                color = pColor
                            )
                        )
                    }
                    shadows = sList
                }

                // Sun Perks
                if (p5.has("sun_confidant_perks")) {
                    val perksArr = p5.getJSONArray("sun_confidant_perks")
                    val perksList = mutableListOf<SunPerk>()
                    for (i in 0 until perksArr.length()) {
                        val pObj = perksArr.getJSONObject(i)
                        perksList.add(
                            SunPerk(
                                rank = pObj.optInt("rank"),
                                name = pObj.optString("name"),
                                effect = pObj.optString("effect")
                            )
                        )
                    }
                    sunPerks = perksList
                }
            } else if (seriesId == "p3" && root.has("p3")) {
                val p3 = root.getJSONObject("p3")
                if (p3.has("major_arcana")) {
                    val mArr = p3.getJSONArray("major_arcana")
                    val mList = mutableListOf<ArcanaCard>()
                    for (i in 0 until mArr.length()) {
                        val mObj = mArr.getJSONObject(i)
                        mList.add(
                            ArcanaCard(
                                num = mObj.optString("num"),
                                name = mObj.optString("name"),
                                effect = mObj.optString("effect"),
                                burst = mObj.optString("burst", "")
                            )
                        )
                    }
                    majorCards = mList
                }
            } else if (seriesId == "p4" && root.has("p4")) {
                val p4 = root.getJSONObject("p4")
                if (p4.has("major_arcana")) {
                    val mArr = p4.getJSONArray("major_arcana")
                    val mList = mutableListOf<ArcanaCard>()
                    for (i in 0 until mArr.length()) {
                        val mObj = mArr.getJSONObject(i)
                        mList.add(
                            ArcanaCard(
                                num = mObj.optString("num"),
                                name = mObj.optString("name"),
                                effect = mObj.optString("effect")
                            )
                        )
                    }
                    majorCards = mList
                }
                if (p4.has("sweep_bonus_rules")) {
                    val sArr = p4.getJSONArray("sweep_bonus_rules")
                    val sList = mutableListOf<String>()
                    for (i in 0 until sArr.length()) {
                        sList.add(sArr.getString(i))
                    }
                    sweepRules = sList
                }
            }
        } catch (_: Exception) {}
    }

    val pageTitle = when (seriesId) {
        "p5" -> "Shadow Negotiation Guide"
        "p3" -> "Shuffle Time & Major Arcana"
        else -> "Shuffle Time & Sweep Bonus"
    }

    val filteredShadows = remember(searchQuery, shadows) {
        if (searchQuery.isBlank()) shadows
        else {
            val q = searchQuery.lowercase()
            shadows.filter {
                it.name.lowercase().contains(q) ||
                it.personaName.lowercase().contains(q) ||
                it.arcana.lowercase().contains(q)
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pageTitle, color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(game?.title ?: "", color = TextSecondary, fontSize = 12.sp)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Surface)
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            if (seriesId == "p5") {
                // Personality Cheat Sheet
                item {
                    Text(
                        "Personality Response Matrix",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        PersonalityCard("Upbeat", "Funny / Joke", "Serious", "Vague / Ambiguous", Color(0xFFFFB74D))
                        PersonalityCard("Timid", "Kind / Gentle", "Vague / Ambiguous", "Funny / Joke", Color(0xFF81C784))
                        PersonalityCard("Gloomy", "Vague / Ambiguous", "Serious", "Kind / Gentle", Color(0xFF64B5F6))
                        PersonalityCard("Irritable", "Serious / Direct", "Vague / Ambiguous", "Kind / Gentle", Color(0xFFE57373))
                    }
                }

                // Sun Confidant Perks
                if (sunPerks.isNotEmpty()) {
                    item {
                        Text(
                            "Sun Confidant (Yoshida) Negotiation Perks",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                sunPerks.forEach { perk ->
                                    Row(verticalAlignment = Alignment.Top) {
                                        Surface(
                                            color = Color(0xFFFFD700).copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp),
                                            modifier = Modifier.padding(end = 8.dp)
                                        ) {
                                            Text(
                                                "Rank ${perk.rank}",
                                                color = Color(0xFFFFD700),
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                            )
                                        }
                                        Column {
                                            Text(perk.name, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                            Text(perk.effect, color = TextSecondary, fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                // Live Shadow Lookup
                item {
                    Text(
                        "Shadow Personality Database",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search by Shadow name or Persona...", color = TextSecondary.copy(alpha = 0.6f)) },
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
                            focusedBorderColor = accentColor,
                            unfocusedBorderColor = HairlineStrong
                        ),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )
                }

                items(filteredShadows) { shadow ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Hairline, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column {
                                    Text(shadow.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                    Text("${shadow.personaName} (${shadow.arcana})", color = TextSecondary, fontSize = 12.sp)
                                }
                                Surface(
                                    color = shadow.color.copy(alpha = 0.15f),
                                    shape = RoundedCornerShape(6.dp)
                                ) {
                                    Text(
                                        shadow.personality,
                                        color = shadow.color,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("Best [Likes]: ${shadow.best}", color = Color(0xFF81C784), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                Text("Neutral [OK]: ${shadow.ok}", color = Color(0xFFFFB74D), fontSize = 11.sp)
                            }
                        }
                    }
                }
            } else {
                // P3 or P4 Guides
                if (sweepRules.isNotEmpty()) {
                    item {
                        Text(
                            "Sweep Bonus Mechanics",
                            color = TextPrimary,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.border(1.dp, Color(0xFFFFD700).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                        ) {
                            Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                sweepRules.forEach { rule ->
                                    Text("• $rule", color = TextPrimary, fontSize = 13.sp)
                                }
                            }
                        }
                    }
                }

                item {
                    Text(
                        "Major Arcana Tarot Cards",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                items(majorCards) { card ->
                    Card(
                        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Hairline, RoundedCornerShape(10.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("${card.num}. ${card.name}", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(card.effect, color = TextSecondary, fontSize = 12.sp)
                            if (!card.burst.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text("Arcana Burst: ${card.burst}", color = Color(0xFFFFD700), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonalityCard(
    personality: String,
    best: String,
    ok: String,
    bad: String,
    color: Color
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = SurfaceCard),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(personality, color = color, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Surface(
                    color = color.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Target Style", color = color, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
                }
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("• Best Response [Likes]: $best", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("• Neutral [OK]: $ok", color = Color(0xFFFFB74D), fontSize = 12.sp)
            Text("• Worst [Hates]: $bad", color = Color(0xFFE57373), fontSize = 12.sp)
        }
    }
}
