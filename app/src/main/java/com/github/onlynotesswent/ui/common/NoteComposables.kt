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
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draganddrop.DragAndDropTransferData
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route.NOTE_OVERVIEW
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.NotesToFlashcard
import com.google.firebase.Timestamp
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
 * @param notesToFlashcard The notes to flashcard object to be passed to the note item.
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
    notesToFlashcard: NotesToFlashcard? = null,
    navigationActions: NavigationActions,
    onClick: () -> Unit
) {
  var showBottomSheet by remember { mutableStateOf(false) }

  if (showBottomSheet) {
    NoteOptionsBottomSheet(
        note = note,
        noteViewModel = noteViewModel,
        folderViewModel = folderViewModel,
        onDismiss = { showBottomSheet = false },
        navigationActions = navigationActions,
        notesToFlashcard = notesToFlashcard)
  }

  Card(
      modifier =
          Modifier.testTag("noteCard")
              .height(140.dp)
              .padding(4.dp)
              .semantics(mergeDescendants = true, properties = {})
              .fillMaxWidth()
              // Enable drag and drop for the note card as a source if the current user is the owner
              .dragAndDropSource {
                detectTapGestures(
                    onTap = { onClick() },
                    onLongPress = {
                      if (note.isOwner(currentUser.value!!.uid)) {
                        noteViewModel.draggedNote(note)
                        // Start a drag-and-drop operation to transfer the data which is being
                        // dragged
                        startTransfer(
                            // Transfer the note Id as a ClipData object
                            DragAndDropTransferData(ClipData.newPlainText("Note", note.id)))
                      }
                    },
                )
              },
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
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
                    navigationActions.currentRoute() != Screen.SEARCH) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        modifier =
                            Modifier.testTag("showBottomSheetButton").size(24.dp).clickable {
                              showBottomSheet = true
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
 * Displays a bottom sheet with options to move or delete a note. The bottom sheet is displayed when
 * the user clicks on the more options icon in the note card.
 *
 * @param note The note data that will be displayed in this card.
 * @param noteViewModel The ViewModel that provides the list of notes to display.
 * @param folderViewModel the folderViewModel used here to move the note.
 * @param onDismiss The callback to be invoked when the bottom sheet is dismissed.
 * @param navigationActions The navigation instance used to transition between different screens.
 * @param notesToFlashcard The notes to flashcard object to be passed to the note item.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteOptionsBottomSheet(
    note: Note,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel,
    onDismiss: () -> Unit,
    navigationActions: NavigationActions,
    notesToFlashcard: NotesToFlashcard?,
) {
  var showFileSystemPopup by remember { mutableStateOf(false) }
  var showDeletePopup by remember { mutableStateOf(false) }
  var showFlashcardDialog by remember { mutableStateOf(false) }

  if (showFileSystemPopup) {
    FileSystemPopup(
        onDismiss = {
          showFileSystemPopup = false
          onDismiss()
        },
        folderViewModel = folderViewModel,
        onMoveHere = { selectedFolder ->
          noteViewModel.updateNote(
              note.copy(folderId = selectedFolder?.id, lastModified = Timestamp.now()))
          showFileSystemPopup = false
          folderViewModel.clearSelectedFolder()
          if (selectedFolder != null) {
            navigationActions.navigateTo(
                Screen.FOLDER_CONTENTS.replace(
                    oldValue = "{folderId}", newValue = selectedFolder.id))
          } else {
            navigationActions.navigateTo(NOTE_OVERVIEW)
          }

          onDismiss() // Dismiss the bottom sheet after moving the note
        })
  }

  if (showDeletePopup) {
    ConfirmationPopup(
        title = stringResource(R.string.delete_note),
        text = stringResource(R.string.delete_note_text),
        onConfirm = {
          noteViewModel.deleteNoteById(note.id, note.userId)
          if (folderViewModel.selectedFolder.value != null) {
            noteViewModel.getNotesFromFolder(folderViewModel.selectedFolder.value!!.id, null)
          } else {
            noteViewModel.getRootNotesFromUid(note.userId)
          }
          showDeletePopup = false // Close the dialog after deleting
          onDismiss() // Dismiss the bottom sheet after deleting the note
        },
        onDismiss = {
          showDeletePopup = false // Close the dialog without deleting
          onDismiss() // Dismiss the bottom sheet after deleting the note
        })
  }

  if (showFlashcardDialog && notesToFlashcard != null) {
    NoteToFlashcardDialog(
        note = note,
        notesToFlashcard = notesToFlashcard,
        navigationActions = navigationActions,
        onDismiss = {
          showFlashcardDialog = false
          onDismiss()
        })
  }

  ModalBottomSheet(
      modifier = Modifier.testTag("noteModalBottomSheet"),
      containerColor = MaterialTheme.colorScheme.onPrimary,
      onDismissRequest = onDismiss,
      content = {
        Column(modifier = Modifier.padding(16.dp)) {
          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .clickable { showFileSystemPopup = true }
                      .padding(vertical = 8.dp)
                      .testTag("moveNoteBottomSheet"),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.FolderOpen,
                    contentDescription = stringResource(R.string.move_note))
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.move_note),
                    style = MaterialTheme.typography.titleMedium)
              }

          // Convert Note to Flashcards
          if (notesToFlashcard != null) {
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .clickable { showFlashcardDialog = true }
                        .padding(vertical = 8.dp)
                        .testTag("convertNoteBottomSheet"),
                verticalAlignment = Alignment.CenterVertically) {
                  Icon(
                      imageVector = Icons.AutoMirrored.Outlined.LibraryBooks,
                      contentDescription = stringResource(R.string.convert_note_to_flashcards))
                  Spacer(modifier = Modifier.width(16.dp))
                  Text(
                      text = stringResource(R.string.convert_note_to_flashcards),
                      style = MaterialTheme.typography.titleMedium)
                }
          }

          HorizontalDivider(Modifier.padding(vertical = 10.dp), 1.dp)

          Row(
              modifier =
                  Modifier.fillMaxWidth()
                      .clickable { showDeletePopup = true }
                      .padding(vertical = 8.dp)
                      .testTag("deleteNoteBottomSheet"),
              verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.Delete,
                    contentDescription = stringResource(R.string.delete_note),
                    tint = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = stringResource(R.string.delete_note),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.error)
              }
        }
      })
}

