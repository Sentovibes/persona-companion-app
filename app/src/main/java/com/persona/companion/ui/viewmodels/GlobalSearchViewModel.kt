package com.persona.companion.ui.viewmodels

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.persona.companion.data.AppPreferences
import com.persona.companion.data.PersonaRepository
import com.persona.companion.data.SeriesData
import com.persona.companion.data.SocialLinkLoader
import com.persona.companion.data.UserPreferences
import com.persona.companion.data.repositories.ItemRepository
import com.persona.companion.data.repositories.SkillRepository
import com.persona.companion.models.SearchCategory
import com.persona.companion.models.SearchResultItem
import com.persona.companion.navigation.Screen
import com.persona.companion.utils.JsonLoader
import com.persona.companion.utils.SpoilerUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class GlobalSearchState(
    val query: String = "",
    val selectedCategory: SearchCategory? = null,
    val selectedGameId: String? = null, // null means all games
    val isNoSpoilersMode: Boolean = false,
    val isLoading: Boolean = false,
    val results: List<SearchResultItem> = emptyList()
)

class GlobalSearchViewModel(application: Application) : AndroidViewModel(application) {

    private val userPrefs = UserPreferences(application)
    private val appPrefs = AppPreferences(application)
    private val itemRepo = ItemRepository(application)
    private val skillRepo = SkillRepository(application)

    private val _state = MutableStateFlow(
        GlobalSearchState(
            isNoSpoilersMode = userPrefs.isNoSpoilersMode()
        )
    )
    val state: StateFlow<GlobalSearchState> = _state.asStateFlow()

    // Cached index of search items per game
    private val gameItemsCache = mutableMapOf<String, List<SearchResultItem>>()
    private var isIndexing = false
    private var searchJob: Job? = null

    init {
        // Preload index in background
        viewModelScope.launch(Dispatchers.IO) {
            indexAllGames()
        }
    }

    fun onQueryChange(newQuery: String) {
        _state.value = _state.value.copy(query = newQuery)
        searchJob?.cancel()
        searchJob = viewModelScope.launch {
            delay(120) // Snappy debounce
            performSearch()
        }
    }

    fun selectCategory(category: SearchCategory?) {
        _state.value = _state.value.copy(selectedCategory = category)
        performSearch()
    }

    fun selectGame(gameId: String?) {
        _state.value = _state.value.copy(selectedGameId = gameId)
        performSearch()
    }

    fun toggleNoSpoilersMode() {
        val next = !_state.value.isNoSpoilersMode
        userPrefs.setNoSpoilersMode(next)
        _state.value = _state.value.copy(isNoSpoilersMode = next)
        performSearch()
    }

