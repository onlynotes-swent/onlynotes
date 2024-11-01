package com.github.onlynotesswent.ui.overview

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.ArrowDropDown
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.scanner.Scanner
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddNoteScreen(
    navigationActions: NavigationActions,
    scanner: Scanner,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel
) {

  var title by remember { mutableStateOf("") }
  var template by remember { mutableStateOf("Choose An Option") }
  var visibility: Note.Visibility? by remember { mutableStateOf(null) }
  var expandedVisibility by remember { mutableStateOf(false) }
  var expandedTemplate by remember { mutableStateOf(false) }
  var saveButton by remember { mutableStateOf("Create Note") }

  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("addNoteScreen"),
      topBar = {
        TopAppBar(
            title = { Text("Create a new note", Modifier.testTag("addNoteTitle")) },
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
              Image(
                  painter = painterResource(id = R.drawable.add_note),
                  contentDescription = "Create Note Image",
                  modifier = Modifier.size(200.dp).testTag("addNoteImage"))

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

              Spacer(modifier = Modifier.height(30.dp))

              OptionDropDownMenu(
                  value = visibility?.toReadableString() ?: "Choose an option",
                  expanded = expandedVisibility,
                  buttonTag = "visibilityButton",
                  menuTag = "visibilityMenu",
                  onExpandedChange = { expandedVisibility = it },
                  items = Note.Visibility.READABLE_STRINGS,
                  onItemClick = { visibility = Note.Visibility.fromReadableString(it) })

              Spacer(modifier = Modifier.height(30.dp))

              OptionDropDownMenu(
                  value = template,
                  expanded = expandedTemplate,
                  buttonTag = "templateButton",
                  menuTag = "templateMenu",
                  onExpandedChange = { expandedTemplate = it },
                  items = listOf("Scan Image", "Create Note From Scratch"),
                  onItemClick = {
                    template = it
                    saveButton =
                        if (template == "Scan Image") {
                          "Take Picture"
                        } else {
                          "Create Note"
                        }
                  })

              Spacer(modifier = Modifier.height(80.dp))

              Button(
                  onClick = {
                    var type = Note.Type.NORMAL_TEXT
                    if (saveButton == "Take Picture") {
                      // call scan image API or functions. Once scanned, add the note to database
                      scanner.scan()
                      type = Note.Type.PDF
                    } else if (saveButton == "Create Note") {
                      type = Note.Type.NORMAL_TEXT
                    }

                    // create the note and add it to database
                    noteViewModel.addNote(
                        // provisional note, we will have to change this later
                        Note(
                            id = noteViewModel.getNewUid(),
                            type = type,
                            title = title,
                            content = "",
                            date = Timestamp.now(),
                            visibility = visibility!!,
                            userId = userViewModel.currentUser.value!!.uid,
                            image = Bitmap.createBitmap(1, 1, Bitmap.Config.ARGB_8888)),
                        userViewModel.currentUser.value!!.uid)
                    navigationActions.goBack()
                  },
                  enabled =
                      title.isNotEmpty() && visibility != null && template != "Choose An Option",
                  modifier = Modifier.fillMaxWidth().testTag("createNoteButton")) {
                    Text(text = saveButton)
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
