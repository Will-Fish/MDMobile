package com.example.mdmobile

import com.example.mdmobile.ui.screens.readerRoute
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class MainScreenNavigationTest {
    @Test
    fun readerRouteDoesNotCarrySeparateEditMode() {
        val route = readerRoute("/storage/emulated/0/Documents/note.md")

        assertEquals("reader/%2Fstorage%2Femulated%2F0%2FDocuments%2Fnote.md?new=false", route)
        assertFalse(route.contains("edit="))
    }

    @Test
    fun newFileRouteOnlyCarriesNewFileState() {
        val route = readerRoute("/storage/emulated/0/Documents/draft.md", isNewFile = true)

        assertEquals("reader/%2Fstorage%2Femulated%2F0%2FDocuments%2Fdraft.md?new=true", route)
        assertFalse(route.contains("edit="))
    }
}
