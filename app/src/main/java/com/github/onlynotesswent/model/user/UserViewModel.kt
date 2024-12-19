package com.github.onlynotesswent.model.user

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.flashcard.UserFlashcard
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
class UserViewModel(
    private val repository: UserRepository,
    private val notificationRepository: NotificationRepository =
        NotificationRepositoryFirestore(Firebase.firestore)
) : ViewModel() {

  private val _allUsers = MutableStateFlow<List<User>>(emptyList())
  val allUsers: StateFlow<List<User>> = _allUsers.asStateFlow()

  private val _currentUser = MutableStateFlow<User?>(null)
  val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

  private val _profileUser = MutableStateFlow<User?>(null)
  val profileUser: StateFlow<User?> = _profileUser.asStateFlow()

  private val _deckUserFlashcards = MutableStateFlow<Map<String, UserFlashcard>>(emptyMap())
  val deckUserFlashcards: StateFlow<Map<String, UserFlashcard>> = _deckUserFlashcards.asStateFlow()

  /** Exception thrown when a user is not logged in. */
  class UserNotLoggedInException : Exception("User Not Logged In")

  /** Initializes the UserViewModel and the repository. */
  init {
    repository.init(FirebaseAuth.getInstance()) { getAllUsers() }
  }

  // Create factory
  companion object {
    val Factory: ViewModelProvider.Factory = viewModelFactory {
      initializer {
        UserViewModel(
            UserRepositoryFirestore(Firebase.firestore),
            NotificationRepositoryFirestore(Firebase.firestore))
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
      onFailure: (Exception) -> Unit = {}
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
          onFailure(it)
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
      onFailure: (Exception) -> Unit = {}
  ) {
    _currentUser.value?.let { user ->
      repository.getUserById(
          id = user.uid,
          onSuccess = {
            onSuccess()
            _currentUser.value = it
          },
          onUserNotFound = {
            onUserNotFound()
            _currentUser.value = null
          },
          onFailure = {
            onFailure(it)
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
   *
   * @param onSuccess function to call when the users are successfully retrieved.
   * @param onFailure function to call when the retrieval fails.
   */
  fun getAllUsers(onSuccess: (List<User>) -> Unit = {}, onFailure: (Exception) -> Unit = {}) {
    repository.getAllUsers(
        onSuccess = {
          onSuccess(it)
          _allUsers.value = it
        },
        onFailure = onFailure)
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
   * @param onUserNotFound function to call when the user is not found.
   * @param onFailure function to call when the deletion fails.
   */
  fun deleteUserById(
      id: String,
      onSuccess: () -> Unit = {},
      onUserNotFound: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    repository.deleteUserById(id, onSuccess, onUserNotFound, onFailure)
  }

  // SOCIAL FUNCTIONS:
  /**
   * request a follow from the specified user. The method changes the current user's pending
   * followers. If the account of the specified user is public, the method will automatically follow
   * else it will send a request.
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

          // send notification to the user
          if (!user.isAccountPublic) {
            notificationRepository.addNotification(
                Notification(
                    notificationRepository.getNewUid(),
                    _currentUser.value!!.uid,
                    followingUID,
                    Timestamp.now(),
                    false,
                    Notification.NotificationType.FOLLOW_REQUEST),
                {},
                { Log.e("UserViewModel", "Failed to send notification", it) })
          } else {
            notificationRepository.addNotification(
                Notification(
                    notificationRepository.getNewUid(),
                    _currentUser.value!!.uid,
                    followingUID,
                    Timestamp.now(),
                    false,
                    Notification.NotificationType.FOLLOW),
                {},
                { Log.e("UserViewModel", "Failed to send notification", it) })
          }
        },
        onUserNotFound = { onFailure(Exception("User not found")) },
        onFailure = onFailure)
  }

  /**
   * Make current user unfollow the specified user. The method changes both the current user's
   * following list and the specified user's followers list. If the user is not following the
   * specified user, but has a pending request, the request is cancelled. If the current user is not
   * logged in, the onFailure callback is invoked with a UserNotLoggedInException.
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

    if (isRequest) {
      notificationRepository.getNotificationByReceiverId(
          followingUID,
          { notifications ->
            notifications.forEach({ notification ->
              if (notification.senderId == _currentUser.value!!.uid &&
                  notification.type == Notification.NotificationType.FOLLOW_REQUEST) {
                notificationRepository.deleteNotification(
                    notification.id,
                    {},
                    { Log.e("UserViewModel", "Failed to delete notification", it) })
              }
            })
          },
          {})
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

  /**
   * get the user flashcards from the specified deck.
   *
   * @param deck The deck to get the user flashcards from.
   * @param onSuccess Callback to be invoked when the retrieval is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getUserFlashcardFromDeck(
      deck: Deck,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    _currentUser.value?.let {
      repository.getUserFlashcardFromDeck(
          it.uid,
          deck,
          { flashcards ->
            _deckUserFlashcards.value = flashcards
            onSuccess()
          },
          onFailure)
    }
  }

  /**
   * Adds a new user UserFlashcard to the user flashcard collection.
   *
   * @param userFlashcard The user flashcard to added.
   * @param onSuccess Callback to be invoked when the addition is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addUserFlashcard(
      userFlashcard: UserFlashcard,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {

    _currentUser.value?.let {
      repository.addUserFlashcard(
          it.uid,
          userFlashcard,
          {
            _deckUserFlashcards.value =
                _deckUserFlashcards.value.toMutableMap().apply {
                  put(userFlashcard.id, userFlashcard)
                }
            onSuccess()
          },
          onFailure)
    }
  }

  /**
   * update a user flashcard by its ID.
   *
   * @param userFlashcard The ID of the user to whom the flashcard belongs.
   * @param onSuccess Callback to be invoked with the retrieved user flashcard.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateUserFlashcard(
      userFlashcard: UserFlashcard,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    _currentUser.value?.let {
      repository.updateUserFlashcard(
          it.uid,
          userFlashcard,
          {
            if (_deckUserFlashcards.value.containsKey(userFlashcard.id)) {
              _deckUserFlashcards.value =
                  _deckUserFlashcards.value.toMutableMap().apply {
                    put(userFlashcard.id, userFlashcard)
                  }
            }
            onSuccess()
          },
          onFailure)
    }
  }

  /**
   * Deletes a user flashcard by its ID.
   *
   * @param flashcardId The ID of the user flashcard to delete.
   * @param onSuccess Callback to be invoked when the deletion is successful.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteUserFlashcardById(
      flashcardId: String,
      onSuccess: () -> Unit = {},
      onFailure: (Exception) -> Unit = {}
  ) {
    _currentUser.value?.let {
      repository.deleteUserFlashcardById(
          it.uid,
          flashcardId,
          {
            if (_deckUserFlashcards.value.containsKey(flashcardId)) {
              _deckUserFlashcards.value =
                  _deckUserFlashcards.value.toMutableMap().apply { remove(flashcardId) }
              onSuccess()
            }
          },
          onFailure)
    }
  }
}
