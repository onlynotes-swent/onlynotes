package com.github.onlynotesswent.model.user

import com.github.onlynotesswent.model.flashcard.UserFlashcard
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
   * Retrieves a user flashcard by its ID.
   *
   * @param userID The ID of the user to whom the flashcard belongs.
   * @param flashcardId The ID of the flashcard to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved user flashcard.
   * @param onFlashcardNotFound Callback to be invoked if the user flashcard is not found.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserFlashcard(
      userID: String,
      flashcardId: String,
      onSuccess: (UserFlashcard) -> Unit,
      onFlashcardNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all user flashcards from the current User.
   *
   * @param userID The ID of the user to whom the flashcard belongs.
   * @param onSuccess Callback to be invoked with the list of retrieved user flashcards.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getAllUserFlashcards(
      userID: String,
      onSuccess: (List<UserFlashcard>) -> Unit,
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
}
