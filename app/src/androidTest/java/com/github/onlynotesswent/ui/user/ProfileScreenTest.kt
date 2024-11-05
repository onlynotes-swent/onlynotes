package com.github.onlynotesswent.ui.user

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.Friends
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.verify

class ProfileScreenTest {
    @Mock private lateinit var mockUserRepository: UserRepository
    @Mock private lateinit var mockNavigationActions: NavigationActions
    @Mock private lateinit var mockNoteRepository: NoteRepository
    private lateinit var noteViewModel: NoteViewModel
    private lateinit var userViewModel: UserViewModel

    private val testUid = "testUid"

    // Following user
    private var testUser2 =
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
    private var testUser3 =
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
    private var testUser =
        User(
            firstName = "testFirstName",
            lastName = "testLastName",
            userName = "testUserName",
            email = "testEmail",
            uid = testUid,
            dateOfJoining = Timestamp.now(),
            rating = 0.0,
            friends = Friends(following = listOf(testUser2.uid), followers = listOf(testUser3.uid)),
            bio = "testBio")

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
        userViewModel = UserViewModel(mockUserRepository)
        noteViewModel = NoteViewModel(mockNoteRepository)

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
    }

    @Test
    fun displayAllComponents() {
        composeTestRule.setContent { UserProfileScreen(mockNavigationActions, userViewModel) }

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
    }

    @Test
    fun displayFollowingAndFollowers() {
        composeTestRule.setContent { UserProfileScreen(mockNavigationActions, userViewModel) }

        composeTestRule.onNodeWithTag("followingButton").assertIsDisplayed().performClick()

        composeTestRule.onNodeWithTag("followingDropdownMenu").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("item--${testUser2.userName}")
            .assertIsDisplayed()
            .assertTextContains(
                "${testUser2.fullName()} — @${testUser2.userName}",
            )

        composeTestRule.onNodeWithTag("followersButton").performClick()
        composeTestRule.onNodeWithTag("followersDropdownMenu").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("item--${testUser3.userName}")
            .assertIsDisplayed()
            .assertTextContains(
                "${testUser3.fullName()} — @${testUser3.userName}",
            )
    }

    @Test
    fun navigateToFollowersAndFollowing() {
        composeTestRule.setContent { UserProfileScreen(mockNavigationActions, userViewModel) }

        composeTestRule.onNodeWithTag("followingButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("item--${testUser2.userName}").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("followingDropdownMenu").assertIsNotDisplayed()

        composeTestRule.onNodeWithTag("followersButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("item--${testUser3.userName}").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("followersDropdownMenu").assertIsNotDisplayed()

        verify(mockNavigationActions, times(2)).navigateTo(Screen.PUBLIC_PROFILE)
    }

    @Test
    fun editProfileButtonNavigatesCorrectly() {
        composeTestRule.setContent { UserProfileScreen(mockNavigationActions, userViewModel) }

        composeTestRule.onNodeWithTag("editProfileButton").assertIsDisplayed().performClick()
        verify(mockNavigationActions).navigateTo(Screen.EDIT_PROFILE)
    }

    @Test
    fun followUnfollowButtonWorksOnPublicProfile() {
        `when`(mockUserRepository.addFollowerTo(any(), any(), any(), any())).thenAnswer {
            val onSuccess = it.arguments[2] as () -> Unit
            val userId = it.arguments[0] as String // testUser2
            val followerId = it.arguments[1] as String // testUser
            testUser2 =
                testUser2.copy(
                    friends =
                    Friends(
                        testUser2.friends.following, testUser2.friends.followers.plus(followerId)))
            testUser =
                testUser.copy(
                    friends =
                    Friends(testUser.friends.following.plus(userId), testUser.friends.followers))
            onSuccess()
        }

        `when`(mockUserRepository.removeFollowerFrom(any(), any(), any(), any())).thenAnswer {
            val onSuccess = it.arguments[2] as () -> Unit
            val userId = it.arguments[0] as String // testUser2
            val followerId = it.arguments[1] as String // testUser
            testUser2 =
                testUser2.copy(
                    friends =
                    Friends(
                        testUser2.friends.following, testUser2.friends.followers.minus(followerId)))
            testUser =
                testUser.copy(
                    friends =
                    Friends(testUser.friends.following.minus(userId), testUser.friends.followers))
            onSuccess()
        }

        composeTestRule.setContent { PublicProfileScreen(mockNavigationActions, userViewModel) }

        composeTestRule.onNodeWithTag("userNotFound").assertIsDisplayed()

        userViewModel.setProfileUser(testUser2)
        composeTestRule.onNodeWithTag("followUnfollowButton").assertIsDisplayed()
        composeTestRule
            .onNodeWithTag("followUnfollowButtonText", useUnmergedTree = true)
            .assertIsDisplayed()
            .assertTextContains("Unfollow")
            .performClick()
            .assertTextContains("Follow")
            .performClick()
            .assertTextContains("Unfollow")

        verify(mockUserRepository, times(4)).getUserById(any(), any(), any(), any())
        verify(mockUserRepository).addFollowerTo(any(), any(), any(), any())
        verify(mockUserRepository).removeFollowerFrom(any(), any(), any(), any())
    }

    @Test
    fun profileLinkRedirectsToUserProfile() {
        composeTestRule.setContent { PublicProfileScreen(mockNavigationActions, userViewModel) }

        userViewModel.setProfileUser(testUser2)
        composeTestRule.onNodeWithTag("followersButton").assertIsDisplayed().performClick()
        composeTestRule.onNodeWithTag("item--${testUser.userName}").assertIsDisplayed().performClick()

        verify(mockNavigationActions).navigateTo(TopLevelDestinations.PROFILE)
    }
}