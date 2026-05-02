package com.example.mdmobile

import com.example.mdmobile.ui.components.MarkdownEditStyleKind
import com.example.mdmobile.ui.components.MarkdownEditBlockKind
import com.example.mdmobile.ui.components.buildMarkdownEditBlockPlan
import com.example.mdmobile.ui.components.shouldConcealInTyporaView
import com.example.mdmobile.ui.components.buildMarkdownEditStylePlan
import com.example.mdmobile.ui.components.continueMarkdownListOnEnter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.TextFieldValue

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

    @Test
    fun inactiveSourceSyntaxIsConcealedButActiveLineSyntaxStaysVisible() {
        val markdown = "# 标题\n\n[链接](https://example.com)"
        val cursor = markdown.indexOf("链接")
        val plan = buildMarkdownEditStylePlan(markdown, cursor)

        val inactiveHeadingMarker = plan.ranges.single {
            it.kind == MarkdownEditStyleKind.SyntaxMarker && it.start == 0 && it.end == 2
        }
        val activeLinkDestination = plan.ranges.single {
            it.kind == MarkdownEditStyleKind.LinkDestination
        }

        assertTrue(inactiveHeadingMarker.shouldConcealInTyporaView(plan.activeLine))
        assertFalse(activeLinkDestination.shouldConcealInTyporaView(plan.activeLine))
    }

    @Test
    fun inactiveListMarkersStayVisibleInTyporaView() {
        val markdown = "- 第一项\n普通段落"
        val cursor = markdown.indexOf("普通")
        val plan = buildMarkdownEditStylePlan(markdown, cursor)

        val listMarker = plan.ranges.single { it.kind == MarkdownEditStyleKind.ListMarker }

        assertFalse(listMarker.shouldConcealInTyporaView(plan.activeLine))
    }

    @Test
    fun enterContinuesUnorderedList() {
        val previous = TextFieldValue("- 第一项", selection = TextRange(5))
        val proposed = TextFieldValue("- 第一项\n", selection = TextRange(6))

        val continued = continueMarkdownListOnEnter(previous, proposed)

        assertEquals("- 第一项\n- ", continued.text)
        assertEquals(TextRange(8), continued.selection)
    }

    @Test
    fun enterIncrementsOrderedList() {
        val previous = TextFieldValue("9. 第九项", selection = TextRange(6))
        val proposed = TextFieldValue("9. 第九项\n", selection = TextRange(7))

        val continued = continueMarkdownListOnEnter(previous, proposed)

        assertEquals("9. 第九项\n10. ", continued.text)
        assertEquals(TextRange(11), continued.selection)
    }

    @Test
    fun enterOnEmptyListItemExitsList() {
        val previous = TextFieldValue("正文\n- ", selection = TextRange(5))
        val proposed = TextFieldValue("正文\n- \n", selection = TextRange(6))

        val continued = continueMarkdownListOnEnter(previous, proposed)

        assertEquals("正文\n\n", continued.text)
        assertEquals(TextRange(4), continued.selection)
    }

    @Test
    fun detectsCodeQuoteAndDividerBlocks() {
        val markdown = "段落\n```kotlin\nprintln(1)\n```\n> 引用\n> 继续\n---\n尾巴"

        val blocks = buildMarkdownEditBlockPlan(markdown)

        val code = blocks.single { it.kind == MarkdownEditBlockKind.Code }
        val quote = blocks.single { it.kind == MarkdownEditBlockKind.Blockquote }
        val divider = blocks.single { it.kind == MarkdownEditBlockKind.Divider }
        assertEquals("println(1)", code.copyText)
        assertEquals(markdown.indexOf("```kotlin"), code.start)
        assertEquals(markdown.indexOf("> 引用"), quote.start)
        assertEquals(markdown.indexOf("---"), divider.start)
    }

    @Test
    fun dividerMarkerIsConcealedEvenWhenCursorIsOnDividerLine() {
        val markdown = "正文\n---\n尾巴"
        val cursor = markdown.indexOf("---") + 1

        val plan = buildMarkdownEditStylePlan(markdown, cursor)
        val divider = plan.ranges.single { it.kind == MarkdownEditStyleKind.Divider }

        assertTrue(divider.shouldConcealInTyporaView(plan.activeLine))
    }

    private fun com.example.mdmobile.ui.components.MarkdownEditStylePlan.hasRange(
        kind: MarkdownEditStyleKind,
        start: Int,
        end: Int
    ): Boolean = ranges.any { it.kind == kind && it.start == start && it.end == end }
}
