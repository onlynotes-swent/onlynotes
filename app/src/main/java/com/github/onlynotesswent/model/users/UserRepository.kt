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
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves a user by their email address.
   *
   * @param email The email address of the user to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved user.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserByEmail(email: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit)

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
  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Generates a new unique ID for a user.
   *
   * @return A new unique user ID.
   */
  fun getNewUid(): String
}
