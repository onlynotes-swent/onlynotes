package com.github.onlynotesswent.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.notification.NotificationRepositoryFirestore
import com.github.onlynotesswent.model.user.Friends
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepositoryFirestore
import com.github.onlynotesswent.model.user.UserViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.robolectric.RobolectricTestRunner

@Suppress("UNCHECKED_CAST")
@RunWith(RobolectricTestRunner::class)
class NavigationActionsTest {

  @Mock private lateinit var mockRepositoryFirestore: UserRepositoryFirestore
  @Mock private lateinit var mockRepostioryFirestoreNotification: NotificationRepositoryFirestore
  private lateinit var userViewModel: UserViewModel

  @Mock private lateinit var mockNavigationDestination: NavDestination
  @Mock private lateinit var mockNavHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  private val folder =
      Folder(id = "1", name = "folderName", userId = "1", lastModified = Timestamp.now())
  private val subfolder =
      Folder(
          id = "2",
          name = "subfolderName",
          userId = "1",
          parentFolderId = "1",
          lastModified = Timestamp.now())

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

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    navigationActions = NavigationActions(mockNavHostController)

    val mockFirebaseAuth = mock(FirebaseAuth::class.java)
    val mockFirebaseUser = mock(FirebaseUser::class.java)
    `when`(mockFirebaseAuth.currentUser).thenReturn(mockFirebaseUser)

    // Mock the add user method to call onSuccess
    `when`(mockRepositoryFirestore.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }

    // Initialize FirebaseApp with Robolectric context
    val context = org.robolectric.RuntimeEnvironment.getApplication()
    FirebaseApp.initializeApp(context)

    // Initialize UserViewModel with the mocked repository and FirebaseAuth
    userViewModel = UserViewModel(mockRepositoryFirestore, mockRepostioryFirestoreNotification)
  }

  @Test
  fun navigateToCallsController() {
    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
    verify(mockNavHostController).navigate(eq(Route.OVERVIEW), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(Screen.AUTH)
    verify(mockNavHostController).navigate(Screen.AUTH)

    navigationActions.navigateTo(TopLevelDestinations.SEARCH)
    verify(mockNavHostController).navigate(eq(Route.SEARCH), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun goBackCallsController() {
    navigationActions.goBack()
    verify(mockNavHostController).popBackStack()
  }

  @Test
  fun currentRouteWorksWithDestination() {
    `when`(mockNavHostController.currentDestination).thenReturn(mockNavigationDestination)
    `when`(mockNavigationDestination.route).thenReturn(Route.OVERVIEW)

    assertThat(navigationActions.currentRoute(), `is`(Route.OVERVIEW))
  }

  @Test
  fun getPreviousScreenCallsPreviousBackStackEntry() {
    navigationActions.getPreviousScreen()
    verify(mockNavHostController).previousBackStackEntry
  }

  @Test
  fun goBackFolderContentsCallsNavigateTo() {
    `when`(mockRepositoryFirestore.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }
    userViewModel.addUser(user, {}, {})
    navigationActions.goBackFolderContents(subfolder, user)
    verify(mockNavHostController)
        .navigate(
            Screen.FOLDER_CONTENTS.replace(
                oldValue = "{folderId}", newValue = subfolder.parentFolderId!!))

    navigationActions.goBackFolderContents(folder, user)
    verify(mockNavHostController).navigate(eq(Route.OVERVIEW), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun navigateToAndPopCallsController() {
    navigationActions.navigateToAndPop(Screen.SEARCH)
    verify(mockNavHostController).navigate(eq(Screen.SEARCH), any<NavOptionsBuilder.() -> Unit>())
  }
}
