package com.example.haber_portali.ui.screens

import android.app.Activity
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.Indicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.imageLoader
import com.example.haber_portali.model.Haber
import com.example.haber_portali.network.HaberApi
import com.example.haber_portali.ui.HaberViewModel
import com.example.haber_portali.ui.components.*
import com.google.android.gms.ads.*
import com.google.android.gms.ads.appopen.AppOpenAd
import com.google.android.gms.ads.nativead.MediaView
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdView
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// =====================================================================
// KURUMSAL TÜNEL: Detay paneli ve metin motorunun tıklama aksiyonuna
// ulaşabilmesi için CompositionLocal tanımı
// =====================================================================
val LocalHaberSecici = staticCompositionLocalOf<((Haber) -> Unit)?> { null }

// =====================================================================
// REKLAM KİMLİK YÖNETİM MERKEZİ
// =====================================================================
object ReklamKimlikleri {
    // Vitrin (yatay kart) reklam pozisyonları
    const val VITRIN_2_SIRA  = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
    const val VITRIN_7_SIRA  = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
    const val VITRIN_10_SIRA = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
    // Haber detayına girerken gösterilen ödüllü geçiş reklamı
    const val ODULLU_GECIS = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
    // 🚀 YENİ: Uygulama Arka Plandan Dönerken Gösterilecek Reklam ID'si
    const val APP_OPEN_REKLAM = "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"

    // Ana akış arası native reklamlar (3, 9, 15, 21... örüntüsünde)
    val AKIS_REKLAM_IDLERI = listOf(
        "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
         "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
         "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
         "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
         "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
         "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
         "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
         "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
        
    )
    // Makale içindeki *reklam* etiketi için max 3 adet havuz
    val IC_METIN_REKLAM_IDLERI = listOf(
        "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
        "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx",
        "ca-app-pub-xxxxxxxxxxxxxxxx/xxxxxxxxxx"
    )
}

