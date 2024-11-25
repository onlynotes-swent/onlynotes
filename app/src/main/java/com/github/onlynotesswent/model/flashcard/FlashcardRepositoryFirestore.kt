package com.github.onlynotesswent.model.flashcard

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FlashcardRepositoryFirestore(private val db: FirebaseFirestore) : FlashcardRepository {

  private val collectionPath = "flashcards"

  /**
   * Converts a Firestore DocumentSnapshot to a Flashcard object. Try catch block is used to handle
   * runtime exceptions.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Flashcard object. Returns null if the conversion fails.
   */
  fun documentSnapshotToFlashcard(document: DocumentSnapshot): Flashcard? {
    return try {
      val type =
          Flashcard.Type.fromString(
              document.getString("type") ?: throw Exception("Invalid flashcard type"))
      Flashcard.from(type, document.data?.toMap() ?: throw Exception("Invalid flashcard data"))
    } catch (e: Exception) {
      Log.e("Firestore", "Error converting document to Flashcard", e)
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
        .set(flashcard.toMap())
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
        .set(flashcard.toMap())
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
