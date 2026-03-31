package com.example.mdmobile

import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer
import org.junit.Test
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

/**
 * Markdown兼容性测试套件
 * 测试CommonMark库对各种Markdown语法的支持
 */
class MarkdownCompatibilityTest {

    private val parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()

    /**
     * 测试基础Markdown语法
     */
    @Test
    fun testBasicSyntax() {
        val testCases = listOf(
            // 标题
            "# 标题1" to "<h1>标题1</h1>\n",
            "## 标题2" to "<h2>标题2</h2>\n",
            "### 标题3" to "<h3>标题3</h3>\n",

            // 粗体和斜体
            "**粗体文本**" to "<p><strong>粗体文本</strong></p>\n",
            "*斜体文本*" to "<p><em>斜体文本</em></p>\n",
            "***粗斜体***" to "<p><em><strong>粗斜体</strong></em></p>\n",

            // 删除线
            "~~删除线~~" to "<p><del>删除线</del></p>\n",

            // 链接
            "[链接文本](https://example.com)" to "<p><a href=\"https://example.com\">链接文本</a></p>\n",

            // 图片
            "![图片alt](image.jpg)" to "<p><img src=\"image.jpg\" alt=\"图片alt\" /></p>\n",

            // 行内代码
            "`行内代码`" to "<p><code>行内代码</code></p>\n",

            // 引用
            "> 引用文本" to "<blockquote>\n<p>引用文本</p>\n</blockquote>\n"
        )

        testCases.forEach { (markdown, expected) ->
            val document = parser.parse(markdown)
            val html = renderer.render(document)
            assertEquals("Markdown语法测试失败: $markdown", expected.trim(), html.trim())
        }
    }

    /**
     * 测试列表语法
     */
    @Test
    fun testLists() {
        val unorderedList = """
            - 项目1
            - 项目2
            - 项目3
        """.trimIndent()

        val orderedList = """
            1. 第一项
            2. 第二项
            3. 第三项
        """.trimIndent()

        val nestedList = """
            - 外层项目
              - 内层项目1
              - 内层项目2
            - 另一个外层项目
        """.trimIndent()

        // 无序列表测试
        val unorderedDoc = parser.parse(unorderedList)
        val unorderedHtml = renderer.render(unorderedDoc)
        assertTrue("无序列表应包含ul标签", unorderedHtml.contains("<ul>"))
        assertTrue("无序列表应包含li标签", unorderedHtml.contains("<li>"))

        // 有序列表测试
        val orderedDoc = parser.parse(orderedList)
        val orderedHtml = renderer.render(orderedDoc)
        assertTrue("有序列表应包含ol标签", orderedHtml.contains("<ol>"))

        // 嵌套列表测试
        val nestedDoc = parser.parse(nestedList)
        val nestedHtml = renderer.render(nestedDoc)
        assertTrue("嵌套列表应包含多个ul标签", nestedHtml.split("<ul>").size > 2)
    }

    /**
     * 测试代码块语法
     */
    @Test
    fun testCodeBlocks() {
        val simpleCodeBlock = """
            ```kotlin
            fun main() {
                println("Hello, World!")
            }
            ```
        """.trimIndent()

        val inlineCodeBlock = """
            这是一个`行内代码`示例。
        """.trimIndent()

        // 代码块测试
        val codeDoc = parser.parse(simpleCodeBlock)
        val codeHtml = renderer.render(codeDoc)
        assertTrue("代码块应包含pre标签", codeHtml.contains("<pre>"))
        assertTrue("代码块应包含code标签", codeHtml.contains("<code>"))
        assertTrue("代码块应包含语言类", codeHtml.contains("language-kotlin"))

        // 行内代码测试
        val inlineDoc = parser.parse(inlineCodeBlock)
        val inlineHtml = renderer.render(inlineDoc)
        assertTrue("行内代码应包含code标签", inlineHtml.contains("<code>"))
        assertFalse("行内代码不应包含pre标签", inlineHtml.contains("<pre>"))
    }

    /**
     * 测试表格语法
     */
    @Test
    fun testTables() {
        val tableMarkdown = """
            | 表头1 | 表头2 | 表头3 |
            |-------|-------|-------|
            | 单元格1 | 单元格2 | 单元格3 |
            | 单元格4 | 单元格5 | 单元格6 |
        """.trimIndent()

        val document = parser.parse(tableMarkdown)
        val html = renderer.render(document)

        assertTrue("表格应包含table标签", html.contains("<table>"))
        assertTrue("表格应包含thead标签", html.contains("<thead>"))
        assertTrue("表格应包含tbody标签", html.contains("<tbody>"))
        assertTrue("表格应包含th标签", html.contains("<th>"))
        assertTrue("表格应包含td标签", html.contains("<td>"))
    }

    /**
     * 测试任务列表语法
     */
    @Test
    fun testTaskLists() {
        val taskList = """
            - [ ] 未完成任务
            - [x] 已完成任务
            - [X] 大写已完成任务
        """.trimIndent()

        val document = parser.parse(taskList)
        val html = renderer.render(document)

        // CommonMark默认可能不支持任务列表，需要扩展
        // 这里我们测试基本的列表渲染
        assertTrue("任务列表应包含ul标签", html.contains("<ul>"))
        assertTrue("任务列表应包含li标签", html.contains("<li>"))
    }

