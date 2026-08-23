package com.persona.companion.ui.viewmodels

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.AppPreferences
import com.persona.companion.fusion.FusionCalculator
import com.persona.companion.fusion.FusionRecipe
import com.persona.companion.models.FusionChart
import com.persona.companion.models.Persona
import com.persona.companion.utils.JsonLoader
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

enum class FusionType {
    NORMAL, TRIPLE
}

enum class CalculatorMode {
    REVERSE, FORWARD, SKILL_ROUTES
}

enum class ForwardSubTab {
    CALCULATOR, FROM_PERSONA
}

data class DirectLearner(
    val personaName: String,
    val arcana: String,
    val level: Int,
    val skillName: String,
    val skillLevel: Int
)

data class ItemizerEntry(
    val personaName: String,
    val arcana: String,
    val level: Int,
    val skillName: String,
    val itemDescription: String
)

data class MultiTreeRecipe(
    val targetPersona: String,
    val parentA: String,
    val parentASkill: String,
    val parentB: String,
    val parentBSkill: String,
    val description: String
)

data class FusionState(
    val personas: List<Persona> = emptyList(),
    val selectedPersona: Persona? = null,
    val fusionType: FusionType? = null,
    val fusionRecipes: List<FusionRecipe> = emptyList(),
    val calculatorMode: CalculatorMode = CalculatorMode.REVERSE,
    // Forward Fusion state
    val selectedIngredients: List<Persona?> = listOf(null, null, null),
    val forwardResult: Persona? = null,
    val forwardSubTab: ForwardSubTab = ForwardSubTab.CALCULATOR,
    val forwardSourcePersona: Persona? = null,
    val forwardOptions: List<com.persona.companion.fusion.ForwardFusionOption> = emptyList(),
    val forwardOptionQuery: String = "",
    // Skill Routes state
    val skillRouteTarget: Persona? = null,
    val skillRouteSkills: List<String> = emptyList(),
    val allAvailableSkills: List<String> = emptyList(),
    val skillLearners: List<DirectLearner> = emptyList(),
    val skillItemizers: List<ItemizerEntry> = emptyList(),
    val skillMultiTrees: List<MultiTreeRecipe> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class FusionViewModel : ViewModel() {

    private val _state = MutableStateFlow(FusionState())
    val state: StateFlow<FusionState> = _state.asStateFlow()

    private var fusionCalculator: FusionCalculator? = null

    fun loadData(context: Context, seriesId: String, gameId: String, dataPath: String) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true, errorMessage = null)

            try {
                val result = withContext(Dispatchers.IO) {
                    val prefs = AppPreferences(context)
                    val settings = prefs.getSettings()

                    // Load personas
                    val allPersonas = JsonLoader.loadPersonas(context, dataPath)

                    // DLC persona names per game (fallback for JSONs without isDlc field)
                    val dlcNames: Set<String> = when (gameId) {
                        "p3fes", "p3p" -> emptySet()
                        "p3r" -> setOf(
                            "Arsene", "Captain Kidd", "Zorro", "Carmen", "Goemon",
                            "Johanna", "Milady", "Robin Hood", "Cendrillon", "Satanael",
                            "Seiten Taisei A", "Mercurius", "Hecate", "Kamu Susano-o",
                            "Anat", "Astarte", "Loki A", "Vanadis", "Izanagi",
                            "Magatsu-Izanagi", "Kaguya"
                        )
                        "p4" -> emptySet()
                        "p4g" -> emptySet()
                        "p5" -> setOf(
                            "Izanagi", "Izanagi Picaro", "Orpheus", "Orpheus Picaro",
                            "Ariadne", "Ariadne Picaro", "Asterius", "Asterius Picaro",
                            "Thanatos", "Thanatos Picaro", "Magatsu-Izanagi", "Magatsu-Izanagi Picaro",
                            "Kaguya", "Kaguya Picaro", "Tsukiyomi", "Tsukiyomi Picaro",
                            "Messiah", "Messiah Picaro"
                        )
                        "p5r" -> setOf(
                            "Orpheus F", "Orpheus F Picaro", "Izanagi", "Izanagi Picaro",
                            "Orpheus", "Orpheus Picaro", "Raoul",
                            "Athena", "Athena Picaro", "Ariadne", "Ariadne Picaro",
                            "Asterius", "Asterius Picaro", "Thanatos", "Thanatos Picaro",
                            "Magatsu-Izanagi", "Magatsu-Izanagi Picaro",
                            "Kaguya", "Kaguya Picaro", "Tsukiyomi", "Tsukiyomi Picaro",
                            "Messiah", "Messiah Picaro",
                            "Izanagi-no-Okami", "Izanagi-no-Okami Picaro"
                        )
                        else -> emptySet()
                    }

                    val personas = allPersonas.filter { p ->
                        val isDlc = p.isDlc == true || dlcNames.contains(p.name)
                        (settings.showDlc || !isDlc) &&
                        (settings.showEpisodeAigis || p.episodeAigis != true)
                    }

                    val allSkills = personas.flatMap { p ->
                        p.skills?.keys ?: emptySet()
                    }.toSet().sorted()

                    // Resolve fusion chart path
                    val chartPath = when (gameId) {
                        "p3fes"     -> "data/fusion-charts/p3-fusion-chart.json"
                        "p3p"       -> "data/fusion-charts/p3p-fusion-chart.json"
                        "p3r"       -> "data/fusion-charts/p3r-fusion-chart.json"
                        "p4"        -> "data/fusion-charts/p4-base-fusion-chart.json"
                        "p4g"       -> "data/fusion-charts/p4-fusion-chart.json"
                        "p5"        -> "data/fusion-charts/p5-base-fusion-chart.json"
                        "p5r"       -> "data/fusion-charts/p5-fusion-chart.json"
                        else        -> null
                    }

                    if (chartPath == null) {
                        return@withContext Triple(personas, null as FusionChart?, Pair(emptyMap<String, List<List<String>>>(), allSkills))
                    }

                    // Load fusion chart — fall back to p3 chart if game-specific one is missing
                    val chart = loadChartOrNull(context, chartPath)
                        ?: loadChartOrNull(context, "data/fusion-charts/p3-fusion-chart.json")
                        ?: return@withContext Triple(personas, null as FusionChart?, Pair(emptyMap<String, List<List<String>>>(), allSkills))

                    // Load special fusions
                    val specialPath = when (gameId) {
                        "p3fes", "p3p" -> "data/special-fusions/p3-special.json"
                        "p3r"          -> "data/special-fusions/p3r-special.json"
                        "p4", "p4g"    -> "data/special-fusions/p4-special.json"
                        "p5"           -> "data/special-fusions/p5-special.json"
                        "p5r"          -> "data/special-fusions/p5r-special.json"
                        else           -> null
                    }
                    val specialFusions: Map<String, List<List<String>>> = if (specialPath != null) {
                        loadSpecialOrEmpty(context, specialPath)
                    } else emptyMap()

                    Triple(personas, chart, Pair(specialFusions, allSkills))
                }

                val (personas, chart, specialAndSkills) = result
                val specialFusions = specialAndSkills.first
                val allSkills = specialAndSkills.second

                if (chart == null) {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        errorMessage = "Fusion not available for this game"
                    )
                    return@launch
                }

                // P3R/P5/P5R use a triangular matrix
                val isTriangular = gameId in listOf("p3r", "p5", "p5r")

                fusionCalculator = FusionCalculator(
                    chart = chart,
                    allPersonas = personas,
                    specialFusions = specialFusions,
                    isTriangular = isTriangular
                )

                _state.value = _state.value.copy(
                    personas = personas.sortedBy { it.name },
                    allAvailableSkills = allSkills,
                    isLoading = false
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Failed to load data: ${e.message}"
                )
            }
        }
    }

    fun isSpecialFusion(name: String): Boolean {
        return fusionCalculator?.isSpecialFusion(name) == true
    }

    fun selectPersona(persona: Persona) {
        val calculator = fusionCalculator
        val isSpecial = calculator?.isSpecialFusion(persona.name) == true

        if (isSpecial) {
            _state.value = _state.value.copy(
                selectedPersona = persona,
                fusionType = FusionType.NORMAL,
                fusionRecipes = emptyList(),
                isLoading = true
            )
            viewModelScope.launch {
                val recipes = withContext(Dispatchers.Default) {
                    calculator?.calculateFusionsFor(persona) ?: emptyList()
                }
                _state.value = _state.value.copy(
                    fusionRecipes = recipes,
                    isLoading = false
                )
            }
        } else {
            _state.value = _state.value.copy(
                selectedPersona = persona,
                fusionType = null,
                fusionRecipes = emptyList()
            )
        }
    }

    fun selectFusionType(type: FusionType) {
        val persona = _state.value.selectedPersona ?: return
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)

            val calculator = fusionCalculator
            if (calculator == null) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    errorMessage = "Fusion calculator not initialized"
                )
                return@launch
            }

            val recipes = withContext(Dispatchers.Default) {
                if (type == FusionType.NORMAL) {
                    calculator.calculateFusionsFor(persona)
                } else {
                    calculator.calculateTripleFusionsFor(persona)
                }
            }

            _state.value = _state.value.copy(
                fusionType = type,
                fusionRecipes = recipes,
                isLoading = false
            )
        }
    }

    fun setCalculatorMode(mode: CalculatorMode) {
        _state.value = _state.value.copy(
            calculatorMode = mode,
            selectedPersona = null,
            fusionType = null,
            fusionRecipes = emptyList(),
            selectedIngredients = listOf(null, null, null),
            forwardResult = null,
            forwardSourcePersona = null,
            forwardOptions = emptyList(),
            forwardOptionQuery = ""
        )
    }

    fun setForwardSubTab(tab: ForwardSubTab) {
        _state.value = _state.value.copy(forwardSubTab = tab)
    }

    fun setIngredient(index: Int, persona: Persona?) {
        val current = _state.value.selectedIngredients.toMutableList()
        if (index in current.indices) {
            current[index] = persona
            _state.value = _state.value.copy(selectedIngredients = current)
            calculateForwardResult()
        }
    }

    fun clearIngredients() {
        _state.value = _state.value.copy(
            selectedIngredients = listOf(null, null, null),
            forwardResult = null
        )
    }

    fun swapIngredients() {
        val current = _state.value.selectedIngredients
        val swapped = listOf(current.getOrNull(1), current.getOrNull(0), current.getOrNull(2))
        _state.value = _state.value.copy(selectedIngredients = swapped)
        calculateForwardResult()
    }

    fun setForwardSourcePersona(persona: Persona?) {
        if (persona == null) {
            _state.value = _state.value.copy(
                forwardSourcePersona = null,
                forwardOptions = emptyList(),
                forwardOptionQuery = ""
            )
            return
        }

        _state.value = _state.value.copy(
            forwardSourcePersona = persona,
            isLoading = true
        )

        viewModelScope.launch {
            val calculator = fusionCalculator
            val options = withContext(Dispatchers.Default) {
                calculator?.calculateForwardFusionsFrom(persona) ?: emptyList()
            }
            _state.value = _state.value.copy(
                forwardOptions = options,
                isLoading = false
            )
        }
    }

    fun setForwardOptionQuery(query: String) {
        _state.value = _state.value.copy(forwardOptionQuery = query)
    }

    private fun calculateForwardResult() {
        val ingredients = _state.value.selectedIngredients.filterNotNull()
        val calculator = fusionCalculator
        if (calculator == null || ingredients.size < 2) {
            _state.value = _state.value.copy(forwardResult = null)
            return
        }

        val result = calculator.fuse(ingredients)
        _state.value = _state.value.copy(forwardResult = result)
    }

    // --- Skill Routes methods ---

    fun setSkillRouteTarget(persona: Persona?) {
        _state.value = _state.value.copy(skillRouteTarget = persona)
        calculateSkillRoutes()
    }

    fun addSkillRouteSkill(skillName: String) {
        val current = _state.value.skillRouteSkills
        if (!current.contains(skillName)) {
            _state.value = _state.value.copy(skillRouteSkills = current + skillName)
            calculateSkillRoutes()
        }
    }

    fun removeSkillRouteSkill(skillName: String) {
        val current = _state.value.skillRouteSkills
        _state.value = _state.value.copy(skillRouteSkills = current - skillName)
        calculateSkillRoutes()
    }

    fun clearSkillRouteSkills() {
        _state.value = _state.value.copy(skillRouteSkills = emptyList())
        calculateSkillRoutes()
    }

    private fun calculateSkillRoutes() {
        val target = _state.value.skillRouteTarget
        val skills = _state.value.skillRouteSkills
        val personas = _state.value.personas
        val calculator = fusionCalculator

        if (skills.isEmpty()) {
            _state.value = _state.value.copy(
                skillLearners = emptyList(),
                skillItemizers = emptyList(),
                skillMultiTrees = emptyList()
            )
            return
        }

        val learners = mutableListOf<DirectLearner>()
        val itemizers = mutableListOf<ItemizerEntry>()

        skills.forEach { sk ->
            personas.forEach { p ->
                val lvl = p.skills?.get(sk)
                if (lvl != null) {
                    learners.add(
                        DirectLearner(
                            personaName = p.name,
                            arcana = p.arcana ?: "",
                            level = p.level ?: 0,
                            skillName = sk,
                            skillLevel = lvl.toInt()
                        )
                    )
                }
                if (p.item?.contains(sk, ignoreCase = true) == true || p.itemr?.contains(sk, ignoreCase = true) == true) {
                    itemizers.add(
                        ItemizerEntry(
                            personaName = p.name,
                            arcana = p.arcana ?: "",
                            level = p.level ?: 0,
                            skillName = sk,
                            itemDescription = p.item ?: p.itemr ?: ""
                        )
                    )
                }
            }
        }

        val multiTrees = mutableListOf<MultiTreeRecipe>()
        if (target != null && calculator != null && skills.size >= 2) {
            val recipes = calculator.calculateFusionsFor(target)
            val s1 = skills[0]
            val s2 = skills[1]
            for (rec in recipes) {
                if (rec.personas.size == 2) {
                    val pA = rec.personas[0]
                    val pB = rec.personas[1]
                    val pALearnsS1 = pA.skills?.containsKey(s1) == true
                    val pBLearnsS2 = pB.skills?.containsKey(s2) == true
                    val pALearnsS2 = pA.skills?.containsKey(s2) == true
                    val pBLearnsS1 = pB.skills?.containsKey(s1) == true

                    if (pALearnsS1 && pBLearnsS2) {
                        multiTrees.add(
                            MultiTreeRecipe(
                                targetPersona = target.name,
                                parentA = pA.name,
                                parentASkill = s1,
                                parentB = pB.name,
                                parentBSkill = s2,
                                description = "${pA.name} [learns $s1] + ${pB.name} [learns $s2] => ${target.name}"
                            )
                        )
                    } else if (pALearnsS2 && pBLearnsS1) {
                        multiTrees.add(
                            MultiTreeRecipe(
                                targetPersona = target.name,
                                parentA = pA.name,
                                parentASkill = s2,
                                parentB = pB.name,
                                parentBSkill = s1,
                                description = "${pA.name} [learns $s2] + ${pB.name} [learns $s1] => ${target.name}"
                            )
                        )
                    }
                }
            }
        }

        _state.value = _state.value.copy(
            skillLearners = learners.sortedWith(compareBy({ it.skillName }, { it.level })),
            skillItemizers = itemizers,
            skillMultiTrees = multiTrees
        )
    }

    fun getPersonaCost(persona: Persona): Int {
        val calculator = fusionCalculator ?: return 0
        return calculator.estimatePersonaCost(persona.level ?: 0)
    }

    fun getRecipeCost(recipe: FusionRecipe): Int {
        val calculator = fusionCalculator ?: return 0
        return recipe.personas.sumOf { calculator.estimatePersonaCost(it.level ?: 0) }
    }

    fun clearSelection() {
        _state.value = _state.value.copy(
            selectedPersona = null,
            fusionType = null,
            fusionRecipes = emptyList(),
            selectedIngredients = listOf(null, null, null),
            forwardResult = null,
            forwardSourcePersona = null,
            forwardOptions = emptyList(),
            forwardOptionQuery = ""
        )
    }

    // --- helpers ---

    private fun loadChartOrNull(context: Context, path: String): FusionChart? {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            Gson().fromJson(json, FusionChart::class.java)
        } catch (e: Exception) {
            null
        }
    }

    private fun loadSpecialOrEmpty(context: Context, path: String): Map<String, List<List<String>>> {
        return try {
            val json = context.assets.open(path).bufferedReader().use { it.readText() }
            Gson().fromJson(json, object : TypeToken<Map<String, List<List<String>>>>() {}.type)
        } catch (e: Exception) {
            emptyMap()
        }
    }
}
