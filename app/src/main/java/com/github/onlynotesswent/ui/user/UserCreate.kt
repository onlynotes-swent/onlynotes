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
import com.github.onlynotesswent.ui.navigation.Screen
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
      modifier = Modifier.testTag("addUserScreen"),
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
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
              OutlinedTextField(
                  value = firstName,
                  onValueChange = { firstName = it },
                  label = { Text("First Name") },
                  modifier =
                      Modifier.fillMaxWidth(0.8f)
                          .padding(vertical = 8.dp)
                          .testTag("inputFirstName"))

              OutlinedTextField(
                  value = lastName,
                  onValueChange = { lastName = it },
                  label = { Text("Last Name") },
                  modifier =
                      Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp).testTag("inputLastName"))

              OutlinedTextField(
                  value = userName,
                  onValueChange = { userName = it },
                  label = { Text("* User Name") },
                  isError = userNameError,
                  modifier =
                      Modifier.fillMaxWidth(0.8f).padding(vertical = 8.dp).testTag("inputUserName"))

              // Save Button
              Button(
                  onClick = {
                    userViewModel.addUser(
                        user =
                            User(
                                firstName = firstName,
                                lastName = lastName,
                                userName = userName,
                                email = "",
                                uid = userViewModel.getNewUid(),
                                dateOfJoining = Timestamp.now(),
                                rating = 0.0),
                        onSuccess = { navigationActions.navigateTo(Screen.OVERVIEW) },
                        onFailure = { exception ->
                          // Show a toast message if the user name is already taken
                          Toast.makeText(
                                  context,
                                  "Error while adding user: $exception",
                                  Toast.LENGTH_SHORT)
                              .show()
                          userNameError = true
                        })
                  },
                  modifier =
                      Modifier.fillMaxWidth(0.8f)
                          .padding(vertical = 16.dp)
                          .testTag("createUserButton"),
                  // Disable the button if the user name is empty
                  enabled = userName.isNotBlank()) {
                    Text("Save")
                  }
            }
      })
}
