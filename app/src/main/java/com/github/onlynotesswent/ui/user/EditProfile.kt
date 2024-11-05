package com.github.onlynotesswent.ui.user

import android.annotation.SuppressLint
import android.content.Context
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
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.utils.ProfilePictureTaker

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
    profilePictureTaker: ProfilePictureTaker,
    fileViewModel: FileViewModel
) {
  val user = userViewModel.currentUser.collectAsState()

  val newFirstName = remember { mutableStateOf(user.value?.firstName ?: "") }
  val newLastName = remember { mutableStateOf(user.value?.lastName ?: "") }
  val newUserName = remember { mutableStateOf(user.value?.userName ?: "") }
  val profilePictureUri = remember { mutableStateOf("") }
  val userNameError = remember { mutableStateOf(false) }
  val saveEnabled = remember { mutableStateOf(true) }
  val isProfilePictureUpToDate = remember { mutableStateOf(false) }
  val hasProfilePictureBeenChanged = remember { mutableStateOf(false) }
  val localContext = LocalContext.current

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
                  onClick = {
                    // when we go back we  we will need to fetch again the old profile picture if it
                    // was changed
                    // because going back don't save the changes
                    isProfilePictureUpToDate.value = !hasProfilePictureBeenChanged.value
                    navigationActions.goBack()
                  },
                  Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back Button")
                  }
            })
      },
      content = { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              ProfilePicture(
                  profilePictureTaker,
                  userViewModel,
                  profilePictureUri,
                  fileViewModel,
                  isProfilePictureUpToDate,
                  hasProfilePictureBeenChanged,
                  localContext)

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
                              hasProfilePicture =
                                  profilePictureUri.value.isNotBlank() || it.hasProfilePicture)
                        }
                    if (updatedUser == null) {
                      Toast.makeText(
                              localContext,
                              "Error while updating user: current user is null",
                              Toast.LENGTH_SHORT)
                          .show()
                      Log.e("EditProfileScreen", "Error while updating user: current user is null")
                    } else {
                      userViewModel.updateUser(
                          user = updatedUser,
                          onSuccess = {
                            navigationActions.goBack()
                            // Upload the profile picture  if it has been changed
                            if (hasProfilePictureBeenChanged.value) {
                              fileViewModel.uploadFile(
                                  userViewModel.currentUser.value!!.uid,
                                  profilePictureUri.value.toUri(),
                                  FileType.PROFILE_PIC_JPEG,
                              )
                            }
                          },
                          onFailure = { exception ->
                            Toast.makeText(
                                    localContext,
                                    "Error while updating user: ${exception.message}",
                                    Toast.LENGTH_SHORT)
                                .show()
                            Log.e("EditProfileScreen", "Error while updating user ", exception)
                            userNameError.value =
                                exception is UserRepositoryFirestore.UsernameTakenException
                          })
                    }
                  },
                  enabled = saveEnabled)
            }
      })
}

@SuppressLint("StateFlowValueCalledInComposition")
@Composable
fun ProfilePicture(
    profilePictureTaker: ProfilePictureTaker,
    userViewModel: UserViewModel,
    profilePictureUri: MutableState<String>,
    fileViewModel: FileViewModel,
    isProfilePictureUpToDate: MutableState<Boolean>,
    hasProfilePictureBeenChanged: MutableState<Boolean>,
    localContext: Context
) {

  Box(modifier = Modifier.size(150.dp)) {
    // Download the profile picture from Firebase Storage if it hasn't been downloaded yet
    if (!isProfilePictureUpToDate.value &&
        userViewModel.currentUser.value?.hasProfilePicture == true) {
      fileViewModel.downloadFile(
          userViewModel.currentUser.value!!.uid,
          FileType.PROFILE_PIC_JPEG,
          context = localContext,
          onSuccess = { file ->
            profilePictureUri.value = file.absolutePath
            isProfilePictureUpToDate.value = true
            // we now the the current profile picture is the same as the one in the database
            hasProfilePictureBeenChanged.value = false
          },
          onFailure = { e -> Log.e("ProfilePicture", "Error downloading profile picture", e) })
    }

    // Profile Picture Painter
    val painter =
        if (profilePictureUri.value.isNotBlank()) {
          // Load the profile picture if it exists
          rememberAsyncImagePainter(profilePictureUri.value)
        } else {
          // Load the default profile picture if it doesn't exist
          rememberVectorPainter(Icons.Default.AccountCircle)
        }

    // Profile Picture
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
                .align(Alignment.BottomEnd) // Position on the bottom-left corner
                .offset(x = (-8).dp, y = (-8).dp)
                .clip(CircleShape)
                .background(Color.White) // Background color
                .clickable {
                  // Edit the image and save the URI to the profilePicture state
                  profilePictureTaker.setOnImageSelected { uri ->
                    if (uri != null) {
                      profilePictureUri.value = uri.toString()
                      hasProfilePictureBeenChanged.value = true
                    }
                  }
                  profilePictureTaker.pickImage()
                },
        tint = Color.Gray // Icon color
        )
  }
}
