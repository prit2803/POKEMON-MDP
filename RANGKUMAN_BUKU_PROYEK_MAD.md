# Rangkuman Buku Proyek MAD: Pokemon MDP

## 1. Gambaran Umum

Proyek ini merupakan aplikasi mobile Android bertema Pokemon. Pengguna dapat membuat akun, memilih Pokemon awal, melihat koleksi dan Pokedex, menangkap atau mengelola Pokemon, menggunakan toko dan inventori, mengunggah konten, melakukan top-up koin, serta menampilkan Pokemon dalam lingkungan nyata menggunakan augmented reality (AR).

Proyek dilengkapi panel admin berbasis web untuk mengelola pengguna, post atau produk toko, Pokemon, dan riwayat pembayaran. Sistem juga memiliki backend API berbasis Node.js yang terhubung ke database MySQL atau TiDB Cloud.

## 2. Tujuan Aplikasi

- Membangun aplikasi mobile interaktif dengan konsep koleksi Pokemon.
- Menerapkan autentikasi dan manajemen sesi pengguna.
- Menyediakan penyimpanan data lokal dan sinkronisasi ke server.
- Mengintegrasikan layanan cloud, API eksternal, kamera, AR, dan pembayaran.
- Menyediakan panel administrasi untuk pengelolaan data aplikasi.

## 3. Teknologi yang Digunakan

### Aplikasi Android

- **Bahasa:** Kotlin.
- **IDE dan sistem build:** Android Studio dan Gradle Kotlin DSL.
- **Android SDK:** compile SDK dan target SDK 35, minimum SDK 24.
- **UI:** XML Layout, Material Components, ConstraintLayout, CardView, RecyclerView, ViewBinding.
- **Navigasi:** AndroidX Navigation Component dengan `NavHostFragment` dan bottom navigation.
- **Arsitektur kode:** pemisahan UI, ViewModel, repository, data source, entity, DAO, dan service. Polanya mendekati MVVM dan Repository Pattern.
- **Asynchronous programming:** Kotlin Coroutines, `lifecycleScope`, dan `viewModelScope`.
- **Pengujian:** JUnit dan AndroidX Espresso.

### Penyimpanan dan backend

- **Room:** database lokal SQLite untuk data pengguna, post, inventori, riwayat pembayaran, riwayat pembelian, dan data Pokemon.
- **MySQL/TiDB Cloud:** database server utama melalui koneksi pool `mysql2`.
- **Node.js dan Express:** REST API backend.
- **Retrofit dan Gson:** komunikasi aplikasi Android dengan REST API.
- **WorkManager:** sinkronisasi data lokal ke backend secara background.
- **Environment variables:** `dotenv` digunakan untuk konfigurasi database dan port server.

### Layanan Firebase

- **Firebase Authentication:** login dan registrasi email/password.
- **Cloud Firestore:** penyimpanan data yang digunakan oleh panel admin web.
- **Firebase Storage:** disiapkan untuk penyimpanan gambar.
- **Firebase BOM:** menyamakan versi library Firebase.

### Kamera, AR, dan computer vision

- **CameraX:** akses kamera Android.
- **ARCore:** pelacakan bidang, anchor, depth, dan posisi objek di ruang nyata.
- **SceneView:** menampilkan serta mengatur model 3D di atas ARCore.
- **MediaPipe Tasks Vision:** deteksi pose manusia dengan landmark tubuh.
- **Model 3D GLB:** aset Pokemon disimpan di folder `models` dan `models_extra`.
- **ZXing:** pembuatan QR Code dari URL gambar.
- **PixelCopy dan MediaStore:** mengambil screenshot AR dan menyimpannya ke galeri.

### Pembayaran dan layanan eksternal

- **Midtrans Snap:** pembuatan transaksi top-up menggunakan token atau redirect URL.
- **WebView:** menampilkan halaman proses pembayaran.
- **PokeAPI:** mencari data Pokemon berdasarkan nama atau nomor species.
- **ImgBB:** digunakan pada fitur capture AR untuk mengunggah gambar dan menghasilkan URL.

### Panel admin web

- **HTML:** struktur halaman.
- **CSS:** tampilan dan layout panel.
- **JavaScript ES Module:** logika aplikasi.
- **Firebase Web SDK:** autentikasi admin dan operasi Cloud Firestore.
- **Firebase Hosting:** konfigurasi deployment tersedia pada `webadmin/firebase.json`.

## 4. Arsitektur Sistem

Secara umum, sistem terdiri dari empat bagian:

```text
Android App
  |-- UI Activity/Fragment
  |-- ViewModel
  |-- Repository/Data Source
  |-- Room Database
  |-- Firebase Services
  |-- Backend REST API
          |-- Express.js
          |-- MySQL/TiDB Cloud

Web Admin
  |-- HTML/CSS/JavaScript
  |-- Firebase Authentication
  |-- Cloud Firestore
```

Alur data lokal menggunakan Room. Data yang belum tersinkronisasi diberi penanda, kemudian `SyncWorker` mengirimkannya ke backend. Pada sisi backend, Express menerima request JSON, menjalankan query terparameterisasi ke database, lalu mengembalikan response JSON.

