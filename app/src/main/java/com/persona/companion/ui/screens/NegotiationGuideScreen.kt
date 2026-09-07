package com.persona.companion.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.MilitaryTech
import androidx.compose.material.icons.filled.Explore
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

data class MinorRankDetail(
    val rank: Int,
    val levelLabel: String,
    val expBonus: String,
    val moneyBonus: String,
    val cupRecovery: String,
    val swordSkills: List<String>
)

data class FloorPersona(
    val name: String,
    val arcana: String,
    val level: Int,
    val floor: String
)

data class DungeonGroup(
    val dungeon: String,
    val recommendedLevel: String,
    val personas: List<FloorPersona>
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
    var selectedTab by remember { mutableStateOf(0) } // 0: Skills & EXP by Rank, 1: Personas by Floor, 2: Major Arcana
    var selectedRank by remember { mutableStateOf(1) }
    var selectedDungeonIndex by remember { mutableStateOf(0) }

    var shadows by remember { mutableStateOf<List<ShadowEntry>>(emptyList()) }
    var sunPerks by remember { mutableStateOf<List<SunPerk>>(emptyList()) }
    var majorCards by remember { mutableStateOf<List<ArcanaCard>>(emptyList()) }
    var minorRanks by remember { mutableStateOf<List<MinorRankDetail>>(emptyList()) }
    var dungeonGroups by remember { mutableStateOf<List<DungeonGroup>>(emptyList()) }

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
            } else {
                // P3 or P4 Shuffle Time & Arcana
                val sectionKey = if (seriesId == "p3") "p3" else "p4"
                if (root.has(sectionKey)) {
                    val sec = root.getJSONObject(sectionKey)

                    // Major Arcana
                    if (sec.has("major_arcana")) {
                        val mArr = sec.getJSONArray("major_arcana")
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

                    // Minor Arcana Details (Ranks 1 - 10)
                    if (sec.has("minor_arcana_details")) {
                        val minObj = sec.getJSONObject("minor_arcana_details")
                        if (minObj.has("ranks")) {
                            val rArr = minObj.getJSONArray("ranks")
                            val rList = mutableListOf<MinorRankDetail>()
                            for (i in 0 until rArr.length()) {
                                val rObj = rArr.getJSONObject(i)
                                val skillsArr = rObj.optJSONArray("sword_skills")
                                val sList = mutableListOf<String>()
                                if (skillsArr != null) {
                                    for (j in 0 until skillsArr.length()) {
                                        sList.add(skillsArr.getString(j))
                                    }
                                }
                                rList.add(
                                    MinorRankDetail(
                                        rank = rObj.optInt("rank"),
                                        levelLabel = rObj.optString("level_label"),
                                        expBonus = rObj.optString("exp_bonus"),
                                        moneyBonus = rObj.optString("money_bonus"),
                                        cupRecovery = rObj.optString("cup_recovery"),
                                        swordSkills = sList
                                    )
                                )
                            }
                            minorRanks = rList
                        }
                    }

                    // Dungeon / Floor Personas
                    if (sec.has("dungeon_personas")) {
                        val dArr = sec.getJSONArray("dungeon_personas")
                        val dList = mutableListOf<DungeonGroup>()
                        for (i in 0 until dArr.length()) {
                            val dObj = dArr.getJSONObject(i)
                            val pArr = dObj.optJSONArray("personas")
                            val pList = mutableListOf<FloorPersona>()
                            if (pArr != null) {
                                for (j in 0 until pArr.length()) {
                                    val pObj = pArr.getJSONObject(j)
                                    pList.add(
                                        FloorPersona(
                                            name = pObj.optString("name"),
                                            arcana = pObj.optString("arcana"),
                                            level = pObj.optInt("level"),
                                            floor = pObj.optString("floor")
                                        )
                                    )
                                }
                            }
                            dList.add(
                                DungeonGroup(
                                    dungeon = dObj.optString("dungeon"),
                                    recommendedLevel = dObj.optString("recommended_level"),
                                    personas = pList
                                )
                            )
                        }
                        dungeonGroups = dList
                    }
                }
            }
        } catch (_: Exception) {}
    }

    val pageTitle = when (seriesId) {
        "p5" -> "Shadow Negotiation Guide"
        "p3" -> "Shuffle Time & Tartarus Personas"
        else -> "Shuffle Time, Skills & Personas"
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

    val filteredMajorCards = remember(searchQuery, majorCards) {
        if (searchQuery.isBlank()) majorCards
        else {
            val q = searchQuery.lowercase()
            majorCards.filter {
                it.name.lowercase().contains(q) ||
                it.effect.lowercase().contains(q) ||
                (it.burst ?: "").lowercase().contains(q)
            }
        }
    }

    Scaffold(
        containerColor = Background,
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(pageTitle, color = TextPrimary, fontSize = 17.sp, fontWeight = FontWeight.Bold)
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
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (seriesId == "p5") {
                // P5 Negotiation Matrix & Shadows
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
                // P3 & P4 Shuffle Time, Arcana Ranks, Floor Personas & Skills
                item {
                    // Navigation Tabs (No emojis)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        FilterChip(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            label = { Text("Skills & EXP by Rank") },
                            leadingIcon = { Icon(Icons.Default.MilitaryTech, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            label = { Text("Personas by Floor") },
                            leadingIcon = { Icon(Icons.Default.Explore, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                        FilterChip(
                            selected = selectedTab == 2,
                            onClick = { selectedTab = 2 },
                            label = { Text("Major Arcana") },
                            leadingIcon = { Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp)) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = accentColor,
                                selectedLabelColor = Color.White,
                                containerColor = SurfaceCard,
                                labelColor = TextSecondary
                            )
                        )
                    }
                }

                // Search Bar
                item {
                    OutlinedTextField(
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("Search skills, personas, floors, arcana...", color = TextSecondary.copy(alpha = 0.6f)) },
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

                if (selectedTab == 0) {
                    // TAB 0: Minor Arcana Ranks (Skills, EXP Multipliers, Yen, Cups)
                    item {
                        Text(
                            "Select Arcana Rank / Level:",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(minorRanks) { r ->
                                val isSelected = r.rank == selectedRank
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedRank = r.rank }
                                        .border(
                                            1.dp,
                                            if (isSelected) accentColor else Hairline,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    color = if (isSelected) accentColor else SurfaceCard
                                ) {
                                    Text(
                                        text = "Rank ${r.rank}",
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    val currentRankDetail = minorRanks.find { it.rank == selectedRank }
                    if (currentRankDetail != null) {
                        item {
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, accentColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            ) {
                                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(
                                            currentRankDetail.levelLabel,
                                            color = accentColor,
                                            fontSize = 15.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Surface(
                                            color = accentColor.copy(alpha = 0.15f),
                                            shape = RoundedCornerShape(6.dp)
                                        ) {
                                            Text(
                                                "Rank ${currentRankDetail.rank}",
                                                color = accentColor,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                            )
                                        }
                                    }

                                    HorizontalDivider(color = Hairline)

                                    // Wands (EXP)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Wands (EXP): ", color = Color(0xFF64B5F6), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(currentRankDetail.expBonus, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    // Coins (Money)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Coins (Money): ", color = Color(0xFFFFD700), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(currentRankDetail.moneyBonus, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    // Cups (HP/SP Recovery)
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("Cups (Recovery): ", color = Color(0xFF81C784), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                        Text(currentRankDetail.cupRecovery, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                    }

                                    HorizontalDivider(color = Hairline)

                                    // Swords (Skill Cards)
                                    Text("Swords (Skill Cards & Skills Obtainable):", color = Color(0xFFE57373), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                                    
                                    val filteredSkills = if (searchQuery.isBlank()) currentRankDetail.swordSkills
                                    else currentRankDetail.swordSkills.filter { it.lowercase().contains(searchQuery.lowercase()) }

                                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                        filteredSkills.forEach { skill ->
                                            Surface(
                                                color = Surface,
                                                shape = RoundedCornerShape(6.dp),
                                                modifier = Modifier.fillMaxWidth()
                                            ) {
                                                Text(
                                                    "• $skill",
                                                    color = TextPrimary,
                                                    fontSize = 12.sp,
                                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else if (selectedTab == 1) {
                    // TAB 1: Personas by Floor / Dungeon
                    item {
                        Text(
                            "Select Dungeon / Tartarus Block:",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    item {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(dungeonGroups.indices.toList()) { idx ->
                                val dg = dungeonGroups[idx]
                                val isSelected = idx == selectedDungeonIndex
                                Surface(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { selectedDungeonIndex = idx }
                                        .border(
                                            1.dp,
                                            if (isSelected) accentColor else Hairline,
                                            RoundedCornerShape(8.dp)
                                        ),
                                    color = if (isSelected) accentColor else SurfaceCard
                                ) {
                                    Text(
                                        text = dg.dungeon.substringBefore("(").trim(),
                                        color = if (isSelected) Color.White else TextPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                                    )
                                }
                            }
                        }
                    }

                    val currentDungeon = dungeonGroups.getOrNull(selectedDungeonIndex)
                    if (currentDungeon != null) {
                        item {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(currentDungeon.dungeon, color = accentColor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
                                Text("Rec. ${currentDungeon.recommendedLevel}", color = TextSecondary, fontSize = 12.sp)
                            }
                        }

                        val filteredPersonas = if (searchQuery.isBlank()) currentDungeon.personas
                        else currentDungeon.personas.filter {
                            it.name.lowercase().contains(searchQuery.lowercase()) ||
                            it.arcana.lowercase().contains(searchQuery.lowercase()) ||
                            it.floor.lowercase().contains(searchQuery.lowercase())
                        }

                        items(filteredPersonas) { p ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = SurfaceCard),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .border(1.dp, Hairline, RoundedCornerShape(8.dp))
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(p.name, color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                        Text("${p.arcana} Arcana  •  Floors: ${p.floor}", color = TextSecondary, fontSize = 12.sp)
                                    }
                                    Surface(
                                        color = accentColor.copy(alpha = 0.15f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            "Lv ${p.level}",
                                            color = accentColor,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // TAB 2: Major Arcana Cards
                    items(filteredMajorCards) { card ->
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
            Text("Best Response [Likes]: $best", color = Color(0xFF81C784), fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            Text("Neutral [OK]: $ok", color = Color(0xFFFFB74D), fontSize = 12.sp)
            Text("Worst [Hates]: $bad", color = Color(0xFFE57373), fontSize = 12.sp)
        }
    }
}
