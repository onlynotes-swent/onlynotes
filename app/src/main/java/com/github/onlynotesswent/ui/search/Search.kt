package com.github.onlynotesswent.ui.search

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.NoteItem

/**
 * Displays the search screen where users can search notes by title.
 *
 * @param navigationActions The navigation view model used to transition between different screens.
 * @param noteViewModel The ViewModel that provides the list of publicNotes to search from.
 */
@Composable
fun SearchScreen(navigationActions: NavigationActions, noteViewModel: NoteViewModel) {
  var searchQuery by remember { mutableStateOf(TextFieldValue("")) }
  val notes = noteViewModel.publicNotes.collectAsState()

  val filteredNotes = notes.value.filter { it.title.contains(searchQuery.text, ignoreCase = true) }

  Scaffold(
      modifier = Modifier.testTag("searchScreen"),
      topBar = {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = {
              searchQuery = it
              noteViewModel.getPublicNotes()
            },
            placeholder = { Text("Search ...") },
            leadingIcon = {
              Icon(
                  imageVector = Icons.Default.Search,
                  contentDescription = "Search Icon",
                  tint = Color.Gray)
            },
            shape = RoundedCornerShape(50),
            singleLine = true,
            modifier = Modifier.fillMaxWidth().padding(16.dp).testTag("searchTextField"))
      },
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      }) { padding ->
        if (searchQuery.text.isNotEmpty()) {
          if (filteredNotes.isNotEmpty()) {
            LazyColumn(
                contentPadding = PaddingValues(vertical = 40.dp),
                modifier =
                    Modifier.fillMaxWidth()
                        .padding(horizontal = 16.dp)
                        .padding(padding)
                        .testTag("filteredNoteList")) {
                  items(filteredNotes.size) { index ->
                    NoteItem(note = filteredNotes[index]) {
                      noteViewModel.selectedNote(filteredNotes[index])
                      navigationActions.navigateTo(Screen.EDIT_NOTE)
                    }
                  }
                }
          } else {
            Text(
                modifier =
                    Modifier.padding(padding)
                        .fillMaxWidth()
                        .padding(24.dp)
                        .testTag("noSearchResults"),
                text = "No notes found matching your search.",
                textAlign = TextAlign.Center)
          }
        }
      }
}
