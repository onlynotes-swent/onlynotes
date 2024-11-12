package com.github.onlynotesswent.ui.user

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.vector.ImageVector
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
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.github.onlynotesswent.utils.ProfilePictureTaker

/**
 * A composable function that displays the profile screen.
 *
 * @param navigationActions An instance of NavigationActions to handle navigation events.
 * @param userViewModel An instance of UserViewModel to manage user data.
 * @param profilePictureTaker An instance of ProfilePictureTaker to choose a profile picture.
 * @param fileViewModel An instance of FileViewModel to manage file operations.
 * @param noteViewModel An instance of UserViewModel to manage note data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    profilePictureTaker: ProfilePictureTaker,
    fileViewModel: FileViewModel,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel
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
  val showDeleteAccountAlert = remember { mutableStateOf(false) }
  val showGoingBackWithoutSavingChanges = remember { mutableStateOf(false) }

  if (user.value == null) {
    // If the user is null, display an error message
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text("User not found ...")
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
                  if (newFirstName.value != user.value?.firstName ||
                      newLastName.value != user.value?.lastName ||
                      newUserName.value != user.value?.userName ||
                      hasProfilePictureBeenChanged.value) {
                    showGoingBackWithoutSavingChanges.value = true
                  } else {
                    navigationActions.goBack()
                  }
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
                                  if (user.value!!.hasProfilePicture) {
                                    fileViewModel.updateFile(
                                        userViewModel.currentUser.value!!.uid,
                                        profilePictureUri.value.toUri(),
                                        FileType.PROFILE_PIC_JPEG,
                                    )
                                  } else {
                                    fileViewModel.uploadFile(
                                        userViewModel.currentUser.value!!.uid,
                                        profilePictureUri.value.toUri(),
                                        FileType.PROFILE_PIC_JPEG,
                                    )
                                  }
                                } else {
                                  fileViewModel.deleteFile(
                                      userViewModel.currentUser.value!!.uid,
                                      FileType.PROFILE_PIC_JPEG,
                                  )
                                }
                              }
                            },
                            onFailure = { exception ->
                              val errorMessage =
                                  when (exception) {
                                    is UserRepositoryFirestore.UsernameTakenException ->
                                        "Username is already taken. Please choose a different one."
                                    else -> "Oops! Something went wrong. Please try again later."
                                  }
                              Toast.makeText(localContext, errorMessage, Toast.LENGTH_SHORT).show()
                              Log.e("EditProfileScreen", "Error while updating user ", exception)
                              userNameError.value =
                                  exception is UserRepositoryFirestore.UsernameTakenException
                            })
                      },
                      enabled = saveEnabled)

                  Button(
                      modifier = Modifier.padding(top = 16.dp).testTag("deleteAccountButton"),
                      colors =
                          ButtonDefaults.buttonColors(
                              containerColor = MaterialTheme.colorScheme.background,
                              contentColor = MaterialTheme.colorScheme.error),
                      border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                      onClick = { showDeleteAccountAlert.value = true },
                      content = { Text("Delete Account") })

                  if (showDeleteAccountAlert.value) {
                    AlertDialog(
                        onDismissRequest = { showDeleteAccountAlert.value = false },
                        title = { Text("Delete Account") },
                        text = { Text("Are you sure you want to delete your account?") },
                        modifier = Modifier.testTag("deleteAccountAlert"),
                        confirmButton = {
                          Button(
                              modifier = Modifier.testTag("confirmDeleteButton"),
                              onClick = {
                                showDeleteAccountAlert.value = false
                                noteViewModel.deleteNotesByUserId(user.value!!.uid)
                                folderViewModel.deleteFoldersByUserId(user.value!!.uid)
                                noteViewModel.getNoteById(user.value!!.uid)
                                noteViewModel.userRootNotes.value.forEach {
                                  fileViewModel.deleteFile(it.id, FileType.NOTE_PDF)
                                }
                                fileViewModel.deleteFile(
                                    user.value!!.uid, FileType.PROFILE_PIC_JPEG)

                                userViewModel.deleteUserById(
                                    user.value!!.uid,
                                    onSuccess = { navigationActions.navigateTo(Route.AUTH) },
                                    onFailure = { e ->
                                      Log.e("EditProfileScreen", "Error deleting user", e)
                                    })
                              },
                              content = { Text("Yes") })
                        },
                        dismissButton = {
                          Button(
                              modifier = Modifier.testTag("dismissDeleteButton"),
                              onClick = { showDeleteAccountAlert.value = false },
                              content = { Text("No") })
                        })
                  }
                  if (showGoingBackWithoutSavingChanges.value) {
                    AlertDialog(
                        onDismissRequest = { showGoingBackWithoutSavingChanges.value = false },
                        title = { Text("Unsaved changes") },
                        text = { Text("Are you sure you want to go back without saving changes?") },
                        modifier = Modifier.testTag("goingBackAlert"),
                        confirmButton = {
                          Button(
                              modifier = Modifier.testTag("confirmGoingBack"),
                              onClick = {
                                showGoingBackWithoutSavingChanges.value = false
                                // When we go back we will need to fetch again the old profile
                                // picture (if the
                                // picture was changed), because the user didn't save the changes
                                isProfilePictureUpToDate.value = !hasProfilePictureBeenChanged.value
                                navigationActions.goBack()
                              },
                              content = { Text("Yes") })
                        },
                        dismissButton = {
                          Button(
                              modifier = Modifier.testTag("dismissGoingBack"),
                              onClick = { showGoingBackWithoutSavingChanges.value = false },
                              content = { Text("No") })
                        })
                  }
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
            Modifier.testTag("displayBottomSheet")
                .align(Alignment.BottomEnd) // Position on the bottom-left corner
                .offset(x = (0).dp, y = (-5).dp)
                .clip(CircleShape)
                .size(40.dp) // Size of the edit icon background
                .background(MaterialTheme.colorScheme.secondary),
        content = {
          Icon(
              imageVector = Icons.Default.Edit,
              contentDescription = "Display Bottom Sheet",
              modifier = Modifier.size(30.dp), // Size of the edit icon
              tint = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.8f))
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
      modifier = Modifier.fillMaxWidth().padding(30.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center) {
        if (profilePictureUri.value.isNotBlank()) {
          // If the user has a profile picture, display the edit and remove options
          Column(horizontalAlignment = Alignment.Start) {
            BottomSheetRow(
                {
                  profilePictureTaker.setOnImageSelected { uri ->
                    if (uri != null) {
                      profilePictureUri.value = uri.toString()
                      hasProfilePictureBeenChanged.value = true
                    }
                  }
                  profilePictureTaker.pickImage()
                  onClose()
                },
                "Edit profile picture",
                Icons.Default.Edit,
                MaterialTheme.colorScheme.tertiary,
                "editProfilePicture")
            Spacer(modifier = Modifier.height(16.dp))
            BottomSheetRow(
                {
                  profilePictureUri.value = ""
                  hasProfilePictureBeenChanged.value = true
                  onClose()
                },
                "Remove profile picture",
                Icons.Default.Delete,
                MaterialTheme.colorScheme.tertiary,
                "removeProfilePicture")
          }
        } else {
          // If the user doesn't have a profile picture, display the add option
          BottomSheetRow(
              {
                profilePictureTaker.setOnImageSelected { uri ->
                  if (uri != null) {
                    profilePictureUri.value = uri.toString()
                    hasProfilePictureBeenChanged.value = true
                  }
                }
                profilePictureTaker.pickImage()
                onClose()
              },
              "Add a profile picture",
              Icons.Default.Add,
              MaterialTheme.colorScheme.tertiary,
              "addProfilePicture")
        }
      }
}

@Composable
fun BottomSheetRow(
    onClick: () -> Unit,
    description: String,
    icon: ImageVector,
    color: Color,
    testTag: String,
) {
  Row(modifier = Modifier.testTag(testTag).clickable { onClick() }) {
    Icon(
        imageVector = icon,
        contentDescription = description,
        modifier = Modifier.size(30.dp).offset(x = (-20).dp, y = (-3).dp),
        tint = color)
    Text(description, fontSize = 18.sp)
  }
}
