package com.example.mdmobile.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * 自适应布局组件
 * 根据屏幕尺寸、方向和设备特性调整布局
 */
@Composable
fun AdaptiveLayout(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val screenHeight = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE

    // 判断是否为平板或大屏设备
    val isTablet = screenWidth >= 600.dp || screenHeight >= 600.dp

    // 判断是否为折叠屏展开状态
    val isFoldableExpanded = screenWidth >= 800.dp && isLandscape

    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when {
            isFoldableExpanded -> {
                // 折叠屏展开状态：使用分栏布局
                FoldableExpandedLayout(content = content)
            }
            isTablet && isLandscape -> {
                // 平板横屏：使用分栏布局
                TabletLandscapeLayout(content = content)
            }
            isTablet -> {
                // 平板竖屏：使用增强布局
                TabletPortraitLayout(content = content)
            }
            isLandscape -> {
                // 手机横屏：优化布局
                PhoneLandscapeLayout(content = content)
            }
            else -> {
                // 手机竖屏：默认布局
                PhonePortraitLayout(content = content)
            }
        }
    }
}

/**
 * 折叠屏展开布局（分栏显示）
 */
@Composable
private fun FoldableExpandedLayout(
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 左侧栏：目录或导航
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                // 左侧内容占位符
                // 在实际应用中这里可能是目录、书签列表等
            }
        }

        // 主内容区
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(2f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }

        // 右侧栏：工具或信息
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.8f),
            shape = MaterialTheme.shapes.large,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.TopStart
            ) {
                // 右侧内容占位符
                // 在实际应用中这里可能是工具面板、文档信息等
            }
        }
    }
}

/**
 * 平板横屏布局
 */
@Composable
private fun TabletLandscapeLayout(
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 侧边栏
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.7f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }

        // 主内容
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.3f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

/**
 * 平板竖屏布局
 */
@Composable
private fun TabletPortraitLayout(
    content: @Composable () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 顶部栏
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.2f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            )
        }

        // 主内容
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.8f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 8.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

/**
 * 手机横屏布局
 */
@Composable
private fun PhoneLandscapeLayout(
    content: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // 侧边导航（可隐藏）
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp)
            )
        }

        // 主内容
        Surface(
            modifier = Modifier
                .fillMaxHeight()
                .weight(1.6f),
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 4.dp
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                content()
            }
        }
    }
}

/**
 * 手机竖屏布局（默认）
 */
@Composable
private fun PhonePortraitLayout(
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        shape = MaterialTheme.shapes.medium,
        tonalElevation = 4.dp
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            content()
        }
    }
}

/**
 * 响应式文本组件
 * 根据屏幕尺寸调整字体大小
 */
@Composable
fun ResponsiveText(
    text: String,
    modifier: Modifier = Modifier,
    smallScreenSize: Int = 14,
    mediumScreenSize: Int = 16,
    largeScreenSize: Int = 18,
    xLargeScreenSize: Int = 20
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val fontSize = when {
        screenWidth >= 1200 -> xLargeScreenSize // 超大屏
        screenWidth >= 800 -> largeScreenSize   // 大屏/平板
        screenWidth >= 600 -> mediumScreenSize  // 中等屏幕
        else -> smallScreenSize                 // 小屏手机
    }

    androidx.compose.material3.Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge.copy(
            fontSize = fontSize.sp
        )
    )
}

/**
 * 响应式间距组件
 * 根据屏幕尺寸调整间距
 */
@Composable
fun ResponsiveSpacer(
    smallSize: Int = 8,
    mediumSize: Int = 16,
    largeSize: Int = 24
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val spacing = when {
        screenWidth >= 800 -> largeSize
        screenWidth >= 600 -> mediumSize
        else -> smallSize
    }

    androidx.compose.foundation.layout.Spacer(
        modifier = Modifier.size(spacing.dp)
    )
}

/**
 * 响应式卡片组件
 * 根据屏幕尺寸调整卡片样式
 */
@Composable
fun ResponsiveCard(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp

    val elevation = when {
        screenWidth >= 800 -> 8.dp  // 大屏：更高阴影
        screenWidth >= 600 -> 4.dp  // 中屏：中等阴影
        else -> 2.dp                // 小屏：低阴影
    }

    val shapeSize = when {
        screenWidth >= 800 -> androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
        screenWidth >= 600 -> androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
        else -> androidx.compose.foundation.shape.RoundedCornerShape(8.dp)
    }

    androidx.compose.material3.Card(
        modifier = modifier,
        shape = shapeSize,
        elevation = androidx.compose.material3.CardDefaults.cardElevation(
            defaultElevation = elevation
        ),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        )
    ) {
        content()
    }
}