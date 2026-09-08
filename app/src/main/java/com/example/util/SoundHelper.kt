package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager

class SoundHelper(context: Context) {
    private val vibrator: Vibrator? = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
        }
    } catch (_: Exception) {
        null
    }

    private var toneGenerator: ToneGenerator? = try {
        ToneGenerator(AudioManager.STREAM_MUSIC, 65)
    } catch (_: Exception) {
        null
    }

    fun playTone(type: ToneType) {
        try {
            when (type) {
                ToneType.TAP -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP, 30)
                    vibrate(15)
                }
                ToneType.COIN -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_PIP, 50)
                    vibrate(25)
                }
                ToneType.POWERUP -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_CDMA_ALERT_CALL_GUARD, 100)
                    vibrate(40)
                }
                ToneType.HIT -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_PROMPT, 120)
                    vibrate(70)
                }
                ToneType.GAMEOVER -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 200)
                    vibrate(120)
                }
                ToneType.VICTORY -> {
                    toneGenerator?.startTone(ToneGenerator.TONE_PROP_ACK, 250)
                    vibrate(60)
                }
            }
        } catch (_: Exception) {
            // Ignore sound errors gracefully
        }
    }

    private fun vibrate(durationMs: Long) {
        try {
            if (vibrator?.hasVibrator() == true) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vibrator.vibrate(
                        VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE)
                    )
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(durationMs)
                }
            }
        } catch (_: Exception) {
            // Ignore vibration errors gracefully
        }
    }

    fun release() {
        try {
            toneGenerator?.release()
            toneGenerator = null
        } catch (_: Exception) {
        }
    }
}

enum class ToneType {
    TAP, COIN, POWERUP, HIT, GAMEOVER, VICTORY
}
