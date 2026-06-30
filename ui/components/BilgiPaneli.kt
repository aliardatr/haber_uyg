package com.example.haber_portali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

// Bilgi adımını temsil eden veri modeli
data class BilgiAdimi(
    val baslik: String,
    val aciklama: String
)

@Composable
fun BilgiPaneli(
    gosterilsinMi: Boolean,
    onKapat: () -> Unit
) {
    if (!gosterilsinMi) return

    val adimlar = listOf(
        BilgiAdimi("Yenilenen Arayüz", "Ögbili daha hızlı ve şık bir haber deneyimi sizi bekliyor."),
        BilgiAdimi("Güven Skoru", "Artık her haberin altında yapay zeka destekli doğruluk puanını görebilirsiniz."),
        BilgiAdimi("Hızlı Erişim", "En üstteki kategorilerle ilgi alanınıza saniyeler içinde ulaşın.")
    )

    var mevcutAdim by remember { mutableStateOf(0) }

    Dialog(onDismissRequest = onKapat) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFF1E1E1E),
            modifier = Modifier.fillMaxWidth().padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Başlık
                Text(
                    text = adimlar[mevcutAdim].baslik,
                    color = Color.White,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Açıklama
                Text(
                    text = adimlar[mevcutAdim].aciklama,
                    color = Color.LightGray,
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    lineHeight = 22.sp
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Butonlar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Atla Butonu
                    TextButton(onClick = onKapat) {
                        Text("Atla", color = Color.Gray)
                    }

                    // İlerleme Noktaları (Sayfa Göstergesi)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        adimlar.forEachIndexed { index, _ ->
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        color = if (index == mevcutAdim) Color(0xFF1976D2) else Color.DarkGray,
                                        shape = RoundedCornerShape(50)
                                    )
                            )
                        }
                    }

                    // Sonraki / Başla Butonu
                    Button(
                        onClick = {
                            if (mevcutAdim < adimlar.size - 1) {
                                mevcutAdim++
                            } else {
                                onKapat()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2))
                    ) {
                        Text(if (mevcutAdim == adimlar.size - 1) "Başla" else "Sonraki")
                    }
                }
            }
        }
    }
}