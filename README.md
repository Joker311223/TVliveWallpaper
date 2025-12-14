# TV 视频播放器

这是一个Android TV全屏视频播放应用，支持播放本地和远程视频，并可通过遥控器左右键切换视频。

## 功能特性

- ✅ 全屏循环播放视频
- ✅ 支持本地assets文件夹中的视频播放
- ✅ 支持从远程API获取视频列表
- ✅ 使用遥控器左右键切换视频
- ✅ 视频循环播放
- ✅ 静音播放
- ✅ 自动处理播放错误并切换到下一个视频

## 项目结构

```
app/src/main/
├── java/com/example/tv/
│   ├── MainActivity.kt                    # 原始主Activity（保留）
│   ├── WallpaperControlActivity.kt        # 全屏视频播放Activity（主启动页）
│   ├── manager/
│   │   └── VideoManager.kt                # 视频管理器
│   ├── model/
│   │   └── VideoSource.kt                 # 视频数据模型
│   ├── api/
│   │   ├── VideoApi.kt                    # API接口定义
│   │   └── RetrofitClient.kt              # Retrofit客户端
│   └── ui/theme/                          # UI主题
├── assets/
│   ├── Intergalactic-Space-Room-4K.mp4    # 本地视频1
│   └── Samurai-Revenge-4K.mp4             # 本地视频2
└── res/
    └── values/
        └── strings.xml                    # 字符串资源
```

## 使用说明

### 1. 安装应用

在Android Studio中打开项目，连接TV设备或模拟器，点击运行。

### 2. 启动应用

1. 应用启动后会自动进入全屏视频播放界面
2. 应用会自动加载本地和远程视频源
3. 视频会自动开始循环播放

### 3. 切换视频

使用遥控器切换视频：

- 按遥控器的**左键**：切换到上一个视频
- 按遥控器的**右键**：切换到下一个视频
- 按遥控器的**返回键**：退出应用

## 视频源配置

### 本地视频

将视频文件（.mp4格式）放入 `app/src/main/assets/` 文件夹中，应用会自动识别并加载。

当前已包含的本地视频：
- Intergalactic-Space-Room-4K.mp4
- Samurai-Revenge-4K.mp4

### 远程视频

#### 配置API地址

编辑 `app/src/main/java/com/example/tv/api/RetrofitClient.kt`：

```kotlin
private const val BASE_URL = "https://your-api-domain.com/" // 替换为实际的API域名
```

#### API接口格式

API应返回以下JSON格式：

```json
{
  "videos": [
    {
      "id": "video_1",
      "url": "https://example.com/video1.mp4",
      "title": "视频标题1"
    },
    {
      "id": "video_2",
      "url": "https://example.com/video2.mp4",
      "title": "视频标题2"
    }
  ]
}
```

#### 模拟数据

如果API未完成或请求失败，应用会自动使用内置的模拟数据（包含3个示例视频）。

## 技术栈

- **Kotlin** - 开发语言
- **Jetpack Compose** - UI框架
- **ExoPlayer (Media3)** - 视频播放
- **Retrofit** - 网络请求
- **Coroutines** - 异步处理

## 依赖项

```kotlin
// ExoPlayer
implementation("androidx.media3:media3-exoplayer:1.2.0")
implementation("androidx.media3:media3-ui:1.2.0")

// Retrofit
implementation("com.squareup.retrofit2:retrofit:2.9.0")
implementation("com.squareup.retrofit2:converter-gson:2.9.0")

// Coroutines
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
```

## 权限说明

应用需要以下权限：

- `INTERNET` - 访问网络获取远程视频
- `ACCESS_NETWORK_STATE` - 检查网络状态

## 注意事项

1. **视频格式**：建议使用H.264编码的MP4格式视频
2. **视频大小**：本地视频文件不宜过大，建议单个文件不超过100MB
3. **网络视频**：确保视频URL可直接访问，支持HTTP/HTTPS协议
4. **性能**：4K视频可能在低端设备上播放卡顿，建议使用1080p或更低分辨率
5. **TV设备**：应用专为Android TV设计，在手机上可能无法正常使用遥控器功能

## 故障排除

### 应用无法启动
- 检查是否授予了必要的权限
- 查看Logcat日志获取详细错误信息
- 尝试清除应用缓存后重新启动

### 视频无法播放
- 检查视频格式是否支持（建议使用H.264编码的MP4）
- 查看Logcat日志获取详细错误信息
- 确保网络连接正常（远程视频）
- 检查本地视频文件是否正确放在assets文件夹中

### 遥控器按键无响应
- 检查TV设备的遥控器是否正常工作
- 尝试重启应用
- 查看Logcat日志确认按键事件是否被正确捕获

## 开发计划

- [ ] 添加视频列表显示界面
- [ ] 支持更多视频格式
- [ ] 添加视频过渡动画
- [ ] 支持视频播放速度调节
- [ ] 添加播放进度显示
- [ ] 支持自定义播放列表

## License

MIT License
