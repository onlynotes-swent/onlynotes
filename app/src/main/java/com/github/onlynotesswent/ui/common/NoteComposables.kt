package com.github.onlynotesswent.ui.common

import android.content.ClipData
import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * Displays a single note item in a card format. The card contains the note's date, name, and user
 * ID. When clicked, it triggers the provided [onClick] action, which can be used for navigation or
 * other interactions.
 *
 * @param note The note data that will be displayed in this card.
 * @param author The author of the note.
 * @param currentUser The current user.
 * @param context The context used to display the dialog.
 * @param noteViewModel The ViewModel that provides the list of notes to display.
 * @param folderViewModel The ViewModel that provides the list of folders to display.
 * @param showDialog A boolean indicating whether the move out dialog should be displayed.
 * @param navigationActions The navigation instance used to transition between different screens.
 * @param onClick The lambda function to be invoked when the note card is clicked.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun NoteItem(
    note: Note,
    author: String? = null,
    currentUser: State<User?>,
    context: Context,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    showDialog: Boolean,
    navigationActions: NavigationActions,
    onClick: () -> Unit
) {
  // Mutable state to show the move out dialog
  var showMoveOutDialog by remember { mutableStateOf(showDialog) }

  if (showMoveOutDialog && note.folderId != null) {
    AlertDialog(
        modifier = Modifier.testTag("MoveOutDialog"),
        onDismissRequest = { showMoveOutDialog = false },
        title = {
          Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Text("Move note out of folder")
          }
        },
        confirmButton = {
          Button(
              modifier = Modifier.testTag("MoveOutConfirmButton"),
              onClick = {
                if (currentUser.value!!.uid == note.userId) {
                  // Move out will move the given note to the parent folder
                  val parentFolderId = folderViewModel.parentFolderId.value
                  if (parentFolderId != null) {
                    noteViewModel.updateNote(note.copy(folderId = parentFolderId))
                    val folderContentsScreen =
                        Screen.FOLDER_CONTENTS.replace(
                            oldValue = "{folderId}", newValue = parentFolderId)
                    navigationActions.navigateTo(folderContentsScreen)
                    // folderViewModel.getFolderById(parentFolderId)
                  } else {
                    noteViewModel.updateNote(note.copy(folderId = null))
                    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                  }
                } else {
                  Toast.makeText(
                          context,
                          "You can't move out a note that you didn't create",
                          Toast.LENGTH_SHORT)
                      .show()
                }
                showMoveOutDialog = false
              }) {
                Text("Move")
              }
        },
        dismissButton = { Button(onClick = { showMoveOutDialog = false }) { Text("Cancel") } })
  }
  Card(
      modifier =
          Modifier.testTag("noteCard")
              .semantics(mergeDescendants = true, properties = {})
              .fillMaxWidth()
              .padding(vertical = 4.dp)
              // Enable drag and drop for the note card (as a source)
              .dragAndDropSource {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                      noteViewModel.draggedNote(note)
                      // Start a drag-and-drop operation to transfer the data which is being dragged
                      startTransfer(
                          // Transfer the note Id as a ClipData object
                          DragAndDropTransferData(ClipData.newPlainText("Note", note.id)))
                    },
                )
              },
      colors =
          CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Column(modifier = Modifier.fillMaxWidth().padding(8.dp)) {
          Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween) {
                Text(
                    text =
                        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(note.date.toDate()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer)

                Row(verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      // Show move out menu when clicking on the Icon
                      modifier =
                          Modifier.testTag("MoveOutButton").clickable(
                              enabled =
                                  note.folderId != null &&
                                      navigationActions.currentRoute() == Screen.FOLDER_CONTENTS) {
                                showMoveOutDialog = true
                              },
                      imageVector = Icons.Filled.MoreVert,
                      contentDescription = null,
                      tint = MaterialTheme.colorScheme.onPrimaryContainer)
                }
              }

          Spacer(modifier = Modifier.height(4.dp))
          Text(
              text = note.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
          if (author != null) {
            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer)
          }
          Text(
              text = note.noteCourse.fullName(),
              style = MaterialTheme.typography.bodySmall,
              color = MaterialTheme.colorScheme.onPrimaryContainer)
        }
      }
}

/**
 * A composable function that displays an `OutlinedTextField` with an optional trailing icon.
 *
 * @param value The current value of the text field.
 * @param onValueChange The callback that is triggered when the value changes.
 * @param label The label to be displayed inside the text field container.
 * @param placeholder The placeholder to be displayed when the text field is in focus and the input
 *   text is empty.
 * @param modifier The modifier to be applied to the `OutlinedTextField`.
 * @param trailingIcon An optional trailing icon displayed at the end of the text field container.
 */
@Composable
fun NoteDataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      modifier = modifier,
      trailingIcon = trailingIcon,
      colors =
          TextFieldDefaults.colors(
              focusedIndicatorColor = MaterialTheme.colorScheme.primary,
              unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
              focusedContainerColor = MaterialTheme.colorScheme.background,
              unfocusedContainerColor = MaterialTheme.colorScheme.background))
}
