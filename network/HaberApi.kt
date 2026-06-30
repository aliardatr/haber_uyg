package com.example.haber_portali.network
import com.example.haber_portali.model.Haber
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
// =====================================================================
// 1. SUNUCUDAN GELEN JSON KALIPLARI (Response Data Classes)
// =====================================================================
/** /kategoriler endpoint'inden döner */
data class KategorilerResponse(
    val kategoriler: List<String>
)
data class CoverConfig(
    val active_cover_url: String
)
data class TokenRequest(val cihaz_id: String, val fcm_token: String)
data class IlgiAlanlariRequest(val ilgi_alanlari: List<String>)
/**
 * /haberler/son-dakika ve /haberler/{kategori} endpoint'lerinden döner.
 *
 * vitrin  → O kategorinin günlük en çok okunan max 10 haberi.
 *           SADECE ilk istekte (skip=0) dolu gelir, sonraki sayfalarda boş listedir.
 *           Bu sayede vitrin yalnızca bir kez indirilir, RAM israf edilmez.
 *
 * haberler → Sayfalama ile gelen 6'lık haber paketi.
 */
data class HaberlerResponse(
    val vitrin  : List<Haber>,
    val haberler: List<Haber>
)
// =====================================================================
// 2. FASTAPİ SUNUCU ENDPOINTLERİ
// =====================================================================
interface HaberApiService {

    @GET("api/cover")
    suspend fun getCoverConfig(): CoverConfig
    /** Sistemdeki tüm aktif kategori isimlerini çeker */
    @GET("kategoriler")
    suspend fun getKategoriler(): KategorilerResponse
    /**
     * Ana akış ve vitrin verisi — hem "Tümü" hem de belirli kategoriler için.
     *
     * @param kategoriAdi  Sunucudaki kategori adı veya "tümü" (tüm haberler)
     * @param skip         Atlanacak kayıt sayısı (sayfalama için 0, 6, 12, 18...)
     * @param limit        Kaç haber isteneceği (sabit: 6)
     *
     * Dönen [HaberlerResponse]:
     *  - vitrin  : skip=0 ise o kategorinin dailyViewCount'a göre en iyi 10'u, aksi hâlde boş liste
     *  - haberler: skip'ten itibaren limit kadar haber paketi
     */
    @GET("haberler/{kategori_adi}")
    suspend fun kategoriHaberGetir(
        @Path("kategori_adi") kategoriAdi: String,
        @Query("skip")        skip        : Int = 0,
        @Query("limit")       limit       : Int = 6
    ): HaberlerResponse
    /**
     * *önerileniçerik=ID* etiketi için tekil haber çekici.
     * Sayfalama nedeniyle o haber henüz RAM'de olmayabilir;
     * detay sayfası açıldığında bu endpoint ile sunucudan çekilir.
     */
    @GET("haberler/detay/{haber_id}")
    suspend fun haberDetayGetir(
        @Path("haber_id") haberId: Int
    ): Haber
    /**
     * Kullanıcı habere tıkladığında viewCount ve dailyViewCount'u artırır.
     * Fire-and-forget — hata olursa sessizce geçilir.
     */
    @POST("haberler/{haber_id}/tikla")
    suspend fun haberTiklanmaArtir(
        @Path("haber_id") haberId: Int
    )
    /**
     * Kullanıcı bildirime tıkladığında bildirim tıklanma sayısını artırır.
     */
    @POST("bildirimler/{bildirim_id}/tikla")
    suspend fun bildirimTiklanmaArtir(
        @Path("bildirim_id") bildirimId: Int
    )
    /**
     * Birden fazla kategori için virgüllü filtre isteği.
     * Örn: kategoriler = "Borsa,Kripto,Finans"
     *
     * @param kategoriler Virgülle ayrılmış kategori isimleri
     * @param skip        Sayfalama — kaçıncı kayıttan başlanacak
     * @param limit       Kaç kayıt isteneceği (sabit: 6)
     */
    @GET("haberler/filtrele")
    suspend fun getCokluKategoriHaberleri(
        @Query("kategoriler") kategoriler: String,
        @Query("skip")        skip        : Int = 0,
        @Query("limit")       limit       : Int = 6
    ): HaberlerResponse
    @POST("kullanicilar/token-kaydet")
    suspend fun tokenKaydet(@retrofit2.http.Body request: TokenRequest): Any
    @POST("kullanicilar/{cihaz_id}/ilgi-alanlari-kaydet")
    suspend fun ilgiAlanlariKaydet(
        @Path("cihaz_id") cihazId: String,
        @retrofit2.http.Body request: IlgiAlanlariRequest
    ): Any
}
// =====================================================================
// 3. RETROFİT MOTORU
// =====================================================================
object HaberApi {
    /**
     * Render'daki canlı sunucu adresi.
     * Emülatörde localhost test etmek isterseniz: "http://10.0.2.2:8000/"
     */
    private const val BASE_URL = "https://render-api-ft5l.onrender.com/"
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
    val retrofitService: HaberApiService by lazy {
        retrofit.create(HaberApiService::class.java)
    }
}
