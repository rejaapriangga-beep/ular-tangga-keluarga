package id.ulartangga.keluarga.data

import android.content.Context
import id.ulartangga.keluarga.game.PowerCardType
import java.time.LocalDate

class DailyRewardManager(context: Context) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    data class ClaimResult(val streak: Int, val reward: PowerCardType)

    /** Mengembalikan hadiah baru jika belum diambil hari ini, atau null kalau sudah. */
    fun claimIfAvailable(): ClaimResult? {
        val today = LocalDate.now().toEpochDay()
        val lastClaim = prefs.getLong(KEY_LAST_CLAIM, Long.MIN_VALUE)
        if (lastClaim == today) return null

        val previousStreak = prefs.getInt(KEY_STREAK, 0)
        val newStreak = if (lastClaim == today - 1) previousStreak + 1 else 1

        prefs.edit()
            .putLong(KEY_LAST_CLAIM, today)
            .putInt(KEY_STREAK, newStreak)
            .apply()

        val reward = PowerCardType.values()[(newStreak - 1) % PowerCardType.values().size]
        return ClaimResult(newStreak, reward)
    }

    companion object {
        private const val PREFS_NAME = "daily_reward"
        private const val KEY_LAST_CLAIM = "last_claim_epoch_day"
        private const val KEY_STREAK = "streak"
    }
}
