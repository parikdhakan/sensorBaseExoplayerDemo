package com.demo.app.media3

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.content.res.Configuration
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.Player
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import com.demo.app.R
import com.demo.app.databinding.ActivityWatchBinding
import com.demo.app.util.setMediaItemExt


class WatchActivity : AppCompatActivity(), SensorEventListener {
    private lateinit var sensorManager: SensorManager
    var pValue: Int = 0
    var isPortrait = false

    companion object {
        val TAG = WatchActivity::class.java.simpleName
    }

    private var isFullScreen = false

    private val mainViewModel: MainViewModel by viewModels()

    private val binding by lazy(LazyThreadSafetyMode.NONE) {
        ActivityWatchBinding.inflate(layoutInflater)
    }

    private val playbackStateListener: Player.Listener = playbackStateListener()
    private var player: ExoPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val fullscreenButton = binding.videoView.findViewById<ImageView>(R.id.exo_fullscreen_icon)
        fullscreenButton.setOnClickListener {
            if (isFullScreen) {
                supportActionBar?.show()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                isFullScreen = false
                fullscreenButton.setImageResource(R.drawable.ic_baseline_fullscreen)
            } else {
                supportActionBar?.hide()
                requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                isFullScreen = true
                fullscreenButton.setImageResource(R.drawable.ic_baseline_fullscreen_exit)
            }
        }
//        getListener()

    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        // Checks the orientation of the screen
        if (newConfig.orientation === Configuration.ORIENTATION_LANDSCAPE) {
            Log.e("sensor_orientation", "landscape")
//            Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show()
            isPortrait = true
        } else if (newConfig.orientation === Configuration.ORIENTATION_PORTRAIT) {
//            Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show()
            Log.e("sensor_orientation", "portrait")
            isPortrait = false

        }
    }

    private fun getListener() {
        player!!.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {

                if (isPlaying) {
                    Log.e("event_details", "isPlaying: playing")
//                  // Active playback.
                } else {
                    Log.e("event_details", "isPlaying: Not playing")
//        // Not playing because playback is paused, ended, suppressed, or the player
//        // is buffering, stopped or failed. Check player.getPlaybackState,
//        // player.getPlayWhenReady, player.getPlaybackError and
//        // player.getPlaybackSuppressionReason for details.
                }
            }
        })
    }
