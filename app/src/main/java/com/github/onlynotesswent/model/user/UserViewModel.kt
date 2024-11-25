package com.github.onlynotesswent.model.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.notification.Notification
import com.github.onlynotesswent.model.notification.NotificationRepository
import com.github.onlynotesswent.model.notification.NotificationRepositoryFirestore
import com.google.firebase.Firebase
import com.google.firebase.Timestamp
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
class UserViewModel(private val repository: UserRepository,private val notificationRepository: NotificationRepository) : ViewModel() {

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
      initializer { UserViewModel(UserRepositoryFirestore(Firebase.firestore), NotificationRepositoryFirestore(Firebase.firestore)) }
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
   */
  fun refreshProfileUser(uid: String) {
    repository.getUserById(
        uid,
        onSuccess = { _profileUser.value = it },
        onUserNotFound = { _profileUser.value = null },
        onFailure = { _profileUser.value = null })
  }

  /**
   * Refreshes the current user by fetching the latest user data from the repository. If the current
   * user is not null, it retrieves the user by their UID and updates the current user state. If the
   * user is not found or an error occurs, the current user state is set to null.
   */
  fun refreshCurrentUser() {
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
   * request a follow from the specified user. The method changes the current user's pending
   * followers. If the account of the specified user is public, the method will automatically follow
   * else it will send a request.
   *
   * @param followingUID The UID of the user to follow.
   * @param onSuccess Callback to be invoked when the follow operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
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
    getUserById(
        followingUID,
        onSuccess = { user ->
          repository.addFollowerTo(
              user = followingUID,
              follower = _currentUser.value!!.uid,
              // if the account is public this is a follow, if not it is a request
              !user.isAccountPublic,
              {
                refreshCurrentUser()
                onSuccess()
              },
              onFailure)
        },
        onUserNotFound = { onFailure(Exception("User not found")) },
        onFailure = onFailure)

      notificationRepository.addNotification(
          Notification(
               notificationRepository.getNewUid(),
                "New Follow Request",
                _currentUser.value!!. userHandle()+" wants to follow you",
                  _currentUser.value!!.uid,
                 followingUID,
                 Timestamp.now(),
                    false,
              Notification.NotificationType.FOLLOW_REQUEST
            ),
            {},
          { Log.e("UserViewModel", "Failed to send notification", it) }
      )
  }

  /**
   * Make current user unfollow the specified user. The method changes both the current user's
   * following list and the specified user's followers list. If the user is not following the
   * specified user, but has a pending request, the request is cancelled. If the current user is not
   * logged in, the onFailure callback is invoked with a UserNotLoggedInException.
   *
   * @param followingUID The UID of the user to unfollow.
   * @param onSuccess Callback to be invoked when the unfollow operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
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
    // this if else will determine if we need to cancel a request or remove a follow
    val isRequest: Boolean
    if (_currentUser.value!!.pendingFriends.following.contains(followingUID)) {
      isRequest = true
    } else if (_currentUser.value!!.friends.following.contains(followingUID)) {
      isRequest = false
    } else {
      onFailure(Exception("User is not in following list"))
      return
    }

    repository.removeFollowerFrom(
        user = followingUID,
        follower = _currentUser.value!!.uid,
        isRequest,
        {
          refreshCurrentUser()
          onSuccess()
        },
        onFailure)

      if(isRequest){
          notificationRepository.getNotificationByReceiverId(followingUID, {notifications ->
              notifications.forEach({
                  notification ->
                  if(notification.senderId == _currentUser.value!!.uid && notification.type == Notification.NotificationType.FOLLOW_REQUEST) {
                      notificationRepository.deleteNotification(
                          notification.id,
                          {},
                          { Log.e("UserViewModel", "Failed to delete notification", it) })
                  }
              })},{})
      }
  }

  /**
   * remove the specified user from the current user's followers list.
   *
   * @param followerUID The UID of the user to to be removed from the followers list.
   * @param onSuccess Callback to be invoked when the operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun removeFollower(
      followerUID: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    if (_currentUser.value == null) {
      onFailure(UserNotLoggedInException())
      return
    }
    if (_currentUser.value!!.friends.followers.contains(followerUID)) {
      repository.removeFollowerFrom(
          user = _currentUser.value!!.uid,
          follower = followerUID,
          false,
          {
            refreshCurrentUser()
            onSuccess()
          },
          onFailure)
    } else {
      onFailure(Exception("User is not in followers list"))
    }
  }

  /**
   * Accepts a follower request from the specified user.
   *
   * @param followerUID The UID of the user who sent the follower request.
   * @param onSuccess Callback to be invoked when the operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun acceptFollowerRequest(
      followerUID: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    if (_currentUser.value == null) {
      onFailure(UserNotLoggedInException())
      return
    }
    if (_currentUser.value!!.pendingFriends.followers.contains(followerUID)) {
      repository.addFollowerTo(
          user = _currentUser.value!!.uid,
          follower = followerUID,
          false,
          {
            repository.removeFollowerFrom(
                user = followerUID,
                follower = _currentUser.value!!.uid,
                true,
                {
                  refreshCurrentUser()
                  onSuccess()
                },
                onFailure)
            onSuccess()
          },
          onFailure)
    } else {
      onFailure(Exception("User is not in pending followers list"))
    }
  }

  /**
   * Declines a follower request from the specified user.
   *
   * @param followerUID The UID of the user who sent the follower request.
   * @param onSuccess Callback to be invoked when the operation is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun declineFollowerRequest(
      followerUID: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    if (_currentUser.value == null) {
      onFailure(UserNotLoggedInException())
      return
    }
    if (_currentUser.value!!.pendingFriends.followers.contains(followerUID)) {
      repository.removeFollowerFrom(
          user = _currentUser.value!!.uid,
          follower = followerUID,
          true,
          {
            refreshCurrentUser()
            onSuccess()
          },
          onFailure)
    } else {
      onFailure(Exception("User is not in pending followers list"))
    }
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
