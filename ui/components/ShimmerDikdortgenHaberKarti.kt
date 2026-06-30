package com.example.haber_portali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun ShimmerDikdortgenHaberKarti() {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        color = Color(0xFF15181F)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            // Sol taraftaki mini kapak görselinin Shimmer alanı
            Box(
                modifier = Modifier
                    .size(width = 110.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .shimmerEfekti()
            )

            Spacer(modifier = Modifier.width(12.dp))

            // Sağ taraftaki metin çizgilerinin Shimmer alanları
            Column(
                modifier = Modifier
                    .weight(1f)
                    .align(androidx.compose.ui.Alignment.CenterVertically)
            ) {
                // Başlık Çizgisi 1
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.9f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEfekti()
                )
                Spacer(modifier = Modifier.height(8.dp))

                // Başlık Çizgisi 2
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .height(16.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEfekti()
                )
                Spacer(modifier = Modifier.height(12.dp))

                // Kısa Özet Çizgisi
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.4f)
                        .height(12.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .shimmerEfekti()
                )
            }
        }
    }
}