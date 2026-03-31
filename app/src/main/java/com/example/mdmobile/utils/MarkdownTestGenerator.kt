package com.example.mdmobile.utils

import java.io.File
import java.io.FileWriter

/**
 * Markdown测试文件生成器
 * 用于生成各种Markdown测试文件，验证渲染兼容性
 */
object MarkdownTestGenerator {

    /**
     * 生成基础语法测试文件
     */
    fun generateBasicSyntaxTest(): String {
        return """
            # Markdown基础语法测试

            ## 标题测试
            # 标题1
            ## 标题2
            ### 标题3
            #### 标题4
            ##### 标题5
            ###### 标题6

            ## 文本样式测试
            **粗体文本**
            *斜体文本*
            ***粗斜体文本***
            ~~删除线文本~~
            普通文本

            ## 链接测试
            - [外部链接](https://example.com)
            - [相对链接](./other.md)
            - [带标题链接](https://example.com "示例网站")
            - [引用链接][1]
            - 自动链接：<https://example.com>

            [1]: https://example.com "引用链接"

            ## 图片测试
            ![风景图片](https://example.com/image.jpg "美丽的风景")
            ![本地图片](./images/photo.png)
            ![引用图片][img1]

            [img1]: https://example.com/logo.png "Logo"

            ## 列表测试

            ### 无序列表
            - 项目1
            - 项目2
              - 嵌套项目2.1
              - 嵌套项目2.2
            - 项目3

            ### 有序列表
            1. 第一项
            2. 第二项
               1. 嵌套第一项
               2. 嵌套第二项
            3. 第三项

            ### 任务列表
            - [ ] 未完成任务
            - [x] 已完成任务
            - [X] 大写已完成任务

            ## 引用测试
            > 单行引用
            >
            > 多行引用
            > 继续第二行
            >
            > > 嵌套引用
            > > 嵌套内容

            ## 代码测试

            ### 行内代码
            这是一个`行内代码`示例。

            ### 代码块
            ```kotlin
            fun main() {
                println("Hello, Kotlin!")
            }
            ```

            ```java
            public class Main {
                public static void main(String[] args) {
                    System.out.println("Hello, Java!");
                }
            }
            ```

            ```python
            def hello():
                print("Hello, Python!")
            ```

            ### 无语言代码块
            ```
            普通代码块
            没有语法高亮
            ```

            ## 表格测试

            | 姓名 | 年龄 | 城市 | 职业 |
            |------|------|------|------|
            | 张三 | 25   | 北京 | 工程师 |
            | 李四 | 30   | 上海 | 设计师 |
            | 王五 | 28   | 广州 | 产品经理 |

            ### 对齐表格
            | 左对齐 | 居中对齐 | 右对齐 |
            |:-------|:--------:|-------:|
            | 左对齐文本 | 居中文本 | 右对齐文本 |
            | 另一个左对齐 | 另一个居中 | 另一个右对齐 |

            ## 分隔线测试
            上面的内容

            ---

            ***

            ___

            下面的内容

            ## HTML测试
            支持内联HTML：<span style="color: red">红色文本</span>

            <div style="background: #f0f0f0; padding: 10px;">
                HTML块级元素
            </div>

            ## 转义字符测试
            \\*不会变成斜体\\*
            \\# 不是标题
            \\[不是链接\\]
            \\`不是代码\\`

            ## 特殊字符测试
            - 表情符号：🚀 🌟 😊 👍
            - 数学符号：α β γ ∑ ∫ √ ∞ ≈ ≠ ≤ ≥
            - 货币符号：€ £ ¥ $
            - 箭头：→ ← ↑ ↓ ↔

            ## 长段落测试
            ${"这是一个非常长的段落，用于测试文本换行和渲染性能。".repeat(20)}

            ## 混合内容测试
            这是一个**混合**了多种*样式*的段落，包含`代码`、[链接](https://example.com)和![图片](image.png)。

            1. 有序列表项**加粗**
            2. *斜体*列表项
                - 嵌套`代码`
                - 嵌套[链接](#)

            > 引用中的**粗体**和*斜体*

            | 功能 | **状态** | *备注* |
            |------|----------|--------|
            | 渲染 | ✅ | 支持`代码` |
            | 性能 | ⚡ | *很快* |

            ---

            **测试完成**
        """.trimIndent()
    }

