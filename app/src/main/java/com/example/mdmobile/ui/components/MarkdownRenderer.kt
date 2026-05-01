package com.example.mdmobile.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Color as AndroidColor
import android.webkit.JavascriptInterface
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import com.example.mdmobile.utils.ScreenOptimization
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

private class MarkdownBridge(
    private val context: Context,
    private val onHeadingChange: (String) -> Unit
) {
    @JavascriptInterface
    fun onHeadingChange(anchor: String?) {
        if (!anchor.isNullOrBlank()) {
            onHeadingChange(anchor)
        }
    }

    @JavascriptInterface
    fun copyCode(code: String?) {
        if (code == null) return
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("markdown-code", code))
        Toast.makeText(context, "代码已复制", Toast.LENGTH_SHORT).show()
    }
}

@Composable
fun MarkdownRenderer(
    markdownContent: String,
    fontSize: Int = 16,
    scrollToAnchor: String? = null,
    onCurrentAnchorChange: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val isDarkTheme = colors.background.luminance() < 0.5f
    val backgroundColor = if (isDarkTheme) "#0C1624" else "#FFFFFF"
    val htmlContent = remember(markdownContent, fontSize, isDarkTheme) {
        renderMarkdownPreviewHtml(markdownContent, isDarkTheme, fontSize)
    }
    val latestAnchor = rememberUpdatedState(scrollToAnchor)
    val latestHeadingCallback = rememberUpdatedState(onCurrentAnchorChange)
    val webViewState = remember { mutableStateOf<WebView?>(null) }

    AndroidView(
        factory = {
            WebView(context).apply {
                webViewClient = WebViewClient()
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.setSupportZoom(true)
                settings.allowFileAccess = true
                settings.allowContentAccess = true
                @Suppress("DEPRECATION")
                runCatching { settings.forceDark = WebSettings.FORCE_DARK_OFF }
                setBackgroundColor(AndroidColor.parseColor(backgroundColor))
                addJavascriptInterface(
                    MarkdownBridge(context) { anchor ->
                        latestHeadingCallback.value(anchor)
                    },
                    "AndroidBridge"
                )
                ScreenOptimization.optimizeWebViewForHighRefreshRate(this)
                webViewState.value = this
            }
        },
        update = { webView ->
            webView.setBackgroundColor(AndroidColor.parseColor(backgroundColor))
            webView.loadDataWithBaseURL("file:///", htmlContent, "text/html", "UTF-8", null)
            webViewState.value = webView
        },
        modifier = modifier
    )

    LaunchedEffect(scrollToAnchor, htmlContent) {
        val anchor = latestAnchor.value ?: return@LaunchedEffect
        webViewState.value?.postDelayed(
            { webViewState.value?.loadUrl("javascript:scrollToHeading('$anchor')") },
            140
        )
    }
}

