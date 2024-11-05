package com.github.onlynotesswent.ui.user

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations

// User Profile Home screen:
@Composable
fun UserProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val user = userViewModel.currentUser.collectAsState()
  // Display the user's profile information
  ProfileScaffold(
      navigationActions,
      includeBackButton = false,
      topBarTitle = "    My Profile",
      floatingActionButton = {
        ExtendedFloatingActionButton(
            modifier = Modifier.testTag("editProfileButton"),
            onClick = { navigationActions.navigateTo(Screen.EDIT_PROFILE) }) {
              Row {
                Icon(Icons.Default.Create, contentDescription = "Edit Profile")
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Profile")
              }
            }
      }) {
        ProfileContent(user, userViewModel, navigationActions)

        Button(onClick = { userViewModel.followUser("8I0wWmmzGk1H89gUwIOS", {}, {}) }) {
          Text("[Debug] Follow Roshan")
        }
        Button(onClick = { userViewModel.unfollowUser("8I0wWmmzGk1H89gUwIOS", {}, {}) }) {
          Text("[Debug] Unfollow Roshan")
        }
        Button(onClick = { userViewModel.followUser("BNTMNZjylKMVwYV7DuVi", {}, {}) }) {
          Text("[Debug] Follow matthieu")
        }
        Button(onClick = { userViewModel.unfollowUser("BNTMNZjylKMVwYV7DuVi", {}, {}) }) {
          Text("[Debug] Unfollow matthieu")
        }
      }
}

// Other users' Profile Home screen:
@Composable
fun PublicProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val currentUser = userViewModel.currentUser.collectAsState()
  val profileUser = userViewModel.profileUser.collectAsState()
  val followButtonText = remember { mutableStateOf("Follow") }

  // Display the user's profile information
  ProfileScaffold(navigationActions) {
    ProfileContent(profileUser, userViewModel, navigationActions)
    if (profileUser.value != null && currentUser.value != null) {
      followButtonText.value =
          if (profileUser.value!!.friends.followers.contains(currentUser.value!!.uid)) "Unfollow"
          else "Follow"
      FollowUnfollowButton(userViewModel, followButtonText)
    }
  }
}

@Composable
private fun ProfileScaffold(
    navigationActions: NavigationActions,
    includeBackButton: Boolean = true,
    topBarTitle: String = "Public Profile",
    floatingActionButton: @Composable () -> Unit = {},
    content: @Composable () -> Unit,
) {
  Scaffold(
      modifier = Modifier.testTag("profileScaffold"),
      floatingActionButton = floatingActionButton,
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      topBar = {
        TopProfileBar(title = topBarTitle, navigationActions = navigationActions, includeBackButton)
      },
      content = { paddingValues -> ProfileColumn(paddingValues) { content() } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopProfileBar(
    title: String,
    navigationActions: NavigationActions,
    includeBackButton: Boolean = true
) {
  TopAppBar(
      title = { Text(title) },
      navigationIcon = {
        if (includeBackButton) {
          IconButton(onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
            Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
          }
        }
      })
}

@Composable
fun ProfileColumn(paddingValues: PaddingValues, content: @Composable () -> Unit = {}) {
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
}

@Composable
fun ProfileContent(
    user: State<User?>,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions
) {
  val isFollowingMenuShown = remember { mutableStateOf(false) }
  val isFollowerMenuShown = remember { mutableStateOf(false) }
  val following: MutableState<List<User>> = remember { mutableStateOf(listOf()) }
  val followers: MutableState<List<User>> = remember { mutableStateOf(listOf()) }

  // Display the user's profile information:
  if (user.value == null) {
    Text("User not found", modifier = Modifier.testTag("userNotFound"))
  } else {
    // ADD PROFILE PICTURE HERE
    ElevatedCard(
        // TODO: Aisel - change colors here
        modifier = Modifier.fillMaxSize().padding(40.dp).testTag("profileCard"),
    ) {
      val borderPadding = 20.dp
      Column(
          modifier = Modifier.padding(borderPadding).fillMaxWidth().testTag("profileCardColumn"),
          verticalArrangement = Arrangement.Center,
          horizontalAlignment = Alignment.CenterHorizontally) {

            // Profile picture (change later)
            Image(
                painter = rememberVectorPainter(Icons.Default.AccountCircle),
                contentDescription = "Profile Picture",
                modifier = Modifier.size(200.dp).padding(10.dp).testTag("profilePicture"))
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
              Text("Rating: ${user.value!!.rating}", modifier = Modifier.testTag("userRating"))
            }
            Spacer(modifier = Modifier.height(10.dp))
            Row {
              Icon(Icons.Default.DateRange, "dateIcon")
              Text(
                  "Member Since: ${user.value!!.dateToString()}",
                  modifier = Modifier.testTag("userDateOfJoining"))
            }
            Spacer(modifier = Modifier.height(10.dp))

            // Display the user's bio
            if (user.value!!.bio.isNotEmpty()) {
              DisplayBioCard(user)
              Spacer(modifier = Modifier.height(10.dp))
            }

            // Display the user's friends
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
                        "Following: ${user.value!!.friends.following.size}",
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
                        "Followers: ${user.value!!.friends.followers.size}",
                        modifier = Modifier.testTag("followersText"))
                  }
            }

            UserDropdownMenu(
                isFollowingMenuShown, following, userViewModel, navigationActions, "following")
            UserDropdownMenu(
                isFollowerMenuShown, followers, userViewModel, navigationActions, "followers")
          }
    }
  }
}

