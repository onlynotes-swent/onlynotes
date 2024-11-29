package com.github.onlynotesswent.ui.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.notification.Notification
import com.github.onlynotesswent.model.notification.NotificationRepository
import com.github.onlynotesswent.model.notification.NotificationViewModel
import com.github.onlynotesswent.model.user.Friends
import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserRepository
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class NotificationScreenTest {
  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockNoteRepository: NoteRepository
  @Mock private lateinit var mockFileRepository: FileRepository
  @Mock private lateinit var mockNotificationRepository: NotificationRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var fileViewModel: FileViewModel
  private lateinit var notificationViewModel: NotificationViewModel

  private val testUid = "testUid"
  private val testUid2 = "testUid2"
  private val testUid3 = "testUid3"

  // current user
  private val initialTestUser =
      User(
          firstName = "testFirstName",
          lastName = "testLastName",
          userName = "testUserName",
          email = "testEmail",
          uid = testUid,
          dateOfJoining = Timestamp.now(),
          rating = 0.0,
          friends = Friends(following = listOf(testUid2), followers = listOf(testUid3)),
          bio = "testBio")

  private var testUser = initialTestUser

  private val testNotification1 =
      Notification(
          id = "testId",
          title = "testTitle",
          body = "testBody",
          senderId = testUid2,
          receiverId = testUid,
          timestamp = Timestamp.now(),
          read = false,
          type = Notification.NotificationType.FOLLOW_REQUEST)

  private val testNotification2 =
      Notification(
          id = "testId2",
          title = "testTitle2",
          body = "testBody2",
          senderId = testUid3,
          receiverId = testUid,
          timestamp = Timestamp.now(),
          read = false,
          type = Notification.NotificationType.FOLLOW_REQUEST_ACCEPTED)

  private val testNotification3 =
      Notification(
          id = "testId3",
          title = "testTitle3",
          body = "testBody3",
          senderId = testUid2,
          receiverId = testUid,
          timestamp = Timestamp.now(),
          read = false,
          type = Notification.NotificationType.FOLLOW_REQUEST_REJECTED)

  private val testNotification4 =
      Notification(
          id = "testId4",
          title = "testTitle4",
          body = "testBody4",
          senderId = testUid2,
          receiverId = testUid,
          timestamp = Timestamp.now(),
          read = false,
          type = Notification.NotificationType.FOLLOW)

  private val testNotification5 =
      Notification(
          id = "testId5",
          title = "testTitle5",
          body = "testBody5",
          senderId = testUid2,
          receiverId = testUid,
          timestamp = Timestamp.now(),
          read = true,
          type = Notification.NotificationType.FOLLOW)

  @get:Rule val composeTestRule = createComposeRule()

  @Suppress("UNCHECKED_CAST")
  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(mockUserRepository, mockNotificationRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)
    fileViewModel = FileViewModel(mockFileRepository)
    notificationViewModel = NotificationViewModel(mockNotificationRepository)
    // Mock the current route to be the user create screen
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.USER_PROFILE)

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser, {}, {})

    // Reset test users:
    testUser = initialTestUser

    `when`(
            mockNotificationRepository.addNotification(
                any(),
                any(),
                any(),
            ))
        .thenAnswer {
          val onSuccess = it.getArgument<() -> Unit>(1)
          onSuccess()
        }
    `when`(mockNotificationRepository.getNewUid()).thenReturn(testUid)

    `when`(mockNotificationRepository.addNotification(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      NotificationScreen(
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions,
          fileViewModel = fileViewModel,
          notificationViewModel = notificationViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").assertExists()
    composeTestRule.onNodeWithTag("notificationsList").assertExists()
    composeTestRule.onNodeWithTag("notificationScreen").assertExists()
  }

  @Test
  fun displayNotifications() {
    // Add a notification to the current user
    val notificationList =
        listOf(
            testNotification1,
            testNotification2,
            testNotification3,
            testNotification4,
            testNotification5)
    `when`(mockNotificationRepository.getNotificationByReceiverId(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<(List<Notification>) -> Unit>(1)
      onSuccess(notificationList)
    }

    composeTestRule.setContent {
      NotificationScreen(
          userViewModel = userViewModel,
          navigationActions = mockNavigationActions,
          fileViewModel = fileViewModel,
          notificationViewModel = notificationViewModel)
    }

    notificationViewModel.getNotificationByReceiverId(testUid)

    composeTestRule.onNodeWithTag("notificationScreen").assertExists()
    composeTestRule.onNodeWithTag("notificationsList").assertExists()
    for (notification in notificationList) {
      composeTestRule.onNodeWithTag("notification-${notification.id}").assertExists()
    }
  }
}
