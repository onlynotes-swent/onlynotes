package com.github.onlynotesswent.model.notification

interface NotificationRepository {

  /**
   * Initializes the repository.
   *
   * @param onSuccess Callback to be invoked when initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves a notification by its ID.
   *
   * @param id The ID of the notification to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved notification.
   * @param onNotificationNotFound Callback to be invoked if the notification is not found.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNotificationById(
      id: String,
      onSuccess: (Notification) -> Unit,
      onNotificationNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all notifications for a receiver.
   *
   * @param receiverId The ID of the receiver.
   * @param onSuccess Callback to be invoked with the retrieved notifications.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNotificationByReceiverId(
      receiverId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Adds a notification.
   *
   * @param notification The notification to add.
   * @param onSuccess Callback to be invoked when the addition is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates a notification.
   *
   * @param notification The notification to update.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a notification by its ID.
   *
   * @param id The ID of the notification to delete.
   * @param onSuccess Callback to be invoked when the deletion is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteNotification(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)
}
