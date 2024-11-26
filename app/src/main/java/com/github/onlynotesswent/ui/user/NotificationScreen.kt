package com.github.onlynotesswent.ui.user

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.notification.Notification
import com.github.onlynotesswent.model.notification.NotificationViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ThumbnailPic
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.theme.Typography

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
          val sortedNotification = userNotifications.value.sortedByDescending { it.timestamp }
          items(userNotifications.value.size) { index ->
            // Display each notification
            val notification = sortedNotification[index]
            // for each notifcationType do something:
            when (notification.type) {
              Notification.NotificationType.FOLLOW_REQUEST ->
                  NotificationTypeFollowRequest(
                      notification, fileViewModel, userViewModel, notificationViewModel)
              Notification.NotificationType.FOLLOW_REQUEST_ACCEPTED ->
                  NotificationTypeDefault(
                      notification, fileViewModel, userViewModel, notificationViewModel)
              Notification.NotificationType.FOLLOW_REQUEST_REJECTED ->
                  NotificationTypeDefault(
                      notification, fileViewModel, userViewModel, notificationViewModel)
              Notification.NotificationType.FOLLOW ->
                  NotificationTypeDefault(
                      notification, fileViewModel, userViewModel, notificationViewModel)
              else -> Text("Not an implemented notification type")
            }
            HorizontalDivider()
          }
        }
      }
}

@Composable
fun NotificationTypeDefault(
    notification: Notification,
    fileViewModel: FileViewModel,
    userViewModel: UserViewModel,
    notificationViewModel: NotificationViewModel
) {
  Box(
      modifier =
          Modifier.testTag("notification-${notification.id}")
              .padding(8.dp)
              .alpha(if (notification.read) 0.5f else 1f)) {
        Column {
          Text(notification.title, style = Typography.titleMedium)
          Row {
            if (notification.senderId != null) {
              ThumbnailPic(notification.senderId, userViewModel, fileViewModel)
            }
            Spacer(modifier = Modifier.padding(5.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
              Text(notification.body, style = Typography.bodyLarge)
            }
          }
        }
        if (!notification.read) {
          notificationViewModel.updateNotification(notification.copy(read = true))
        }
      }
}

@Composable
fun NotificationTypeFollowRequest(
    notification: Notification,
    fileViewModel: FileViewModel,
    userViewModel: UserViewModel,
    notificationViewModel: NotificationViewModel
) {
  Box(
      modifier =
          Modifier.testTag("notification-${notification.id}")
              .padding(8.dp)
              .alpha(if (notification.read) 0.5f else 1f)) {
        Column {
          Text(notification.title, style = Typography.titleMedium)
          Row {
            assert(
                notification.senderId !=
                    null) // the senderId should not be null for this type of notification
            ThumbnailPic(notification.senderId!!, userViewModel, fileViewModel)
            Spacer(modifier = Modifier.padding(5.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
              Text(notification.body, style = Typography.bodyLarge)
            }
            if (!notification.read) {
              Button(
                  onClick = {
                    userViewModel.acceptFollowerRequest(notification.senderId)
                    userViewModel.getUserById(
                        notification.senderId,
                        { user ->
                          notificationViewModel.updateNotification(
                              notification.copy(
                                  read = true,
                                  type = Notification.NotificationType.FOLLOW_REQUEST_ACCEPTED,
                                  body = user.userHandle() + " is now following you"))
                        },
                        {},
                        {})
                  }) {
                    Text("Accept", maxLines = 1)
                  }
              Button(
                  onClick = {
                    userViewModel.declineFollowerRequest(notification.senderId)
                    userViewModel.getUserById(
                        notification.senderId,
                        { user ->
                          notificationViewModel.updateNotification(
                              notification.copy(
                                  read = true,
                                  type = Notification.NotificationType.FOLLOW_REQUEST_REJECTED,
                                  body = "you have rejected the request from:" + user.userHandle()))
                        },
                        {},
                        {})
                  }) {
                    Text("Reject", maxLines = 1)
                  }
            }
          }
        }
      }
}
