package com.github.onlynotesswent.ui.user

import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.utils.ProfilePictureTaker
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations

/**
 * A composable function that displays the profile screen.
 *
 * @param navigationActions An instance of NavigationActions to handle navigation events.
 * @param userViewModel An instance of UserViewModel to manage user data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    profilePictureTaker: ProfilePictureTaker
) {
  val user = userViewModel.currentUser.collectAsState()

  val newFirstName = remember { mutableStateOf(user.value?.firstName ?: "") }
  val newLastName = remember { mutableStateOf(user.value?.lastName ?: "") }
  val newUserName = remember { mutableStateOf(user.value?.userName ?: "") }
  val newProfilePicture = remember { mutableStateOf(user.value?.profilePicture ?: "") }
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
            horizontalAlignment = Alignment.CenterHorizontally) {
              ProfilePicture(newProfilePicture, userViewModel, profilePictureTaker)

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
                              rating = it.rating,
                              profilePicture = it.profilePicture,
                          )
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
                          onSuccess = { navigationActions.navigateTo(Screen.OVERVIEW) },
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

@Composable
fun ProfilePicture(
    profileImage: MutableState<String>,
    userViewModel: UserViewModel,
    profilePictureTaker: ProfilePictureTaker
) {

  Box(modifier = Modifier.size(150.dp)) {
    val painter =
        if (profileImage.value.isNotEmpty()) {
          rememberAsyncImagePainter(profileImage.value)
        } else {
          rememberVectorPainter(Icons.Default.AccountCircle)
        }

    Image(
        painter = painter,
        contentDescription = "Profile Picture",
        modifier =
            Modifier.testTag("profilePicture")
                .size(150.dp)
                .clip(CircleShape)
                .border(2.dp, Color.Gray, CircleShape),
        contentScale = ContentScale.Crop)

    // Edit Icon Overlay
    Icon(
        imageVector = Icons.Default.Edit,
        contentDescription = "Edit Profile Picture",
        modifier =
            Modifier.testTag("editProfilePicture")
                .size(40.dp) // Size of the edit icon
                .align(Alignment.BottomEnd)
                .offset(x = (-8).dp, y = (-8).dp) // Position on the bottom-left
                .clip(CircleShape)
                .background(Color.White) // Optional: background for contrast
                .clickable {
                  // add  the image here
                  editProfilePicture(profilePictureTaker, userViewModel, profileImage)
                }, // Trigger the onEditClick callback
        tint = Color.Gray // Icon color
        )
  }
}

fun editProfilePicture(
    profilePictureTaker: ProfilePictureTaker,
    userViewModel: UserViewModel,
    profileImage: MutableState<String>
) {
  profilePictureTaker.onImageSelected = { uri ->
    userViewModel.updateUser(
        userViewModel.currentUser.value!!.copy(profilePicture = uri.toString()),
        {},
        { e -> println(e) })
    profileImage.value = uri.toString()
  }
  profilePictureTaker.pickImage()
}
