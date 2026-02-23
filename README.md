Android Audio Player
Foreground Service • Equalizer • Real-Time Waveform • Kotlin
Project Summary
--------------
This project is a modern Android Audio Player built using Kotlin, implementing:
* Foreground Service–based audio playback
* Real-time waveform visualization
* 5-band Equalizer with presets
* Persistent preset selection
* Splash screen
* Clean, responsive UI

The app demonstrates strong understanding of:
*Android Media APIs
*Service lifecycle management
*Binder communication
*Audio session handling
*Dynamic UI generation

Architecture Overview
---------------------
The application follows a Service-Oriented Architecture with clear separation between UI and playback logic.

Components
----------
1️ UI Layer
----------
* SplashActivity
* PlaybackActivity
* EqualizerActivity
  
2️ Service Layer
---------------
* AudioService
* Manages MediaPlayer
* Initializes Equalizer
* Runs as Foreground Service
* Exposes control methods via Binder

 Data Flow
 --------
SplashActivity
        ↓
PlaybackActivity
        ↓ (bindService)
AudioService (Foreground)
        ↓
MediaPlayer + Equalizer

Foreground Service Implementation
---------------------------------
Audio playback is handled inside AudioService.
</> code
startForeground(1, notification)

The service returns START_STICKY to improve resilience.

Equalizer Implementation
------------------------
The app integrates Android's built-in:
</>code
android.media.audiofx.Equalizer

Initialization
-------------
</> code
equalizer = Equalizer(0, mediaPlayer!!.audioSessionId).apply {
    enabled = true
}

Features
---------
* 5 frequency bands
* Vertical rotated SeekBars
* Custom UI created dynamically
* Preset support:
    Flat,Rock,Jazz,Classical,Pop,Vocal

Preset Persistence
-------------------
Uses SharedPreferences to save selected preset.

Waveform Implementation
-----------------------
Waveform visualization is connected using the MediaPlayer's audio session ID.
</>code
val sessionId = audioService?.getAudioSessionId()
waveformView.attachToSession(sessionId)

Approach
---------
1.MediaPlayer generates audio session.
2.WaveformView attaches to that session.
3.View reacts to amplitude changes.
4.Visualization updates in real-time.
This ensures waveform automatically switches when songs change.

UI Design Decisions
--------------------
* Dark theme optimized for music apps
* Purple accent for modern aesthetic
* Rotated vertical equalizer bars
* Clean separation of controls and visualizers
* Splash screen with gradient background

AI Tools Used
-------------
The following AI tools were used for:
* Architecture refinement
* UI enhancement suggestions
* Code optimization
* Equalizer and waveform integration guidance
*Documentation polishing

Verification
-------------
All AI-generated suggestions were:
* Manually implemented
* Tested on device
* Verified for correctness
* Refactored where necessary

 Testing & Validation
 ----------------------
The following scenarios were tested:
* Song switching (Next/Previous)
* Equalizer preset switching
* Preset persistence after restart
* Service lifecycle handling
* Notification persistence
* Waveform session reattachment
* App destruction and resource release

 Known Limitations
 ------------------
* Songs are loaded only from assets (no file picker)
* Notification lacks media controls
* Equalizer bands fixed to 5
* Waveform uses basic visualization (not full FFT)
* No MediaSession API integration yet

 Future Improvements
 -------------------
* MediaSession + notification controls
* Dynamic color extraction from album art
* Custom equalizer save profiles
* Advanced FFT-based waveform
* Background playback control buttons
* Smooth animated UI transitions
* Jetpack Compose migration
  
Build & Run Instructions
------------------------
Requirements
------------
1) Android Studio (Hedgehog or newer)
2) Minimum SDK 21+
3) Kotlin enabled
   
Steps
-----
1. Clone Repository
git clone https://github.com/jeenajosejr/Android-Audio-player
2. Open in Android Studio
Open project folder.
3. Sync Gradle
Allow dependencies to download.
4. Run App
Connect physical device or emulator and press  Run.
