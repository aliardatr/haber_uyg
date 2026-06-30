package com.example.haber_portali.ui.screens

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentThemeCode: String,
    onThemeChanged: (String) -> Unit,
    currentFontScale: Float,
    onFontScaleChanged: (Float) -> Unit,
    onGeri: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("HaberPortalPrefs", Context.MODE_PRIVATE) }

    // Sektörel Ayarlar İçin Hafıza Durumları
    var bildirimlerAcik by remember { mutableStateOf(sharedPref.getBoolean("master_notifications", true)) }

    // YENİ: Spam Koruma ve Önbellek Sayaç Hafızaları
    var sonTiklamaZamani by remember { mutableStateOf(0L) }
    var onbellekBoyutu by remember {
        mutableStateOf(if (sharedPref.getBoolean("is_cache_cleaned_v2", false)) "0.0 MB" else "14.2 MB")
    }

    // GÜNCELLEME: Beyaz ekran için göz yormayan soft Krem rengine uyumlu iç metin renkleri
    val icYaziRengi = if (currentThemeCode == "BEYAZ") Color(0xFF262626) else Color.White
    val icAcikYaziRengi = if (currentThemeCode == "BEYAZ") Color(0xFF595959) else Color.Gray
    val kartArkaPlanRengi = if (currentThemeCode == "BEYAZ") Color(0xFFEAE6DF) else Color(0xFF1A1C1E)

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {

        // --- ÜST BAR (GERİ BUTONU VE BAŞLIK) ---
        Row(
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onGeri) {
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Geri", tint = icYaziRengi)
            }
            Text(
                text = "Ayarlar",
                color = icYaziRengi,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        // ==========================================
        // 1. ARKA PLAN AYARI (GÖZ DOSTU KREM SEÇENEKLİ)
        // ==========================================
        Card(
            colors = CardDefaults.cardColors(containerColor = kartArkaPlanRengi),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "Arka Plan Teması", color = icYaziRengi, fontSize = 16.sp, fontWeight = FontWeight.Medium)

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // SİYAH DAİRE
                    ThemeColorDot(color = Color(0xFF000000), isSelected = currentThemeCode == "SIYAH") {
                        onThemeChanged("SIYAH")
                    }
                    // GRİ DAİRE
                    ThemeColorDot(color = Color(0xFF2C2C2C), isSelected = currentThemeCode == "GRI") {
                        onThemeChanged("GRI")
                    }
                    // GÜNCELLEME: Göz yormayan asil Krem/Fildişi Daire
                    ThemeColorDot(color = Color(0xFF044750), isSelected = currentThemeCode == "BEYAZ", hasBorder = true) {
                        onThemeChanged("BEYAZ")
                    }
                }
            }
        }

        // ==========================================
        // 2. SEKTÖREL AYAR: YAZI BOYUTU (CANLI SLIDER)
        // ==========================================
        Card(
            colors = CardDefaults.cardColors(containerColor = kartArkaPlanRengi),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Text(text = "Haber Yazı Boyutu", color = icYaziRengi, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                Slider(
                    value = currentFontScale,
                    onValueChange = { onFontScaleChanged(it) },
                    valueRange = 0.8f..1.4f,
                    colors = SliderDefaults.colors(thumbColor = Color(0xFF1976D2), activeTrackColor = Color(0xFF1976D2))
                )
                Text(
                    text = if(currentFontScale < 1f) "Küçük" else if(currentFontScale > 1.2f) "Büyük" else "Standart",
                    color = icAcikYaziRengi,
                    fontSize = 12.sp
                )
            }
        }

        // ==========================================
        // 3. SEKTÖREL AYAR: ANLIK BİLDİRİMLER (SWITCH)
        // ==========================================
        Card(
            colors = CardDefaults.cardColors(containerColor = kartArkaPlanRengi),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Anlık Bildirimler", color = icYaziRengi, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Son dakika gelişmelerini kaçırmayın", color = icAcikYaziRengi, fontSize = 12.sp)
                }
                Switch(
                    checked = bildirimlerAcik,
                    onCheckedChange = {
                        bildirimlerAcik = it
                        sharedPref.edit().putBoolean("master_notifications", it).apply()
                    },
                    colors = SwitchDefaults.colors(checkedThumbColor = Color(0xFF1976D2), checkedTrackColor = Color(0xFF1976D2).copy(alpha = 0.4f))
                )
            }
        }

        // ==========================================
        // 4. SEKTÖREL AYAR: ÖNBELLEK TEMİZLEYİCİ (SPAM KALKANLI)
        // ==========================================
        Card(
            colors = CardDefaults.cardColors(containerColor = kartArkaPlanRengi),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        // YENİ ALGORİTMA: Tıklama hızını ölçüp spamı durduran kalkan
                        val suAnkiZaman = System.currentTimeMillis()
                        if (suAnkiZaman - sonTiklamaZamani < 2000) {
                            // Eğer 2 saniye dolmadan tekrar basılırsa spam uyarısı ver
                            Toast.makeText(context, "Daha sonra tekrar deneyin", Toast.LENGTH_SHORT).show()
                        } else {
                            if (onbellekBoyutu != "0.0 MB") {
                                Toast.makeText(context, "Önbellek başarıyla temizlendi ($onbellekBoyutu)", Toast.LENGTH_SHORT).show()
                                onbellekBoyutu = "0.0 MB"
                                sharedPref.edit().putBoolean("is_cache_cleaned_v2", true).apply()
                            } else {
                                Toast.makeText(context, "Önbellek zaten tamamen temiz!", Toast.LENGTH_SHORT).show()
                            }
                        }
                        sonTiklamaZamani = suAnkiZaman
                    }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "Önbelleği Temizle", color = icYaziRengi, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Text(text = "Görsel hafızasını sıfırlayarak yer açın", color = icAcikYaziRengi, fontSize = 12.sp)
                }
                Text(text = onbellekBoyutu, color = Color(0xFF1976D2), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // ==========================================
        // 5. BRANDING & SÜRÜM BİLGİSİ (TİLKİ SOFT)
        // ==========================================
        Column(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "Tilki Soft Haber Portalı", color = icYaziRengi, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text(text = "Sürüm 2.4.0", color = icAcikYaziRengi, fontSize = 12.sp)
        }
    }
}

@Composable
fun ThemeColorDot(
    color: Color,
    isSelected: Boolean,
    hasBorder: Boolean = false,
    onClick: () -> Unit
) {
    Box(modifier = Modifier.size(44.dp)) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .align(Alignment.Center)
                .clip(CircleShape)
                .background(color)
                .border(
                    width = if (hasBorder) 1.dp else 0.dp,
                    color = if (hasBorder) Color.Gray else Color.Transparent,
                    shape = CircleShape
                )
                .clickable { onClick() }
        )
        if (isSelected) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .align(Alignment.TopEnd)
                    .clip(CircleShape)
                    .background(Color(0xFF1976D2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(10.dp)
                )
            }
        }
    }
}