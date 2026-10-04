package id.ulartangga.keluarga.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Placeholder visual untuk baris iklan (sample saja, belum terhubung ke SDK iklan sungguhan).
 * Nanti tinggal diganti isinya dengan AdView (misal Google AdMob) setelah ada akun & ad unit id.
 */
@Composable
fun AdBannerPlaceholder(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .background(Color(0xFFE0E0E0))
            .border(1.dp, Color(0xFFBDBDBD)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Contoh Ruang Iklan (Banner 6% Tinggi Layar)",
            color = Color(0xFF757575),
            fontSize = 10.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
