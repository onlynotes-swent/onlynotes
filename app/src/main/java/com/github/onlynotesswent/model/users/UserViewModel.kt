package com.github.onlynotesswent.model.users

import android.util.Log
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

  private val _allUsers = MutableStateFlow<List<User>>(emptyList())
  val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _profileUser = MutableStateFlow<User?>(null)
  val profileUser: StateFlow<User?> = _profileUser.asStateFlow()

  /** Exception thrown when a user is not logged in. */
  class UserNotLoggedInException : Exception("User Not Logged In")

  /** Initializes the UserViewModel and the repository. */
  init {
    repository.init(FirebaseAuth.getInstance()) { getAllUsers() }
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
   * Sets the profile user to the specified user, used for displaying public profiles.
   *
   * @param user The user to set as the profile user.
   */
  fun setProfileUser(user: User) {
    _profileUser.value = user
  }

  /**
   * Refreshes the profile user to the specified user, used for displaying public profiles.
   *
   * @param uid The UID of the user to set as the profile user.
   * @param onSuccess The function to call when the refresh is successful.
   * @param onUserNotFound The function to call when the user is not found.
   * @param onFailure The function to call when the refresh fails.
   */
  fun refreshProfileUser(
      uid: String,
      onSuccess: () -> Unit = {},
      onUserNotFound: () -> Unit = {},
      onFailure: () -> Unit = {}
  ) {
    repository.getUserById(
        id = uid,
        onSuccess = {
          onSuccess()
          _profileUser.value = it
        },
        onUserNotFound = {
          onUserNotFound()
          _profileUser.value = null
        },
        onFailure = {
          onFailure()
          _profileUser.value = null
        })
  }

  /**
   * Refreshes the current user by fetching the latest user data from the repository. If the current
   * user is not null, it retrieves the user by their UID and updates the current user state. If the
   * user is not found or an error occurs, the current user state is set to null.
   *
   * @param onSuccess The function to call when the refresh is successful.
   * @param onUserNotFound The function to call when the user is not found.
   * @param onFailure The function to call when the refresh fails.
   */
  fun refreshCurrentUser(
      onSuccess: () -> Unit = {},
      onUserNotFound: () -> Unit = {},
      onFailure: () -> Unit = {}
  ) {
    _currentUser.value?.let { user ->
      repository.getUserById(
          id = user.uid,
          onSuccess = { _currentUser.value = it },
          onUserNotFound = {
            onUserNotFound()
            _currentUser.value = null
          },
          onFailure = {
            onFailure()
            _currentUser.value = null
          })
    }
  }

  /**
   * Adds a new user to the repository, and sets the current user to the added user.
   *
   * @param user The user to add.
   * @param onSuccess function to call when the user is successfully added.
   * @param onFailure function to call when the adding fails.
   */
  fun addUser(user: User, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
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
   * @param onSuccess function to call when the user is successfully updated.
   * @param onFailure function to call when the updating fails.
   */
  fun updateUser(user: User, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.updateUser(
        user,
        {
          _currentUser.value = user
          onSuccess()
        },
        onFailure)
  }

  /**
   * Retrieves all users from the repository and sets the all users state to the retrieved users.
   */
  fun getAllUsers() {
    repository.getAllUsers(
        { _allUsers.value = it }, { Log.e("UserViewModel", "Failed to get all users", it) })
  }

  /**
   * Retrieves a user by their email, and sets the current user to the retrieved user.
   *
   * @param email The email of the user to retrieve.
   * @param onSuccess function to call when the user is successfully retrieved.
   * @param onUserNotFound function to call when the user is not found.
   * @param onFailure function to call when the retrieval fails.
   */
  fun getCurrentUserByEmail(
      email: String,
      onSuccess: (User) -> Unit = {},
      onUserNotFound: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
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
   * @param onSuccess function to call when the user is successfully retrieved.
   * @param onUserNotFound function to call when the user is not found.
   * @param onFailure function to call when the retrieval fails.
   */
  fun getUserById(
      id: String,
      onSuccess: (User) -> Unit = {},
      onUserNotFound: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getUserById(id, onSuccess, onUserNotFound, onFailure)
  }

  /**
   * Deletes a user by their ID.
   *
   * @param id The ID of the user to delete.
   * @param onSuccess function to call when the user is successfully deleted.
   * @param onFailure function to call when the deletion fails.
   */
  fun deleteUserById(id: String, onSuccess: () -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.deleteUserById(id, onSuccess, onFailure)
  }

  // SOCIAL FUNCTIONS:
  /**
   * Make current user follow the specified user. The method changes both the current user's
   * following list and the specified user's followers list. If the current user is not logged in,
   * the onFailure callback is invoked with a UserNotLoggedInException.
   *
   * @param followingUID The UID of the user to follow.
   * @param onSuccess function to call when the follow operation is successful.
   * @param onFailure function to call if the follow operation fails.
   */
  fun followUser(
      followingUID: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    if (_currentUser.value == null) {
      onFailure(UserNotLoggedInException())
      return
    }
    repository.addFollowerTo(
        user = followingUID,
        follower = _currentUser.value!!.uid,
        {
          refreshCurrentUser()
          onSuccess()
        },
        onFailure)
  }

  /**
   * Make current user unfollow the specified user. The method changes both the current user's
   * following list and the specified user's followers list. If the current user is not logged in,
   * the onFailure callback is invoked with a UserNotLoggedInException.
   *
   * @param followingUID The UID of the user to unfollow.
   * @param onSuccess function to call when the unfollow operation is successful.
   * @param onFailure function to call if the unfollow operation fails.
   */
  fun unfollowUser(
      followingUID: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    if (_currentUser.value == null) {
      onFailure(UserNotLoggedInException())
      return
    }
    repository.removeFollowerFrom(
        user = followingUID,
        follower = _currentUser.value!!.uid,
        {
          refreshCurrentUser()
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
   * @param onSuccess function to call when the followers are successfully retrieved.
   * @param onFailure function to call if the retrieval fails.
   */
  fun getFollowersFrom(
      userID: String,
      onSuccess: (List<User>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getUserById(
        id = userID,
        onSuccess = { user ->
          repository.getUsersById(user.friends.followers, onSuccess, onFailure)
        },
        onUserNotFound = { onSuccess(emptyList()) },
        onFailure = onFailure)
  }

  /**
   * Retrieves the list of users that a specified user is following. If only a list of following IDs
   * of the current user is wanted (not List<User>), access directly the friends.following property
   * of the current user.
   *
   * @param userID The ID of the user whose following list is to be retrieved.
   * @param onSuccess function to call when the following list is successfully retrieved.
   * @param onFailure function to call if the retrieval fails.
   */
  fun getFollowingFrom(
      userID: String,
      onSuccess: (List<User>) -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.getUserById(
        id = userID,
        onSuccess = { user ->
          repository.getUsersById(user.friends.following, onSuccess, onFailure)
        },
        onUserNotFound = { onSuccess(emptyList()) },
        onFailure = onFailure)
  }
}
