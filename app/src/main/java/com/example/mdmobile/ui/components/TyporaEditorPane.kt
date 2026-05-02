package com.example.mdmobile.ui.components

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

@Composable
fun TyporaEditorPane(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusLost: () -> Unit,
    fontSize: Int,
    modifier: Modifier = Modifier
) {
    var wasFocused by remember { mutableStateOf(false) }
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    val scrollState = rememberScrollState()
    val blockPlan = remember(value.text) { buildMarkdownEditBlockPlan(value.text) }
    val clipboardManager = LocalClipboardManager.current
    val context = LocalContext.current
    val density = LocalDensity.current
    val codeBlockColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.72f)
    val quoteBlockColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.58f)
    val dividerColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.56f)

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 22.dp, vertical = 20.dp)
        ) {
            if (value.text.isBlank()) {
                Text(
                    text = "从这里开始写作，当前行会淡淡显示 Markdown 语法，其余内容尽量接近排版效果。",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = fontSize.sp,
                        lineHeight = (fontSize * 1.75f).sp
                    ),
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
            }

            BasicTextField(
                value = value,
                onValueChange = { proposed ->
                    onValueChange(continueMarkdownListOnEnter(value, proposed))
                },
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .drawBehind {
                        val layout = textLayoutResult ?: return@drawBehind
                        drawMarkdownBlockDecorations(
                            layout = layout,
                            blocks = blockPlan,
                            codeBlockColor = codeBlockColor,
                            quoteBlockColor = quoteBlockColor,
                            dividerColor = dividerColor
                        )
                    }
                    .onFocusChanged { focusState ->
                        if (wasFocused && !focusState.isFocused) {
                            onFocusLost()
                        }
                        wasFocused = focusState.isFocused
                    },
                onTextLayout = { textLayoutResult = it },
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = fontSize.sp,
                    lineHeight = (fontSize * 1.75f).sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                visualTransformation = markdownEditVisualTransformation(
                    cursorPosition = value.selection.end,
                    fontSize = fontSize,
                    baseTextColor = MaterialTheme.colorScheme.onSurface,
                    mutedTextColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.45f),
                    headingColor = MaterialTheme.colorScheme.onSurface,
                    accentColor = MaterialTheme.colorScheme.primary,
                    quoteColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f),
                    codeColor = MaterialTheme.colorScheme.onSurface,
                    concealedColor = MaterialTheme.colorScheme.surface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
            )

            val layout = textLayoutResult
            if (layout != null) {
                val endPadding = with(density) { 8.dp.roundToPx() }
                val topPadding = with(density) { 6.dp.toPx() }
                blockPlan
                    .filter { it.kind == MarkdownEditBlockKind.Code }
                    .forEach { block ->
                        val textLength = layout.layoutInput.text.text.length
                        if (textLength > 0 && block.start < textLength) {
                            val line = layout.getLineForOffset(block.start.coerceIn(0, textLength - 1))
                            val top = (layout.getLineTop(line) - scrollState.value + topPadding).roundToInt()
                            IconButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(block.copyText))
                                    Toast.makeText(context, "代码已复制", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .offset { IntOffset(x = -endPadding, y = top) }
                                    .size(32.dp)
                                    .background(
                                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                        shape = RoundedCornerShape(10.dp)
                                    )
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "复制代码",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
            }
        }
    }
}

private fun DrawScope.drawMarkdownBlockDecorations(
    layout: TextLayoutResult,
    blocks: List<MarkdownEditBlock>,
    codeBlockColor: Color,
    quoteBlockColor: Color,
    dividerColor: Color
) {
    val textLength = layout.layoutInput.text.text.length
    if (textLength == 0) return

    blocks.forEach { block ->
        val start = block.start.coerceIn(0, textLength - 1)
        val end = (block.end - 1).coerceIn(0, textLength - 1)
        val firstLine = layout.getLineForOffset(start)
        val lastLine = layout.getLineForOffset(end)
        val top = layout.getLineTop(firstLine)
        val bottom = layout.getLineBottom(lastLine)

        when (block.kind) {
            MarkdownEditBlockKind.Code -> drawRoundRect(
                color = codeBlockColor,
                topLeft = Offset(0f, top - 6.dp.toPx()),
                size = Size(size.width, bottom - top + 12.dp.toPx()),
                cornerRadius = CornerRadius(14.dp.toPx(), 14.dp.toPx())
            )

            MarkdownEditBlockKind.Blockquote -> {
                drawRoundRect(
                    color = quoteBlockColor,
                    topLeft = Offset(0f, top - 4.dp.toPx()),
                    size = Size(size.width, bottom - top + 8.dp.toPx()),
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx())
                )
                drawRoundRect(
                    color = dividerColor.copy(alpha = 0.8f),
                    topLeft = Offset(0f, top),
                    size = Size(3.dp.toPx(), bottom - top),
                    cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                )
            }

            MarkdownEditBlockKind.Divider -> {
                val centerY = (top + bottom) / 2f
                drawLine(
                    color = dividerColor,
                    start = Offset(0f, centerY),
                    end = Offset(size.width, centerY),
                    strokeWidth = 1.dp.toPx()
                )
            }
        }
    }
}

