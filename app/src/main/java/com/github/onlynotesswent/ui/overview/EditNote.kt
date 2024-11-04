package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
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
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.Calendar

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
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {
  val note by noteViewModel.selectedNote.collectAsState()
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
            type = note?.type ?: Note.Type.NORMAL_TEXT,
            title = note?.title ?: "",
            content = note?.content ?: "",
            date = Timestamp.now(), // Use current timestamp
            visibility = note?.visibility ?: Note.Visibility.DEFAULT,
            noteClass = note?.noteClass ?: Note.Class("", "", currentYear, ""),
            userId = note?.userId ?: "",
            image =
                note?.image
                    ?: Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888), // Placeholder Bitmap
            comments = updatedComments),
        userViewModel.currentUser.value!!.uid)
  }

  Scaffold(
      modifier = Modifier.testTag("editNoteScreen"),
      topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB3E5FC)),
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1.5f))
                    Text("Edit note", Modifier.testTag("editNoteTitle"))
                    Spacer(modifier = Modifier.weight(2f))
                  }
            },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
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
              OutlinedTextField(
                  value = updatedNoteTitle,
                  onValueChange = { updatedNoteTitle = it },
                  label = { Text("Note Title") },
                  placeholder = { Text("Enter the new title here") },
                  modifier = Modifier.fillMaxWidth().testTag("EditTitle textField"))

              OptionDropDownMenu(
                  value =
                      visibility?.toReadableString() ?: Note.Visibility.DEFAULT.toReadableString(),
                  expanded = expandedVisibility,
                  buttonTag = "visibilityEditButton",
                  menuTag = "visibilityEditMenu",
                  onExpandedChange = { expandedVisibility = it },
                  items = Note.Visibility.READABLE_STRINGS,
                  onItemClick = { visibility = Note.Visibility.fromReadableString(it) })

              OutlinedTextField(
                  value = updatedClassName,
                  onValueChange = { updatedClassName = it },
                  label = { Text("Class Name") },
                  placeholder = { Text("Set the Class Name for the Note") },
                  modifier = Modifier.fillMaxWidth().testTag("EditClassName textField"))

              OutlinedTextField(
                  value = updatedClassCode,
                  onValueChange = { updatedClassCode = it },
                  label = { Text("Class Code") },
                  placeholder = { Text("Set the Class Code for the Note") },
                  modifier = Modifier.fillMaxWidth().testTag("EditClassCode textField"))

              OutlinedTextField(
                  value = updatedClassYear.toString(),
                  onValueChange = { updatedClassYear = it.toIntOrNull() ?: currentYear },
                  label = { Text("Class Year") },
                  placeholder = { Text("Set the Class Year for the Note") },
                  modifier = Modifier.fillMaxWidth().testTag("EditClassYear textField"))

              OutlinedTextField(
                  value =
                      if (note?.type == Note.Type.NORMAL_TEXT) "Typed note"
                      else note?.type?.name ?: "Typed note",
                  onValueChange = {},
                  readOnly = true,
                  label = { Text("Note Type") },
                  modifier = Modifier.fillMaxWidth().testTag("EditType textField"))
              if (note?.type == Note.Type.NORMAL_TEXT) {
                OutlinedTextField(
                    value = updatedNoteText,
                    onValueChange = { updatedNoteText = it },
                    label = { Text("Note Content") },
                    placeholder = { Text("Enter your note here...") },
                    modifier = Modifier.fillMaxWidth().height(400.dp).testTag("EditNote textField"))
              }

              Button(
                  enabled = updatedNoteTitle.isNotEmpty(),
                  onClick = {
                    noteViewModel.updateNote(
                        Note(
                            id = note?.id ?: "1",
                            type = note?.type ?: Note.Type.NORMAL_TEXT,
                            title = updatedNoteTitle,
                            content = updatedNoteText,
                            date = Timestamp.now(), // Use current timestamp
                            visibility = visibility ?: Note.Visibility.DEFAULT,
                            noteClass =
                                Note.Class(
                                    updatedClassCode, updatedClassName, updatedClassYear, "path"),
                            userId = note?.userId ?: userViewModel.currentUser.value!!.uid,
                            image =
                                note?.image
                                    ?: Bitmap.createBitmap(
                                        1, 1, Bitmap.Config.ARGB_8888) // Placeholder Bitmap
                            ),
                        userViewModel.currentUser.value!!.uid)
                    navigationActions.navigateTo(Screen.OVERVIEW)
                  },
                  modifier = Modifier.testTag("Save button")) {
                    Text("Update note")
                  }

              Button(
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF0000)),
                  onClick = {
                    noteViewModel.deleteNoteById(
                        note?.id ?: "", note?.userId ?: userViewModel.currentUser.value!!.uid)
                    navigationActions.navigateTo(Screen.OVERVIEW)
                  },
                  modifier = Modifier.testTag("Delete button")) {
                    Text("Delete note")
                  }
              Button(
                  onClick = {
                    updatedComments =
                        Note.CommentCollection.addComment(
                            userViewModel.currentUser.value?.uid ?: "1", "", updatedComments)
                    updateOnlyNoteCommentAndDate()
                  },
                  modifier = Modifier.testTag("Add Comment Button")) {
                    Text("Add Comment")
                  }

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
                              updatedComments =
                                  Note.CommentCollection.editComment(
                                      comment.commentId, it, updatedComments)
                              updateOnlyNoteCommentAndDate()
                            },
                            label = { Text(comment.userId) },
                            placeholder = { Text("Enter comment here") },
                            modifier = Modifier.weight(1f).testTag("EditCommentTextField"))

                        IconButton(
                            onClick = {
                              updatedComments =
                                  Note.CommentCollection.deleteComment(
                                      comment.commentId, updatedComments)
                              updateOnlyNoteCommentAndDate()
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
      })
}