    private suspend fun indexAllGames() {
        if (isIndexing) return
        isIndexing = true

        val settings = appPrefs.getSettings()

        for (series in SeriesData.allSeries) {
            for (game in series.games) {
                val items = mutableListOf<SearchResultItem>()
                val gameTitle = game.title

                // 1. Personas
                game.dataPath?.let { path ->
                    try {
                        val personas = JsonLoader.loadPersonas(getApplication(), path)
                        personas.forEach { p ->
                            val isDlc = p.isDlc == true || (p.dlc != null && p.dlc > 0)
                            if (settings.showDlc || !isDlc) {
                                val isSpoiler = SpoilerUtils.isSpoilerPersona(p.name)
                                items.add(
                                    SearchResultItem(
                                        title = p.name,
                                        subtitle = "Lv ${p.level ?: 1} ${p.arcana ?: "Persona"}",
                                        category = SearchCategory.PERSONA,
                                        seriesId = series.id,
                                        gameId = game.id,
                                        gameTitle = gameTitle,
                                        destinationRoute = Screen.PersonaDetail.createRoute(series.id, game.id, p.name),
                                        isSpoiler = isSpoiler,
                                        extraInfo = p.arcana
                                    )
                                )
                            }
                        }
                    } catch (_: Exception) {}
                }

                // 2. Enemies & Bosses
                game.enemyPath?.let { path ->
                    try {
                        val enemies = JsonLoader.loadEnemies(getApplication(), path)
                        enemies.forEach { e ->
                            val isSpoiler = SpoilerUtils.isSpoilerBoss(e.name)
                            val typeLabel = when {
                                e.isBoss -> "Main Boss"
                                e.isMiniBoss -> "Mini-Boss"
                                else -> "Shadow"
                            }
                            val safeArea = if (e.area.isNotBlank()) e.area else "Unknown"
                            items.add(
                                SearchResultItem(
                                    title = e.name,
                                    subtitle = "Lv ${e.level} • $typeLabel • ${e.area}",
                                    category = SearchCategory.ENEMY,
                                    seriesId = series.id,
                                    gameId = game.id,
                                    gameTitle = gameTitle,
                                    destinationRoute = Screen.EnemyDetail.createRoute(series.id, game.id, e.name, safeArea),
                                    isSpoiler = isSpoiler,
                                    extraInfo = typeLabel
                                )
                            )
                        }
                    } catch (_: Exception) {}
                }

                // 3. Skills
                game.skillPath?.let { path ->
                    try {
                        val skills = skillRepo.getSkills(path)
                        skills.forEach { s ->
                            items.add(
                                SearchResultItem(
                                    title = s.name,
                                    subtitle = "${s.element} • ${s.effect}",
                                    category = SearchCategory.SKILL,
                                    seriesId = series.id,
                                    gameId = game.id,
                                    gameTitle = gameTitle,
                                    destinationRoute = Screen.SkillList.createRoute(series.id, game.id),
                                    isSpoiler = false,
                                    extraInfo = s.element
                                )
                            )
                        }
                    } catch (_: Exception) {}
                }

                // 4. Items
                game.itemPath?.let { path ->
                    try {
                        val itemList = itemRepo.getItems(game.id, path, game.aigisItemPath)
                        itemList.forEach { itm ->
                            items.add(
                                SearchResultItem(
                                    title = itm.name,
                                    subtitle = "${itm.category ?: "Item"} • ${itm.effect ?: itm.description ?: ""}",
                                    category = SearchCategory.ITEM,
                                    seriesId = series.id,
                                    gameId = game.id,
                                    gameTitle = gameTitle,
                                    destinationRoute = Screen.ItemList.createRoute(series.id, game.id),
                                    isSpoiler = false,
                                    extraInfo = itm.category
                                )
                            )
                        }
                    } catch (_: Exception) {}
                }

                // 5. Social Links
                try {
                    val slData = SocialLinkLoader.loadSocialLinks(getApplication(), game.id)
                    slData?.socialLinks?.forEach { sl ->
                        val charName = sl.characterName ?: ""
                        val title = if (charName.isNotBlank()) "${sl.arcana} ($charName)" else sl.arcana
                        val isSpoiler = SpoilerUtils.isSpoilerSocialLink(sl.arcana) || SpoilerUtils.isSpoilerSocialLink(charName)
                        items.add(
                            SearchResultItem(
                                title = title,
                                subtitle = "Social Link / Confidant • ${sl.ranks.size} ranks",
                                category = SearchCategory.SOCIAL_LINK,
                                seriesId = series.id,
                                gameId = game.id,
                                gameTitle = gameTitle,
                                destinationRoute = Screen.SocialLinks.createRoute(series.id, game.id),
                                isSpoiler = isSpoiler,
                                extraInfo = sl.arcana
                            )
                        )
                    }
                } catch (_: Exception) {}

                // 6. Requests
                game.requestPath?.let { path ->
                    try {
                        val reqs = JsonLoader.loadRequests(getApplication(), path)
                        reqs.forEach { rq ->
                            items.add(
                                SearchResultItem(
                                    title = rq.name,
                                    subtitle = "Request • Reward: ${rq.reward}",
                                    category = SearchCategory.QUEST,
                                    seriesId = series.id,
                                    gameId = game.id,
                                    gameTitle = gameTitle,
                                    destinationRoute = Screen.RequestList.createRoute(series.id, game.id),
                                    isSpoiler = false
                                )
                            )
                        }
                    } catch (_: Exception) {}
                }

                gameItemsCache[game.id] = items
            }
        }
        isIndexing = false
        performSearch()
    }

    private fun performSearch() {
        val q = _state.value.query.trim().lowercase()
        val cat = _state.value.selectedCategory
        val targetGame = _state.value.selectedGameId

        if (q.isEmpty()) {
            _state.value = _state.value.copy(results = emptyList(), isLoading = false)
            return
        }

        viewModelScope.launch(Dispatchers.Default) {
            val candidatePool = if (targetGame != null) {
                gameItemsCache[targetGame] ?: emptyList()
            } else {
                gameItemsCache.values.flatten()
            }

            val filtered = candidatePool.filter { item ->
                val matchesCategory = cat == null || item.category == cat
                val matchesQuery = item.title.lowercase().contains(q) ||
                        item.subtitle.lowercase().contains(q) ||
                        (item.extraInfo?.lowercase()?.contains(q) == true)
                matchesCategory && matchesQuery
            }.sortedWith(
                compareBy(
                    // Exact prefix match first
                    { !it.title.lowercase().startsWith(q) },
                    // Title contains before subtitle
                    { !it.title.lowercase().contains(q) },
                    // Category grouping
                    { it.category.ordinal },
                    { it.title }
                )
            ).take(120) // Limit display count for fast UI rendering

            withContext(Dispatchers.Main) {
                _state.value = _state.value.copy(results = filtered, isLoading = false)
            }
        }
    }
}
