# İBB / İETT Veri Kaynakları — Araştırma ve Öneri

> Bu doküman, **canlı otobüs verisi entegrasyonundan önce** sunulması istenen
> araştırmanın çıktısıdır. Kod tarafında canlı veri katmanı bilinçli olarak
> **bağlanmadı** (`OfflineOnlyLiveDataSource`); aşağıdaki öneri onaylanınca
> `LiveDataSource` arayüzüne gerçek implementasyon takılacaktır.

_Last reviewed: 2026-06-13._

## TL;DR (Özet)

- İstanbul toplu taşıma verisi **İBB Açık Veri Portalı** (`data.ibb.gov.tr`)
  üzerinden **ücretsiz** olarak yayımlanıyor. Ödemeli bir resmi API bulunamadı —
  yani şartnamedeki "API ücretli olursa AdMob ekle" senaryosu büyük olasılıkla
  **tetiklenmeyecek**. Uygulamayı reklamsız tutuyoruz.
- **Çevrimdışı çekirdek için en iyi kaynak: GTFS veri setleri.** İBB hem İETT
  (otobüs) hem de Metro/Marmaray/Metrobüs/vapur için GTFS yayımlıyor. Bu, durak,
  hat, güzergah ve **planlanan sefer saatlerini** tek pakette verir — Room DB'yi
  bununla doldurmak çevrimdışı-öncelikli mimarinin temeli.
- **Gerçek "kaç dakika kaldı" (canlı varış tahmini) için temiz, dokümante,
  ücretsiz bir endpoint yok.** Resmi "Otobüsüm Nerede?" uygulaması ETA gösteriyor
  ama bunu açık API olarak sunmuyor. Canlı veri için iki gerçekçi yol var (aşağıda).

## Kaynaklar ve endpoint'ler

### 1) İBB Açık Veri Portalı — GTFS (önerilen çevrimdışı kaynak)
- İETT GTFS: <https://data.ibb.gov.tr/dataset/iett-gtfs-verisi>
- Toplu Ulaşım GTFS (Metro, Marmaray, Metrobüs, vapur, minibüs vb.):
  <https://data.ibb.gov.tr/dataset/public-transport-gtfs-data>
- İçerik: `stops.txt`, `routes.txt`, `trips.txt`, `stop_times.txt`, `shapes.txt`…
- Maliyet: Ücretsiz (indirme için ücretsiz İBB hesabı/giriş gerekebilir).
- Bizim için değeri: Durak arama, hattan-geçen-hatlar, hat güzergahı ve
  **kalkış saatleri** doğrudan buradan gelir. Tamamen çevrimdışı çalışır.

### 2) İETT Hat-Durak-Güzergâh SOAP servisi (statik/yarı-statik)
- Veri seti: <https://data.ibb.gov.tr/dataset/iett-hat-durak-guzergah-web-servisi>
- Endpoint (WSDL): `https://api.ibb.gov.tr/iett/UlasimAnaVeri/HatDurakGuzergah.asmx?wsdl`
- İçerik: Hatlar, duraklar, hat-durak ilişkileri, güzergah noktaları.
- Maliyet: Ücretsiz, API anahtarı görünmüyor. **SOAP**, yavaş; gece bakımına
  girip kısa süre kapanabildiği topluluk notlarında belirtiliyor.

### 3) Canlı araç konumu / sefer gerçekleşme (gerçek zamanlı)
- `SeferGerceklesme.asmx` (FiloDurum) — bir hattaki otobüslerin **anlık GPS
  konumları**. Topluluk kütüphanelerinde `GetVehicleLocationsAsync` olarak da
  geçiyor (dakikada bir güncelleniyor).
- `PlanlananSeferSaati` — **planlanan kalkış saatleri** (canlı değil, tarifeli).
- Maliyet: Ücretsiz; SOAP; kararlılık değişken.

### Topluluk sarmalayıcıları (referans, doğrudan bağımlılık önerilmez)
- `AydinAdn/IBB.Api` (.NET), `onur2677/iett-api` (Node), `hakanatak/dataibbgovtr`
  (GeoJSON). Eski `mekansal.herokuapp.com` proxy'leri **artık ölü** (Heroku free
  tier kapandı). Bu da çevrimdışı-öncelikli tasarımı doğruluyor.

## "Kaç dakika kaldı?" sorununun durumu

Durak bazında resmi, ücretsiz, dokümante bir **varış tahmini** endpoint'i yok.
Seçenekler:

1. **ETA'yı kendimiz hesaplayalım:** `SeferGerceklesme` canlı araç konumları +
   güzergah geometrisi (GTFS `shapes.txt`) + ortalama hız ile durak ETA'sı üret.
   Tam kontrol, dış bağımlılık yok; biraz hesap işi.
2. **GTFS-Realtime varsa onu kullanalım:** İBB'nin GTFS-RT (TripUpdates /
   VehiclePositions) feed'i yayımlayıp yayımlamadığı netleştirilmeli. Varsa en
   temiz ve standart yol budur.
3. **Şimdilik tarifeli saat göster:** GTFS `stop_times`/`PlanlananSeferSaati`
   ile "planlanan" saatleri göster (uygulamada şu an yapılan budur; UI bunu
   "canlı değil, planlanan saat" olarak nazikçe etiketliyor).

## Öneri (onayınıza sunulur)

- **Faz 1 — şimdi (onay gerektirmez, yapıldı):** Çevrimdışı çekirdek. Veriler
  paketlenmiş GTFS alt kümesinden Room'a tohumlanır. Otobüs saatleri "planlanan"
  olarak gösterilir. Hata mesajı yok; sadece nazik bilgi notu.
- **Faz 2 — onayınızla:** Canlı veriyi bağla. Önerim: önce GTFS-RT'nin varlığını
  doğrulamak; varsa onu kullanmak. Yoksa `SeferGerceklesme` ile ETA'yı kendimiz
  hesaplamak. Her iki durumda da yalnızca `LiveDataSource` implementasyonu
  eklenecek; UI ve repository imzaları değişmeyecek.

### Karar bekleyen sorular
1. Canlı veriye **GTFS-Realtime** ile mi (varsa) yoksa **SOAP + kendi ETA
   hesabımız** ile mi gidelim?
2. Tam GTFS'i uygulamaya **paketleyelim mi** (büyük, çevrimdışı tam kapsama) yoksa
   ilk açılışta **bir kez indirip** Room'a mı yazalım (küçük APK, ilk açılışta net
   gerekir)?
3. Resmi servislerin ücretsiz olduğu doğrulandığına göre **AdMob tamamen iptal**
   mi (önerim: evet, reklamsız kalsın)?

## Kaynaklar
- <https://data.ibb.gov.tr/> ve alt veri setleri (yukarıda link verildi)
- <https://github.com/AydinAdn/IBB.Api>
- <https://github.com/onur2677/iett-api>
- <https://github.com/hakanatak/dataibbgovtr>
- <https://burakbayramli.github.io/dersblog/sk/2023/01/iett-ibb-otobus-verisi.html>
