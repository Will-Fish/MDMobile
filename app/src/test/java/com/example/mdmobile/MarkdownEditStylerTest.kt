package com.example.mdmobile

import com.example.mdmobile.ui.components.MarkdownEditStyleKind
import com.example.mdmobile.ui.components.buildMarkdownEditStylePlan
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MarkdownEditStylerTest {
    @Test
    fun detectsActiveLineFromCursorPosition() {
        val markdown = "# 标题\n\n正文第二行"
        val cursor = markdown.indexOf("正文")

        val plan = buildMarkdownEditStylePlan(markdown, cursor)

        assertEquals(cursor, plan.activeLine.start)
        assertEquals(markdown.length, plan.activeLine.end)
    }

    @Test
    fun stylesHeadingMarkersAndContent() {
        val plan = buildMarkdownEditStylePlan("## 小标题", cursorPosition = 0)

        assertTrue(plan.hasRange(MarkdownEditStyleKind.SyntaxMarker, 0, 3))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.Heading2, 3, 6))
    }

    @Test
    fun stylesListsAndBlockquotes() {
        val markdown = "- 第一项\n12. 第二项\n> 引用"
        val plan = buildMarkdownEditStylePlan(markdown, cursorPosition = markdown.length)

        assertTrue(plan.hasRange(MarkdownEditStyleKind.ListMarker, 0, 2))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.ListMarker, 6, 10))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.BlockquoteMarker, 14, 16))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.BlockquoteText, 16, 18))
    }

    @Test
    fun stylesFencedCodeBlockWithoutBreakingBlankLines() {
        val markdown = "```kotlin\nfun main() {\n\n}\n```"
        val plan = buildMarkdownEditStylePlan(markdown, cursorPosition = 0)

        assertTrue(plan.hasRange(MarkdownEditStyleKind.CodeFence, 0, 9))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.CodeBlock, 10, 22))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.CodeFence, 26, 29))
    }

    @Test
    fun stylesInlineMarkupTokensAndContent() {
        val markdown = "**粗体** *斜体* `代码` [链接](https://example.com) ![图](image.png)"
        val plan = buildMarkdownEditStylePlan(markdown, cursorPosition = markdown.length)

        assertTrue(plan.hasRange(MarkdownEditStyleKind.SyntaxMarker, 0, 2))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.BoldText, 2, 4))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.ItalicText, 8, 10))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.InlineCode, 13, 15))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.LinkText, 18, 20))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.LinkDestination, 22, 41))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.ImageAltText, 45, 46))
        assertTrue(plan.hasRange(MarkdownEditStyleKind.LinkDestination, 48, 57))
    }

    @Test
    fun malformedMarkdownDoesNotThrowOrInventClosedRanges() {
        val markdown = "**未闭合 [链接"
        val plan = buildMarkdownEditStylePlan(markdown, cursorPosition = markdown.length)

        assertEquals(markdown.length, plan.sourceLength)
        assertFalse(plan.ranges.any { it.end > markdown.length || it.start < 0 || it.start > it.end })
    }

    private fun com.example.mdmobile.ui.components.MarkdownEditStylePlan.hasRange(
        kind: MarkdownEditStyleKind,
        start: Int,
        end: Int
    ): Boolean = ranges.any { it.kind == kind && it.start == start && it.end == end }
}
