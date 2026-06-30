package com.example.haber_portali.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Dün Color.kt dosyasında oluşturduğumuz özel renkleri buraya bağlıyoruz
private val KoyuTemaRenkleri = darkColorScheme(
    background = KoyuArkaplan,
    surface = KoyuKartArkaplan,
    primary = UygMavisi
)

private val AcikTemaRenkleri = lightColorScheme(
    background = AcikArkaplan,
    surface = AcikKartArkaplan,
    primary = UygMavisi
)

@Composable
fun HaberPortaliTheme(
    // ÇÖZÜM BURADA: Artık telefonun temasını dinlemiyoruz. Temel sistem hep Koyu (true) kalacak.
    // Geri kalan her şeyi kendi SettingsScreen'imiz yönetecek!
    karanlikMod: Boolean = true,
    content: @Composable () -> Unit
) {
    // Telefonun ayarlarına göre açık veya koyu renkleri seç
    val renkSemasi = if (karanlikMod) KoyuTemaRenkleri else AcikTemaRenkleri

    // Telefonun en üstündeki saat ve şarj simgelerinin olduğu şeridi (Status Bar) arkaplan rengimize uyarlıyoruz
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = renkSemasi.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !karanlikMod
        }
    }

    // Material 3 Motorunu bizim renklerimizle başlat
    MaterialTheme(
        colorScheme = renkSemasi,
        content = content
    )
}