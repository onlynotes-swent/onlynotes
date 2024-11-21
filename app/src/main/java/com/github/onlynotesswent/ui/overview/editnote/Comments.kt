package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
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
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale

@Composable
fun CommentsScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {

  val note by noteViewModel.selectedNote.collectAsState()
  val currentUser by userViewModel.currentUser.collectAsState()
  var updatedComments by remember { mutableStateOf(note!!.comments) }
  Scaffold(
      modifier = Modifier.testTag("editNoteScreen"),
      topBar = {
        ScreenTopBar(
            title = "Edit note",
            titleTestTag = "editNoteTitle",
            onBackClick = {
              // Unselects the note and navigates back to the previous screen
              noteViewModel.selectedNote(null)
              navigationActions.goBack()
            },
            icon = {
              Icon(
                  imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                  contentDescription = "Back",
                  tint = MaterialTheme.colorScheme.onSurface)
            },
            iconTestTag = "goBackButton")
      },
      bottomBar = { EditNoteNavigationMenu(navigationActions, Screen.EDIT_NOTE_COMMENT) }) {
          paddingValues ->
        if (currentUser == null) {
          ErrorScreen("User not found. Please sign out then in again.")
        } else if (note == null) {
          ErrorScreen("No note is selected to edit")
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(16.dp)
                      .padding(paddingValues)
                      .testTag("editNoteColumn")
                      .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                AddCommentButton(
                    currentUser = currentUser!!,
                    updatedComments = updatedComments,
                    onCommentsChange = { updatedComments = it },
                    updateNoteComment = {
                      noteViewModel.updateNote(
                          note!!.copy(comments = updatedComments), currentUser!!.uid)
                    })
                CommentsSection(
                    updatedComments,
                    { updatedComments = it },
                    {
                      noteViewModel.updateNote(
                          note!!.copy(comments = updatedComments), currentUser!!.uid)
                    })
              }
        }
      }
}

/**
 * Displays a button that adds a new comment to the note. When clicked, the button adds a new
 * comment to the note in the ViewModel and updates the note's comments and date.
 *
 * @param currentUser The current user.
 * @param updatedComments The updated collection of comments for the note.
 * @param onCommentsChange The callback function to update the comments collection.
 * @param updateNoteComment The callback function to update the note's comments.
 */
@Composable
fun AddCommentButton(
    currentUser: User,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    updateNoteComment: () -> Unit
) {
  Button(
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary),
      onClick = {
        onCommentsChange(
            updatedComments.addCommentByUser(currentUser.uid, currentUser.userName, ""))
        updateNoteComment()
      },
      modifier = Modifier.testTag("Add Comment Button")) {
        Text("Add Comment")
      }
}

/**
 * Displays the comments section of the edit note screen. The section includes a list of comments
 * with text fields for editing each comment, and buttons for deleting comments and adding new
 * comments.
 *
 * @param updatedComments The collection of comments to be displayed and edited.
 * @param onCommentsChange The callback function to update the comments collection.
 * @param updateNoteComment The callback function to update only the note's comments and date.
 */
@Composable
fun CommentsSection(
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    updateNoteComment: () -> Unit
) {
  if (updatedComments.commentsList.isEmpty()) {
    Text(
        text = "No comments yet. Add a comment to start the discussion.",
        color = Color.Gray,
        modifier = Modifier.padding(8.dp).testTag("NoCommentsText"))
  } else {
    updatedComments.commentsList.forEachIndexed { _, comment ->
      Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically) {
            OutlinedTextField(
                value = comment.content,
                onValueChange = {
                  onCommentsChange(updatedComments.editCommentByCommentId(comment.commentId, it))
                  updateNoteComment()
                },
                label = {
                  val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                  val formattedDate = formatter.format(comment.editedDate.toDate())
                  val displayedText =
                      if (comment.isUnedited()) {
                        "${comment.userName} : $formattedDate"
                      } else {
                        "${comment.userName} edited: $formattedDate"
                      }
                  Text(displayedText)
                },
                placeholder = { Text("Enter comment here") },
                modifier = Modifier.weight(1f).testTag("EditCommentTextField"))

            IconButton(
                onClick = {
                  onCommentsChange(updatedComments.deleteCommentByCommentId(comment.commentId))
                  updateNoteComment()
                },
                modifier = Modifier.testTag("DeleteCommentButton")) {
                  Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Delete Comment",
                      tint = Color.Red)
                }
          }
    }
  }
}
