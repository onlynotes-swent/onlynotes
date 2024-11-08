package com.github.onlynotesswent.ui.user

import android.net.Uri
import androidx.compose.ui.semantics.SemanticsProperties
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.assert
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.core.net.toUri
import com.github.onlynotesswent.model.file.FileRepository
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.note.NoteRepository
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.users.User
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserRepositoryFirestore
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.navigation.TopLevelDestinations
import com.github.onlynotesswent.utils.ProfilePictureTaker
import com.google.firebase.Timestamp
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing

class EditProfileScreenTest {
  @Mock private lateinit var mockUserRepository: UserRepository
  @Mock private lateinit var mockNavigationActions: NavigationActions
  @Mock private lateinit var profilePictureTaker: ProfilePictureTaker
  @Mock private lateinit var mockNoteRepository: NoteRepository
  @Mock private lateinit var mockFileRepository: FileRepository
  private lateinit var noteViewModel: NoteViewModel
  private lateinit var userViewModel: UserViewModel
  private lateinit var fileViewModel: FileViewModel
  private val testUid = "testUid123"
  private val testUser =
      User(
          firstName = "testFirstName",
          lastName = "testLastName",
          userName = "testUserName",
          email = "testEmail",
          uid = testUid,
          dateOfJoining = Timestamp.now(),
          rating = 0.0)
  private val existingUserName = "alreadyTakenUsername"

  @get:Rule val composeTestRule = createComposeRule()

  @Suppress("UNCHECKED_CAST")
  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    MockitoAnnotations.openMocks(this)
    userViewModel = UserViewModel(mockUserRepository)
    noteViewModel = NoteViewModel(mockNoteRepository)
    fileViewModel = FileViewModel(mockFileRepository)

    // Mock the current route to be the user create screen
    `when`(mockNavigationActions.currentRoute()).thenReturn(Screen.EDIT_PROFILE)

    // Mock add user to initialize current user
    `when`(mockUserRepository.addUser(any(), any(), any())).thenAnswer {
      val onSuccess = it.arguments[1] as () -> Unit
      onSuccess()
    }
    // Initialize current user
    userViewModel.addUser(testUser, {}, {})

    // Mock update user to throw error when applicable, or update user
    `when`(mockUserRepository.updateUser(any(), any(), any())).thenAnswer {
      val user = it.arguments[0] as User
      val onSuccess = it.arguments[1] as () -> Unit
      val onFailure = it.arguments[2] as (Exception) -> Unit

      if (user.userName == existingUserName) {
        onFailure(UserRepositoryFirestore.UsernameTakenException())
      } else {
        onSuccess()
      }
    }

