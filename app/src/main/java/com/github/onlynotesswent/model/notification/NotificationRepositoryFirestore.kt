package com.github.onlynotesswent.model.notification

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class NotificationRepositoryFirestore(private val db: FirebaseFirestore) : NotificationRepository {

  private val collectionPath = "notifications"

  fun documentSnapshotToNotification(document: DocumentSnapshot): Notification {
    return Notification(
        id = document.getString("id") ?: "",
        title = document.getString("title") ?: "",
        body = document.getString("body") ?: "",
        senderId = document.getString("senderId"),
        receiverId = document.getString("receiverId") ?: "",
        timestamp = document.getTimestamp("timestamp") ?: Timestamp.now(),
        read = document.getBoolean("read") ?: false,
        type = Notification.NotificationType.valueOf(document.getString("type") ?: "DEFAULT"))
  }

  override fun init(onSuccess: () -> Unit) {
    Firebase.auth.addAuthStateListener {
      if (it.currentUser != null) {
        onSuccess()
      }
    }
  }

    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
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
          else onSuccess(documentSnapshotToNotification(document))
        }
        .addOnFailureListener(onFailure)
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
          onSuccess(querySnapshot.documents.map { documentSnapshotToNotification(it) })
        }
        .addOnFailureListener(onFailure)
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
        .addOnFailureListener(onFailure)
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
        .addOnFailureListener(onFailure)
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
        .addOnFailureListener(onFailure)
  }
}
