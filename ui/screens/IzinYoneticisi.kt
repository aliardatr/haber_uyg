package com.example.haber_portali.ui.screens
import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
/**
 * Akıllı ve Rahatsız Etmeyen İzin Yönetim Sistemi (IzinYoneticisi)
 *
 * Sizin istediğiniz şu akıllı kuralları tam uyumlulukla işletir:
 * 1. Uygulama ilk kez açıldığında (1. Giriş) kullanıcıdan bildirim iznini ister.
 * 2. Kullanıcı izin vermezse, sonraki 2 girişte (2. ve 3. Giriş) izin istemeye devam eder.
 * 3. Eğer kullanıcı üst üste 3 kez izni reddederse, artık kullanıcıyı sıkmamak için
 *    her 5 girişte bir (Örn: 8. Giriş, 13. Giriş, 18. Giriş...) izni hatırlatır.
 * 4. İzin herhangi bir zamanda verilirse tüm red sayaçları otomatik sıfırlanır ve bir daha sorulmaz.
 */
@Composable
fun IzinYoneticisi(
    onFinish: () -> Unit
) {
    val context = LocalContext.current
    val sharedPref = remember { context.getSharedPreferences("AppPermissionPrefs", Context.MODE_PRIVATE) }
    // Android 13 (TIRAMISU / API 33) ve sonrası için bildirim gönderme izni (POST_NOTIFICATIONS) runtime izindir.
    // İnternet, Ağ Durumu ve Reklam Kimliği izinleri normal izin olup kurulumda otomatik verilir.
    val bildirimIzni = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        Manifest.permission.POST_NOTIFICATIONS
    } else {
        null
    }
    // Eğer cihazın işletim sistemi sürümü bildirim izni gerektirmiyorsa veya izin ZATEN VERİLMİŞSE doğrudan geç
    if (bildirimIzni == null || ContextCompat.checkSelfPermission(context, bildirimIzni) == PackageManager.PERMISSION_GRANTED) {
        LaunchedEffect(Unit) {
            // İzin zaten verilmiş olduğundan tüm red sayaçlarını sıfırla
            sharedPref.edit().putInt("consecutive_denied_count", 0).apply()
            onFinish()
        }
        return
    }
    var showRequest by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        // Giriş (Açılış) sayısını 1 artır
        val launchCount = sharedPref.getInt("launch_count", 0) + 1
        sharedPref.edit().putInt("launch_count", launchCount).apply()
        val consecutiveDeniedCount = sharedPref.getInt("consecutive_denied_count", 0)
        val lastRequestLaunch = sharedPref.getInt("last_request_launch", 0)
        // Sizin istediğiniz akıllı periyot kontrolü:
        val shouldRequest = when {
            // 1. İlk açılışta kesinlikle iste
            launchCount == 1 -> true

            // 2. Red durumunda sonraki 2 girişte de ardı ardına istemeye devam et (Launch 2 ve 3)
            consecutiveDeniedCount < 3 -> true

            // 3. Toplamda 3 kez reddettiyse, her 5 girişte bir iste
            else -> (launchCount - lastRequestLaunch) >= 5
        }
        if (shouldRequest) {
            // Son istek yapılan giriş numarasını kaydet
            sharedPref.edit().putInt("last_request_launch", launchCount).apply()
            showRequest = true
        } else {
            // Periyot sırası değilse doğrudan ana ekrana geç
            onFinish()
        }
    }
    // Compose ActivityResult API ile izni işleten launcher
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            // Kullanıcı izin verdiyse ardışık reddetme sayacını sıfırla
            sharedPref.edit().putInt("consecutive_denied_count", 0).apply()
        } else {
            // Red durumunda ardışık reddetme sayısını 1 artır
            val currentDenied = sharedPref.getInt("consecutive_denied_count", 0) + 1
            sharedPref.edit().putInt("consecutive_denied_count", currentDenied).apply()
        }
        onFinish()
    }
    if (showRequest) {
        LaunchedEffect(Unit) {
            launcher.launch(bildirimIzni)
        }
    }
}
