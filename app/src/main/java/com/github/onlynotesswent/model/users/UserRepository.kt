package com.github.onlynotesswent.model.users

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
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

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
   * Adds a follower to a specified user.
   *
   * @param user The ID of the user to whom the follower is to be added.
   * @param follower The ID of the follower to be added.
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
   * Removes a follower from a specified user.
   *
   * @param user The ID of the user from whom the follower is to be removed.
   * @param follower The ID of the follower to be removed.
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
}
