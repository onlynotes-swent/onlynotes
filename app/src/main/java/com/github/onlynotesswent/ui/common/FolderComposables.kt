package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.common.Visibility
import com.github.onlynotesswent.model.folder.Folder

/**
 * Displays a single folder item in a card format. The card contains the folder's name. When
 * clicked, it triggers the provided onClick action, which can be used for navigation or other
 * interactions.
 *
 * @param folder The folder data that will be displayed in this card.
 * @param onClick The lambda function to be invoked when the folder card is clicked.
 */
@Composable
fun FolderItem(folder: Folder, onClick: () -> Unit) {
  Card(
      modifier =
          Modifier.testTag("folderCard").padding(vertical = 4.dp).clickable(onClick = onClick),
      colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.background)) {
        Column(
            modifier = Modifier.padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
              Image(
                  painter = painterResource(id = R.drawable.folder_icon),
                  contentDescription = "Folder Icon",
                  modifier = Modifier.size(80.dp))

              Text(
                  modifier = Modifier.align(Alignment.CenterHorizontally),
                  text = folder.name,
                  style = MaterialTheme.typography.bodyMedium,
                  fontWeight = FontWeight.Bold,
                  color = MaterialTheme.colorScheme.onBackground)
            }
      }
}

/**
 * Dialog that allows the user to create or rename a folder.
 *
 * @param onDismiss callback to be invoked when the dialog is dismissed
 * @param onConfirm callback to be invoked when the user confirms the new name
 * @param action the action to be performed (create or rename)
 * @param oldVis the old visibility of the folder (if renaming)
 * @param oldName the old name of the folder (if renaming)
 */
@Composable
fun FolderDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Visibility) -> Unit,
    action: String,
    oldVis: Visibility? = null,
    oldName: String = ""
) {

  var name by remember { mutableStateOf(oldName) }
  var visibility: Visibility? by remember { mutableStateOf(oldVis) }
  var expandedVisibility by remember { mutableStateOf(false) }

  Dialog(onDismissRequest = onDismiss) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
      Column(
          modifier = Modifier.padding(16.dp).testTag("folderDialog"),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally) {
            Row(
                modifier = Modifier.fillMaxWidth(0.92f),
                horizontalArrangement = Arrangement.Start) {
                  Text("$action Folder", style = MaterialTheme.typography.titleLarge)
                }

            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Folder Name") },
                modifier = Modifier.testTag("inputFolderName"))

            OptionDropDownMenu(
                value = visibility?.toReadableString() ?: "Choose visibility",
                expanded = expandedVisibility,
                buttonTag = "visibilityButton",
                menuTag = "visibilityMenu",
                onExpandedChange = { expandedVisibility = it },
                items = Visibility.READABLE_STRINGS,
                onItemClick = { visibility = Visibility.fromReadableString(it) },
                modifier = Modifier.testTag("visibilityDropDown"),
                widthFactor = 0.94f)

            Row(modifier = Modifier.fillMaxWidth(0.92f), horizontalArrangement = Arrangement.End) {
              Button(onClick = onDismiss, modifier = Modifier.testTag("dismissFolderAction")) {
                Text("Cancel")
              }
              Button(
                  enabled = name.isNotEmpty() && visibility != null,
                  onClick = { onConfirm(name, visibility ?: Visibility.DEFAULT) },
                  modifier = Modifier.testTag("confirmFolderAction")) {
                    Text(action)
                  }
            }
          }
    }
  }
}