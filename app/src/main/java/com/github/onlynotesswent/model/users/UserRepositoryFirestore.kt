package com.github.onlynotesswent.model.users

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A repository for managing user data in Firestore.
 *
 * @property db The Firestore database instance.
 */
class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  class UsernameTakenException : Exception("Username already taken")

  private val collectionPath = "users"

  /**
   * Converts a Firestore DocumentSnapshot to a User object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted User object.
   */
  fun documentSnapshotToUser(document: DocumentSnapshot): User {
    return User(
        firstName = document.getString("firstName") ?: "",
        lastName = document.getString("lastName") ?: "",
        userName = document.getString("userName") ?: "",
        email = document.getString("email") ?: "",
        uid = document.getString("uid") ?: "",
        dateOfJoining = document.getTimestamp("dateOfJoining") ?: Timestamp.now(),
        rating = document.getDouble("rating") ?: 0.0)
  }

  override fun init(auth: FirebaseAuth, onSuccess: () -> Unit) {
    if (auth.currentUser != null) {
      onSuccess()
    }
  }

  override fun getUserById(
      id: String,
      onSuccess: (User) -> Unit,
      onUserNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          if (!document.exists()) onUserNotFound() else onSuccess(documentSnapshotToUser(document))
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUserByEmail(
      email: String,
      onSuccess: (User) -> Unit,
      onUserNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("email", email)
        .get()
        .addOnSuccessListener { result ->
          if (result.isEmpty) onUserNotFound()
          else onSuccess(documentSnapshotToUser(result.documents[0]))
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(user.uid)
        .set(user)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .whereEqualTo("userName", user.userName)
        .get()
        .addOnSuccessListener { result ->
          if (result.isEmpty) {
            db.collection(collectionPath)
                .document(user.uid)
                .set(user)
                .addOnSuccessListener { onSuccess() }
                .addOnFailureListener { exception -> onFailure(exception) }
          } else onFailure(UsernameTakenException())
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { result ->
          val users = result.documents.map { document -> documentSnapshotToUser(document) }
          onSuccess(users)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }
}
