package com.example.mdmobile

import com.example.mdmobile.utils.FileRenameResult
import com.example.mdmobile.utils.FileUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class FileUtilsRenameTest {
    @get:Rule
    val tempFolder = TemporaryFolder()

    @Test
    fun renameMarkdownFileAddsMdExtensionAndKeepsContent() {
        val source = tempFolder.newFile("old.md")
        source.writeText("# Notes", Charsets.UTF_8)

        val result = FileUtils.renameMarkdownFile(source.absolutePath, "new-name")

        val success = result as FileRenameResult.Success
        assertEquals("new-name.md", success.file.name)
        assertEquals("# Notes", success.file.readText(Charsets.UTF_8))
        assertFalse(source.exists())
    }

    @Test
    fun renameMarkdownFileRejectsBlankNames() {
        val source = tempFolder.newFile("old.md")

        val result = FileUtils.renameMarkdownFile(source.absolutePath, "   ")

        assertEquals(FileRenameResult.BlankName, result)
        assertTrue(source.exists())
    }

    @Test
    fun renameMarkdownFileRejectsExistingTarget() {
        val source = tempFolder.newFile("old.md")
        tempFolder.newFile("target.md")

        val result = FileUtils.renameMarkdownFile(source.absolutePath, "target.md")

        assertEquals(FileRenameResult.TargetExists, result)
        assertTrue(source.exists())
    }

    @Test
    fun renameMarkdownFileRejectsNonMarkdownFiles() {
        val source = tempFolder.newFile("old.txt")

        val result = FileUtils.renameMarkdownFile(source.absolutePath, "new-name")

        assertEquals(FileRenameResult.NotMarkdownFile, result)
        assertTrue(source.exists())
    }
}