Catatan arsitektur: aplikasi Android memiliki integrasi Firebase sekaligus backend MySQL/TiDB, sedangkan panel admin pada implementasi saat ini membaca dan mengubah koleksi Cloud Firestore secara langsung. Karena itu, konsistensi antara data Firestore dan database backend perlu dijelaskan atau disatukan pada pengembangan berikutnya.

## 5. Fitur Utama Aplikasi Mobile

### Autentikasi dan onboarding

Pengguna dapat melakukan login dan registrasi. Setelah login, pengguna dapat memilih Pokemon awal. `SessionManager` menyimpan informasi sesi untuk digunakan oleh halaman lain.

`FirebaseAuthManager` menyediakan autentikasi email/password dengan fallback ketika Firebase belum tersedia. Proyek juga memiliki endpoint login pada backend.

### Home dan feed

Halaman home menampilkan informasi pengguna, banner, feed, dan informasi aplikasi. Data ditampilkan menggunakan Fragment, ViewModel, dan beberapa RecyclerView Adapter.

### Koleksi dan Pokedex

Pengguna dapat melihat Pokemon yang dimiliki dan daftar species. Data Pokemon memiliki informasi seperti nama, HP, level, experience, species ID, status starter, status terkunci, gambar, dan waktu diperoleh.

Pokedex menyimpan cache species agar data dapat digunakan kembali tanpa selalu mengambil ulang dari sumber eksternal.

### Toko dan inventori

Post digunakan sebagai produk atau item toko. Setiap post memiliki judul, deskripsi, harga, kategori, gambar, status aktif, stok, dan waktu pembuatan.

Setelah pembelian, item dicatat pada inventori pengguna. Sistem juga menyimpan riwayat pembelian dan memiliki operasi pengurangan stok secara atomik di backend.

### Koin dan top-up

Pengguna dapat melakukan top-up koin melalui alur pemilihan nominal, metode pembayaran, pembuatan transaksi Midtrans, halaman WebView, dan halaman sukses. Riwayat pembayaran disimpan untuk ditampilkan kembali.

### Kamera AR

Pada mode Place, pengguna memilih Pokemon lalu mengetuk bidang yang terdeteksi kamera. Aplikasi membuat anchor AR dan menempatkan model 3D GLB pada posisi tersebut. Model dapat diputar, diperbesar, diperkecil, di-reset, dipindahkan, atau dihapus.

### Pose tracking

Pada mode Tracking, MediaPipe mendeteksi landmark tubuh manusia. Posisi tubuh dihitung terhadap kamera dan depth ARCore. Model Pokemon kemudian mengikuti posisi manusia. Rotasi lengan, lengan bawah, dan tangan diterapkan ke rig model 3D. Low-pass smoothing digunakan untuk mengurangi gerakan yang patah-patah.

### Capture, upload, dan QR Code

Mode Capture mengambil gambar dari tampilan AR, menyimpannya ke galeri, mengunggah salinan sementara ke ImgBB, lalu membuat QR Code dari URL gambar tersebut.

## 6. Backend REST API

Backend berada pada folder `backend` dan menggunakan Express. Middleware utamanya adalah:

- `cors()` untuk mengizinkan request dari client lain.
- `express.json()` untuk membaca body JSON.
- middleware logging untuk mencatat method dan URL request.
- endpoint `/health` untuk memeriksa koneksi database.

Kelompok endpoint yang tersedia:

- `/api/users`: tambah, ubah, hapus, login, pencarian, status banned, statistik, dan koin.
- `/api/posts`: CRUD post, daftar post aktif, jumlah post, serta pengurangan stok.
- `/api/inventory`: membaca dan memperbarui inventori pengguna.
- `/api/payments`: menyimpan dan membaca riwayat pembayaran.
- `/api/purchases`: menyimpan dan membaca riwayat pembelian.
- `/api/pokemon`: CRUD Pokemon, Pokemon starter, Pokemon milik pengguna, dan ringkasan species.
- `/api/species`: menyimpan batch cache species dan membaca range Pokedex.

Query database menggunakan placeholder `?`, sehingga nilai request dipisahkan dari query SQL. Server juga memiliki graceful shutdown untuk menutup pool database saat proses dihentikan.

## 7. Panel Admin

Panel admin mempunyai layar login dan beberapa bagian utama:

- Dashboard statistik jumlah user, post, Pokemon, dan user banned.
- Manajemen user: pencarian, edit, ban/unban, hapus, dan pemberian Pokemon.
- Manajemen post/shop: tambah, edit, aktif/nonaktif, hapus, dan pengaturan stok.
- Manajemen Pokemon: pencarian, edit, dan hapus.
- Riwayat pembayaran.

Panel menggunakan cache array di sisi browser untuk mempercepat proses render tabel. Modal digunakan untuk form tambah dan edit. Fungsi `escapeHtml` digunakan ketika data dimasukkan ke HTML tabel atau form.

