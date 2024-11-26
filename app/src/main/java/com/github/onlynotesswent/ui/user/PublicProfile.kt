package com.github.onlynotesswent.ui.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.ExitToApp
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.github.onlynotesswent.ui.theme.Typography
import com.google.firebase.auth.FirebaseAuth

// User Profile Home screen:
/**
 * A composable function that displays the user profile screen.
 *
 * @param navigationActions An instance of NavigationActions to handle navigation events.
 * @param userViewModel An instance of UserViewModel to manage user data.
 * @param fileViewModel An instance of FileViewModel to manage file data.
 */
@Composable
fun UserProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
) {
  val user = userViewModel.currentUser.collectAsState()
  // Display the user's profile information
  ProfileScaffold(
      navigationActions = navigationActions,
      userViewModel = userViewModel,
      includeBackButton = false,
      topBarTitle = stringResource(R.string.my_profile),
      floatingActionButton = {
        ExtendedFloatingActionButton(
            modifier = Modifier.testTag("editProfileButton"),
            onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) }) {
              Row {
                Icon(Icons.Default.Create, contentDescription = "Edit Profile")
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.edit_profile))
              }
            }
      }) {
        ProfileContent(user, navigationActions, userViewModel, fileViewModel)
      }
}

// Public Profile screen:
/**
 * A composable function that displays the public profile screen.
 *
 * @param navigationActions An instance of NavigationActions to handle navigation events.
 * @param userViewModel An instance of UserViewModel to manage user data.
 * @param fileViewModel An instance of FileViewModel to manage file data.
 */
@Composable
fun PublicProfileScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
) {
  val currentUser = userViewModel.currentUser.collectAsState()
  val profileUser = userViewModel.profileUser.collectAsState()

  // Display the user's profile information
  ProfileScaffold(navigationActions, userViewModel) {
    ProfileContent(profileUser, navigationActions, userViewModel, fileViewModel)
    if (profileUser.value != null && currentUser.value != null) {
      FollowUnfollowButton(userViewModel, profileUser.value!!.uid)
    }
  }
}

/**
 * Displays the scaffold for the profile screen.
 *
 * @param navigationActions The navigation actions.
 * @param userViewModel The ViewModel for the user.
 * @param includeBackButton Whether to include the back button in the app bar.
 * @param topBarTitle The title to be displayed on the app bar.
 * @param floatingActionButton The floating action button to be displayed on the screen.
 * @param content The content to be displayed on the screen.
 */
@Composable
private fun ProfileScaffold(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    includeBackButton: Boolean = true,
    topBarTitle: String = stringResource(R.string.public_profile),
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
  Scaffold(
      modifier = Modifier.testTag("profileScaffold"),
      floatingActionButton = floatingActionButton,
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route ->
              // Navigate to route will clear navigation stack
              navigationActions.navigateTo(route)
              if (route == TopLevelDestinations.SEARCH) {
                navigationActions.pushToScreenNavigationStack(Screen.SEARCH)
              }
            },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      topBar = {
        TopProfileBar(
            title = topBarTitle,
            navigationActions = navigationActions,
            userViewModel = userViewModel,
            includeBackButton = includeBackButton)
      },
      content = { paddingValues ->
        Column(
            modifier =
                Modifier.fillMaxSize()
                    .padding(paddingValues)
                    .verticalScroll(rememberScrollState())
                    .testTag("profileScaffoldColumn"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              content()
            }
      })
}

