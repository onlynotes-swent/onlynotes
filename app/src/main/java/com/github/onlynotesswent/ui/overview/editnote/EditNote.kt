package com.github.onlynotesswent.ui.overview.editnote

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.common.Course
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.common.DeletePopup
import com.github.onlynotesswent.ui.common.NoteDataTextField
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
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
  var courseYear by remember { mutableIntStateOf(note?.noteCourse?.courseYear ?: 0) }
  var visibility by remember { mutableStateOf(note?.visibility ?: Visibility.DEFAULT) }

  Scaffold(
      floatingActionButton = { DeleteButton(currentUser, note, navigationActions, noteViewModel) },
      modifier = Modifier.testTag("editNoteScreen"),
      topBar = {
        EditNoteTopBar(
            title = "Edit Note",
            titleTestTag = "editNoteTitle",
            noteViewModel = noteViewModel,
            navigationActions = navigationActions,
            actions = {
              if (note != null && currentUser != null) {
                SaveButton(
                    noteTitle = noteTitle,
                    note = note!!,
                    visibility = visibility,
                    courseCode = courseCode,
                    courseName = courseName,
                    courseYear = courseYear,
                    currentUser = currentUser!!,
                    navigationActions = navigationActions,
                    noteViewModel = noteViewModel)
              }
            })
      },
      bottomBar = {
        EditNoteNavigationMenu(
            navigationActions = navigationActions, selectedItem = Screen.EDIT_NOTE)
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
                NoteSection(
                    noteTitle = noteTitle,
                    onNoteTitleChange = { noteTitle = it },
                    courseName = courseName,
                    onCourseNameChange = { courseName = it },
                    courseCode = courseCode,
                    onCourseCodeChange = { courseCode = it },
                    courseYear = courseYear,
                    onCourseYearChange = { courseYear = it },
                    visibility = visibility,
                    onVisibilityChange = { visibility = it })
              }
        }
      }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NoteSection(
    noteTitle: String,
    onNoteTitleChange: (String) -> Unit,
    courseName: String,
    onCourseNameChange: (String) -> Unit,
    courseCode: String,
    onCourseCodeChange: (String) -> Unit,
    courseYear: Int,
    onCourseYearChange: (Int) -> Unit,
    visibility: Visibility,
    onVisibilityChange: (Visibility) -> Unit
) {
  val currentYear = Calendar.getInstance().get(Calendar.YEAR)
  var expandedVisibility by remember { mutableStateOf(false) }

  NoteDataTextField(
      value = noteTitle,
      onValueChange = onNoteTitleChange,
      label = "Note Title",
      placeholder = "Enter the new title here",
      modifier = Modifier.fillMaxWidth().testTag("EditTitle textField"),
      trailingIcon = {
        IconButton(onClick = { onNoteTitleChange("") }) {
          Icon(Icons.Outlined.Clear, contentDescription = "Clear Title")
        }
      })

  ExposedDropdownMenuBox(
      modifier = Modifier.fillMaxWidth().testTag("VisibilityEditMenu"),
      expanded = expandedVisibility,
      onExpandedChange = { expandedVisibility = it },
  ) {
    OutlinedTextField(
        value = visibility.toReadableString(),
        onValueChange = {},
        readOnly = true,
        modifier =
            Modifier.menuAnchor(
                    type =
                        MenuAnchorType.PrimaryEditable, // Ensures proper alignment for text fields
                    enabled = true // Enables the anchor functionality
                    )
                .fillMaxWidth(),
        label = { Text("Visibility") },
        trailingIcon = {
          IconButton(onClick = { expandedVisibility = !expandedVisibility }) {
            Icon(
                imageVector =
                    if (expandedVisibility) Icons.Default.ArrowDropUp
                    else Icons.Default.ArrowDropDown,
                contentDescription = "Toggle Visibility Dropdown")
          }
        },
        colors =
            TextFieldDefaults.colors(
                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
                focusedContainerColor = MaterialTheme.colorScheme.background,
                unfocusedContainerColor = MaterialTheme.colorScheme.background))
    ExposedDropdownMenu(
        expanded = expandedVisibility, onDismissRequest = { expandedVisibility = false }) {
          Visibility.entries.forEach { visibilityOption ->
            DropdownMenuItem(
                onClick = {
                  onVisibilityChange(visibilityOption)
                  expandedVisibility = false
                },
                text = { Text(visibilityOption.toReadableString()) })
          }
        }
  }

  NoteDataTextField(
      value = courseName,
      onValueChange = onCourseNameChange,
      label = "Course Name",
      placeholder = "Set the course name for the note",
      modifier = Modifier.fillMaxWidth().testTag("EditCourseName textField"))

  NoteDataTextField(
      value = courseCode,
      onValueChange = onCourseCodeChange,
      label = "Course Code",
      placeholder = "Set the course code for the note",
      modifier = Modifier.fillMaxWidth().testTag("EditCourseCode textField"))

  NoteDataTextField(
      value = courseYear.toString(),
      onValueChange = { onCourseYearChange(it.toIntOrNull() ?: currentYear) },
      label = "Course Year",
      placeholder = "Set the course year for the note",
      modifier = Modifier.fillMaxWidth().testTag("EditCourseYear textField"))
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
 * @param currentUser The current user.
 * @param navigationActions The navigation view model used to transition between different screens.
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
    courseYear: Int,
    currentUser: User,
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel
) {
  IconButton(
      enabled = noteTitle.isNotEmpty(),
      onClick = {
        noteViewModel.updateNote(
            Note(
                id = note.id,
                title = noteTitle,
                date = Timestamp.now(), // Use current timestamp
                visibility = visibility ?: Visibility.DEFAULT,
                noteCourse = Course(courseCode, courseName, courseYear, "path"),
                userId = note.userId,
                folderId = note.folderId,
                comments = note.comments),
            currentUser.uid)
        if (note.folderId != null) {
          noteViewModel.selectedNote(null)
          navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
        } else {
          noteViewModel.selectedNote(null)
          navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
        }
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
      DeletePopup(
          title = "Delete Note?",
          text = "Are you sure you want to delete this note? This action cannot be undone.",
          onConfirm = {
            noteViewModel.deleteNoteById(note.id, note.userId)
            noteViewModel.selectedNote(null)
            navigationActions.navigateTo(Screen.OVERVIEW)
          },
          onDismiss = {
            // Close the dialog without deleting
            showDeleteConfirmation = false
          })
    }
  }
}
