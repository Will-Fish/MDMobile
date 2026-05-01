package com.example.mdmobile.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun TyporaEditorPane(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    onFocusLost: () -> Unit,
    fontSize: Int,
    modifier: Modifier = Modifier
) {
    var wasFocused by remember { mutableStateOf(false) }
    val scrollState = rememberScrollState()

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
                onValueChange = onValueChange,
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(scrollState)
                    .onFocusChanged { focusState ->
                        if (wasFocused && !focusState.isFocused) {
                            onFocusLost()
                        }
                        wasFocused = focusState.isFocused
                    },
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
                    quoteColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    codeColor = MaterialTheme.colorScheme.primary,
                    concealedColor = MaterialTheme.colorScheme.surface
                ),
                cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                keyboardOptions = KeyboardOptions.Default.copy(imeAction = ImeAction.Default)
            )
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
        MarkdownEditStyleKind.CodeFence -> syntaxStyle

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
