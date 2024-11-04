package com.github.onlynotesswent.ui.user

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.CardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedCard
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.BottomNavigationMenu
import com.github.onlynotesswent.ui.navigation.LIST_TOP_LEVEL_DESTINATION
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import java.text.SimpleDateFormat
import java.util.Locale

// User Profile Home screen:
@Composable
fun UserProfileScreen(navigationActions: NavigationActions, userViewModel: UserViewModel) {
  val user = userViewModel.currentUser.collectAsState()
  // Display the user's profile information
  ProfileScaffold(navigationActions) {
      ProfileContent(user)
      Button(onClick = {navigationActions.navigateTo(Screen.EDIT_PROFILE)}){
          Text("Click to Edit Profile")
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
    ProfileContent(profileUser)
    if (profileUser.value != null && currentUser.value != null) {
      FollowUnfollowButton(userViewModel, followButtonText)
    }
  }
}

@Composable
private fun ProfileScaffold(
    navigationActions: NavigationActions,
    content: @Composable () -> Unit,
) {
  Scaffold(
      modifier = Modifier.testTag("EditProfileScreen"),
      bottomBar = {
        BottomNavigationMenu(
            onTabSelect = { route -> navigationActions.navigateTo(route) },
            tabList = LIST_TOP_LEVEL_DESTINATION,
            selectedItem = navigationActions.currentRoute())
      },
      topBar = { TopProfileBar(title = "Public Profile", navigationActions = navigationActions) },
      content = { paddingValues -> ProfileColumn(paddingValues) { content() } })
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopProfileBar(title: String, navigationActions: NavigationActions) {
  TopAppBar(
      title = { Text(title) },
      navigationIcon = {
        IconButton(onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
          Icon(imageVector = Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "Back")
        }
      })
}

@Composable
fun ProfileColumn(paddingValues: PaddingValues, content: @Composable () -> Unit = {}) {
  Column(
      modifier =
          Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        content()
      }
}

@Composable
fun ProfileContent(user: State<User?>) {
  // Display the user's profile information:
  if (user.value == null) {
    Text("User not found")
  } else {
    // ADD PROFILE PICTURE HERE
      ElevatedCard(
          // Aisel - change colors here
          modifier = Modifier.padding(16.dp),
      ) {
          Spacer(modifier = Modifier.height(5.dp))
          Row {
              Text(
                  "Name: ${user.value!!.firstName} ${user.value!!.lastName}",
                  fontWeight = FontWeight(500)
              )
          }
          Spacer(modifier = Modifier.height(10.dp))
          Text(
              "@${user.value!!.userName}",
              fontWeight = FontWeight(300),
              modifier = Modifier.alpha(0.7f)
          )
          Spacer(modifier = Modifier.height(10.dp))
          Row {
              Icon(Icons.Default.DateRange, "dateIcon")
              Text("Member Since: ${SimpleDateFormat("d/M/yyyy", Locale.FRANCE).format(user.value!!.dateOfJoining.toDate())}")
          }
          Spacer(modifier = Modifier.height(10.dp))
          Row {
              Icon(Icons.Default.Star, "starIcon")
              Text("Rating: ${user.value!!.rating}")
          }
          Spacer(modifier = Modifier.height(5.dp))
      }
    Spacer(modifier = Modifier.height(30.dp))

    // Display the user's friends
    Text("Following: ${user.value!!.friends.following.size}")
    Text("Followers: ${user.value!!.friends.followers.size}")
    Spacer(modifier = Modifier.height(8.dp))
  }
}

@Composable
fun FollowUnfollowButton(userViewModel: UserViewModel, followButtonText: MutableState<String>) {
  OutlinedButton(
      onClick = {
        if (followButtonText.value == "Follow")
            userViewModel.followUser(
                userViewModel.profileUser.value!!.uid, { followButtonText.value = "Unfollow" }, {})
        else
            userViewModel.unfollowUser(
                userViewModel.profileUser.value!!.uid, { followButtonText.value = "Follow" }, {})
      }) {
        Text(followButtonText.value)
      }
}
