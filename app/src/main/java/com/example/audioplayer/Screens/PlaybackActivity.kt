package com.example.audioplayer.Screens

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.audioplayer.Screens.EqualizerActivity
import com.example.audioplayer.Model.AudioService
import com.example.audioplayer.R
import com.example.audioplayer.ui.theme.WaveformView

class PlaybackActivity : AppCompatActivity() {

    private var audioService: AudioService? = null
    private var isBound = false

    private lateinit var songImage: ImageView
    private lateinit var songName: TextView
    private lateinit var seekBar: SeekBar
    private lateinit var txtCurrent: TextView
    private lateinit var txtDuration: TextView
    private lateinit var linearplay: LinearLayout
    private lateinit var waveformView: WaveformView
    private val handler = Handler(Looper.getMainLooper())

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioBinder
            audioService = binder.getService()
            isBound = true

            val sessionId = audioService?.getAudioSessionId() ?: 0
            if (sessionId != 0) {
                waveformView.attachToSession(sessionId)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_playback)

        songImage = findViewById(R.id.songimage)
        songName = findViewById(R.id.songName)
        seekBar = findViewById(R.id.seekBar)
        txtCurrent = findViewById(R.id.txtCurrent)
        txtDuration = findViewById(R.id.txtDuration)
        linearplay = findViewById(R.id.playlinear)
        linearplay.visibility= View.VISIBLE
        waveformView = findViewById(R.id.waveformView)
        val intent = Intent(this, AudioService::class.java)
        startService(intent)
        bindService(intent, connection, BIND_AUTO_CREATE)
        songName.isSelected = true
        val imgPause=findViewById<ImageView>(R.id.play)

        linearplay.setOnClickListener  {
            playSong(0)
            imgPause.setImageResource(R.drawable.pausebutton)
            linearplay.visibility= View.GONE
        }
        findViewById<ImageView>(R.id.btnEqualizer).setOnClickListener {
            startActivity(Intent(this, EqualizerActivity::class.java))
        }
        imgPause.setOnClickListener {

            if (audioService?.isPlaying() == true) {
                audioService?.pause()
                imgPause.setImageResource(R.drawable.playbutton)
            } else {
                audioService?.play()
                imgPause.setImageResource(R.drawable.pausebutton)
            }
        }


        findViewById<ImageView>(R.id.nextsong).setOnClickListener {
            audioService?.nextSong()
            updateUI()
            setupSeekBar()
            updatePlayButton()
        }

        findViewById<ImageView>(R.id.previousimg).setOnClickListener {
            audioService?.previousSong()
            updateUI()
            setupSeekBar()
            updatePlayButton()
        }
        seekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(
                seekBar: SeekBar?,
                progress: Int,
                fromUser: Boolean
            ) {
                if (fromUser) {
                    audioService?.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun playSong(index: Int) {
        audioService?.playSong(index)
        updateUI()
        setupSeekBar()
        updatePlayButton()

        val sessionId = audioService?.getAudioSessionId() ?: 0
        if (sessionId != 0) {
            waveformView.attachToSession(sessionId)
        }
    }
    private fun updatePlayButton() {

            if (audioService?.isPlaying() == true)
                findViewById<ImageView>(R.id.play).setImageResource(R.drawable.pausebutton)
            else
                findViewById<ImageView>(R.id.play).setImageResource(R.drawable.pausebutton)
    }
    private fun updateUI() {
        songName.text = audioService?.getSongName()
        songImage.setImageResource(audioService?.getSongImage() ?: 0)
    }

    private fun setupSeekBar() {
        val duration = audioService?.getDuration() ?: 0
        seekBar.max = duration
        txtDuration.text = formatTime(duration)

        handler.post(object : Runnable {
            override fun run() {
                val current = audioService?.getCurrentPosition() ?: 0
                seekBar.progress = current
                txtCurrent.text = formatTime(current)
                handler.postDelayed(this, 500)
            }
        })
    }

    private fun formatTime(ms: Int): String {
        val seconds = ms / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        if (isBound) {
            unbindService(connection)
        }

        stopService(Intent(this, AudioService::class.java)) //  stop service

        super.onDestroy()
    }
}