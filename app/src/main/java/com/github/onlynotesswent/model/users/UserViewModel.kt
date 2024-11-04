package com.github.onlynotesswent.model.users

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * ViewModel for managing user data.
 *
 * @property repository The repository for managing user data.
 */
class UserViewModel(private val repository: UserRepository) : ViewModel() {

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _profileUser = MutableStateFlow<User?>(null)
  val profileUser: StateFlow<User?> = _profileUser.asStateFlow()

  /** Exception thrown when a user is not logged in. */
  class UserNotLoggedInException : Exception("User Not Logged In")

  /** Initializes the UserViewModel and the repository. */
  init {
    repository.init(FirebaseAuth.getInstance()) {}
  }

  // Create factory
  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer { UserViewModel(UserRepositoryFirestore(Firebase.firestore)) }
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
   * Refreshes the current user by fetching the latest user data from the repository. If the current
   * user is not null, it retrieves the user by their UID and updates the current user state. If the
   * user is not found or an error occurs, the current user state is set to null.
   */
  internal fun refreshUser() {
    _currentUser.value?.let { user ->
      repository.getUserById(
          user.uid,
          onSuccess = { _currentUser.value = it },
          onUserNotFound = { _currentUser.value = null },
          onFailure = { _currentUser.value = null })
    }
  }

  /**
   * Adds a new user to the repository, and sets the current user to the added user.
   *
   * @param user The user to add.
   * @param onSuccess Callback to be invoked when the addition is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.addUser(
        user,
        {
          _currentUser.value = user
          onSuccess()
        },
        onFailure)
  }

  /**
   * Updates the information of the current user locally and in the database.
   *
   * @param user The user with updated information.
   * @param onSuccess Callback to be invoked when the update is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    repository.updateUser(
        user,
        {
          _currentUser.value = user
          onSuccess()
        },
        onFailure)
  }

  /**
   * Retrieves all users from the repository.
   *
   * @param onSuccess Callback to be invoked with the list of retrieved users.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
    repository.getAllUsers(onSuccess, onFailure)
  }

  /**
   * Retrieves a user by their email, and sets the current user to the retrieved user.
   *
   * @param email The email of the user to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved user.
   * @param onUserNotFound Callback to be invoked if the user is not found.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getCurrentUserByEmail(
      email: String,
      onSuccess: (User) -> Unit,
      onUserNotFound: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.getUserByEmail(
        email,
        { usr ->
          _currentUser.value = usr
          onSuccess(usr)
        },
        onUserNotFound,
        onFailure)
  }

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
  ) {
    repository.getUserById(id, onSuccess, onUserNotFound, onFailure)
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

  // SOCIAL FUNCTIONS:
  /**
   * Make current user follow the specified user. The method changes both the current user's
   * following list and the specified user's followers list. If the current user is not logged in,
   * the onFailure callback is invoked with a UserNotLoggedInException.
   *
   * @param followingUID The UID of the user to follow.
   * @param onSuccess Callback to be invoked when the follow operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun followUser(followingUID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (_currentUser.value == null) {
      onFailure(UserNotLoggedInException())
      return
    }
    repository.addFollowerTo(
        user = followingUID,
        follower = _currentUser.value!!.uid,
        {
          refreshUser()
          onSuccess()
        },
        onFailure)
  }

  /**
   * Make current user follow the specified user. The method changes both the current user's
   * following list and the specified user's followers list. If the current user is not logged in,
   * the onFailure callback is invoked with a UserNotLoggedInException.
   *
   * @param followingUID The UID of the user to unfollow.
   * @param onSuccess Callback to be invoked when the unfollow operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun unfollowUser(followingUID: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
    if (_currentUser.value == null) {
      onFailure(UserNotLoggedInException())
      return
    }
    repository.removeFollowerFrom(
        user = followingUID,
        follower = _currentUser.value!!.uid,
        {
          refreshUser()
          onSuccess()
        },
        onFailure)
  }

  /**
   * Retrieves the list of followers for a specified user. If only a list of follower IDs of the
   * current user is wanted (not List<User>), access directly the friends.followers property of the
   * current user.
   *
   * @param userID The ID of the user whose followers are to be retrieved.
   * @param onSuccess Callback to be invoked with the list of retrieved followers.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getFollowersFrom(
      userID: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.getUserById(
        id = userID,
        onSuccess = { usr -> repository.getUsersById(usr.friends.followers, onSuccess, onFailure) },
        onUserNotFound = { onSuccess(emptyList()) },
        onFailure = onFailure)
  }

  /**
   * Retrieves the list of users that a specified user is following. If only a list of following IDs
   * of the current user is wanted (not List<User>), access directly the friends.following property
   * of the current user.
   *
   * @param userID The ID of the user whose following list is to be retrieved.
   * @param onSuccess Callback to be invoked with the list of retrieved users.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getFollowingFrom(
      userID: String,
      onSuccess: (List<User>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    repository.getUserById(
        id = userID,
        onSuccess = { usr -> repository.getUsersById(usr.friends.following, onSuccess, onFailure) },
        onUserNotFound = { onSuccess(emptyList()) },
        onFailure = onFailure)
  }
}
