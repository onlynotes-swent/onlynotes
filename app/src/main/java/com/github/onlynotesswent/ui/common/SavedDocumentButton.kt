package com.github.onlynotesswent.ui.common

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag

/**
 * A button that allows the user to save or remove a document from saved notes.
 *
 * @param isSaved Whether the document is already saved.
 * @param onSave Callback to be invoked when the user saves the document.
 * @param onDelete Callback to be invoked when the user removes the document.
 */
@Composable
fun SavedDocumentButton(isSaved: Boolean, onSave: () -> Unit, onDelete: () -> Unit) {
  // Todo: If an inheritance hierarchy established between different types of documents and their
  // MVVMs, could be simplified

  // If the document is already saved, display a button to remove it from saved notes, if not,
  // display a button to save it
  if (isSaved) {
    IconButton(onClick = { onDelete() }, modifier = Modifier.testTag("removeSavedDocumentButton")) {
      Icon(
          imageVector = Icons.Default.Bookmark,
          contentDescription = "Remove Document",
          tint = MaterialTheme.colorScheme.onSurface)
    }
  } else {
    IconButton(onClick = { onSave() }, modifier = Modifier.testTag("saveSavedDocumentButton")) {
      Icon(
          imageVector = Icons.Default.BookmarkBorder,
          contentDescription = "Save Note",
          tint = MaterialTheme.colorScheme.onSurface)
    }
  }
}
