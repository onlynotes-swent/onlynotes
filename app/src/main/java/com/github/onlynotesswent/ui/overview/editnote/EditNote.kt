package com.github.onlynotesswent.ui.overview.editnote

import android.util.Log
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ConfirmationPopup
import com.github.onlynotesswent.ui.common.NoteDataTextField
import com.github.onlynotesswent.ui.common.SelectVisibility
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp

/**
 * Displays the edit note screen, where users can update the title and content of an existing note.
 * The screen includes two text fields for editing the note's title and content, and a button to
 * save the changes. The save button updates the note in the ViewModel and navigates back to the
 * overview screen.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 * @param userViewModel The ViewModel that provides the current user.
 */
@Composable
fun EditNoteScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {
  val note by noteViewModel.selectedNote.collectAsState()
  val currentUser by userViewModel.currentUser.collectAsState()

  // State variables to hold the editable fields
  var noteTitle by remember { mutableStateOf(note?.title ?: "") }
  var courseName by remember { mutableStateOf(note?.noteCourse?.courseName ?: "") }
  var courseCode by remember { mutableStateOf(note?.noteCourse?.courseCode ?: "") }
  var courseYear by remember { mutableStateOf(note?.noteCourse?.courseYear) }
  var visibility by remember { mutableStateOf(note?.visibility ?: Visibility.DEFAULT) }

  var isModified by remember { mutableStateOf(false) }

  // Check if the note has been modified
  LaunchedEffect(note, noteTitle, courseName, courseCode, courseYear, visibility) {
    isModified =
        note != null &&
            (noteTitle != note!!.title ||
                courseName != (note!!.noteCourse?.courseName ?: "") ||
                courseCode != (note!!.noteCourse?.courseCode ?: "") ||
                courseYear != (note!!.noteCourse?.courseYear) ||
                visibility != note!!.visibility)
  }

  Scaffold(
      floatingActionButton = {
        if (note != null && note!!.isOwner(currentUser!!.uid)) {
          DeleteButton(currentUser, note, navigationActions, noteViewModel)
        }
      },
      modifier = Modifier.testTag("editNoteScreen"),
      topBar = {
        EditNoteGeneralTopBar(
            noteViewModel = noteViewModel,
            userViewModel = userViewModel,
            navigationActions = navigationActions,
            actions = {
              if (note != null && currentUser != null && note!!.isOwner(currentUser!!.uid)) {
                SaveButton(
                    noteTitle = noteTitle,
                    note = note!!,
                    visibility = visibility,
                    courseCode = courseCode,
                    courseName = courseName,
                    courseYear = courseYear,
                    noteViewModel = noteViewModel)
              }
            },
            isModified = isModified)
      },
      bottomBar = {
        EditNoteNavigationMenu(
            navigationActions = navigationActions,
            selectedItem = Screen.EDIT_NOTE,
            isModified = isModified)
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
                      .testTag("editNoteColumn")
                      .verticalScroll(rememberScrollState()),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              horizontalAlignment = Alignment.CenterHorizontally) {
                // Display the note fields
                NoteSection(
                    noteTitle = noteTitle,
                    onNoteTitleChange = { noteTitle = it },
                    courseName = courseName,
                    onCourseNameChange = { courseName = it },
                    courseCode = courseCode,
                    onCourseCodeChange = { courseCode = it },
                    courseYear = courseYear,
                    onCourseYearChange = { courseYear = it.toIntOrNull() },
                    visibility = visibility,
                    onVisibilityChange = { visibility = it },
                    currentUserId = currentUser!!.uid,
                    note = note!!)
              }
        }
      }
}

