package com.github.onlynotesswent.ui.common

import android.content.ClipData
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.draganddrop.dragAndDropSource
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
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
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    showDialog: Boolean,
    navigationActions: NavigationActions,
    onClick: () -> Unit
) {
  var showMoveOutDialog by remember { mutableStateOf(showDialog) }

  if (showMoveOutDialog && note.folderId != null) {
    ConfirmationPopup(
        title = stringResource(R.string.move_note_out_of_folder),
        text = stringResource(R.string.move_note_out_of_folder_confirmation),
        onConfirm = {
          val parentFolderId = folderViewModel.parentFolderId.value
          if (parentFolderId != null) {
            noteViewModel.updateNote(note.copy(folderId = parentFolderId))
            navigationActions.navigateTo(
                Screen.FOLDER_CONTENTS.replace(oldValue = "{folderId}", newValue = parentFolderId))
          } else {
            noteViewModel.updateNote(note.copy(folderId = null))
            navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
          }
          showMoveOutDialog = false
        },
        onDismiss = { showMoveOutDialog = false })
  }

  Card(
      modifier =
          Modifier.testTag("noteCard")
              .height(140.dp)
              .padding(4.dp)
              .semantics(mergeDescendants = true, properties = {})
              .fillMaxWidth()
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
              }) {
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

                if (note.isOwner(currentUser.value!!.uid) &&
                    note.folderId != null &&
                    navigationActions.currentRoute() == Screen.FOLDER_CONTENTS) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        // Show move out menu when clicking on the Icon
                        modifier =
                            Modifier.testTag("MoveOutButton").size(24.dp).clickable {
                              showMoveOutDialog = true
                            },
                        imageVector = Icons.Filled.MoreVert,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimaryContainer)
                  }
                }
              }

          Spacer(modifier = Modifier.height(8.dp))

          Text(
              text = note.title,
              style = MaterialTheme.typography.bodyMedium,
              fontWeight = FontWeight.Bold,
              color = MaterialTheme.colorScheme.onSurface)

          if (author != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = author,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
          if (note.noteCourse != null && note.noteCourse != Course.EMPTY) {

            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = note.noteCourse.fullName(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      }
}
/**
 * Dialog that allows the user to create a note.
 *
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new name
 * @param action the action to be performed (create)
 * @param oldVisibility the old visibility of the note (if renaming), defaults to
 *   [Visibility.PRIVATE]
 * @param oldName the old name of the note (if renaming), defaults to an empty string
 */
@Composable
fun NoteDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Visibility) -> Unit,
    action: String,
    oldVisibility: Visibility = Visibility.PRIVATE,
    oldName: String = ""
) {
  CreationDialog(onDismiss, onConfirm, action, oldVisibility, oldName, "Note")
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
 * @param enabled A boolean indicating whether the text field is enabled.
 * @param trailingIcon An optional trailing icon displayed at the end of the text field container.
 */
@Composable
fun NoteDataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean,
    trailingIcon: @Composable (() -> Unit)? = null
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      modifier = modifier,
      enabled = enabled,
      trailingIcon = trailingIcon,
      colors =
          TextFieldDefaults.colors(
              focusedIndicatorColor = MaterialTheme.colorScheme.primary,
              unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
              focusedContainerColor = MaterialTheme.colorScheme.background,
              unfocusedContainerColor = MaterialTheme.colorScheme.background,
              disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
}
