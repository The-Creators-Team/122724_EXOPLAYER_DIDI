/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
* limitations under the License.
 */
package com.example.exoplayer

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player

import androidx.media3.common.util.UnstableApi
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.ExoPlayer
import com.example.exoplayer.databinding.ActivityPlayerBinding

private const val TAG = "PlayerActivity"
/**
 * A fullscreen activity to play audio or video streams.
 */
class PlayerActivity : AppCompatActivity() {

  private val playbackStateListener: Player.Listener = playbackStateListener()

  private val viewBinding by lazy(LazyThreadSafetyMode.NONE) {
    ActivityPlayerBinding.inflate(layoutInflater)
  }

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(viewBinding.root)
  }

  private var player: ExoPlayer? = null

  private fun initializePlayer() {
    player = ExoPlayer.Builder(this)
      .build()
      .also { exoPlayer ->
        exoPlayer.trackSelectionParameters = exoPlayer.trackSelectionParameters
          .buildUpon()
          .setMaxVideoSizeSd()
          .build()

        viewBinding.videoView.player = exoPlayer

        val mediaItem = MediaItem.Builder()
          .setUri(getString(R.string.media_url_dash))
          .setMimeType(MimeTypes.APPLICATION_MPD)
          .build()
        exoPlayer.setMediaItems(listOf(mediaItem), mediaItemIndex, playbackPosition)
        exoPlayer.playWhenReady = playWhenReady
        exoPlayer.addListener(playbackStateListener)
        exoPlayer.prepare()
      }
  }

  @OptIn(UnstableApi::class)
  public override fun onStart() {
    super.onStart()
    if (Util.SDK_INT > 23) {
      initializePlayer()
    }
  }

  @OptIn(UnstableApi::class)
  public override fun onResume() {
    super.onResume()
    hideSystemUi()
    if ((Util.SDK_INT <= 23 || player == null)) {
      initializePlayer()
    }
  }

  @SuppressLint("InlinedApi")
  private fun hideSystemUi() {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    WindowInsetsControllerCompat(window, viewBinding.videoView).let { controller ->
      controller.hide(WindowInsetsCompat.Type.systemBars())
      controller.systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
    }
  }

  @OptIn(UnstableApi::class)
  public override fun onPause() {
    super.onPause()
    if (Util.SDK_INT <= 23) {
      releasePlayer()
    }
  }

  @OptIn(UnstableApi::class)
  public override fun onStop() {
    super.onStop()
    if (Util.SDK_INT > 23) {
      releasePlayer()
    }
  }

  private var playWhenReady = true
  private var mediaItemIndex = 0
  private var playbackPosition = 0L

  private fun releasePlayer() {
    player?.let { exoPlayer ->
      playbackPosition = exoPlayer.currentPosition
      mediaItemIndex = exoPlayer.currentMediaItemIndex
      playWhenReady = exoPlayer.playWhenReady
      exoPlayer.removeListener(playbackStateListener)
      exoPlayer.release()
    }
    player = null
  }
}

private fun playbackStateListener() = object : Player.Listener {
  override fun onPlaybackStateChanged(playbackState: Int) {
    val stateString: String = when (playbackState) {
      ExoPlayer.STATE_IDLE -> "ExoPlayer.STATE_IDLE      -"
      ExoPlayer.STATE_BUFFERING -> "ExoPlayer.STATE_BUFFERING -"
      ExoPlayer.STATE_READY -> "ExoPlayer.STATE_READY     -"
      ExoPlayer.STATE_ENDED -> "ExoPlayer.STATE_ENDED     -"
      else -> "UNKNOWN_STATE             -"
    }
    Log.d(TAG, "changed state to $stateString")
  }
}