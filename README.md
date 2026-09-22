# Ular Tangga Keluarga

Game ular tangga Android untuk dimainkan bersama keluarga — papan klasik dengan polesan visual modern dan beberapa mekanik permainan baru, dibuat dengan Kotlin + Jetpack Compose.

## Konsep

- **Papan hangat & hidup**: papan 10x10 dengan checkerboard warna hijau lembut, tangga digambar dengan dua rel + anak tangga (bukan garis polos), ular dengan badan menyegmen mengecil ke ekor dan kepala bermata, plus backdrop vektor bertema (bukit & pohon di Hutan Tropis, gedung neon di Kota Malam, bintang & planet di Luar Angkasa) di belakang papan. Pion pemain berupa token karakter hewan (🦁🐼🐰🦖) berukuran besar di atas lingkaran berwarna, berpindah dengan animasi halus per kotak (bukan lompat kaku). Dadu putih dengan titik (pip) seperti dadu asli, selalu di posisi tengah layar. Judul ditampilkan sebagai "papan nama kayu" ala petualangan.
- **Mekanik modern (Power-Card)**: tiga kartu sederhana yang bisa didapat dari kotak khusus di papan.
  - 🛡️ **Perisai** — otomatis melindungi dari satu gigitan ular berikutnya.
  - 🎲 **Dadu Ganda** — aktifkan sebelum melempar untuk melempar dua dadu dan memakai angka tertinggi.
  - 🔁 **Tukar Posisi** — tukar posisi dengan pemain lain, aktifkan sebelum melempar dadu.
- **Mode lawan Bot**: cocok untuk main berdua dengan anak saat anggota keluarga lain belum siap; bot melempar dadu otomatis dan cukup pintar memakai kartu Tukar Posisi/Dadu Ganda saat tertinggal.
- **Suara ringan**: efek bunyi sederhana (nada dadu, naik tangga, kena ular, kartu, menang) lewat `ToneGenerator`, bisa dimatikan dari tombol suara di layar permainan.
- **3 tema visual**: 🌿 Hutan Tropis, 🏙️ Kota Malam (neon), 🪐 Luar Angkasa — dipilih di layar setup dengan preview warna langsung ke seluruh aplikasi (papan, tombol, dadu).

## Status: MVP (Fase 1)

Yang sudah ada di project ini:
- Papan 10x10 dengan tangga & ular klasik, animasi pion per kotak.
- Dadu 3D-style dengan animasi putar.
- Mode lokal pass-and-play 2–4 pemain di satu perangkat, atau vs Bot.
- Kustomisasi warna pion otomatis dari palet 4 warna.
- 3 power-card dasar (Perisai, Dadu Ganda, Tukar Posisi).
- Toggle suara on/off.
- 3 tema visual yang bisa dipilih di layar setup (Hutan Tropis, Kota Malam, Luar Angkasa).
- Kotak Misteri (efek acak: maju/mundur 5 langkah, dapat kartu, tukar posisi acak, atau tidak ada efek).
- Combo/Streak: dadu kembar dua kali berturut-turut untuk pemain yang sama memberi giliran ekstra.
- Token karakter hewan (Singa, Panda, Kelinci, Dino), papan nama kayu untuk judul, dan backdrop vektor bertema di belakang papan.
- Personalisasi pemain: nama & avatar bisa diedit per pemain di layar setup.
- Mode "Pemain Muda": pemain yang ditandai mulai dengan kartu Perisai gratis dan Kotak Misteri tidak akan memberi efek mundur — bikin permainan lintas usia lebih seimbang.
- Ritual harian: streak hari main berturut-turut (disimpan di perangkat via `SharedPreferences`), memberi kartu power-card gratis tiap kali app dibuka di hari baru.
- Mini-game "Tap Cepat" (⚡ kotak spesial): tantangan reflex 3 detik, berhasil = maju 4 langkah, gagal = tidak ada efek buruk.
- 3 Mode Permainan yang bisa dipilih di layar setup:
  - 🏁 **Klasik** — pemain pertama sampai kotak 100 menang.
  - 🤝 **Co-op** — semua pemain bekerja sama, menang bersama saat semua pemain sampai finish.
  - 💥 **Battle Royale Mini** — kotak acak di papan runtuh setiap beberapa giliran; mendarat di kotak yang sudah runtuh membuat pemain terpental kembali ke posisi sebelumnya.