## 8. Struktur Folder Penting

```text
app/src/main/java/.../
  auth/                 Login, registrasi, dan sesi
  Data/local/           Entity, DAO, database Room, data source
  Data/remote/          API, Firebase, dan response
  Data/repository/      Abstraksi akses data
  Data/worker/          Sinkronisasi background
  UI/                   Activity, Fragment, adapter, pembayaran, AR
  viewmodel/            State dan logika layar

app/src/main/res/
  layout/               Layout XML
  navigation/            Navigation graph
  menu/                 Menu aplikasi
  drawable/             Background dan shape UI
  values/               Warna, string, dan tema

backend/
  server.js             REST API Express
  db.js                 Pool koneksi MySQL/TiDB
  seeder.js             Data awal database

webadmin/public/
  index.html             Struktur panel admin
  app.js                 Firebase dan logika admin
  style.css              Tampilan panel admin

models_extra/
  *.glb                  Model 3D Pokemon tambahan
```

## 9. Alur Penggunaan Utama

1. Pengguna membuka aplikasi dan melakukan login atau registrasi.
2. Pengguna memilih Pokemon awal.
3. Aplikasi menampilkan home, koleksi, Pokedex, inventori, dan profil.
4. Pengguna dapat membeli item dari toko dengan koin.
5. Jika koin tidak cukup, pengguna menjalankan proses top-up melalui Midtrans.
6. Pengguna membuka kamera AR, memilih Pokemon, lalu menempatkannya pada bidang nyata.
7. Pengguna dapat mengaktifkan pose tracking untuk membuat Pokemon mengikuti gerakan manusia.
8. Hasil capture dapat disimpan, diunggah, dan dibagikan melalui QR Code.
9. Admin mengelola data dari panel web.

## 10. Konsep MAD yang Dapat Dibahas dalam Buku

Bab-bab buku dapat mengaitkan proyek dengan konsep berikut:

- Pengembangan aplikasi Android native menggunakan Kotlin.
- Activity, Fragment, lifecycle, dan Navigation Component.
- ViewBinding dan desain UI berbasis XML.
- RecyclerView dan Adapter untuk daftar dinamis.
- MVVM, ViewModel, LiveData, dan Repository Pattern.
- Coroutine untuk operasi asynchronous.
- Database lokal Room: Entity, DAO, dan RoomDatabase.
- REST API, HTTP method, JSON, Retrofit, dan Express.
- Sinkronisasi offline-first menggunakan WorkManager.
- Autentikasi Firebase dan manajemen sesi.
- Cloud Firestore dan Firebase Storage.
- Integrasi layanan pihak ketiga melalui API.
- Integrasi payment gateway.
- CameraX dan pengelolaan permission kamera.
- ARCore, plane detection, anchor, depth, dan model 3D.
- Pose estimation menggunakan MediaPipe.
- Pengolahan citra, screenshot, upload, dan QR Code.
- Panel admin dan operasi CRUD.
- Pengujian aplikasi Android.

## 11. Catatan Keamanan dan Pengembangan Lanjutan

Bagian ini penting dijadikan bab evaluasi, bukan dianggap sebagai fitur selesai:

- Password pada endpoint backend terlihat dibandingkan langsung. Produksi seharusnya menggunakan hashing seperti bcrypt dan token/session yang aman.
- Konfigurasi Firebase web, API key ImgBB, dan kredensial pembayaran tidak boleh ditanam langsung di source code publik.
- Secret Midtrans idealnya berada di backend, bukan di aplikasi Android.
- Endpoint backend belum terlihat memiliki autentikasi dan otorisasi yang kuat untuk setiap operasi CRUD.
- `usesCleartextTraffic="true"` sebaiknya dihindari pada produksi dan diganti komunikasi HTTPS.
- Panel admin dan aplikasi mobile memakai sumber data yang berbeda pada beberapa bagian. Diperlukan satu sumber data utama atau mekanisme sinkronisasi resmi.
- `fallbackToDestructiveMigration()` dapat menghapus data lokal saat skema berubah. Produksi sebaiknya memakai migration Room yang eksplisit.
- Input, batas stok, nominal pembayaran, dan status transaksi harus divalidasi di server.
- Aset dan layanan Pokemon memiliki ketentuan lisensi serta penggunaan yang perlu diperiksa sebelum aplikasi didistribusikan.

## 12. Kesimpulan

Proyek Pokemon MDP merupakan contoh proyek MAD yang menggabungkan pengembangan Android native, arsitektur berlapis, penyimpanan lokal, REST API, cloud service, payment gateway, web admin, AR, model 3D, dan pose estimation. Nilai utama proyek ini bukan hanya pada tampilan aplikasi, tetapi pada integrasi banyak komponen menjadi satu alur pengalaman pengguna.

Untuk penulisan buku, pembahasan paling efektif dimulai dari kebutuhan aplikasi dan desain arsitektur, kemudian berlanjut ke autentikasi, pengelolaan data, fitur toko dan pembayaran, panel admin, serta ditutup dengan implementasi AR dan evaluasi keamanan.
