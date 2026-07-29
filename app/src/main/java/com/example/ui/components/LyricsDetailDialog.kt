package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.R
import com.example.data.local.MezmurEntity
import com.example.ui.theme.*

@Composable
fun LyricsDetailContent(
    mezmur: MezmurEntity,
    fontSizeSp: Float,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onFontSizeChange: (Float) -> Unit,
    onToggleFavorite: (MezmurEntity) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    Surface(
        modifier = Modifier
            .fillMaxSize()
            .testTag("lyrics_detail_content"),
        color = ChurchBackground
    ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Top Header Bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChurchTealDark)
                        .statusBarsPadding()
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("close_detail_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = ChurchGold
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f, fill = false)
                    ) {
                        Box(
                            modifier = Modifier
                                .height(36.dp)
                                .widthIn(min = 36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(ChurchGold)
                                .padding(horizontal = 8.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${mezmur.numberInt}",
                                fontWeight = FontWeight.Bold,
                                color = ChurchTealDark,
                                fontSize = 16.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = mezmur.title,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 18.sp,
                                maxLines = 1
                            )
                            Text(
                                text = mezmur.category,
                                fontSize = 12.sp,
                                color = ChurchGoldLight
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Copy Button moved to top corner
                        IconButton(
                            onClick = {
                                val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clip = ClipData.newPlainText("Mezmur Lyrics", "${mezmur.title}\n${mezmur.artist}\n\n${mezmur.lyrics}")
                                clipboard.setPrimaryClip(clip)
                                Toast.makeText(context, "ግጥሙ ተገልብጧል (Lyrics copied!)", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.testTag("copy_lyrics_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = "Copy Lyrics",
                                tint = ChurchGold
                            )
                        }

                        IconButton(
                            onClick = { onToggleFavorite(mezmur) },
                            modifier = Modifier.testTag("detail_favorite_button")
                        ) {
                            Icon(
                                imageVector = if (mezmur.isFavorite) Icons.Default.Favorite else Icons.Outlined.FavoriteBorder,
                                contentDescription = "Favorite",
                                tint = if (mezmur.isFavorite) Color.Red else ChurchGold
                            )
                        }
                    }
                }

                // Font Size Control Bar (Without slider or hide button)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(ChurchTealDarkContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FormatSize,
                                contentDescription = "Font size",
                                tint = ChurchGold,
                                modifier = Modifier.size(20.dp)
                            )
                            Text(
                                text = "የጽሑፍ መጠን: ${fontSizeSp.toInt()}sp",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            // Zoom Out button
                            FilledIconButton(
                                onClick = onZoomOut,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("zoom_out_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = ChurchTealDark,
                                    contentColor = ChurchGold
                                )
                            ) {
                                Text("A-", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }

                            // Reset button
                            TextButton(
                                onClick = { onFontSizeChange(18f) },
                                modifier = Modifier.testTag("reset_zoom_button")
                            ) {
                                Text("100%", color = ChurchGold, fontSize = 12.sp)
                            }

                            // Zoom In button
                            FilledIconButton(
                                onClick = onZoomIn,
                                modifier = Modifier
                                    .size(36.dp)
                                    .testTag("zoom_in_button"),
                                colors = IconButtonDefaults.filledIconButtonColors(
                                    containerColor = ChurchTealDark,
                                    contentColor = ChurchGold
                                )
                            ) {
                                Text("A+", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            }
                        }
                    }
                }

                // Main Lyrics Scroll Area (Full Screen Content)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .padding(12.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(ChurchSurface)
                        .border(1.dp, ChurchTealContainer, RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(20.dp)
                            .verticalScroll(rememberScrollState()),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Title Card inside Lyrics View
                        Text(
                            text = mezmur.title,
                            fontWeight = FontWeight.Bold,
                            fontSize = (fontSizeSp + 4f).sp,
                            color = ChurchTealDark,
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = mezmur.artist,
                            fontStyle = FontStyle.Italic,
                            fontSize = 14.sp,
                            color = TextSecondary,
                            textAlign = TextAlign.Center
                        )

                        HorizontalDivider(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .width(80.dp),
                            thickness = 2.dp,
                            color = ChurchGold
                        )

                        // Full Lyrics Text - sharp, high contrast and perfectly legible
                        Text(
                            text = mezmur.lyrics,
                            fontSize = fontSizeSp.sp,
                            color = TextPrimary,
                            fontWeight = FontWeight.Medium,
                            lineHeight = (fontSizeSp * 1.6f).sp,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("mezmur_lyrics_content")
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = "❖ ❖ ❖",
                            color = ChurchGoldDark,
                            fontSize = 18.sp,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // Spacer for spacing at the bottom
                Spacer(
                    modifier = Modifier
                        .navigationBarsPadding()
                        .height(8.dp)
                )
            }
        }
}

