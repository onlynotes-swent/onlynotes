package com.github.onlynotesswent.ui.user

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.R
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.notification.Notification
import com.github.onlynotesswent.model.notification.NotificationViewModel
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ThumbnailDynamicPic
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
      modifier = Modifier.testTag("notificationScreen"),
      topBar = {
        // TopAppBar with title
        TopAppBar(
            title = { Text(stringResource(R.string.your_notifications)) },
            navigationIcon = {
              IconButton(
                  onClick = {
                    navigationActions.goBack()
                    userNotifications.value.forEach { notification ->
                      if (!notification.read &&
                          notification.type != Notification.NotificationType.FOLLOW_REQUEST) {
                        notificationViewModel.updateNotification(notification.copy(read = true))
                      }
                    }
                  },
                  Modifier.testTag("goBackButton")) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                        contentDescription = "Back")
                  }
            })
      }) { innerPadding ->
        LazyColumn(
            contentPadding = innerPadding, modifier = Modifier.testTag("notificationsList")) {
              val sortedNotification = userNotifications.value.sortedByDescending { it.timestamp }
              if (userNotifications.value.isEmpty()) {
                item {
                  Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        modifier = Modifier.align(Alignment.Center),
                        text = stringResource(R.string.no_notifications),
                        style = Typography.titleLarge)
                  }
                }
              } else {
                items(userNotifications.value.size) { index ->
                  // Display each notification
                  val notification = sortedNotification[index]
                  val senderUser: MutableState<User?> = remember { mutableStateOf(null) }

                  if (notification.senderId != null) {
                    userViewModel.getUserById(
                        notification.senderId, { user -> senderUser.value = user }, {}, {})
                  }
                  // for each notifcationType do something:
                  when (notification.type) {
                    Notification.NotificationType.FOLLOW_REQUEST ->
                        NotificationTypeFollowRequest(
                            stringResource(R.string.new_follow_request),
                            stringResource(
                                R.string.want_to_follow_you, senderUser.value?.userHandle() ?: ""),
                            notification,
                            senderUser,
                            navigationActions,
                            fileViewModel,
                            userViewModel,
                            notificationViewModel)
                    Notification.NotificationType.FOLLOW_REQUEST_ACCEPTED ->
                        NotificationTypeDefault(
                            stringResource(R.string.follow_request_accepted),
                            stringResource(
                                R.string.is_now_following_you,
                                senderUser.value?.userHandle() ?: ""),
                            notification,
                            senderUser,
                            navigationActions,
                            fileViewModel,
                            userViewModel,
                            notificationViewModel)
                    Notification.NotificationType.FOLLOW_REQUEST_REJECTED ->
                        NotificationTypeDefault(
                            stringResource(R.string.follow_request_rejected),
                            stringResource(
                                R.string.you_have_rejected_the_follow_request_from,
                                senderUser.value?.userHandle() ?: ""),
                            notification,
                            senderUser,
                            navigationActions,
                            fileViewModel,
                            userViewModel,
                            notificationViewModel)
                    Notification.NotificationType.FOLLOW ->
                        NotificationTypeDefault(
                            stringResource(R.string.new_follower),
                            stringResource(
                                R.string.is_now_following_you,
                                senderUser.value?.userHandle() ?: ""),
                            notification,
                            senderUser,
                            navigationActions,
                            fileViewModel,
                            userViewModel,
                            notificationViewModel)
                    Notification.NotificationType.CHAT_MESSAGE ->
                        NotificationTypeDefault(
                            stringResource(
                                R.string.new_message_from, senderUser.value?.userHandle() ?: ""),
                            // content will never be null for this type of notification
                            notification.content!!,
                            notification,
                            senderUser,
                            navigationActions,
                            fileViewModel,
                            userViewModel,
                            notificationViewModel)
                    else -> Text(stringResource(R.string.not_an_implemented_notification_type))
                  }
                  HorizontalDivider()
                }
              }
            }
      }
}

@Composable
fun NotificationTypeDefault(
    title: String,
    body: String,
    notification: Notification,
    senderUser: MutableState<User?>,
    navigationActions: NavigationActions,
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
          Text(title, style = Typography.titleMedium)
          Row {
            ThumbnailDynamicPic(
                senderUser,
                fileViewModel,
                { senderUser.value?.let { switchProfileTo(it, userViewModel, navigationActions) } })

            Spacer(modifier = Modifier.padding(5.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
              Text(body, style = Typography.bodyLarge)
            }
            DeleteNotificationIcon(notification, notificationViewModel, userViewModel)
          }
        }
      }
}

@Composable
fun NotificationTypeFollowRequest(
    title: String,
    body: String,
    notification: Notification,
    senderUser: MutableState<User?>,
    navigationActions: NavigationActions,
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
          Text(title, style = Typography.titleMedium)
          Row {
            // the senderId should not be null for this type of notification
            assert(notification.senderId != null)
            ThumbnailDynamicPic(
                senderUser,
                fileViewModel,
                { senderUser.value?.let { switchProfileTo(it, userViewModel, navigationActions) } })
            Spacer(modifier = Modifier.padding(5.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.Center) {
              Text(body, style = Typography.bodyLarge)
            }
            Button(
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.primary),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                onClick = {
                  userViewModel.acceptFollowerRequest(notification.senderId!!)
                  notificationViewModel.updateNotification(
                      notification.copy(
                          type = Notification.NotificationType.FOLLOW_REQUEST_ACCEPTED))
                  notificationViewModel.getNotificationByReceiverId(
                      userViewModel.currentUser.value?.uid!!)
                }) {
                  Text(stringResource(R.string.accept), maxLines = 1)
                }
            Spacer(modifier = Modifier.padding(3.dp))
            Button(
                colors =
                    ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        contentColor = MaterialTheme.colorScheme.error),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                onClick = {
                  userViewModel.declineFollowerRequest(notification.senderId!!)
                  notificationViewModel.updateNotification(
                      notification.copy(
                          type = Notification.NotificationType.FOLLOW_REQUEST_REJECTED))
                  notificationViewModel.getNotificationByReceiverId(
                      userViewModel.currentUser.value?.uid!!)
                }) {
                  Text(stringResource(R.string.reject), maxLines = 1)
                }
          }
        }
      }
}

@Composable
fun DeleteNotificationIcon(
    notification: Notification,
    notificationViewModel: NotificationViewModel,
    userViewModel: UserViewModel
) {
  IconButton(
      onClick = {
        notificationViewModel.deleteNotification(notification.id)
        notificationViewModel.getNotificationByReceiverId(userViewModel.currentUser.value?.uid!!)
      }) {
        Icon(imageVector = Icons.Default.DeleteForever, contentDescription = "delete notification")
      }
}
