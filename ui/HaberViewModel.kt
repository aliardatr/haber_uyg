package com.example.haber_portali.ui
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.haber_portali.model.Haber
import com.example.haber_portali.network.HaberApi
import kotlinx.coroutines.launch
class HaberViewModel : ViewModel() {
    // =====================================================================
    // STATE TANIMLARI
    // =====================================================================
    /** Ana akış haberleri — sunucudan 6'şar 6'şar sayfalanarak gelir, max 50 */
    var haberler: List<Haber> by mutableStateOf(emptyList())
        private set
    /**
     * Vitrin haberleri — o kategorinin günlük en çok okunan max 10'u.
     * Yalnızca skip=0 (ilk istek) anında dolu gelir; kategori değişince sıfırlanır.
     */
    var vitrinHaberleri: List<Haber> by mutableStateOf(emptyList())
        private set
    /** Yan menüde gösterilecek kategori listesi */
    var sunucuKategorileri: List<String> by mutableStateOf(emptyList())
        private set
    /** İlk yükleme spinner/shimmer kontrolü */
    var yukleniyorMu: Boolean by mutableStateOf(false)
        private set
    /** "Daha fazla yükle" sırasında alt shimmer kontrolü */
    var isPaginating: Boolean by mutableStateOf(false)
        private set
    /**
     * Sunucuda daha fazla haber var mı?
     * Gelen paket 6'dan az ise veya toplam 50'ye ulaşıldıysa false olur
     * → LazyColumn sayfalama tetikleyicisi ve shimmer kapanır.
     */
    var canLoadMore: Boolean by mutableStateOf(true)
        private set
    // =====================================================================
    // İLK AÇILIŞ
    // =====================================================================
    init {
        // Uygulama açılınca önce kategorileri, sonra ilk haber paketini çek
        verileriBaslat()
    }
    private fun verileriBaslat() {
        viewModelScope.launch {
            yukleniyorMu = true
            try {
                // Kategori listesini çek (menü ve üst filtre barı için)
                val katResponse = HaberApi.retrofitService.getKategoriler()
                sunucuKategorileri = katResponse.kategoriler
                // İlk açılışta "Tümü" kategorisinden ilk 6 haberi + vitrini getir
                val haberResponse = HaberApi.retrofitService.kategoriHaberGetir(
                    kategoriAdi = "tümü",
                    skip        = 0,
                    limit       = 6
                )
                vitrinHaberleri = haberResponse.vitrin
                haberler        = haberResponse.haberler
                canLoadMore     = haberResponse.haberler.size >= 6
            } catch (e: Exception) {
                println("❌ Başlangıç Hatası: ${e.message}")
            } finally {
                yukleniyorMu = false
            }
        }
    }
    // =====================================================================
    // KATEGORİYE GÖRE HABER GETİR  (sıfırdan yükleme)
    // UI'dan kategori değişince veya Pull-to-Refresh tetiklenince çağrılır.
    // skip=0 → haberler ve vitrin sıfırlanır, sayfa başından başlar.
    // =====================================================================
    fun kategoriyeGoreGetir(kategori: String, skip: Int = 0) {
        viewModelScope.launch {
            if (skip == 0) {
                // Sıfırdan yükleme: state'leri temizle
                yukleniyorMu    = true
                haberler        = emptyList()
                vitrinHaberleri = emptyList()
                canLoadMore     = true
            } else {
                // Sayfalama: sadece alt shimmer'ı aç
                isPaginating = true
            }
            try {
                // "Tümü" için sunucuda "tümü" path'ini kullan
                val apiKategori = if (kategori == "Tümü") "tümü" else kategori
                val response = HaberApi.retrofitService.kategoriHaberGetir(
                    kategoriAdi = apiKategori,
                    skip        = skip,
                    limit       = 6
                )
                if (skip == 0) {
                    // İlk sayfa: vitrin + ilk haber paketi
                    vitrinHaberleri = response.vitrin
                    haberler        = response.haberler
                } else {
                    // Sonraki sayfalar: mevcut listeye ekle (vitrin değişmez)
                    haberler = haberler + response.haberler
                }
                // Yükleme durdurma koşulları:
                // - Gelen paket 6'dan az → sunucuda haber bitti
                // - Toplam 50'ye ulaştı → UI limiti doldu
                canLoadMore = response.haberler.size >= 6 && haberler.size < 50
            } catch (e: Exception) {
                println("❌ Haber Getirme Hatası: ${e.message}")
            } finally {
                yukleniyorMu = false
                isPaginating = false
            }
        }
    }
    // =====================================================================
    // DAHA FAZLA YÜKLE  (sayfalama devamı)
    // AnaEkran'dan listenin sonuna yaklaşıldığında çağrılır.
    // =====================================================================
    fun loadMore(kategori: String) {
        // Çakışan istekleri ve 50 sınırını geç koşulları engelle
        if (!isPaginating && canLoadMore && haberler.size < 50) {
            kategoriyeGoreGetir(kategori, skip = haberler.size)
        }
    }
    // =====================================================================
    // ÇOKLU KATEGORİ FİLTRELEME  (Drawer'daki bildirim tercihleri için)
    // Birden fazla kategori virgülle birleştirilerek sunucuya gönderilir.
    // Örn: seciliKategoriler = {"Borsa", "Kripto"} → "Borsa,Kripto"
    // =====================================================================
    fun haberleriFiltreleVeGetir(seciliKategoriler: Set<String>) {
        viewModelScope.launch {
            yukleniyorMu    = true
            haberler        = emptyList()
            vitrinHaberleri = emptyList()
            canLoadMore     = true
            try {
                val sorguMetni = seciliKategoriler.joinToString(",")
                val response   = HaberApi.retrofitService.getCokluKategoriHaberleri(
                    kategoriler = sorguMetni,
                    skip        = 0,
                    limit       = 6
                )
                vitrinHaberleri = response.vitrin
                haberler        = response.haberler
                canLoadMore     = response.haberler.size >= 6
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                yukleniyorMu = false
            }
        }
    }
}
