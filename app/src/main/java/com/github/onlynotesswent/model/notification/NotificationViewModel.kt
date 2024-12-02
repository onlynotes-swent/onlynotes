package com.github.onlynotesswent.model.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NotificationViewModel(private val repository: NotificationRepository) : ViewModel() {
  private val _userNotifications = MutableStateFlow<List<Notification>>(emptyList())
  val userNotifications: StateFlow<List<Notification>> = _userNotifications.asStateFlow()

  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { NotificationViewModel(NotificationRepositoryFirestore(Firebase.firestore)) }
    }
  }

  /**
   * Retrieves all notifications for a receiver.
   *
   * @param userID The ID of the notification to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved notification.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNotificationByReceiverId(
      userID: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getNotificationByReceiverId(
        receiverId = userID,
        onSuccess = {
          _userNotifications.value = it
          onSuccess()
        },
        onFailure = { onFailure(it) })
  }

  /**
   * Adds a notification.
   *
   * @param notification The notification to added.
   * @param onSuccess Callback to be invoked when the addition is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addNotification(
      notification: Notification,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.addNotification(
        notification = notification, onSuccess = { onSuccess() }, onFailure = { onFailure(it) })
  }

  /**
   * Deletes a notification.
   *
   * @param id The ID of the notification to delete.
   * @param onSuccess Callback to be invoked when the deletion is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteNotification(
      id: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteNotification(
        id = id, onSuccess = { onSuccess() }, onFailure = { onFailure(it) })
  }

  /**
   * Updates a notification.
   *
   * @param notification The notification to update.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateNotification(
      notification: Notification,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.updateNotification(
        notification = notification, onSuccess = { onSuccess() }, onFailure = { onFailure(it) })
  }
}
