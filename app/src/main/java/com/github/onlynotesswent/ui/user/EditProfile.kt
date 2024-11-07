package com.github.onlynotesswent.ui.user

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
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
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
  val sheetState = rememberModalBottomSheetState()
  val showSheet = remember { mutableStateOf(false) }

  if (user.value == null) {
    // If the user is null, display an error message
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text("User  not found ...")
        }
    Log.e("EditProfileScreen", "User not found")
  } else
      Scaffold(
          modifier = Modifier.testTag("ProfileScreen"),
          bottomBar = {
            BottomNavigationMenu(
                onTabSelect = { route -> navigationActions.navigateTo(route) },
                tabList = LIST_TOP_LEVEL_DESTINATION,
                selectedItem = navigationActions.currentRoute())
          },
          topBar = {
            TopProfileBar(
                "Edit Profile",
                navigationActions,
                includeBackButton = true,
                onBackButtonClick = {
                  // When we go back we  we will need to fetch again the old profile picture if it
                  // was changed, because going back doesn't save the changes
                  isProfilePictureUpToDate.value = !hasProfilePictureBeenChanged.value
                  navigationActions.goBack()
                })
          },
          content = { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                  EditableProfilePicture(
                      profilePictureTaker,
                      userViewModel,
                      profilePictureUri,
                      fileViewModel,
                      isProfilePictureUpToDate,
                      hasProfilePictureBeenChanged,
                      localContext,
                      showSheet,
                      sheetState,
                  )

                  // Text Fields for user information
                  FirstNameTextField(newFirstName)
                  LastNameTextField(newLastName)
                  UserNameTextField(newUserName, userNameError)

                  // Save Button
                  saveEnabled.value = newUserName.value.isNotBlank()
                  SaveButton(
                      onClick = {
                        val updatedUser =
                            user.value!!.copy(
                                firstName = newFirstName.value,
                                lastName = newLastName.value,
                                userName = newUserName.value,
                                hasProfilePicture = profilePictureUri.value.isNotBlank())

                        userViewModel.updateUser(
                            user = updatedUser,
                            onSuccess = {
                              navigationActions.navigateTo(TopLevelDestinations.PROFILE)
                              // Upload or delete the profile picture if it has been changed
                              if (hasProfilePictureBeenChanged.value) {
                                if (profilePictureUri.value.isNotBlank()) {
                                  fileViewModel.uploadFile(
                                      userViewModel.currentUser.value!!.uid,
                                      profilePictureUri.value.toUri(),
                                      FileType.PROFILE_PIC_JPEG,
                                  )
                                } else {
                                  fileViewModel.deleteFile(
                                      userViewModel.currentUser.value!!.uid,
                                      FileType.PROFILE_PIC_JPEG,
                                  )
                                }
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
                      },
                      enabled = saveEnabled)
                }
          })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableProfilePicture(
    profilePictureTaker: ProfilePictureTaker,
    userViewModel: UserViewModel,
    profilePictureUri: MutableState<String>,
    fileViewModel: FileViewModel,
    isProfilePictureUpToDate: MutableState<Boolean>,
    hasProfilePictureBeenChanged: MutableState<Boolean>,
    localContext: Context,
    showSheet: MutableState<Boolean>,
    sheetState: SheetState
) {
  val user = userViewModel.currentUser.collectAsState()

  Box(modifier = Modifier.size(150.dp)) {
    // Download the profile picture from Firebase Storage if it hasn't been downloaded yet
    if (!isProfilePictureUpToDate.value && user.value!!.hasProfilePicture) {
      fileViewModel.downloadFile(
          user.value!!.uid,
          FileType.PROFILE_PIC_JPEG,
          context = localContext,
          onSuccess = { file ->
            profilePictureUri.value = file.absolutePath
            isProfilePictureUpToDate.value = true
            // Now the current profile picture is the same as the one in the database
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

    IconButton(
        onClick = { showSheet.value = true },
        modifier =
            Modifier.testTag("displayBottomSheet") // Size of the edit icon
                .align(Alignment.BottomEnd) // Position on the bottom-left corner
                .offset(x = (0).dp, y = (-5).dp)
                .clip(CircleShape)
                .size(40.dp)
                .background(
                    androidx.compose.material3.ButtonDefaults.buttonColors()
                        .containerColor) // Background color
        ,
        content = {
          Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Edit Profile Picture",
              modifier = Modifier.size(30.dp),
              tint =
                  androidx.compose.material3.ButtonDefaults.buttonColors()
                      .contentColor
                      .copy(alpha = 0.8f))
        })

    if (showSheet.value) {
      ModalBottomSheet(onDismissRequest = { showSheet.value = false }, sheetState = sheetState) {
        BottomSheetContent(
            onClose = { showSheet.value = false },
            profilePictureTaker,
            profilePictureUri,
            hasProfilePictureBeenChanged)
      }
    }
  }
}

@Composable
fun BottomSheetContent(
    onClose: () -> Unit,
    profilePictureTaker: ProfilePictureTaker,
    profilePictureUri: MutableState<String>,
    hasProfilePictureBeenChanged: MutableState<Boolean>
) {
  Column(
      modifier = Modifier.fillMaxWidth().padding(30.dp)
      // .padding(start = 80.dp)
      ,
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        if (profilePictureUri.value.isNotBlank()) {
          Column(horizontalAlignment = Alignment.Start) {
            Row(
                modifier =
                    Modifier.testTag("editProfilePicture").clickable {
                      profilePictureTaker.setOnImageSelected { uri ->
                        if (uri != null) {
                          profilePictureUri.value = uri.toString()
                          hasProfilePictureBeenChanged.value = true
                        }
                      }
                      profilePictureTaker.pickImage()
                      onClose()
                    }) {
                  Icon(
                      imageVector = Icons.Default.Edit,
                      contentDescription = "Edit Profile Picture",
                      modifier = Modifier.size(30.dp).offset(x = (-20).dp, y = (-4).dp),
                      tint = Color.LightGray)
                  Text("Edit Profile Picture", fontSize = 18.sp)
                }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier =
                    Modifier.testTag("removeProfilePicture").clickable {
                      profilePictureUri.value = ""
                      hasProfilePictureBeenChanged.value = true
                      onClose()
                    }) {
                  Icon(
                      imageVector = Icons.Default.Delete,
                      contentDescription = "Edit Profile Picture",
                      modifier = Modifier.size(30.dp).offset(x = (-20).dp, y = (-4).dp),
                      tint = Color.Red)
                  Text("Remove Profile Picture", fontSize = 18.sp)
                }
          }
        } else {
          Row(
              modifier =
                  Modifier.testTag("addProfilePicture").clickable {
                    profilePictureTaker.setOnImageSelected { uri ->
                      if (uri != null) {
                        profilePictureUri.value = uri.toString()
                        hasProfilePictureBeenChanged.value = true
                      }
                    }
                    profilePictureTaker.pickImage()
                    onClose()
                  }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "add Profile Picture",
                    modifier = Modifier.size(30.dp).offset(x = (-20).dp, y = (-3).dp),
                    tint = Color.LightGray)
                Text("Add a profile picture", fontSize = 18.sp)
              }
        }
      }
}
