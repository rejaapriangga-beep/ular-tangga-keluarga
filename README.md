# Ular Tangga Keluarga

Game ular tangga Android untuk dimainkan bersama keluarga — papan klasik dengan polesan visual modern dan beberapa mekanik permainan baru, dibuat dengan Kotlin + Jetpack Compose.

## Konsep

- **Papan hangat & hidup**: papan 10x10 dengan checkerboard warna hijau lembut, tangga digambar sebagai balok oranye, ular sebagai garis lengkung merah. Pion pemain berupa lingkaran berwarna yang berpindah dengan animasi halus per kotak (bukan lompat kaku).
- **Mekanik modern (Power-Card)**: tiga kartu sederhana yang bisa didapat dari kotak khusus di papan.
  - 🛡️ **Perisai** — otomatis melindungi dari satu gigitan ular berikutnya.
  - 🎲 **Dadu Ganda** — aktifkan sebelum melempar untuk melempar dua dadu dan memakai angka tertinggi.
  - 🔁 **Tukar Posisi** — tukar posisi dengan pemain lain, aktifkan sebelum melempar dadu.
- **Mode lawan Bot**: cocok untuk main berdua dengan anak saat anggota keluarga lain belum siap; bot melempar dadu otomatis dan cukup pintar memakai kartu Tukar Posisi/Dadu Ganda saat tertinggal.
- **Suara ringan**: efek bunyi sederhana (nada dadu, naik tangga, kena ular, kartu, menang) lewat `ToneGenerator`, bisa dimatikan dari tombol suara di layar permainan.

## Status: MVP (Fase 1)

Yang sudah ada di project ini:
- Papan 10x10 dengan tangga & ular klasik, animasi pion per kotak.
- Dadu 3D-style dengan animasi putar.
- Mode lokal pass-and-play 2–4 pemain di satu perangkat, atau vs Bot.
- Kustomisasi warna pion otomatis dari palet 4 warna.
- 3 power-card dasar (Perisai, Dadu Ganda, Tukar Posisi).
- Toggle suara on/off.

## Roadmap (Fase berikutnya)

- Tema visual tambahan (Kota Malam, Luar Angkasa, dll) selain tema Hutan Tropis saat ini.
- Mini-game singkat saat mendarat di kotak spesial.
- Kotak Misteri acak & mekanik Combo/Streak (dadu kembar berturut-turut).
- Mode Co-op dan Battle Royale Mini.
- Replay/highlight akhir permainan yang bisa dibagikan.
- Multiplayer online.

## Struktur Project

```
app/src/main/java/id/ulartangga/keluarga/
├── MainActivity.kt              # Entry point, navigasi Setup <-> Game
├── game/
│   ├── Player.kt                 # Model pemain (posisi, kartu, warna)
│   ├── PowerCardType.kt          # Definisi 3 jenis power-card
│   ├── BoardConfig.kt            # Posisi tangga, ular, kotak kartu
│   └── GameEngine.kt             # Logika giliran, lempar dadu, kartu, menang
├── sound/
│   └── SoundManager.kt           # Efek suara sederhana via ToneGenerator
└── ui/
    ├── theme/                    # Warna & tipografi Compose Material3
    ├── components/                # BoardView (papan + pion), DiceView
    └── screens/                   # SetupScreen, GameScreen
```

## Menjalankan Project

1. Buka folder ini dengan Android Studio (Koala/2024.1 atau lebih baru).
2. Biarkan Gradle sync selesai (project menggunakan AGP 8.5.2, Kotlin 1.9.24, Compose BOM 2024.06.00).
3. Jalankan konfigurasi `app` ke emulator atau perangkat Android 8.0 (API 26) ke atas.

> Catatan: kode ini disusun langsung tanpa proses build lokal di lingkungan pembuatan (tidak ada Android SDK tersedia di sana). Saat pertama kali dibuka di Android Studio, lakukan Gradle sync dan perbaiki bila ada ketidakcocokan versi API kecil sebelum run pertama.
