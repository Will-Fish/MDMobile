package com.example.mdmobile.data.model

import androidx.compose.runtime.Immutable

/**
 * 用户偏好设置数据类
 */
@Immutable
data class UserPreferences(
    val themeMode: ThemeMode = ThemeMode.AUTO,
    val defaultFolder: String? = null,
    val fontSize: Int = 16
)

enum class ThemeMode {
    LIGHT,
    DARK,
    AUTO
}