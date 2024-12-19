package com.github.onlynotesswent.model.flashcard

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class FlashcardRepositoryFirestore(private val db: FirebaseFirestore) : FlashcardRepository {

  private val collectionPath = "flashcards"

  companion object {
    const val TAG = "FlashcardRepositoryFirestore"
  }

  /**
   * Converts a Firestore DocumentSnapshot to a Flashcard object. Try catch block is used to handle
   * runtime exceptions.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Flashcard object. Returns null if the conversion fails.
   */
  fun documentSnapshotToFlashcard(document: DocumentSnapshot): Flashcard? {
    return try {
      Flashcard(
          id = document.id,
          front = document.getString("front") ?: throw Exception("Front is null"),
          back = document.getString("back") ?: throw Exception("Back is null"),
          latexFormula =
              document.getString("latexFormula") ?: throw Exception("Latex formula is null"),
          hasImage = document.getBoolean("hasImage") ?: throw Exception("hasImage is null"),
          fakeBacks =
              document.get("fakeBacks") as List<String>? ?: throw Exception("Fake backs is null"),
          userId = document.getString("userId") ?: throw Exception("User ID is null"),
          folderId = document.getString("folderId"),
          noteId = document.getString("noteId"))
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to Flashcard", e)
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting flashcards from user", exception)
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
          val flashcard = documentSnapshotToFlashcard(document)
          if (flashcard == null) onFailure(Exception("Error converting document to Flashcard"))
          else onSuccess(flashcard)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting flashcard by id", exception)
        }
  }

  override fun getFlashcardsById(
      ids: List<String>,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    if (ids.isEmpty()) {
      onSuccess(emptyList())
      return
    }
    db.collection(collectionPath)
        .whereIn("id", ids)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val flashcards = querySnapshot.documents.mapNotNull { documentSnapshotToFlashcard(it) }
          onSuccess(flashcards)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting flashcards by ids", exception)
        }
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting flashcards by folder", exception)
        }
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting flashcards by note", exception)
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error adding flashcard", exception)
        }
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error updating flashcard", exception)
        }
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
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error deleting flashcard", exception)
        }
  }

  override fun deleteFlashcardsFromUser(
      userID: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("userId", userID)
        .get()
        .addOnSuccessListener { querySnapshot ->
          querySnapshot.documents.forEach { document ->
            db.collection(collectionPath).document(document.id).delete().addOnFailureListener {
                exception ->
              onFailure(exception)
              Log.e(TAG, "Error deleting flashcard", exception)
            }
          }
          onSuccess()
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error deleting flashcard", exception)
        }
  }
}
