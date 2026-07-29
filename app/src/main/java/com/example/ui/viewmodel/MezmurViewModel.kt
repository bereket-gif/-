package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.MezmurDatabase
import com.example.data.local.MezmurEntity
import com.example.data.local.SearchHistoryEntity
import com.example.data.repository.MezmurRepository
import com.example.util.GeezUtil
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

enum class AppNavTab(val title: String) {
    MEZMURAT("መዝሙራት"),
    WELAYTGNA("ወላይትኛ"),
    WUDASE_MARIAM("ውዳሴ ማርያም"),
    FAVORITES("ተወዳጅ"),
    INFO("ስለ እኛ")
}

class MezmurViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = MezmurRepository(MezmurDatabase.getDatabase(application).mezmurDao(), application)

    val syncStatus: StateFlow<String> = repository.syncStatus
    val isSyncing: StateFlow<Boolean> = repository.isSyncing

    val recentSearchHistory: StateFlow<List<SearchHistoryEntity>> = repository.recentSearchHistory
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertSearchQuery(query: String) {
        viewModelScope.launch {
            repository.insertSearchQuery(query)
        }
    }

    fun deleteSearchQuery(query: String) {
        viewModelScope.launch {
            repository.deleteSearchQuery(query)
        }
    }

    fun clearSearchHistory() {
        viewModelScope.launch {
            repository.clearSearchHistory()
        }
    }

    private val _mezmuratSearchQuery = MutableStateFlow("")
    val mezmuratSearchQuery: StateFlow<String> = _mezmuratSearchQuery

    private val _welaytgnaSearchQuery = MutableStateFlow("")
    val welaytgnaSearchQuery: StateFlow<String> = _welaytgnaSearchQuery

    private val _selectedCategory = MutableStateFlow("ሁሉም")
    val selectedCategory: StateFlow<String> = _selectedCategory

    private val _selectedTab = MutableStateFlow(AppNavTab.MEZMURAT)
    val selectedTab: StateFlow<AppNavTab> = _selectedTab

    private val sharedPrefs = application.getSharedPreferences("lyrics_settings", Context.MODE_PRIVATE)

    private val _fontSizeSp = MutableStateFlow(sharedPrefs.getFloat("font_size", 18f))
    val fontSizeSp: StateFlow<Float> = _fontSizeSp

    private val _selectedMezmur = MutableStateFlow<MezmurEntity?>(null)
    val selectedMezmur: StateFlow<MezmurEntity?> = _selectedMezmur

    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog: StateFlow<Boolean> = _showAddDialog

    val categories = listOf("ሁሉም", "የንስሐ", "የምስጋና", "የበዓላት", "የድንግል ማርያም", "የኪዳን", "ወላይትኛ")

    private val dayOrder = listOf("እሑድ", "ሰኞ", "ማክሰኞ", "ረቡዕ", "ሐሙስ", "ዓርብ", "ቅዳሜ")

    private val numberRegex = Regex("""^(\d+)""")

    val allMezmurs: StateFlow<List<MezmurEntity>> = repository.allMezmurs.map { allList ->
        // 1. Non-Wudase mezmurs sorted alphabetically by title
        val nonWudase = allList.filter { it.category != "ውዳሴ ማርያም" }
            .sortedBy { it.title }
            .mapIndexed { index, mezmur ->
                mezmur.copy(
                    numberInt = index + 1,
                    numberGeez = GeezUtil.toGeezNumeral(index + 1)
                )
            }

        // 2. Wudase Amharic sorted by title (with 1 / 2 / 3 natural number ordering)
        val wudaseAmharicList = allList.filter { it.category == "ውዳሴ ማርያም" && (it.artist.contains("አማርኛ") || it.id.contains("amharic")) }
        val wudaseAmharic = wudaseAmharicList
            .sortedWith(compareBy<MezmurEntity> { mezmur ->
                val match = numberRegex.find(mezmur.title.trim())
                match?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
            }.thenBy { mezmur ->
                val idx = dayOrder.indexOfFirst { mezmur.title.contains(it) }
                if (idx >= 0) idx else 99
            }.thenBy { it.title })
            .mapIndexed { index, mezmur ->
                mezmur.copy(
                    numberInt = index + 1,
                    numberGeez = GeezUtil.toGeezNumeral(index + 1)
                )
            }

        // 3. Wudase Geez sorted by title (with 1 / 2 / 3 natural number ordering)
        val wudaseGeezList = allList.filter { it.category == "ውዳሴ ማርያም" && (it.artist.contains("ግዕዝ") || it.id.contains("geez")) }
        val wudaseGeez = wudaseGeezList
            .sortedWith(compareBy<MezmurEntity> { mezmur ->
                val match = numberRegex.find(mezmur.title.trim())
                match?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
            }.thenBy { mezmur ->
                val idx = dayOrder.indexOfFirst { mezmur.title.contains(it) }
                if (idx >= 0) idx else 99
            }.thenBy { it.title })
            .mapIndexed { index, mezmur ->
                mezmur.copy(
                    numberInt = index + 1,
                    numberGeez = GeezUtil.toGeezNumeral(index + 1)
                )
            }

        val remainingWudase = allList.filter {
            it.category == "ውዳሴ ማርያም" &&
            !wudaseAmharicList.contains(it) &&
            !wudaseGeezList.contains(it)
        }

        nonWudase + wudaseAmharic + wudaseGeez + remainingWudase
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val mezmursList: StateFlow<List<MezmurEntity>> = combine(
        _mezmuratSearchQuery,
        _welaytgnaSearchQuery,
        _selectedCategory,
        _selectedTab,
        allMezmurs
    ) { mezmuratQuery, welaytgnaQuery, category, tab, all ->
        var sourceList = when (tab) {
            AppNavTab.FAVORITES -> all.filter { it.isFavorite }
            AppNavTab.WUDASE_MARIAM -> all.filter { it.category == "ውዳሴ ማርያም" }
            AppNavTab.WELAYTGNA -> all.filter { it.category == "ወላይትኛ" }
            AppNavTab.MEZMURAT -> all.filter { it.category != "ውዳሴ ማርያም" && it.category != "ወላይትኛ" }
            else -> all.filter { it.category != "ውዳሴ ማርያም" && it.category != "ወላይትኛ" }
        }

        if (category != "ሁሉም") {
            sourceList = sourceList.filter { it.category == category }
        }

        val query = when (tab) {
            AppNavTab.MEZMURAT -> mezmuratQuery
            AppNavTab.WELAYTGNA -> welaytgnaQuery
            else -> ""
        }

        if (query.isNotBlank()) {
            val q = query.trim()
            sourceList = sourceList.filter {
                it.title.contains(q, ignoreCase = true) ||
                it.lyrics.contains(q, ignoreCase = true) ||
                it.artist.contains(q, ignoreCase = true)
            }
        }

        sourceList.sortedBy { it.title }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChange(tab: AppNavTab, query: String) {
        when (tab) {
            AppNavTab.MEZMURAT -> _mezmuratSearchQuery.value = query
            AppNavTab.WELAYTGNA -> _welaytgnaSearchQuery.value = query
            else -> { /* no-op for other tabs */ }
        }
    }

    fun onCategorySelect(category: String) {
        _selectedCategory.value = category
    }

    fun onTabSelect(tab: AppNavTab) {
        _selectedTab.value = tab
    }

    fun selectMezmur(mezmur: MezmurEntity?) {
        _selectedMezmur.value = mezmur
    }

    fun toggleFavorite(mezmur: MezmurEntity) {
        viewModelScope.launch {
            repository.toggleFavorite(mezmur.id, mezmur.isFavorite)
            // update currently open detail if same
            if (_selectedMezmur.value?.id == mezmur.id) {
                _selectedMezmur.value = _selectedMezmur.value?.copy(isFavorite = !mezmur.isFavorite)
            }
        }
    }

    fun zoomInText() {
        if (_fontSizeSp.value < 38f) {
            val newSize = _fontSizeSp.value + 2f
            _fontSizeSp.value = newSize
            saveFontSize(newSize)
        }
    }

    fun zoomOutText() {
        if (_fontSizeSp.value > 14f) {
            val newSize = _fontSizeSp.value - 2f
            _fontSizeSp.value = newSize
            saveFontSize(newSize)
        }
    }

    fun setFontSize(size: Float) {
        val coerced = size.coerceIn(14f, 38f)
        _fontSizeSp.value = coerced
        saveFontSize(coerced)
    }

    private fun saveFontSize(size: Float) {
        sharedPrefs.edit().putFloat("font_size", size).apply()
    }

    fun showAddMezmurDialog(show: Boolean) {
        _showAddDialog.value = show
    }

    fun addCustomMezmur(title: String, artist: String, category: String, lyrics: String) {
        viewModelScope.launch {
            repository.addCustomMezmur(title, artist, category, lyrics)
            _showAddDialog.value = false
        }
    }

    fun triggerSync() {
        viewModelScope.launch {
            repository.triggerCloudSync()
        }
    }
}