@Composable
fun FollowUnfollowButton(userViewModel: UserViewModel, followButtonText: MutableState<String>) {
  OutlinedButton(
      modifier = Modifier.testTag("followUnfollowButton"),
      onClick = {
        if (followButtonText.value == "Follow")
            userViewModel.followUser(
                userViewModel.profileUser.value!!.uid,
                { userViewModel.refreshProfileUser(userViewModel.profileUser.value!!.uid) },
                {})
        else
            userViewModel.unfollowUser(
                userViewModel.profileUser.value!!.uid,
                { userViewModel.refreshProfileUser(userViewModel.profileUser.value!!.uid) },
                {})
      }) {
        Text(followButtonText.value, modifier = Modifier.testTag("followUnfollowButtonText"))
      }
}

@Composable
fun UserDropdownMenu(
    expanded: MutableState<Boolean>,
    users: State<List<User>>,
    userViewModel: UserViewModel,
    navigationActions: NavigationActions,
    tag: String = "user",
) {
  DropdownMenu(
      expanded = expanded.value,
      onDismissRequest = { expanded.value = false },
      modifier =
          Modifier.testTag("${tag}DropdownMenu")
              .fillMaxWidth(0.7f) // Set a fixed width for the dropdown menu
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          if (users.value.isEmpty()) {
            Text("No $tag to display", modifier = Modifier.padding(8.dp).testTag("${tag}Absent"))
          }
          users.value.forEach { user ->
            Text(
                "${user.fullName()} — @${user.userName}",
                modifier =
                    Modifier.testTag("item--${user.userName}")
                        .clickable {
                          expanded.value = false
                          switchProfileTo(user, userViewModel, navigationActions)
                        }
                        .padding(8.dp) // Add padding for better UI
                )
          }
        }
      }
}

@Composable
fun DisplayBioCard(user: State<User?>) {
  OutlinedCard {
    Text(
        buildAnnotatedString {
          withStyle(style = SpanStyle(fontWeight = FontWeight(500), fontSize = 15.sp)) {
            append("Bio: ")
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
 * Switches the profile to the specified user. If the user is the current user, navigates to the
 * user's profile. Otherwise, sets the profile user and navigates to the public profile.
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
    navigationActions.navigateTo(Screen.PUBLIC_PROFILE)
  }
}