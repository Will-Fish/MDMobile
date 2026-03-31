package com.example.mdmobile.utils

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.view.View
import android.view.Window
import android.view.WindowManager
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * 小米15屏幕特性适配工具
 * 针对高刷新率、边缘手势、分屏模式等进行优化
 */
object ScreenOptimization {

    /**
     * 启用高刷新率模式（如果设备支持）
     */
    fun enableHighRefreshRate(window: Window) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Android 11+ 支持设置首选刷新率
            try {
                val attributes = window.attributes
                // 尝试设置高刷新率
                attributes.preferredRefreshRate = getMaxRefreshRate(window)
                window.attributes = attributes
            } catch (e: Exception) {
                // 忽略错误，设备可能不支持
            }
        }

        // 启用流畅滚动优化
        window.setFlags(
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED,
            WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED
        )
    }

    /**
     * 获取设备最大刷新率
     */
    private fun getMaxRefreshRate(window: Window): Float {
        return try {
            val display = window.context.display
            display?.supportedModes?.maxByOrNull { it.refreshRate }?.refreshRate ?: 60f
        } catch (e: Exception) {
            60f // 默认60Hz
        }
    }

    /**
     * 适配边缘手势（全面屏手势）
     */
    fun adaptEdgeToEdge(window: Window, activity: Activity) {
        // 设置全屏布局，内容延伸到状态栏 and 导航栏后面
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 配置系统栏行为
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        // 设置状态栏和导航栏颜色
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT

        // 根据主题设置系统栏图标颜色
        val isDarkTheme = (activity.resources.configuration.uiMode and
                Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

        insetsController.isAppearanceLightStatusBars = !isDarkTheme
        insetsController.isAppearanceLightNavigationBars = !isDarkTheme
    }

    /**
     * 适配分屏和多窗口模式
     */
    fun adaptMultiWindow(activity: Activity) {
        // 启用多窗口支持
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            // 监听分屏模式变化
            activity.registerComponentCallbacks(object : android.content.ComponentCallbacks {
                override fun onConfigurationChanged(newConfig: Configuration) {
                    // 分屏模式改变时重新布局
                    if (activity.isInMultiWindowMode) {
                        optimizeForMultiWindow(activity)
                    } else {
                        optimizeForFullScreen(activity)
                    }
                }

                override fun onLowMemory() {
                    // 内存不足时清理资源
                }
            })
        }
    }

    /**
     * 为分屏模式优化
     */
    fun optimizeForMultiWindow(activity: Activity) {
        // 分屏模式下优化布局
        val window = activity.window
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        // 减小一些视觉效果以节省资源
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
    }

    /**
     * 为全屏模式优化
     */
    fun optimizeForFullScreen(activity: Activity) {
        val window = activity.window
        window.clearFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)
    }

    /**
     * 获取当前屏幕方向
     */
    fun getScreenOrientation(context: Context): String {
        return when (context.resources.configuration.orientation) {
            Configuration.ORIENTATION_LANDSCAPE -> "landscape"
            Configuration.ORIENTATION_PORTRAIT -> "portrait"
            else -> "undefined"
        }
    }

    /**
     * 检查是否支持HDR显示
     */
    fun supportsHDR(window: Window): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val display = window.context.display
            display?.hdrCapabilities != null
        } else {
            false
        }
    }

    /**
     * 检查是否支持宽色域
     */
    fun supportsWideColorGamut(window: Window): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val display = window.context.display
            display?.isWideColorGamut == true
        } else {
            false
        }
    }

    /**
     * 获取屏幕密度信息
     */
    fun getScreenDensityInfo(context: Context): String {
        val metrics = context.resources.displayMetrics
        return "密度: ${metrics.densityDpi}dpi, 缩放: ${metrics.density}x"
    }

    /**
     * 适配折叠屏设备（如果将来支持）
     */
    fun adaptFoldableScreen(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val windowManager = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val windowMetrics = windowManager.currentWindowMetrics
            val bounds = windowMetrics.bounds

            // 检查是否为折叠屏
            val isFoldable = bounds.width() != bounds.height() &&
                    Math.abs(bounds.width() - bounds.height()) > 100

            if (isFoldable) {
                // 折叠屏特定优化
            }
        }
    }

    /**
     * 优化WebView渲染性能（用于Markdown渲染）
     */
    fun optimizeWebViewForHighRefreshRate(webView: android.webkit.WebView) {
        // 启用硬件加速
        webView.setLayerType(View.LAYER_TYPE_HARDWARE, null)

        // 优化滚动性能
        webView.settings.apply {
            cacheMode = android.webkit.WebSettings.LOAD_DEFAULT
            domStorageEnabled = true
            databaseEnabled = true
            useWideViewPort = true
            loadWithOverviewMode = true
            builtInZoomControls = true
            displayZoomControls = false
            setSupportZoom(true)

            // 针对高刷新率优化
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                disabledActionModeMenuItems = android.webkit.WebSettings.MENU_ITEM_NONE
            }
        }

        // 禁用长按菜单以提高响应速度
        webView.setOnLongClickListener { true }
    }

    /**
     * 检查设备是否为小米15或类似高端设备
     */
    fun isXiaomi15OrSimilar(): Boolean {
        return Build.MANUFACTURER.equals("xiaomi", ignoreCase = true) &&
                (Build.MODEL.contains("15") || Build.DEVICE.contains("15"))
    }

    /**
     * 获取设备性能等级（用于动态调整UI复杂度）
     */
    fun getDevicePerformanceLevel(context: Context): String {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
        val memoryInfo = android.app.ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val totalMemory = memoryInfo.totalMem / (1024 * 1024) // MB
        val isLowRam = activityManager.isLowRamDevice

        return when {
            totalMemory > 8000 && !isLowRam -> "high" // 8GB+ RAM
            totalMemory > 4000 && !isLowRam -> "medium" // 4GB-8GB RAM
            else -> "low"
        }
    }

    /**
     * 根据设备性能动态调整UI效果
     */
    fun adjustUIForPerformance(context: Context): UIOptimizationSettings {
        val performanceLevel = getDevicePerformanceLevel(context)

        return when (performanceLevel) {
            "high" -> UIOptimizationSettings(
                enableAnimations = true,
                animationDurationScale = 1.0f,
                enableShadows = true,
                enableBlurEffects = true,
                maxImageQuality = 100,
                enableComplexLayouts = true
            )
            "medium" -> UIOptimizationSettings(
                enableAnimations = true,
                animationDurationScale = 0.8f,
                enableShadows = true,
                enableBlurEffects = false,
                maxImageQuality = 80,
                enableComplexLayouts = true
            )
            else -> UIOptimizationSettings(
                enableAnimations = false,
                animationDurationScale = 0.5f,
                enableShadows = false,
                enableBlurEffects = false,
                maxImageQuality = 60,
                enableComplexLayouts = false
            )
        }
    }
}

/**
 * UI优化设置
 */
data class UIOptimizationSettings(
    val enableAnimations: Boolean,
    val animationDurationScale: Float,
    val enableShadows: Boolean,
    val enableBlurEffects: Boolean,
    val maxImageQuality: Int,
    val enableComplexLayouts: Boolean
)

/**
 * Compose修饰符：处理边缘手势区域
 */
@Composable
fun Modifier.edgeToEdgePadding(): Modifier = this.windowInsetsPadding(
    WindowInsets.systemBars.only(
        WindowInsetsSides.Horizontal + WindowInsetsSides.Top
    )
)

/**
 * Compose修饰符：安全区域padding（避开摄像头和圆角）
 */
@Composable
fun Modifier.safeAreaPadding(): Modifier =
    this.windowInsetsPadding(WindowInsets.systemBars)
