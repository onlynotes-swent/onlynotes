package com.github.onlynotesswent.model.flashcard

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FlashcardRepositoryFirestore(private val db: FirebaseFirestore) : FlashcardRepository {

  private val collectionPath = "flashcards"

  /**
   * Converts a Firestore DocumentSnapshot to a Flashcard object.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Flashcard object.
   */
  fun documentSnapshotToFlashcard(document: DocumentSnapshot): Flashcard? {
    return try {
      val id = document.id
      val front = document.getString("front") ?: ""
      val back = document.getString("back") ?: ""
      val nextReview = document.getTimestamp("nextReview") ?: Timestamp.now()
      val userId = document.getString("userId") ?: ""
      val folderId = document.getString("folderId") ?: ""
      val noteId = document.getString("noteId") ?: ""
      Flashcard(id, front, back, nextReview, userId, folderId, noteId)
    } catch (e: Exception) {
      Log.e("Firestore", "Error converting document to Flashcard", e)
      null
    }
  }

  override fun getNewUid(): String {
    return db.collection(collectionPath).document().id
  }

  override fun init(auth: FirebaseAuth, onSuccess: () -> Unit) {
    if (auth.currentUser != null) {
      onSuccess()
    }
  }

  override fun getFlashcards(
      userId: String,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).get().addOnCompleteListener { task ->
      if (task.isSuccessful) {
        val userFlashcards =
            task.result
                ?.mapNotNull { document -> documentSnapshotToFlashcard(document) }
                ?.filter { it.userId == userId } ?: emptyList()
        onSuccess(userFlashcards)
      } else {
        onFailure(task.exception ?: Exception("Unknown exception"))
      }
    }
  }

  override fun getFlashcardById(
      id: String,
      onSuccess: (Flashcard?) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { document ->
          if (!document.exists()) onFailure(Exception("Flashcard not found"))
          else onSuccess(documentSnapshotToFlashcard(document))
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getFlashcardsByFolder(
      folderId: String,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath).whereEqualTo("folderId", folderId).get().addOnCompleteListener {
        task ->
      if (task.isSuccessful) {
        val flashcards =
            task.result?.mapNotNull { document -> documentSnapshotToFlashcard(document) }
                ?: emptyList()
        onSuccess(flashcards)
      } else {
        onFailure(task.exception ?: Exception("Unknown exception"))
      }
    }
  }

  override fun addFlashcard(
      flashcard: Flashcard,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(flashcard.id)
        .set(flashcard)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun updateFlashcard(
      flashcard: Flashcard,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(flashcard.id)
        .set(flashcard)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun deleteFlashcard(
      flashcard: Flashcard,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(flashcard.id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception -> onFailure(exception) }
  }
}