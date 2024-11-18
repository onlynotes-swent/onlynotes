package com.github.onlynotesswent.model.notification

interface NotificationRepository {
  fun init(onSuccess: () -> Unit)

  fun getNotificationById(
      id: String,
      onSuccess: (Notification) -> Unit,
      onNotificationNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun getNotificationByReceiverId(
      receiverId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun addNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun updateNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  fun deleteNotification(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
