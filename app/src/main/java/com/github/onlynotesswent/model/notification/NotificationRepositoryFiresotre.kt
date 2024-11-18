package com.github.onlynotesswent.model.notification



import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore

class NotifcationRepositoryFirestore(private val db: FirebaseFirestore) : NotificationRepository {

    private val collectionPath = "notifications"

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
                if (!document.exists()) onNotificationNotFound() else onSuccess(document.toObject(Notification::class.java)!!)
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
                onSuccess(querySnapshot.toObjects(Notification::class.java))
            }
            .addOnFailureListener(onFailure)
    }

    override fun addNotification(
        notification: Notification,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
            db.collection(collectionPath)
            .add(notification)
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
