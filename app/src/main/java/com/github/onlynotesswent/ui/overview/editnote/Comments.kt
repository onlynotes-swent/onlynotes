package com.github.onlynotesswent.ui.overview.editnote

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.MoreVert
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
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ThumbnailDynamicPic
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.theme.Typography

/**
 * Screen for viewing and editing comments on a note.
 *
 * @param navigationActions Actions used for navigating between screens.
 *     @param noteViewModel The ViewModel for notes.
 *     @param userViewModel The ViewModel for users.
 *     @param fileViewModel The ViewModel for files.
 */
@Composable
fun CommentsScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
) {
  val note by noteViewModel.selectedNote.collectAsState()
  var noteUpdated by remember { mutableStateOf(note) }
  val currentUser by userViewModel.currentUser.collectAsState()
  val updatedComments by remember { derivedStateOf { (noteUpdated!!.comments) } }

  LaunchedEffect(Unit) {
    while (true) {
      kotlinx.coroutines.delay(1000L)
      // Delay for 1 second to not saturate firestore but also to not get too much lag
      if (note != null) {
        noteViewModel.getNoteById(note!!.id, { noteUpdated = it }, useCache = (note!!.userId == userViewModel.currentUser.value?.uid))
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
              note = noteUpdated,
              updatedComments = updatedComments,
              {
                val newNote = noteUpdated!!.copy(comments = Note.CommentCollection(it.commentsList))
                noteViewModel.selectedNote(newNote)
                noteViewModel.updateNote(note = newNote)
              },
              modifier =
                  Modifier.testTag("SendCommentBar")
                      .fillMaxWidth()
                      .padding(horizontal = 2.dp, vertical = 2.dp))

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
            Column(
                modifier =
                    Modifier.fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp)
                        .align(Alignment.TopStart)
                        .testTag("commentsColumn"),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                  CommentsSection(
                      updatedComments,
                      {
                        val newNote =
                            note!!.copy(comments = Note.CommentCollection(it.commentsList))
                        noteViewModel.selectedNote(newNote)
                        noteViewModel.updateNote(note = newNote)
                      },
                      currentUser!!,
                      noteUpdated!!,
                      userViewModel,
                      fileViewModel)
                }
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
              modifier = Modifier.weight(1f).padding(bottom = 8.dp).testTag("SendCommentTextField"),
              label = { Text(stringResource(R.string.enter_comment_here)) },
              placeholder = { Text(stringResource(R.string.enter_comment_here)) })

          // Send Button
          FloatingActionButton(
              modifier = Modifier.testTag("SendCommentButton"),
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
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Add Comment")
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
 * @param userViewModel The ViewModel for users.
 * @param fileViewModel The ViewModel for files.
 */
@Composable
fun CommentsSection(
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    currentUser: User,
    note: Note,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
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
          userViewModel,
          fileViewModel)
    }
  }
}
/** Displays a message when there are no comments to display. */
@Composable
fun NoCommentsText() {
  Text(
      text = stringResource(R.string.no_comments_text),
      color = Color.Gray,
      modifier = Modifier.padding(8.dp).testTag("noCommentsText"))
}
/**
 * Represents a comment in the comments section of the edit note screen. The comment includes the
 * user's profile picture, user handle, comment content, and options menu for editing and deleting
 * the comment.
 *
 * @param comment The comment to be displayed.
 * @param updatedComments The updated collection of comments for the note.
 * @param onCommentsChange The callback function to update the comments collection.
 * @param currentUser The current user.
 * @param note The note being edited.
 * @param userViewModel The ViewModel for users.
 * @param fileViewModel The ViewModel for files.
 */