- **Main Online (v1, room code)**: satu anggota keluarga jadi host, buat room dan dapat kode 5 karakter, anggota lain gabung pakai kode itu dari device masing-masing (tidak perlu akun/login). Real-time sync posisi & giliran lewat Firebase Realtime Database, host-authoritative (hanya device host yang menjalankan logika permainan; device tamu hanya merender & mengirim permintaan lempar dadu).
  - **Batasan v1** (sengaja disederhanakan): hanya mode Klasik yang tersedia online (Co-op/Battle Royale belum disinkronkan); kartu Dadu Ganda/Tukar Posisi & mini-game Tap Cepat untuk pemain tamu diproses otomatis oleh sistem (belum ada dialog interaktif lintas-device); tidak ada chat.
  - **Perlu setup manual**: lihat bagian [Setup Firebase untuk Main Online](#setup-firebase-untuk-main-online) di bawah — tanpa ini, tombol Main Online akan menampilkan pesan "belum dikonfigurasi" tapi sisa aplikasi tetap jalan normal.
- **Web (tamu)**: halaman statis di folder [`web/`](web/) supaya anggota keluarga tanpa Android bisa gabung room lewat browser (kode room yang sama), pakai skema Firebase yang identik dengan app Android. Cara deploy ke VPS sendiri ada di [`web/README.md`](web/README.md).

## Roadmap (Fase berikutnya)

- Replay/highlight akhir permainan yang bisa dibagikan.
- Main Online untuk mode Co-op/Battle Royale, kartu & mini-game interaktif lintas-device, chat/reaction ringan.

## Struktur Project

```
app/src/main/java/id/ulartangga/keluarga/
├── MainActivity.kt              # Entry point, navigasi antar layar (Setup/Local/Online)
├── game/
│   ├── Player.kt                 # Model pemain (posisi, kartu, warna, remote id)
│   ├── PowerCardType.kt          # Definisi 3 jenis power-card
│   ├── GameMode.kt               # Klasik / Co-op / Battle Royale Mini
│   ├── BoardConfig.kt            # Posisi tangga, ular, kotak kartu/misteri/mini-game
│   └── GameEngine.kt             # Logika giliran, lempar dadu, kartu, menang
├── data/
│   └── DailyRewardManager.kt     # Streak harian via SharedPreferences
├── online/
│   ├── FirebaseConfig.kt         # Kredensial Firebase (diisi manual, lihat di bawah)
│   ├── FirebaseBootstrap.kt      # Init FirebaseApp manual tanpa google-services.json
│   ├── DeviceId.kt               # Id stabil per instalasi app
│   ├── RoomRepository.kt         # Wrapper Firebase Realtime Database (room code)
│   ├── OnlineHostController.kt   # Device host: jalankan GameEngine, siarkan state
│   └── OnlineGuestController.kt  # Device tamu: render state, kirim aksi lempar dadu
├── sound/
│   └── SoundManager.kt           # Efek suara sederhana via ToneGenerator
└── ui/
    ├── theme/                    # BoardTheme (3 tema visual), tipografi Material3
    ├── components/                # BoardView, DiceView, MiniGameDialog, WoodenSign, dst.
    └── screens/                   # SetupScreen, GameScreen, OnlineLobbyScreen, OnlineGameScreen
```

## Menjalankan Project

1. Buka folder ini dengan Android Studio (Koala/2024.1 atau lebih baru).
2. Biarkan Gradle sync selesai (project menggunakan AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00).
3. Jalankan konfigurasi `app` ke emulator atau perangkat Android 8.0 (API 26) ke atas.

> Catatan: kode ini disusun langsung tanpa proses build lokal di lingkungan pembuatan (tidak ada Android SDK tersedia di sana). Saat pertama kali dibuka di Android Studio, lakukan Gradle sync dan perbaiki bila ada ketidakcocokan versi API kecil sebelum run pertama.

## Setup Firebase untuk Main Online

Fitur Main Online butuh project Firebase milik kamu sendiri (gratis untuk skala keluarga). Ini tidak bisa disiapkan otomatis dari sesi pembuatan project ini karena butuh akun Google.

1. Buka [console.firebase.google.com](https://console.firebase.google.com), buat project baru (nama bebas).
2. Di project itu, buka **Build → Realtime Database → Create Database**. Pilih lokasi server, lalu mulai dalam **test mode** (aturan akses terbuka) — cukup aman untuk v1 karena akses room dilindungi kode room yang hanya dibagikan ke keluarga sendiri, bukan untuk data sensitif.
3. Buka **Project settings (ikon gerigi) → General**, scroll ke "Your apps", klik ikon **Web (`</>`)** untuk mendaftarkan "web app" (tidak perlu app Android sungguhan — kita hanya butuh nilai config-nya untuk init manual).
4. Salin nilai `apiKey`, `appId`, `projectId`, dan `databaseURL` dari config yang muncul.
5. Tempel ke `app/src/main/java/id/ulartangga/keluarga/online/FirebaseConfig.kt`, isi 4 konstanta di sana.
6. Commit & push — CI akan build ulang otomatis, dan tombol "Main Online" di app akan aktif.

Tanpa langkah di atas, aplikasi tetap berjalan normal (semua fitur offline utuh) — tombol Main Online hanya akan menampilkan pesan bahwa fitur belum dikonfigurasi.
