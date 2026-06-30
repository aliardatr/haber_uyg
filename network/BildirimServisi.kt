package com.example.haber_portali.network
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.haber_portali.MainActivity
import com.example.haber_portali.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.net.URL
class BildirimServisi : FirebaseMessagingService() {
    // 1. Yeni bir Token üretildiğinde sunucuya haber ver
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Yeni Token: $token")
        sunucuyaTokenGonder(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        // Bildirimin başlık ve içeriğini al
        val baslik = remoteMessage.notification?.title ?: remoteMessage.data["baslik"] ?: "Yeni Haber"
        val icerik = remoteMessage.notification?.body ?: remoteMessage.data["icerik"] ?: ""
        val fallbackImageUrl = remoteMessage.notification?.imageUrl?.toString() ?: remoteMessage.data["image_url"] // <--- GÖRSEL LİNKİ

        // Data payload içinden verileri çek
        val haberId = remoteMessage.data["haber_id"]
        val hedefKategori = remoteMessage.data["hedef_kategori"]
        val bildirimId = remoteMessage.data["bildirim_id"]
        val kucukResim = remoteMessage.data["kucuk_resim"]
        val buyukResim = remoteMessage.data["buyuk_resim"]
        val genisMetin = remoteMessage.data["genis_metin"]
        val genisletmeTipi = remoteMessage.data["genisletme_tipi"] // "resim" veya "metin"
        bildirimGoster(
            baslik = baslik,
            icerik = icerik,
            haberId = haberId,
            hedefKategori = hedefKategori,
            bildirimId = bildirimId,
            kucukResim = kucukResim,
            buyukResim = buyukResim,
            genisMetin = genisMetin,
            genisletmeTipi = genisletmeTipi,
            fallbackImageUrl = fallbackImageUrl
        )
    }
    private fun downloadBitmap(urlStr: String): android.graphics.Bitmap? {
        return try {
            val url = java.net.URL(urlStr)
            val connection = url.openConnection().apply {
                connectTimeout = 8000
                readTimeout = 8000
                setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36")
            }
            BitmapFactory.decodeStream(connection.getInputStream())
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    private fun centerCropTo2To1(source: android.graphics.Bitmap): android.graphics.Bitmap {
        val width = source.width
        val height = source.height
        val targetRatio = 2.0 // En / Boy = 2 (Android Bildirim Standardı)

        val newWidth: Int
        val newHeight: Int
        val startX: Int
        val startY: Int

        if (width.toDouble() / height.toDouble() > targetRatio) {
            // Görsel 2:1 oranından geniş, yanlardan eşit kırpıp ortalayalım
            newHeight = height
            newWidth = (height * targetRatio).toInt()
            startX = (width - newWidth) / 2
            startY = 0
        } else {
            // Görsel 2:1 oranından dikey, üst/alttan eşit kırpıp ortalayalım
            newWidth = width
            newHeight = (width / targetRatio).toInt()
            startX = 0
            startY = (height - newHeight) / 2
        }

        return try {
            android.graphics.Bitmap.createBitmap(source, startX, startY, newWidth, newHeight)
        } catch (e: java.lang.Exception) {
            source
        }
    }
    private fun bildirimGoster(
        baslik: String,
        icerik: String,
        haberId: String?,
        hedefKategori: String?,
        bildirimId: String?,
        kucukResim: String?,
        buyukResim: String?,
        genisMetin: String?,
        genisletmeTipi: String?,
        fallbackImageUrl: String?
    ) {
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "haber_kanali"
        // Android 8.0 ve üzeri için kanal oluşturma
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Flaş Haberler",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Son dakika haber bildirimleri"
            }
            manager.createNotificationChannel(channel)
        }
        // Bildirime tıklandığında açılacak ekranı ayarla
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            if (!haberId.isNullOrEmpty()) {
                putExtra("haber_id", haberId)
            }
            if (!hedefKategori.isNullOrEmpty()) {
                putExtra("hedef_kategori", hedefKategori)
            }
            if (!bildirimId.isNullOrEmpty()) {
                putExtra("bildirim_id", bildirimId)
            }
        }
        val pendingIntent = PendingIntent.getActivity(
            this, System.currentTimeMillis().toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val builder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Kendi ikonunuzu koyun
            .setContentTitle(baslik)
            .setContentText(icerik)
            .setAutoCancel(true) // Tıklanınca kaybolsun
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
        // 1. Sağdaki Küçük Görsel (Large Icon - Kapalıyken sağda görünür)
        val kucukResimUrl = if (!kucukResim.isNullOrEmpty()) kucukResim else fallbackImageUrl
        if (!kucukResimUrl.isNullOrEmpty()) {
            try {
                val bitmap = downloadBitmap(kucukResimUrl)
                if (bitmap != null) {
                    builder.setLargeIcon(bitmap)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        // 2. Genişletilmiş Görünüm Stilleri (Metin veya Resim)
        if (genisletmeTipi == "metin") {
            // Metin Genişleme Modu: Genişleyince sadece başlık ve özel metin gözükür, normal özet kalkar
            val finalMetin = if (!genisMetin.isNullOrEmpty()) genisMetin else icerik
            if (!finalMetin.isNullOrEmpty()) {
                val bigTextStyle = NotificationCompat.BigTextStyle()
                    .setBigContentTitle(baslik)
                    .bigText(finalMetin)
                    .setSummaryText("") // Normal bildirimde duran alt özeti gizler/kaldırır
                builder.setStyle(bigTextStyle)
            }
        } else {
            // Resim Genişleme Modu: Genişleyince altta sadece büyük görsel gözükür, normal özet kalkar
            val buyukResimUrl = if (!buyukResim.isNullOrEmpty()) buyukResim else fallbackImageUrl
            if (!buyukResimUrl.isNullOrEmpty()) {
                try {
                    val rawBitmap = downloadBitmap(buyukResimUrl)
                    if (rawBitmap != null) {
                        // Android bildirim arayüzlerinde resmin sağa/sola kaymasını önlemek için
                        // görseli tam ortasından kırpıp 2:1 en-boy oranına getiriyoruz.
                        val centeredBitmap = centerCropTo2To1(rawBitmap)

                        // 1x1 şeffaf bitmap oluşturarak sağdaki büyük görseli genişletilmiş görünümde gizliyoruz.
                        val transparentBitmap = android.graphics.Bitmap.createBitmap(1, 1, android.graphics.Bitmap.Config.ARGB_8888).apply {
                            eraseColor(android.graphics.Color.TRANSPARENT)
                        }

                        val bigPictureStyle = NotificationCompat.BigPictureStyle()
                            .setBigContentTitle(baslik)
                            .bigPicture(centeredBitmap)
                            .bigLargeIcon(transparentBitmap) // Genişleyince sağdaki küçük resim gizlenir ve ortalanır
                            .setSummaryText("") // Normal bildirimde duran alt özeti gizler/kaldırır
                        builder.setStyle(bigPictureStyle)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }
    private fun sunucuyaTokenGonder(token: String) {
        // Cihazın benzersiz kimliğini al (örnek olarak SharedPreferences'tan okunabilir)
        val sharedPref = getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        var cihazId = sharedPref.getString("cihaz_id", null)

        if (cihazId == null) {
            cihazId = java.util.UUID.randomUUID().toString()
            sharedPref.edit().putString("cihaz_id", cihazId).apply()
        }
        // Retrofit ile sunucuya post at
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val request = TokenRequest(cihaz_id = cihazId, fcm_token = token)
                HaberApi.retrofitService.tokenKaydet(request)
                Log.d("FCM_TOKEN", "Token sunucuya kaydedildi.")
            } catch (e: Exception) {
                Log.e("FCM_TOKEN", "Sunucu hatası: ${e.message}")
            }
        }
    }
}
