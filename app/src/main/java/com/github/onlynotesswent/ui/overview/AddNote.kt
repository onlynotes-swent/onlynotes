package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserViewModel
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
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
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

              Button(
                  onClick = {
                    val noteUid = noteViewModel.getNewUid()
                    if (template == scanNoteText) {
                      // call scan image API or functions. Once scanned, add the note to database
                      scanner.scan { fileViewModel.uploadFile(noteUid, it, FileType.NOTE_PDF) }
                    }
                    // provisional note, we will have to change this later
                    val note =
                        Note(
                            id = noteUid,
                            title = title,
                            content = "",
                            date = Timestamp.now(),
                            visibility = visibility!!,
                            noteCourse = Course(courseCode, courseName, courseYear, "path"),
                            userId = userViewModel.currentUser.value!!.uid,
                            image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888),
                            folderId = folderId.value)
                    // create the note and add it to database
                    noteViewModel.addNote(note, userViewModel.currentUser.value!!.uid)

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
                  enabled =
                      title.isNotEmpty() && visibility != null && template != templateInitialText,
                  modifier = Modifier.fillMaxWidth().testTag("createNoteButton")) {
                    Text(text = template)
                  }
            }
      })
}

/**
 * A composable function that displays an `OutlinedTextField` with a dropdown menu.
 *
 * @param value The current value of the text field.
 * @param expanded A boolean indicating whether the dropdown menu is expanded.
 * @param onExpandedChange A callback to be invoked when the expanded state of the dropdown menu
 *   changes.
 * @param items A list of strings representing the items to be displayed in the dropdown menu.
 * @param onItemClick A callback to be invoked when an item in the dropdown menu is clicked.
 * @param modifier The modifier to be applied to the `OutlinedTextField`.
 */
@Composable
fun OptionDropDownMenu(
    expanded: Boolean,
    value: String,
    buttonTag: String,
    menuTag: String,
    onExpandedChange: (Boolean) -> Unit,
    items: List<String>,
    onItemClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    widthFactor: Float = 0.8f
) {
  BoxWithConstraints(modifier = Modifier.fillMaxWidth(widthFactor)) {
    Button(
        onClick = { onExpandedChange(!expanded) },
        modifier = Modifier.width(maxWidth).testTag(buttonTag)) {
          Text(text = value)
          Icon(Icons.Outlined.ArrowDropDown, "Dropdown icon")
        }
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        modifier = modifier.width(maxWidth).testTag(menuTag)) {
          items.forEach { item ->
            DropdownMenuItem(
                modifier = Modifier.testTag("item--$item"),
                text = { Text(item) },
                onClick = {
                  onItemClick(item)
                  onExpandedChange(false)
                })
          }
        }
  }
}

/**
 * A composable function that displays an `OutlinedTextField` with an optional trailing icon.
 *
 * @param value The current value of the text field.
 * @param onValueChange The callback that is triggered when the value changes.
 * @param label The label to be displayed inside the text field container.
 * @param placeholder The placeholder to be displayed when the text field is in focus and the input
 *   text is empty.
 * @param modifier The modifier to be applied to the `OutlinedTextField`.
 * @param trailingIcon An optional trailing icon displayed at the end of the text field container.
 */
@Composable
fun NoteDataTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    placeholder: String,
    modifier: Modifier = Modifier,
    trailingIcon: @Composable (() -> Unit)? = null
) {
  OutlinedTextField(
      value = value,
      onValueChange = onValueChange,
      label = { Text(label) },
      placeholder = { Text(placeholder) },
      modifier = modifier,
      trailingIcon = trailingIcon,
      colors =
          TextFieldDefaults.colors(
              focusedIndicatorColor = MaterialTheme.colorScheme.primary,
              unfocusedIndicatorColor = MaterialTheme.colorScheme.onBackground,
              focusedContainerColor = MaterialTheme.colorScheme.background,
              unfocusedContainerColor = MaterialTheme.colorScheme.background))
}

/**
 * A composable function that displays the top app bar for the screen. It is composed of an icon
 * button and a title.
 *
 * @param title The title to be displayed in the top app bar.
 * @param titleTestTag The test tag for the title.
 * @param onBackClick The callback to be invoked when the back button is clicked.
 * @param icon The icon to be displayed in the top app bar.
 * @param iconTestTag The test tag for the icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    title: String,
    titleTestTag: String,
    onBackClick: () -> Unit,
    icon: @Composable () -> Unit,
    iconTestTag: String
) {
  TopAppBar(
      colors =
          TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
      title = {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Spacer(modifier = Modifier.weight(1f))
          Text(
              title,
              color = MaterialTheme.colorScheme.onSurface,
              modifier = Modifier.testTag(titleTestTag))
          Spacer(modifier = Modifier.weight(2f))
        }
      },
      navigationIcon = {
        IconButton(onClick = onBackClick, Modifier.testTag(iconTestTag), content = icon)
      })
}
