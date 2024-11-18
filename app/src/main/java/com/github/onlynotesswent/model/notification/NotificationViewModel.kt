package com.github.onlynotesswent.model.notification

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel(private val repository: NotificationRepository) {
  private val _userNotifications = MutableStateFlow<List<Notification>>(emptyList())
  val userNotifications: StateFlow<List<Notification>> = _userNotifications.asStateFlow()

  fun getNotificationByReceiverId(
      userID: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.getNotificationByReceiverId(
        receiverId = userID,
        onSuccess = {
          _userNotifications.value = it
          onSuccess()
        },
        onFailure = { onFailure(it) })
  }

  fun addNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.addNotification(
        notification = notification, onSuccess = { onSuccess() }, onFailure = { onFailure(it) })
  }

  fun deleteNotification(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.deleteNotification(
        id = id, onSuccess = { onSuccess() }, onFailure = { onFailure(it) })
  }

  fun updateNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.updateNotification(
        notification = notification, onSuccess = { onSuccess() }, onFailure = { onFailure(it) })
  }
}
