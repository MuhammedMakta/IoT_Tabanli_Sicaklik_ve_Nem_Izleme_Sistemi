Proje Özeti
Bu proje, bir IoT (Nesnelerin İnterneti) cihazı kullanarak ortam sıcaklığı ve nem değerlerini izlemeyi amaçlamaktadır. Proje iki ana bileşenden oluşmaktadır: bir ESP32 mikrodenetleyici ve bir Android mobil uygulama. ESP32, DHT11 sensöründen sıcaklık ve nem verilerini alır ve bu verileri bir web sunucusu aracılığıyla yayınlar. Android uygulaması, bu verilere erişir ve kullanıcıya gösterir.
Kullanılan Teknolojiler
Donanım
1.	ESP32: WiFi özellikli mikrodenetleyici.
2.	DHT11: Sıcaklık ve nem sensörü.
Yazılım
1.	Arduino IDE: ESP32'nin programlanması için kullanılmıştır.
2.	Android Studio: Android uygulamasının geliştirilmesi için kullanılmıştır.
ESP32 Kodu Açıklaması
ESP32, WiFi ağına bağlanarak bir web sunucusu kurar ve DHT11 sensöründen sıcaklık ve nem verilerini alır. Bu veriler JSON formatında bir web endpoint üzerinden sunulur.
Android Uygulaması Açıklaması
Android uygulaması, ESP32 tarafından sağlanan verileri düzenli aralıklarla alır ve kullanıcıya gösterir. Ayrıca, belirli sıcaklık ve nem değerleri aşıldığında kullanıcıya bildirim gönderir.
1.	MainActivity: Uygulamanın ana aktivitesi. Sıcaklık ve nem değerlerini ekranda gösterir.
2.	FetchDataService: Arka planda çalışan bir servis. Belirli aralıklarla ESP32'den veri alır ve işleyerek bildirim gönderir.
Yayma Alıcısı (Broadcast Receiver)
MainActivity, bir yayma alıcısı (BroadcastReceiver) tanımlar ve bu alıcı, FetchDataService tarafından gönderilen sıcaklık ve nem verilerini alır.
Bildirimler
FetchDataService, sıcaklık veya nem belirli eşik değerlerini aştığında bildirim gönderir.
