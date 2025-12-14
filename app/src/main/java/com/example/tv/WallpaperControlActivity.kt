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
import com.example.tv.manager.PlaylistMode
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
                    videoManager = videoManager,
                    onPlayerReady = { playerView ->
                        initializePlayer(playerView)
                    }
                )
            }
        }
    }
    
    private var confirmPressedTime = 0L
    private val LONG_PRESS_DURATION = 500L // 500ms为长按
    
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
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                // 记录按下时间
                if (event?.repeatCount == 0) {
                    confirmPressedTime = System.currentTimeMillis()
                }
                return true
            }
            KeyEvent.KEYCODE_MENU -> {
                // 切换播放列表模式
                switchPlaylistMode()
                return true
            }
            KeyEvent.KEYCODE_BACK -> {
                // 允许返回
                return super.onKeyDown(keyCode, event)
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    override fun onKeyUp(keyCode: Int, event: KeyEvent?): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_DPAD_CENTER, KeyEvent.KEYCODE_ENTER -> {
                val pressDuration = System.currentTimeMillis() - confirmPressedTime
                if (pressDuration >= LONG_PRESS_DURATION) {
                    // 长按 - 收藏当前视频
                    toggleFavoriteVideo()
                } else {
                    // 短按 - 暂停/继续播放（可选）
                    // 目前不做任何操作
                }
                return true
            }
        }
        return super.onKeyUp(keyCode, event)
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
    
    private fun toggleFavoriteVideo() {
        val currentVideo = videoManager.getCurrentVideo()
        if (currentVideo != null) {
            val isFavorite = videoManager.toggleFavorite(currentVideo)
            val message = if (isFavorite) {
                "已收藏: ${currentVideo.title}"
            } else {
                "已取消收藏: ${currentVideo.title}"
            }
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
            Log.d(TAG, message)
        }
    }
    
    private fun switchPlaylistMode() {
        val newMode = videoManager.switchPlaylistMode()
        val modeText = when (newMode) {
            PlaylistMode.ALL -> "全局列表"
            PlaylistMode.FAVORITE -> "收藏列表"
        }
        val playlistSize = videoManager.getPlaylistSize()
        val message = "切换到$modeText (共$playlistSize 个视频)"
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        Log.d(TAG, message)
        
        // 重新播放当前列表的第一个视频
        val firstVideo = videoManager.getCurrentVideo()
        if (firstVideo != null) {
            playVideo(firstVideo)
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
    videoManager: VideoManager,
    onPlayerReady: (PlayerView) -> Unit
) {
    var playerView by remember { mutableStateOf<PlayerView?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var playlistMode by remember { mutableStateOf(videoManager.getPlaylistMode()) }
    var currentVideo by remember { mutableStateOf(videoManager.getCurrentVideo()) }
    var isFavorite by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        // 延迟一点时间确保UI已经渲染
        isLoading = false
    }
    
    // 监听视频变化
    LaunchedEffect(currentVideo) {
        currentVideo = videoManager.getCurrentVideo()
        playlistMode = videoManager.getPlaylistMode()
        isFavorite = currentVideo?.let { videoManager.isFavorite(it) } ?: false
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
        
        // 顶部状态栏
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 左侧：播放列表模式
                Text(
                    text = when (playlistMode) {
                        PlaylistMode.ALL -> "全局列表"
                        PlaylistMode.FAVORITE -> "❤ 收藏列表"
                    },
                    color = Color.White,
                    fontSize = 14.sp
                )
                
                // 右侧：收藏状态和视频信息
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (isFavorite) {
                        Text(
                            text = "❤",
                            color = Color.Red,
                            fontSize = 16.sp,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                    Text(
                        text = "${videoManager.getCurrentIndex() + 1}/${videoManager.getPlaylistSize()}",
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
            }
        }
        
        // 底部视频标题
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.Black.copy(alpha = 0.5f))
                .padding(16.dp)
                .align(Alignment.BottomCenter)
        ) {
            Text(
                text = currentVideo?.title ?: "加载中...",
                color = Color.White,
                fontSize = 16.sp,
                maxLines = 2
            )
        }
        
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
