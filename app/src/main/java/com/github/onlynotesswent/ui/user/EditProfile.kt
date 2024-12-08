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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.net.toUri
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepositoryFirestore
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.github.onlynotesswent.ui.theme.Typography
import com.github.onlynotesswent.utils.PictureTaker

/**
 * A composable function that displays the profile screen.
 *
 * @param navigationActions An instance of NavigationActions to handle navigation events.
 * @param userViewModel An instance of UserViewModel to manage user data.
 * @param pictureTaker An instance of ProfilePictureTaker to choose a profile picture.
 * @param fileViewModel An instance of FileViewModel to manage file operations.
 * @param noteViewModel An instance of UserViewModel to manage note data.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    pictureTaker: PictureTaker,
    fileViewModel: FileViewModel,
    noteViewModel: NoteViewModel,
    folderViewModel: FolderViewModel
) {
  val user = userViewModel.currentUser.collectAsState()

  val newFirstName = remember { mutableStateOf(user.value?.firstName ?: "") }
  val newLastName = remember { mutableStateOf(user.value?.lastName ?: "") }
  val newUserName = remember { mutableStateOf(user.value?.userName ?: "") }
  val newBio = remember { mutableStateOf(user.value?.bio ?: "") }
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
  val newIsAccountPublic = remember { mutableStateOf(user.value?.isAccountPublic ?: false) }

  if (user.value == null) {
    // If the user is null, display an error message
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {
          Text(stringResource(R.string.user_not_found))
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
            TopAppBar(
                title = { Text(stringResource(R.string.edit_profile)) },
                navigationIcon = {
                  IconButton(
                      onClick = {
                        if (newFirstName.value != user.value?.firstName ||
                            newLastName.value != user.value?.lastName ||
                            newUserName.value != user.value?.userName ||
                            newBio.value != user.value?.bio ||
                            newIsAccountPublic.value != user.value?.isAccountPublic ||
                            hasProfilePictureBeenChanged.value) {
                          showGoingBackWithoutSavingChanges.value = true
                        } else {
                          navigationActions.goBack()
                        }
                      },
                      Modifier.testTag("goBackButton")) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = "Back")
                      }
                },
                actions = {
                  IconButton(
                      onClick = {
                        val updatedUser =
                            user.value!!.copy(
                                firstName = newFirstName.value.trim(),
                                lastName = newLastName.value.trim(),
                                userName = newUserName.value.trim(),
                                bio = newBio.value.trim(),
                                hasProfilePicture = profilePictureUri.value.isNotBlank(),
                                isAccountPublic = newIsAccountPublic.value,
                            )

                        userViewModel.updateUser(
                            user = updatedUser,
                            onSuccess = {
                              navigationActions.navigateTo(TopLevelDestinations.PROFILE)
                              // Upload or delete the profile picture if it has been changed
                              if (hasProfilePictureBeenChanged.value) {
                                if (profilePictureUri.value.isNotBlank()) {
                                  if (user.value!!.hasProfilePicture) {
                                    fileViewModel.updateFile(
                                        uid = userViewModel.currentUser.value!!.uid,
                                        fileUri = profilePictureUri.value.toUri(),
                                        fileType = FileType.PROFILE_PIC_JPEG,
                                        onFailure = {
                                          Toast.makeText(
                                                  localContext,
                                                  "Error updating profile picture",
                                                  Toast.LENGTH_SHORT)
                                              .show()
                                        },
                                    )
                                  } else {
                                    fileViewModel.uploadFile(
                                        uid = userViewModel.currentUser.value!!.uid,
                                        fileUri = profilePictureUri.value.toUri(),
                                        fileType = FileType.PROFILE_PIC_JPEG,
                                        onFailure = {
                                          Toast.makeText(
                                                  localContext,
                                                  "Error uploading profile picture",
                                                  Toast.LENGTH_SHORT)
                                              .show()
                                        })
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
                              userNameError.value =
                                  exception is UserRepositoryFirestore.UsernameTakenException
                            })
                      },
                      modifier = Modifier.testTag("saveButton"),
                      enabled = saveEnabled.value) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Save Note",
                            tint = MaterialTheme.colorScheme.onSurface)
                      }
                })
          },
          content = { paddingValues ->
            Column(
                modifier = Modifier.fillMaxSize().padding(paddingValues),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally) {
                  EditableProfilePicture(
                      pictureTaker,
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
                  BioTextField(newBio)

                  // Save Button
                  saveEnabled.value = newUserName.value.isNotBlank()

                  Row(modifier = Modifier.padding(top = 16.dp)) {
                    FilterChip(
                        modifier =
                            Modifier.width(130.dp).height(40.dp).testTag("publicAccountChip"),
                        selected = newIsAccountPublic.value,
                        onClick = { newIsAccountPublic.value = true },
                        label = {
                          Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.Center) {
                                Text(
                                    stringResource(R.string.public_account),
                                    style = Typography.titleMedium)
                              }
                        },
                        leadingIcon = {
                          if (newIsAccountPublic.value)
                              Icon(
                                  imageVector = Icons.Default.Public,
                                  contentDescription = "Public Account",
                                  tint = MaterialTheme.colorScheme.onSurface)
                        })
                    Spacer(modifier = Modifier.width(16.dp))
                    FilterChip(
                        modifier =
                            Modifier.width(130.dp).height(40.dp).testTag("privateAccountChip"),
                        selected = !newIsAccountPublic.value,
                        onClick = { newIsAccountPublic.value = false },
                        label = {
                          Row(
                              modifier = Modifier.fillMaxWidth(),
                              horizontalArrangement = Arrangement.Center) {
                                Text(
                                    stringResource(R.string.private_account),
                                    style = Typography.titleMedium)
                              }
                        },
                        leadingIcon = {
                          if (!newIsAccountPublic.value)
                              Icon(
                                  imageVector = Icons.Default.Lock,
                                  contentDescription = "Private Account",
                                  tint = MaterialTheme.colorScheme.onSurface)
                        })
                  }

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
                        title = { Text(stringResource(R.string.delete_account)) },
                        text = { Text(stringResource(R.string.delete_account_prompt)) },
                        modifier = Modifier.testTag("deleteAccountAlert"),
                        confirmButton = {
                          Button(
                              modifier = Modifier.testTag("confirmDeleteButton"),
                              onClick = {
                                showDeleteAccountAlert.value = false

                                noteViewModel.deleteNotesFromUid(user.value!!.uid)
                                folderViewModel.deleteFoldersFromUid(user.value!!.uid)
                                noteViewModel.getNoteById(user.value!!.uid)
                                noteViewModel.userRootNotes.value.forEach {
                                  fileViewModel.deleteFile(it.id, FileType.NOTE_PDF)
                                }
                                fileViewModel.deleteFile(
                                    user.value!!.uid, FileType.PROFILE_PIC_JPEG)
                                userViewModel.deleteUserById(
                                    user.value!!.uid,
                                    onSuccess = { navigationActions.navigateTo(Route.AUTH) })
                              },
                              content = { Text(stringResource(R.string.yes)) })
                        },
                        dismissButton = {
                          Button(
                              modifier = Modifier.testTag("dismissDeleteButton"),
                              onClick = { showDeleteAccountAlert.value = false },
                              content = { Text(stringResource(R.string.no)) })
                        })
                  }
                  if (showGoingBackWithoutSavingChanges.value) {
                    AlertDialog(
                        onDismissRequest = { showGoingBackWithoutSavingChanges.value = false },
                        title = { Text(stringResource(R.string.discard_changes)) },
                        text = { Text(stringResource(R.string.discard_changes_text)) },
                        modifier = Modifier.testTag("goingBackAlert"),
                        confirmButton = {
                          Button(
                              modifier = Modifier.testTag("confirmGoingBack"),
                              onClick = {
                                showGoingBackWithoutSavingChanges.value = false
                                // When we go back, we will need to fetch again the old profile
                                // picture (if it was changed), if the user didn't save the changes
                                isProfilePictureUpToDate.value = !hasProfilePictureBeenChanged.value
                                navigationActions.goBack()
                              },
                              content = { Text(stringResource(R.string.yes)) })
                        },
                        dismissButton = {
                          Button(
                              modifier = Modifier.testTag("dismissGoingBack"),
                              onClick = { showGoingBackWithoutSavingChanges.value = false },
                              content = { Text(stringResource(R.string.no)) })
                        })
                  }
                }
          })
}

@Composable
fun BioTextField(bioState: MutableState<String>) {
  OutlinedTextField(
      minLines = 2,
      maxLines = 4,
      value = bioState.value,
      onValueChange = { bioState.value = User.formatBio(it) },
      label = { Text(stringResource(R.string.biography)) },
      modifier = Modifier.fillMaxWidth(0.8f).padding(vertical = 12.dp).testTag("inputBio"))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditableProfilePicture(
    pictureTaker: PictureTaker,
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
          onFileNotFound = {
            // Shouldn't happen if correctly implemented
          },
          onFailure = {
            Toast.makeText(localContext, "Error downloading profile picture", Toast.LENGTH_SHORT)
                .show()
          })
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
            pictureTaker,
            profilePictureUri,
            hasProfilePictureBeenChanged)
      }
    }
  }
}

@Composable
fun BottomSheetContent(
    onClose: () -> Unit,
    pictureTaker: PictureTaker,
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
                onClick = {
                  pictureTaker.setOnImageSelected { uri ->
                    if (uri != null) {
                      profilePictureUri.value = uri.toString()
                      hasProfilePictureBeenChanged.value = true
                    }
                  }
                  pictureTaker.pickImage()
                  onClose()
                },
                description = stringResource(R.string.edit_profile_picture),
                icon = Icons.Default.Edit,
                color = MaterialTheme.colorScheme.tertiary,
                testTag = "editProfilePicture")
            Spacer(modifier = Modifier.height(16.dp))
            BottomSheetRow(
                onClick = {
                  profilePictureUri.value = ""
                  hasProfilePictureBeenChanged.value = true
                  onClose()
                },
                description = stringResource(R.string.remove_profile_picture),
                icon = Icons.Default.Delete,
                color = MaterialTheme.colorScheme.tertiary,
                testTag = "removeProfilePicture")
          }
        } else {
          // If the user doesn't have a profile picture, display the add option
          BottomSheetRow(
              onClick = {
                pictureTaker.setOnImageSelected { uri ->
                  if (uri != null) {
                    profilePictureUri.value = uri.toString()
                    hasProfilePictureBeenChanged.value = true
                  }
                }
                pictureTaker.pickImage()
                onClose()
              },
              description = stringResource(R.string.add_a_profile_picture),
              icon = Icons.Default.Add,
              color = MaterialTheme.colorScheme.tertiary,
              testTag = "addProfilePicture")
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
