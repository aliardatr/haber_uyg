package com.example.haber_portali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerHaberKarti() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF121212))
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top
    ) {
        // Sol Taraf: Metin İskeletleri
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(end = 16.dp)
        ) {
            // Başlık satırı 1
            Box(modifier = Modifier.fillMaxWidth().height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEfekti())
            Spacer(modifier = Modifier.height(6.dp))
            // Başlık satırı 2
            Box(modifier = Modifier.fillMaxWidth(0.8f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEfekti())
            Spacer(modifier = Modifier.height(6.dp))
            // Başlık satırı 3
            Box(modifier = Modifier.fillMaxWidth(0.6f).height(16.dp).clip(RoundedCornerShape(4.dp)).shimmerEfekti())

            Spacer(modifier = Modifier.height(12.dp))

            // Alt satır (Kaynak ve Skor iskeleti)
            Box(modifier = Modifier.fillMaxWidth(0.4f).height(12.dp).clip(RoundedCornerShape(4.dp)).shimmerEfekti())
        }

        // Sağ Taraf: Görsel İskeleti
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(RoundedCornerShape(8.dp))
                .shimmerEfekti()
        )
    }
}

@Composable
fun ShimmerKareHaberKarti() {
    Box(
        modifier = Modifier
            .size(width = 280.dp, height = 200.dp) // Senin "KareHaberKarti" tasarımının boyutlarına göre burayı milimetrik ayarlayabilirsin
            .clip(RoundedCornerShape(16.dp))
            .shimmerEfekti() // İşte senin o efsanevi animasyon motorun burada devreye giriyor!
    )
}