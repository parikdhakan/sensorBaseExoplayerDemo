package com.demo.app.exoplayer

import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import com.demo.app.databinding.ActivityWatchExoBinding
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.SeekParameters
import com.google.android.exoplayer2.SimpleExoPlayer

class WatchExoActivity : AppCompatActivity() {

    private val binding: ActivityWatchExoBinding by lazy {
        ActivityWatchExoBinding.inflate(layoutInflater)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)
        val simpleExoPlayer = SimpleExoPlayer.Builder(this)
            .setSeekParameters(SeekParameters(5000, 5000))
            .build()
        binding.exoPlayer.player = simpleExoPlayer
        binding.exoPlayer.keepScreenOn = true
        simpleExoPlayer.addListener(object : Player.Listener {
            override fun onPlayerStateChanged(playWhenReady: Boolean, playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    simpleExoPlayer.seekTo(0)
                    simpleExoPlayer.playWhenReady = true
                }
            }
        })
        val videoSource = Uri.parse("")
        val mediaItem = MediaItem.fromUri(videoSource)
        simpleExoPlayer.setMediaItem(mediaItem)
        simpleExoPlayer.prepare()
        simpleExoPlayer.play()
    }

}