    /**
     * 生成性能测试文件（非常大的文件）
     */
    fun generatePerformanceTest(): String {
        val content = StringBuilder()
        content.append("# 性能测试文档\n\n")

        // 添加大量内容
        repeat(100) { section ->
            content.append("## 章节 ${section + 1}\n\n")

            // 添加段落
            repeat(5) { paragraph ->
                content.append("这是第${section + 1}章节的第${paragraph + 1}段落。")
                content.append("包含一些**重要内容**和*强调文本*。")
                content.append("还有`代码示例`和[链接](#)。")
                content.append("${"长文本".repeat(10)}。\n\n")
            }

            // 添加列表
            content.append("### 项目列表\n")
            repeat(10) { item ->
                content.append("- 项目${item + 1}: 描述内容")
                if (item % 3 == 0) content.append(" **重要**")
                if (item % 4 == 0) content.append(" *特殊*")
                content.append("\n")
            }
            content.append("\n")

            // 添加表格
            if (section % 5 == 0) {
                content.append("### 数据表格\n")
                content.append("| ID | 名称 | 值 | 状态 |\n")
                content.append("|----|------|-----|------|\n")
                repeat(8) { row ->
                    content.append("| ${row + 1} | 项目${row + 1} | ${row * 100} | ${if (row % 2 == 0) "✅" else "🔄"} |\n")
                }
                content.append("\n")
            }

            // 添加代码块
            if (section % 3 == 0) {
                content.append("### 代码示例\n")
                content.append("```kotlin\n")
                content.append("fun section${section + 1}() {\n")
                content.append("    println(\"章节${section + 1}代码\")\n")
                content.append("    val data = listOf(${section * 10}, ${section * 20}, ${section * 30})\n")
                content.append("    data.forEach { println(it) }\n")
                content.append("}\n")
                content.append("```\n\n")
            }
        }

        content.append("## 测试结束\n")
        content.append("文档总大小：约${content.length}字符")

        return content.toString()
    }

    /**
     * 生成边界情况测试文件
     */
    fun generateEdgeCaseTest(): String {
        return """
            # 边界情况测试

            ## 空内容测试
            （这里故意留空）

            ## 只有空格




            ## 超长行测试
            ${"这是一个非常长的行，没有换行符，用于测试长文本的渲染和换行处理。" + "继续添加内容".repeat(100)}

            ## 特殊Unicode测试
            ### Emoji测试
            🚀 🌟 😊 👍 🎉 💯 ⭐ 🌈 🦄 🐱‍👤

            ### 中日韩文字
            中文测试 日本語テスト 한국어테스트

            ### 数学符号
            αβγδεζηθικλμνξοπρστυφχψω
            ΓΔΘΛΞΠΣΦΨΩ
            ∑∏∫∂∇∞≈≠≡≤≥

            ### 货币符号
            € £ ¥ $ ¢ ₩ ₹ ₽

            ## HTML实体测试
            &lt;div&gt; &amp; &quot; &apos; &copy; &reg; &trade; &nbsp;

            ## 混合换行符
            使用LF换行
            使用CRLF换行

            使用多个连续换行






            结束

            ## 非法Markdown语法测试
            ### 未闭合的语法
            **未闭合的粗体
            *未闭合的斜体
            `未闭合的代码
            [未闭合的链接
            ![未闭合的图片

            ### 嵌套过深
            > > > > > > > > > > 10层嵌套引用
            - - - - - - - - - - 10层嵌套列表

            ## 超大字体大小测试
            # 正常标题
            ###### 最小标题
            ####### 非法7级标题（应该渲染为段落）

            ## 表格边界测试
            |||
            |-|
            ||

            | 只有表头 | 没有内容 |
            |----------|----------|

            | 表头1 | 表头2 |
            |-------|-------|
            | 单元格1 | （空单元格） |

            ## 代码块边界
            ``````
            六个反引号
            ``````

            ~~~~~~
            波浪线代码块
            ~~~~~~

            ## 链接边界
            [空链接]()
            [](空文本)
            ![空图片]()

            ## 结束测试
            所有边界情况测试完成。
        """.trimIndent()
    }