// =====================================================================
// ANA EKRAN
// =====================================================================
@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
fun AnaEkran(
    viewModel: HaberViewModel = viewModel(),
    baslangicKategori: String? = null,
    baslangicHaberId: String? = null
) {
    val context    = LocalContext.current
    val activity   = context as? Activity
    val sharedPref = remember { context.getSharedPreferences("HaberPortalPrefs", Context.MODE_PRIVATE) }
    val isOnline by rememberNetworkConnectivity(context)
    val yukleniyorMu    = viewModel.yukleniyorMu
    val haberListesi    = viewModel.haberler
    val vitrinListesi   = viewModel.vitrinHaberleri
    var yerelRefreshDurumu by remember { mutableStateOf(false) }

    var currentLocalUrl by remember { mutableStateOf(sharedPref.getString("local_cover_url", "") ?: "") }
    var temaKodu by remember { mutableStateOf(sharedPref.getString("app_theme_code", "SIYAH") ?: "SIYAH") }
    var yaziBoyutuSkalasi by remember { mutableStateOf(sharedPref.getFloat("font_scale", 1f)) }
    var ayarlarAcik by remember { mutableStateOf(false) }
    val dinamikArkaPlanRengi = when (temaKodu) {
        "GRI"   -> Color(0xFF2C2C2C)
        "BEYAZ" -> Color(0xFF0A1D1C)
        else    -> Color(0xFF000000)
    }
    val sunucuKategorileri = viewModel.sunucuKategorileri.ifEmpty {
        listOf(
            "Teknoloji", "Bilim", "Gündem", "Finans", "Tarih", "Siyaset",
            "Yapay Zeka", "Galatasaray", "Fenerbahçe", "Beşiktaş", "Trabzonspor",
            "Resmi Gazete", "Borsa", "Kripto", "Girişim", "Otomobil"
        )
    }
    val nativeAdCache = remember { mutableStateMapOf<Int, NativeAd>() }
    val bannerAdCache = remember { mutableStateMapOf<Int, NativeAd>() }
    var rewardedInterstitialAd by remember { mutableStateOf<RewardedInterstitialAd?>(null) }
    var isAdLoading by remember { mutableStateOf(false) }

    // 🚀 ÇAKIŞMA ÖNLEYİCİ: Ekranda halihazırda tam ekran bir reklam varsa App Open Ad göstermeyi engeller
    var isTamEkranReklamGosteriliyor by remember { mutableStateOf(false) }

    val loadRewardedInterstitialAd = {
        if (!isAdLoading && rewardedInterstitialAd == null) {
            isAdLoading = true
            RewardedInterstitialAd.load(
                context,
                ReklamKimlikleri.ODULLU_GECIS,
                AdRequest.Builder().build(),
                object : RewardedInterstitialAdLoadCallback() {
                    override fun onAdLoaded(ad: RewardedInterstitialAd) { rewardedInterstitialAd = ad; isAdLoading = false }
                    override fun onAdFailedToLoad(error: LoadAdError)   { rewardedInterstitialAd = null; isAdLoading = false }
                }
            )
        }
    }
    var seciliHaber       by remember { mutableStateOf<Haber?>(null) }
    var tiklamaSayaci by remember { mutableStateOf(0) }
    var gecenSureSaniye by remember { mutableStateOf(0) }

    var seciliKategori by remember {
        mutableStateOf(if (baslangicKategori != null && baslangicKategori != "Tümü") baslangicKategori else "Tümü")
    }

    LaunchedEffect(baslangicHaberId) {
        if (baslangicHaberId != null && baslangicHaberId != "-1") {
            try {
                val haber = HaberApi.retrofitService.haberDetayGetir(baslangicHaberId.toInt())
                seciliHaber = haber
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    var bilgiPaneliGoster by remember { mutableStateOf(sharedPref.getBoolean("is_first_launch", true)) }
    val drawerState    = rememberDrawerState(DrawerValue.Closed)
    val coroutineScope = rememberCoroutineScope()

    // =====================================================================
    // 🚀 YENİ: APP OPEN AD (UYGULAMA AÇILIŞ REKLAMI) YÖNETİM MERKEZİ
    // =====================================================================
    var appOpenAd by remember { mutableStateOf<AppOpenAd?>(null) }
    var isAppOpenAdLoading by remember { mutableStateOf(false) }

    val loadAppOpenAd = {
        if (!isAppOpenAdLoading && appOpenAd == null && activity != null) {
            isAppOpenAdLoading = true
            val request = AdRequest.Builder().build()
            AppOpenAd.load(
                context,
                ReklamKimlikleri.APP_OPEN_REKLAM,
                request,
                object : AppOpenAd.AppOpenAdLoadCallback() {
                    override fun onAdLoaded(ad: AppOpenAd) {
                        appOpenAd = ad
                        isAppOpenAdLoading = false
                    }
                    override fun onAdFailedToLoad(error: LoadAdError) {
                        appOpenAd = null
                        isAppOpenAdLoading = false
                    }
                }
            )
        }
    }

    // Uygulama açılır açılmaz arka planda App Open reklamını hazırla
    LaunchedEffect(Unit) { loadAppOpenAd() }

    // Uygulamanın arka plandan ön plana dönüşünü izleyen gözlemci
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_START) {
                // Eğer hazırda bir reklam varsa VE ekranda başka tam ekran bir reklam (ödüllü vs) yoksa göster
                if (appOpenAd != null && !isTamEkranReklamGosteriliyor && activity != null) {
                    isTamEkranReklamGosteriliyor = true
                    appOpenAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                        override fun onAdDismissedFullScreenContent() {
                            appOpenAd = null
                            isTamEkranReklamGosteriliyor = false
                            loadAppOpenAd() // Kapanınca yenisini hazırla
                        }
                        override fun onAdFailedToShowFullScreenContent(error: AdError) {
                            appOpenAd = null
                            isTamEkranReklamGosteriliyor = false
                            loadAppOpenAd()
                        }
                    }
                    appOpenAd?.show(activity)
                } else if (appOpenAd == null && !isAppOpenAdLoading) {
                    loadAppOpenAd()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }
    // =====================================================================

    LaunchedEffect(isOnline) {
        if (isOnline) {
            coroutineScope.launch {
                try {
                    val serverConfig = HaberApi.retrofitService.getCoverConfig()
                    val serverUrl = serverConfig.active_cover_url
                    val localUrl = sharedPref.getString("local_cover_url", "")

                    if (serverUrl != localUrl) {
                        if (serverUrl.isEmpty()) {
                            sharedPref.edit().putString("local_cover_url", "").apply()
                            currentLocalUrl = ""
                        } else {
                            val request = coil.request.ImageRequest.Builder(context)
                                .data(serverUrl)
                                .build()
                            val result = context.imageLoader.execute(request)
                            if (result is coil.request.SuccessResult) {
                                sharedPref.edit().putString("local_cover_url", serverUrl).apply()
                                currentLocalUrl = serverUrl
                            }
                        }
                    }
                } catch (e: Exception) {
                    // Sessizce geç
                }
            }
        }
    }

    var seciliFiltreler by remember {
        mutableStateOf(sharedPref.getStringSet("secili_kategoriler", setOf()) ?: setOf())
    }

    LaunchedEffect(isOnline) {
        if (isOnline && haberListesi.isEmpty()) {
            viewModel.kategoriyeGoreGetir(seciliKategori, skip = 0)
        }
    }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000L)
            gecenSureSaniye++
            if (gecenSureSaniye == 150) {
                loadRewardedInterstitialAd()
            }
        }
    }
    LaunchedEffect(Unit) { loadRewardedInterstitialAd() }
    var sagPanelAcik by remember { mutableStateOf(false) }

    val habereTikla: (Haber) -> Unit = { haber ->
        coroutineScope.launch {
            try { HaberApi.retrofitService.haberTiklanmaArtir(haber.id) } catch (e: Exception) {}
        }

        tiklamaSayaci++

        if (tiklamaSayaci == 2) {
            loadRewardedInterstitialAd()
        }

        if (tiklamaSayaci >= 3 || gecenSureSaniye >= 180) {
            if (rewardedInterstitialAd != null && activity != null) {

                var odulKazanildi = false
                isTamEkranReklamGosteriliyor = true // 🚀 Ödüllü reklam açıldı, App Open engellenir

                rewardedInterstitialAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
                    override fun onAdDismissedFullScreenContent() {
                        isTamEkranReklamGosteriliyor = false // 🚀 Reklam kapandı, kilit açıldı
                        if (odulKazanildi) {
                            seciliHaber = haber
                            tiklamaSayaci = 0
                            gecenSureSaniye = 0
                        } else {
                            android.widget.Toast.makeText(context, "Haberi okumak için reklamı tamamlamalısınız.", android.widget.Toast.LENGTH_SHORT).show()
                        }
                        rewardedInterstitialAd = null
                    }
                    override fun onAdFailedToShowFullScreenContent(error: AdError) {
                        isTamEkranReklamGosteriliyor = false // 🚀 Hata oldu, kilit açıldı
                        seciliHaber = haber
                        tiklamaSayaci = 0
                        gecenSureSaniye = 0
                        rewardedInterstitialAd = null
                    }
                }

                rewardedInterstitialAd?.show(activity) { _ ->
                    odulKazanildi = true
                }
            } else {
                seciliHaber = haber
                loadRewardedInterstitialAd()
            }
        } else {
            seciliHaber = haber
        }
    }

    val mevcutDensity = LocalDensity.current
    val akilliYogunlukMotoru = remember(mevcutDensity, yaziBoyutuSkalasi) {
        androidx.compose.ui.unit.Density(
            density   = mevcutDensity.density,
            fontScale = mevcutDensity.fontScale * yaziBoyutuSkalasi
        )
    }
    val pullToRefreshState = rememberPullToRefreshState()

    CompositionLocalProvider(
        LocalDensity     provides akilliYogunlukMotoru,
        LocalHaberSecici provides habereTikla
    ) {
        ModalNavigationDrawer(
            drawerState   = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    drawerContainerColor = Color(0xFF121418),
                    modifier = Modifier.width(320.dp)
                ) {
                    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                        Text(
                            text     = "Bildirim Tercihleri",
                            color    = Color.White,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(vertical = 16.dp)
                        )
                        Text(
                            text     = "Hangi konularda bildirim almak istiyorsunuz?",
                            color    = Color.Gray,
                            fontSize = 14.sp,
                            modifier = Modifier.padding(bottom = 16.dp)
                        )
                        HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 1.dp, modifier = Modifier.padding(bottom = 16.dp))
                        FlowRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement   = Arrangement.spacedBy(12.dp)
                        ) {
                            sunucuKategorileri.forEach { kategori ->
                                val seciliMi      = seciliFiltreler.contains(kategori)
                                val bgRenk        = if (seciliMi) Color(0xFF1976D2).copy(alpha = 0.2f) else Color.Transparent
                                val yaziRenk      = if (seciliMi) Color(0xFF64B5F6) else Color(0xFFE0E0E0)
                                val cerceveRenk   = if (seciliMi) Color(0xFF1976D2).copy(alpha = 0.5f) else Color(0xFF424242)
                                val ikon          = if (seciliMi) Icons.Default.Check else Icons.Default.Add
                                Row(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(20.dp))
                                        .background(bgRenk)
                                        .border(1.dp, cerceveRenk, RoundedCornerShape(20.dp))
                                        .clickable {
                                            val yeniFiltreler = if (seciliMi) seciliFiltreler - kategori else seciliFiltreler + kategori
                                            seciliFiltreler = yeniFiltreler
                                            sharedPref.edit().putStringSet("secili_kategoriler", yeniFiltreler).apply()

                                            coroutineScope.launch {
                                                try {
                                                    val appPref = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
                                                    var cihazId = appPref.getString("cihaz_id", null) ?: sharedPref.getString("cihaz_id", null)
                                                    if (cihazId == null) {
                                                        cihazId = java.util.UUID.randomUUID().toString()
                                                        appPref.edit().putString("cihaz_id", cihazId).apply()
                                                    }
                                                    val req = com.example.haber_portali.network.IlgiAlanlariRequest(yeniFiltreler.toList())
                                                    HaberApi.retrofitService.ilgiAlanlariKaydet(cihazId, req)
                                                } catch (e: Exception) {
                                                    e.printStackTrace()
                                                }
                                            }
                                        }
                                        .padding(horizontal = 14.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = kategori, color = yaziRenk, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                                    Spacer(Modifier.width(6.dp))
                                    Icon(imageVector = ikon, contentDescription = null, tint = yaziRenk, modifier = Modifier.size(16.dp))
                                }
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                containerColor = if (currentLocalUrl.isNotEmpty()) Color.Transparent else dinamikArkaPlanRengi
            ) { paddingValues ->
                Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

                    if (currentLocalUrl.isNotEmpty()) {
                        AsyncImage(
                            model = currentLocalUrl,
                            contentDescription = "Özel Gün Arka Planı",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                        Box(modifier = Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.7f)))
                    }

                    Column(modifier = Modifier.fillMaxSize()) {
                        Row(
                            modifier          = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { coroutineScope.launch { drawerState.open() } }) {
                                Icon(Icons.Default.Menu, contentDescription = "Menü", tint = Color.White)
                            }
                            val ustKategoriListesi = listOf("Tümü") + sunucuKategorileri
                            LazyRow(
                                modifier            = Modifier.weight(1f),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                contentPadding      = PaddingValues(horizontal = 8.dp)
                            ) {
                                items(ustKategoriListesi) { kat ->
                                    val isSelected = seciliKategori == kat
                                    Text(
                                        text       = kat,
                                        color      = if (isSelected) Color.White else Color.Gray,
                                        fontSize   = 14.sp,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        modifier   = Modifier
                                            .clip(RoundedCornerShape(16.dp))
                                            .background(if (isSelected) Color(0xFF1976D2) else Color.Transparent)
                                            .clickable {
                                                seciliKategori = kat
                                                viewModel.kategoriyeGoreGetir(kat, skip = 0)
                                            }
                                            .padding(horizontal = 14.dp, vertical = 6.dp)
                                    )
                                }
                            }
                            IconButton(onClick = { sagPanelAcik = true }) {
                                Icon(Icons.Default.MoreVert, contentDescription = "Seçenekler", tint = Color.White)
                            }
                        }

                        AnimatedVisibility(!isOnline, enter = expandVertically() + fadeIn(), exit = shrinkVertically() + fadeOut()) {
                            Row(
                                modifier              = Modifier.fillMaxWidth().background(Color(0xFF252525)).border(1.dp, Color(0xFF424242)).padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("İnternet bağlantınız kesildi. Yenilemek için aşağı kaydırın.", color = Color(0xFFE0E0E0), fontSize = 13.sp)
                            }
                        }

                        AnimatedVisibility(
                            visible = isOnline && !yukleniyorMu && !yerelRefreshDurumu && haberListesi.isEmpty(),
                            enter   = expandVertically() + fadeIn(),
                            exit    = shrinkVertically() + fadeOut()
                        ) {
                            Row(
                                modifier              = Modifier.fillMaxWidth().background(Color(0xFF252525)).border(1.dp, Color(0xFF424242)).padding(vertical = 8.dp, horizontal = 12.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment     = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.Info, contentDescription = null, tint = Color(0xFFE53935), modifier = Modifier.size(16.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("Sunucuya ulaşılamıyor. Yenilemek için aşağı kaydırın.", color = Color(0xFFE0E0E0), fontSize = 13.sp)
                            }
                        }

                        PullToRefreshBox(
                            isRefreshing = yerelRefreshDurumu,
                            onRefresh    = {
                                coroutineScope.launch {
                                    yerelRefreshDurumu = true
                                    try {
                                        viewModel.kategoriyeGoreGetir(seciliKategori, skip = 0)
                                        delay(800L)
                                    } finally {
                                        yerelRefreshDurumu = false
                                    }
                                }
                            },
                            state    = pullToRefreshState,
                            modifier = Modifier.fillMaxSize(),
                            indicator = {
                                Indicator(
                                    modifier       = Modifier.align(Alignment.TopCenter),
                                    isRefreshing   = yerelRefreshDurumu,
                                    state          = pullToRefreshState,
                                    containerColor = Color(0xFF15181F),
                                    color          = Color(0xFF1976D2)
                                )
                            }
                        ) {
                            LazyColumn(modifier = Modifier.fillMaxSize()) {

                                if ((yukleniyorMu || yerelRefreshDurumu) && vitrinListesi.isEmpty() && haberListesi.isEmpty()) {
                                    item {
                                        Text(
                                            text     = "Son 24 Saatin En İyileri",
                                            color    = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                        LazyRow(
                                            contentPadding        = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier              = Modifier.padding(bottom = 16.dp)
                                        ) {
                                            items(3) { ShimmerKareHaberKarti() }
                                        }
                                    }
                                } else if (vitrinListesi.isNotEmpty()) {
                                    item {
                                        Text(
                                            text     = "Son 24 Saatin En İyileri",
                                            color    = Color.White,
                                            fontSize = 18.sp,
                                            fontWeight = FontWeight.ExtraBold,
                                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                        )
                                        LazyRow(
                                            contentPadding        = PaddingValues(horizontal = 16.dp),
                                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                                            modifier              = Modifier.padding(bottom = 16.dp)
                                        ) {
                                            itemsIndexed(vitrinListesi) { index, haber ->
                                                KareHaberKarti(haber = haber, onClick = { habereTikla(haber) })
                                                when (index + 1) {
                                                    1 -> KareReklamKarti(adId = ReklamKimlikleri.VITRIN_2_SIRA,  cache = nativeAdCache, position = 1)
                                                    6 -> KareReklamKarti(adId = ReklamKimlikleri.VITRIN_7_SIRA,  cache = nativeAdCache, position = 6)
                                                    9 -> KareReklamKarti(adId = ReklamKimlikleri.VITRIN_10_SIRA, cache = nativeAdCache, position = 9)
                                                }
                                            }
                                        }
                                    }
                                }

                                if ((yukleniyorMu || yerelRefreshDurumu) && haberListesi.isEmpty()) {
                                    items(6) { ShimmerHaberKarti() }
                                } else {
                                    itemsIndexed(haberListesi) { index, haber ->
                                        StandartHaberKarti(haber = haber, onClick = { habereTikla(haber) })
                                        if ((index + 1) % 6 == 3) {
                                            val reklamIndeksi   = index / 6
                                            val gecerliReklamId = ReklamKimlikleri.AKIS_REKLAM_IDLERI[reklamIndeksi % ReklamKimlikleri.AKIS_REKLAM_IDLERI.size]
                                            ReklamAlani(adId = gecerliReklamId, cache = bannerAdCache, position = index)
                                        }
                                        if (
                                            index == haberListesi.size - 2 &&
                                            !viewModel.isPaginating        &&
                                            viewModel.canLoadMore          &&
                                            haberListesi.size < 50
                                        ) {
                                            LaunchedEffect(index) {
                                                viewModel.loadMore(seciliKategori)
                                            }
                                        }
                                    }
                                    if (viewModel.isPaginating && haberListesi.size < 50) {
                                        items(3) { ShimmerHaberKarti() }
                                    }
                                    if ((!viewModel.canLoadMore || haberListesi.size >= 50) && haberListesi.isNotEmpty()) {
                                        item {
                                            Text(
                                                text      = "Bu kategoride gösterilecek başka içerik kalmadı.",
                                                color     = Color.Gray,
                                                fontSize  = 13.sp,
                                                textAlign = TextAlign.Center,
                                                modifier  = Modifier.fillMaxWidth().padding(vertical = 24.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    AnimatedVisibility(
                        visible  = sagPanelAcik,
                        enter    = fadeIn(),
                        exit     = fadeOut(),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = 0.5f))
                                .clickable { sagPanelAcik = false }
                        )
                    }

                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.CenterEnd) {
                        AnimatedVisibility(
                            visible = sagPanelAcik,
                            enter   = slideInHorizontally(initialOffsetX = { it }),
                            exit    = slideOutHorizontally(targetOffsetX = { it })
                        ) {
                            Surface(
                                modifier = Modifier.fillMaxHeight().width(280.dp).clickable(enabled = false) {},
                                color    = Color(0xFF121418)
                            ) {
                                Column(modifier = Modifier.fillMaxSize().padding(vertical = 24.dp, horizontal = 16.dp)) {
                                    SagPanelMenuElemani(text = "İlgilenebileceğiniz", isRed = true, onClick = { /* TODO */ })
                                    Spacer(Modifier.height(8.dp))
                                    HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                    SagPanelMenuElemani(text = "Kaydedilenler", onClick = { /* TODO */ })
                                    SagPanelMenuElemani(text = "Ayarlar", onClick = { sagPanelAcik = false; ayarlarAcik = true })
                                    Spacer(Modifier.weight(1f))
                                    HorizontalDivider(color = Color(0xFF2C2C2C), thickness = 1.dp, modifier = Modifier.padding(bottom = 8.dp))
                                    SagPanelMenuElemani(text = "Bizi Değerlendirin", onClick = { /* TODO */ })
                                }
                            }
                        }
                    }

                    BilgiPaneli(
                        gosterilsinMi = bilgiPaneliGoster,
                        onKapat       = {
                            bilgiPaneliGoster = false
                            sharedPref.edit().putBoolean("is_first_launch", false).apply()
                        }
                    )
                    seciliHaber?.let { haber ->
                        HaberDetayPaneli(haber = haber, paneliKapat = { seciliHaber = null })
                    }
                    AnimatedVisibility(
                        visible = ayarlarAcik,
                        enter   = slideInHorizontally(initialOffsetX = { it }) + fadeIn(),
                        exit    = slideOutHorizontally(targetOffsetX = { it }) + fadeOut()
                    ) {
                        Box(modifier = Modifier.fillMaxSize().background(dinamikArkaPlanRengi)) {
                            SettingsScreen(
                                currentThemeCode  = temaKodu,
                                onThemeChanged    = { yeniTema  -> temaKodu = yeniTema;  sharedPref.edit().putString("app_theme_code", yeniTema).apply() },
                                currentFontScale  = yaziBoyutuSkalasi,
                                onFontScaleChanged = { yeniSkala -> yaziBoyutuSkalasi = yeniSkala; sharedPref.edit().putFloat("font_scale", yeniSkala).apply() },
                                onGeri            = { ayarlarAcik = false }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun SagPanelMenuElemani(text: String, isRed: Boolean = false, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(vertical = 14.dp, horizontal = 8.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = text, color = if (isRed) Color(0xFFE53935) else Color.White, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = if (isRed) Color(0xFFE53935) else Color.Gray, modifier = Modifier.size(20.dp))
    }
}

@Composable
fun KareReklamKarti(adId: String, cache: MutableMap<Int, NativeAd>, position: Int) {
    Surface(
        modifier = Modifier.size(width = 340.dp, height = 320.dp),
        shape    = RoundedCornerShape(16.dp),
        color    = Color(0xFF1A1A1A)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Text(
                text     = "SPONSORLU",
                color    = Color(0xFF1976D2),
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp)
            )
            AndroidView(
                modifier = Modifier.fillMaxSize().background(Color(0xFF252525)),
                factory  = { ctx ->
                    val nativeAdView   = NativeAdView(ctx)
                    val parentLayout   = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = ViewGroup.LayoutParams(-1, -1) }
                    val mediaView      = MediaView(ctx).apply { layoutParams = LinearLayout.LayoutParams(-1, 600) }
                    val headlineView   = TextView(ctx).apply { setTextColor(android.graphics.Color.WHITE); textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(40, 16, 40, 8); maxLines = 2 }
                    val callToAction   = Button(ctx).apply  { setBackgroundColor(android.graphics.Color.parseColor("#1976D2")); setTextColor(android.graphics.Color.WHITE) }
                    nativeAdView.mediaView       = mediaView
                    nativeAdView.headlineView    = headlineView
                    nativeAdView.callToActionView = callToAction
                    parentLayout.addView(mediaView)
                    parentLayout.addView(headlineView)
                    parentLayout.addView(callToAction)
                    nativeAdView.addView(parentLayout)
                    cache[position]?.let { bindNativeAd(it, nativeAdView) } ?: AdLoader.Builder(ctx, adId).forNativeAd { ad -> cache[position] = ad; bindNativeAd(ad, nativeAdView) }.build().loadAd(AdRequest.Builder().build())
                    nativeAdView
                }
            )
        }
    }
}

@Composable
fun ReklamAlani(adId: String, cache: MutableMap<Int, NativeAd>, position: Int) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Surface(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = Color.Transparent) {
            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Text("SPONSORLU İÇERİK", color = Color.Gray, fontSize = 9.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                AndroidView(
                    modifier = Modifier.fillMaxWidth().height(280.dp).padding(horizontal = 16.dp).background(Color(0xFF1E1E1E), shape = RoundedCornerShape(12.dp)),
                    factory  = { ctx ->
                        val nativeAdView = NativeAdView(ctx)
                        val parentLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = ViewGroup.LayoutParams(-1, -1) }
                        val mediaView    = MediaView(ctx).apply { layoutParams = LinearLayout.LayoutParams(-1, 500) }
                        val headlineView = TextView(ctx).apply { setTextColor(android.graphics.Color.WHITE); textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(32, 16, 32, 8); maxLines = 2 }
                        val callToAction = Button(ctx).apply  { setBackgroundColor(android.graphics.Color.parseColor("#1976D2")); setTextColor(android.graphics.Color.WHITE) }
                        nativeAdView.mediaView        = mediaView
                        nativeAdView.headlineView     = headlineView
                        nativeAdView.callToActionView = callToAction
                        parentLayout.addView(mediaView)
                        parentLayout.addView(headlineView)
                        parentLayout.addView(callToAction)
                        nativeAdView.addView(parentLayout)
                        cache[position]?.let { bindNativeAd(it, nativeAdView) } ?: AdLoader.Builder(ctx, adId).forNativeAd { ad -> cache[position] = ad; bindNativeAd(ad, nativeAdView) }.build().loadAd(AdRequest.Builder().build())
                        nativeAdView
                    }
                )
            }
        }
        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp), thickness = 1.dp, color = Color(0xFF2C2C2C))
    }
}

@Composable
fun IcMetinReklamAlani(adId: String, cache: MutableMap<Int, NativeAd>, position: Int) {
    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
        Surface(
            modifier = Modifier.size(width = 340.dp, height = 320.dp),
            shape    = RoundedCornerShape(16.dp),
            color    = Color(0xFF1A1A1A)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                Text("SPONSORLU İÇERİK", color = Color(0xFF1976D2), fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(start = 16.dp, top = 12.dp, bottom = 4.dp))
                AndroidView(
                    modifier = Modifier.fillMaxSize().background(Color(0xFF252525)),
                    factory  = { ctx ->
                        val nativeAdView = NativeAdView(ctx)
                        val parentLayout = LinearLayout(ctx).apply { orientation = LinearLayout.VERTICAL; layoutParams = ViewGroup.LayoutParams(-1, -1) }
                        val mediaView    = MediaView(ctx).apply { layoutParams = LinearLayout.LayoutParams(-1, 600) }
                        val headlineView = TextView(ctx).apply { setTextColor(android.graphics.Color.WHITE); textSize = 16f; setTypeface(null, android.graphics.Typeface.BOLD); setPadding(40, 16, 40, 8); maxLines = 2 }
                        val callToAction = Button(ctx).apply  { setBackgroundColor(android.graphics.Color.parseColor("#1976D2")); setTextColor(android.graphics.Color.WHITE) }
                        nativeAdView.mediaView        = mediaView
                        nativeAdView.headlineView     = headlineView
                        nativeAdView.callToActionView = callToAction
                        parentLayout.addView(mediaView)
                        parentLayout.addView(headlineView)
                        parentLayout.addView(callToAction)
                        nativeAdView.addView(parentLayout)
                        cache[position]?.let { bindNativeAd(it, nativeAdView) } ?: AdLoader.Builder(ctx, adId).forNativeAd { ad -> cache[position] = ad; bindNativeAd(ad, nativeAdView) }.build().loadAd(AdRequest.Builder().build())
                        nativeAdView
                    }
                )
            }
        }
    }
}

