package com.github.onlynotesswent.model.user

import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.flashcard.UserFlashcard
import com.github.onlynotesswent.model.user.UserRepositoryFirestore.SavedDocumentType
import com.google.firebase.auth.FirebaseAuth

/** Interface representing a repository for managing user data. */
interface UserRepository {

  /**
   * Initializes the repository.
   *
   * @param auth The FirebaseAuth instance to use for initialization.
   * @param onSuccess Callback to be invoked when initialization is successful.
   */
  fun init(auth: FirebaseAuth, onSuccess: () -> Unit)

  /**
   * Retrieves a user by their ID.
   *
   * @param id The ID of the user to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved user.
   * @param onUserNotFound Callback to be invoked if the user is not found.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserById(
      id: String,
      onSuccess: (User) -> Unit,
      onUserNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves a user by their email address.
   *
   * @param email The email address of the user to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved user.
   * @param onUserNotFound Callback to be invoked if the user is not found.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserByEmail(
      email: String,
      onSuccess: (User) -> Unit,
      onUserNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates the information of an existing user.
   *
   * @param user The user with updated information.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a user by their ID.
   *
   * @param id The ID of the user to delete.
   * @param onSuccess Callback to be invoked when the deletion is successful.
   * @param onUserNotFound Callback to be invoked if the user is not found.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteUserById(
      id: String,
      onSuccess: () -> Unit,
      onUserNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Adds a new user to the repository. The user must have a unique username and uid.
   *
   * @param user The user to add.
   * @param onSuccess Callback to be invoked when the addition is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all users from the repository.
   *
   * @param onSuccess Callback to be invoked with the list of retrieved users.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Generates a new unique ID for a user.
   *
   * @return A new unique user ID.
   */
  fun getNewUid(): String

  /**
   * If is request is true, adds a follower to a specified user as a request. Otherwise, adds a
   * follower to a specified user.
   *
   * @param user The ID of the user to whom the follower is to be added.
   * @param follower The ID of the follower to be added.
   * @param isRequest Whether we add a follow request or a follower.
   * @param onSuccess Callback to be invoked when the operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addFollowerTo(
      user: String,
      follower: String,
      isRequest: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * if is request is true, removes a follow request from a specified user. Otherwise, removes a
   * follower from a specified user.
   *
   * @param user The ID of the user from whom the follower is to be removed.
   * @param follower The ID of the follower to be removed.
   * @param isRequest whether we remove a follow request or a follower.
   * @param onSuccess Callback to be invoked when the operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun removeFollowerFrom(
      user: String,
      follower: String,
      isRequest: Boolean,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves a list of users by their IDs.
   *
   * @param userIDs The list of user IDs to retrieve.
   * @param onSuccess Callback to be invoked with the list of retrieved users.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUsersById(
      userIDs: List<String>,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Adds a new user UserFlashcard to the user flashcard collection.
   *
   * @param userID The ID of the user to whom the flashcard belongs.
   * @param userFlashcard The user flashcard to added.
   * @param onSuccess Callback to be invoked when the addition is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addUserFlashcard(
      userID: String,
      userFlashcard: UserFlashcard,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Updates the information of an existing user flashcard.
   *
   * @param userID The ID of the user to whom the flashcard belongs.
   * @param userFlashcard The user flashcard with updated information.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateUserFlashcard(
      userID: String,
      userFlashcard: UserFlashcard,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a user flashcard by its ID.
   *
   * @param userID The ID of the user to whom the flashcard belongs.
   * @param flashcardId The ID of the user flashcard to delete.
   * @param onSuccess Callback to be invoked when the deletion is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteUserFlashcardById(
      userID: String,
      flashcardId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all user flashcards from the current User from a specific deck.
   *
   * @param userID The ID of the user to whom the flashcard belongs.
   * @param deck The deck from which to retrieve the flashcards.
   * @param onSuccess Callback to be invoked with the list of retrieved user flashcards.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserFlashcardFromDeck(
      userID: String,
      deck: Deck,
      onSuccess: (Map<String, UserFlashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Sets the saved document ids list of the given type for the current user.
   *
   * @param currentUserID The ID of the current user.
   * @param documentIds The new saved document ids list.
   * @param documentType The type of the document to add to the saved document list.
   * @param onSuccess Callback to be invoked when the addition is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun setSavedDocumentIdsOfType(
      currentUserID: String,
      documentIds: List<String>,
      documentType: SavedDocumentType,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all saved document IDs of the given type from the current User.
   *
   * @param currentUserID The ID of the current user.
   * @param documentType The type of the saved document to retrieve.
   * @param onSuccess Callback to be invoked with the list of retrieved saved document ids.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getSavedDocumentsIdOfType(
      currentUserID: String,
      documentType: SavedDocumentType,
      onSuccess: (List<String>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Deletes a saved document id of the given type from the user's saved document list.
   *
   * @param currentUserID The ID of the current user.
   * @param documentIds The ID of the document to delete from the saved document list.
   * @param documentType The type of the document to delete from the saved document list.
   * @param onSuccess Callback to be invoked when the deletion is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteSavedDocumentIdsOfType(
      currentUserID: String,
      documentIds: List<String>,
      documentType: SavedDocumentType,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit
  )
}
