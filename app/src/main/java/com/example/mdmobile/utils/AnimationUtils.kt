package com.example.mdmobile.utils

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 为可点击元素添加波纹动画效果
 */
fun Modifier.clickableWithRipple(
    onClick: () -> Unit,
    enabled: Boolean = true
): Modifier = composed {
    val haptic = LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }

    this.clickable(
        interactionSource = interactionSource,
        indication = ripple(
            bounded = true,
            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)
        ),
        onClick = {
            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
            onClick()
        },
        enabled = enabled
    )
}

/**
 * 添加按压缩放动画
 */
fun Modifier.scaleOnPress(
    scale: Float = 0.95f
): Modifier = composed {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scaleValue by animateFloatAsState(
        targetValue = if (isPressed) scale else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioLowBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    this
        .graphicsLayer {
            scaleX = scaleValue
            scaleY = scaleValue
        }
        .clickable(
            interactionSource = interactionSource,
            indication = null,
            onClick = {}
        )
}

/**
 * 创建加载时的脉动动画
 */
@Composable
fun Modifier.pulsingEffect(isActive: Boolean = true): Modifier {
    val infiniteTransition = rememberInfiniteTransition(label = "pulsing")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.8f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = EaseInOutCubic),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    return if (isActive) {
        this.graphicsLayer {
            this.alpha = alpha
        }
    } else {
        this
    }
}

/**
 * 为列表项添加入场动画
 */
@Composable
fun Modifier.enterAnimation(
    index: Int,
    isVisible: Boolean = true
): Modifier {
    val offsetY = remember { Animatable(50f) }
    val alpha = remember { Animatable(0f) }

    LaunchedEffect(isVisible) {
        if (isVisible) {
            // 交错延迟：每个项目延迟50ms
            delay(index * 50L)

            // 同时开始两个动画
            launch {
                offsetY.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioLowBouncy,
                        stiffness = Spring.StiffnessMedium
                    )
                )
            }

            launch {
                alpha.animateTo(
                    targetValue = 1f,
                    animationSpec = tween(300)
                )
            }
        }
    }

    return this
        .graphicsLayer {
            translationY = offsetY.value
            this.alpha = alpha.value
        }
}

/**
 * 添加优雅的边框高亮效果
 */
fun Modifier.highlightBorder(
    color: Color? = null,
    width: Float = 2f,
    isActive: Boolean = true
): Modifier = composed {
    val borderColor = color ?: MaterialTheme.colorScheme.primary
    if (isActive) {
        this.drawWithCache {
            onDrawBehind {
                drawRect(
                    color = borderColor,
                    style = Stroke(width = width),
                    topLeft = Offset(0f, 0f),
                    size = size
                )
            }
        }
    } else {
        this
    }
}

/**
 * 创建滑动删除动画
 */
fun Modifier.swipeToDismissAnimation(
    offsetX: Float,
    maxOffset: Float = 100f
): Modifier = graphicsLayer {
    val progress = (offsetX / maxOffset).coerceIn(-1f, 1f)
    val rotation = progress * 5f // 轻微旋转效果

    translationX = offsetX
    rotationZ = rotation
    alpha = 1f - (kotlin.math.abs(progress) * 0.3f) // 滑动时逐渐透明
}
