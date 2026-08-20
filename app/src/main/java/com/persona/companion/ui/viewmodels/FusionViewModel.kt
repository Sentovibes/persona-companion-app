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
    REVERSE, FORWARD
}

enum class ForwardSubTab {
    CALCULATOR, FROM_PERSONA
}

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
                        return@withContext Triple(personas, null as FusionChart?, emptyMap<String, List<List<String>>>())
                    }

                    // Load fusion chart — fall back to p3 chart if game-specific one is missing
                    val chart = loadChartOrNull(context, chartPath)
                        ?: loadChartOrNull(context, "data/fusion-charts/p3-fusion-chart.json")
                        ?: return@withContext Triple(personas, null as FusionChart?, emptyMap<String, List<List<String>>>())

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

                    Triple(personas, chart, specialFusions)
                }

                val (personas, chart, specialFusions) = result

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
