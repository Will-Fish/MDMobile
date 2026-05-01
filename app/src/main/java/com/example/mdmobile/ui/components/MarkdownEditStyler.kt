package com.example.mdmobile.ui.components

import kotlin.math.max

enum class MarkdownEditStyleKind {
    SyntaxMarker,
    Heading1,
    Heading2,
    Heading3,
    Heading4,
    Heading5,
    Heading6,
    ListMarker,
    BlockquoteMarker,
    BlockquoteText,
    CodeFence,
    CodeBlock,
    BoldText,
    ItalicText,
    InlineCode,
    LinkText,
    ImageAltText,
    LinkDestination
}

data class MarkdownEditStyleRange(
    val kind: MarkdownEditStyleKind,
    val start: Int,
    val end: Int
)

data class MarkdownEditLineRange(
    val start: Int,
    val end: Int
)

data class MarkdownEditStylePlan(
    val sourceLength: Int,
    val activeLine: MarkdownEditLineRange,
    val ranges: List<MarkdownEditStyleRange>
)

fun MarkdownEditStyleRange.shouldConcealInTyporaView(
    activeLine: MarkdownEditLineRange
): Boolean {
    val onActiveLine = start >= activeLine.start && end <= activeLine.end
    return !onActiveLine && kind.isSourceSyntax()
}

fun buildMarkdownEditStylePlan(
    markdown: String,
    cursorPosition: Int
): MarkdownEditStylePlan {
    val safeCursor = cursorPosition.coerceIn(0, markdown.length)
    val activeLine = findActiveLine(markdown, safeCursor)
    val ranges = mutableListOf<MarkdownEditStyleRange>()
    var lineStart = 0
    var inFence = false

    markdown.splitToSequence('\n').forEach { line ->
        val lineEnd = lineStart + line.length
        val trimmedStart = line.indexOfFirst { !it.isWhitespace() }.let { if (it == -1) line.length else it }
        val contentStart = lineStart + trimmedStart
        val trimmed = line.drop(trimmedStart)
        val fenceMatch = Regex("^(`{3,}|~{3,})").find(trimmed)

        if (fenceMatch != null) {
            ranges.addStyle(MarkdownEditStyleKind.CodeFence, contentStart, lineEnd)
            inFence = !inFence
        } else if (inFence) {
            ranges.addStyle(MarkdownEditStyleKind.CodeBlock, lineStart, lineEnd)
        } else {
            addBlockStyles(line, lineStart, ranges)
            addInlineStyles(line, lineStart, ranges)
        }

        lineStart = lineEnd + 1
    }

    return MarkdownEditStylePlan(
        sourceLength = markdown.length,
        activeLine = activeLine,
        ranges = ranges
            .filter { it.start >= 0 && it.end <= markdown.length && it.start < it.end }
            .sortedWith(compareBy<MarkdownEditStyleRange> { it.start }.thenBy { it.end })
    )
}

private fun findActiveLine(markdown: String, cursorPosition: Int): MarkdownEditLineRange {
    val safeCursor = cursorPosition.coerceIn(0, markdown.length)
    val start = markdown.lastIndexOf('\n', max(0, safeCursor - 1)).let { if (it == -1) 0 else it + 1 }
    val end = markdown.indexOf('\n', safeCursor).let { if (it == -1) markdown.length else it }
    return MarkdownEditLineRange(start, end)
}

private fun addBlockStyles(
    line: String,
    lineStart: Int,
    ranges: MutableList<MarkdownEditStyleRange>
) {
    val heading = Regex("^(\\s*)(#{1,6})\\s+(.+)$").find(line)
    if (heading != null) {
        val markerStart = lineStart + heading.groupValues[1].length
        val markerEnd = markerStart + heading.groupValues[2].length + 1
        val contentStart = markerEnd
        val contentEnd = lineStart + line.length
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, markerStart, markerEnd)
        ranges.addStyle(headingKind(heading.groupValues[2].length), contentStart, contentEnd)
        return
    }

    val unordered = Regex("^(\\s*)([-+*])\\s+").find(line)
    if (unordered != null) {
        val markerStart = lineStart + unordered.groupValues[1].length
        ranges.addStyle(MarkdownEditStyleKind.ListMarker, markerStart, markerStart + unordered.value.length - unordered.groupValues[1].length)
    }

    val ordered = Regex("^(\\s*)(\\d+[.)])\\s+").find(line)
    if (ordered != null) {
        val markerStart = lineStart + ordered.groupValues[1].length
        ranges.addStyle(MarkdownEditStyleKind.ListMarker, markerStart, markerStart + ordered.value.length - ordered.groupValues[1].length)
    }

    val blockquote = Regex("^(\\s*>\\s?)").find(line)
    if (blockquote != null) {
        val markerEnd = lineStart + blockquote.value.length
        ranges.addStyle(MarkdownEditStyleKind.BlockquoteMarker, lineStart, markerEnd)
        ranges.addStyle(MarkdownEditStyleKind.BlockquoteText, markerEnd, lineStart + line.length)
    }
}

