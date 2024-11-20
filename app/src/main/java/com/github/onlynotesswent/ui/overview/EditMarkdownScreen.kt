package com.github.onlynotesswent.ui.overview

import android.content.Context
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.FormatStrikethrough
import androidx.compose.material.icons.filled.FormatUnderlined
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
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
 * @param userViewModel ViewModel for user-specific actions, if required.
 * @param fileViewModel ViewModel to handle file downloads and uploads for markdown files.
 */
@Composable
fun EditMarkdownScreen(
    navigationActions: NavigationActions,
    noteViewModel: NoteViewModel,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
) {
  val state = rememberRichTextState()

  val context = LocalContext.current
  val selectedNote by noteViewModel.selectedNote.collectAsState()
  var markdownContent: File? by remember { mutableStateOf(null) }

  // Function to download and set the Markdown file
  LaunchedEffect(Unit) {
    fileViewModel.downloadFile(
        uid = selectedNote!!.id,
        fileType = FileType.NOTE_TEXT,
        context = context,
        onSuccess = { downloadedFile: File ->
          // Update the UI with the downloaded file reference
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

        // Get the file URI
        val fileUri = Uri.fromFile(markdownContent)

        // Upload the file to update it in Firebase Storage
        fileViewModel.updateFile(uid = uid, fileUri = fileUri, fileType = FileType.NOTE_TEXT)
      }
    } catch (e: IOException) {
      Toast.makeText(context, "Error updating file: ${e.message}", Toast.LENGTH_LONG).show()
      Log.e("FileUpdate", "Error updating file: ${e.message}")
    }
  }

  Scaffold(
      topBar = {
        ScreenTopBar(
            titleTestTag = "modifyMDTitle",
            onBackClick = { navigationActions.goBack() },
            title = "Modify Markdown",
            icon = {
              Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
            },
            iconTestTag = "goBackButton")
      }) { paddingValues ->
        if (selectedNote == null) {
          ErrorScreen("No note is selected")
        } else {
          Column(
              modifier =
                  Modifier.fillMaxSize()
                      .padding(16.dp)
                      .padding(paddingValues)
                      .testTag("editMarkdownColumn")
                      .verticalScroll(rememberScrollState()),
          ) {
            EditorControls(modifier = Modifier.testTag("EditorControl"), state = state)
            RichTextEditor(
                modifier = Modifier.fillMaxWidth().weight(2f).testTag("RichTextEditor"),
                state = state,
            )

            Button(
                modifier = Modifier.fillMaxWidth().testTag("Save button"),
                onClick = { updateMarkdownFile(context, selectedNote!!.id, fileViewModel) }) {
                  Text("Save")
                }
          }
        }
      }
}
/**
 * A composable component that provides a set of text formatting controls for the rich text editor.
 * It manually controls the state SpanStyle depending on the selected toggles.
 *
 * @param modifier Modifier to be applied to the Row layout containing the controls.
 * @param state The current state of the rich text editor, used to apply styles.
 */
@Composable
fun EditorControls(modifier: Modifier, state: RichTextState) {
  var boldSelected by rememberSaveable { mutableStateOf(false) }
  var italicSelected by rememberSaveable { mutableStateOf(false) }
  var underlineSelected by rememberSaveable { mutableStateOf(false) }
  var strikethroughSelected by rememberSaveable { mutableStateOf(false) }
  var previousMarkdown by rememberSaveable { mutableStateOf("") }
  // fix as using a state.toggleSpanStyle() seems to be broken now need to manually set the
  // SpanStyle
  LaunchedEffect(Unit) {
    while (true) {
      val currentMarkdown = state.toMarkdown()
      previousMarkdown = currentMarkdown
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

  Row(
      modifier = modifier.fillMaxWidth().padding(all = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
  ) {
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
          Icon(imageVector = Icons.Filled.FormatStrikethrough, contentDescription = "Strikethrough")
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
              contentDescription = if (selected) "Selected" else "Not Selected",
              tint = MaterialTheme.colorScheme.onBackground)
        }
      },
      label = {
        Box(modifier = Modifier.padding(all = 4.dp), contentAlignment = Alignment.Center) {
          content()
        }
      })
}