/*
    private fun mediaVideo(): MutableList<String> {
        val data = mutableListOf<String>()
        data.add(getString(R.string.media_url_jwp_1))
        data.add(getString(R.string.media_url_jwp_2))
        data.add(getString(R.string.media_url_mp4))
        data.add(getString(R.string.media_url_mp3))
        data.add(getString(R.string.media_url_mp4_from_youtube_1))
        data.add(getString(R.string.media_url_mp4_from_youtube_2))
        data.add(getString(R.string.media_url_mp4_from_youtube_3))
        return data
    }*/

    private fun setupExoPlayerByViewModel(
        exoPlayer: ExoPlayer,
        listener: Player.Listener,
    ) {
        mainViewModel.apply {
            playWhenReady.observe(this@WatchActivity) {
                exoPlayer.playWhenReady = it
            }
            seekExoPlayer.observe(this@WatchActivity) {
                exoPlayer.seekTo(it.currentItem, it.playBackPosition)
            }
        }
        exoPlayer.addListener(listener)
        exoPlayer.addAnalyticsListener(object : AnalyticsListener {
            override fun onPlaybackStateChanged(
                eventTime: AnalyticsListener.EventTime,
                state: Int,
            ) {
                super.onPlaybackStateChanged(eventTime, state)
                Log.e("event_details", "onPlaybackStateChanged: $state")
            }
        })
        exoPlayer.prepare()
        setUpSensorStuff()

    }

    public override fun onStart() {
        super.onStart()
        if (Util.SDK_INT > 23) {
            initializePlayer()
        }
    }

    public override fun onResume() {
        super.onResume()
        hideSystemUi()
        if (Util.SDK_INT <= 23 || player == null) {
            initializePlayer()
        }
    }

    public override fun onPause() {
        super.onPause()
        if (Util.SDK_INT <= 23) {
            releasePlayer()
        }
    }

    public override fun onStop() {
        super.onStop()
        if (Util.SDK_INT > 23) {
            releasePlayer()
        }
    }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(this)
            .build()
            .also { exoPlayer ->
                binding.videoView.player = exoPlayer

                // Setup Media Single Video
                exoPlayer.setMediaItemExt(getString(R.string.media_url_mp4))

                // Setup Media Multiple Video
//                exoPlayer.setMediaSourceExt(getString(R.string.media_url_jwp_2))

                // Setup Media Single Video Youtube Url
                // exoPlayer.setMediaItemExtYT(getString(R.string.media_url_dash))

                // Default setup
                setupExoPlayerByViewModel(exoPlayer, playbackStateListener)
            }
        getListener()
    }

    private fun releasePlayer() {
        player?.let { exoPlayer ->
            mainViewModel.setSeekExoPlayer(
                SeekExoPlayer(
                    exoPlayer.currentMediaItemIndex,
                    exoPlayer.currentPosition
                )
            )
            mainViewModel.setPlayWhenReady(exoPlayer.playWhenReady)
            exoPlayer.removeListener(playbackStateListener)
            exoPlayer.release()
        }
        player = null
    }

    @SuppressLint("InlinedApi")
    private fun hideSystemUi() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(window, binding.videoView).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    override fun onBackPressed() {
        val position = (player?.currentPosition ?: 0)
        Log.d(TAG, "Position : $position")
        super.onBackPressed()
    }

    private fun playbackStateListener() = object : Player.Listener {
        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.e("event_details", playbackState.toString())
            when (playbackState) {
                ExoPlayer.STATE_IDLE -> {

                    Log.e("event_details", "ExoPlayer.STATE_IDLE")
                    Log.e("event_details", "Duration : ${player?.duration}")
                    val position = (player?.currentPosition ?: 0)
                    Log.e("event_details", "Position : $position")
                }
                ExoPlayer.STATE_BUFFERING -> {
                    Log.e("event_details", "ExoPlayer.STATE_BUFFERING")
                    Log.e("event_details", "Duration : ${player?.duration}")
                    val position = (player?.currentPosition ?: 0)
                    Log.e("event_details", "Position : $position")
                }
                ExoPlayer.STATE_READY -> {
                    Log.e("event_details", "ExoPlayer.STATE_READY")
                    Log.e("event_details", "Duration : ${player?.duration}")
                    val position = (player?.currentPosition ?: 0)
                    Log.e("event_details", "Position : $position")
                }
                ExoPlayer.STATE_ENDED -> {
                    Log.e("event_details", "ExoPlayer.STATE_ENDED")
                    Log.e("event_details", "Duration : ${player?.duration}")
                    val position = (player?.currentPosition ?: 0)
                    Log.e("event_details", "Position : $position")
                }
                else -> {
                    Log.e("event_details", "Unknown state")
                }

            }

        }

    }


    private fun setUpSensorStuff() {
        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager

        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)?.also {
            sensorManager.registerListener(
                this,
                it,
                SensorManager.SENSOR_DELAY_FASTEST,
                SensorManager.SENSOR_DELAY_FASTEST
            )
        }
    }

    override fun onSensorChanged(event: SensorEvent?) {
        if (event?.sensor?.type == Sensor.TYPE_ACCELEROMETER) {
            val sides = event.values[0]
            val upDown = event.values[1]

            /* square.apply {
                 rotationX = upDown * 3f
                 rotationY = sides * 3f
                 rotation = -sides
                 translationX = sides * -10
                 translationY = upDown * 10
             }*/
//            val color = if (upDown.toInt() == 0 && sides.toInt() == 0) Color.GREEN else Color.RED
//            square.setBackgroundColor(color)
//            Log.e("sensor_details", "sides: " + sides.toDouble() + ",upDown: " + upDown.toDouble())
            if (sides.toDouble() > 5.0000) {
                Log.e("sensor", "=====LEFT====" + sides.toDouble());
                Log.e("sensor_Playing", "status:" + isPlaying())
                if (isPortrait && upDown.toDouble() < -4) {
                    Log.e("cValues", "Left side Down $upDown.toDouble()")
                    if (player != null && !isPlaying()) {
                        Log.e("sensor_Playing", "Playing. Now playing")
                        player!!.setPlayWhenReady(true);
                        pValue = sides.toInt()
                    }
//                Log.e("cValues", "sides: " + sides.toDouble() + ",upDown: " + upDown.toDouble())

                } else if (isPortrait && upDown.toDouble() > 4) {
                    Log.e("cValues", "Right side Down $upDown.toDouble()")
                    if (player != null && isPlaying()) {
                        Log.e("sensor_Playing", "Playing. Now stop");
                        player!!.setPlayWhenReady(false);
                    }
//                Log.e("cValues", "sides: " + sides.toDouble() + ",upDown: " + upDown.toDouble())
                } else if (sides.toInt() > pValue && player != null && !isPlaying()) {
                    Log.e("sensor_Playing", "Playing. Now playing")
                    player!!.setPlayWhenReady(true);
                    pValue = sides.toInt()
                }
            } else if (sides.toDouble() != 0.0 && sides.toDouble() < -5.0000) {
                Log.e("sensor", "=====RIGHT====" + sides.toDouble());
                Log.e("sensor_Playing", "status:" + isPlaying())
                if (isPortrait && upDown.toDouble() < -4) {
                    Log.e("cValues", "Left side Down $upDown.toDouble()")
                    if (player != null && isPlaying()) {
                        Log.e("sensor_Playing", "Playing. Now stop");
                        player!!.setPlayWhenReady(false);
                    }
//                Log.e("cValues", "sides: " + sides.toDouble() + ",upDown: " + upDown.toDouble())

                } else if (isPortrait && upDown.toDouble() > 4) {
                    Log.e("cValues", "Right side Down $upDown.toDouble()")
                    if (player != null && !isPlaying()) {
                        Log.e("sensor_Playing", "Playing. Now playing")
                        player!!.setPlayWhenReady(true);
                        pValue = sides.toInt()
                    }
//                Log.e("cValues", "sides: " + sides.toDouble() + ",upDown: " + upDown.toDouble())
                } else if (player != null && isPlaying()) {
                    Log.e("sensor_Playing", "Playing. Now stop");
                    player!!.setPlayWhenReady(false);
                    pValue = sides.toInt()
                }
            } else if (upDown.toDouble() > 5/* && upDown.toDouble() < 8.0 && upDown.toDouble() > -8.0*/) {
                Log.e("sensor", "=====UP====" + upDown.toInt());
//                changeOrientation()
            } else if (upDown.toDouble() != 0.0 && upDown.toDouble() < -5.0) {
                Log.e("sensor", "=====DOWN====" + upDown.toDouble());
            }
//            square.text = "up/down ${upDown.toInt()}\nleft/ right ${sides.toInt()}"
        }
    }

    private fun changeOrientation() {
        requestedOrientation = if (isPortrait) {
            ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
        } else {
            ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
        }

        // opposite the value of isPortrait
        isPortrait = !isPortrait
    }

    private fun isPlaying(): Boolean {
        return player != null && player!!.playbackState != Player.STATE_ENDED && player!!.playbackState != Player.STATE_IDLE && player!!.playWhenReady
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        return
    }

    override fun onDestroy() {
        sensorManager.unregisterListener(this)
        super.onDestroy()
    }

}