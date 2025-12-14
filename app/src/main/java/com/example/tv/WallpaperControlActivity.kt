package com.example.tv

import android.content.res.AssetFileDescriptor
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.example.tv.manager.VideoManager
import com.example.tv.model.VideoSource
import com.example.tv.ui.theme.TVTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import androidx.activity.compose.setContent

class WallpaperControlActivity : ComponentActivity() {

    private val TAG = "VideoPlayActivity"
    private var player: ExoPlayer? = null
    private lateinit var videoManager: VideoManager
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob)
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 初始化视频管理器
        videoManager = VideoManager(this)
        
        // 异步加载远程视频
        serviceScope.launch {
            videoManager.loadRemoteVideos()
        }
        
        setContent {
            TVTheme {
                VideoPlayScreen(
                    onPlayerReady = { playerView ->
                        initializePlayer(playerView)
                    }
                )
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_RIGHT -> {
                switchToNextVideo()
                Toast.makeText(this, "切换到下一个视频", Toast.LENGTH_SHORT).show()
                return true
            }
            KeyEvent.KEYCODE_DPAD_LEFT -> {
                switchToPreviousVideo()
                Toast.makeText(this, "切换到上一个视频", Toast.LENGTH_SHORT).show()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                // 允许返回
                return super.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun initializePlayer(playerView: PlayerView) {
        if (player == null) {
            player = ExoPlayer.Builder(this).build().apply {
                repeatMode = Player.REPEAT_MODE_ONE
                volume = 0f // 静音播放
                playerView.player = this
                
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
                        switchToNextVideo()
                    }
                })
            }
            Log.d(TAG, "Player initialized")
            playCurrentVideo()
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
    
    override fun onPause() {
        super.onPause()
        player?.pause()
    }
    
    override fun onResume() {
        super.onResume()
        player?.play()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        serviceJob.cancel()
    }
}

@Composable
fun VideoPlayScreen(
    onPlayerReady: (PlayerView) -> Unit
) {
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    
    LaunchedEffect(Unit) {
        // 延迟一点时间确保UI已经渲染
        isLoading = false
    }
    
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    playerView = this
                    onPlayerReady(this)
                }
            },
            modifier = Modifier.fillMaxSize()
        )
        
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.7f)),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    CircularProgressIndicator(
                        color = Color.White,
                        modifier = Modifier.size(50.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "加载视频中...",
                        color = Color.White,
                        fontSize = 18.sp
                    )
                }
            }
        }
    }
}
