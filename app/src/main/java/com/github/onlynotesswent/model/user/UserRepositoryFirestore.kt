package com.github.onlynotesswent.model.user

import android.util.Log
import com.github.onlynotesswent.model.flashcard.UserFlashcard
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

/**
 * A repository for managing user data in Firestore.
 *
 * @property db The Firestore database instance.
 */
class UserRepositoryFirestore(private val db: FirebaseFirestore) : UserRepository {

  class UsernameTakenException : Exception("Username already taken")

  private val collectionPath = "users"

  private val flashcardLevelSubcollection = "flashcardLevel"
  /**
   * Converts a Firestore DocumentSnapshot to a User object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted User object.
   */
  fun documentSnapshotToUser(document: DocumentSnapshot): User? {
    return try {
      User(
          firstName = document.getString("firstName")!!,
          lastName = document.getString("lastName")!!,
          userName = document.getString("userName")!!,
          email = document.getString("email")!!,
          uid = document.getString("uid")!!,
          dateOfJoining = document.getTimestamp("dateOfJoining")!!,
          rating = document.getDouble("rating")!!,
          friends =
              Friends(
                  following = document.get("friends.following") as List<String>,
                  followers = document.get("friends.followers") as List<String>,
              ),
          pendingFriends =
              Friends(
                  following = document.get("pendingFriends.following") as List<String>,
                  followers = document.get("pendingFriends.followers") as List<String>,
              ),
          hasProfilePicture = document.getBoolean("hasProfilePicture")!!,
          bio = document.getString("bio")!!,
          isAccountPublic = document.getBoolean("isAccountPublic")!!)
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to User", e)
      null
    }
  }

  fun documentSnapshotToUserFlashcard(document: DocumentSnapshot): UserFlashcard? {
    return try {
      UserFlashcard(
          lastReviewed = document.getTimestamp("lastReviewed")!!,
          level = document.getLong("level")!!.toInt(),
          id = document.getString("id")!!,
      )
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to UserFlashcard", e)
      null
    }
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
          if (!document.exists()) {
            onUserNotFound()
            Log.e(TAG, "User not found by id")
          } else {
            val user = documentSnapshotToUser(document)
            if (user == null) onFailure(Exception("Error converting document to User"))
            else onSuccess(user)
          }
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting user by id", exception)
        }
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
          if (result.isEmpty) {
            onUserNotFound()
            Log.e(TAG, "User not found by email")
          } else {
            val user = documentSnapshotToUser(result.documents[0])
            if (user == null) onFailure(Exception("Error converting document to User"))
            else onSuccess(user)
          }
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting user by email", exception)
        }
  }

  override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .whereEqualTo("userName", user.userName)
        .whereNotEqualTo("uid", user.uid)
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error updating user", exception)
        }
  }

  override fun deleteUserById(
      id: String,
      onSuccess: () -> Unit,
      onUserNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    getUserById(
        id = id,
        onSuccess = { user ->
          // for each of the current user follower remove them from their following list
          user.friends.followers.forEach { follower ->
            db.collection(collectionPath)
                .document(follower)
                .update("friends.following", FieldValue.arrayRemove(id))
          }
          // for each of the current user following remove them from their follower list
          user.friends.following.forEach { following ->
            db.collection(collectionPath)
                .document(following)
                .update("friends.followers", FieldValue.arrayRemove(id))
          }

          db.collection(collectionPath)
              .document(id)
              .delete()
              .addOnSuccessListener { onSuccess() }
              .addOnFailureListener { exception ->
                onFailure(exception)
                Log.e(TAG, "Error deleting user by id", exception)
              }
        },
        onUserNotFound = onUserNotFound,
        onFailure = onFailure)
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error adding user", exception)
        }
  }

  override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .get()
        .addOnSuccessListener { result ->
          val users = result.documents.mapNotNull { document -> documentSnapshotToUser(document) }
          onSuccess(users)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting all users", exception)
        }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun addFollowerTo(
      user: String,
      follower: String,
      isRequest: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val type: String = if (isRequest) "pendingFriends" else "friends"
    db.collection(collectionPath)
        .document(user)
        .update("$type.followers", FieldValue.arrayUnion(follower))
        .addOnSuccessListener {
          db.collection(collectionPath)
              .document(follower)
              .update("$type.following", FieldValue.arrayUnion(user))
              .addOnSuccessListener { onSuccess() }
              .addOnFailureListener { exception ->
                onFailure(exception)
                Log.e(TAG, "Error adding follower to user", exception)
              }
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error adding follower to user", exception)
        }
  }

  override fun removeFollowerFrom(
      user: String,
      follower: String,
      isRequest: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val type: String = if (isRequest) "pendingFriends" else "friends"
    db.collection(collectionPath)
        .document(user)
        .update("$type.followers", FieldValue.arrayRemove(follower))
        .addOnSuccessListener {
          db.collection(collectionPath)
              .document(follower)
              .update("$type.following", FieldValue.arrayRemove(user))
              .addOnSuccessListener { onSuccess() }
              .addOnFailureListener { exception ->
                onFailure(exception)
                Log.e(TAG, "Error removing follower from user", exception)
              }
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error removing follower from user", exception)
        }
  }

  override fun getUsersById(
      userIDs: List<String>,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (userIDs.isEmpty()) {
      onSuccess(emptyList())
      return
    }
    db.collection(collectionPath)
        .whereIn("uid", userIDs)
        .get()
        .addOnSuccessListener { result ->
          val users = result.documents.mapNotNull { document -> documentSnapshotToUser(document) }
          onSuccess(users)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting users by id", exception)
        }
  }

  override fun addUserFlashcard(
      userID: String,
      userFlashcard: UserFlashcard,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userID)
        .collection(flashcardLevelSubcollection)
        .document(userFlashcard.id)
        .set(userFlashcard)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error adding user flashcard", exception)
        }
  }

  override fun updateUserFlashcard(
      userID: String,
      userFlashcard: UserFlashcard,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userID)
        .collection(flashcardLevelSubcollection)
        .document(userFlashcard.id)
        .set(userFlashcard)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error updating user flashcard", exception)
        }
  }

  override fun deleteUserFlashcardById(
      userID: String,
      flashcardId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userID)
        .collection(flashcardLevelSubcollection)
        .document(flashcardId)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error deleting user flashcard by id", exception)
        }
  }

  override fun getUserFlashcardFromDeck(
      userID: String,
      deck: Deck,
      onSuccess: (Map<String, UserFlashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(userID)
        .collection(flashcardLevelSubcollection)
        .whereIn("uid", deck.flashcardIds)
        .get()
        .addOnSuccessListener { result ->
          val userFlashcards =
              result.documents.mapNotNull { document -> documentSnapshotToUserFlashcard(document) }
          val userFlashcardsMap = userFlashcards.associateBy { it.id }
          onSuccess(userFlashcardsMap)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting user flashcards by deck", exception)
        }
  }

  companion object {
    const val TAG = "UserRepositoryFirestore"
  }
}