private fun addInlineStyles(
    line: String,
    lineStart: Int,
    ranges: MutableList<MarkdownEditStyleRange>
) {
    Regex("(!?)\\[([^\\]\\n]+)]\\(([^)\\n]+)\\)").findAll(line).forEach { match ->
        val isImage = match.groupValues[1] == "!"
        val absoluteStart = lineStart + match.range.first
        val altStart = absoluteStart + if (isImage) 2 else 1
        val altEnd = altStart + match.groupValues[2].length
        val destinationStart = altEnd + 2
        val destinationEnd = destinationStart + match.groupValues[3].length
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, absoluteStart, altStart)
        ranges.addStyle(
            if (isImage) MarkdownEditStyleKind.ImageAltText else MarkdownEditStyleKind.LinkText,
            altStart,
            altEnd
        )
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, altEnd, destinationStart)
        ranges.addStyle(MarkdownEditStyleKind.LinkDestination, destinationStart, destinationEnd)
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, destinationEnd, destinationEnd + 1)
    }

    Regex("`([^`\\n]+)`").findAll(line).forEach { match ->
        val start = lineStart + match.range.first
        val contentStart = start + 1
        val contentEnd = contentStart + match.groupValues[1].length
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, start, contentStart)
        ranges.addStyle(MarkdownEditStyleKind.InlineCode, contentStart, contentEnd)
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, contentEnd, contentEnd + 1)
    }

    Regex("(\\*\\*|__)(.+?)\\1").findAll(line).forEach { match ->
        val start = lineStart + match.range.first
        val markerLength = match.groupValues[1].length
        val contentStart = start + markerLength
        val contentEnd = contentStart + match.groupValues[2].length
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, start, contentStart)
        ranges.addStyle(MarkdownEditStyleKind.BoldText, contentStart, contentEnd)
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, contentEnd, contentEnd + markerLength)
    }

    Regex("(?<!\\*)\\*([^*\\n]+)\\*(?!\\*)|(?<!_)_([^_\\n]+)_(?!_)").findAll(line).forEach { match ->
        val content = match.groupValues[1].ifEmpty { match.groupValues[2] }
        if (content.isBlank()) return@forEach
        val start = lineStart + match.range.first
        val contentStart = start + 1
        val contentEnd = contentStart + content.length
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, start, contentStart)
        ranges.addStyle(MarkdownEditStyleKind.ItalicText, contentStart, contentEnd)
        ranges.addStyle(MarkdownEditStyleKind.SyntaxMarker, contentEnd, contentEnd + 1)
    }
}

private fun headingKind(level: Int): MarkdownEditStyleKind {
    return when (level.coerceIn(1, 6)) {
        1 -> MarkdownEditStyleKind.Heading1
        2 -> MarkdownEditStyleKind.Heading2
        3 -> MarkdownEditStyleKind.Heading3
        4 -> MarkdownEditStyleKind.Heading4
        5 -> MarkdownEditStyleKind.Heading5
        else -> MarkdownEditStyleKind.Heading6
    }
}

private fun MarkdownEditStyleKind.isSourceSyntax(): Boolean {
    return when (this) {
        MarkdownEditStyleKind.SyntaxMarker,
        MarkdownEditStyleKind.ListMarker,
        MarkdownEditStyleKind.BlockquoteMarker,
        MarkdownEditStyleKind.CodeFence,
        MarkdownEditStyleKind.LinkDestination -> true

        MarkdownEditStyleKind.Heading1,
        MarkdownEditStyleKind.Heading2,
        MarkdownEditStyleKind.Heading3,
        MarkdownEditStyleKind.Heading4,
        MarkdownEditStyleKind.Heading5,
        MarkdownEditStyleKind.Heading6,
        MarkdownEditStyleKind.BlockquoteText,
        MarkdownEditStyleKind.CodeBlock,
        MarkdownEditStyleKind.BoldText,
        MarkdownEditStyleKind.ItalicText,
        MarkdownEditStyleKind.InlineCode,
        MarkdownEditStyleKind.LinkText,
        MarkdownEditStyleKind.ImageAltText -> false
    }
}

private fun MutableList<MarkdownEditStyleRange>.addStyle(
    kind: MarkdownEditStyleKind,
    start: Int,
    end: Int
) {
    if (start < end) {
        add(MarkdownEditStyleRange(kind = kind, start = start, end = end))
    }
}
