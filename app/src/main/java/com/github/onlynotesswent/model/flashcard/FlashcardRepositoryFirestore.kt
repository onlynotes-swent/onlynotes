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
   * Try catch block is used to handle runtime exceptions.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Flashcard object. Returns null if the conversion fails.
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

  override fun getFlashcardsFrom(
      userId: String,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val flashcards = querySnapshot.documents.mapNotNull { documentSnapshotToFlashcard(it) }
          onSuccess(flashcards)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
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
    db.collection(collectionPath)
        .whereEqualTo("folderId", folderId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val flashcards = querySnapshot.documents.mapNotNull { documentSnapshotToFlashcard(it) }
          onSuccess(flashcards)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
  }

  override fun getFlashcardsByNote(
      noteId: String,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("noteId", noteId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val flashcards = querySnapshot.documents.mapNotNull { documentSnapshotToFlashcard(it) }
          onSuccess(flashcards)
        }
        .addOnFailureListener { exception -> onFailure(exception) }
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
