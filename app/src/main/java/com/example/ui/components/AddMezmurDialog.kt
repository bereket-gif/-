package com.example.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchTealDark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddMezmurDialog(
    categories: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (title: String, artist: String, category: String, lyrics: String) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var artist by remember { mutableStateOf("") }
    var category by remember { mutableStateOf(categories.getOrElse(1) { "የንስሐ" }) }
    var lyrics by remember { mutableStateOf("") }
    var expandedCategory by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "አዲስ መዝሙር ጨምር",
                fontWeight = FontWeight.Bold,
                color = ChurchTealDark,
                fontSize = 20.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("የመዝሙር ርዕስ (Title)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_title_input"),
                    singleLine = true
                )

                OutlinedTextField(
                    value = artist,
                    onValueChange = { artist = it },
                    label = { Text("ዘማሪ / መዝሙር (Artist/Singer)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("add_artist_input"),
                    singleLine = true
                )

                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("ምድብ (Category)") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier
                            .menuAnchor()
                            .fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categories.filter { it != "ሁሉም" }.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat) },
                                onClick = {
                                    category = cat
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                OutlinedTextField(
                    value = lyrics,
                    onValueChange = { lyrics = it },
                    label = { Text("የመዝሙር ግጥም (Full Lyrics)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(140.dp)
                        .testTag("add_lyrics_input"),
                    maxLines = 10
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank() && lyrics.isNotBlank()) {
                        onConfirm(title, artist, category, lyrics)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = ChurchTealDark,
                    contentColor = ChurchGold
                ),
                enabled = title.isNotBlank() && lyrics.isNotBlank(),
                modifier = Modifier.testTag("save_mezmur_button")
            ) {
                Text("አስቀምጥ (Save)")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ተመለስ (Cancel)", color = ChurchTealDark)
            }
        },
        shape = RoundedCornerShape(16.dp)
    )
}
