package com.github.onlynotesswent.ui.user

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.isNotDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextReplacement
import com.github.onlynotesswent.model.authentication.Authenticator
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
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import junit.framework.TestCase.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.verify

@RunWith(MockitoJUnitRunner::class)
class ProfileScreenTest {
  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockNoteRepository: NoteRepository
  @Mock private lateinit var mockFileRepository: FileRepository
  @Mock private lateinit var mockNotificationRepository: NotificationRepository
  @Mock private lateinit var authenticator: Authenticator
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var fileViewModel: FileViewModel
  private lateinit var notificationViewModel: NotificationViewModel

  private val testUid = "testUid"
  private val testUid2 = "testUid2"
  private val testUid3 = "testUid3"
  private val testUidNotification = "testUidNotification"
  // Following user
  private val initialTestUser2 =
      User(
          firstName = "testFirstName2",
          lastName = "testLastName2",
          userName = "testUserName2",
          email = "testEmail2",
          uid = testUid2,
          dateOfJoining = Timestamp.now(),
          rating = 0.0,
          friends = Friends(listOf(testUid3), listOf(testUid)),
          bio = "testBio2")
  // Follower user
  private val initialTestUser3 =
      User(
          firstName = "testFirstName3",
          lastName = "testLastName3",
          userName = "testUserName3",
          email = "testEmail3",
          uid = testUid3,
          dateOfJoining = Timestamp.now(),
          rating = 0.0,
          friends = Friends(listOf(testUid), listOf(testUid2)),
          bio = "testBio3")

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
  private var testUser2 = initialTestUser2
  private var testUser3 = initialTestUser3

  private val uidToUser = { s: String ->
    when (s) {
      testUser.uid -> testUser
      testUser2.uid -> testUser2
      testUser3.uid -> testUser3
      else -> null
    }
  }

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

    // Mock the user repository to return the specified user
    `when`(mockUserRepository.getUserById(any(), any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (User) -> Unit
      val onNotFound = it.arguments[2] as () -> Unit
      val uid = it.arguments[0] as String

      uidToUser(uid)?.let { it1 -> onSuccess(it1) } ?: onNotFound()
    }

    // Mock the user repository to return the specified users
    `when`(mockUserRepository.getUsersById(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as (List<User>) -> Unit
      val userIds = it.arguments[0] as List<String>

      onSuccess(userIds.mapNotNull(uidToUser))
    }

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser, {}, {})

    // Reset test users:
    testUser = initialTestUser
    testUser2 = initialTestUser2
    testUser3 = initialTestUser3

    // ----------------- Follow and Unfollow Mechanics -----------------
    // Mock the user repository to add and remove followers
    `when`(mockUserRepository.addFollowerTo(any(), any(), any(), any(), any())).thenAnswer {
      val userId = it.arguments[0] as String // testUser2
      val followerId = it.arguments[1] as String // testUser
      val onSuccess = it.getArgument<() -> Unit>(3)
      var user = uidToUser(userId)!!
      var follower = uidToUser(followerId)!!
      user =
          user.copy(
              friends = user.friends.copy(followers = user.friends.followers.plus(followerId)))
      follower =
          follower.copy(
              friends = follower.friends.copy(following = follower.friends.following.plus(userId)))
      // Update the test user and follower
      when (userId) {
        testUser.uid -> testUser = user
        testUser2.uid -> testUser2 = user
        testUser3.uid -> testUser3 = user
      }
      when (followerId) {
        testUser.uid -> testUser = follower
        testUser2.uid -> testUser2 = follower
        testUser3.uid -> testUser3 = follower
      }
      onSuccess()
    }

    `when`(mockUserRepository.removeFollowerFrom(any(), any(), any(), any(), any())).thenAnswer {
      val userId = it.arguments[0] as String // testUser2
      val followerId = it.arguments[1] as String // testUser
      val onSuccess = it.getArgument<() -> Unit>(3)
      var user = uidToUser(userId)!!
      var follower = uidToUser(followerId)!!
      user =
          user.copy(
              friends = user.friends.copy(followers = user.friends.followers.minus(followerId)))

      follower =
          follower.copy(
              friends = follower.friends.copy(following = follower.friends.following.minus(userId)))

      // Update the test user and follower
      when (userId) {
        testUser.uid -> testUser = user
        testUser2.uid -> testUser2 = user
        testUser3.uid -> testUser3 = user
      }
      when (followerId) {
        testUser.uid -> testUser = follower
        testUser2.uid -> testUser2 = follower
        testUser3.uid -> testUser3 = follower
      }
      onSuccess()
    }

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
    `when`(mockNotificationRepository.getNewUid()).thenReturn(testUidNotification)

    `when`(mockNotificationRepository.addNotification(any(), any(), any())).thenAnswer {
      val onSuccess = it.getArgument<() -> Unit>(1)
      onSuccess()
    }
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      UserProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }

