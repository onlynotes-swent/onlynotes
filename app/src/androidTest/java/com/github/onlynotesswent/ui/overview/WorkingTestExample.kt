package com.github.onlynotesswent.ui.overview

import androidx.compose.ui.test.junit4.createComposeRule
import com.github.onlynotesswent.model.users.UserRepository
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class UserCreateScreenTest {
  private lateinit var userRepository: UserRepository
  private lateinit var userViewModel: UserViewModel
  private lateinit var navigationActions: NavigationActions

  @get:Rule val composeTestRule = createComposeRule()

  @Before
  fun setUp() {
    // Mock is a way to create a fake object that can be used in place of a real object
    userRepository = mock(UserRepository::class.java)
    navigationActions = mock(NavigationActions::class.java)
    userViewModel = UserViewModel(userRepository)

    // Mock the current route to be the user create screen
    `when`(navigationActions.currentRoute()).thenReturn(Screen.OVERVIEW)
  }

  @Test fun mockTest() {}

  /*
  @Test
  fun displayAllComponents() {
      composeTestRule.setContent { UserCreate(navigationActions, userViewModel) }

      composeTestRule.onNodeWithTag("addUserScreen").assertExists()
      composeTestRule.onNodeWithTag("goBackButton").assertExists()
      composeTestRule.onNodeWithTag("inputFirstName").assertExists()
      composeTestRule.onNodeWithTag("inputLastName").assertExists()
      composeTestRule.onNodeWithTag("inputUserName").assertExists()
      composeTestRule.onNodeWithTag("createUserButton").assertExists()
  }

  @Test
  fun doesNotSubmitWithoutUser() {
      composeTestRule.setContent { UserCreate(navigationActions, userViewModel) }

      composeTestRule.onNodeWithTag("createUserButton").performClick()
      verify(userRepository, never()).addUser(any(), any(), any())
  }

  @Test
  fun doesNotSubmitAlreadyExistingUser(){

  }*/
}