@Composable
fun OnerilenIcerikKarti(haber: Haber, onClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF15181F))
            .border(1.dp, Color(0xFF1976D2).copy(alpha = 0.25f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.background(Color(0xFF1976D2).copy(alpha = 0.15f), RoundedCornerShape(4.dp)).padding(horizontal = 8.dp, vertical = 4.dp)) {
                Text("ÖNERİLEN İÇERİK", color = Color(0xFF64B5F6), fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF64B5F6), modifier = Modifier.size(18.dp))
        }
        Spacer(Modifier.height(12.dp))
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = haber.title, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Bold, maxLines = 2, lineHeight = 20.sp)
                if (!haber.feedSummary.isNullOrBlank()) {
                    Spacer(Modifier.height(6.dp))
                    Text(text = haber.feedSummary, color = Color.Gray, fontSize = 13.sp, lineHeight = 18.sp)
                }
            }
            if (!haber.headerImage.isNullOrBlank()) {
                AsyncImage(model = haber.headerImage, contentDescription = null, modifier = Modifier.size(width = 80.dp, height = 60.dp).clip(RoundedCornerShape(8.dp)).background(Color(0xFF1E222B)), contentScale = ContentScale.Crop)
            }
        }
    }
}

@Composable
fun OnerilenIcerikPlaceholder(targetId: Int) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF15181F))
            .border(1.dp, Color(0xFF334155).copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .padding(16.dp)
    ) {
        Text("ÖNERİLEN İÇERİK (ID: #$targetId)", color = Color.Gray, fontSize = 11.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
        Spacer(Modifier.height(6.dp))
        Text("İlgili içerik yükleniyor veya şu an mevcut değil.", color = Color(0xFF64748B), fontSize = 14.sp, fontStyle = FontStyle.Italic)
    }
}

