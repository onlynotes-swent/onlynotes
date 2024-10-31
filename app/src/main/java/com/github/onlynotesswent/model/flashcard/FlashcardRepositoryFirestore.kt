package com.github.onlynotesswent.model.flashcard

import com.google.firebase.Firebase
import com.google.firebase.Timestamp
import com.google.firebase.auth.auth
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
  fun documentSnapshotToFlashcard(document: DocumentSnapshot): Flashcard {
    return Flashcard(
        id = document.id,
        front = document.getString("front") ?: "",
        back = document.getString("back") ?: "",
        nextReview = document.getTimestamp("nextReview") ?: Timestamp.now(),
        userId = document.getString("userId") ?: "",
        folderId = document.getString("folderId") ?: "")
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
      onSuccess: (Flashcard) -> Unit,
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
