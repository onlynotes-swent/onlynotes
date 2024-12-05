package com.github.onlynotesswent.model.notification

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class NotificationRepositoryFirestore(private val db: FirebaseFirestore) : NotificationRepository {

  private val collectionPath = "notifications"

  private fun documentSnapshotToNotification(document: DocumentSnapshot): Notification? {
    return try {
      Notification(
          id = document.getString("id")!!,
          senderId = document.getString("senderId"),
          receiverId = document.getString("receiverId")!!,
          timestamp = document.getTimestamp("timestamp")!!,
          read = document.getBoolean("read")!!,
          type = Notification.NotificationType.valueOf(document.getString("type")!!),
          content = document.getString("content"))
    } catch (e: Exception) {
      Log.e(TAG, "Failed to convert document snapshot to notification", e)
      null
    }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

  override fun getNotificationById(
      id: String,
      onSuccess: (Notification) -> Unit,
      onNotificationNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          if (!document.exists()) onNotificationNotFound()
          else {
            val notification = documentSnapshotToNotification(document)
            if (notification != null) onSuccess(notification)
            else onFailure(Exception("Failed to convert document snapshot to notification"))
          }
        }
        .addOnFailureListener {
          onFailure(it)
          Log.e(TAG, "Failed to get notification by ID", it)
        }
  }

  override fun getNotificationByReceiverId(
      receiverId: String,
      onSuccess: (List<Notification>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("receiverId", receiverId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          onSuccess(querySnapshot.documents.mapNotNull { documentSnapshotToNotification(it) })
        }
        .addOnFailureListener {
          onFailure(it)
          Log.e(TAG, "Failed to get notifications by receiver ID", it)
        }
  }

  override fun addNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(notification.id)
        .set(notification)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener {
          onFailure(it)
          Log.e(TAG, "Failed to add notification", it)
        }
  }

  override fun updateNotification(
      notification: Notification,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(notification.id)
        .set(notification)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener {
          onFailure(it)
          Log.e(TAG, "Failed to update notification", it)
        }
  }

  override fun deleteNotification(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener {
          onFailure(it)
          Log.e(TAG, "Failed to delete notification", it)
        }
  }

  companion object {
    private const val TAG = "NotificationRepositoryFirestore"
  }
}