    composeTestRule.onNodeWithTag("profileScaffold").assertExists()
    composeTestRule.onNodeWithTag("editProfileButton").assertExists()
    composeTestRule.onNodeWithTag("profileScaffoldColumn").assertExists()
    composeTestRule.onNodeWithTag("profileCard").assertExists()
    composeTestRule.onNodeWithTag("profileCardColumn").assertExists()
    composeTestRule.onNodeWithTag("profilePicture").assertExists()
    composeTestRule.onNodeWithTag("userFullName").assertExists()
    composeTestRule.onNodeWithTag("userHandle").assertExists()
    composeTestRule.onNodeWithTag("userRating").assertExists()
    composeTestRule.onNodeWithTag("userDateOfJoining").assertExists()
    composeTestRule.onNodeWithTag("userBio").assertExists()
    composeTestRule.onNodeWithTag("followingButton").assertExists()
    composeTestRule.onNodeWithTag("followersButton").assertExists()
    composeTestRule.onNodeWithTag("followingText", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("followersText", useUnmergedTree = true).assertExists()
    composeTestRule.onNodeWithTag("editProfileButton").assertExists()
    composeTestRule.onNodeWithTag("logoutButton").assertExists()
  }

  @Test
  fun displayAndNavigateToFollowersAndFollowing() {
    composeTestRule.setContent {
      UserProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }

    composeTestRule.onNodeWithTag("followingButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("followUnfollowButton--$testUid2").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("userItem")
        .assertCountEquals(1)
        .filter(hasText(testUser2.fullName()))
        .onFirst()
        .assertIsDisplayed()
        .performClick()
    assertEquals(testUser2, userViewModel.profileUser.value)

    composeTestRule.onNodeWithTag("followersButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("removeFollowerButton--$testUid3").assertIsDisplayed()
    composeTestRule
        .onAllNodesWithTag("userItem")
        .assertCountEquals(1)
        .filter(hasText(testUser3.fullName()))
        .onFirst()
        .assertIsDisplayed()
        .performClick()
    assertEquals(testUser3, userViewModel.profileUser.value)

    val publicProfileScreen1 = Screen.PUBLIC_PROFILE.replace("{userId}", testUid2)
    val publicProfileScreen2 = Screen.PUBLIC_PROFILE.replace("{userId}", testUid3)

    verify(mockNavigationActions).navigateTo(publicProfileScreen1)
    verify(mockNavigationActions).navigateTo(publicProfileScreen2)
  }

  @Test
  fun editProfileButtonNavigatesCorrectly() {
    composeTestRule.setContent {
      UserProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }

    composeTestRule.onNodeWithTag("editProfileButton").assertIsDisplayed().performClick()
    verify(mockNavigationActions).navigateTo(Screen.EDIT_PROFILE)
  }

  @Test
  fun followUnfollowButtonsWork() {
    composeTestRule.setContent {
      PublicProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }

    composeTestRule.onNodeWithTag("userNotFound").assertIsDisplayed()

    userViewModel.setProfileUser(testUser2)
    composeTestRule.onNodeWithTag("followUnfollowButton--$testUid2").assertIsDisplayed()

    // Follow and unfollow the user
    composeTestRule
        .onNodeWithTag("followUnfollowButtonText--$testUid2", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Unfollow")
        .performClick()
        .assertTextContains("Follow")

    assert(testUser.friends.following.isEmpty())
    assert(testUser2.friends.followers.isEmpty())

    composeTestRule.onNodeWithTag("followersButton").performClick()
    composeTestRule
        .onNodeWithTag("followersAbsent")
        .assertIsDisplayed()
        .assertTextContains("No followers to display")
    // Follow and unfollow the user, in the bottom sheet this time
    composeTestRule.onNodeWithTag("followingButton").performClick()
    composeTestRule.onNodeWithTag("followingAbsent").assertIsNotDisplayed()
    composeTestRule
        .onAllNodesWithTag("userItem")
        .assertCountEquals(1)
        .filter(hasText(testUser3.fullName()))
        .onFirst()
        .assertIsDisplayed()
    composeTestRule
        .onNodeWithTag("followUnfollowButtonText--$testUid3", useUnmergedTree = true)
        .assertIsDisplayed()
        .assertTextContains("Follow")
        .performClick()

    assert(testUser.friends.following.contains(testUser3.uid))
    assert(testUser3.friends.followers.contains(testUser.uid))

    verify(mockUserRepository).addFollowerTo(any(), any(), any(), any(), any())
    verify(mockUserRepository).removeFollowerFrom(any(), any(), any(), any(), any())
  }

  @Test
  fun profileLinkRedirectsToUserProfile() {
    composeTestRule.setContent {
      PublicProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }

    userViewModel.setProfileUser(testUser2)
    composeTestRule.onNodeWithTag("followersButton").assertIsDisplayed().performClick()
    composeTestRule
        .onAllNodesWithTag("userItem")
        .filter(hasText(testUser.fullName()))
        .onFirst()
        .performClick()

    verify(mockNavigationActions).navigateTo(TopLevelDestinations.PROFILE)
  }

  @Test
  fun goBackButtonNavigatesCorrectly() {
    composeTestRule.setContent {
      PublicProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }

    composeTestRule.onNodeWithTag("goBackButton").assertIsDisplayed().performClick()
    verify(mockNavigationActions).goBack()
  }

  @Test
  fun logoutButtonWorksCorrectly() {
    composeTestRule.setContent {
      UserProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }

    composeTestRule.onNodeWithTag("logoutButton").assertIsDisplayed()
  }

  @Test
  fun sendMessagesButtonWorksCorrectly() {

    val userThatWantToSendAMessage =
        User(
            firstName = "testFirstName4",
            lastName = "testLastName4",
            userName = "testUserName4",
            email = "testEmail4",
            uid = testUid2,
            dateOfJoining = Timestamp.now(),
            rating = 0.0,
            friends = Friends(listOf(testUid3), listOf()),
            bio = "testBio4")
    userViewModel.addUser(userThatWantToSendAMessage)
    userViewModel.setProfileUser(testUser3)
    composeTestRule.setContent {
      PublicProfileScreen(
          mockNavigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
    }
    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("messageDialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("inputmessage").assertIsDisplayed()
    composeTestRule.onNodeWithTag("confirmmessageAction").assertIsDisplayed()
    composeTestRule.onNodeWithTag("dismissmessageAction").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("messageDialog").isNotDisplayed()
    composeTestRule.onNodeWithTag("sendMessageButton").assertIsDisplayed().performClick()
    composeTestRule.onNodeWithTag("confirmmessageAction").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("inputmessage").performTextReplacement("Hello")
    composeTestRule.onNodeWithTag("confirmmessageAction").assertIsEnabled().performClick()
    composeTestRule.onNodeWithTag("messageDialog").isNotDisplayed()
    // I used captor instead of  verify equals because the timestamp is not the same
    val captor = argumentCaptor<Notification>()
    verify(mockNotificationRepository).addNotification(captor.capture(), any(), any())
    val capturedNotification = captor.firstValue
    assertEquals(testUidNotification, capturedNotification.id)
    assertEquals(testUid2, capturedNotification.senderId)
    assertEquals(testUid3, capturedNotification.receiverId)
    assertEquals(false, capturedNotification.read)
    assertEquals(Notification.NotificationType.CHAT_MESSAGE, capturedNotification.type)
    assertEquals("Hello", capturedNotification.content)
    assertNotNull(capturedNotification.timestamp)
  }
}
