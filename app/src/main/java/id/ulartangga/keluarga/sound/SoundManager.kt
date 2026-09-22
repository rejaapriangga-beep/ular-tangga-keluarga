package id.ulartangga.keluarga.sound

import android.media.AudioManager
import android.media.ToneGenerator

enum class SoundEvent { TICK, LADDER, SNAKE, CARD, WIN }

class SoundManager {
    private val toneGenerator = runCatching { ToneGenerator(AudioManager.STREAM_MUSIC, 80) }.getOrNull()

    fun play(event: SoundEvent) {
        val tg = toneGenerator ?: return
        when (event) {
            // ToneGenerator hanya punya preset nada telepon (tidak ada nada bel asli); TONE_CDMA_PIP
            // yang pendek & bernada tinggi ini yang paling mendekati bunyi "cling" bel sepeda.
            SoundEvent.TICK -> tg.startTone(ToneGenerator.TONE_CDMA_PIP, 100)
            SoundEvent.LADDER -> tg.startTone(ToneGenerator.TONE_PROP_ACK, 180)
            SoundEvent.SNAKE -> tg.startTone(ToneGenerator.TONE_PROP_NACK, 180)
            SoundEvent.CARD -> tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 150)
            SoundEvent.WIN -> tg.startTone(ToneGenerator.TONE_PROP_PROMPT, 500)
        }
    }

    fun release() {
        toneGenerator?.release()
    }
}
