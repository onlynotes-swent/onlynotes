package com.github.onlynotesswent.ui.user

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.google.firebase.Timestamp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserCreate(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  var firstName by remember { mutableStateOf("") }
  var lastName by remember { mutableStateOf("") }
  var userName by remember { mutableStateOf("") }
  var userNameError by remember { mutableStateOf(false) }
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("addScreen"),
      topBar = {
        TopAppBar(
            title = {},
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
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.Center, // Centers the content vertically
            horizontalAlignment =
                androidx.compose.ui.Alignment.CenterHorizontally // Centers the content horizontally
            ) {
              OutlinedTextField(
                  value = firstName,
                  onValueChange = { firstName = it },
                  label = { Text("First Name") },
                  modifier =
                      Modifier.fillMaxWidth(0.8f) // Set width to 80% of the screen
                          .padding(vertical = 8.dp))

              OutlinedTextField(
                  value = lastName,
                  onValueChange = { lastName = it },
                  label = { Text("Last Name") },
                  modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp))

              OutlinedTextField(
                  value = userName,
                  onValueChange = { userName = it },
                  label = { Text("* User Name") },
                  isError = userNameError,
                  modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp))

              // Save Button
              Button(
                  onClick = {
                    userViewModel.addUser(
                        user =
                            User(
                                name = "$firstName $lastName",
                                email = "",
                                uid = userViewModel.getNewUid(),
                                dateOfJoining = Timestamp.now(),
                                rating = 0.0),
                        onSuccess = { navigationActions.goBack() },
                        onFailure = {
                          Toast.makeText(context, "Error while adding user", Toast.LENGTH_SHORT)
                              .show()
                          userNameError = true
                        })
                  },
                  modifier =
                      Modifier.fillMaxWidth(0.8f)
                          .padding(vertical = 16.dp), // Adjust padding for spacing
                  enabled = userName.isNotBlank()) {
                    Text("Save")
                  }
            }
      })
}
