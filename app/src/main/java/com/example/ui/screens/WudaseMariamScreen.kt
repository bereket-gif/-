package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import com.example.data.local.MezmurEntity
import com.example.ui.theme.*
import com.example.util.GeezUtil

enum class WudaseLanguage(val folderName: String, val displayName: String) {
    AMHARIC("amharic", "አማርኛ"),
    GEEZ("geez", "ግዕዝ")
}

@Composable
fun WudaseMariamScreen(
    wudaseMezmurs: List<MezmurEntity>,
    onSelectMezmur: (MezmurEntity) -> Unit,
    listState: LazyListState = rememberLazyListState()
) {
    var selectedLanguage by remember { mutableStateOf(WudaseLanguage.AMHARIC) }

    val numberRegex = remember { Regex("""^(\d+)""") }

    // Filter Wudase items based on selected language
    val displayList = remember(wudaseMezmurs, selectedLanguage) {
        val filtered = wudaseMezmurs.filter { mezmur ->
            if (selectedLanguage == WudaseLanguage.AMHARIC) {
                mezmur.artist.contains("አማርኛ") || mezmur.id.contains("amharic")
            } else {
                mezmur.artist.contains("ግዕዝ") || mezmur.id.contains("geez")
            }
        }

        val listToSort = if (filtered.isNotEmpty()) filtered else wudaseMezmurs

        listToSort.sortedWith(compareBy<MezmurEntity> { mezmur ->
            val match = numberRegex.find(mezmur.title.trim())
            match?.groupValues?.get(1)?.toIntOrNull() ?: Int.MAX_VALUE
        }.thenBy { it.title })
        .mapIndexed { index, mezmur ->
            mezmur.copy(
                numberInt = index + 1,
                numberGeez = GeezUtil.toGeezNumeral(index + 1)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(ChurchBackground)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Header
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = ChurchTealDark),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "ውዳሴ ማርያም (Wudase Mariam)",
                    fontWeight = FontWeight.Bold,
                    color = ChurchGold,
                    fontSize = 18.sp
                )

                Text(
                    text = "ቋንቋ ይምረጡ (Select Language):",
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )

                // Side-by-Side Language Toggle Buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black.copy(alpha = 0.2f))
                        .padding(4.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Amharic Button
                    Surface(
                        onClick = { selectedLanguage = WudaseLanguage.AMHARIC },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedLanguage == WudaseLanguage.AMHARIC) ChurchGold else Color.Transparent,
                        contentColor = if (selectedLanguage == WudaseLanguage.AMHARIC) ChurchTealDark else Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wudase_lang_amharic_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "በአማርኛ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }

                    // Geez Button
                    Surface(
                        onClick = { selectedLanguage = WudaseLanguage.GEEZ },
                        shape = RoundedCornerShape(10.dp),
                        color = if (selectedLanguage == WudaseLanguage.GEEZ) ChurchGold else Color.Transparent,
                        contentColor = if (selectedLanguage == WudaseLanguage.GEEZ) ChurchTealDark else Color.White,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("wudase_lang_geez_button")
                    ) {
                        Box(
                            modifier = Modifier.padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "በግዕዝ",
                                fontWeight = FontWeight.Bold,
                                fontSize = 14.sp
                            )
                        }
                    }
                }
            }
        }

        Text(
            text = "ውዳሴ ማርያም (${selectedLanguage.displayName}):",
            fontWeight = FontWeight.Bold,
            color = ChurchTealDark,
            fontSize = 15.sp
        )

        // Vertical List of Wudase Items (styled identical to Mezmur list)
        if (displayList.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "ምንም የውዳሴ ማርያም ጸሎት አልተገኘም",
                    color = TextSecondary,
                    fontSize = 14.sp
                )
            }
        } else {
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(displayList, key = { _, mezmur -> mezmur.id }) { index, mezmur ->
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = ChurchSurface),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectMezmur(mezmur) }
                            .testTag("wudase_list_item_${mezmur.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                // Number Badge styled like Lyrics button badge
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
                                        text = "${index + 1}",
                                        fontWeight = FontWeight.Bold,
                                        color = ChurchGold,
                                        fontSize = 18.sp
                                    )
                                }

                                Column {
                                    Text(
                                        text = mezmur.title,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 16.sp
                                    )
                                    Text(
                                        text = mezmur.artist,
                                        fontSize = 12.sp,
                                        color = TextSecondary
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "Open prayer",
                                tint = ChurchTealDark,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
