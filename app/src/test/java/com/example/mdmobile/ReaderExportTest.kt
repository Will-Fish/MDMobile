package com.example.mdmobile

import com.example.mdmobile.ui.screens.exportTargetFile
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
}
