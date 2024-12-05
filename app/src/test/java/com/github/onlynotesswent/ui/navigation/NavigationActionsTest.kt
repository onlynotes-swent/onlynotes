package com.github.onlynotesswent.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.github.onlynotesswent.model.folder.Folder
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class NavigationActionsTest {

  @Mock private lateinit var mockNavigationDestination: NavDestination
  @Mock private lateinit var mockNavHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  private val subfolder = Folder("folderId", "folderName", "folderUser", "folderParentId")
  private val folder = Folder("folderId2", "folderName2", "folderUser2")

  @Before
  fun setUp() {
    MockitoAnnotations.openMocks(this)
    navigationActions = NavigationActions(mockNavHostController)
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
}
