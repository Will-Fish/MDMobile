package com.example.mdmobile.ui.components

import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import org.commonmark.parser.Parser
import org.commonmark.renderer.html.HtmlRenderer

@Composable
fun MarkdownRenderer(
    markdownContent: String,
    isDarkTheme: Boolean = false,
    fontSize: Int = 16,
    modifier: Modifier = Modifier
) {
    val htmlContent = remember(markdownContent) {
        convertMarkdownToHtml(markdownContent, isDarkTheme, fontSize)
    }

    AndroidView(
        factory = { context ->
            android.webkit.WebView(context).apply {
                webViewClient = android.webkit.WebViewClient()
                settings.javaScriptEnabled = false
                settings.domStorageEnabled = false
                settings.loadWithOverviewMode = true
                settings.useWideViewPort = true
                settings.builtInZoomControls = true
                settings.displayZoomControls = false
                settings.setSupportZoom(true)

                // 针对高刷新率优化
                com.example.mdmobile.utils.ScreenOptimization.optimizeWebViewForHighRefreshRate(this)
            }
        },
        update = { webView ->
            webView.loadDataWithBaseURL(
                null,
                htmlContent,
                "text/html",
                "UTF-8",
                null
            )
        },
        modifier = modifier
    )
}

private fun convertMarkdownToHtml(
    markdown: String,
    isDarkTheme: Boolean,
    fontSize: Int
): String {
    val parser = Parser.builder().build()
    val document = parser.parse(markdown)
    val renderer = HtmlRenderer.builder().build()
    val htmlBody = renderer.render(document)

    val backgroundColor = if (isDarkTheme) "#1E1E1E" else "#FFFFFF"
    val textColor = if (isDarkTheme) "#E0E0E0" else "#000000"
    val codeBackground = if (isDarkTheme) "#2D2D2D" else "#F5F5F5"
    val borderColor = if (isDarkTheme) "#404040" else "#E0E0E0"

    return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <style>
                body {
                    font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Oxygen, Ubuntu, sans-serif;
                    font-size: ${fontSize}px;
                    line-height: 1.6;
                    color: $textColor;
                    background-color: $backgroundColor;
                    padding: 16px;
                    margin: 0;
                    max-width: 100%;
                    overflow-wrap: break-word;
                }
                h1, h2, h3, h4, h5, h6 {
                    margin-top: 24px;
                    margin-bottom: 16px;
                    font-weight: 600;
                    line-height: 1.25;
                }
                h1 { font-size: 2em; border-bottom: 1px solid $borderColor; padding-bottom: 0.3em; }
                h2 { font-size: 1.5em; border-bottom: 1px solid $borderColor; padding-bottom: 0.3em; }
                h3 { font-size: 1.25em; }
                p { margin: 0 0 16px 0; }
                a { color: #0366d6; text-decoration: none; }
                a:hover { text-decoration: underline; }
                ul, ol { padding-left: 2em; margin: 0 0 16px 0; }
                li { margin: 0.25em 0; }
                blockquote {
                    padding: 0 1em;
                    color: #6a737d;
                    border-left: 0.25em solid $borderColor;
                    margin: 0 0 16px 0;
                }
                pre {
                    background-color: $codeBackground;
                    border-radius: 6px;
                    padding: 16px;
                    overflow: auto;
                    margin: 0 0 16px 0;
                }
                code {
                    font-family: 'SFMono-Regular', Consolas, 'Liberation Mono', Menlo, monospace;
                    background-color: $codeBackground;
                    padding: 0.2em 0.4em;
                    border-radius: 3px;
                    font-size: 85%;
                }
                pre code {
                    padding: 0;
                    background-color: transparent;
                    border-radius: 0;
                }
                table {
                    border-collapse: collapse;
                    margin: 0 0 16px 0;
                    width: 100%;
                }
                th, td {
                    padding: 6px 13px;
                    border: 1px solid $borderColor;
                }
                th {
                    font-weight: 600;
                    background-color: $codeBackground;
                }
                tr:nth-child(even) {
                    background-color: $codeBackground;
                }
                img {
                    max-width: 100%;
                    height: auto;
                }
                hr {
                    height: 1px;
                    background-color: $borderColor;
                    border: none;
                    margin: 24px 0;
                }
            </style>
        </head>
        <body>
            $htmlBody
        </body>
        </html>
    """.trimIndent()
}