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
        convertMarkdownToHtml(markdownContent, isDarkTheme, fontSize)
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

private fun convertMarkdownToHtml(
    markdown: String,
    isDarkTheme: Boolean,
    fontSize: Int
): String {
    val parser = Parser.builder().build()
    val document = parser.parse(markdown)
    val renderer = HtmlRenderer.builder().build()
    val rawHtmlBody = renderer.render(document)
    val htmlBody = enhanceRenderedHtml(rawHtmlBody)

    val backgroundColor = if (isDarkTheme) "#0C1624" else "#FFFFFF"
    val textColor = if (isDarkTheme) "#E8F1FF" else "#12233B"
    val mutedText = if (isDarkTheme) "#AFC3E2" else "#5A6F89"
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
                p, ul, ol, table, .code-block, blockquote {
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
                .code-block {
                    background: linear-gradient(180deg, $codeBackground, ${if (isDarkTheme) "#0B1522" else "#ECF4FF"});
                    border: 1px solid $codeBorder;
                    border-radius: 18px;
                    overflow: hidden;
                    box-shadow: 0 14px 32px rgba(10, 30, 60, 0.12);
                    color: $textColor;
                }
                .code-toolbar {
                    display: flex;
                    align-items: center;
                    justify-content: space-between;
                    padding: 10px 14px;
                    border-bottom: 1px solid $codeBorder;
                    background: rgba(255,255,255,0.03);
                }
                .code-language {
                    font-family: "JetBrains Mono", Consolas, monospace;
                    font-size: 0.82em;
                    letter-spacing: 0.05em;
                    text-transform: uppercase;
                    color: $mutedText;
                }
                .copy-button {
                    border: 0;
                    border-radius: 10px;
                    background: $accentSoft;
                    color: $accent;
                    font-size: 0.82em;
                    padding: 6px 10px;
                    cursor: pointer;
                }
                .code-scroll {
                    overflow-x: auto;
                }
                table.code-table {
                    width: 100%;
                    border-collapse: collapse;
                    table-layout: fixed;
                }
                .code-table tr:nth-child(even) {
                    background-color: transparent;
                }
                .code-table td {
                    border: none;
                    padding: 0;
                    vertical-align: top;
                }
                .code-line-number {
                    width: 48px;
                    min-width: 48px;
                    padding: 0 10px 0 14px;
                    text-align: right;
                    color: $mutedText;
                    user-select: none;
                    border-right: 1px solid $codeBorder;
                }
                .code-line-content {
                    padding: 0 16px;
                    white-space: pre;
                    color: $textColor;
                }
                .code-line {
                    height: 1.75em;
                    line-height: 1.75;
                }
                .code-shell { color: #7CC8FF; }
                .code-keyword { color: #C678DD; font-weight: 600; }
                .code-string { color: #98C379; }
                .code-number { color: #E5C07B; }
                .code-comment { color: $mutedText; font-style: italic; }
                .code-type { color: #61AFEF; }
                .code-func { color: #56B6C2; }
                .code-operator { color: #D19A66; }
                .code-annotation { color: #E06C75; }
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

    val codeRegex = Regex(
        "<pre><code(?: class=\"language-([^\"]+)\")?>(.*?)</code></pre>",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
    )
    return codeRegex.replace(withAnchors) { match ->
        val language = match.groupValues[1].ifBlank { "plain" }
        val rawCode = match.groupValues[2]
        val highlighted = highlightCode(rawCode, language)
        val lined = addLineNumbers(highlighted)
        val escapedRaw = rawCode
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .replace("\n", "&#10;")
            .replace("\"", "&quot;")
        """
            <div class="code-block">
                <div class="code-toolbar">
                    <span class="code-language">${displayLanguage(language)}</span>
                    <button class="copy-button" data-raw-code="$escapedRaw" onclick="copyCode(this)">复制代码</button>
                </div>
                <div class="code-scroll">
                    <table class="code-table"><tbody>$lined</tbody></table>
                </div>
            </div>
        """.trimIndent()
    }
}

private fun addLineNumbers(codeHtml: String): String {
    val lines = codeHtml.split("\n")
    return lines.mapIndexed { index, line ->
        """
            <tr class="code-line">
                <td class="code-line-number">${index + 1}</td>
                <td class="code-line-content">${if (line.isEmpty()) "&nbsp;" else line}</td>
            </tr>
        """.trimIndent()
    }.joinToString("")
}

private fun highlightCode(encodedCode: String, language: String): String {
    var code = encodedCode
    val normalized = language.lowercase()
    val commentPatterns = when (normalized) {
        "kotlin", "java", "javascript", "typescript", "js", "ts", "c", "cpp", "csharp", "swift", "go", "rust", "php" ->
            listOf("//.*?(?=<br>|\\n|$)")
        "python", "bash", "shell", "sh", "yaml", "toml", "ruby", "dockerfile" ->
            listOf("#.*?(?=<br>|\\n|$)")
        "sql", "lua", "haskell" ->
            listOf("--.*?(?=<br>|\\n|$)")
        else -> emptyList()
    }

    commentPatterns.forEach { pattern ->
        code = code.replace(Regex(pattern)) { "<span class=\"code-comment\">${it.value}</span>" }
    }

    code = code.replace(
        Regex("(&quot;.*?&quot;|'.*?'|`.*?`)", RegexOption.DOT_MATCHES_ALL)
    ) { "<span class=\"code-string\">${it.value}</span>" }

    code = code.replace(Regex("\\b(0x[0-9A-Fa-f]+|\\d+(?:\\.\\d+)?)\\b")) {
        "<span class=\"code-number\">${it.value}</span>"
    }

    val keywordSet = when (normalized) {
        "kotlin" -> listOf("fun", "val", "var", "class", "object", "when", "if", "else", "return", "suspend", "private", "public", "data", "interface", "sealed", "inline", "package", "import")
        "java" -> listOf("class", "public", "private", "void", "static", "new", "return", "if", "else", "extends", "implements", "package", "import", "final", "try", "catch")
        "javascript", "typescript", "js", "ts" -> listOf("const", "let", "var", "function", "return", "if", "else", "class", "await", "async", "import", "export", "from", "new", "switch", "case")
        "python" -> listOf("def", "class", "return", "if", "elif", "else", "import", "from", "for", "while", "async", "await", "with", "as", "pass", "lambda")
        "bash", "shell", "sh" -> listOf("if", "then", "else", "fi", "for", "do", "done", "echo", "cat", "grep", "export", "function")
        "go" -> listOf("func", "package", "import", "return", "if", "else", "for", "range", "type", "struct", "interface", "go", "defer")
        "rust" -> listOf("fn", "let", "mut", "pub", "impl", "trait", "struct", "enum", "match", "if", "else", "return", "use")
        "swift" -> listOf("func", "let", "var", "class", "struct", "enum", "protocol", "if", "else", "return", "import", "guard")
        "sql" -> listOf("select", "from", "where", "join", "left", "right", "inner", "outer", "group", "order", "by", "insert", "update", "delete", "into", "values")
        "yaml", "toml", "json" -> listOf("true", "false", "null")
        "html", "xml" -> listOf("div", "span", "body", "head", "script", "style", "meta", "link")
        "css", "scss" -> listOf("display", "position", "color", "background", "border", "padding", "margin", "font-size", "flex", "grid")
        else -> emptyList()
    }
    if (keywordSet.isNotEmpty()) {
        code = code.replace(Regex("\\b(${keywordSet.joinToString("|")})\\b", RegexOption.IGNORE_CASE)) {
            "<span class=\"code-keyword\">${it.value}</span>"
        }
    }

    val typeSet = when (normalized) {
        "kotlin", "java" -> listOf("String", "Int", "Long", "Float", "Double", "Boolean", "Unit", "List", "Map", "MutableList", "MutableMap")
        "typescript", "ts" -> listOf("string", "number", "boolean", "Promise", "Record", "unknown", "never")
        "go" -> listOf("string", "int", "bool", "error", "byte", "rune")
        "rust" -> listOf("String", "str", "usize", "Result", "Option", "Vec")
        else -> emptyList()
    }
    if (typeSet.isNotEmpty()) {
        code = code.replace(Regex("\\b(${typeSet.joinToString("|")})\\b")) {
            "<span class=\"code-type\">${it.value}</span>"
        }
    }

    code = code.replace(Regex("@[A-Za-z_][A-Za-z0-9_]*")) {
        "<span class=\"code-annotation\">${it.value}</span>"
    }

    code = code.replace(Regex("\\b([A-Za-z_][A-Za-z0-9_]*)\\s*(?=\\()")) {
        "<span class=\"code-func\">${it.value}</span>"
    }

    code = code.replace(Regex("([=+\\-*/%<>!&|]{1,3})")) {
        "<span class=\"code-operator\">${it.value}</span>"
    }

    if (normalized in listOf("bash", "shell", "sh")) {
        code = code.replace(Regex("(^|<br>)(\\$\\s?.*?)(?=<br>|$)")) {
            "${it.groupValues[1]}<span class=\"code-shell\">${it.groupValues[2]}</span>"
        }
    }

    return code
}

private fun displayLanguage(language: String): String {
    return when (language.lowercase()) {
        "kt" -> "kotlin"
        "js" -> "javascript"
        "ts" -> "typescript"
        "sh" -> "shell"
        "py" -> "python"
        "yml" -> "yaml"
        else -> language
    }.uppercase()
}

private fun slugifyHeading(text: String, index: Int): String {
    val base = text
        .lowercase()
        .replace(Regex("[^\\p{L}\\p{N}\\s-]"), "")
        .trim()
        .replace(Regex("\\s+"), "-")
    return if (base.isBlank()) "section-$index" else "$base-$index"
}
