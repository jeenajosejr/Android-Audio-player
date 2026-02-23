package com.example.audioplayer.Model

import android.app.*
import android.content.Intent
import android.media.MediaPlayer
import android.media.audiofx.Equalizer
import android.os.*
import androidx.core.app.NotificationCompat
import com.example.audioplayer.R

class AudioService : Service() {

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null

    private val binder = AudioBinder()
    private val CHANNEL_ID = "audio_channel"

    private val songList = listOf(
        "audio/song3.mp3",
        "audio/song2.mp3",
        "audio/song1.mp3"
    )

    private val songNames = listOf(
        "Maha Ganapatim Manasa Smarami With Lyrics | Popular Devotional Ganpati Songs | Rajshri Soul",
        "Christian Prayer Song | Semiclassical Dance Song ",
        "Nilaa Kaayum Lyric Video | Kalamkaval | Mammootty | Jithin K Jose | Mujeeb Majeed | MammoottyKampany"
    )

    private val songImages = listOf(
        R.drawable.ganapthi,
        R.drawable.jesus,
        R.drawable.kalamkaval
    )

    private var currentIndex = 0

    inner class AudioBinder : Binder() {
        fun getService(): AudioService = this@AudioService
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        createNotificationChannel()

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Audio Player")
            .setContentText("Playing music...")
            .setSmallIcon(R.mipmap.ic_launcher)
            .build()

        startForeground(1, notification)

        return START_STICKY
    }

    fun playSong(index: Int) {
        currentIndex = index
        mediaPlayer?.release()

        try {
            val afd = assets.openFd(songList[currentIndex])

            mediaPlayer = MediaPlayer().apply {
                setDataSource(afd.fileDescriptor, afd.startOffset, afd.length)
                prepare()
                start()
            }

            // Initialize Equalizer
            equalizer?.release()
            equalizer = Equalizer(0, mediaPlayer!!.audioSessionId).apply {
                enabled = true
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun pause() = mediaPlayer?.pause()
    fun play() = mediaPlayer?.start()
    fun replay() {
        mediaPlayer?.seekTo(0)
        mediaPlayer?.start()
    }

    fun isPlaying(): Boolean = mediaPlayer?.isPlaying == true

    fun seekTo(position: Int) {
        mediaPlayer?.seekTo(position)
    }

    fun getDuration(): Int = mediaPlayer?.duration ?: 0
    fun getCurrentPosition(): Int = mediaPlayer?.currentPosition ?: 0
    fun getSongName(): String = songNames[currentIndex]
    fun getSongImage(): Int = songImages[currentIndex]

    fun nextSong() {
        currentIndex = (currentIndex + 1) % songList.size
        playSong(currentIndex)
    }

    fun previousSong() {
        currentIndex =
            if (currentIndex - 1 < 0) songList.size - 1
            else currentIndex - 1
        playSong(currentIndex)
    }
    fun getAudioSessionId(): Int {
        return mediaPlayer?.audioSessionId ?: 0
    }
    // ================== EQUALIZER FUNCTIONS ==================

    fun getBandLevelRange(): ShortArray {
        return equalizer?.bandLevelRange ?: shortArrayOf(-1500, 1500)
    }

    fun getNumberOfBands(): Short {
        return equalizer?.numberOfBands ?: 5
    }

    fun setBandLevel(band: Short, level: Short) {
        equalizer?.setBandLevel(band, level)
    }

    fun getBandLevel(band: Short): Short {
        return equalizer?.getBandLevel(band) ?: 0
    }

    // ==========================================================

    override fun onDestroy() {
        equalizer?.release()
        mediaPlayer?.release()
        super.onDestroy()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Audio Playback",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }
}