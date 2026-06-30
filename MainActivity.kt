package com.example.haber_portali
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.haber_portali.ui.screens.AnaEkran
import com.example.haber_portali.ui.screens.OnboardingScreen
import com.example.haber_portali.ui.theme.HaberPortaliTheme
import com.google.android.gms.ads.MobileAds
import com.example.haber_portali.ui.screens.IzinYoneticisi
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Uygulama ayağa kalktığı an Google servislerini (AdMob) başlatıyor. (YAPI BOZULMADI)
        MobileAds.initialize(this) { initializationStatus ->
            // Reklamlar hazır olduğunda istersen buraya log yazılabilir.
        }
        // YENİ: SharedPreferences'tan kullanıcının uygulamaya ilk kez mi girdiğini kontrol ediyoruz.
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isOnboardingDone = sharedPref.getBoolean("onboarding_completed", false)
        setContent {
            HaberPortaliTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // YENİ: İzin Yoneticisi denetimi için durum (state) ve entegrasyonu
                    var showPermissionCheck by remember { mutableStateOf(true) }
                    if (showPermissionCheck) {
                        IzinYoneticisi {
                            showPermissionCheck = false
                        }
                    }
                    var showOnboarding by remember { mutableStateOf(!isOnboardingDone) }
                    // YENİ: Bildirime tıklanıp açıldıysa gelen verileri (Intent Extras) yakala!
                    val gelenKategori = intent.extras?.getString("hedef_kategori")

                    // Hem "haber_id" hem "hedef_haber_id" anahtarlarını, hem String hem Int olarak güvenli bir şekilde denetle:
                    val gelenHaberId = when {
                        intent.extras?.containsKey("haber_id") == true -> {
                            intent.extras?.get("haber_id")?.toString()
                        }
                        intent.extras?.containsKey("hedef_haber_id") == true -> {
                            intent.extras?.get("hedef_haber_id")?.toString()
                        }
                        else -> null
                    }

                    val gelenBildirimId = when {
                        intent.extras?.containsKey("bildirim_id") == true -> {
                            intent.extras?.get("bildirim_id")?.toString()
                        }
                        else -> null
                    }
                    // Bildirim tıklanma istatistiğini sunucuya raporla
                    androidx.compose.runtime.LaunchedEffect(gelenBildirimId) {
                        if (gelenBildirimId != null && gelenBildirimId.isNotEmpty()) {
                            try {
                                com.example.haber_portali.network.HaberApi.retrofitService.bildirimTiklanmaArtir(gelenBildirimId.toInt())
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    }
                    // Eğer Onboarding yapılmamışsa Karşılama Ekranını göster
                    // Eğer Onboarding yapılmamışsa Karşılama Ekranını göster
                    if (showOnboarding) {
                        // ESKİ popKategoriler = listOf(...) SATIRINI SİLİN!
                        OnboardingScreen(
                            onFinish = {
                                // Kullanıcı "Uygulamaya Başla" dediğinde tetiklenir, Ana Ekrana geçer.
                                showOnboarding = false
                            }
                        )
                    } else {
                        // Zaten seçim yapılmış, Ana Haber Akışını göster
                        AnaEkran(
                            baslangicKategori = gelenKategori,
                            baslangicHaberId = gelenHaberId
                        )
                    }
                }
            }
        }
    }
}
