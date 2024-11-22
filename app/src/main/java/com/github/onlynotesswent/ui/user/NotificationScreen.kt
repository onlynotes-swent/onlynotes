package com.github.onlynotesswent.ui.user

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.notification.NotificationViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationScreen(
    userViewModel: UserViewModel,
    navigationActions: NavigationActions,
    fileViewModel: FileViewModel,
    notificationViewModel: NotificationViewModel
) {
  userViewModel.currentUser.collectAsState().value?.let { user ->
    notificationViewModel.getNotificationByReceiverId(user.uid)
  }
  val userNotifications = notificationViewModel.userNotifications.collectAsState()

  Scaffold(
      topBar = {
        // TopAppBar with title
        TopAppBar(
            title = { Text("Your Notifications") },
            navigationIcon = {
              IconButton(
                  onClick = { navigationActions.goBack() }, Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { innerPadding ->
        LazyColumn(contentPadding = innerPadding) {
          items(userNotifications.value.size) { index ->
            // Display each notification
            val notification = userNotifications.value[index]
            Text(text = notification.title)
          }
        }
      }
}
