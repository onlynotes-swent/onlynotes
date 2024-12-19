package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(1000L) // Delay for 1 second to not saturate firestore
      if (note != null) {

        noteViewModel.updateNote(
            note = note!!.copy(comments = Note.CommentCollection(updatedComments.commentsList)))
      }
    }
  }
  Scaffold(
      floatingActionButton = {},
      modifier = Modifier.testTag("commentsScreen"),
      topBar = {
        EditNoteTopBar(
            title = stringResource(R.string.comments),
            titleTestTag = "commentsTitle",
            noteViewModel = noteViewModel,
            navigationActions = navigationActions)
      },
      bottomBar = {
        Column(modifier = Modifier.background(MaterialTheme.colorScheme.surface)) {
          HorizontalDivider(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)

          SendCommentBar(
              currentUser = currentUser,
              note = note,
              updatedComments = updatedComments,
              { updatedComments = it },
              modifier = Modifier.fillMaxWidth().padding(horizontal = 2.dp, vertical = 2.dp))

          HorizontalDivider(
              color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)

          EditNoteNavigationMenu(
              navigationActions = navigationActions,
              selectedItem = Screen.EDIT_NOTE_COMMENT,
              onClick = {})
        }
      }) { paddingValues ->
        if (currentUser == null) {
          ErrorScreen("User not found. Please sign out then in again.")
        } else if (note == null) {
          ErrorScreen("No note is selected to edit")
        } else {
          Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            // Scrollable comments section
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .align(Alignment.TopStart) // Aligns the Column to the top of the Box
                        .testTag("commentsColumn"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  CommentsSection(
                      updatedComments,
                      { updatedComments = it },
                      currentUser!!,
                      note!!,
                      userViewModel)
                }

            // SendCommentBar fixed at the bottom

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
fun SendCommentBar(
    currentUser: User?,
    note: Note?,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    modifier: Modifier = Modifier
) {
  var commentText by remember { mutableStateOf("") }

  if (currentUser != null && note != null) {
    Row(
        modifier =
            modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface).padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          // TextField for entering a comment
          OutlinedTextField(
              value = commentText,
              onValueChange = { commentText = it },
              modifier = Modifier.weight(1f),
              label = { Text(stringResource(R.string.enter_comment_here)) },
              placeholder = { Text(stringResource(R.string.enter_comment_here)) })

          // Send Button
          FloatingActionButton(
              onClick = {
                if (commentText.isNotEmpty()) {
                  val newComments =
                      updatedComments.addCommentByUser(
                          currentUser.uid, currentUser.userName, commentText)
                  onCommentsChange(newComments)
                  commentText = "" // Clear the text field after sending
                }
              },
              containerColor =
                  if (commentText.isNotEmpty()) MaterialTheme.colorScheme.primary
                  else MaterialTheme.colorScheme.surface,
              contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(imageVector = Icons.Default.Send, contentDescription = "Add Comment")
              }
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
 * @param currentUser The current user.
 * @param note The note being edited.
 */
@Composable
fun CommentsSection(
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    currentUser: User,
    note: Note,
    userViewModel: UserViewModel
) {
  if (updatedComments.commentsList.isEmpty()) {
    NoCommentsText()
  } else {
    updatedComments.commentsList.forEach { comment ->
      CommentRow(
          comment = comment,
          updatedComments = updatedComments,
          onCommentsChange = onCommentsChange,
          currentUser = currentUser,
          note = note,
          userViewModel)
    }
  }
}

@Composable
fun NoCommentsText() {
  Text(
      text = stringResource(R.string.no_comments_text),
      color = Color.Gray,
      modifier = Modifier.padding(8.dp).testTag("noCommentsText"))
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CommentRow(
    comment: Note.Comment,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    currentUser: User,
    note: Note,
    userViewModel: UserViewModel
) {
  var isEditing by remember { mutableStateOf(false) }
  var currentContent by remember { mutableStateOf(comment.content) }
  var expanded by remember { mutableStateOf(false) } // Shared state for CommentOptionsMenu
  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Start) {
        if (isEditing) {
          Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).padding(8.dp)) {
            OutlinedTextField(
                value = currentContent,
                onValueChange = { currentContent = it },
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
                modifier = Modifier.weight(1f).testTag("EditCommentTextField"))
            IconButton(
                onClick = {
                  val updatedCollection =
                      updatedComments.editCommentByCommentId(comment.commentId, currentContent)
                  onCommentsChange(updatedCollection)
                  isEditing = false
                }) {
                  Icon(imageVector = Icons.Default.Check, contentDescription = "Save Comment")
                }
          }
        } else {
          Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp)) {
            Text(
                text = comment.userName, // Replace with your desired title
                style =
                    MaterialTheme.typography.bodySmall.copy(
                        color = MaterialTheme.colorScheme.onBackground),
                modifier = Modifier.padding(bottom = 4.dp) // Space between title and row
                )
            // Outlined Row
            Row(
                modifier =
                    Modifier.fillMaxWidth()
                        .border(
                            1.dp,
                            MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(4.dp))
                        .padding(8.dp)
                        .combinedClickable(
                            onClick = { /* Add desired click behavior if needed */},
                            onLongClick = {
                              if (comment.isOwner(currentUser.uid) ||
                                  note.isOwner(currentUser.uid)) {
                                expanded = true
                              }
                            })) {
                  Text(
                      text = comment.content,
                      modifier =
                          Modifier.weight(
                                  1f) // Ensures the text takes up all available space before the
                              // Spacer
                              .padding(
                                  end = 8.dp) // Optional: adds some space between text and options
                      // menu
                      )
                  Spacer(modifier = Modifier.weight(0.1f)) // Add this to push the menu to the end
                  if (comment.isOwner(currentUser.uid) || note.isOwner(currentUser.uid)) {
                    CommentOptionsMenu(
                        comment = comment,
                        updatedComments = updatedComments,
                        onCommentsChange = onCommentsChange,
                        currentUser = currentUser,
                        note = note,
                        expanded = expanded,
                        onDismissRequest = { expanded = false },
                        onEditRequest = { isEditing = true })
                  }
                }
          }
        }
      }
}

@Composable
fun CommentOptionsMenu(
    comment: Note.Comment,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    currentUser: User,
    note: Note,
    expanded: Boolean, // Receive expanded state
    onDismissRequest: () -> Unit, // Callback to dismiss menu
    onEditRequest: () -> Unit
) {
  DropdownMenu(
      expanded = expanded, onDismissRequest = onDismissRequest // Use callback to handle dismissal
      ) {
        if (comment.isOwner(currentUser.uid)) {
          DropdownMenuItem(
              modifier = Modifier.background(MaterialTheme.colorScheme.onPrimary),
              text = { Text("Edit comment") },
              onClick = {
                onDismissRequest()
                onEditRequest()
              })
        }
        if (comment.isOwner(currentUser.uid) || note.isOwner(currentUser.uid)) {
          DropdownMenuItem(
              text = { Text("Delete comment") },
              onClick = {
                onDismissRequest()
                onCommentsChange(updatedComments.deleteCommentByCommentId(comment.commentId))
              })
        }
      }
}
