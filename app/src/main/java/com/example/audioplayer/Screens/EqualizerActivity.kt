package com.example.audioplayer.Screens

import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.IBinder
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.audioplayer.Model.AudioService
import com.example.audioplayer.R

class EqualizerActivity : AppCompatActivity() {

    private var audioService: AudioService? = null
    private var isBound = false
    private lateinit var bandContainer: LinearLayout

    private val presets = mapOf(
        "Flat" to shortArrayOf(0, 0, 0, 0, 0),
        "Rock" to shortArrayOf(300, 200, 0, 200, 300),
        "Jazz" to shortArrayOf(0, 200, 300, 200, 0),
        "Classical" to shortArrayOf(300, 0, 0, 0, 300),
        "Pop" to shortArrayOf(-100, 200, 300, 200, -100),
        "Vocal" to shortArrayOf(-200, 0, 300, 0, -200)
    )

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as AudioService.AudioBinder
            audioService = binder.getService()
            isBound = true
            setupEqualizer()
            loadPreset()
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            isBound = false
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_equalizer)

        bandContainer = findViewById(R.id.bandContainer)

        val intent = Intent(this, AudioService::class.java)
        bindService(intent, connection, BIND_AUTO_CREATE)

        // Preset Buttons
        findViewById<Button>(R.id.btnFlat).setOnClickListener { applyPreset("Flat") }
        findViewById<Button>(R.id.btnRock).setOnClickListener { applyPreset("Rock") }
        findViewById<Button>(R.id.btnJazz).setOnClickListener { applyPreset("Jazz") }
        findViewById<Button>(R.id.btnClassical).setOnClickListener { applyPreset("Classical") }
        findViewById<Button>(R.id.btnPop).setOnClickListener { applyPreset("Pop") }
        findViewById<Button>(R.id.btnVocal).setOnClickListener { applyPreset("Vocal") }
    }

    private fun setupEqualizer() {

        if (!isBound) return

        val bands = audioService?.getNumberOfBands() ?: 5
        val range = audioService?.getBandLevelRange() ?: return

        val frequencies = listOf("60Hz", "230Hz", "910Hz", "3.6kHz", "14kHz")

        bandContainer.removeAllViews()

        for (i in 0 until bands) {

            val container = LinearLayout(this)
            container.orientation = LinearLayout.VERTICAL
            container.layoutParams =
                LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1f)
            container.gravity = Gravity.CENTER

            val seekBar = SeekBar(this)
            seekBar.rotation = -90f
            seekBar.max = range[1] - range[0]
            seekBar.progress =
                audioService?.getBandLevel(i.toShort())?.minus(range[0]) ?: 0

            seekBar.layoutParams =
                LinearLayout.LayoutParams(300, 80)
            val accentColor = Color.parseColor("#BB86FC")
            val backgroundColor = Color.parseColor("#FFFFFF")
            val thumbColor = Color.parseColor("#FFFFFF")

            seekBar.progressTintList = ColorStateList.valueOf(accentColor)
            seekBar.progressBackgroundTintList = ColorStateList.valueOf(backgroundColor)
            seekBar.thumbTintList = ColorStateList.valueOf(thumbColor)
            seekBar.setOnSeekBarChangeListener(object :
                SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(
                    sb: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    if (fromUser) {
                        val level = (progress + range[0]).toShort()
                        audioService?.setBandLevel(i.toShort(), level)
                    }
                }

                override fun onStartTrackingTouch(sb: SeekBar?) {}
                override fun onStopTrackingTouch(sb: SeekBar?) {}
            })

            val label = TextView(this)
            label.text = frequencies[i]

            container.addView(seekBar)
            container.addView(label)

            bandContainer.addView(container)
        }
    }

    private fun applyPreset(name: String) {

        val preset = presets[name] ?: return

        for (i in preset.indices) {
            audioService?.setBandLevel(i.toShort(), preset[i])
        }

        savePreset(name)
        setupEqualizer()
    }

    private fun savePreset(name: String) {
        val prefs = getSharedPreferences("equalizer", MODE_PRIVATE)
        prefs.edit().putString("selected_preset", name).apply()
    }

    private fun loadPreset() {
        val prefs = getSharedPreferences("equalizer", MODE_PRIVATE)
        val preset = prefs.getString("selected_preset", "Flat")
        applyPreset(preset ?: "Flat")
    }

    override fun onDestroy() {
        if (isBound) unbindService(connection)
        super.onDestroy()
    }
}