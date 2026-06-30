package com.example.haber_portali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.haber_portali.model.Haber
import com.example.haber_portali.ui.theme.UygMavisi
import androidx.compose.ui.viewinterop.AndroidView
import com.example.haber_portali.ui.screens.AkilliHaberMetni
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HaberDetayPaneli(haber: Haber, paneliKapat: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = { paneliKapat() },
        sheetState = sheetState,
        containerColor = Color(0xFF121212),
        dragHandle = null
    ) {
        val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
        val barBoslugu = statusBarHeight * 1.2f

        // 1. ANA KATMAN: İçerik ve Reklamı üst üste bindirmek için Box kullanıyoruz
        Box(modifier = Modifier.fillMaxSize()) {

            // 2. KAYDIRILABİLİR ALAN (Senin mevcut Column yapın)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = barBoslugu)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
            ) {
                // Özel Çubuk (Drag Handle)
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(UygMavisi)
                        .align(Alignment.CenterHorizontally)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 1. Ana Kapak Görseli
                AsyncImage(
                    model = haber.headerImage,
                    contentDescription = "Ana Kapak Görseli",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                        .clip(RoundedCornerShape(12.dp))
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Kategori ve Tarih
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = haber.categories.firstOrNull()?.uppercase() ?: "GÜNDEM",
                        color = Color(0xFF1976D2),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )


                    // 1. Adım: Haberin ham integer ID'sini otomatik olarak 10 haneye tamamlıyoruz
                    val formatliId = haber.id.toString().padStart(10, '0')

                    // 2. Adım: Bileşenin içindeki Text alanına tarihi ve formatlı ID'yi yan yana bağlıyoruz
                    Text(
                        text = "${haber.date} // $formatliId", // 👈 Çıktı: 2026-05-23 // 0000000019
                        color = Color.Gray,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 3. Haber Başlığı
                Text(
                    text = haber.title,
                    color = Color.White,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.ExtraBold,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Kaynak ve Güven Skoru
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(Color(0xFF1E1E1E), RoundedCornerShape(8.dp))
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = "Kaynak: ${haber.sources.joinToString(", ")}", color = Color.LightGray, fontSize = 13.sp)
                    Text(text = "Güven: %${haber.trustScore}", color = Color(0xFF2E7D32), fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 5. Haber Metni
                // YENİ: Bizim yazdığımız akıllı formatlayıcıyı çağırıyoruz!
                // Hata vermeyen, doğrudan net beyaz renk basan temiz entegrasyon:
                AkilliHaberMetni(
                    fullText = haber.content,
                    baseFontSize = 15,
                    defaultColor = Color(0xFFE0E0E0)
                )

                // KRİTİK: Reklam banner'ının içeriği kapatmaması için en alta boşluk ekliyoruz
                Spacer(modifier = Modifier.height(80.dp))
            }

            // 3. SABİT REKLAM ALANI (Column dışında olduğu için kaymaz)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .background(Color(0xFF1E1E1E))
                    .align(Alignment.BottomCenter),
                contentAlignment = Alignment.Center
            ) {
                // GOOGLE TEST BANNER REKLAMI
                AndroidView(
                    modifier = Modifier.fillMaxWidth(),
                    factory = { context ->
                        AdView(context).apply {
                            // Google'ın standart Test Banner ID'si
                            setAdSize(AdSize.BANNER)
                            adUnitId = "ca-app-pub-1338752009803690/8350841923"
                            loadAd(AdRequest.Builder().build())
                        }
                    }
                )
            }
        }
    }
}