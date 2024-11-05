package com.github.onlynotesswent.ui.user

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.Friends
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any

class UserProfileScreenTest {
  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var mockNoteRepository: NoteRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel

  val testUid = "testUid123"

  // Following user
  private val testUser2 =
    User(
      firstName = "testFirstName2",
      lastName = "testLastName2",
      userName = "testUserName2",
      email = "testEmail2",
      uid = "testUid2",
      dateOfJoining = Timestamp.now(),
      rating = 0.0,
      friends = Friends(listOf(), listOf(testUid)),
      bio = "testBio2")
  // Follower user
  private val testUser3 =
    User(
      firstName = "testFirstName3",
      lastName = "testLastNam3e",
      userName = "testUserName3",
      email = "testEmail3",
      uid = "testUid3",
      dateOfJoining = Timestamp.now(),
      rating = 0.0,
      friends = Friends(listOf(testUid), listOf()),
      bio = "testBio3")

  // current user
  private val testUser =
      User(
          firstName = "testFirstName",
          lastName = "testLastName",
          userName = "testUserName",
          email = "testEmail",
          uid = testUid,
          dateOfJoining = Timestamp.now(),
          rating = 0.0,
          friends = Friends(listOf(testUser2.uid), listOf(testUser3.uid)),
          bio = "testBio")



  @get:Rule val composeTestRule = createComposeRule()

  @Suppress("UNCHECKED_CAST")
  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(mockUserRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)

    // Mock the current route to be the user create screen
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.USER_PROFILE)

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser, {}, {})
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent { UserProfileScreen(mockNavigationActions, userViewModel) }

    composeTestRule.onNodeWithTag("profileScaffold").assertExists()
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
  }


}