@Composable
fun AkilliHaberMetni(
    fullText      : String,
    modifier      : Modifier        = Modifier,
    baseFontSize  : Int             = 15,
    defaultColor  : Color           = Color.White,
    tumHaberler   : List<Haber>     = emptyList(),
    onHaberClick  : ((Haber) -> Unit)? = null
) {
    val satirlar = remember(fullText) {
        var metin = fullText
        metin = metin.replace("(\\*önerileniçerik=\\d+\\*)".toRegex(), "\n$1\n")
        metin = metin.replace("(\\*reklam\\*)".toRegex(), "\n$1\n")
        metin.split("\n")
    }
    val icMetinAdCache = remember { mutableStateMapOf<Int, NativeAd>() }
    var reklamSayaci  = 0
    val paylasilanViewModel  = viewModel<HaberViewModel>()
    val nihaiHaberHavuzu     = if (tumHaberler.isEmpty()) paylasilanViewModel.haberler else tumHaberler
    val tunelHaberSecici     = LocalHaberSecici.current
    val nihaiTiklamaAksiyonu = onHaberClick ?: tunelHaberSecici

    Column(modifier = modifier.fillMaxWidth()) {
        satirlar.forEach { satir ->
            if (satir.isBlank()) {
                Spacer(Modifier.height(8.dp))
                return@forEach
            }
            val temizSatir    = satir.trim()
            val onerilenMatch = "^\\*önerileniçerik=(\\d+)\\*$".toRegex().find(temizSatir)
            when {
                temizSatir == "*reklam*" -> {
                    if (reklamSayaci < 3) {
                        Spacer(Modifier.height(14.dp))
                        IcMetinReklamAlani(
                            adId     = ReklamKimlikleri.IC_METIN_REKLAM_IDLERI[reklamSayaci],
                            cache    = icMetinAdCache,
                            position = reklamSayaci
                        )
                        Spacer(Modifier.height(14.dp))
                        reklamSayaci++
                    }
                }
                onerilenMatch != null -> {
                    val targetId = onerilenMatch.groupValues[1].toIntOrNull()
                    if (targetId != null) {
                        Spacer(Modifier.height(14.dp))
                        var bulunanHaber by remember(targetId) {
                            mutableStateOf(nihaiHaberHavuzu.find { it.id == targetId })
                        }
                        var isLoading by remember(targetId) {
                            mutableStateOf(bulunanHaber == null)
                        }
                        LaunchedEffect(targetId) {
                            if (bulunanHaber == null) {
                                try {
                                    isLoading    = true
                                    bulunanHaber = HaberApi.retrofitService.haberDetayGetir(targetId)
                                } catch (_: Exception) {
                                } finally {
                                    isLoading = false
                                }
                            }
                        }
                        when {
                            isLoading          -> OnerilenIcerikPlaceholder(targetId)
                            bulunanHaber != null -> OnerilenIcerikKarti(
                                haber   = bulunanHaber!!,
                                onClick = { nihaiTiklamaAksiyonu?.invoke(bulunanHaber!!) }
                            )
                            else               -> OnerilenIcerikPlaceholder(targetId)
                        }
                        Spacer(Modifier.height(14.dp))
                    }
                }
                else -> {
                    val isFullLinePp2     = temizSatir.startsWith("pp2 ") && temizSatir.endsWith(" pp2")
                    val isFullLinePp1     = (temizSatir.startsWith("pp1 ") && temizSatir.endsWith(" pp1")) || (temizSatir.startsWith("*") && temizSatir.endsWith("*") && !temizSatir.startsWith("**"))
                    val isFullLineBold    = (temizSatir.startsWith("***") && temizSatir.endsWith("***")) || (temizSatir.startsWith("**") && temizSatir.endsWith("**"))
                    val isFullLineUnderline = temizSatir.startsWith("ccc ") && temizSatir.endsWith(" ccc")

                    when {
                        isFullLinePp2 -> {
                            val metin = temizSatir.removePrefix("pp2 ").removeSuffix(" pp2")
                            val annotated = buildInlineAnnotatedString(metin, baseFontSize * 3, Color(0xFF1976D2))
                            Text(text = annotated, fontSize = (baseFontSize * 3).sp, fontWeight = FontWeight.ExtraBold, modifier = Modifier.padding(vertical = 4.dp))
                        }
                        isFullLineBold -> {
                            val metin = temizSatir.removePrefix("***").removeSuffix("***").removePrefix("**").removeSuffix("**")
                            val annotated = buildInlineAnnotatedString(metin, (baseFontSize * 1.1f).toInt(), defaultColor)
                            Text(text = annotated, fontSize = (baseFontSize * 1.1f).sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(vertical = 2.dp))
                        }
                        isFullLinePp1 -> {
                            val metin = temizSatir.removePrefix("pp1 ").removeSuffix(" pp1").removePrefix("*").removeSuffix("*")
                            val annotated = buildInlineAnnotatedString(metin, baseFontSize, defaultColor)
                            Text(text = annotated, fontSize = baseFontSize.sp, fontStyle = FontStyle.Italic, modifier = Modifier.padding(vertical = 2.dp))
                        }
                        isFullLineUnderline -> {
                            val metin = temizSatir.removePrefix("ccc ").removeSuffix(" ccc")
                            val annotated = buildInlineAnnotatedString(metin, baseFontSize, defaultColor)
                            Text(text = annotated, fontSize = baseFontSize.sp, textDecoration = TextDecoration.Underline, modifier = Modifier.padding(vertical = 2.dp))
                        }
                        else -> {
                            val annotated = buildInlineAnnotatedString(temizSatir, baseFontSize, defaultColor)
                            Text(text = annotated, fontSize = baseFontSize.sp, lineHeight = (baseFontSize * 1.6).sp, modifier = Modifier.padding(vertical = 3.dp))
                        }
                    }
                }
            }
        }
    }
}

