package com.github.onlynotesswent.ui.common

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.draganddrop.dragAndDropTarget
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropEvent
import androidx.compose.ui.draganddrop.DragAndDropTarget
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.draganddrop.toAndroidDragEvent
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions

/**
 * Displays a single folder item in a card format. The card contains the folder's name. When
 * clicked, it triggers the provided onClick action, which can be used for navigation or other
 * interactions.
 *
 * @param folder The folder data that will be displayed in this card.
 * @param navigationActions The navigationActions instance used to transition between different
 *   screens.
 * @param noteViewModel The Note view model.
 * @param folderViewModel The Folder view model.
 * @param onClick The lambda function to be invoked when the folder card is clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun FolderItem(
    folder: Folder,
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    onClick: () -> Unit
) {
  val dropSuccess = remember { mutableStateOf(false) }

  Card(
      modifier =
          Modifier.testTag("folderCard")
              .semantics(mergeDescendants = true, properties = {})
              .padding(vertical = 4.dp)
              .dragAndDropSource {
                detectTapGestures(
                    // When tapping on a folder, perform onCLick
                    onTap = { onClick() },
                    onLongPress = {
                      // Only allow drag-and-drop for root folders
                      if (folder.parentFolderId == null) {
                        folderViewModel.draggedFolder(folder)
                        // Start a drag-and-drop operation to transfer the data being dragged
                        startTransfer(
                            DragAndDropTransferData(
                                // Transfer the folder Id as a ClipData object
                                ClipData.newPlainText("Folder", folder.id)))
                      }
                    })
              } // Enable drag-and-drop for the folder (as a target)
              .dragAndDropTarget(
                  // Accept any drag-and-drop event (either folder or note in this case)
                  shouldStartDragAndDrop = { true },
                  // Handle the drop event
                  target =
                      remember {
                        object : DragAndDropTarget {
                          override fun onDrop(event: DragAndDropEvent): Boolean {
                            // Get the dragged object Id
                            val draggedObjectId =
                                event.toAndroidDragEvent().clipData.getItemAt(0).text.toString()
                            val draggedNote = noteViewModel.draggedNote.value
                            if (draggedNote != null && draggedNote.id == draggedObjectId) {
                              // Update the dragged note with the new folder Id
                              noteViewModel.updateNote(draggedNote.copy(folderId = folder.id))
                              noteViewModel.draggedNote(null)
                              dropSuccess.value = true
                              return true
                            }
                            // Get the dragged folder in case a folder is being dragged
                            val draggedFolder = folderViewModel.draggedFolder.value
                            if (draggedFolder != null &&
                                draggedFolder.id == draggedObjectId &&
                                draggedFolder.id != folder.id) {
                              // Update the dragged folder with the new parent folder Id.
                              folderViewModel.updateFolder(
                                  draggedFolder.copy(parentFolderId = folder.id))
                              folderViewModel.draggedFolder(null)
                              // Set dropSuccess to true to indicate that the drop was successful
                              dropSuccess.value = true
                              return true
                            }
                            dropSuccess.value = false
                            return false
                          }

                          override fun onEnded(event: DragAndDropEvent) {
                            if (dropSuccess.value) {
                              folderViewModel.selectedFolder(folder)
                              navigationActions.navigateToFolderContents(folder)
                            }
                            // Reset dropSuccess value
                            dropSuccess.value = false
                          }
                        }
                      }),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.padding(8.dp).fillMaxWidth(),
            verticalArrangement = Arrangement.Center) {
              Image(
                  painter = painterResource(id = R.drawable.folder_icon),
                  contentDescription = "Folder Icon",
                  modifier = Modifier.size(80.dp).align(Alignment.CenterHorizontally))

              Text(
                  modifier = Modifier.align(Alignment.CenterHorizontally),
                  text = folder.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onBackground)
            }
      }
}

/**
 * Dialog that allows the user to create or rename a folder.
 *
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new name
 * @param action the action to be performed (create or rename)
 * @param oldVisibility the old visibility of the folder (if renaming)
 * @param oldName the old name of the folder (if renaming)
 */
@Composable
fun FolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Visibility) -> Unit,
    action: String,
    oldVisibility: Visibility? = Visibility.DEFAULT,
    oldName: String = ""
) {
  CreationDialog(onDismiss, onConfirm, action, oldVisibility, oldName, "Folder")
}