@Composable
fun CommentRow(
    comment: Note.Comment,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    currentUser: User,
    note: Note,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
) {
  var isEditing by remember { mutableStateOf(false) }
  var currentContent by remember { mutableStateOf(comment.content) }
  var expanded by remember { mutableStateOf(false) } // Shared state for CommentOptionsMenu
  val commentUser = remember { mutableStateOf<User?>(null) }
  userViewModel.getUserById(comment.userId, { commentUser.value = it })
  if (commentUser.value == null) {
    return
  }
  if (isEditing) {
    // Editing Mode
    // Top Row: Thumbnail, User Handle, and Options Menu
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically) {
          // Thumbnail on the left
          ThumbnailDynamicPic(commentUser, fileViewModel, size = 30)

          // User handle in the center
          Text(
              text = commentUser.value!!.userHandle(),
              style = Typography.bodyMedium,
              modifier = Modifier.weight(2f).alpha(0.7f).padding(start = 4.dp))
        }
    Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp)) {
      TextField(
          value = currentContent,
          onValueChange = { currentContent = it },
          placeholder = { Text(stringResource(R.string.enter_comment_here)) },
          modifier = Modifier.weight(1f).testTag("EditCommentTextField"))
      IconButton(
          modifier = Modifier.testTag("SaveCommentButton"),
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
    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
      Row(
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp, vertical = 4.dp),
          verticalAlignment = Alignment.CenterVertically) {
            ThumbnailDynamicPic(commentUser, fileViewModel, size = 30)

            Text(
                text = commentUser.value!!.userHandle(),
                style = Typography.bodyMedium,
                modifier = Modifier.weight(2f).alpha(0.7f).padding(start = 4.dp))
            // Options menu on the right
            if (comment.isOwner(currentUser.uid) || note.isOwner(currentUser.uid)) {
              Box { // Anchor box for the dropdown
                IconButton(
                    onClick = { expanded = true },
                    modifier = Modifier.testTag("CommentOptionsButton")) {
                      Icon(
                          imageVector = Icons.Default.MoreVert, contentDescription = "More Options")
                    }
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

      Text(
          text = comment.content,
          modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp).testTag("CommentContent"),
          style = Typography.bodyLarge)
    }
  }
  HorizontalDivider(color = Color.Gray, thickness = 0.5.dp)
}
/**
 * Displays a dropdown menu with options for editing and deleting a comment.
 *
 * @param comment The comment to be edited or deleted.
 * @param updatedComments The updated collection of comments for the note.
 * @param onCommentsChange The callback function to update the comments collection.
 * @param currentUser The current user.
 * @param note The note being edited.
 * @param expanded The expanded state of the dropdown menu.
 * @param onDismissRequest The callback function to dismiss the dropdown menu.
 * @param onEditRequest The callback function to edit the comment.
 */
@Composable
fun CommentOptionsMenu(
    comment: Note.Comment,
    updatedComments: Note.CommentCollection,
    onCommentsChange: (Note.CommentCollection) -> Unit,
    currentUser: User,
    note: Note,
    expanded: Boolean,
    onDismissRequest: () -> Unit,
    onEditRequest: () -> Unit
) {
  DropdownMenu(
      modifier =
          Modifier.background(MaterialTheme.colorScheme.onPrimary).testTag("CommentOptionsMenu"),
      expanded = expanded,
      onDismissRequest = onDismissRequest // Use callback to handle dismissal
      ) {
        if (comment.isOwner(currentUser.uid)) {
          DropdownMenuItem(
              modifier =
                  Modifier.background(MaterialTheme.colorScheme.onPrimary)
                      .testTag("EditCommentMenuItem"),
              text = { Text("Edit comment") },
              onClick = {
                onDismissRequest()
                onEditRequest()
              })
        }
        if (comment.isOwner(currentUser.uid) || note.isOwner(currentUser.uid)) {
          DropdownMenuItem(
              modifier =
                  Modifier.background(MaterialTheme.colorScheme.onPrimary)
                      .testTag("DeleteCommentMenuItem"),
              text = { Text("Delete comment") },
              onClick = {
                onDismissRequest()
                onCommentsChange(updatedComments.deleteCommentByCommentId(comment.commentId))
              })
        }
      }
}
