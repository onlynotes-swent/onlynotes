package com.github.onlynotesswent.model.users

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import com.google.firebase.firestore.firestore

/**
 * ViewModel for managing user data.
 *
 * @property repository The repository for managing user data.
 */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

  private val currentUser_ = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = currentUser_.asStateFlow()

  /** Initializes the UserViewModel and the repository. */
  init {
    repository.init(FirebaseAuth.getInstance()) {}
    setCurrentUser(FirebaseAuth.getInstance())
  }

  fun setCurrentUser(firebaseAuth: FirebaseAuth) {
    val firebaseUser = firebaseAuth.currentUser
    val email = firebaseUser?.email ?: return
    repository.getUserByEmail(
        email,
        { currentUser_.value = it },
        { e -> Log.e("UserViewModel", "Error getting user", e) })
  }

  companion object {
    val Factory: ViewModelProvider.Factory =
        object : ViewModelProvider.Factory {
          @Suppress("UNCHECKED_CAST")
          override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return UserViewModel(UserRepositoryFirestore(Firebase.firestore)) as T
          }
        }
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
    // add uid here
    repository.addUser(user, onSuccess, onFailure)
  }

  /**
   * Updates the information of an existing user.
   *
   * @param user The user with updated information.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.updateUser(user, onSuccess, onFailure)
  }

  /**
   * Retrieves all users from the repository.
   *
   * @param onSuccess Callback to be invoked with the list of retrieved users.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    repository.getUsers(onSuccess, onFailure)
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param id The ID of the user to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved user.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserByEmail(email: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
    repository.getUserByEmail(email, onSuccess, onFailure)
  }

  /**
   * Retrieves a user by their ID.
   *
   * @param id The ID of the user to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved user.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserById(id: String, onSuccess: (User) -> Unit, onFailure: (Exception) -> Unit) {
    repository.getUserById(id, onSuccess, onFailure)
  }

  /**
   * Deletes a user by their ID.
   *
   * @param id The ID of the user to delete.
   * @param onSuccess Callback to be invoked when the deletion is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteUserById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.deleteUserById(id, onSuccess, onFailure)
  }
}
