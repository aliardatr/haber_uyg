package com.example.haber_portali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth // Bunu ekledik
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.max

@Composable
fun KategoriMenusu(seciliKategori: String, kategoriSecildi: (String) -> Unit) {
    val kategoriler = listOf("Tümü", "Gündem", "Teknoloji", "Bilim", "Finans", "Spor", "Dünya", "Otomobil")

    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    LazyRow(
        state = listState,
        // DOKUNUŞ BURADA: .background() modifier'ını tamamen sildik.
        // Onun yerine .fillMaxWidth() koyduk ki ekranı tam kaplasın ama arkaplanı şeffaf olsun (Ana Ekranın siyahını alsın)
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(kategoriler) { index, kategori ->
            val seciliMi = seciliKategori == kategori
            val interactionSource = remember { MutableInteractionSource() }

            Text(
                text = kategori,
                color = if (seciliMi) Color.White else Color.Gray,
                fontWeight = if (seciliMi) FontWeight.Bold else FontWeight.Normal,
                modifier = Modifier
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = {
                            kategoriSecildi(kategori)

                            coroutineScope.launch {
                                val hedefIndeks = maxOf(0, index - 1)
                                listState.animateScrollToItem(hedefIndeks)
                            }
                        }
                    )
                    .background(
                        color = if (seciliMi) Color(0xFF1976D2) else Color.Transparent,
                        shape = RoundedCornerShape(percent = 50)
                    )
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
    }
}