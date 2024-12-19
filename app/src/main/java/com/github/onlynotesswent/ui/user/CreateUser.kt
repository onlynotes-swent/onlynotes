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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepositoryFirestore
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.authentication.Logo
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth

/**
 * A composable function that displays the profile screen.
 *
 * @param navigationActions An instance of NavigationActions to handle navigation events.
 * @param userViewModel An instance of UserViewModel to manage user data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateUserScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  // State variables to hold user input
  val firstName = remember { mutableStateOf("") }
  val lastName = remember { mutableStateOf("") }
  val userName = remember { mutableStateOf("") }
  val userNameError = remember { mutableStateOf(false) }
  val enableSaving = remember { mutableStateOf(false) }
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
              // Logo
              Logo()
              // Text Fields
              FirstNameTextField(firstName)
              LastNameTextField(lastName)
              UserNameTextField(userName, userNameError)

              // Save Button
              enableSaving.value = userName.value.isNotBlank()
              SaveButton(
                  onClick = {
                    val user =
                        User(
                            firstName = firstName.value.trim(),
                            lastName = lastName.value.trim(),
                            userName = userName.value.trim(),
                            email = Firebase.auth.currentUser?.email ?: "",
                            uid = userViewModel.getNewUid(),
                            dateOfJoining = Timestamp.now(),
                            rating = 0.0)
                    userViewModel.addUser(
                        user = user,
                        onSuccess = {
                          navigationActions.navigateTo(TopLevelDestinations.NOTE_OVERVIEW)
                        },
                        onFailure = { exception ->
                          Toast.makeText(
                                  context,
                                  "Error while adding user: ${exception.message}",
                                  Toast.LENGTH_SHORT)
                              .show()
                          userNameError.value =
                              exception is UserRepositoryFirestore.UsernameTakenException
                        })
                  },
                  // Disable the button if the user name is empty
                  enabled = enableSaving)
            }
      })
}

/**
 * A composable function that creates an OutlinedTextField for entering the first name.
 *
 * @param newFirstName A MutableState object that holds the value of the first name.
 */
@Composable
fun FirstNameTextField(newFirstName: MutableState<String>) {
  OutlinedTextField(
      value = newFirstName.value,
      onValueChange = { newFirstName.value = User.formatName(it) },
      label = { Text(stringResource(R.string.first_name)) },
      modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 12.dp).testTag("inputFirstName"))
}

/**
 * A composable function that creates an OutlinedTextField for entering the user name.
 *
 * @param newUserName A MutableState object that holds the value of the user name.
 * @param userNameError A MutableState object that indicates whether there is an error with the user
 *   name.
 */
@Composable
fun UserNameTextField(newUserName: MutableState<String>, userNameError: MutableState<Boolean>) {
  OutlinedTextField(
      value = newUserName.value,
      onValueChange = { newUserName.value = User.formatUsername(it) },
      label = { Text(stringResource(R.string.user_name)) },
      isError = userNameError.value,
      modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 12.dp).testTag("inputUserName"))
}

/**
 * A composable function that creates an OutlinedTextField for entering the last name.
 *
 * @param newLastName A MutableState object that holds the value of the last name.
 */
@Composable
fun LastNameTextField(newLastName: MutableState<String>) {
  OutlinedTextField(
      value = newLastName.value,
      onValueChange = { newLastName.value = User.formatName(it) },
      label = { Text(stringResource(R.string.last_name)) },
      modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 12.dp).testTag("inputLastName"))
}

/**
 * A composable function that creates a Button for saving the user information.
 *
 * @param onClick A lambda function to be executed when the button is clicked.
 * @param enabled A MutableState object that indicates whether the button is enabled.
 */
@Composable
fun SaveButton(onClick: () -> Unit, enabled: MutableState<Boolean>) {
  Button(
      onClick = onClick,
      modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 16.dp).testTag("saveButton"),
      enabled = enabled.value) {
        Text(stringResource(R.string.save))
      }
}