/**
 * Displays the top app bar for the edit note screen. The top app bar includes the title of the
 * screen and a close button that navigates back to the overview screen. If the user has made
 * changes to the note, a dialog is displayed to confirm discarding the changes.
 *
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 * @param userViewModel The ViewModel that provides the current user.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param actions The actions to display in the top app bar.
 * @param isModified A flag indicating whether the note has been modified.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditNoteGeneralTopBar(
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions,
    actions: @Composable RowScope.() -> Unit,
    isModified: Boolean
) {
  var showDiscardChangesDialog by remember { mutableStateOf(false) }
  val note by noteViewModel.selectedNote.collectAsState()
  val currentUser by userViewModel.currentUser.collectAsState()

  TopAppBar(
      title = {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
              if (currentUser?.uid != note?.userId) {
                Spacer(modifier = Modifier.weight(1.5f))
              }
              Text(
                  if (currentUser?.uid == note?.userId) "Edit Note" else "View Note",
                  color = MaterialTheme.colorScheme.onSurface,
                  modifier = Modifier.testTag("editNoteTitle"))
              if (currentUser?.uid != note?.userId) {
                Spacer(modifier = Modifier.weight(2f))
              }
            }
      },
      navigationIcon = {
        IconButton(
            modifier = Modifier.testTag("closeButton"),
            onClick = {
              // Check if any fields were modified
              if (isModified) {
                showDiscardChangesDialog = true
              } else {
                navigationActions.goBack()
                noteViewModel.selectedNote(null)
              }
            }) {
              Icon(Icons.Default.Close, contentDescription = "Close")
            }
      },
      actions = { actions() })

  // Discard Changes Dialog
  if (showDiscardChangesDialog) {
    ConfirmationPopup(
        title = "Discard Changes?",
        text = "You have unsaved changes. Are you sure you want to discard them?",
        onConfirm = {
          // Discard changes and navigate away
          showDiscardChangesDialog = false
          if (noteViewModel.selectedNote.value?.folderId != null) {
            navigationActions.navigateTo(
                Screen.FOLDER_CONTENTS.replace(
                    oldValue = "{folderId}",
                    newValue = noteViewModel.selectedNote.value?.folderId!!))
          } else {
            navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
          }
          noteViewModel.selectedNote(null)
        },
        onDismiss = {
          // Close the dialog without discarding
          showDiscardChangesDialog = false
        })
  }
}

/**
 * Displays the note fields for editing. The fields include the note title, visibility, course name,
 *
 * @param noteTitle The title of the note.
 * @param onNoteTitleChange The callback function to update the note title.
 * @param courseName The name of the course.
 * @param onCourseNameChange The callback function to update the course name.
 * @param courseCode The code of the course.
 * @param onCourseCodeChange The callback function to update the course code.
 * @param courseYear The year of the course.
 * @param onCourseYearChange The callback function to update the course year.
 * @param visibility The visibility of the note.
 * @param onVisibilityChange The callback function to update the visibility.
 * @param currentUserId The Id of the current user.
 * @param note The note to be edited.
 */
@Composable
fun NoteSection(
    noteTitle: String,
    onNoteTitleChange: (String) -> Unit,
    courseName: String,
    onCourseNameChange: (String) -> Unit,
    courseCode: String,
    onCourseCodeChange: (String) -> Unit,
    courseYear: Int?,
    onCourseYearChange: (String) -> Unit,
    visibility: Visibility,
    onVisibilityChange: (Visibility) -> Unit,
    currentUserId: String,
    note: Note
) {
  var showCourseDetails by remember { mutableStateOf(false) }

  val isEnabled = note.isOwner(currentUserId)

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(text = "Title", style = MaterialTheme.typography.titleMedium)
    NoteDataTextField(
        value = noteTitle,
        onValueChange = { onNoteTitleChange(Note.formatTitle(it)) },
        label = "",
        placeholder = "Enter the new title here",
        modifier = Modifier.fillMaxWidth().testTag("EditTitle textField"),
        enabled = note.isOwner(currentUserId),
        trailingIcon = {
          IconButton(onClick = { onNoteTitleChange("") }, enabled = isEnabled) {
            Icon(Icons.Outlined.Clear, contentDescription = "Clear Title")
          }
        })
  }

  Spacer(modifier = Modifier.height(8.dp))

  Column(modifier = Modifier.fillMaxWidth()) {
    Text(text = "Visibility", style = MaterialTheme.typography.titleMedium)
    SelectVisibility(visibility, currentUserId, note.userId) { onVisibilityChange(it) }
  }

  Spacer(modifier = Modifier.height(8.dp))

  // Course Section
  Column(modifier = Modifier.fillMaxWidth()) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween, // Align title and button
        verticalAlignment = Alignment.CenterVertically // Align text and button vertically
        ) {
          Text(
              text = "Course",
              style = MaterialTheme.typography.titleMedium,
              modifier = Modifier.padding(bottom = 8.dp))

          IconButton(onClick = { showCourseDetails = !showCourseDetails }) {
            Icon(
                imageVector =
                    if (showCourseDetails) Icons.Default.ArrowDropUp
                    else Icons.Default.ArrowDropDown,
                contentDescription =
                    if (showCourseDetails) "Hide Course Details" else "Show Course Details")
          }
        }

    // Course Full Name
    if (!showCourseDetails) {
      Box(
          modifier =
              Modifier.fillMaxWidth()
                  .testTag("CourseFullName textField")
                  .clickable { showCourseDetails = !showCourseDetails } // Handle click
                  .border(1.dp, Color.Black, OutlinedTextFieldDefaults.shape)) {
            val course = Course(courseCode, courseName, courseYear, "")
            val courseFullName = if (course == Course.EMPTY) "No course" else course.fullName()
            Text(
                text = courseFullName,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(16.dp))
          }
    }

    // Animated dropdown for course details
    AnimatedVisibility(
        visible = showCourseDetails, enter = expandVertically(), exit = shrinkVertically()) {
          Column(modifier = Modifier.fillMaxWidth()) {
            // Course Code
            NoteDataTextField(
                value = courseCode,
                onValueChange = { onCourseCodeChange(Course.formatCourseCode(it)) },
                label = "Course Code",
                placeholder = "Set the course code for the note",
                modifier = Modifier.fillMaxWidth().testTag("EditCourseCode textField"),
                enabled = isEnabled)

            // Course Name
            NoteDataTextField(
                value = courseName,
                onValueChange = { onCourseNameChange(Course.formatCourseName(it)) },
                label = "Course Name",
                placeholder = "Set the course name for the note",
                modifier = Modifier.fillMaxWidth().testTag("EditCourseName textField"),
                enabled = isEnabled)

            // Course Year
            NoteDataTextField(
                value = courseYear?.toString() ?: "",
                onValueChange = { onCourseYearChange(it) },
                label = "Course Year",
                placeholder = "Set the course year for the note",
                modifier = Modifier.fillMaxWidth().testTag("EditCourseYear textField"),
                enabled = isEnabled)
          }
        }
  }
}

