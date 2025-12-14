# TV 视频播放器 - 快速开始指南

## 应用已完全改造！

这个应用现在是一个 **全屏视频循环播放应用**，而不是系统动态壁纸。

## 快速开始

### 1️⃣ 安装应用

```bash
# 在Android Studio中
1. 打开项目
2. 连接TV设备或启动模拟器
3. 点击 "Run" 按钮
```

### 2️⃣ 应用启动

应用启动后会：
- ✅ 自动进入全屏视频播放界面
- ✅ 自动加载本地视频（assets文件夹中的MP4文件）
- ✅ 自动加载远程视频（从API获取，失败时使用模拟数据）
- ✅ 自动开始播放第一个视频

### 3️⃣ 控制播放

使用遥控器控制：

| 按键 | 功能 |
|------|------|
| ⬅️ 左键 | 切换到上一个视频 |
| ➡️ 右键 | 切换到下一个视频 |
| 🔙 返回键 | 退出应用 |

## 主要特性

- 🎬 **全屏播放** - 占满整个屏幕
- 🔄 **循环播放** - 视频自动循环
- 🔇 **静音播放** - 默认无声
- 📱 **遥控器支持** - 完全支持TV遥控器
- 🌐 **本地+远程** - 支持本地和网络视频
- ⚡ **自动切换** - 播放错误时自动切换下一个

## 视频源配置

### 添加本地视频

1. 在项目中找到 `app/src/main/assets/` 文件夹
2. 将MP4视频文件放入该文件夹
3. 重新编译应用
4. 应用会自动识别并加载

### 配置远程视频

编辑 `app/src/main/java/com/example/tv/api/RetrofitClient.kt`：

```kotlin
private const val BASE_URL = "https://your-api-domain.com/"
```

API应返回以下格式：

```json
{
  "videos": [
    {
      "id": "video_1",
      "url": "https://example.com/video1.mp4",
      "title": "视频标题"
    }
  ]
}
```

## 常见问题

### Q: 应用启动后没有视频播放？
**A:** 
- 检查是否有本地视频在 `assets` 文件夹中
- 检查网络连接（远程视频）
- 查看Logcat日志获取详细错误信息

### Q: 遥控器按键无响应？
**A:**
- 确保TV设备的遥控器正常工作
- 尝试重启应用
- 检查应用是否在前台运行

### Q: 如何退出应用？
**A:** 按遥控器的返回键即可退出

### Q: 视频播放卡顿？
**A:**
- 使用较低分辨率的视频（建议1080p或以下）
- 检查网络连接速度
- 在低端设备上可能需要优化视频

## 技术信息

- **开发语言** - Kotlin
- **UI框架** - Jetpack Compose
- **视频播放** - ExoPlayer (Media3)
- **网络请求** - Retrofit
- **异步处理** - Coroutines

## 权限

应用需要以下权限：
- `INTERNET` - 获取远程视频
- `ACCESS_NETWORK_STATE` - 检查网络状态

## 文件结构

```
app/src/main/
├── java/com/example/tv/
│   ├── WallpaperControlActivity.kt    ← 主应用（全屏播放）
│   ├── manager/VideoManager.kt        ← 视频管理
│   ├── api/                           ← 网络请求
│   └── model/                         ← 数据模型
├── assets/                            ← 本地视频文件夹
└── res/
    └── values/strings.xml             ← 字符串资源
```

## 修改历史

### 从壁纸应用改为全屏播放应用

**删除的内容：**
- ❌ VideoWallpaperService（壁纸服务）
- ❌ 壁纸相关权限
- ❌ 壁纸配置文件

**新增的内容：**
- ✅ 全屏视频播放界面
- ✅ 直接集成ExoPlayer
- ✅ 简化的启动流程

**优势：**
- 🚀 启动更快
- 💾 内存占用更低
- 👥 用户体验更好
- 📝 代码更简洁

## 下一步

想要进一步定制应用？

1. **修改播放列表** - 编辑 `VideoManager.kt`
2. **自定义UI** - 修改 `VideoPlayScreen` Composable
3. **添加控制按钮** - 在 `VideoPlayScreen` 中添加UI元素
4. **支持更多格式** - 配置ExoPlayer的解码器

## 需要帮助？

查看以下文件获取更多信息：
- `README.md` - 详细文档
- `MIGRATION_SUMMARY.md` - 修改总结
- `app/build.gradle.kts` - 依赖配置

祝您使用愉快！🎉
