package com.example.tv.service

import android.content.res.AssetFileDescriptor
import android.service.wallpaper.WallpaperService
import android.util.Log
import android.view.KeyEvent
import android.view.SurfaceHolder
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.tv.manager.VideoManager
import com.example.tv.model.VideoSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.SupervisorJob

class VideoWallpaperService : WallpaperService() {
    
    private val TAG = "VideoWallpaperService"
    
    override fun onCreateEngine(): Engine {
        return VideoWallpaperEngine()
    }
    
    inner class VideoWallpaperEngine : Engine() {
        
        private var player: ExoPlayer? = null
        private lateinit var videoManager: VideoManager
        private val serviceJob = SupervisorJob()
        private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
        
        override fun onCreate(surfaceHolder: SurfaceHolder?) {
            super.onCreate(surfaceHolder)
            Log.d(TAG, "Engine onCreate")
            
            // 初始化视频管理器
            videoManager = VideoManager(this@VideoWallpaperService)
            
            // 异步加载远程视频
            serviceScope.launch {
                videoManager.loadRemoteVideos()
                // 加载完成后，如果当前没有播放视频，则开始播放
                if (player == null || player?.isPlaying == false) {
                    playCurrentVideo()
                }
            }
            
            // 设置触摸事件监听（用于接收按键事件）
            setTouchEventsEnabled(false)
        }
        
        override fun onSurfaceCreated(holder: SurfaceHolder?) {
            super.onSurfaceCreated(holder)
            Log.d(TAG, "Surface created")
            initializePlayer(holder)
            playCurrentVideo()
        }
        
        override fun onSurfaceChanged(
            holder: SurfaceHolder?,
            format: Int,
            width: Int,
            height: Int
        ) {
            super.onSurfaceChanged(holder, format, width, height)
            Log.d(TAG, "Surface changed: ${width}x${height}")
        }
        
        override fun onSurfaceDestroyed(holder: SurfaceHolder?) {
            super.onSurfaceDestroyed(holder)
            Log.d(TAG, "Surface destroyed")
            releasePlayer()
        }
        
        override fun onVisibilityChanged(visible: Boolean) {
            super.onVisibilityChanged(visible)
            Log.d(TAG, "Visibility changed: $visible")
            if (visible) {
                player?.play()
            } else {
                player?.pause()
            }
        }
        
        override fun onDestroy() {
            super.onDestroy()
            Log.d(TAG, "Engine onDestroy")
            releasePlayer()
            serviceJob.cancel()
        }
        
        override fun onCommand(
            action: String?,
            x: Int,
            y: Int,
            z: Int,
            extras: android.os.Bundle?,
            resultRequested: Boolean
        ): android.os.Bundle? {
            // 处理外部命令，可用于切换视频
            when (action) {
                "NEXT_VIDEO" -> switchToNextVideo()
                "PREVIOUS_VIDEO" -> switchToPreviousVideo()
            }
            return super.onCommand(action, x, y, z, extras, resultRequested)
        }
        
        private fun initializePlayer(holder: SurfaceHolder?) {
            if (player == null) {
                player = ExoPlayer.Builder(this@VideoWallpaperService).build().apply {
                    repeatMode = Player.REPEAT_MODE_ONE
                    volume = 0f // 静音播放
                    setVideoSurfaceHolder(holder)
                    
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_READY -> {
                                    Log.d(TAG, "Player ready")
                                    play()
                                }
                                Player.STATE_ENDED -> {
                                    Log.d(TAG, "Playback ended")
                                }
                                Player.STATE_BUFFERING -> {
                                    Log.d(TAG, "Buffering...")
                                }
                                Player.STATE_IDLE -> {
                                    Log.d(TAG, "Player idle")
                                }
                            }
                        }
                        
                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.e(TAG, "Player error: ${error.message}", error)
                            // 如果播放出错，尝试播放下一个视频
                            switchToNextVideo()
                        }
                    })
                }
                Log.d(TAG, "Player initialized")
            }
        }
        
        private fun playCurrentVideo() {
            val currentVideo = videoManager.getCurrentVideo()
            if (currentVideo != null) {
                playVideo(currentVideo)
            } else {
                Log.w(TAG, "No video available to play")
            }
        }
        
        private fun playVideo(videoSource: VideoSource) {
            player?.let { exoPlayer ->
                try {
                    Log.d(TAG, "Playing video: ${videoSource.title} (${videoSource.url})")
                    
                    val mediaItem = if (videoSource.isLocal) {
                        // 处理本地assets视频
                        val assetPath = videoSource.url.removePrefix("asset:///")
                        val afd: AssetFileDescriptor = assets.openFd(assetPath)
                        MediaItem.fromUri("asset:///$assetPath")
                    } else {
                        // 处理远程视频
                        MediaItem.fromUri(videoSource.url)
                    }
                    
                    exoPlayer.setMediaItem(mediaItem)
                    exoPlayer.prepare()
                    exoPlayer.play()
                } catch (e: Exception) {
                    Log.e(TAG, "Error playing video: ${videoSource.title}", e)
                    // 播放失败，尝试下一个
                    switchToNextVideo()
                }
            }
        }
        
        private fun switchToNextVideo() {
            val nextVideo = videoManager.getNextVideo()
            if (nextVideo != null) {
                playVideo(nextVideo)
            }
        }
        
        private fun switchToPreviousVideo() {
            val previousVideo = videoManager.getPreviousVideo()
            if (previousVideo != null) {
                playVideo(previousVideo)
            }
        }
        
        private fun releasePlayer() {
            player?.release()
            player = null
            Log.d(TAG, "Player released")
        }
    }
}
