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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val user = userViewModel.currentUser.collectAsState()

  var firstName by remember { mutableStateOf(user.value?.firstName ?: "") }
  var lastName by remember { mutableStateOf(user.value?.lastName ?: "") }
  var userName by remember { mutableStateOf(user.value?.userName ?: "") }
  var userNameError by remember { mutableStateOf(false) }
  val context = LocalContext.current

  Scaffold(
      modifier = Modifier.testTag("ProfileScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
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
                    val updatedUser =
                        user.value?.let {
                          User(
                              firstName = firstName,
                              lastName = lastName,
                              userName = userName,
                              email = it.email,
                              uid = it.uid,
                              dateOfJoining = it.dateOfJoining,
                              rating = it.rating)
                        }
                    if (updatedUser == null) {
                      Toast.makeText(
                              context,
                              "Error while updating user: current user is null",
                              Toast.LENGTH_SHORT)
                          .show()
                    } else {
                      userViewModel.updateUser(
                          user = updatedUser,
                          onSuccess = {
                            userViewModel.setCurrentUser(updatedUser)
                            navigationActions.navigateTo(Screen.OVERVIEW)
                          },
                          onFailure = { exception ->
                            Toast.makeText(
                                    context,
                                    "Error while updating user: ${exception.message}",
                                    Toast.LENGTH_SHORT)
                                .show()
                            userNameError =
                                exception is UserRepositoryFirestore.UsernameTakenException
                          })
                    }
                  },
                  modifier =
                      Modifier.fillMaxWidth(0.8f)
                          .padding(vertical = 16.dp)
                          .testTag("modifyUserButton"),
                  // Disable the button if the user name is empty
                  enabled = userName.isNotBlank()) {
                    Text("Save")
                  }
            }
      })
}