@Composable
private fun markdownEditVisualTransformation(
    cursorPosition: Int,
    fontSize: Int,
    baseTextColor: Color,
    mutedTextColor: Color,
    headingColor: Color,
    accentColor: Color,
    quoteColor: Color,
    codeColor: Color,
    concealedColor: Color
): VisualTransformation {
    val normalStyle = SpanStyle(color = baseTextColor)
    val syntaxStyle = SpanStyle(color = mutedTextColor)
    val concealedSyntaxStyle = SpanStyle(
        color = concealedColor.copy(alpha = 0.02f),
        fontSize = 1.sp
    )
    val codeStyle = SpanStyle(
        color = codeColor,
        fontFamily = FontFamily.Monospace
    )
    return remember(cursorPosition, fontSize, baseTextColor, mutedTextColor, headingColor, accentColor, quoteColor, codeColor, concealedColor) {
        VisualTransformation { text ->
            val plan = buildMarkdownEditStylePlan(text.text, cursorPosition)
            val transformed = buildAnnotatedString {
                append(text.text)
                if (text.text.isNotEmpty()) {
                    addStyle(normalStyle, 0, text.text.length)
                }
                plan.ranges.forEach { range ->
                    addStyle(styleForRange(range.kind, fontSize, syntaxStyle, headingColor, accentColor, quoteColor, codeStyle), range.start, range.end)
                }
                plan.ranges
                    .filter { it.shouldConcealInTyporaView(plan.activeLine) }
                    .forEach { addStyle(concealedSyntaxStyle, it.start, it.end) }
            }
            TransformedText(transformed, OffsetMapping.Identity)
        }
    }
}

private fun styleForRange(
    kind: MarkdownEditStyleKind,
    fontSize: Int,
    syntaxStyle: SpanStyle,
    headingColor: Color,
    accentColor: Color,
    quoteColor: Color,
    codeStyle: SpanStyle
): SpanStyle {
    return when (kind) {
        MarkdownEditStyleKind.SyntaxMarker,
        MarkdownEditStyleKind.ListMarker,
        MarkdownEditStyleKind.BlockquoteMarker,
        MarkdownEditStyleKind.CodeFence,
        MarkdownEditStyleKind.Divider -> syntaxStyle

        MarkdownEditStyleKind.Heading1 -> SpanStyle(
            color = headingColor,
            fontWeight = FontWeight.Bold,
            fontSize = (fontSize * 1.48f).sp
        )

        MarkdownEditStyleKind.Heading2 -> SpanStyle(
            color = headingColor,
            fontWeight = FontWeight.Bold,
            fontSize = (fontSize * 1.3f).sp
        )

        MarkdownEditStyleKind.Heading3 -> SpanStyle(
            color = headingColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = (fontSize * 1.16f).sp
        )

        MarkdownEditStyleKind.Heading4,
        MarkdownEditStyleKind.Heading5,
        MarkdownEditStyleKind.Heading6 -> SpanStyle(
            color = headingColor,
            fontWeight = FontWeight.SemiBold,
            fontSize = (fontSize * 1.04f).sp
        )

        MarkdownEditStyleKind.BlockquoteText -> SpanStyle(color = quoteColor)
        MarkdownEditStyleKind.CodeBlock,
        MarkdownEditStyleKind.InlineCode -> codeStyle
        MarkdownEditStyleKind.BoldText -> SpanStyle(fontWeight = FontWeight.Bold)
        MarkdownEditStyleKind.ItalicText -> SpanStyle(fontStyle = FontStyle.Italic)
        MarkdownEditStyleKind.LinkText -> SpanStyle(color = accentColor, fontWeight = FontWeight.Medium)
        MarkdownEditStyleKind.ImageAltText -> SpanStyle(color = accentColor, fontWeight = FontWeight.Medium)
        MarkdownEditStyleKind.LinkDestination -> syntaxStyle
    }
}
