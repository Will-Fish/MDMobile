package com.example.mdmobile

import com.example.mdmobile.ui.components.renderMarkdownPreviewHtml
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownRendererHtmlTest {
    @Test
    fun fencedHtmlCodeStaysEscapedWithoutLeakingRenderedTags() {
        val html = renderMarkdownPreviewHtml(
            markdown = """
                ```html
                <div class="note">Hello</div>
                ```
            """.trimIndent(),
            isDarkTheme = false,
            fontSize = 16
        )

        assertTrue(html.contains("&lt;div class=&quot;note&quot;&gt;Hello&lt;/div&gt;"))
        assertFalse(html.contains("<div class=\"note\">Hello</div>"))
    }

    @Test
    fun softLineBreaksRenderAsVisibleBreaks() {
        val html = renderMarkdownPreviewHtml(
            markdown = "第一行\n第二行\n第三行",
            isDarkTheme = false,
            fontSize = 16
        )

        assertTrue(html.contains("第一行<br"))
        assertTrue(html.contains("第二行<br"))
    }

    @Test
    fun repeatedBlankLinesRenderAsVisibleSpacing() {
        val html = renderMarkdownPreviewHtml(
            markdown = "第一行\n\n\n\n第五行",
            isDarkTheme = false,
            fontSize = 16
        )

        assertTrue(html.contains("第一行"))
        assertTrue(html.contains("第五行"))
        assertTrue(Regex("<br\\s*/?>").findAll(html).count() >= 3)
    }

    @Test
    fun fencedCodeKeepsLineBreaksAndBlankLinesInsideCodeBlock() {
        val html = renderMarkdownPreviewHtml(
            markdown = """
                ```kotlin
                fun main() {

                    println("<hello>")
                }
                ```
            """.trimIndent(),
            isDarkTheme = false,
            fontSize = 16
        )

        assertTrue(html.contains("<pre><code class=\"language-kotlin\">"))
        assertTrue(html.contains("FUN MAIN", ignoreCase = true))
        assertTrue(html.contains("&lt;hello&gt;"))
        assertFalse(html.contains("code-table"))
    }
}
