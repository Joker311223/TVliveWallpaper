package com.example.tv

import android.app.WallpaperManager
import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.tv.material3.*
import com.example.tv.service.VideoWallpaperService
import com.example.tv.ui.theme.TVTheme

class WallpaperControlActivity : ComponentActivity() {

    private val TAG: String? = "WallpaperControlActivity"
    private var isWallpaperActive = false
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        checkWallpaperStatus()
        
        setContent {
            TVTheme {
                WallpaperControlScreen(
                    isWallpaperActive = isWallpaperActive,
                    onSetWallpaper = { setVideoWallpaper() },
                    onNextVideo = { sendWallpaperCommand("NEXT_VIDEO") },
                    onPreviousVideo = { sendWallpaperCommand("PREVIOUS_VIDEO") }
                )
            }
        }
    }
    
    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if (isWallpaperActive) {
            when (keyCode) {
                KeyEvent.KEYCODE_DPAD_RIGHT -> {
                    sendWallpaperCommand("NEXT_VIDEO")
                    Toast.makeText(this, "切换到下一个视频", Toast.LENGTH_SHORT).show()
                    return true
                }
                KeyEvent.KEYCODE_DPAD_LEFT -> {
                    sendWallpaperCommand("PREVIOUS_VIDEO")
                    Toast.makeText(this, "切换到上一个视频", Toast.LENGTH_SHORT).show()
                    return true
                }
            }
        }
        return super.onKeyDown(keyCode, event)
    }
    
    private fun checkWallpaperStatus() {
        val wallpaperManager = WallpaperManager.getInstance(this)
        val wallpaperInfo = wallpaperManager.wallpaperInfo
        isWallpaperActive = wallpaperInfo?.packageName == packageName
    }
    
    private fun setVideoWallpaper() {
        Log.i(TAG, "setVideoWallpaper: ")
        try {
            val intent = Intent(WallpaperManager.ACTION_CHANGE_LIVE_WALLPAPER).apply {
                putExtra(
                    WallpaperManager.EXTRA_LIVE_WALLPAPER_COMPONENT,
                    ComponentName(this@WallpaperControlActivity, VideoWallpaperService::class.java)
                )
            }
            startActivity(intent)
        } catch (e: Exception) {
            Log.i(TAG, "无法设置动态壁纸: ${e.message}")
            Toast.makeText(this, "无法设置动态壁纸: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun sendWallpaperCommand(command: String) {
        if (!isWallpaperActive) {
            Toast.makeText(this, "请先设置视频壁纸", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val wallpaperManager = WallpaperManager.getInstance(this)
            // 从 window 的 decorView 获取 token
            val token = window?.decorView?.windowToken
            if (token != null) {
                wallpaperManager.sendWallpaperCommand(
                    token,
                    command,
                    0, 0, 0, null
                )
            } else {
                Toast.makeText(this, "无法获取窗口令牌", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "发送命令失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun WallpaperControlScreen(
    isWallpaperActive: Boolean,
    onSetWallpaper: () -> Unit,
    onNextVideo: () -> Unit,
    onPreviousVideo: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(48.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "TV 视频壁纸控制",
                style = androidx.tv.material3.MaterialTheme.typography.displayMedium
            )
            
            Spacer(modifier = Modifier.height(32.dp))
            
            if (!isWallpaperActive) {
                Text(
                    text = "请先设置视频壁纸",
                    style = androidx.tv.material3.MaterialTheme.typography.bodyLarge
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Button(
                    onClick = onSetWallpaper,
                    modifier = Modifier.width(300.dp)
                ) {
                    Text("设置视频壁纸")
                }
            } else {
                Text(
                    text = "壁纸已激活",
                    style = androidx.tv.material3.MaterialTheme.typography.bodyLarge,
                    color = androidx.tv.material3.MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(32.dp))
                
                Text(
                    text = "使用遥控器左右键切换视频",
                    style = androidx.tv.material3.MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(24.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = onPreviousVideo,
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("← 上一个")
                    }
                    
                    Button(
                        onClick = onNextVideo,
                        modifier = Modifier.width(200.dp)
                    ) {
                        Text("下一个 →")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            Text(
                text = "提示：视频源包括本地assets文件夹中的视频和远程API获取的视频",
                style = androidx.tv.material3.MaterialTheme.typography.bodySmall,
                color = androidx.tv.material3.MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            )
        }
    }
}
