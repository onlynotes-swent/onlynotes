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

/**
 * Extracts the UID from the URI.
 *
 * @param uri The URI from which the UID is to be extracted.
 * @param extension The extension of the file in the URI.
 * @return The UID extracted from the URI.
 */
fun extractUID(uri: String?, extension: String = FileType.FLASHCARD_IMAGE.fileExtension): String? {
  val parts = uri?.split("/") ?: return null
  val fileName = parts.lastOrNull() ?: return null
  return fileName.substringBeforeLast(extension)
}

/**
 * Compares the UID extracted from the URI with the given UID.
 *
 * @param uri The URI from which the UID is to be extracted.
 * @param uid The UID to compare with the extracted UID.
 * @param extension The extension of the file in the URI.
 * @return True if the extracted UID matches the given UID, false otherwise.
 */
fun compareUID(
    uri: String?,
    uid: String?,
    extension: String = FileType.FLASHCARD_IMAGE.fileExtension
): Boolean {
  return uid?.let { extractUID(uri, extension)?.take(it.length) == it } == true
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
    onClick: (() -> Unit)? = null,
    size: Int = 40,
) {
  val profilePictureUri = remember { mutableStateOf("") }
  NonModifiableProfilePicture(
      user,
      profilePictureUri,
      fileViewModel,
      size,
      "thumbnail--${user.value?.uid?:"default"}",
      onClick)
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
    onClick: (() -> Unit)? = null
) {
  val boxModifier = Modifier.size(size.dp)
  Box(modifier = onClick?.let { boxModifier.clickable { onClick() } } ?: boxModifier) {
    val painter =
        if (compareUID(
            profilePictureUri.value, user.value?.uid, FileType.PROFILE_PIC_JPEG.fileExtension)) {
          rememberAsyncImagePainter(profilePictureUri.value)
        } else {
          // Load the default profile picture if no picture was found
          // and download the profile picture if it should exist
          if (user.value?.hasProfilePicture == true) {
            fileViewModel.downloadFile(
                user.value!!.uid,
                FileType.PROFILE_PIC_JPEG,
                context = LocalContext.current,
                onSuccess = { file -> profilePictureUri.value = file.absolutePath },
                onFileNotFound = { Log.e("ProfilePicture", "Profile picture not found") },
                onFailure = { e ->
                  Log.e("ProfilePicture", "Error downloading profile picture", e)
                })
          }
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