    /**
     * 测试分隔线语法
     */
    @Test
    fun testHorizontalRules() {
        val separators = listOf(
            "---",
            "***",
            "___"
        )

        separators.forEach { separator ->
            val document = parser.parse(separator)
            val html = renderer.render(document)
            assertTrue("分隔线应包含hr标签: $separator", html.contains("<hr />"))
        }
    }

    /**
     * 测试转义字符
     */
    @Test
    fun testEscapedCharacters() {
        val testCases = listOf(
            "\\*不会斜体\\*" to "<p>*不会斜体*</p>\n",
            "\\# 不是标题" to "<p># 不是标题</p>\n",
            "\\[不是链接\\](not-a-link)" to "<p>[不是链接](not-a-link)</p>\n"
        )

        testCases.forEach { (markdown, expected) ->
            val document = parser.parse(markdown)
            val html = renderer.render(document)
            assertEquals("转义字符测试失败: $markdown", expected.trim(), html.trim())
        }
    }

    /**
     * 测试HTML实体
     */
    @Test
    fun testHtmlEntities() {
        val testCases = listOf(
            "&lt;div&gt;" to "<p>&lt;div&gt;</p>\n",
            "&amp;符号" to "<p>&amp;符号</p>\n",
            "&copy;版权" to "<p>&copy;版权</p>\n"
        )

        testCases.forEach { (markdown, expected) ->
            val document = parser.parse(markdown)
            val html = renderer.render(document)
            assertEquals("HTML实体测试失败: $markdown", expected.trim(), html.trim())
        }
    }

    /**
     * 测试混合复杂语法
     */
    @Test
    fun testComplexMixedSyntax() {
        val complexMarkdown = """
            # 复杂文档示例

            ## 章节1

            这是一个**粗体**和*斜体*的混合文本，包含`行内代码`。

            - 列表项1
            - 列表项2
              - 嵌套列表项
              - 另一个嵌套项

            > 引用文本
            > 多行引用

            | 表头 | 描述 |
            |------|------|
            | 项目1 | 描述1 |
            | 项目2 | 描述2 |

            ```java
            public class Example {
                public static void main(String[] args) {
                    System.out.println("Hello");
                }
            }
            ```

            ---

            最后一段文本包含[链接](https://example.com)和![图片](image.png)。
        """.trimIndent()

        val document = parser.parse(complexMarkdown)
        val html = renderer.render(document)

        // 验证所有主要元素都存在
        assertTrue("应包含h1标题", html.contains("<h1>"))
        assertTrue("应包含h2标题", html.contains("<h2>"))
        assertTrue("应包含粗体", html.contains("<strong>"))
        assertTrue("应包含斜体", html.contains("<em>"))
        assertTrue("应包含代码", html.contains("<code>"))
        assertTrue("应包含列表", html.contains("<ul>"))
        assertTrue("应包含引用", html.contains("<blockquote>"))
        assertTrue("应包含表格", html.contains("<table>"))
        assertTrue("应包含分隔线", html.contains("<hr />"))
        assertTrue("应包含链接", html.contains("<a href="))
        assertTrue("应包含图片", html.contains("<img src="))
    }

    /**
     * 测试空输入和边界情况
     */
    @Test
    fun testEdgeCases() {
        // 空输入
        val emptyDoc = parser.parse("")
        val emptyHtml = renderer.render(emptyDoc)
        assertEquals("空输入应生成空字符串", "", emptyHtml.trim())

        // 只有空格
        val spacesDoc = parser.parse("   \n  \t  \n  ")
        val spacesHtml = renderer.render(spacesDoc)
        assertEquals("只有空格应生成空字符串", "", spacesHtml.trim())

        // 非常长的行
        val longLine = "A".repeat(1000)
        val longDoc = parser.parse(longLine)
        val longHtml = renderer.render(longDoc)
        assertTrue("长行应正确渲染", longHtml.contains(longLine))

        // 特殊Unicode字符
        val unicodeDoc = parser.parse("中文测试 🚀 🌟 😊")
        val unicodeHtml = renderer.render(unicodeDoc)
        assertTrue("应包含中文字符", unicodeHtml.contains("中文测试"))
        assertTrue("应包含表情符号", unicodeHtml.contains("🚀"))
    }
}

/**
 * 参数化测试类，用于测试多种Markdown变体
 */
@RunWith(Parameterized::class)
class ParameterizedMarkdownTest(
    private val markdown: String,
    private val shouldContain: String
) {

    companion object {
        @JvmStatic
        @Parameterized.Parameters
        fun data(): Collection<Array<Any>> {
            return listOf(
                arrayOf("# 标题", "<h1>"),
                arrayOf("**粗体**", "<strong>"),
                arrayOf("*斜体*", "<em>"),
                arrayOf("[链接](url)", "<a href="),
                arrayOf("![图片](img)", "<img src="),
                arrayOf("`代码`", "<code>"),
                arrayOf("> 引用", "<blockquote>"),
                arrayOf("- 列表", "<li>"),
                arrayOf("1. 项目", "<li>"),
                arrayOf("---", "<hr />")
            )
        }
    }

    private val parser = Parser.builder().build()
    private val renderer = HtmlRenderer.builder().build()

    @Test
    fun testParameterized() {
        val document = parser.parse(markdown)
        val html = renderer.render(document)
        assertTrue("参数化测试失败: $markdown 应包含 $shouldContain",
                   html.contains(shouldContain))
    }
}