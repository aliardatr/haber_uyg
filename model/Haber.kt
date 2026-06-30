package com.example.haber_portali.model

data class Haber(
    val id: Int,
    val title: String,                // Ana başlık
    val viewCount: Int,               // YENİ: Okunma sayısını buraya ekledik
    val pushSummary: String,          // İŞTE BURASI: Bildirimde görünen "merak unsuru" kısa metin
    val feedSummary: String,          // İŞTE BURASI: Uygulama içindeki kartta görünen özet metin
    val content: String,              // Haberin tam detay metni
    val dailyViewCount: Int = 0,      // Günlük tıklanma sayısını ifade eder
    val headerImage: String,          // Ana kapak görseli
    val contentImages: List<String>,  // Yazı arası görseller (değişken sayıda)
    val trustScore: Int,              // Güven skoru
    val categories: List<String>,     // Kategoriler
    val sources: List<String>,        // Kaynaklar
    val date: String                  // Yayınlanma tarihi
)