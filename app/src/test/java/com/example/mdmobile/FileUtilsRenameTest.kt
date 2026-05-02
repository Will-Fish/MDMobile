package com.example.mdmobile

import com.example.mdmobile.utils.FileRenameResult
import com.example.mdmobile.utils.FileUtils
import com.example.mdmobile.data.model.MarkdownFile
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import java.util.Date

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

    @Test
    fun filterDocumentFilesKeepsOnlySupportedFilesAndExcludesDirectories() {
        val files = listOf(
            MarkdownFile("notes.md", "/tmp/notes.md", 1, Date(), false),
            MarkdownFile("page.html", "/tmp/page.html", 1, Date(), false),
            MarkdownFile("paper.pdf", "/tmp/paper.pdf", 1, Date(), false),
            MarkdownFile("draft.markdown", "/tmp/draft.markdown", 1, Date(), false),
            MarkdownFile("folder", "/tmp/folder", 0, Date(), true),
            MarkdownFile("image.png", "/tmp/image.png", 1, Date(), false)
        )

        val filtered = FileUtils.filterDocumentFiles(files)

        assertEquals(listOf("notes.md", "page.html", "paper.pdf"), filtered.map { it.name })
    }

    @Test
    fun getAppDocumentDirectoryCreatesMdFilesFolderUnderInstallLocation() {
        val baseDir = tempFolder.newFolder("files")

        val directory = FileUtils.getAppDocumentDirectory(baseDir)

        assertEquals("md_files", directory.name)
        assertEquals(baseDir.absolutePath, directory.parentFile?.absolutePath)
        assertTrue(directory.isDirectory)
    }

    @Test
    fun deleteDocumentFileDeletesSupportedFilesOnly() {
        val mdFile = tempFolder.newFile("delete-me.md")
        val textFile = tempFolder.newFile("keep-me.txt")

        assertTrue(FileUtils.deleteDocumentFile(mdFile.absolutePath))
        assertFalse(mdFile.exists())

        assertFalse(FileUtils.deleteDocumentFile(textFile.absolutePath))
        assertTrue(textFile.exists())
    }
}
