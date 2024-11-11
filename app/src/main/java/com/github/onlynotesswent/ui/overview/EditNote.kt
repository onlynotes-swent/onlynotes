package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import java.io.File
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
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
) {
  val state = rememberRichTextState()
  val context = LocalContext.current
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
  var attemptedDoawnloadAttempts = 0
    /**
     * Downloads a markdown file associated with the note. If no file exists, it attempts once to create and upload an empty markdown file, then re-download it.
     */
    fun downloadMarkdownFile() {
    fileViewModel.downloadFile(
        uid = note?.id ?: "errorNoId",
        fileType = FileType.NOTE_TEXT,
        context = context,
        onSuccess = { downloadedFile: File ->
          // Update the UI with the downloaded file reference
          state.setMarkdown(downloadedFile.readText())
        },
        onFailure = { _ ->
          attemptedDoawnloadAttempts += 1
          if (attemptedDoawnloadAttempts < 2) {
            val file = File(context.cacheDir, "${note?.id ?: "errorNoId"}.md")
            if (!file.exists()) {
              file.createNewFile()
            }
            file.writeText("")
            // Get the file URI
            val fileUri = Uri.fromFile(file)

            fileViewModel.uploadFile(note?.id ?: "errorNoId", fileUri, FileType.NOTE_TEXT)
            Toast.makeText(
                    context,
                    "No markdown associated attempting to attach a Markdown to this note",
                    Toast.LENGTH_SHORT)
                .show()
            downloadMarkdownFile()
          }
          Toast.makeText(context, "Failed to attach a Markdown to this note", Toast.LENGTH_SHORT)
              .show()
        })
  }
  LaunchedEffect(Unit) { downloadMarkdownFile() }

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
    // If the user is null, display an error message
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text("User  not found ...")
        }
    Log.e("EditNoteScreen", "User not found")
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

                RichTextEditor(
                    modifier = Modifier.fillMaxWidth().pointerInput(Unit) {},
                    state = state,
                    readOnly = true)

                Button(
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary),
                    onClick = { navigationActions.navigateTo(Screen.EDIT_MARKDOWN) },
                    modifier = Modifier.testTag("Edit Markdown button")) {
                      Text("Edit Markdown")
                    }

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
                              noteClass =
                                  Note.Class(
                                      updatedClassCode, updatedClassName, updatedClassYear, "path"),
                              userId = note?.userId ?: currentUser!!.uid,
                              folderId = note?.folderId,
                              image =
                                  note?.image
                                      ?: Bitmap.createBitmap(
                                          1, 1, Bitmap.Config.ARGB_8888), // Placeholder Bitmap
                              comments = updatedComments),
                          currentUser!!.uid)
                      if (note?.folderId != null) {
                        navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                      } else {
                        navigationActions.navigateTo(Screen.OVERVIEW)
                      }
                    },
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary),
                    modifier = Modifier.testTag("Save button")) {
                      Text("Update note")
                    }

                Button(
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.background,
                            contentColor = MaterialTheme.colorScheme.error),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    onClick = {
                      noteViewModel.deleteNoteById(
                          note?.id ?: "", note?.userId ?: currentUser!!.uid)
                      navigationActions.navigateTo(Screen.OVERVIEW)
                    },
                    modifier = Modifier.testTag("Delete button")) {
                      Text("Delete note")
                    }

                Button(
                    colors =
                        ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary),
                    onClick = {
                      updatedComments =
                          updatedComments.addCommentByUser(
                              currentUser?.uid ?: "1",
                              currentUser?.userName ?: "Invalid username",
                              "")
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
                                    updatedComments.editCommentByCommentId(comment.commentId, it)
                                updateOnlyNoteCommentAndDate()
                              },
                              label = {
                                val formatter =
                                    SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
                                val formatedDate = formatter.format(comment.editedDate.toDate())
                                val displayedText =
                                    if (comment.isUnedited()) {
                                      "${comment.userName} : $formatedDate"
                                    } else {
                                      "${comment.userName} edited: $formatedDate"
                                    }
                                Text(displayedText)
                              },
                              placeholder = { Text("Enter comment here") },
                              modifier = Modifier.weight(1f).testTag("EditCommentTextField"))

                          IconButton(
                              onClick = {
                                updatedComments =
                                    updatedComments.deleteCommentByCommentId(comment.commentId)
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
}
