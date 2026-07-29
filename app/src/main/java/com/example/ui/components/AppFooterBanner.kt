package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.ChurchGold
import com.example.ui.theme.ChurchTealDark

@Composable
fun AppFooterBanner(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(ChurchTealDark)
            .padding(vertical = 6.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "በጉኑኖ ገ/ፅ/ቅ/ጊዮርጊስ ቤ/ክ የተሰራ @Bereket_wolde",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = ChurchGold,
            textAlign = TextAlign.Center
        )
    }
}
