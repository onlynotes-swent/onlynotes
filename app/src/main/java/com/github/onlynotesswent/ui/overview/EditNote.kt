package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.NoteDataTextField
import com.github.onlynotesswent.ui.OptionDropDownMenu
import com.github.onlynotesswent.ui.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Displays the edit note screen, where users can update the title and content of an existing note.
 * The screen includes two text fields for editing the note's title and content, and a button to
 * save the changes. The save button updates the note in the ViewModel and navigates back to the
 * overview screen.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@Composable
fun EditNoteScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {
  val note by noteViewModel.selectedNote.collectAsState()
  val currentUser by userViewModel.currentUser.collectAsState()
  val currentYear = Calendar.getInstance().get(Calendar.YEAR)
  var updatedNoteText by remember { mutableStateOf(note?.content ?: "") }
  var updatedNoteTitle by remember { mutableStateOf(note?.title ?: "") }
  var updatedClassName by remember { mutableStateOf(note?.noteClass?.className ?: "") }
  var updatedClassCode by remember { mutableStateOf(note?.noteClass?.classCode ?: "") }
  var updatedClassYear by remember { mutableIntStateOf(note?.noteClass?.classYear ?: currentYear) }
  var visibility by remember { mutableStateOf(note?.visibility) }
  var expandedVisibility by remember { mutableStateOf(false) }
  var updatedComments by remember { mutableStateOf(note?.comments ?: Note.CommentCollection()) }

  fun updateOnlyNoteCommentAndDate() {
    noteViewModel.updateNote(
        Note(
            id = note?.id ?: "1",
            title = note?.title ?: "",
            content = note?.content ?: "",
            date = Timestamp.now(), // Use current timestamp
            visibility = note?.visibility ?: Note.Visibility.DEFAULT,
            noteClass = note?.noteClass ?: Note.Class("", "", currentYear, ""),
            userId = note?.userId ?: "",
            folderId = note?.folderId,
            image =
                note?.image
                    ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), // Placeholder Bitmap
            comments = updatedComments),
        currentUser!!.uid)
  }

  if (currentUser == null) {
    UserNotFoundEditScreen()
  } else {
    Scaffold(
        modifier = Modifier.testTag("editNoteScreen"),
        topBar = {
          ScreenTopBar(
              title = "Edit note",
              titleTestTag = "editNoteTitle",
              onBackClick = { navigationActions.goBack() },
              icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = "Back",
                    tint = MaterialTheme.colorScheme.onSurface)
              },
              iconTestTag = "goBackButton")
        },
        content = { paddingValues ->
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(16.dp)
                      .padding(paddingValues)
                      .testTag("editNoteColumn")
                      .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                NoteDataTextField(
                    value = updatedNoteTitle,
                    onValueChange = { updatedNoteTitle = it },
                    label = "Note Title",
                    placeholder = "Enter the new title here",
                    modifier = Modifier.fillMaxWidth().testTag("EditTitle textField"),
                    trailingIcon = {
                      IconButton(onClick = { updatedNoteTitle = "" }) {
                        Icon(Icons.Outlined.Clear, contentDescription = "Clear Title")
                      }
                    })

                OptionDropDownMenu(
                    value =
                        visibility?.toReadableString()
                            ?: Note.Visibility.DEFAULT.toReadableString(),
                    expanded = expandedVisibility,
                    buttonTag = "visibilityEditButton",
                    menuTag = "visibilityEditMenu",
                    onExpandedChange = { expandedVisibility = it },
                    items = Note.Visibility.READABLE_STRINGS,
                    onItemClick = { visibility = Note.Visibility.fromReadableString(it) })

                NoteDataTextField(
                    value = updatedClassName,
                    onValueChange = { updatedClassName = it },
                    label = "Class Name",
                    placeholder = "Set the class name for the note",
                    modifier = Modifier.fillMaxWidth().testTag("EditClassName textField"))

                NoteDataTextField(
                    value = updatedClassCode,
                    onValueChange = { updatedClassCode = it },
                    label = "Class Code",
                    placeholder = "Set the class code for the note",
                    modifier = Modifier.fillMaxWidth().testTag("EditClassCode textField"))

                NoteDataTextField(
                    value = updatedClassYear.toString(),
                    onValueChange = { updatedClassYear = it.toIntOrNull() ?: currentYear },
                    label = "Class Year",
                    placeholder = "Set the class year for the note",
                    modifier = Modifier.fillMaxWidth().testTag("EditClassYear textField"))

                NoteDataTextField(
                    value = updatedNoteText,
                    onValueChange = { updatedNoteText = it },
                    label = "Note Content",
                    placeholder = "Enter your note here...",
                    modifier = Modifier.fillMaxWidth().height(400.dp).testTag("EditNote textField"))

                SaveButton(
                    updatedNoteTitle = updatedNoteTitle,
                    note = note,
                    updatedNoteText = updatedNoteText,
                    visibility = visibility,
                    updatedClassCode = updatedClassCode,
                    updatedClassName = updatedClassName,
                    updatedClassYear = updatedClassYear,
                    updatedComments = updatedComments,
                    currentUser = currentUser,
                    navigationActions = navigationActions,
                    noteViewModel = noteViewModel
                )

                DeleteButton(
                    note = note,
                    currentUser = currentUser,
                    navigationActions = navigationActions,
                    noteViewModel = noteViewModel
                )

                AddCommentButton(
                    currentUser = currentUser,
                    updatedComments = updatedComments,
                    onCommentsChange = { updatedComments = it },
                    updateOnlyNoteCommentAndDate = { updateOnlyNoteCommentAndDate() }
                )

                CommentsSection(updatedComments, { updatedComments = it }, { updateOnlyNoteCommentAndDate() })
              }
        })
  }
}

