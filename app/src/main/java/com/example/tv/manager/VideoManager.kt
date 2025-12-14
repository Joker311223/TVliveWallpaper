package com.example.tv.manager

import android.content.Context
import android.util.Log
import com.example.tv.api.RetrofitClient
import com.example.tv.model.VideoSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class VideoManager(private val context: Context) {
    
    private val TAG = "VideoManager"
    private val videoList = mutableListOf<VideoSource>()
    private var currentIndex = 0
    
    init {
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
    
    fun getCurrentVideo(): VideoSource? {
        return if (videoList.isNotEmpty()) {
            videoList[currentIndex]
        } else {
            null
        }
    }
    
    fun getNextVideo(): VideoSource? {
        if (videoList.isEmpty()) return null
        currentIndex = (currentIndex + 1) % videoList.size
        Log.d(TAG, "Switched to next video: ${getCurrentVideo()?.title}")
        return getCurrentVideo()
    }
    
    fun getPreviousVideo(): VideoSource? {
        if (videoList.isEmpty()) return null
        currentIndex = if (currentIndex - 1 < 0) {
            videoList.size - 1
        } else {
            currentIndex - 1
        }
        Log.d(TAG, "Switched to previous video: ${getCurrentVideo()?.title}")
        return getCurrentVideo()
    }
    
    fun getVideoCount(): Int = videoList.size
    
    fun getAllVideos(): List<VideoSource> = videoList.toList()
}