    /**
     * 生成主题测试文件
     */
    fun generateThemeTest(): String {
        return """
            # 主题渲染测试

            ## 亮色/暗色主题对比测试

            ### 文本颜色
            - **粗体文本** - 应该在不同主题下保持可读性
            - *斜体文本* - 应该在不同主题下保持可读性
            - ~~删除线文本~~ - 应该在不同主题下保持可读性
            - `代码文本` - 应该在不同主题下有合适的背景色

            ### 链接颜色
            - [普通链接](https://example.com)
            - [访问过的链接](https://example.com) (可能显示不同颜色)
            - [悬停链接](https://example.com) (需要实际悬停测试)

            ### 表格样式
            | 元素 | 亮色主题 | 暗色主题 |
            |------|----------|----------|
            | 背景 | 白色/浅灰 | 深灰/黑色 |
            | 文字 | 黑色/深灰 | 白色/浅灰 |
            | 边框 | 浅灰色 | 深灰色 |
            | 表头 | 更深的背景 | 稍亮的背景 |

            ### 代码块样式
            ```kotlin
            // 代码块应该有合适的背景色
            fun testTheme() {
                val light = Color.White
                val dark = Color.Black
                println("主题测试")
            }
            ```

            ### 引用块样式
            > 引用块应该有明显的背景色和边框
            > 在不同主题下都应该清晰可辨

            ### 列表样式
            1. 有序列表项
            2. 应该有合适的标记颜色
                - 嵌套无序列表
                - 应该保持一致性

            ### 分隔线样式
            上面的内容

            ---

            下面的内容
            分隔线应该有合适的颜色和粗细

            ## 高对比度测试
            **重要：** 确保所有文本在高对比度模式下都清晰可读。

            ## 自定义CSS测试
            以下HTML/CSS用于测试主题覆盖：

            <div style="background: var(--background); color: var(--text); padding: 10px; border: 1px solid var(--border);">
                使用CSS变量的自定义样式
            </div>

            ## 字体测试
            ### 不同字体大小
            # 32px 标题
            ## 24px 二级标题
            正常16px正文
            <small>小号12px文本</small>

            ### 字体族测试
            <div style="font-family: monospace;">
                等宽字体测试
                `代码也是等宽字体`
            </div>

            ## 图片透明度测试
            ![透明PNG](https://example.com/transparent.png)
            透明图片应该与背景融合

            ## 阴影和效果测试
            <div style="box-shadow: 0 2px 4px rgba(0,0,0,0.1); padding: 10px; border-radius: 5px;">
                带阴影的盒子
                在不同主题下阴影应该合适
            </div>

            ## 结束语
            主题测试完成。请分别在亮色和暗色主题下检查所有元素的显示效果。
        """.trimIndent()
    }

    /**
     * 将测试内容保存到文件
     */
    fun saveTestFile(content: String, fileName: String, directory: File): Boolean {
        return try {
            val file = File(directory, fileName)
            FileWriter(file).use { writer ->
                writer.write(content)
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 生成所有测试文件
     */
    fun generateAllTestFiles(directory: File): Map<String, Boolean> {
        val results = mutableMapOf<String, Boolean>()

        results["basic_syntax.md"] = saveTestFile(
            generateBasicSyntaxTest(),
            "basic_syntax.md",
            directory
        )

        results["performance_test.md"] = saveTestFile(
            generatePerformanceTest(),
            "performance_test.md",
            directory
        )

        results["edge_cases.md"] = saveTestFile(
            generateEdgeCaseTest(),
            "edge_cases.md",
            directory
        )

        results["theme_test.md"] = saveTestFile(
            generateThemeTest(),
            "theme_test.md",
            directory
        )

        return results
    }
}