package com.github.onlynotesswent.model.user

import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyBoolean
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.eq
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@Suppress("UNCHECKED_CAST")
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Specify the SDK version to use
class UserViewModelTest {

  @Mock private lateinit var mockRepositoryFirestore: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  private val user =
      User(
          firstName = "User",
          lastName = "Name",
          userName = "username",
          email = "example@gmail.com",
          uid = "1",
          dateOfJoining = Timestamp.now(),
          rating = 0.0,
          friends = Friends(following = listOf("3"), followers = listOf("3")))

  private val otherUser =
      User(
          firstName = "User",
          lastName = "Name",
          userName = "username",
          email = "other@gmail.com",
          uid = "3",
          dateOfJoining = Timestamp.now(),
          rating = 0.0,
          friends = Friends(following = listOf("1"), followers = listOf("1")))

  @Before
  fun setUp() {
    mockRepositoryFirestore = mock(UserRepositoryFirestore::class.java)
    val mockFirebaseAuth = mock(FirebaseAuth::class.java)
    val mockFirebaseUser = mock(FirebaseUser::class.java)

    // Mock the currentUser and its email
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)
    `when`(mockFirebaseUser.email).thenReturn("example@gmail.com")

    // Mock the add user method to call onSuccess
    `when`(mockRepositoryFirestore.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }

    // Mock the update user method to call onSuccess
    `when`(mockRepositoryFirestore.updateUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }

