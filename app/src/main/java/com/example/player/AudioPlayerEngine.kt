package com.example.player

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.PlaybackParams
import android.media.audiofx.BassBoost
import android.media.audiofx.Equalizer
import android.os.Build
import android.os.CountDownTimer
import android.util.Log
import com.example.data.model.RepeatMode
import com.example.data.model.Song
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class AudioPlayerEngine(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null
    private var equalizer: Equalizer? = null
    private var bassBoost: BassBoost? = null

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var progressJob: Job? = null
    private var sleepTimerJob: CountDownTimer? = null

    // UI States
    private val _currentSong = MutableStateFlow<Song?>(null)
    val currentSong = _currentSong.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying = _isPlaying.asStateFlow()

    private val _currentPositionMs = MutableStateFlow(0L)
    val currentPositionMs = _currentPositionMs.asStateFlow()

    private val _durationMs = MutableStateFlow(0L)
    val durationMs = _durationMs.asStateFlow()

    private val _repeatMode = MutableStateFlow(RepeatMode.ALL)
    val repeatMode = _repeatMode.asStateFlow()

    private val _isShuffle = MutableStateFlow(false)
    val isShuffle = _isShuffle.asStateFlow()

    private val _queue = MutableStateFlow<List<Song>>(emptyList())
    val queue = _queue.asStateFlow()

    private val _queueIndex = MutableStateFlow(-1)
    val queueIndex = _queueIndex.asStateFlow()

    private val _sleepTimerMinutesLeft = MutableStateFlow<Int?>(null)
    val sleepTimerMinutesLeft = _sleepTimerMinutesLeft.asStateFlow()

    private val _playbackSpeed = MutableStateFlow(1.0f)
    val playbackSpeed = _playbackSpeed.asStateFlow()

    private val _selectedPreset = MutableStateFlow("Normal")
    val selectedPreset = _selectedPreset.asStateFlow()

    private val _bassBoostStrength = MutableStateFlow(0.4f)
    val bassBoostStrength = _bassBoostStrength.asStateFlow()

    private val _volume = MutableStateFlow(1.0f)
    val volume = _volume.asStateFlow()

    val availablePresets = listOf("Normal", "Bass Boost", "Electronic", "Rock", "Pop", "Jazz", "Vocal Booster", "Arabesque Oud")

    fun playQueue(songs: List<Song>, startIndex: Int = 0) {
        if (songs.isEmpty()) return
        _queue.value = songs
        val validIndex = startIndex.coerceIn(0, songs.size - 1)
        _queueIndex.value = validIndex
        playSong(songs[validIndex])
    }

    fun playSong(song: Song) {
        try {
            mediaPlayer?.stop()
            mediaPlayer?.release()
            releaseAudioFx()

            _currentSong.value = song
            _currentPositionMs.value = 0L

            val player = MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .build()
                )
                setDataSource(context, song.contentUri)
                setOnPreparedListener { mp ->
                    _durationMs.value = mp.duration.toLong()
                    initAudioFx(mp.audioSessionId)
                    applyPlaybackSpeed(_playbackSpeed.value)
                    mp.start()
                    _isPlaying.value = true
                    startProgressTracking()
                    MusicPlaybackService.startOrUpdate(context, song, isPlaying = true)
                }
                setOnCompletionListener {
                    handleTrackCompletion()
                }
                setOnErrorListener { _, what, extra ->
                    Log.e("AudioPlayerEngine", "MediaPlayer error: what=$what, extra=$extra")
                    _isPlaying.value = false
                    false
                }
                prepareAsync()
            }
            mediaPlayer = player
        } catch (e: Exception) {
            Log.e("AudioPlayerEngine", "Error playing song: ${e.message}", e)
        }
    }

    fun togglePlayPause() {
        val player = mediaPlayer ?: run {
            _currentSong.value?.let { playSong(it) }
            return
        }

        if (player.isPlaying) {
            player.pause()
            _isPlaying.value = false
            stopProgressTracking()
            _currentSong.value?.let {
                MusicPlaybackService.startOrUpdate(context, it, isPlaying = false)
            }
        } else {
            player.start()
            _isPlaying.value = true
            startProgressTracking()
            _currentSong.value?.let {
                MusicPlaybackService.startOrUpdate(context, it, isPlaying = true)
            }
        }
    }

    fun seekTo(positionMs: Long) {
        mediaPlayer?.let { player ->
            try {
                player.seekTo(positionMs.toInt())
                _currentPositionMs.value = positionMs
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun playNext() {
        val q = _queue.value
        if (q.isEmpty()) return

        if (_isShuffle.value) {
            val nextIndex = (q.indices).random()
            _queueIndex.value = nextIndex
            playSong(q[nextIndex])
            return
        }

        val nextIndex = _queueIndex.value + 1
        if (nextIndex < q.size) {
            _queueIndex.value = nextIndex
            playSong(q[nextIndex])
        } else if (_repeatMode.value == RepeatMode.ALL) {
            _queueIndex.value = 0
            playSong(q[0])
        }
    }

    fun playPrevious() {
        val q = _queue.value
        if (q.isEmpty()) return

        // If playing for more than 3 seconds, restart current track
        if (_currentPositionMs.value > 3000) {
            seekTo(0)
            return
        }

        val prevIndex = _queueIndex.value - 1
        if (prevIndex >= 0) {
            _queueIndex.value = prevIndex
            playSong(q[prevIndex])
        } else if (_repeatMode.value == RepeatMode.ALL) {
            _queueIndex.value = q.size - 1
            playSong(q[q.size - 1])
        }
    }

    private fun handleTrackCompletion() {
        when (_repeatMode.value) {
            RepeatMode.ONE -> {
                seekTo(0)
                mediaPlayer?.start()
                _isPlaying.value = true
            }
            RepeatMode.ALL -> {
                playNext()
            }
            RepeatMode.OFF -> {
                val nextIndex = _queueIndex.value + 1
                if (nextIndex < _queue.value.size) {
                    playNext()
                } else {
                    _isPlaying.value = false
                    stopProgressTracking()
                    _currentSong.value?.let {
                        MusicPlaybackService.startOrUpdate(context, it, isPlaying = false)
                    }
                }
            }
        }
    }

    fun toggleShuffle() {
        _isShuffle.value = !_isShuffle.value
    }

    fun cycleRepeatMode() {
        _repeatMode.value = when (_repeatMode.value) {
            RepeatMode.OFF -> RepeatMode.ALL
            RepeatMode.ALL -> RepeatMode.ONE
            RepeatMode.ONE -> RepeatMode.OFF
        }
    }

    fun setPlaybackSpeed(speed: Float) {
        _playbackSpeed.value = speed
        applyPlaybackSpeed(speed)
    }

    private fun applyPlaybackSpeed(speed: Float) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                mediaPlayer?.playbackParams = PlaybackParams().apply {
                    this.speed = speed
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun setVolume(vol: Float) {
        val clamped = vol.coerceIn(0f, 1f)
        _volume.value = clamped
        mediaPlayer?.setVolume(clamped, clamped)
    }

    fun setEqualizerPreset(presetName: String) {
        _selectedPreset.value = presetName
        applyAudioFxPreset(presetName)
    }

    fun setBassBoost(strength: Float) {
        val clamped = strength.coerceIn(0f, 1f)
        _bassBoostStrength.value = clamped
        try {
            bassBoost?.let { bb ->
                bb.enabled = clamped > 0.05f
                bb.setStrength((clamped * 1000).toInt().toShort())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun initAudioFx(sessionId: Int) {
        try {
            equalizer = Equalizer(0, sessionId).apply {
                enabled = true
            }
            bassBoost = BassBoost(0, sessionId).apply {
                enabled = true
                setStrength((_bassBoostStrength.value * 1000).toInt().toShort())
            }
            applyAudioFxPreset(_selectedPreset.value)
        } catch (e: Exception) {
            Log.w("AudioPlayerEngine", "Audio FX initialization failed: ${e.message}")
        }
    }

    private fun applyAudioFxPreset(presetName: String) {
        try {
            val eq = equalizer ?: return
            val numBands = eq.numberOfBands
            if (numBands == 0.toShort()) return

            val minLevel = eq.bandLevelRange[0]
            val maxLevel = eq.bandLevelRange[1]
            val midLevel = (minLevel + maxLevel) / 2

            when (presetName) {
                "Bass Boost" -> {
                    if (numBands > 0) eq.setBandLevel(0, maxLevel)
                    if (numBands > 1) eq.setBandLevel(1, (maxLevel * 0.75).toInt().toShort())
                    bassBoost?.setStrength(1000)
                }
                "Electronic" -> {
                    if (numBands > 0) eq.setBandLevel(0, (maxLevel * 0.8).toInt().toShort())
                    if (numBands > 2) eq.setBandLevel((numBands - 1).toShort(), maxLevel)
                }
                "Rock" -> {
                    if (numBands > 0) eq.setBandLevel(0, (maxLevel * 0.7).toInt().toShort())
                    if (numBands > 1) eq.setBandLevel(1, (minLevel * 0.3).toInt().toShort())
                    if (numBands > 2) eq.setBandLevel((numBands - 1).toShort(), (maxLevel * 0.6).toInt().toShort())
                }
                "Pop" -> {
                    if (numBands > 0) eq.setBandLevel(0, (minLevel * 0.2).toInt().toShort())
                    if (numBands > 1) eq.setBandLevel(1, (maxLevel * 0.6).toInt().toShort())
                }
                "Jazz" -> {
                    if (numBands > 0) eq.setBandLevel(0, (maxLevel * 0.4).toInt().toShort())
                    if (numBands > 2) eq.setBandLevel((numBands - 1).toShort(), (maxLevel * 0.3).toInt().toShort())
                }
                "Vocal Booster" -> {
                    val midBand = (numBands / 2).toShort()
                    eq.setBandLevel(midBand, maxLevel)
                }
                "Arabesque Oud" -> {
                    if (numBands > 0) eq.setBandLevel(0, (maxLevel * 0.5).toInt().toShort())
                    val midBand = (numBands / 2).toShort()
                    eq.setBandLevel(midBand, (maxLevel * 0.7).toInt().toShort())
                    if (numBands > 2) eq.setBandLevel((numBands - 1).toShort(), (maxLevel * 0.4).toInt().toShort())
                }
                else -> { // Normal
                    for (i in 0 until numBands) {
                        eq.setBandLevel(i.toShort(), midLevel.toShort())
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun releaseAudioFx() {
        try {
            equalizer?.release()
            equalizer = null
            bassBoost?.release()
            bassBoost = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun startSleepTimer(minutes: Int) {
        cancelSleepTimer()
        val totalMs = minutes * 60 * 1000L
        _sleepTimerMinutesLeft.value = minutes

        sleepTimerJob = object : CountDownTimer(totalMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val mins = (millisUntilFinished / 60000).toInt() + 1
                _sleepTimerMinutesLeft.value = mins
            }

            override fun onFinish() {
                _sleepTimerMinutesLeft.value = null
                if (_isPlaying.value) {
                    togglePlayPause()
                }
            }
        }.start()
    }

    fun cancelSleepTimer() {
        sleepTimerJob?.cancel()
        sleepTimerJob = null
        _sleepTimerMinutesLeft.value = null
    }

    private fun startProgressTracking() {
        stopProgressTracking()
        progressJob = scope.launch {
            while (isActive) {
                mediaPlayer?.let { player ->
                    if (player.isPlaying) {
                        _currentPositionMs.value = player.currentPosition.toLong()
                    }
                }
                delay(300)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    fun release() {
        stopProgressTracking()
        cancelSleepTimer()
        mediaPlayer?.release()
        mediaPlayer = null
        releaseAudioFx()
        scope.cancel()
    }
}
