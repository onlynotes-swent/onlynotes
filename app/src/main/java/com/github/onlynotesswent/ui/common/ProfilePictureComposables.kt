package com.github.onlynotesswent.ui.common

import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.user.User

private val doesNothing = {}
/**
 * Displays the user's thumbnail profile picture, by wrapping the NonModifiableProfilePicture
 * composable.
 *
 * @param user The user whose profile picture is to be displayed.
 * @param fileViewModel The ViewModel for downloading images.
 * @param size The size of the profile picture.
 */
@Composable
fun ThumbnailPic(user: User?, fileViewModel: FileViewModel, size: Int = 40) {
  val profilePictureUri = remember { mutableStateOf("") }
  val userState = remember { mutableStateOf(user) }
  NonModifiableProfilePicture(
      userState, profilePictureUri, fileViewModel, size, "thumbnail--${user?.uid?:"default"}")
}

/**
 * Displays the user's thumbnail profile picture, by wrapping the NonModifiableProfilePicture
 * composable.
 *
 * @param user The user whose profile picture is to be displayed.
 * @param fileViewModel The ViewModel for downloading images.
 * @param size The size of the profile picture.
 */
@Composable
fun ThumbnailDynamicPic(
    user: State<User?>,
    fileViewModel: FileViewModel,
    onClick: () -> Unit = doesNothing,
    size: Int = 40,
) {
  val profilePictureUri = remember { mutableStateOf("") }
  NonModifiableProfilePicture(
      user,
      profilePictureUri,
      fileViewModel,
      size,
      "thumbnail--${user.value?.uid?:"default"}",
      onClick = { onClick() })
}

/**
 * Displays the user's profile picture.
 *
 * @param user The user whose profile picture is to be displayed.
 * @param profilePictureUri The URI of the profile picture.
 * @param fileViewModel The ViewModel for downloading images.
 * @param size The size of the profile picture.
 * @param testTag The test tag for the profile picture.
 */
@Composable
fun NonModifiableProfilePicture(
    user: State<User?>,
    profilePictureUri: MutableState<String>,
    fileViewModel: FileViewModel,
    size: Int = 150,
    testTag: String = "profilePicture",
    onClick: () -> Unit = doesNothing
) {
  val boxModifier =
      if (onClick === doesNothing) Modifier.size(size.dp)
      else Modifier.size(size.dp).clickable { onClick() }
  Box(modifier = boxModifier) {

    // Download the profile picture from Firebase Storage if it hasn't been downloaded yet
    if (user.value != null && user.value!!.hasProfilePicture && profilePictureUri.value.isBlank()) {
      fileViewModel.downloadFile(
          user.value!!.uid,
          FileType.PROFILE_PIC_JPEG,
          context = LocalContext.current,
          onSuccess = { file -> profilePictureUri.value = file.absolutePath },
          onFileNotFound = { Log.e("ProfilePicture", "Profile picture not found") },
          onFailure = { e -> Log.e("ProfilePicture", "Error downloading profile picture", e) })
    }

    // Profile Picture Painter
    val painter =
        if (user.value != null &&
            user.value!!.hasProfilePicture &&
            profilePictureUri.value.isNotBlank()) {
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
        modifier = Modifier.testTag(testTag).size(size.dp).clip(CircleShape),
        contentScale = ContentScale.Crop)
  }
}
