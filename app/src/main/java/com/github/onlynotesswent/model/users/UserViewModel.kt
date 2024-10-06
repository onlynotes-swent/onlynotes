package com.github.onlynotesswent.model.users

import androidx.lifecycle.ViewModel

/**
 * ViewModel for managing user data.
 *
 * @property repository The repository for managing user data.
 */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

    /**
     * Initializes the UserViewModel and the repository.
     */
    init {
        repository.init {  }
    }

    /**
     * Generates a new unique ID for a user.
     *
     * @return A new unique user ID.
     */
    fun getNewUid(): String {
        return repository.getNewUid()
    }

    /**
     * Adds a new user to the repository.
     *
     * @param user The user to add.
     * @param onSuccess Callback to be invoked when the addition is successful.
     * @param onFailure Callback to be invoked if an error occurs.
     */
    fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.addUser(
            user,
            onSuccess,
            onFailure
        )
    }

    /**
     * Updates the information of an existing user.
     *
     * @param user The user with updated information.
     * @param onSuccess Callback to be invoked when the update is successful.
     * @param onFailure Callback to be invoked if an error occurs.
     */
    fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.updateUser(
            user,
            onSuccess,
            onFailure
        )
    }

    /**
     * Retrieves all users from the repository.
     *
     * @param onSuccess Callback to be invoked with the list of retrieved users.
     * @param onFailure Callback to be invoked if an error occurs.
     */
    fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        repository.getUsers(
            onSuccess,
            onFailure
        )
    }

    /**
     * Retrieves a user by their ID.
     *
     * @param id The ID of the user to retrieve.
     * @param onSuccess Callback to be invoked with the retrieved user.
     * @param onFailure Callback to be invoked if an error occurs.
     */
    fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
        repository.getUserById(
            id,
            onSuccess,
            onFailure
        )
    }

    /**
     * Deletes a user by their ID.
     *
     * @param id The ID of the user to delete.
     * @param onSuccess Callback to be invoked when the deletion is successful.
     * @param onFailure Callback to be invoked if an error occurs.
     */
    fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        repository.deleteUserById(
            id,
            onSuccess,
            onFailure
        )
    }

}