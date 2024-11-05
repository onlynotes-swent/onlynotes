package com.github.onlynotesswent.ui.overview

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.outlined.Clear
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations

/**
 * Displays the add folder screen, where users can create a new folder. The screen
 * allows users to enter a name for the folder and save it.
 *
 * @param navigationActions The navigation actions.
 * @param folderViewModel The view model for folders.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFolderScreen(
    navigationActions: NavigationActions,
    folderViewModel: FolderViewModel,
    userViewModel: UserViewModel
) {

    val parentFolderId = folderViewModel.parentFolderId.collectAsState()

    var name by remember { mutableStateOf("") }

    Scaffold(
        modifier = Modifier.testTag("addFolderScreen"),
        topBar = {
            TopAppBar(
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text("Create a new folder", Modifier.testTag("addFolderTitle"))
                        Spacer(modifier = Modifier.weight(2f))
                    }
                },
                navigationIcon = {
                    IconButton(
                        onClick = { navigationActions.navigateTo(TopLevelDestinations.OVERVIEW) },
                        modifier = Modifier.testTag("clearButton")
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            contentDescription = "Back"
                        )
                    }
                }
            )
        },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp)
                    .padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Folder Name") },
                    placeholder = { Text("Enter the Folder Name") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("inputFolderName"),
                    trailingIcon = {
                        IconButton(onClick = { name = "" }) {
                            Icon(Icons.Outlined.Clear, contentDescription = "Clear title")
                        }
                    })

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        folderViewModel.addFolder(
                            Folder(
                                id = folderViewModel.getNewFolderId(),
                                name = name,
                                userId = userViewModel.currentUser.value!!.uid,
                                parentFolderId = parentFolderId.value
                            ),
                            userViewModel.currentUser.value!!.uid
                        )
                        if (parentFolderId.value != null) {
                            navigationActions.navigateTo(Screen.FOLDER_CONTENTS)
                        } else {
                            navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
                        }
                    },
                    enabled = name.isNotEmpty(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("createFolderButton")
                )
                {
                    Text("Create Folder")
                }
            }
        })
}