/**
 * Displays the top app bar for the profile screen, can be used for Public, User or Edit profile
 * screens.
 *
 * @param title The title to be displayed on the app bar.
 * @param navigationActions The navigation actions.
 * @param userViewModel The ViewModel for the user.
 * @param includeBackButton Whether to include the back button in the app bar.
 * @param onBackButtonClick The action to be performed when the back button is clicked.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopProfileBar(
    title: String,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    includeBackButton: Boolean = true,
    onBackButtonClick: () -> Unit = {
      var userProfileId = navigationActions.popFromScreenNavigationStack()
      when {
        userProfileId == Screen.SEARCH -> {
          // If we come from search screen, we go back to search screen
          navigationActions.navigateTo(Screen.SEARCH)
        }
        userProfileId != null && userProfileId == userViewModel.profileUser.value?.uid -> {
          userProfileId = navigationActions.popFromScreenNavigationStack()
          if (userProfileId == Screen.SEARCH) {
            navigationActions.navigateTo(Screen.SEARCH)
          } else if (userProfileId != null) {
            // set profile user to userProfileId and navigate to public profile screen
            // If we pop from stack and the profile id corresponds to profile user (we will navigate
            // to the current screen), so we pop twice to get to previous visited public profile
            userViewModel.getUserById(
                userProfileId,
                { userViewModel.setProfileUser(it) },
                { navigationActions.navigateTo(TopLevelDestinations.PROFILE) },
                {})
            navigationActions.navigateTo(Screen.PUBLIC_PROFILE)
          } else {
            navigationActions.navigateTo(TopLevelDestinations.PROFILE)
          }
        }
        userProfileId != null -> {
          userViewModel.getUserById(
              userProfileId,
              { userViewModel.setProfileUser(it) },
              { navigationActions.navigateTo(TopLevelDestinations.PROFILE) },
              {})
          navigationActions.navigateTo(Screen.PUBLIC_PROFILE)
        }
        else -> {
          // If no user profile id is found, navigate to profile screen
          navigationActions.navigateTo(TopLevelDestinations.PROFILE)
        }
      }
    }
) {
  TopAppBar(
      title = { Text(title) },
      navigationIcon = {
        if (includeBackButton) {
          IconButton(onClick = onBackButtonClick, Modifier.testTag("goBackButton")) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
          }
        }
      },
      actions = {
        if (!includeBackButton) {
          LogoutButton {
            FirebaseAuth.getInstance().signOut()
            navigationActions.navigateTo(Screen.AUTH)
          }
        }
      })
}

/**
 * Displays the user's profile information.
 *
 * @param user The user whose profile information is to be displayed.
 * @param userViewModel The ViewModel for the user.
 * @param navigationActions The navigation actions.
 * @param fileViewModel The ViewModel for downloading images.
 */
