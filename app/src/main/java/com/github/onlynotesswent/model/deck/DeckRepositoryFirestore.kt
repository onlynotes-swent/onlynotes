package com.github.onlynotesswent.model.deck

import android.util.Log
import com.github.onlynotesswent.model.common.Visibility
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class DeckRepositoryFirestore(private val db: FirebaseFirestore) : DeckRepository {

  private val collectionPath = "decks"

  companion object {
    const val TAG = "DeckRepositoryFirestore"
  }

  /**
   * Converts a Firestore DocumentSnapshot to a Deck object. Try catch block is used to handle
   * runtime exceptions.
   *
   * @param document The DocumentSnapshot to convert.
   * @return The converted Deck object. Returns null if the conversion fails.
   */
  fun documentSnapshotToDeck(document: DocumentSnapshot): Deck? {
    return try {
      Deck(
          id = document.id,
          name = document.getString("name") ?: throw Exception("Invalid deck name"),
          userId = document.getString("userId") ?: throw Exception("Invalid user ID"),
          folderId = document.getString("folderId"),
          flashcardIds = document.get("flashcardIds") as List<String>? ?: emptyList(),
          visibility =
              Visibility.fromString(
                  document.getString("visibility") ?: throw Exception("Invalid deck visibility")),
          lastModified =
              document.getTimestamp("lastModified")
                  ?: throw Exception("Invalid last modified timestamp"),
          description =
              document.getString("description") ?: throw Exception("Invalid deck description"))
    } catch (e: Exception) {
      Log.e(TAG, "Error converting document to Deck", e)
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

  override fun getPublicDecks(onSuccess: (List<Deck>) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .whereEqualTo("visibility", Visibility.PUBLIC.toString())
        .get()
        .addOnSuccessListener { querySnapshot ->
          val decks = querySnapshot.documents.mapNotNull { documentSnapshotToDeck(it) }
          onSuccess(decks)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting public decks", exception)
        }
  }

  override fun getDecksFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Deck>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // The current user is not following anyone
    if (followingListIds.isEmpty()) {
      onSuccess(emptyList())
      return
    }

    db.collection(collectionPath)
        .whereIn("userId", followingListIds)
        .whereEqualTo("visibility", Visibility.FRIENDS.toString())
        .get()
        .addOnSuccessListener { querySnapshot ->
          val decks = querySnapshot.documents.mapNotNull { documentSnapshotToDeck(it) }
          onSuccess(decks)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting decks from following list", exception)
        }
  }

  override fun getDecksFrom(
      userId: String,
      onSuccess: (List<Deck>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val decks = querySnapshot.documents.mapNotNull { documentSnapshotToDeck(it) }
          onSuccess(decks)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting decks from user", exception)
        }
  }

  override fun getRootDecksFromUserId(
      userId: String,
      onSuccess: (List<Deck>) -> Unit,
      onFailure: (Exception) -> Unit,
  ) {
    db.collection(collectionPath)
        .whereEqualTo("userId", userId)
        .whereEqualTo("folderId", null)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val decks = querySnapshot.documents.mapNotNull { documentSnapshotToDeck(it) }
          onSuccess(decks)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting root decks from user", exception)
        }
  }

  override fun getDeckById(id: String, onSuccess: (Deck) -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(id)
        .get()
        .addOnSuccessListener { documentSnapshot ->
          val deck = documentSnapshotToDeck(documentSnapshot)
          if (deck != null) {
            onSuccess(deck)
          } else {
            onFailure(Exception("Deck not found"))
            Log.e(TAG, "Deck not found by ID")
          }
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting deck by ID", exception)
        }
  }

  override fun getDecksByFolder(
      folderId: String,
      onSuccess: (List<Deck>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("folderId", folderId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          val decks = querySnapshot.documents.mapNotNull { documentSnapshotToDeck(it) }
          onSuccess(decks)
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error getting decks by folder", exception)
        }
  }

  override fun updateDeck(deck: Deck, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(deck.id)
        .set(deck)
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error updating deck", exception)
        }
  }

  override fun addFlashcardToDeck(
      deckId: String,
      flashcardId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(deckId)
        .update("flashcardIds", FieldValue.arrayUnion(flashcardId))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error adding flashcard to deck", exception)
        }
  }

  override fun addFlashcardsToDeck(
      deckId: String,
      flashcardIds: List<String>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(deckId)
        .update("flashcardIds", FieldValue.arrayUnion(flashcardIds))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error adding flashcards to deck", exception)
        }
  }

  override fun removeFlashcardFromDeck(
      deckId: String,
      flashcardId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .document(deckId)
        .update("flashcardIds", FieldValue.arrayRemove(flashcardId))
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error removing flashcard from deck", exception)
        }
  }

  override fun deleteDeck(deck: Deck, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    db.collection(collectionPath)
        .document(deck.id)
        .delete()
        .addOnSuccessListener { onSuccess() }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error deleting deck", exception)
        }
  }

  override fun deleteAllDecksFromUserId(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("userId", userId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          querySnapshot.documents.forEach { document ->
            document.reference.delete().addOnFailureListener { exception ->
              onFailure(exception)
              Log.e(TAG, "Error deleting notification", exception)
            }
          }
          onSuccess()
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error deleting all decks from user", exception)
        }
  }

  override fun deleteDecksFromFolder(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    db.collection(collectionPath)
        .whereEqualTo("folderId", folderId)
        .get()
        .addOnSuccessListener { querySnapshot ->
          querySnapshot.documents.forEach { document -> document.reference.delete() }
          onSuccess()
        }
        .addOnFailureListener { exception ->
          onFailure(exception)
          Log.e(TAG, "Error deleting decks from folder", exception)
        }
  }
}
