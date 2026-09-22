package id.ulartangga.keluarga.sound

import android.media.AudioManager
import android.media.ToneGenerator

enum class SoundEvent { TICK, STEP, LADDER, SNAKE, CARD, WIN }

class SoundManager {
    private val toneGenerator = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }.getOrNull()

    fun play(event: SoundEvent) {
        val tg = toneGenerator ?: return
        when (event) {
            // ToneGenerator hanya punya preset nada telepon (tidak ada nada bel asli); TONE_CDMA_PIP
            // yang pendek & bernada tinggi ini yang paling mendekati bunyi "cling" bel sepeda. Dipanggil
            // berulang cepat saat dadu "dikocok" supaya terdengar seperti getaran/kocokan, bukan satu ketuk.
            SoundEvent.TICK -> tg.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
            // Langkah kaki pion per kotak: nada pendek & lembut, tidak mengganggu.
            SoundEvent.STEP -> tg.startTone(ToneGenerator.TONE_PROP_BEEP, 60)
            // Naik tangga: nada "confirm" yang naik/positif.
            SoundEvent.LADDER -> tg.startTone(ToneGenerator.TONE_CDMA_CONFIRM, 200)
            // Kena ular: nada error/negatif, sengaja dibuat jelas berbeda dari nada tangga.
            SoundEvent.SNAKE -> tg.startTone(ToneGenerator.TONE_SUP_ERROR, 200)
            SoundEvent.CARD -> tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
            SoundEvent.WIN -> tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 500)
        }
    }

    fun release() {
        toneGenerator?.release()
    }
}
