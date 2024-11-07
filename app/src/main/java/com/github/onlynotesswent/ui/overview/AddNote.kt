package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
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
import com.github.onlynotesswent.model.scanner.Scanner
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import java.util.Calendar

/**
 * Displays the add note screen, where users can create a new note. The screen includes text fields
 * for entering the note's title, class name, class code, and class year, as well as dropdown menus
 * for selecting the note's visibility and template. The screen also includes a button to create the
 * note, which navigates to the edit note screen if the template is "Create Note", or navigates back
 * to the overview screen if the template is "Upload Note" or "Scan Note".
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param scanner The scanner used to scan images and create notes.
 * @param noteViewModel The ViewModel that provides the current note to be edited and handles note
 *   updates.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    navigationActions: NavigationActions,
    scanner: Scanner,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {

  val folderId = noteViewModel.folderId.collectAsState()
  val currentYear = Calendar.getInstance().get(Calendar.YEAR)

  var title by remember { mutableStateOf("") }
  var className by remember { mutableStateOf("") }
  var classCode by remember { mutableStateOf("") }
  var classYear by remember { mutableIntStateOf(currentYear) }
  var template by remember { mutableStateOf("Choose An Option") }
  var visibility: Note.Visibility? by remember { mutableStateOf(null) }
  var expandedVisibility by remember { mutableStateOf(false) }
  var expandedTemplate by remember { mutableStateOf(false) }

  Scaffold(
      modifier = Modifier.testTag("addNoteScreen"),
      topBar = {
        TopAppBar(
            colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFFB3E5FC)),
            title = {
              Row(
                  modifier = Modifier.fillMaxWidth(),
                  verticalAlignment = Alignment.CenterVertically) {
                    Spacer(modifier = Modifier.weight(1f))
                    Text("Create a new note", Modifier.testTag("addNoteTitle"))
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
            modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)) {
              Spacer(modifier = Modifier.height(30.dp))

              OutlinedTextField(
                  value = title,
                  onValueChange = { title = it },
                  label = { Text("Title") },
                  placeholder = { Text("Add a Note Title") },
                  modifier = Modifier.fillMaxWidth().testTag("inputNoteTitle"),
                  trailingIcon = {
                    IconButton(onClick = { title = "" }) {
                      Icon(Icons.Outlined.Clear, contentDescription = "Clear title")
                    }
                  })

              Spacer(modifier = Modifier.height(10.dp))

              OptionDropDownMenu(
                  value = visibility?.toReadableString() ?: "Choose an option",
                  expanded = expandedVisibility,
                  buttonTag = "visibilityButton",
                  menuTag = "visibilityMenu",
                  onExpandedChange = { expandedVisibility = it },
                  items = Note.Visibility.READABLE_STRINGS,
                  onItemClick = { visibility = Note.Visibility.fromReadableString(it) })

              Spacer(modifier = Modifier.height(10.dp))

              OutlinedTextField(
                  value = className,
                  onValueChange = { className = it },
                  label = { Text("Class Name") },
                  placeholder = { Text("Set the Class Name for the Note") },
                  modifier = Modifier.fillMaxWidth().testTag("ClassNameTextField"))

              Spacer(modifier = Modifier.height(5.dp))

              OutlinedTextField(
                  value = classCode,
                  onValueChange = { classCode = it },
                  label = { Text("Class Code") },
                  placeholder = { Text("Set the Class Code for the Note") },
                  modifier = Modifier.fillMaxWidth().testTag("ClassCodeTextField"))

              Spacer(modifier = Modifier.height(5.dp))

              OutlinedTextField(
                  value = classYear.toString(),
                  onValueChange = { classYear = it.toIntOrNull() ?: currentYear },
                  label = { Text("Class Year") },
                  placeholder = { Text("Set the Class Year for the Note") },
                  modifier = Modifier.fillMaxWidth().testTag("ClassYearTextField"))

              Spacer(modifier = Modifier.height(10.dp))

              val scanNoteText = "Scan Note"
              val createNoteText = "Create Note"
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
                    if (template == scanNoteText) {
                      // call scan image API or functions. Once scanned, add the note to database
                      scanner.scan()
                    }
                    // provisional note, we will have to change this later
                    val note =
                        Note(
                            id = noteViewModel.getNewUid(),
                            title = title,
                            content = "",
                            date = Timestamp.now(),
                            visibility = visibility!!,
                            noteClass = Note.Class(classCode, className, classYear, "path"),
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
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50)),
                  enabled =
                      title.isNotEmpty() && visibility != null && template != "Choose An Option",
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
) {
  Box(modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = { onExpandedChange(!expanded) },
        modifier = Modifier.fillMaxWidth().testTag(buttonTag)) {
          Text(text = value)
          Icon(Icons.Outlined.ArrowDropDown, "Dropdown icon")
        }

    DropdownMenu(
        expanded = expanded,
        onDismissRequest = { onExpandedChange(false) },
        modifier = modifier.fillMaxWidth().testTag(menuTag)) {
          items.forEach { item ->
            DropdownMenuItem(
                text = { Text(item) },
                onClick = {
                  onItemClick(item)
                  onExpandedChange(false)
                })
          }
        }
  }
}
