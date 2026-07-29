package com.example.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.activity.compose.BackHandler
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.zIndex
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.local.MezmurEntity
import com.example.ui.components.AddMezmurDialog
import com.example.ui.components.AppFooterBanner
import com.example.ui.components.LyricsDetailContent
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.LazyListState
import com.example.ui.theme.*
import com.example.ui.viewmodel.AppNavTab
import com.example.ui.viewmodel.MezmurViewModel
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MezmurMainScreen(viewModel: MezmurViewModel) {
    val mezmurs by viewModel.mezmursList.collectAsStateWithLifecycle()
    val allMezmurs by viewModel.allMezmurs.collectAsStateWithLifecycle()
    val mezmuratSearchQuery by viewModel.mezmuratSearchQuery.collectAsStateWithLifecycle()
    val welaytgnaSearchQuery by viewModel.welaytgnaSearchQuery.collectAsStateWithLifecycle()
    val selectedTab by viewModel.selectedTab.collectAsStateWithLifecycle()
    val fontSizeSp by viewModel.fontSizeSp.collectAsStateWithLifecycle()
    val showAddDialog by viewModel.showAddDialog.collectAsStateWithLifecycle()
    val recentSearchHistory by viewModel.recentSearchHistory.collectAsStateWithLifecycle()

    var isSearchFocused by remember { mutableStateOf(false) }

    // Independent Scroll States for each tab
    val mezmuratListState = rememberLazyListState()
    val welaytgnaListState = rememberLazyListState()
    val favoritesListState = rememberLazyListState()
    val wudaseListState = rememberLazyListState()

    // Independent Selected Lyric Tracker for each tab
    val selectedMezmurIdMap = remember { mutableStateMapOf<AppNavTab, String?>() }
    val currentTabSelectedMezmurId = selectedMezmurIdMap[selectedTab]
    val currentTabSelectedMezmur = remember(currentTabSelectedMezmurId, allMezmurs) {
        allMezmurs.find { it.id == currentTabSelectedMezmurId }
    }

    val focusManager = LocalFocusManager.current
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    val anyBackActive = drawerState.isOpen || isSearchFocused || currentTabSelectedMezmur != null || showAddDialog || selectedTab != AppNavTab.MEZMURAT
    
    BackHandler(enabled = anyBackActive) {
        if (drawerState.isOpen) {
            coroutineScope.launch { drawerState.close() }
        } else if (isSearchFocused) {
            focusManager.clearFocus()
        } else if (currentTabSelectedMezmur != null) {
            selectedMezmurIdMap[selectedTab] = null
        } else if (showAddDialog) {
            viewModel.showAddMezmurDialog(false)
        } else if (selectedTab != AppNavTab.MEZMURAT) {
            viewModel.onTabSelect(AppNavTab.MEZMURAT)
        }
    }

    if (currentTabSelectedMezmur != null) {
        LyricsDetailContent(
            mezmur = currentTabSelectedMezmur,
            fontSizeSp = fontSizeSp,
            onZoomIn = { viewModel.zoomInText() },
            onZoomOut = { viewModel.zoomOutText() },
            onFontSizeChange = { viewModel.setFontSize(it) },
            onToggleFavorite = { viewModel.toggleFavorite(it) },
            onDismiss = { selectedMezmurIdMap[selectedTab] = null }
        )
    } else {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.fillMaxWidth(0.7f),
                drawerContainerColor = ChurchBackground,
                drawerContentColor = ChurchTealDark
            ) {
                // Header in Drawer
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChurchTealDark)
                        .statusBarsPadding()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(ChurchGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⛪", fontSize = 26.sp)
                    }
                    Text(
                        text = "መዝሙር ደብተር",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = ChurchGold
                    )
                    Text(
                        text = "የኢኦተቤ መዝሙራትና ጸሎት ደብተር",
                        fontSize = 12.sp,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Navigation Items in Drawer Menu
                AppNavTab.entries.forEach { tab ->
                    val isSelected = selectedTab == tab
                    NavigationDrawerItem(
                        label = {
                            Text(
                                text = tab.title,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 15.sp
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            if (selectedTab != tab) {
                                viewModel.onTabSelect(tab)
                            }
                            coroutineScope.launch { drawerState.close() }
                        },
                        icon = {
                            Icon(
                                imageVector = when (tab) {
                                    AppNavTab.MEZMURAT -> Icons.Default.MenuBook
                                    AppNavTab.WELAYTGNA -> Icons.Default.MenuBook
                                    AppNavTab.FAVORITES -> Icons.Default.Favorite
                                    AppNavTab.WUDASE_MARIAM -> Icons.Default.AutoAwesome
                                    AppNavTab.INFO -> Icons.Outlined.Info
                                },
                                contentDescription = tab.title,
                                tint = if (isSelected) ChurchTealDark else TextSecondary
                            )
                        },
                        colors = NavigationDrawerItemDefaults.colors(
                            selectedContainerColor = ChurchTealContainer,
                            selectedIconColor = ChurchTealDark,
                            selectedTextColor = ChurchTealDark,
                            unselectedContainerColor = Color.Transparent,
                            unselectedTextColor = TextPrimary
                        ),
                        modifier = Modifier
                            .padding(horizontal = 12.dp, vertical = 2.dp)
                            .testTag("drawer_tab_${tab.name}")
                    )
                }
            }
        }
    ) {
        Scaffold(
            topBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChurchTealDark)
                ) {
                    // Top App Bar Header with Hamburger Menu Icon
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        // Hamburger Menu Button
                        IconButton(
                            onClick = { coroutineScope.launch { drawerState.open() } },
                            modifier = Modifier.testTag("menu_drawer_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Open Menu",
                                tint = ChurchGold
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(ChurchGold),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "⛪",
                                    fontSize = 20.sp
                                )
                            }
                            Text(
                                text = "መዝሙር ደብተር",
                                fontWeight = FontWeight.Bold,
                                fontSize = 22.sp,
                                color = ChurchGold,
                                modifier = Modifier.testTag("app_title")
                            )
                        }
                    }

                    // Search Bar Input Section - ONLY show on MEZMURAT and WELAYTGNA tabs, and when currentTabSelectedMezmur == null
                    if ((selectedTab == AppNavTab.MEZMURAT || selectedTab == AppNavTab.WELAYTGNA) && currentTabSelectedMezmur == null) {
                        val currentQuery = if (selectedTab == AppNavTab.MEZMURAT) mezmuratSearchQuery else welaytgnaSearchQuery
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 16.dp, end = 16.dp, bottom = 12.dp, top = 2.dp)
                                .zIndex(10f)
                        ) {
                            Column {
                                OutlinedTextField(
                                    value = currentQuery,
                                    onValueChange = { viewModel.onSearchQueryChange(selectedTab, it) },
                                    placeholder = {
                                        Text(
                                            if (selectedTab == AppNavTab.MEZMURAT) "መዝሙር ይፈልጉ... (Search Lyrics)" else "የወላይትኛ መዝሙር ይፈልጉ...",
                                            color = Color.White.copy(alpha = 0.6f),
                                            fontSize = 14.sp
                                        )
                                    },
                                    leadingIcon = {
                                        IconButton(onClick = {
                                            if (currentQuery.isNotBlank()) {
                                                viewModel.insertSearchQuery(currentQuery)
                                            }
                                            focusManager.clearFocus()
                                        }) {
                                            Icon(
                                                imageVector = Icons.Default.Search,
                                                contentDescription = "Search icon",
                                                tint = ChurchGold
                                            )
                                        }
                                    },
                                    trailingIcon = {
                                        if (currentQuery.isNotEmpty()) {
                                            IconButton(onClick = { viewModel.onSearchQueryChange(selectedTab, "") }) {
                                                Icon(
                                                    imageVector = Icons.Default.Clear,
                                                    contentDescription = "Clear search",
                                                    tint = Color.White.copy(alpha = 0.7f)
                                                )
                                            }
                                        }
                                    },
                                    singleLine = true,
                                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                                    keyboardActions = KeyboardActions(onSearch = {
                                        if (currentQuery.isNotBlank()) {
                                            viewModel.insertSearchQuery(currentQuery)
                                        }
                                        focusManager.clearFocus()
                                    }),
                                    colors = OutlinedTextFieldDefaults.colors(
                                        focusedContainerColor = Color.White.copy(alpha = 0.15f),
                                        unfocusedContainerColor = Color.White.copy(alpha = 0.1f),
                                        focusedBorderColor = ChurchGold,
                                        unfocusedBorderColor = Color.Transparent,
                                        focusedTextColor = Color.White,
                                        unfocusedTextColor = Color.White,
                                        cursorColor = ChurchGold
                                    ),
                                    shape = RoundedCornerShape(24.dp),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(52.dp)
                                        .onFocusChanged { isSearchFocused = it.isFocused }
                                        .testTag("search_input")
                                )

                                val isImeVisible = WindowInsets.isImeVisible
                                if (isSearchFocused && isImeVisible && recentSearchHistory.isNotEmpty()) {
                                    Card(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 4.dp),
                                        shape = RoundedCornerShape(12.dp),
                                        colors = CardDefaults.cardColors(
                                            containerColor = ChurchTealDark,
                                            contentColor = Color.White
                                        ),
                                        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                    ) {
                                        Column(
                                            modifier = Modifier.padding(8.dp)
                                        ) {
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(horizontal = 8.dp, vertical = 4.dp),
                                                horizontalArrangement = Arrangement.SpaceBetween,
                                                verticalAlignment = Alignment.CenterVertically
                                            ) {
                                                Text(
                                                    "የቅርብ ፍለጋዎች (Recent Searches)",
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = ChurchGold
                                                )
                                                Text(
                                                    "አጥፋ (Clear)",
                                                    fontSize = 12.sp,
                                                    color = Color.LightGray.copy(alpha = 0.8f),
                                                    modifier = Modifier.clickable {
                                                        viewModel.clearSearchHistory()
                                                    }
                                                )
                                            }
                                            Divider(
                                                color = Color.White.copy(alpha = 0.2f),
                                                modifier = Modifier.padding(vertical = 4.dp)
                                            )

                                            recentSearchHistory.forEach { history ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .clickable {
                                                            viewModel.onSearchQueryChange(selectedTab, history.searchQuery)
                                                            viewModel.insertSearchQuery(history.searchQuery)
                                                            focusManager.clearFocus()
                                                        }
                                                        .padding(horizontal = 8.dp, vertical = 8.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                                        Icon(
                                                            imageVector = Icons.Default.History,
                                                            contentDescription = "History Icon",
                                                            tint = Color.LightGray,
                                                            modifier = Modifier
                                                                .size(18.dp)
                                                                .padding(end = 8.dp)
                                                        )
                                                        Text(
                                                            text = history.searchQuery,
                                                            fontSize = 14.sp,
                                                            color = Color.White
                                                        )
                                                    }
                                                    IconButton(
                                                        onClick = {
                                                            viewModel.deleteSearchQuery(history.searchQuery)
                                                        },
                                                        modifier = Modifier.size(24.dp)
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Close,
                                                            contentDescription = "Delete search history item",
                                                            tint = Color.LightGray,
                                                            modifier = Modifier.size(16.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            },
            bottomBar = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChurchTealDark)
                ) {
                    AppFooterBanner()
                    NavigationBar(
                        containerColor = ChurchBackground,
                        contentColor = ChurchTealDark,
                        tonalElevation = 6.dp,
                        windowInsets = WindowInsets.navigationBars,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("bottom_nav_bar")
                    ) {
                        AppNavTab.entries.forEach { tab ->
                            val isSelected = selectedTab == tab
                            NavigationBarItem(
                                selected = isSelected,
                                onClick = {
                                    if (selectedTab != tab) {
                                        viewModel.onTabSelect(tab)
                                    }
                                },
                                icon = {
                                    Icon(
                                        imageVector = when (tab) {
                                            AppNavTab.MEZMURAT -> Icons.Default.MenuBook
                                            AppNavTab.WELAYTGNA -> Icons.Default.MenuBook
                                            AppNavTab.FAVORITES -> Icons.Default.Favorite
                                            AppNavTab.WUDASE_MARIAM -> Icons.Default.AutoAwesome
                                            AppNavTab.INFO -> Icons.Outlined.Info
                                        },
                                        contentDescription = tab.title
                                    )
                                },
                                label = {
                                    Text(
                                        text = tab.title,
                                        fontSize = 11.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = ChurchTealDark,
                                    selectedTextColor = ChurchTealDark,
                                    indicatorColor = ChurchTealContainer,
                                    unselectedIconColor = TextSecondary,
                                    unselectedTextColor = TextSecondary
                                ),
                                modifier = Modifier.testTag("nav_tab_${tab.name}")
                            )
                        }
                    }
                }
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(ChurchBackground)
            ) {
                when (selectedTab) {
                    AppNavTab.INFO -> {
                        InfoScreenContent()
                    }
                    AppNavTab.WUDASE_MARIAM -> {
                        val wudaseItems = remember(allMezmurs) {
                            allMezmurs.filter { it.category == "ውዳሴ ማርያም" }
                        }
                        WudaseMariamScreen(
                            wudaseMezmurs = wudaseItems,
                            onSelectMezmur = { mezmur -> selectedMezmurIdMap[selectedTab] = mezmur.id },
                            listState = wudaseListState
                        )
                    }
                    AppNavTab.MEZMURAT -> {
                        MezmurListContent(
                            mezmurs = mezmurs,
                            listState = mezmuratListState,
                            selectedTab = selectedTab,
                            onSelectMezmur = { mezmur -> selectedMezmurIdMap[selectedTab] = mezmur.id },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                    AppNavTab.WELAYTGNA -> {
                        MezmurListContent(
                            mezmurs = mezmurs,
                            listState = welaytgnaListState,
                            selectedTab = selectedTab,
                            onSelectMezmur = { mezmur -> selectedMezmurIdMap[selectedTab] = mezmur.id },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                    AppNavTab.FAVORITES -> {
                        MezmurListContent(
                            mezmurs = mezmurs,
                            listState = favoritesListState,
                            selectedTab = selectedTab,
                            onSelectMezmur = { mezmur -> selectedMezmurIdMap[selectedTab] = mezmur.id },
                            onToggleFavorite = { viewModel.toggleFavorite(it) }
                        )
                    }
                }
            }
        }
    }
}

    // Add Mezmur Dialog
    if (showAddDialog) {
        AddMezmurDialog(
            categories = viewModel.categories,
            onDismiss = { viewModel.showAddMezmurDialog(false) },
            onConfirm = { title, artist, category, lyrics ->
                viewModel.addCustomMezmur(title, artist, category, lyrics)
            }
        )
    }
}

@Composable
fun MezmurListContent(
    mezmurs: List<MezmurEntity>,
    listState: androidx.compose.foundation.lazy.LazyListState,
    selectedTab: AppNavTab,
    onSelectMezmur: (MezmurEntity) -> Unit,
    onToggleFavorite: (MezmurEntity) -> Unit
) {
    if (mezmurs.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "⛪",
                    fontSize = 48.sp
                )
                Text(
                    text = if (selectedTab == AppNavTab.FAVORITES) "ምንም የተወደዱ መዝሙራት የሉም" else "ምንም መዝሙር አልተገኘም",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = ChurchTealDark
                )
                Text(
                    text = if (selectedTab == AppNavTab.FAVORITES)
                        "በመዝሙሩ ላይ የልብ ምልክቱን በመንካት ወደ ተወዳጆች ይጨምሩ::"
                    else
                        "ሌላ ርዕስ ይፈልጉ::",
                    fontSize = 14.sp,
                    color = TextSecondary,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    } else {
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            itemsIndexed(mezmurs, key = { _, mezmur -> mezmur.id }) { index, mezmur ->
                MezmurListItem(
                    mezmur = mezmur,
                    displayIndex = index + 1,
                    onClick = { onSelectMezmur(mezmur) },
                    onToggleFavorite = { onToggleFavorite(mezmur) }
                )
                Divider(
                    color = Color.SlateLight,
                    thickness = 0.8.dp,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}

val Color.Companion.SlateLight: Color
    get() = Color(0xFFE2E8F0)

@Composable
fun MezmurListItem(
    mezmur: MezmurEntity,
    displayIndex: Int = mezmur.numberInt,
    onClick: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
            .testTag("mezmur_item_${mezmur.id}"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.weight(1f)
        ) {
            // Number Badge
            Box(
                modifier = Modifier
                    .height(44.dp)
                    .widthIn(min = 44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(ChurchTealDark)
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$displayIndex",
                    fontWeight = FontWeight.Bold,
                    color = ChurchGold,
                    fontSize = 18.sp
                )
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = mezmur.title,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary,
                    fontSize = 16.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = mezmur.artist,
                        fontSize = 12.sp,
                        fontStyle = FontStyle.Italic,
                        color = TextSecondary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = " • ",
                        fontSize = 12.sp,
                        color = TextMuted
                    )
                    Text(
                        text = mezmur.category,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium,
                        color = ChurchTealPrimary
                    )
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.testTag("favorite_item_button_${mezmur.id}")
            ) {
                Icon(
                    imageVector = if (mezmur.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                    contentDescription = "Favorite toggle",
                    tint = if (mezmur.isFavorite) Color.Red else TextMuted,
                    modifier = Modifier.size(22.dp)
                )
            }
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "Open lyrics",
                tint = TextMuted,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun InfoScreenContent() {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // App Overview Header Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChurchTealDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ChurchGold),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("⛪", fontSize = 22.sp)
                    }
                    Text(
                        text = "መዝሙር ደብተር",
                        fontWeight = FontWeight.Bold,
                        color = ChurchGold,
                        fontSize = 22.sp
                    )
                }

                Text(
                    text = "የኢትዮጵያ ኦርቶዶክስ ተዋሕዶ ቤተክርስቲያን የመዝሙራትና የውዳሴ ማርያም ጸሎት ደብተር መተግበሪያ::",
                    color = Color.White.copy(alpha = 0.95f),
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }

        // About the App Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChurchSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "ስለ መተግበሪያው (About App)",
                    fontWeight = FontWeight.Bold,
                    color = ChurchTealDark,
                    fontSize = 16.sp
                )

                Text(
                    text = "ይህ መተግበሪያ ምእመናን በማንኛውም ጊዜና ቦታ ያለ ኢንተርኔት (100% Offline) የመዝሙራት ግጥሞችን እንዲሁም የዕለቱን የውዳሴ ማርያም ጸሎት በአማርኛ እና በግዕዝ ቋንቋዎች በቀላሉ አግኝተው እንዲጠቀሙ ታስቦ የተዘጋጀ ነው::",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 22.sp
                )

                Divider(color = ChurchSurfaceVariant)

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChurchGoldDark, modifier = Modifier.size(18.dp))
                        Text("ያለ ኢንተርኔት (100% Offline) የሚሰራ", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, tint = ChurchGoldDark, modifier = Modifier.size(18.dp))
                        Text("የውዳሴ ማርያም ጸሎት (በአማርኛ እና በግዕዝ)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Person, contentDescription = null, tint = ChurchGoldDark, modifier = Modifier.size(18.dp))
                        Text("አዘጋጅ: በረከት (Bereket)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(Icons.Default.Home, contentDescription = null, tint = ChurchGoldDark, modifier = Modifier.size(18.dp))
                        Text("የተዘጋጀው : በጉኑኖ ገ/ፅ/ቅ/ጊዮርጊስ ቤ/ክሪስቲያን የተሰራ", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }

        // Contact Us Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChurchSurface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = "እኛን ለማግኘት (Contact Us)",
                    fontWeight = FontWeight.Bold,
                    color = ChurchTealDark,
                    fontSize = 16.sp
                )

                Text(
                    text = "አስተያየት፣ ጥያቄ ወይም ተጨማሪ መዝሙራት ለማስገባት ከታች ያሉትን አዝራሮች በመጫን ያግኙን:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                // Telegram Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse("https://t.me/bereket_wmicheal")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            // Fallback
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF24A1DE), // Official Telegram Blue
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_telegram_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "Telegram", modifier = Modifier.size(20.dp), tint = Color.White)
                        Text("TELEGRAM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }

                // Email / Gmail Button
                Button(
                    onClick = {
                        try {
                            val intent = Intent(
                                Intent.ACTION_SENDTO,
                                Uri.parse("mailto:bereket.wmicheal.g@gmail.com")
                            )
                            context.startActivity(intent)
                        } catch (e: Exception) {
                            try {
                                val intent = Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("mailto:bereket.wmicheal.g@gmail.com")
                                )
                                context.startActivity(intent)
                            } catch (ex: Exception) {
                                // Fallback
                            }
                        }
                    },
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFDB4437), // Official Gmail Red
                        contentColor = Color.White
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("contact_email_button")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Email, contentDescription = "Gmail", modifier = Modifier.size(20.dp), tint = Color.White)
                        Text("GMAIL", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
