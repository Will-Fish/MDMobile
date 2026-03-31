package com.example.mdmobile

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.mdmobile.ui.components.MarkdownRenderer
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Markdown渲染器UI测试
 * 测试Markdown渲染器在Android设备上的实际表现
 */
@RunWith(AndroidJUnit4::class)
class MarkdownRendererUITest {

    @get:Rule
    val composeTestRule = createComposeRule()

    /**
     * 测试基本Markdown渲染
     */
    @Test
    fun testBasicMarkdownRendering() {
        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = "# 测试标题\n\n这是测试段落。",
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 注意：WebView内容无法直接通过Compose测试访问
        // 这里我们主要测试组件是否存在
        // 实际渲染测试需要通过其他方式验证
    }

    /**
     * 测试主题切换
     */
    @Test
    fun testThemeSwitching() {
        var isDarkTheme = false

        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = "# 主题测试",
                isDarkTheme = isDarkTheme,
                fontSize = 16
            )
        }

        // 主题切换无法直接测试，因为WebView内部实现
        // 这里主要是组件集成测试
    }

    /**
     * 测试字体大小调整
     */
    @Test
    fun testFontSizeAdjustment() {
        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = "# 字体大小测试",
                isDarkTheme = false,
                fontSize = 20 // 使用较大字体
            )
        }

        // 字体大小调整在WebView内部实现，无法直接测试
        // 这里主要是确保组件能接受不同字体大小参数
    }

    /**
     * 测试长文档渲染性能
     */
    @Test
    fun testLongDocumentPerformance() {
        val longContent = buildString {
            append("# 长文档测试\n\n")
            repeat(100) { i ->
                append("## 章节 ${i + 1}\n\n")
                append("这是第${i + 1}章节的内容。")
                append("包含一些**粗体**和*斜体*文本。\n\n")
                append("- 列表项1\n")
                append("- 列表项2\n")
                append("- 列表项3\n\n")
            }
        }

        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = longContent,
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 性能测试：确保长文档能正常渲染而不崩溃
        // 实际性能测试需要更复杂的工具
    }

    /**
     * 测试特殊字符渲染
     */
    @Test
    fun testSpecialCharacters() {
        val specialContent = """
            # 特殊字符测试

            ## Unicode字符
            - 中文测试
            - 🚀 火箭表情
            - 🌟 星星表情
            - 😊 笑脸表情

            ## HTML实体
            - &lt;div&gt;
            - &amp;
            - &copy;

            ## 数学符号
            - α β γ
            - ∑ ∫ ∮
            - √ ∞ ≈

            ## 代码相关
            - `console.log("Hello")`
            - ```kotlin
              fun main() = println("世界")
              ```
        """.trimIndent()

        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = specialContent,
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 特殊字符渲染测试
        // WebView应能正确处理这些字符
    }

    /**
     * 测试链接和图片
     */
    @Test
    fun testLinksAndImages() {
        val contentWithLinks = """
            # 链接和图片测试

            ## 链接
            - [Google](https://google.com)
            - [相对链接](./document.md)
            - [带标题的链接](https://example.com "示例网站")

            ## 图片
            - ![风景图](https://example.com/image.jpg)
            - ![带标题图片](https://example.com/photo.png "美丽的风景")

            ## 混合内容
            这是一个包含[链接](https://example.com)和![图片](image.png)的段落。
        """.trimIndent()

        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = contentWithLinks,
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 链接和图片渲染测试
        // 注意：实际图片加载需要网络权限和Coil配置
    }

    /**
     * 测试表格渲染
     */
    @Test
    fun testTableRendering() {
        val tableContent = """
            # 表格测试

            | 姓名 | 年龄 | 城市 |
            |------|------|------|
            | 张三 | 25   | 北京 |
            | 李四 | 30   | 上海 |
            | 王五 | 28   | 广州 |

            ## 复杂表格
            | 项目 | 描述 | 状态 | 优先级 |
            |------|------|------|--------|
            | 功能A | 实现基础功能 | ✅ 完成 | 高 |
            | 功能B | 优化性能 | 🔄 进行中 | 中 |
            | 功能C | 修复BUG | 📅 计划中 | 低 |
        """.trimIndent()

        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = tableContent,
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 表格渲染测试
        // CommonMark应能正确渲染表格
    }

    /**
     * 测试代码高亮
     */
    @Test
    fun testCodeHighlighting() {
        val codeContent = """
            # 代码高亮测试

            ## Kotlin代码
            ```kotlin
            fun main() {
                println("Hello, Kotlin!")
                val list = listOf(1, 2, 3)
                list.forEach { println(it) }
            }
            ```

            ## Java代码
            ```java
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello, Java!");
                }
            }
            ```

            ## Python代码
            ```python
            def hello_world():
                print("Hello, Python!")
                return True
            ```

            ## JavaScript代码
            ```javascript
            function hello() {
                console.log("Hello, JavaScript!");
                return { message: "Hello" };
            }
            ```
        """.trimIndent()

        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = codeContent,
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 代码高亮测试
        // 注意：CommonMark本身不提供语法高亮，需要额外库
        // 这里测试基础代码块渲染
    }

    /**
     * 测试空内容和边界情况
     */
    @Test
    fun testEdgeCases() {
        // 测试空内容
        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = "",
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 测试只有空格的内容
        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = "   \n  \t  \n  ",
                isDarkTheme = false,
                fontSize = 16
            )
        }

        // 测试非常大的字体
        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = "# 大字体测试",
                isDarkTheme = false,
                fontSize = 32
            )
        }

        // 测试非常小的字体
        composeTestRule.setContent {
            MarkdownRenderer(
                markdownContent = "# 小字体测试",
                isDarkTheme = false,
                fontSize = 8
            )
        }
    }

    /**
     * 测试Markdown渲染器与Compose主题集成
     */
    @Test
    fun testThemeIntegration() {
        // 测试亮色主题
        composeTestRule.setContent {
            androidx.compose.material3.MaterialTheme {
                MarkdownRenderer(
                    markdownContent = "# 亮色主题测试",
                    isDarkTheme = false,
                    fontSize = 16
                )
            }
        }

        // 测试暗色主题
        composeTestRule.setContent {
            androidx.compose.material3.MaterialTheme {
                MarkdownRenderer(
                    markdownContent = "# 暗色主题测试",
                    isDarkTheme = true,
                    fontSize = 16
                )
            }
        }

        // 测试主题切换
        var currentTheme = false
        composeTestRule.setContent {
            androidx.compose.material3.MaterialTheme {
                MarkdownRenderer(
                    markdownContent = "# 动态主题测试",
                    isDarkTheme = currentTheme,
                    fontSize = 16
                )
            }
        }

        // 动态切换主题（无法在测试中实现，但组件应支持）
    }
}