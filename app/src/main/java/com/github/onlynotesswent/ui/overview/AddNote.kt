package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
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
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.utils.NoteDataTextField
import com.github.onlynotesswent.utils.OptionDropDownMenu
import com.github.onlynotesswent.utils.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.Course
import com.github.onlynotesswent.utils.Scanner
import com.github.onlynotesswent.utils.Visibility
import com.google.firebase.Timestamp
import java.util.Calendar

/**
 * Displays the add note screen, where users can create a new note. The screen includes text fields
 * for entering the note's title, course name, course code, and course year, as well as dropdown
 * menus for selecting the note's visibility and template. The screen also includes a button to
 * create the note, which navigates to the edit note screen if the template is "Create Note", or
 * navigates back to the overview screen if the template is "Upload Note" or "Scan Note".
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param scanner The scanner used to scan images and create notes.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@Composable
fun AddNoteScreen(
    navigationActions: NavigationActions,
    scanner: Scanner,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {
  val templateInitialText = "Choose mode"
  val folderId = noteViewModel.currentFolderId.collectAsState()
  val currentYear = Calendar.getInstance().get(Calendar.YEAR)
  var title by remember { mutableStateOf("") }
  var courseName by remember { mutableStateOf("") }
  var courseCode by remember { mutableStateOf("") }
  var courseYear by remember { mutableIntStateOf(currentYear) }
  var template by remember { mutableStateOf(templateInitialText) }
  var visibility: Visibility? by remember { mutableStateOf(null) }
  var expandedVisibility by remember { mutableStateOf(false) }
  var expandedTemplate by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("addNoteScreen"),
      topBar = {
        ScreenTopBar(
            title = "Create a new note",
            titleTestTag = "addNoteTitle",
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
                    .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Spacer(modifier = Modifier.height(30.dp))

              NoteDataTextField(
                  value = title,
                  onValueChange = { title = it },
                  label = "Title",
                  placeholder = "Add a note title",
                  modifier = Modifier.fillMaxWidth().testTag("inputNoteTitle"),
                  trailingIcon = {
                    IconButton(onClick = { title = "" }) {
                      Icon(Icons.Outlined.Clear, contentDescription = "Clear title")
                    }
                  })

              Spacer(modifier = Modifier.height(10.dp))

              OptionDropDownMenu(
                  value = visibility?.toReadableString() ?: "Choose visibility",
                  expanded = expandedVisibility,
                  buttonTag = "visibilityButton",
                  menuTag = "visibilityMenu",
                  onExpandedChange = { expandedVisibility = it },
                  items = Visibility.READABLE_STRINGS,
                  onItemClick = { visibility = Visibility.fromReadableString(it) })

              Spacer(modifier = Modifier.height(10.dp))

              NoteDataTextField(
                  value = courseName,
                  onValueChange = { courseName = Course.formatCourseName(it) },
                  label = "Course Name",
                  placeholder = "Set the course name for the note",
                  modifier = Modifier.fillMaxWidth().testTag("CourseNameTextField"))

              Spacer(modifier = Modifier.height(5.dp))

              NoteDataTextField(
                  value = courseCode,
                  onValueChange = { courseCode = Course.formatCourseCode(it) },
                  label = "Course Code",
                  placeholder = "Set the course code for the note",
                  modifier = Modifier.fillMaxWidth().testTag("CourseCodeTextField"))

              Spacer(modifier = Modifier.height(5.dp))

              NoteDataTextField(
                  value = courseYear.toString(),
                  onValueChange = { courseYear = it.toIntOrNull() ?: currentYear },
                  label = "Course Year",
                  placeholder = "Set the course year for the note",
                  modifier = Modifier.fillMaxWidth().testTag("CourseYearTextField"))

              Spacer(modifier = Modifier.height(10.dp))

              val scanNoteText = "Scan note"
              val createNoteText = "Create note"
              OptionDropDownMenu(
                  value = template,
                  expanded = expandedTemplate,
                  buttonTag = "templateButton",
                  menuTag = "templateMenu",
                  onExpandedChange = { expandedTemplate = it },
                  items = listOf(scanNoteText, createNoteText),
                  onItemClick = { template = it })

              Spacer(modifier = Modifier.height(70.dp))

              CreateNoteButton(
                  title = title,
                  visibility = visibility,
                  template = template,
                  templateInitialText = templateInitialText,
                  scanNoteText = scanNoteText,
                  createNoteText = createNoteText,
                  folderId = folderId.value,
                  currentUser = userViewModel.currentUser.collectAsState(),
                  noteViewModel = noteViewModel,
                  navigationActions = navigationActions,
                  scanner = scanner,
                  courseCode = courseCode,
                  courseName = courseName,
                  courseYear = courseYear)
            }
      })
}

/**
 * Displays a button that creates a new note when clicked. The button is enabled when the title is
 * not empty, the visibility is selected, and the template is not the initial text.
 *
 * @param title The title of the note.
 * @param visibility The visibility of the note.
 * @param template The template of the note.
 * @param templateInitialText The initial text of the template dropdown menu.
 * @param scanNoteText The text for the "Scan Note" template.
 * @param createNoteText The text for the "Create Note" template.
 * @param folderId The ID of the folder containing the note.
 * @param currentUser The current user.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param scanner The scanner used to scan images and create notes.
 * @param courseCode The code of the course for which the note is created.
 * @param courseName The name of the course for which the note is created.
 * @param courseYear The year of the course for which the note is created.
 */
@Composable
fun CreateNoteButton(
    title: String,
    visibility: Visibility?,
    template: String,
    templateInitialText: String,
    scanNoteText: String,
    createNoteText: String,
    folderId: String?,
    currentUser: State<User?>,
    noteViewModel: NoteViewModel,
    navigationActions: NavigationActions,
    scanner: Scanner,
    courseCode: String,
    courseName: String,
    courseYear: Int
) {
  Button(
      onClick = {
        if (template == scanNoteText) {
          scanner.scan()
        }
        val note =
            Note(
                id = noteViewModel.getNewUid(),
                title = title,
                content = "",
                date = Timestamp.now(),
                visibility = visibility!!,
                noteCourse = Course(courseCode, courseName, courseYear, "path"),
                userId = currentUser.value!!.uid,
                image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                folderId = folderId)
        noteViewModel.addNote(note, currentUser.value!!.uid)

        if (template == createNoteText) {
          noteViewModel.selectedNote(note)
          navigationActions.navigateTo(Screen.EDIT_NOTE)
        } else {
          navigationActions.goBack()
        }
      },
      colors =
          ButtonDefaults.buttonColors(
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary),
      enabled = title.isNotEmpty() && visibility != null && template != templateInitialText,
      modifier = Modifier.fillMaxWidth().testTag("createNoteButton")) {
        Text(text = template)
      }
}