@Composable
fun ProfileContent(
    user: State<User?>,
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel
) {
  val isFollowingMenuShown = remember { mutableStateOf(false) }
  val isFollowerMenuShown = remember { mutableStateOf(false) }
  val following: MutableState<List<User>> = remember { mutableStateOf(listOf()) }
  val followers: MutableState<List<User>> = remember { mutableStateOf(listOf()) }
  val profilePictureUri = remember { mutableStateOf("") }

  // Display the user's profile information:
  if (user.value == null) {
    Text(stringResource(R.string.user_not_found_2), modifier = Modifier.testTag("userNotFound"))
  } else {
    ElevatedCard(
        modifier = Modifier.fillMaxSize().padding(40.dp).testTag("profileCard"),
    ) {
      val borderPadding = 20.dp
      Column(
          modifier = Modifier.padding(borderPadding).fillMaxWidth().testTag("profileCardColumn"),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally) {

            // Profile picture
            NonModifiableProfilePicture(user, profilePictureUri, fileViewModel)

            Spacer(modifier = Modifier.height(20.dp))

            // Display the user's full name and handle (username)
            Row {
              Icon(Icons.Default.AccountCircle, "profileIcon")
              Text(
                  user.value!!.fullName(),
                  fontWeight = FontWeight(500),
                  modifier = Modifier.testTag("userFullName"))
            }
            Spacer(modifier = Modifier.height(5.dp))
            Text(
                user.value!!.userHandle(),
                fontWeight = FontWeight(400),
                fontStyle = FontStyle.Italic,
                modifier = Modifier.alpha(0.7f).testTag("userHandle"))
            Spacer(modifier = Modifier.height(10.dp))

            // Display the user's date of joining and rating
            Row {
              Icon(Icons.Default.Star, "starIcon")
              Text(
                  stringResource(R.string.rating, user.value!!.rating),
                  modifier = Modifier.testTag("userRating"))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
              Icon(Icons.Default.DateRange, "dateIcon")
              Text(
                  stringResource(R.string.member_since, user.value!!.dateToString()),
                  modifier = Modifier.testTag("userDateOfJoining"))
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Display the user's bio
            if (user.value!!.bio.isNotEmpty()) {
              DisplayBioCard(user)
              Spacer(modifier = Modifier.height(10.dp))
            }

            // Display the number of following and followers on clickable buttons
            Row {
              OutlinedButton(
                  modifier = Modifier.testTag("followingButton"),
                  onClick = {
                    userViewModel.getFollowingFrom(
                        user.value!!.uid,
                        {
                          following.value = it
                          isFollowingMenuShown.value = true
                        },
                        { isFollowingMenuShown.value = false })
                  }) {
                    Text(
                        stringResource(R.string.following, user.value!!.friends.following.size),
                        modifier = Modifier.testTag("followingText"))
                  }
              Spacer(modifier = Modifier.width(10.dp))
              OutlinedButton(
                  modifier = Modifier.testTag("followersButton"),
                  onClick = {
                    userViewModel.getFollowersFrom(
                        user.value!!.uid,
                        {
                          followers.value = it
                          isFollowerMenuShown.value = true
                        },
                        { isFollowerMenuShown.value = false })
                  }) {
                    Text(
                        stringResource(R.string.followers, user.value!!.friends.followers.size),
                        modifier = Modifier.testTag("followersText"))
                  }
            }
            // Display bottom sheets for the user's following and followers
            UserBottomSheet(
                expanded = isFollowingMenuShown,
                users = following,
                userViewModel = userViewModel,
                fileViewModel = fileViewModel,
                navigationActions = navigationActions,
                tag = "following")
            UserBottomSheet(
                expanded = isFollowerMenuShown,
                users = followers,
                userViewModel = userViewModel,
                fileViewModel = fileViewModel,
                navigationActions = navigationActions,
                tag = "followers",
                isFollowerSheetOfCurrentUser =
                    user.value == userViewModel.currentUser.collectAsState().value)
          }
    }
  }
}

/**
 * Displays a button that allows the user to follow or unfollow another user.
 *
 * @param userViewModel The ViewModel for the user.
 * @param otherUserId The ID of the user to follow or unfollow.
 */
@Composable
fun FollowUnfollowButton(userViewModel: UserViewModel, otherUserId: String) {
  val followButtonText = remember { mutableStateOf("") }
  followButtonText.value =
      if (userViewModel.currentUser
          .collectAsState()
          .value!!
          .friends
          .following
          .contains(otherUserId))
          stringResource(R.string.unfollow)
      else if (userViewModel.currentUser
          .collectAsState()
          .value!!
          .pendingFriends
          .following
          .contains(otherUserId))
          stringResource(R.string.pending)
      else stringResource(R.string.follow)
  OutlinedButton(
      contentPadding = PaddingValues(horizontal = 10.dp),
      shape = RoundedCornerShape(25),
      modifier = Modifier.testTag("followUnfollowButton--$otherUserId").width(90.dp),
      onClick = {
        if (followButtonText.value == "Follow")
            userViewModel.followUser(
                otherUserId,
                {
                  userViewModel.profileUser.value?.let { userViewModel.refreshProfileUser(it.uid) }
                },
                {})
        else
            userViewModel.unfollowUser(
                otherUserId,
                {
                  userViewModel.profileUser.value?.let { userViewModel.refreshProfileUser(it.uid) }
                },
                {})
      }) {
        Text(
            followButtonText.value,
            fontWeight = FontWeight(600),
            modifier = Modifier.testTag("followUnfollowButtonText--$otherUserId"),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
      }
}

/**
 * Displays a button that allows the user to remove a follower.
 *
 * @param userViewModel The ViewModel for the user.
 * @param followerId The ID of the follower to remove.
 */
@Composable
fun RemoveFollowerButton(userViewModel: UserViewModel, followerId: String) {
  val context = LocalContext.current
  OutlinedButton(
      contentPadding = PaddingValues(horizontal = 10.dp),
      shape = RoundedCornerShape(25),
      modifier = Modifier.testTag("removeFollowerButton--$followerId").width(90.dp),
      onClick = {
        userViewModel.removeFollower(
            followerId,
            { userViewModel.profileUser.value?.let { userViewModel.refreshProfileUser(it.uid) } },
            {})
      }) {
        Text(
            stringResource(R.string.remove),
            fontWeight = FontWeight(600),
            modifier = Modifier.testTag("removeFollowerText--$followerId"),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis)
      }
}

/**
 * Displays a bottom sheet with the list of users.
 *
 * @param expanded The state of the bottom sheet.
 * @param users The state of the list of users to be displayed in the dropdown menu.
 * @param userViewModel The ViewModel for the user.
 * @param fileViewModel The ViewModel for downloading images.
 * @param navigationActions The navigation actions.
 * @param tag The tag for the dropdown menu (either "following" or "followers"), used for testing.
 * @param isFollowerSheetOfCurrentUser Whether the sheet is for the current user's followers, in
 *   which case the "Remove" button is displayed.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserBottomSheet(
    expanded: MutableState<Boolean>,
    users: State<List<User>>,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel,
    navigationActions: NavigationActions,
    tag: String = "",
    isFollowerSheetOfCurrentUser: Boolean = false
) {
  if (expanded.value) {
    ModalBottomSheet(
        onDismissRequest = { expanded.value = false },
        modifier = Modifier.testTag("${tag}BottomSheet")) {
          Column(
              modifier = Modifier.fillMaxWidth().padding(bottom = 30.dp),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    stringResource(R.string.list_of, tag.replaceFirstChar { it.uppercase() }),
                    style = Typography.headlineMedium,
                    modifier = Modifier.padding(8.dp).testTag("${tag}Title"))
                if (users.value.isEmpty()) {
                  Text(
                      stringResource(R.string.no_to_display, tag),
                      modifier = Modifier.padding(8.dp).testTag("${tag}Absent"))
                }
                Column(
                    modifier = Modifier.fillMaxWidth().padding(start = 30.dp, end = 10.dp),
                    horizontalAlignment = Alignment.Start) {
                      users.value.forEach { user ->
                        UserItem(user, userViewModel, fileViewModel, isFollowerSheetOfCurrentUser) {
                          expanded.value = false
                          switchProfileTo(user, userViewModel, navigationActions)
                        }
                      }
                    }
              }
        }
  }
}

/**
 * Displays a user item with a thumbnail picture, full name, handle and follow/unfollow button.
 *
 * @param user The user to be displayed.
 * @param userViewModel The ViewModel for the user.
 * @param fileViewModel The ViewModel for downloading images.
 * @param isFollowerSheetOfCurrentUser Whether the sheet is for the current user's followers, in
 *   which case the "Remove" button is displayed.
 * @param onClick The action to be performed when the user item is clicked.
 */
@Composable
fun UserItem(
    user: User,
    userViewModel: UserViewModel,
    fileViewModel: FileViewModel,
    isFollowerSheetOfCurrentUser: Boolean = false,
    onClick: () -> Unit,
) {
  Row(
      modifier = Modifier.padding(8.dp).testTag("userItem").clickable { onClick() },
  ) {
    ThumbnailPic(user, fileViewModel)
    Column(
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.padding(horizontal = 10.dp).weight(1f)) {
          // Display the user's full name and handle (username)
          Text(
              user.fullName(),
              style = Typography.bodyLarge,
              fontWeight = FontWeight(500),
              modifier = Modifier.alpha(0.9f),
              maxLines = 1,
              overflow = TextOverflow.Ellipsis)
          Text(user.userHandle(), style = Typography.bodyLarge, modifier = Modifier.alpha(0.7f))
        }
    if (isFollowerSheetOfCurrentUser) {
      RemoveFollowerButton(userViewModel, user.uid)
    } else if (user.uid != userViewModel.currentUser.collectAsState().value!!.uid) {
      FollowUnfollowButton(userViewModel, user.uid)
    }
  }
}

/**
 * Switches the profile to the specified user. If the user is the current user, navigates to the
 * user's profile. Otherwise, sets the profile user and navigates to the public profile.
 *
 * @param user The user to switch the profile to.
 * @param userViewModel The ViewModel for the user.
 * @param navigationActions The navigation actions.
 */
fun switchProfileTo(
    user: User,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions
) {
  if (user.uid == userViewModel.currentUser.value?.uid) {
    navigationActions.navigateTo(TopLevelDestinations.PROFILE) // clears backstack
  } else {
    userViewModel.setProfileUser(user)
    // Add the visited user profile to the screen navigation stack
    navigationActions.pushToScreenNavigationStack(user.uid)
    navigationActions.navigateTo(Screen.PUBLIC_PROFILE)
  }
}

/**
 * Displays the user's bio in an OutlinedCard.
 *
 * @param user The user whose bio is to be displayed.
 */
@Composable
fun DisplayBioCard(user: State<User?>) {
  OutlinedCard {
    Text(
        buildAnnotatedString {
          withStyle(style = SpanStyle(fontWeight = FontWeight(500), fontSize = 15.sp)) {
            append(stringResource(R.string.bio))
          }
          // Add the user's bio in italics
          withStyle(style = SpanStyle(fontStyle = FontStyle.Italic, fontSize = 14.sp)) {
            append(user.value!!.bio)
          }
        },
        modifier = Modifier.padding(10.dp).testTag("userBio"))
  }
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
fun ThumbnailPic(user: User, fileViewModel: FileViewModel, size: Int = 40) {
  val profilePictureUri = remember { mutableStateOf("") }
  val userState = remember { mutableStateOf(user) }
  NonModifiableProfilePicture(
      userState, profilePictureUri, fileViewModel, size, "thumbnail--${user.uid}")
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
    testTag: String = "profilePicture"
) {
  Box(modifier = Modifier.size(size.dp)) {
    // Download the profile picture from Firebase Storage if it hasn't been downloaded yet
    if (user.value!!.hasProfilePicture && profilePictureUri.value.isBlank()) {
      fileViewModel.downloadFile(
          user.value!!.uid,
          FileType.PROFILE_PIC_JPEG,
          context = LocalContext.current,
          onSuccess = { file -> profilePictureUri.value = file.absolutePath })
    }

    // Profile Picture Painter
    val painter =
        if (user.value!!.hasProfilePicture && profilePictureUri.value.isNotBlank()) {
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

/**
 * Displays a logout button.
 *
 * @param onClick The action to be performed when the button is clicked.
 */
@Composable
fun LogoutButton(onClick: () -> Unit) {
  Button(
      onClick = onClick,
      modifier = Modifier.testTag("logoutButton"),
      colors =
          ButtonDefaults.buttonColors(
              containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.error),
      content = {
        Icon(imageVector = Icons.AutoMirrored.Outlined.ExitToApp, contentDescription = "Logout")
      })
}