    `when`(mockFileRepository.downloadFile(any(), any(), any(), any(), any())).thenAnswer {}
  }

  @Test
  fun displayAllComponents() {
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("ProfileScreen").assertExists()
    composeTestRule.onNodeWithTag("goBackButton").assertExists()
    composeTestRule.onNodeWithTag("inputFirstName").assertExists()
    composeTestRule.onNodeWithTag("inputLastName").assertExists()
    composeTestRule.onNodeWithTag("inputUserName").assertExists()
    composeTestRule.onNodeWithTag("saveButton").assertExists()
    composeTestRule.onNodeWithTag("profilePicture").assertExists()
    composeTestRule.onNodeWithTag("displayBottomSheet").assertExists()
  }

  private fun hasError(): SemanticsMatcher {
    return SemanticsMatcher.expectValue(SemanticsProperties.Error, "Invalid input")
  }

  @Test
  fun submitNavigatesToHomeProfile() {
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("saveButton").performClick()
    verify(mockNavigationActions).navigateTo(TopLevelDestinations.PROFILE)
  }

  @Test
  fun modifyProfile() {
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("inputUserName").performTextClearance()
    composeTestRule.onNodeWithTag("inputUserName").performTextInput("newUserName")
    assert(userViewModel.currentUser.value?.userName == "testUserName")
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.userName == "newUserName")

    composeTestRule.onNodeWithTag("inputFirstName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput("newFirstName")
    assert(userViewModel.currentUser.value?.firstName == "testFirstName")
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.firstName == "newFirstName")

    composeTestRule.onNodeWithTag("inputLastName").performTextClearance()
    composeTestRule.onNodeWithTag("inputLastName").performTextInput("newLastName")
    assert(userViewModel.currentUser.value?.lastName == "testLastName")
    composeTestRule.onNodeWithTag("saveButton").performClick()
    assert(userViewModel.currentUser.value?.lastName == "newLastName")
  }

  @Test
  fun userNameFieldDisplaysError() {
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("inputFirstName").performTextClearance()
    composeTestRule.onNodeWithTag("inputFirstName").performTextInput("newFirstName")

    composeTestRule.onNodeWithTag("inputUserName").performTextClearance()
    composeTestRule.onNodeWithTag("inputUserName").performTextInput(existingUserName)

    composeTestRule.onNodeWithTag("saveButton").performClick() // error occurs
    composeTestRule.onNodeWithTag("inputUserName").assert(hasError()) // error shown

    assert(userViewModel.currentUser.value?.userName == "testUserName") // did not change
    assert(userViewModel.currentUser.value?.firstName == "testFirstName") // did not change
  }

  @Test
  fun saveButtonDisabledWhenUserNameIsEmpty() {
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("saveButton").assertIsEnabled()
    composeTestRule.onNodeWithTag("inputUserName").performTextClearance()
    composeTestRule.onNodeWithTag("saveButton").assertIsNotEnabled()
    composeTestRule.onNodeWithTag("inputUserName").performTextInput("test")
    composeTestRule.onNodeWithTag("saveButton").assertIsEnabled()
  }

  @Test
  fun goBackButtonWork() {
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("goBackButton").performClick()
    verify(mockNavigationActions).goBack()
  }

  @Test
  fun addProfilePicture() {
    doNothing().`when`(profilePictureTaker).pickImage()
    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }
    composeTestRule.onNodeWithTag("displayBottomSheet").assertIsEnabled()
    composeTestRule.onNodeWithTag("displayBottomSheet").performClick()

    composeTestRule.onNodeWithTag("addProfilePicture").assertIsEnabled()
    composeTestRule.onNodeWithTag("addProfilePicture").performClick()
    verify(profilePictureTaker).pickImage()
  }

  @Test
  fun downloadProfilePicture() {
    userViewModel.addUser(
        User(
            firstName = "testFirstName",
            lastName = "testLastName",
            userName = "testUserName",
            email = "testEmail",
            uid = testUid,
            dateOfJoining = Timestamp.now(),
            rating = 0.0,
            hasProfilePicture = true),
        {},
        {})

    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()
    verify(mockFileRepository).downloadFile(any(), any(), any(), any(), any())
  }

  @Test
  fun testUriHandling() {
    doNothing().`when`(profilePictureTaker).pickImage()
    `when`(profilePictureTaker.setOnImageSelected(any())).thenAnswer {
      val onImageSelected = it.arguments[0] as (Uri?) -> Unit
      onImageSelected("testUri".toUri())
    }

    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("displayBottomSheet").assertIsEnabled()
    composeTestRule.onNodeWithTag("displayBottomSheet").performClick()
    composeTestRule.onNodeWithTag("addProfilePicture").performClick()
    verify(profilePictureTaker).pickImage()
  }

  @Test
  fun testChangingProfilePicture(){
    doNothing().`when`(profilePictureTaker).pickImage()
    `when`(profilePictureTaker.setOnImageSelected(any())).thenAnswer {
      val onImageSelected = it.arguments[0] as (Uri?) -> Unit
      onImageSelected("testUri".toUri())
    }

    composeTestRule.setContent {
      EditProfileScreen(mockNavigationActions, userViewModel, profilePictureTaker, fileViewModel)
    }

    composeTestRule.onNodeWithTag("displayBottomSheet").assertIsEnabled()
    composeTestRule.onNodeWithTag("displayBottomSheet").performClick()
    composeTestRule.onNodeWithTag("addProfilePicture").performClick()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()

    composeTestRule.onNodeWithTag("displayBottomSheet").performClick()

    composeTestRule.onNodeWithTag("removeProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("removeProfilePicture").assertIsEnabled()
    composeTestRule.onNodeWithTag("editProfilePicture").assertIsEnabled()

    composeTestRule.onNodeWithTag("removeProfilePicture").performClick()


    composeTestRule.onNodeWithTag("displayBottomSheet").performClick()
    composeTestRule.onNodeWithTag("addProfilePicture").performClick()



    composeTestRule.onNodeWithTag("displayBottomSheet").performClick()

    composeTestRule.onNodeWithTag("editProfilePicture").assertIsDisplayed()
    composeTestRule.onNodeWithTag("editProfilePicture").assertIsEnabled()

    composeTestRule.onNodeWithTag("editProfilePicture").performClick()
    composeTestRule.onNodeWithTag("profilePicture").assertIsDisplayed()






  }
}