/**
 * Displays a screen indicating that the user was not found.
 */
@Composable
fun UserNotFoundEditScreen() {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text("User  not found ...")
    }
    Log.e("EditNoteScreen", "User not found")
}

/**
 * Displays the comments section of the edit note screen. The section includes a list of comments
 * with text fields for editing each comment, and buttons for deleting comments and adding new
 * comments.
 *
 * @param updatedComments The collection of comments to be displayed and edited.
 * @param onCommentsChange The callback function to update the comments collection.
 * @param updateOnlyNoteCommentAndDate The callback function to update only the note's comments and
 *   date.
 */
@Composable
fun CommentsSection(
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    updateOnlyNoteCommentAndDate: () -> Unit
) {
    if (updatedComments.commentsList.isEmpty()) {
        Text(
            text = "No comments yet. Add a comment to start the discussion.",
            color = Color.Gray,
            modifier = Modifier.padding(8.dp).testTag("NoCommentsText")
        )
    } else {
        updatedComments.commentsList.forEachIndexed { _, comment ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = comment.content,
                    onValueChange = {
                        onCommentsChange(updatedComments.editCommentByCommentId(comment.commentId, it))
                        updateOnlyNoteCommentAndDate()
                    },
                    label = {
                        val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                        val formattedDate = formatter.format(comment.editedDate.toDate())
                        val displayedText = if (comment.isUnedited()) {
                            "${comment.userName} : $formattedDate"
                        } else {
                            "${comment.userName} edited: $formattedDate"
                        }
                        Text(displayedText)
                    },
                    placeholder = { Text("Enter comment here") },
                    modifier = Modifier.weight(1f).testTag("EditCommentTextField")
                )

                IconButton(
                    onClick = {
                        onCommentsChange(updatedComments.deleteCommentByCommentId(comment.commentId))
                        updateOnlyNoteCommentAndDate()
                    },
                    modifier = Modifier.testTag("DeleteCommentButton")
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Comment",
                        tint = Color.Red
                    )
                }
            }
        }
    }
}

/**
 * Displays a button that saves the updated note. The button is enabled only if the note title is
 * not empty. When clicked, the button updates the note in the ViewModel and navigates back to the
 * overview screen.
 *
 * @param updatedNoteTitle The updated title of the note.
 * @param note The note to be updated.
 * @param updatedNoteText The updated content of the note.
 * @param visibility The updated visibility of the note.
 * @param updatedClassCode The updated class code of the note.
 * @param updatedClassName The updated class name of the note.
 * @param updatedClassYear The updated class year of the note.
 * @param updatedComments The updated collection of comments for the note.
 * @param currentUser The current user.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@Composable
fun SaveButton(
    updatedNoteTitle: String,
    note: Note?,
    updatedNoteText: String,
    visibility: Note.Visibility?,
    updatedClassCode: String,
    updatedClassName: String,
    updatedClassYear: Int,
    updatedComments: Note.CommentCollection,
    currentUser: User?,
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel
) {
    Button(
        enabled = updatedNoteTitle.isNotEmpty(),
        onClick = {
            noteViewModel.updateNote(
                Note(
                    id = note?.id ?: "1",
                    title = updatedNoteTitle,
                    content = updatedNoteText,
                    date = Timestamp.now(), // Use current timestamp
                    visibility = visibility ?: Note.Visibility.DEFAULT,
                    noteClass = Note.Class(updatedClassCode, updatedClassName, updatedClassYear, "path"),
                    userId = note?.userId ?: currentUser!!.uid,
                    folderId = note?.folderId,
                    image = note?.image ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), // Placeholder Bitmap
                    comments = updatedComments
                ),
                currentUser!!.uid
            )
            if (note?.folderId != null) {
                navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
            } else {
                navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
            }
        },
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        modifier = Modifier.testTag("Save button")
    ) {
        Text("Update note")
    }
}

/**
 * Displays a button that deletes the note. When clicked, the button deletes the note from the
 * ViewModel and navigates back to the overview screen.
 *
 * @param note The note to be deleted.
 * @param currentUser The current user.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@Composable
fun DeleteButton(
    note: Note?,
    currentUser: User?,
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.background,
            contentColor = MaterialTheme.colorScheme.error
        ),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
        onClick = {
            noteViewModel.deleteNoteById(note?.id ?: "", note?.userId ?: currentUser!!.uid)
            navigationActions.navigateTo(Screen.OVERVIEW)
        },
        modifier = Modifier.testTag("Delete button")
    ) {
        Text("Delete note")
    }
}

/**
 * Displays a button that adds a new comment to the note. When clicked, the button adds a new
 * comment to the note in the ViewModel and updates the note's comments and date.
 *
 * @param currentUser The current user.
 * @param updatedComments The updated collection of comments for the note.
 * @param onCommentsChange The callback function to update the comments collection.
 * @param updateOnlyNoteCommentAndDate The callback function to update only the note's comments and
 *   date.
 */
@Composable
fun AddCommentButton(
    currentUser: User?,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    updateOnlyNoteCommentAndDate: () -> Unit
) {
    Button(
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ),
        onClick = {
            onCommentsChange(
                updatedComments.addCommentByUser(
                    currentUser?.uid ?: "1",
                    currentUser?.userName ?: "Invalid username",
                    ""
                )
            )
            updateOnlyNoteCommentAndDate()
        },
        modifier = Modifier.testTag("Add Comment Button")
    ) {
        Text("Add Comment")
    }
}
