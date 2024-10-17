package com.github.onlynotesswent.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
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
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

/**
 * A composable function that displays the profile screen.
 *
 * @param navigationActions An instance of NavigationActions to handle navigation events.
 * @param userViewModel An instance of UserViewModel to manage user data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val user = userViewModel.currentUser.collectAsState()

  val newFirstName = remember { mutableStateOf(user.value?.firstName ?: "") }
  val newLastName = remember { mutableStateOf(user.value?.lastName ?: "") }
  val newUserName = remember { mutableStateOf(user.value?.userName ?: "") }
  val userNameError = remember { mutableStateOf(false) }
  val saveEnabled = remember { mutableStateOf(true) }
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

              // Text Fields for user information
              FirstNameTextField(newFirstName)
              LastNameTextField(newLastName)
              UserNameTextField(newUserName, userNameError)

              // Save Button
              saveEnabled.value = newUserName.value.isNotBlank()
              SaveButton(
                  onClick = {
                    val updatedUser =
                        user.value?.let {
                          User(
                              firstName = newFirstName.value,
                              lastName = newLastName.value,
                              userName = newUserName.value,
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
                      Log.e("ProfileScreen", "Error while updating user: current user is null")
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
                            Log.e("ProfileScreen", "Error while updating user ", exception)
                            userNameError.value =
                                exception is UserRepositoryFirestore.UsernameTakenException
                          })
                    }
                  },
                  enabled = saveEnabled)
            }
      })
}