/**
 * Dialog that displays a loading indicator while converting a note to flashcards.
 *
 * @param note The note to be converted to flashcards.
 * @param notesToFlashcard The notes to flashcard object to be passed to the note item.
 * @param navigationActions The navigation instance used to transition between different screens.
 * @param onDismiss The callback to be invoked when the dialog is dismissed.
 */
@Composable
fun NoteToFlashcardDialog(
    note: Note,
    notesToFlashcard: NotesToFlashcard,
    navigationActions: NavigationActions,
    onDismiss: () -> Unit
) {
  val context = LocalContext.current
  var isLoading by remember { mutableStateOf(true) }
  var flashcardErrorMessage by remember { mutableStateOf<String?>(null) }
  var noFlashcardsCreated by remember { mutableStateOf(false) }

  AlertDialog(
      onDismissRequest = {
        if (!isLoading) {
          onDismiss()
        }
      },
      title = { Text(stringResource(R.string.convert_folder_to_decks)) },
      text = {
        Column {
          if (isLoading) {
            LoadingIndicator(
                text = stringResource(R.string.converting_note_to_flashcards),
                modifier = Modifier.fillMaxWidth(),
                spacerHeight = 8.dp)
          }

          if (flashcardErrorMessage != null) {
            Spacer(modifier = Modifier.height(16.dp))
            Column {
              Text(
                  text =
                      buildAnnotatedString {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.error)) {
                          append(stringResource(R.string.error_while_converting))
                        }
                        append(": $flashcardErrorMessage")
                      })
            }
          }
          if (noFlashcardsCreated) {
            Spacer(modifier = Modifier.height(16.dp))
            Column { Text(text = stringResource(R.string.no_flashcards_created)) }
          }
        }
      },
      confirmButton = {},
      dismissButton = {
        if (!isLoading) {
          TextButton(onClick = { onDismiss() }, modifier = Modifier.testTag("closeAction")) {
            Text(stringResource(R.string.close), color = MaterialTheme.colorScheme.error)
          }
        }
      })

  if (isLoading) {
    LaunchedEffect(Unit) {
      notesToFlashcard.convertNoteToDeck(
          note,
          onSuccess = {
            isLoading = false
            if (it != null) {
              navigationActions.navigateTo(
                  Screen.DECK_MENU.replace(oldValue = "{deckId}", newValue = it.id))
            } else {
              noFlashcardsCreated = true
            }
          },
          onFileNotFoundException = {
            isLoading = false
            flashcardErrorMessage = context.getString(R.string.no_note_text_found)
          },
          onFailure = {
            isLoading = false
            flashcardErrorMessage = context.getString(R.string.error_creating_flashcards)
          })
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
    oldVisibility: Visibility = Visibility.DEFAULT,
    oldName: String = ""
) {
  CreationDialog(
      onDismiss, onConfirm, action, oldVisibility, oldName, stringResource(R.string.note))
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
              // Set disabled text field colors to be less prominent
              disabledTextColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              disabledLabelColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              disabledPlaceholderColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
              disabledIndicatorColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)))
}
