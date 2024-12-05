package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
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
      floatingActionButton = {
        AddCommentButton(
            currentUser = currentUser,
            note = note,
            updatedComments = updatedComments,
            onCommentsChange = { updatedComments = it })
      },
      modifier = Modifier.testTag("commentsScreen"),
      topBar = {
        EditNoteTopBar(
            title = stringResource(R.string.comments),
            titleTestTag = "commentsTitle",
            noteViewModel = noteViewModel,
            navigationActions = navigationActions,
            onClick = {
              val commentsNotEmpty = updatedComments.commentsList.filter { it.content.isNotEmpty() }
              noteViewModel.updateNote(
                  note!!.copy(comments = Note.CommentCollection(commentsNotEmpty)))
            })
      },
      bottomBar = {
        EditNoteNavigationMenu(
            navigationActions = navigationActions,
            selectedItem = Screen.EDIT_NOTE_COMMENT,
            onClick = {
              val commentsNotEmpty = updatedComments.commentsList.filter { it.content.isNotEmpty() }
              noteViewModel.updateNote(
                  note!!.copy(comments = Note.CommentCollection(commentsNotEmpty)))
              noteViewModel.getNoteById(note!!.id)
            })
      }) { paddingValues ->
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
                      .testTag("commentsColumn")
                      .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                CommentsSection(updatedComments, { updatedComments = it })
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
 */
@Composable
fun AddCommentButton(
    currentUser: User?,
    note: Note?,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
) {
  if (currentUser != null && note != null) {
    FloatingActionButton(
        modifier = Modifier.testTag("addCommentButton"),
        onClick = {
          onCommentsChange(
              updatedComments.addCommentByUser(currentUser.uid, currentUser.userName, ""))
        },
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
    ) {
      Icon(imageVector = Icons.Default.Add, contentDescription = "Add Comment")
    }
  }
}

/**
 * Displays the comments section of the edit note screen. The section includes a list of comments
 * with text fields for editing each comment, and buttons for deleting comments and adding new
 * comments.
 *
 * @param updatedComments The collection of comments to be displayed and edited.
 * @param onCommentsChange The callback function to update the comments collection.
 */
@Composable
fun CommentsSection(
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
) {
  if (updatedComments.commentsList.isEmpty()) {
    Text(
        text = stringResource(R.string.no_comments_text),
        color = Color.Gray,
        modifier = Modifier.padding(8.dp).testTag("noCommentsText"))
  } else {
    updatedComments.commentsList.forEachIndexed { _, comment ->
      Row(
          modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically, // Align items vertically
          horizontalArrangement = Arrangement.SpaceBetween // Distribute space
          ) {
            // Comment TextField
            OutlinedTextField(
                value = comment.content,
                onValueChange = {
                  val updatedCollection =
                      updatedComments.editCommentByCommentId(comment.commentId, it)
                  onCommentsChange(updatedCollection)
                },
                label = {
                  val formatter = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                  val formattedDate = formatter.format(comment.editedDate.toDate())
                  val displayedText =
                      if (comment.isUnedited()) {
                        "${comment.userName} : $formattedDate"
                      } else {
                        stringResource(R.string.edited, comment.userName, formattedDate)
                      }
                  Text(displayedText)
                },
                placeholder = { Text(stringResource(R.string.enter_comment_here)) },
                modifier =
                    Modifier.weight(1f) // Ensure the text field takes up remaining space
                        .testTag("EditCommentTextField"))

            // Delete Button with Background
            Box(
                modifier =
                    Modifier.padding(
                            start = 8.dp, top = 7.dp) // Space between TextField and IconButton
                        .background(
                            color = MaterialTheme.colorScheme.error,
                            shape = MaterialTheme.shapes.small)
                        .size(50.dp), // Ensure consistent size for the delete button
                contentAlignment = Alignment.Center) {
                  IconButton(
                      onClick = {
                        onCommentsChange(
                            updatedComments.deleteCommentByCommentId(comment.commentId))
                      },
                      modifier = Modifier.testTag("DeleteCommentButton")) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Comment",
                            tint = MaterialTheme.colorScheme.onError,
                        )
                      }
                }
          }
    }
  }
}