    // Mock the getUserByEmail method to return a valid user
    `when`(mockRepositoryFirestore.getUserByEmail(eq(user.email), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      onSuccess(user)
    }

    // Mock the getUserById method to return a valid user
    `when`(mockRepositoryFirestore.getUserById(eq("1"), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      onSuccess(user)
    }
    // Mock the getUsers method to return a valid user
    `when`(mockRepositoryFirestore.getUsersById(eq(listOf("1")), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(listOf(user))
    }
    // Mock getAllUsers to return a list of users
    `when`(mockRepositoryFirestore.getAllUsers(any(), any())).thenAnswer {
      val onSuccess = it.arguments[0] as (List<User>) -> Unit
      onSuccess(listOf(user, otherUser))
    }

    // Mock the delete user method to call onSuccess
    `when`(mockRepositoryFirestore.deleteUserById(eq("1"), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }
    // Mock the addFollowerTo method to call onSuccess
    `when`(
            mockRepositoryFirestore.addFollowerTo(
                anyString(), anyString(), anyBoolean(), anyOrNull(), anyOrNull()))
        .thenAnswer {
          val onSuccess = it.arguments[3] as () -> Unit
          onSuccess()
        }
    // Mock the removeFollowerFrom method to call onSuccess
    `when`(
            mockRepositoryFirestore.removeFollowerFrom(
                anyString(), anyString(), anyBoolean(), anyOrNull(), anyOrNull()))
        .thenAnswer {
          val onSuccess = it.arguments[3] as () -> Unit
          onSuccess()
        }

    // Initialize FirebaseApp with Robolectric context
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    FirebaseApp.initializeApp(context)

    // Initialize UserViewModel with the mocked repository and FirebaseAuth
    userViewModel = UserViewModel(mockRepositoryFirestore)
  }

  @Test
  fun `init should call repository init`() {
    verify(mockRepositoryFirestore, timeout(1000)).init(any(), any())
  }

  @Test
  fun `refreshUser refreshes user`() {
    userViewModel.refreshCurrentUser()
    assertNull(userViewModel.currentUser.value) // no current user is set, cannot refresh

    userViewModel.addUser(user, {}, {}) // set current user
    assert(userViewModel.currentUser.value == user)

    // Mock getUserById to return the other user, this will be called by refreshUser
    `when`(mockRepositoryFirestore.getUserById(eq("1"), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      onSuccess(otherUser)
    }
    userViewModel.refreshCurrentUser()
    assert(userViewModel.currentUser.value == otherUser) // user was correctly refreshed
  }

  @Test
  fun `getNewUid should return new UID`() {
    `when`(mockRepositoryFirestore.getNewUid()).thenReturn("1")
    val uid = userViewModel.getNewUid()
    assertEquals("1", uid)
  }

  @Test
  fun `addUser should call repository addUser and set current user`() {
    var onSuccessCalled = false
    userViewModel.addUser(user, { onSuccessCalled = true }, { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000)).addUser(any(), anyOrNull(), anyOrNull())
    assertEquals(user, userViewModel.currentUser.value)
    assert(onSuccessCalled)
  }

  @Test
  fun `updateUser should call repository updateUser and set current user`() {
    // Initialize the currentUser
    userViewModel.addUser(user, {}, {})

    var onSuccessCalled = false
    userViewModel.updateUser(user, { onSuccessCalled = true }, { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000)).updateUser(any(), anyOrNull(), anyOrNull())
    assertEquals(user, userViewModel.currentUser.value)
    assert(onSuccessCalled)
  }

  @Test
  fun `getAllUsers should call repository getAllUsers`() {
    userViewModel.getAllUsers()
    verify(mockRepositoryFirestore, timeout(1000)).getAllUsers(anyOrNull(), anyOrNull())
    assert(userViewModel.allUsers.value.isNotEmpty())
  }

  @Test
  fun `getUserById should call repository getUserById`() {
    var returnedUser: User? = null
    userViewModel.getUserById(user.uid, { returnedUser = it }, { assert(false) }, { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000)).getUserById(anyString(), any(), any(), any())
    assertNotNull(returnedUser)
    assertEquals(user, returnedUser!!)
  }

  @Test
  fun `getCurrentUserByEmail should call repository getUserByEmail`() {
    var returnedUser: User? = null
    userViewModel.getCurrentUserByEmail(
        user.email, { returnedUser = it }, { assert(false) }, { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000)).getUserByEmail(anyString(), any(), any(), any())
    assertNotNull(returnedUser)
    assertEquals(user, returnedUser!!)
  }

  @Test
  fun `deleteUserById should call repository deleteUserById`() {
    var onSuccessCalled = false
    userViewModel.deleteUserById(user.uid, { onSuccessCalled = true }, { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000))
        .deleteUserById(anyString(), anyOrNull(), anyOrNull())
    assert(onSuccessCalled)
  }

  @Test
  fun `followUser works correctly`() {
    // followUser should fail if the user is not signed in
    var exception: Exception? = null
    userViewModel.followUser("1", { assert(false) }, { exception = it })
    assert(exception is UserViewModel.UserNotLoggedInException)

    // Mock the currentUser to be non-null
    userViewModel.addUser(user, { assert(true) }, { assert(false) })

    // Call followUser with a logged in user
    var onSuccessCalled = false
    userViewModel.followUser("2", { onSuccessCalled = true }, { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000))
        .addFollowerTo(eq("2"), eq("1"), anyBoolean(), anyOrNull(), anyOrNull())
    verify(mockRepositoryFirestore, timeout(1000))
        .getUserById(eq("1"), anyOrNull(), anyOrNull(), anyOrNull())
    assert(onSuccessCalled)
  }

  @Test
  fun `unfollowUser works correctly`() {
    // unfollowUser should fail if the user is not signed in
    var exception: Exception? = null
    userViewModel.unfollowUser("2", { assert(false) }, { exception = it })
    assert(exception is UserViewModel.UserNotLoggedInException)

    // Mock the currentUser to be non-null

    userViewModel.addUser(user, { assert(true) }, { assert(false) })

    // Call unfollowUser with a logged in user
    var onSuccessCalled = false
    userViewModel.unfollowUser("3", { onSuccessCalled = true }, { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000))
        .removeFollowerFrom(eq("3"), eq("1"), anyBoolean(), anyOrNull(), anyOrNull())
    assert(onSuccessCalled)
  }

  @Test
  fun `getFollowersFrom works correctly`() {
    var returnedUsers: List<User> = emptyList()
    var onSuccessCalled = false
    // Mock the getUserById method to return a valid user
    `when`(mockRepositoryFirestore.getUserById(eq("1"), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      onSuccess(user)
    }
    `when`(mockRepositoryFirestore.getUsersById(eq(listOf("3")), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(listOf(otherUser))
    }

    userViewModel.getFollowersFrom(
        "1",
        { users ->
          returnedUsers = users
          onSuccessCalled = true
        },
        { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000))
        .getUserById(eq("1"), anyOrNull(), anyOrNull(), anyOrNull())
    verify(mockRepositoryFirestore, timeout(1000))
        .getUsersById(eq(listOf("3")), anyOrNull(), anyOrNull())

    assert(onSuccessCalled)
    assert(returnedUsers.isNotEmpty())
    assert(returnedUsers.contains(otherUser))
  }

  @Test
  fun `getFollowersFrom returns empty list`() {
    var returnedUsers: List<User> = emptyList()
    var onSuccessCalled = false
    // Mock the getUserById method to return a valid user
    `when`(mockRepositoryFirestore.getUserById(eq("1"), any(), any(), any())).thenAnswer {
      val onUserNotFound = it.arguments[2] as () -> Unit
      onUserNotFound()
    }

    userViewModel.getFollowersFrom(
        "1",
        { users ->
          returnedUsers = users
          onSuccessCalled = true
        },
        { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000))
        .getUserById(eq("1"), anyOrNull(), anyOrNull(), anyOrNull())

    assert(onSuccessCalled)
    assert(returnedUsers.isEmpty())
  }

  @Test
  fun `getFollowingFrom works correctly`() {
    var returnedUsers: List<User> = emptyList()
    var onSuccessCalled = false
    // Mock the getUserById method to return a valid user
    `when`(mockRepositoryFirestore.getUserById(eq("1"), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      onSuccess(user)
    }
    `when`(mockRepositoryFirestore.getUsersById(eq(listOf("3")), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      onSuccess(listOf(otherUser))
    }

    userViewModel.getFollowingFrom(
        "1",
        { users ->
          returnedUsers = users
          onSuccessCalled = true
        },
        { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000))
        .getUserById(eq("1"), anyOrNull(), anyOrNull(), anyOrNull())
    verify(mockRepositoryFirestore, timeout(1000))
        .getUsersById(eq(listOf("3")), anyOrNull(), anyOrNull())

    assert(onSuccessCalled)
    assert(returnedUsers.isNotEmpty())
    assert(returnedUsers.contains(otherUser))
  }

  @Test
  fun `getFollowingFrom returns empty list`() {
    var returnedUsers: List<User> = emptyList()
    var onSuccessCalled = false
    // Mock the getUserById method to return a valid user
    `when`(mockRepositoryFirestore.getUserById(eq("1"), any(), any(), any())).thenAnswer {
      val onUserNotFound = it.arguments[2] as () -> Unit
      onUserNotFound()
    }

    userViewModel.getFollowingFrom(
        "1",
        { users ->
          returnedUsers = users
          onSuccessCalled = true
        },
        { assert(false) })
    verify(mockRepositoryFirestore, timeout(1000))
        .getUserById(eq("1"), anyOrNull(), anyOrNull(), anyOrNull())

    assert(onSuccessCalled)
    assert(returnedUsers.isEmpty())
  }
}
