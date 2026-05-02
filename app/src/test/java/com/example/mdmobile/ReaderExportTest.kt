package com.example.mdmobile

import com.example.mdmobile.ui.screens.exportDocumentName
import com.example.mdmobile.ui.screens.exportTargetFile
import com.example.mdmobile.ui.screens.markdownPdfBlocks
import com.example.mdmobile.ui.screens.pdfTypefaceFamily
import com.example.mdmobile.ui.screens.pdfPageCount
import com.example.mdmobile.ui.screens.PdfBlockKind
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

class ReaderExportTest {
    @Test
    fun exportTargetFileReplacesMarkdownExtensionWithPdf() {
        val source = File("notes/daily.note.md")

        val target = exportTargetFile(source, "pdf")

        assertEquals(File("notes/daily.note.pdf"), target)
    }

    @Test
    fun exportDocumentNameMatchesPdfTargetFileName() {
        val source = File("notes/daily.note.md")

        val documentName = exportDocumentName(source, "pdf")

        assertEquals("daily.note.pdf", documentName)
    }

    @Test
    fun pdfPageCountKeepsBlankDocumentsExportable() {
        assertEquals(1, pdfPageCount(contentHeight = 0, pageBodyHeight = 1200))
    }

    @Test
    fun pdfPageCountRoundsUpLongDocuments() {
        assertEquals(3, pdfPageCount(contentHeight = 2401, pageBodyHeight = 1200))
    }

    @Test
    fun markdownPdfBlocksRenderMarkdownStructureInsteadOfRawSyntax() {
        val blocks = markdownPdfBlocks(
            """
            # 中文标题

            正文 **加粗** 和 `代码`

            - 第一项
            - 第二项
            """.trimIndent()
        )

        assertEquals("中文标题", blocks[0].text)
        assertEquals("正文 加粗 和 代码", blocks[1].text)
        assertEquals("• 第一项", blocks[2].text)
        assertEquals("• 第二项", blocks[3].text)
    }

    @Test
    fun pdfTypefaceUsesAndroidCjkFriendlyFallback() {
        assertEquals("sans-serif", pdfTypefaceFamily(PdfBlockKind.PARAGRAPH))
        assertEquals("sans-serif-medium", pdfTypefaceFamily(PdfBlockKind.HEADING))
        assertEquals("monospace", pdfTypefaceFamily(PdfBlockKind.CODE))
    }
}
