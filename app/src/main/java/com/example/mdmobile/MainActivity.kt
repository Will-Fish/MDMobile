package com.example.mdmobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.mdmobile.ui.theme.MDMobileTheme
import com.example.mdmobile.utils.ScreenOptimization
import com.example.mdmobile.utils.safeAreaPadding

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 应用屏幕优化
        applyScreenOptimizations()

        setContent {
            MDMobileTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier
                        .fillMaxSize()
                        .safeAreaPadding(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MDMobileApp()
                }
            }
        }
    }

    private fun applyScreenOptimizations() {
        val window = window

        // 启用高刷新率（如果设备支持）
        ScreenOptimization.enableHighRefreshRate(window)

        // 适配边缘手势（全面屏）
        ScreenOptimization.adaptEdgeToEdge(window, this)

        // 适配分屏模式
        ScreenOptimization.adaptMultiWindow(this)

        // 检查设备性能并调整UI
        val uiSettings = ScreenOptimization.adjustUIForPerformance(this)
        applyUISettings(uiSettings)
    }

    private fun applyUISettings(settings: com.example.mdmobile.utils.UIOptimizationSettings) {
        // 根据设备性能调整全局UI设置
        if (!settings.enableAnimations) {
            // 禁用或简化动画
            window.setWindowAnimations(0)
        }

        // 这里可以添加更多UI优化逻辑
        // 例如：根据settings调整Compose动画速度等
    }

    override fun onResume() {
        super.onResume()
        // 确保边缘手势适配在每次恢复时都生效
        ScreenOptimization.adaptEdgeToEdge(window, this)
    }

    override fun onMultiWindowModeChanged(isInMultiWindowMode: Boolean) {
        super.onMultiWindowModeChanged(isInMultiWindowMode)
        // 分屏模式变化时重新优化
        if (isInMultiWindowMode) {
            ScreenOptimization.optimizeForMultiWindow(this)
        } else {
            ScreenOptimization.optimizeForFullScreen(this)
        }
    }
}