/**
 * Displays a screen indicating that the user was not found.
 *
 * @param errorText The error message to display.
 */
@Composable
fun ErrorScreen(errorText: String) {
  Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        Text(errorText)
      }
  Log.e("EditNoteScreen", errorText)
}

/**
 * Displays a button that saves the updated note. The button is enabled only if the note title is
 * not empty. When clicked, the button updates the note in the ViewModel and navigates back to the
 * overview screen.
 *
 * @param noteTitle The updated title of the note.
 * @param note The note to be updated.
 * @param visibility The updated visibility of the note.
 * @param courseCode The updated course code of the note.
 * @param courseName The updated course name of the note.
 * @param courseYear The updated course year of the note.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@Composable
fun SaveButton(
    noteTitle: String,
    note: Note,
    visibility: Visibility?,
    courseCode: String,
    courseName: String,
    courseYear: Int?,
    noteViewModel: NoteViewModel
) {
  val context = LocalContext.current
  IconButton(
      enabled = noteTitle.isNotEmpty(),
      onClick = {
        val course = Course(courseCode, courseName, courseYear, "")
        noteViewModel.updateNote(
            Note(
                id = note.id,
                title = noteTitle,
                date = note.date, // Use current timestamp
                lastModified = Timestamp.now(),
                visibility = visibility ?: Visibility.DEFAULT,
                noteCourse = if (course == Course.EMPTY) null else course,
                userId = note.userId,
                folderId = note.folderId,
                comments = note.comments))
        noteViewModel.getNoteById(note.id)
        Toast.makeText(context, "Note saved", Toast.LENGTH_SHORT).show()
      },
      modifier = Modifier.testTag("saveNoteButton")) {
        Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Save Note",
            tint = MaterialTheme.colorScheme.onSurface)
      }
}

/**
 * Displays a button that deletes the note. When clicked, the button deletes the note from the
 * ViewModel and navigates back to the overview screen.
 *
 * @param currentUser The current user.
 * @param note The note to be deleted.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@Composable
fun DeleteButton(
    currentUser: User?,
    note: Note?,
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel
) {
  if (currentUser != null && note != null) {
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    FloatingActionButton(
        modifier = Modifier.testTag("deleteNoteButton"),
        onClick = {
          // Show confirmation dialog when delete button is clicked
          showDeleteConfirmation = true
        },
        containerColor = MaterialTheme.colorScheme.error,
        contentColor = MaterialTheme.colorScheme.onError,
    ) {
      Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete Note")
    }

    // Confirmation dialog for deletion
    if (showDeleteConfirmation) {
      ConfirmationPopup(
          title = stringResource(R.string.delete_note),
          text = stringResource(R.string.delete_note_text),
          onConfirm = {
            noteViewModel.deleteNoteById(note.id, note.userId)
            noteViewModel.selectedNote(null)
            navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
          },
          onDismiss = {
            // Close the dialog without deleting
            showDeleteConfirmation = false
          })
    }
  }
}
