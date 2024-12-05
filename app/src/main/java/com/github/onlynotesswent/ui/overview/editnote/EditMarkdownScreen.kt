package com.github.onlynotesswent.ui.overview.editnote

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.mohamedrejeb.richeditor.model.RichTextState
import com.mohamedrejeb.richeditor.model.rememberRichTextState
import com.mohamedrejeb.richeditor.ui.material3.RichTextEditor
import java.io.File
import java.io.IOException
import kotlinx.coroutines.delay

/**
 * A screen to edit markdown content for a selected note.
 *
 * This screen allows users to edit markdown text with rich text formatting options (bold, italic,
 * underline, and strikethrough), using a rich text editor. It displays existing markdown content or
 * downloads it if not available. If there is no markdown file associated with the note, a new empty
 * markdown file is created and uploaded, then re-downloaded.
 *
 * @param navigationActions Object to handle navigation actions, including going back to the
 *   previous screen.
 * @param noteViewModel ViewModel to manage the note's state and interactions.
 * @param fileViewModel ViewModel to handle file downloads and uploads for markdown files.
 * @param userViewModel ViewModel to manage the user's state and interactions.
 */
@Composable
fun EditMarkdownScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    fileViewModel: FileViewModel,
    userViewModel: UserViewModel
) {
  val state = rememberRichTextState()
  val context = LocalContext.current
  val selectedNote by noteViewModel.selectedNote.collectAsState()
  val currentUser by userViewModel.currentUser.collectAsState()
  var markdownContent: File? by remember { mutableStateOf(null) }
  var isEditing by rememberSaveable { mutableStateOf(false) } // Add this line

  // Function to download and set the Markdown file
  LaunchedEffect(Unit) {
    fileViewModel.downloadFile(
        uid = selectedNote!!.id,
        fileType = FileType.NOTE_TEXT,
        context = context,
        onSuccess = { downloadedFile: File ->
          markdownContent = downloadedFile
          state.setMarkdown(markdownContent?.readText() ?: "")
        },
        onFileNotFound = {},
        onFailure = { exception ->
          Toast.makeText(context, "Error downloading file: ${exception.message}", Toast.LENGTH_LONG)
              .show()
        })
  }

  @Suppress("kotlin:S6300") // as there is no need to encrypt file
  fun updateMarkdownFile(context: Context, uid: String, fileViewModel: FileViewModel) {
    try {
      if (markdownContent != null) {
        markdownContent!!.writeText(state.toMarkdown())
        val fileUri = Uri.fromFile(markdownContent)
        fileViewModel.updateFile(uid = uid, fileUri = fileUri, fileType = FileType.NOTE_TEXT)
      }
    } catch (e: IOException) {
      Toast.makeText(context, "Error updating file: ${e.message}", Toast.LENGTH_LONG).show()
      Log.e("FileUpdate", "Error updating file: ${e.message}")
    }
  }

  Scaffold(
      topBar = {
        EditNoteTopBar(
            title = stringResource(R.string.content),
            titleTestTag = "contentTitle",
            noteViewModel = noteViewModel,
            userViewModel = userViewModel,
            navigationActions = navigationActions,
            onClick = {
              if (!selectedNote!!.isOwner(currentUser!!.uid)) {
                navigationActions.navigateTo(TopLevelDestinations.SEARCH)
              }
            })
      },
      bottomBar = {
        EditNoteNavigationMenu(navigationActions, selectedItem = Screen.EDIT_NOTE_MARKDOWN)
      },
      floatingActionButton = {
        if (!isEditing && selectedNote!!.isOwner(currentUser!!.uid)) {
          FloatingActionButton(
              modifier = Modifier.testTag("editMarkdownFAB"),
              onClick = {
                isEditing = true // Switch to edit mode
              },
              containerColor = MaterialTheme.colorScheme.primary,
              contentColor = MaterialTheme.colorScheme.onPrimary) {
                Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit")
              }
        }
      }) { paddingValues ->
        if (selectedNote == null) {
          ErrorScreen(stringResource(R.string.no_note_is_selected))
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(16.dp)
                      .padding(paddingValues)
                      .testTag("editMarkdownColumn")
                      .verticalScroll(rememberScrollState()),
          ) {
            RichTextEditor(
                modifier = Modifier.fillMaxWidth().weight(2f).testTag("RichTextEditor"),
                state = state,
                readOnly = !isEditing,
                label = {
                  if (isEditing) {
                    EditorControls(
                        modifier = Modifier.testTag("EditorControl"),
                        state = state,
                        onSaveClick = {
                          updateMarkdownFile(context, selectedNote!!.id, fileViewModel)
                          isEditing = false // Switch back to view mode after saving
                        })
                  }
                })
          }
        }
      }
}

