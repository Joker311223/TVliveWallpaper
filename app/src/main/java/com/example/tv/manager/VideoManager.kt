package com.example.tv.manager

import android.content.Context
import android.util.Log
import com.example.tv.api.RetrofitClient
import com.example.tv.model.VideoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

enum class PlaylistMode {
    ALL,      // 全局列表
    FAVORITE  // 收藏列表
}

class VideoManager(private val context: Context) {
    
    private val TAG = "VideoManager"
    private val videoList = mutableListOf<VideoSource>()
    private var currentIndex = 0
    private var playlistMode = PlaylistMode.ALL
    private lateinit var favoriteManager: FavoriteManager
    
    init {
        // 初始化收藏管理器
        favoriteManager = FavoriteManager(context)
        // 添加本地视频源
        addLocalVideos()
    }
    
    private fun addLocalVideos() {
        try {
            val assetManager = context.assets
            val videoFiles = assetManager.list("")?.filter { 
                it.endsWith(".mp4", ignoreCase = true) 
            } ?: emptyList()
            
            videoFiles.forEach { fileName ->
                videoList.add(
                    VideoSource(
                        id = "local_$fileName",
                        url = "asset:///$fileName",
                        title = fileName.substringBeforeLast("."),
                        isLocal = true
                    )
                )
            }
            Log.d(TAG, "Added ${videoFiles.size} local videos")
        } catch (e: Exception) {
            Log.e(TAG, "Error loading local videos", e)
        }
    }
    
    suspend fun loadRemoteVideos() {
        withContext(Dispatchers.IO) {
            try {
                // 尝试从远程API获取视频列表
                val response = RetrofitClient.videoApi.getVideoList()
                response.videos.forEach { videoInfo ->
                    videoList.add(
                        VideoSource(
                            id = videoInfo.id,
                            url = videoInfo.url,
                            title = videoInfo.title,
                            isLocal = false
                        )
                    )
                }
                Log.d(TAG, "Loaded ${response.videos.size} remote videos")
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load remote videos, using mock data", e)
                // 如果API失败，使用模拟数据
                addMockRemoteVideos()
            }
        }
    }
    
    private fun addMockRemoteVideos() {
        // 模拟远程视频数据
        val mockVideos = listOf(
            VideoSource(
                id = "remote_1",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4",
                title = "Big Buck Bunny",
                isLocal = false
            ),
            VideoSource(
                id = "remote_2",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ElephantsDream.mp4",
                title = "Elephants Dream",
                isLocal = false
            ),
            VideoSource(
                id = "remote_3",
                url = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/ForBiggerBlazes.mp4",
                title = "For Bigger Blazes",
                isLocal = false
            )
        )
        videoList.addAll(mockVideos)
        Log.d(TAG, "Added ${mockVideos.size} mock remote videos")
    }
    
    // 获取当前列表中的视频
    private fun getCurrentPlaylist(): List<VideoSource> {
        return when (playlistMode) {
            PlaylistMode.ALL -> videoList
            PlaylistMode.FAVORITE -> {
                val favoriteIds = favoriteManager.getFavoriteIds()
                videoList.filter { favoriteIds.contains(it.id) }
            }
        }
    }
    
    fun getCurrentVideo(): VideoSource? {
        val playlist = getCurrentPlaylist()
        return if (playlist.isNotEmpty() && currentIndex < playlist.size) {
            playlist[currentIndex]
        } else {
            null
        }
    }
    
    fun getNextVideo(): VideoSource? {
        val playlist = getCurrentPlaylist()
        if (playlist.isEmpty()) return null
        currentIndex = (currentIndex + 1) % playlist.size
        Log.d(TAG, "Switched to next video in ${playlistMode} mode: ${getCurrentVideo()?.title}")
        return getCurrentVideo()
    }
    
    fun getPreviousVideo(): VideoSource? {
        val playlist = getCurrentPlaylist()
        if (playlist.isEmpty()) return null
        currentIndex = if (currentIndex - 1 < 0) {
            playlist.size - 1
        } else {
            currentIndex - 1
        }
        Log.d(TAG, "Switched to previous video in ${playlistMode} mode: ${getCurrentVideo()?.title}")
        return getCurrentVideo()
    }
    
    fun getVideoCount(): Int = videoList.size
    
    fun getAllVideos(): List<VideoSource> = videoList.toList()
    
    // 收藏相关方法
    fun toggleFavorite(video: VideoSource): Boolean {
        return favoriteManager.toggleFavorite(video)
    }
    
    fun isFavorite(video: VideoSource): Boolean {
        return favoriteManager.isFavorite(video)
    }
    
    fun getFavoriteCount(): Int {
        return favoriteManager.getFavoriteCount()
    }
    
    // 列表模式相关方法
    fun switchPlaylistMode(): PlaylistMode {
        playlistMode = if (playlistMode == PlaylistMode.ALL) {
            PlaylistMode.FAVORITE
        } else {
            PlaylistMode.ALL
        }
        currentIndex = 0
        Log.d(TAG, "Switched to playlist mode: $playlistMode")
        return playlistMode
    }
    
    fun getPlaylistMode(): PlaylistMode {
        return playlistMode
    }
    
    fun setPlaylistMode(mode: PlaylistMode) {
        playlistMode = mode
        currentIndex = 0
        Log.d(TAG, "Set playlist mode to: $playlistMode")
    }
    
    fun getPlaylistSize(): Int {
        return getCurrentPlaylist().size
    }
    
    fun getCurrentIndex(): Int {
        return currentIndex
    }
}