private fun buildInlineAnnotatedString(text: String, baseFontSize: Int, defaultColor: Color): AnnotatedString {
    return buildAnnotatedString {
        var remaining = text
        while (remaining.isNotEmpty()) {
            val idxBoldTriple = remaining.indexOf("***")
            val idxBoldDouble = remaining.indexOf("**")
            val idxPp2        = remaining.indexOf("pp2")
            val idxPp1        = remaining.indexOf("pp1")
            val idxCcc        = remaining.indexOf("ccc")
            val idxItalicStar = remaining.indexOf("*")

            val matches = mutableListOf<Pair<String, Int>>()
            if (idxBoldTriple != -1) matches.add(Pair("***", idxBoldTriple))
            if (idxBoldDouble != -1 && idxBoldTriple != idxBoldDouble) {
                matches.add(Pair("**", idxBoldDouble))
            }
            if (idxPp2 != -1) matches.add(Pair("pp2", idxPp2))
            if (idxPp1 != -1 && idxPp2 != idxPp1) {
                matches.add(Pair("pp1", idxPp1))
            }
            if (idxCcc != -1) matches.add(Pair("ccc", idxCcc))
            if (idxItalicStar != -1 && idxItalicStar != idxBoldDouble && idxItalicStar != idxBoldTriple) {
                matches.add(Pair("*", idxItalicStar))
            }

            if (matches.isEmpty()) {
                append(remaining)
                break
            }

            val bestMatch = matches.minWithOrNull(compareBy<Pair<String, Int>> { it.second }.thenByDescending { it.first.length })
            if (bestMatch == null) {
                append(remaining)
                break
            }

            val tag = bestMatch.first
            val startIdx = bestMatch.second

            if (startIdx > 0) {
                append(remaining.substring(0, startIdx))
            }

            remaining = remaining.substring(startIdx + tag.length)
            val endIdx = remaining.indexOf(tag)

            if (endIdx != -1) {
                val rawContent = remaining.substring(0, endIdx)
                val parsedContent = buildInlineAnnotatedString(rawContent, baseFontSize, defaultColor)
                when (tag) {
                    "pp2" -> {
                        pushStyle(SpanStyle(color = Color(0xFF1976D2), fontWeight = FontWeight.ExtraBold))
                        append(parsedContent)
                        pop()
                    }
                    "pp1" -> {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic, fontWeight = FontWeight.SemiBold))
                        append(parsedContent)
                        pop()
                    }
                    "***" -> {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(parsedContent)
                        pop()
                    }
                    "**" -> {
                        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
                        append(parsedContent)
                        pop()
                    }
                    "*" -> {
                        pushStyle(SpanStyle(fontStyle = FontStyle.Italic))
                        append(parsedContent)
                        pop()
                    }
                    "ccc" -> {
                        pushStyle(SpanStyle(textDecoration = TextDecoration.Underline))
                        append(parsedContent)
                        pop()
                    }
                }
                remaining = remaining.substring(endIdx + tag.length)
            } else {
                append(tag)
            }
        }
    }
}

@Composable
fun rememberNetworkConnectivity(context: Context): State<Boolean> {
    val connectivityManager = remember { context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager }
    val isOnline = remember {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        mutableStateOf(capabilities?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) == true)
    }
    DisposableEffect(Unit) {
        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network)  { isOnline.value = true  }
            override fun onLost(network: Network)       { isOnline.value = false }
        }
        connectivityManager.registerNetworkCallback(NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(), callback)
        onDispose { connectivityManager.unregisterNetworkCallback(callback) }
    }
    return isOnline
}

fun bindNativeAd(ad: NativeAd, view: NativeAdView) {
    (view.headlineView as? TextView)?.text = ad.headline
    view.mediaView?.let { mediaView ->
        ad.mediaContent?.let { mediaView.mediaContent = it }
    }
    (view.callToActionView as? android.widget.Button)?.let { btn ->
        val cta = ad.callToAction
        if (!cta.isNullOrBlank()) {
            btn.text       = cta
            btn.visibility = android.view.View.VISIBLE
        } else {
            btn.visibility = android.view.View.GONE
        }
    }
    view.setNativeAd(ad)
}
