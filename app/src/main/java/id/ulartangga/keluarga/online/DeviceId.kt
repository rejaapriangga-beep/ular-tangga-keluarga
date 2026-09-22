package id.ulartangga.keluarga.online

import android.content.Context
import android.provider.Settings
import java.util.UUID

/** Id stabil per instalasi app, dipakai sebagai key pemain di room online. */
object DeviceId {
    private const val PREFS_NAME = "device_id"
    private const val KEY_ID = "id"

    fun get(context: Context): String {
        val androidId = Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
        if (!androidId.isNullOrBlank() && androidId != "9774d56d682e549c") {
            return sanitize(androidId)
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val existing = prefs.getString(KEY_ID, null)
        if (existing != null) return existing

        val generated = sanitize(UUID.randomUUID().toString())
        prefs.edit().putString(KEY_ID, generated).apply()
        return generated
    }

    private fun sanitize(raw: String) = raw.replace(Regex("[^A-Za-z0-9]"), "")
}
