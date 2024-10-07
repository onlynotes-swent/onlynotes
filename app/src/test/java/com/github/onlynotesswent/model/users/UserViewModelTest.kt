package com.github.onlynotesswent.model.users

import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.anyString
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.timeout
import org.mockito.kotlin.verify
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28]) // Specify the SDK version to use
class UserViewModelTest {

  @Mock private lateinit var mockRepositoryFirestore: UserRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  private val user =
      User(
          name = "User",
          email = "example@gmail.com",
          uid = "1",
          dateOfJoining = Timestamp.now(),
          rating = 0.0)

  @Before
  fun setUp() {
    mockRepositoryFirestore = mock(UserRepositoryFirestore::class.java)

    // Initialize FirebaseApp with Robolectric context
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    FirebaseApp.initializeApp(context)

    userViewModel = UserViewModel(mockRepositoryFirestore)
  }

  @Test
  fun `init should call repository init`() {
    verify(mockRepositoryFirestore, timeout(1000)).init(any(), any())
  }

  @Test
  fun `getNewUid should return new UID`() {
    `when`(mockRepositoryFirestore.getNewUid()).thenReturn("1")
    val uid = userViewModel.getNewUid()
    assertEquals("1", uid)
  }

  @Test
  fun `addUser should call repository addUser`() {
    userViewModel.addUser(user, {}, {})
    verify(mockRepositoryFirestore, timeout(1000)).addUser(any(), anyOrNull(), anyOrNull())
  }

  @Test
  fun `updateUser should call repository updateUser`() {
    userViewModel.updateUser(user, {}, {})
    verify(mockRepositoryFirestore, timeout(1000)).updateUser(any(), anyOrNull(), anyOrNull())
  }

  @Test
  fun `getUsers should call repository getUsers`() {
    userViewModel.getUsers({}, {})
    verify(mockRepositoryFirestore, timeout(1000)).getUsers(anyOrNull(), anyOrNull())
  }

  @Test
  fun `getUserById should call repository getUserById`() {
    userViewModel.getUserById(user.uid, {}, {})
    verify(mockRepositoryFirestore, timeout(1000)).getUserById(anyString(), any(), any())
  }

  @Test
  fun `deleteUserById should call repository deleteUserById`() {
    // Ensure user is properly initialized
    val user =
        User(
            name = "User",
            email = "example@gmail.com",
            uid = "1",
            dateOfJoining = Timestamp.now(),
            rating = 0.0)
    userViewModel.deleteUserById(user.uid, {}, {})
    verify(mockRepositoryFirestore, timeout(1000))
        .deleteUserById(anyString(), anyOrNull(), anyOrNull())
  }
}