internal fun renderMarkdownPreviewHtml(
    markdown: String,
    isDarkTheme: Boolean,
    fontSize: Int
): String {
    val parser = Parser.builder().build()
    val document = parser.parse(preserveEditorBlankLines(markdown))
    val renderer = HtmlRenderer.builder()
        .softbreak("<br>\n")
        .build()
    val rawHtmlBody = renderer.render(document)
    val htmlBody = enhanceRenderedHtml(rawHtmlBody)

    val backgroundColor = if (isDarkTheme) "#0C1624" else "#FFFFFF"
    val textColor = if (isDarkTheme) "#E8F1FF" else "#12233B"
    val codeBackground = if (isDarkTheme) "#101D30" else "#F4F8FD"
    val codeInlineBackground = if (isDarkTheme) "#162741" else "#E6F0FF"
    val codeBorder = if (isDarkTheme) "#26415F" else "#D3E0F0"
    val borderColor = if (isDarkTheme) "#304A6A" else "#D7E0E7"
    val blockquoteColor = if (isDarkTheme) "#C7DCFF" else "#5379A7"
    val accent = if (isDarkTheme) "#8AB8FF" else "#1859B8"
    val accentSoft = if (isDarkTheme) "#112C4C" else "#D9E8FF"
    val tableAlt = if (isDarkTheme) "rgba(138,184,255,0.06)" else "rgba(76,151,255,0.04)"

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="utf-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                * { box-sizing: border-box; }
                html, body {
                    margin: 0;
                    padding: 0;
                    background: $backgroundColor;
                }
                body {
                    font-family: "Noto Serif", "Source Han Serif SC", "Georgia", serif;
                    font-size: ${fontSize}px;
                    line-height: 1.9;
                    color: $textColor;
                    padding: 28px 22px 120px;
                    max-width: 900px;
                    margin: 0 auto;
                    overflow-wrap: break-word;
                    -webkit-text-size-adjust: 100%;
                }
                h1, h2, h3, h4, h5, h6 {
                    margin-top: 34px;
                    margin-bottom: 14px;
                    font-weight: 700;
                    line-height: 1.3;
                    letter-spacing: -0.02em;
                    color: $textColor;
                    scroll-margin-top: 76px;
                }
                h1 {
                    font-size: 2.05em;
                    padding-bottom: 0.35em;
                    border-bottom: 1px solid $borderColor;
                }
                h2 {
                    font-size: 1.6em;
                    padding-bottom: 0.28em;
                    border-bottom: 1px solid $borderColor;
                }
                h3 { font-size: 1.28em; }
                p, ul, ol, table, blockquote {
                    margin: 0 0 18px 0;
                    color: $textColor;
                }
                a {
                    color: $accent;
                    text-decoration: none;
                    border-bottom: 1px solid rgba(24,89,184,0.24);
                }
                blockquote {
                    padding: 10px 18px;
                    border-left: 4px solid $accent;
                    background: $accentSoft;
                    color: $blockquoteColor;
                    border-radius: 0 14px 14px 0;
                }
                code, pre {
                    font-family: "JetBrains Mono", Consolas, monospace;
                }
                code {
                    background-color: $codeInlineBackground;
                    color: $accent;
                    border-radius: 6px;
                    padding: 0.18em 0.42em;
                    font-size: 0.92em;
                }
                pre {
                    background: linear-gradient(180deg, $codeBackground, ${if (isDarkTheme) "#0B1522" else "#ECF4FF"});
                    border: 1px solid $codeBorder;
                    border-radius: 18px;
                    overflow-x: auto;
                    box-shadow: 0 14px 32px rgba(10, 30, 60, 0.12);
                    padding: 16px;
                    line-height: 1.75;
                    white-space: pre;
                }
                pre code {
                    display: block;
                    background: transparent;
                    color: $textColor;
                    border-radius: 0;
                    padding: 0;
                    font-size: 0.92em;
                    white-space: pre;
                }
                ul, ol {
                    padding-left: 1.45em;
                }
                li { margin: 0.3em 0; }
                table {
                    width: 100%;
                    border-collapse: collapse;
                    border-radius: 14px;
                    overflow: hidden;
                    box-shadow: 0 8px 20px rgba(18, 35, 59, 0.06);
                }
                th, td {
                    border: 1px solid $borderColor;
                    padding: 10px 12px;
                    text-align: left;
                }
                th {
                    background-color: $accentSoft;
                    color: $accent;
                    font-weight: 700;
                }
                tr:nth-child(even) {
                    background-color: $tableAlt;
                }
                img {
                    display: block;
                    max-width: 100%;
                    height: auto;
                    border-radius: 16px;
                    margin: 12px 0 20px;
                    box-shadow: 0 12px 28px rgba(18, 35, 59, 0.12);
                }
                hr {
                    border: none;
                    border-top: 1px solid $borderColor;
                    margin: 34px 0;
                }
                .heading-anchor-active {
                    animation: highlightHeading 1.1s ease;
                }
                @keyframes highlightHeading {
                    0% { background: rgba(138,184,255,0.22); }
                    100% { background: transparent; }
                }
            </style>
            <script>
                let currentHeadingId = '';
                function scrollToHeading(id) {
                    var target = document.getElementById(id);
                    if (!target) return;
                    target.scrollIntoView({ behavior: 'smooth', block: 'start' });
                    target.classList.remove('heading-anchor-active');
                    void target.offsetWidth;
                    target.classList.add('heading-anchor-active');
                }
                function notifyCurrentHeading() {
                    const headings = Array.from(document.querySelectorAll('h1[id], h2[id], h3[id], h4[id], h5[id], h6[id]'));
                    if (!headings.length) return;
                    const threshold = 120;
                    let active = headings[0];
                    headings.forEach((heading) => {
                        const rect = heading.getBoundingClientRect();
                        if (rect.top <= threshold) active = heading;
                    });
                    if (active && active.id && active.id !== currentHeadingId) {
                        currentHeadingId = active.id;
                        if (window.AndroidBridge && window.AndroidBridge.onHeadingChange) {
                            window.AndroidBridge.onHeadingChange(active.id);
                        }
                    }
                }
                function copyCode(button) {
                    const code = button.getAttribute('data-raw-code') || '';
                    if (window.AndroidBridge && window.AndroidBridge.copyCode) {
                        window.AndroidBridge.copyCode(code);
                    }
                }
                window.addEventListener('scroll', notifyCurrentHeading, { passive: true });
                window.addEventListener('load', function() {
                    setTimeout(notifyCurrentHeading, 60);
                });
            </script>
        </head>
        <body>$htmlBody</body>
        </html>
    """.trimIndent()
}

private fun preserveEditorBlankLines(markdown: String): String {
    val normalized = markdown.replace("\r\n", "\n").replace('\r', '\n')
    val lines = normalized.split("\n")
    var inFence = false
    var fenceMarker = ""

    return buildString {
        lines.forEachIndexed { index, line ->
            val trimmed = line.trimStart()
            val fenceMatch = Regex("^(`{3,}|~{3,})").find(trimmed)
            if (fenceMatch != null) {
                val marker = fenceMatch.value
                if (!inFence) {
                    inFence = true
                    fenceMarker = marker.take(3)
                } else if (marker.startsWith(fenceMarker)) {
                    inFence = false
                    fenceMarker = ""
                }
            }

            if (!inFence && line.isBlank()) {
                append("<br>")
            } else {
                append(line)
            }

            if (index < lines.lastIndex) {
                append('\n')
            }
        }
    }
}

private fun enhanceRenderedHtml(html: String): String {
    val headingRegex = Regex("<h([1-6])>(.*?)</h\\1>", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    var headingIndex = 0
    val withAnchors = headingRegex.replace(html) { match ->
        val level = match.groupValues[1]
        val inner = match.groupValues[2]
        val plain = inner.replace(Regex("<.*?>"), "").trim()
        val id = slugifyHeading(plain, headingIndex++)
        "<h$level id=\"$id\">$inner</h$level>"
    }

    return withAnchors
}

private fun slugifyHeading(text: String, index: Int): String {
    val base = text
        .lowercase()
        .replace(Regex("[^\\p{L}\\p{N}\\s-]"), "")
        .trim()
        .replace(Regex("\\s+"), "-")
    return if (base.isBlank()) "section-$index" else "$base-$index"
}
