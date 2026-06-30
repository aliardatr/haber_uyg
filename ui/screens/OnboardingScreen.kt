package com.example.haber_portali.ui.screens

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.haber_portali.network.HaberApi
import com.example.haber_portali.network.IlgiAlanlariRequest
import kotlinx.coroutines.launch

@Composable
fun OnboardingScreen(
    onFinish: () -> Unit // Kategori listesi parametresini kaldırdık
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // YENİ: Sunucudan çekilecek kategorileri tutan liste
    var kategoriler by remember { mutableStateOf<List<String>>(emptyList()) }
    var secilenKategoriler by remember { mutableStateOf(setOf<String>()) }
    var isLoading by remember { mutableStateOf(false) }
    var isCategoriesLoading by remember { mutableStateOf(true) }

    // YENİ: Sayfa açılır açılmaz internetten kategorileri çekiyoruz
    LaunchedEffect(Unit) {
        try {
            val gelenler = HaberApi.retrofitService.getKategoriler()
            kategoriler = gelenler.kategoriler  // ÇÖZÜM BURADA
            isCategoriesLoading = false
        } catch (e: Exception) {
            e.printStackTrace()
            // Hata olursa varsayılan yedekleri yükle
            kategoriler = listOf("Gündem", "Teknoloji", "Borsa", "Spor", "Ekonomi", "Dünya")
            isCategoriesLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0E1013))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        Text(
            text = "Hoş Geldiniz! 🚀",
            color = Color.White,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Sizi daha iyi tanıyabilmemiz için ilgilendiğiniz haber kategorilerini seçin. Yalnızca seçtiğiniz alanlarda size özel bildirimler göndereceğiz.",
            color = Color(0xFF94A3B8),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(30.dp))

        // YENİ: Kategoriler yüklenirken dönecek daire
        if (isCategoriesLoading) {
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF64B5F6))
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(kategoriler) { kategori ->
                    val isSelected = secilenKategoriler.contains(kategori)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                if (isSelected) Color(0xFF1976D2).copy(alpha = 0.15f) else Color(0xFF1E222B),
                                RoundedCornerShape(12.dp)
                            )
                            .border(
                                1.dp,
                                if (isSelected) Color(0xFF64B5F6) else Color(0xFF2D323F),
                                RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                secilenKategoriler = if (isSelected) secilenKategoriler - kategori else secilenKategoriler + kategori
                            }
                            .padding(horizontal = 20.dp, vertical = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "# $kategori",
                            color = if (isSelected) Color(0xFF64B5F6) else Color(0xFFF8FAFC),
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                            fontSize = 16.sp
                        )

                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Seçildi",
                                tint = Color(0xFF64B5F6)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = {
                isLoading = true
                // 🚀 ÇÖZÜM: Her iki SharedPreferences dosyasını da net ve izole olarak ayırıyoruz
                val appPrefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                val haberPortalPrefs = context.getSharedPreferences("HaberPortalPrefs", Context.MODE_PRIVATE)

                coroutineScope.launch {
                    try {
                        var cihazId = appPrefs.getString("cihaz_id", null)
                        if (cihazId == null) {
                            cihazId = java.util.UUID.randomUUID().toString()
                            appPrefs.edit().putString("cihaz_id", cihazId).apply()
                        }

                        // 🚀 GÜNCELLEME 1: Seçilen kategorileri AnaEkran'ın (Yan panelin) okuduğu doğru dosyaya yazıyoruz
                        haberPortalPrefs.edit().putStringSet("secili_kategoriler", secilenKategoriler).apply()

                        val req = IlgiAlanlariRequest(secilenKategoriler.toList())
                        HaberApi.retrofitService.ilgiAlanlariKaydet(cihazId, req)

                        // 🚀 GÜNCELLEME 2: Onboarding durumunu AppPrefs'e tek işlemde yazıyoruz (Ezilme önlendi)
                        appPrefs.edit().putBoolean("onboarding_completed", true).apply()

                        isLoading = false
                        onFinish()

                    } catch (e: Exception) {
                        e.printStackTrace()
                        // 🚀 GÜNCELLEME 3: İnternet veya sunucu hatası olsa bile lokal hafıza kilitlenmesin diye burayı da sağlama alıyoruz
                        haberPortalPrefs.edit().putStringSet("secili_kategoriler", secilenKategoriler).apply()
                        appPrefs.edit().putBoolean("onboarding_completed", true).apply()
                        isLoading = false
                        onFinish()
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
            enabled = !isLoading
        ) {
            if (isLoading) {
                CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
            } else {
                Text(text = "UYGULAMAYA BAŞLA", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}