/**
 * A composable component that provides a set of text formatting controls for the rich text editor.
 * It manually controls the state SpanStyle depending on the selected toggles. It also provides a
 * save button to save the current text content and formatting.
 *
 * @param modifier Modifier to be applied to the Row layout containing the controls.
 * @param state RichTextState object to manage the text content and formatting.
 * @param onSaveClick Callback function to save the current text content and formatting.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun EditorControls(modifier: Modifier, state: RichTextState, onSaveClick: () -> Unit) {
  var boldSelected by rememberSaveable { mutableStateOf(false) }
  var italicSelected by rememberSaveable { mutableStateOf(false) }
  var underlineSelected by rememberSaveable { mutableStateOf(false) }
  var strikethroughSelected by rememberSaveable { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    while (true) {
      if (!boldSelected) {
        state.removeSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
      } else {
        state.addSpanStyle(SpanStyle(fontWeight = FontWeight.Bold))
      }

      if (italicSelected) {
        state.addSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
      } else {
        state.removeSpanStyle(SpanStyle(fontStyle = FontStyle.Italic))
      }
      if (underlineSelected) {
        state.addSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
      } else {
        state.removeSpanStyle(SpanStyle(textDecoration = TextDecoration.Underline))
      }
      if (strikethroughSelected) {
        state.addSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
      } else {
        state.removeSpanStyle(SpanStyle(textDecoration = TextDecoration.LineThrough))
      }
      delay(20)
    }
  }

  FlowRow(
      verticalArrangement = Arrangement.Center,
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      modifier = modifier) {
        ControlWrapper(
            modifier = Modifier.testTag("BoldControl"),
            selected = boldSelected,
            onChangeClick = { boldSelected = it }) {
              Icon(imageVector = Icons.Filled.FormatBold, contentDescription = "Bold")
            }
        ControlWrapper(
            modifier = Modifier.testTag("ItalicControl"),
            selected = italicSelected,
            onChangeClick = { italicSelected = it }) {
              Icon(imageVector = Icons.Filled.FormatItalic, contentDescription = "Italic")
            }
        ControlWrapper(
            modifier = Modifier.testTag("UnderlinedControl"),
            selected = underlineSelected,
            onChangeClick = { underlineSelected = it }) {
              Icon(imageVector = Icons.Filled.FormatUnderlined, contentDescription = "Underlined")
            }
        ControlWrapper(
            modifier = Modifier.testTag("StrikethroughControl"),
            selected = strikethroughSelected,
            onChangeClick = { strikethroughSelected = it }) {
              Icon(
                  imageVector = Icons.Filled.FormatStrikethrough,
                  contentDescription = "Strikethrough")
            }

        IconButton(
            modifier =
                Modifier.size(35.dp)
                    .align(Alignment.CenterVertically)
                    .testTag("SaveButton")
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(6.dp),
                    )
                    .padding(0.dp),
            onClick = { onSaveClick() }) {
              Icon(
                  imageVector = Icons.Default.Check,
                  contentDescription = "Save",
                  tint = MaterialTheme.colorScheme.onPrimary)
            }
      }
}

/**
 * A composable component to create a toggleable control button with custom styling.
 *
 * @param modifier Modifier to be applied to the FilterChip for layout and styling adjustments.
 * @param selected Boolean indicating whether the control is in a selected (active) state.
 * @param selectedColor Color used when the control is selected, defaulting to the primary color.
 * @param unselectedColor Color used when the control is not selected, defaulting to the inverse
 *   primary color.
 * @param onChangeClick Callback function to update the `selected` state when the control is
 *   clicked.
 * @param content Composable lambda representing the visual content of the chip, typically an icon.
 */
@Composable
fun ControlWrapper(
    modifier: Modifier,
    selected: Boolean,
    selectedColor: Color = MaterialTheme.colorScheme.primary,
    unselectedColor: Color = MaterialTheme.colorScheme.inversePrimary,
    onChangeClick: (Boolean) -> Unit,
    content: @Composable () -> Unit
) {
  FilterChip(
      modifier =
          modifier.semantics { contentDescription = if (selected) "Selected" else "Unselected" },
      selected = selected,
      onClick = { onChangeClick(!selected) },
      shape = RoundedCornerShape(6.dp),
      colors =
          FilterChipDefaults.filterChipColors(
              containerColor = if (selected) selectedColor else unselectedColor,
              selectedContainerColor = selectedColor,
              disabledContainerColor = unselectedColor),
      border = BorderStroke(1.dp, Color.LightGray),
      leadingIcon = {
        if (selected) {
          Icon(
              modifier = Modifier.size(20.dp).testTag("FilterChipIcon"),
              imageVector = Icons.Default.Check,
              contentDescription = "Selected",
              tint = MaterialTheme.colorScheme.onBackground)
        }
      },
      label = {
        Box(modifier = Modifier.padding(all = 4.dp), contentAlignment = Alignment.Center) {
          content()
        }
      })
}
