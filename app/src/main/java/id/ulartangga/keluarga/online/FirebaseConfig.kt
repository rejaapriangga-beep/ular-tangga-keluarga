package id.ulartangga.keluarga.online

/**
 * Isi nilai di bawah ini dengan konfigurasi project Firebase kamu sendiri
 * (Firebase Console -> Project settings -> General -> Your apps -> Web app "Config").
 * Realtime Database saja yang dipakai, jadi tidak perlu google-services.json:
 * FirebaseApp diinisialisasi manual pakai nilai-nilai ini (lihat FirebaseBootstrap.kt).
 *
 * Sampai nilai-nilai ini diisi, fitur Main Online tidak akan bisa dipakai
 * (isConfigured = false) tapi sisa aplikasi tetap berjalan normal.
 */
object FirebaseConfig {
    const val API_KEY = ""
    const val APPLICATION_ID = ""
    const val PROJECT_ID = ""
    const val DATABASE_URL = ""

    val isConfigured: Boolean
        get() = API_KEY.isNotBlank() && APPLICATION_ID.isNotBlank() &&
            PROJECT_ID.isNotBlank() && DATABASE_URL.isNotBlank()
}
