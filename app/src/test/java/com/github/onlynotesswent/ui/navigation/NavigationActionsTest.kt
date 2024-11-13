package com.github.onlynotesswent.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any
import org.mockito.kotlin.eq

class NavigationActionsTest {

  private lateinit var navigationDestination: NavDestination
  private lateinit var navHostController: NavHostController
  private lateinit var navigationActions: NavigationActions

  @Before
  fun setUp() {
    navigationDestination = mock(NavDestination::class.java)
    navHostController = mock(NavHostController::class.java)
    navigationActions = NavigationActions(navHostController)
  }

  @Test
  fun navigateToCallsController() {
    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
    verify(navHostController).navigate(eq(Route.OVERVIEW), any<NavOptionsBuilder.() -> Unit>())

    navigationActions.navigateTo(Screen.AUTH)
    verify(navHostController).navigate(Screen.AUTH)

    navigationActions.navigateTo(TopLevelDestinations.SEARCH)
    verify(navHostController).navigate(eq(Route.SEARCH), any<NavOptionsBuilder.() -> Unit>())
  }

  @Test
  fun goBackCallsController() {
    navigationActions.goBack()
    verify(navHostController).popBackStack()
  }

  @Test
  fun currentRouteWorksWithDestination() {
    `when`(navHostController.currentDestination).thenReturn(navigationDestination)
    `when`(navigationDestination.route).thenReturn(Route.OVERVIEW)

    assertThat(navigationActions.currentRoute(), `is`(Route.OVERVIEW))
  }

  @Test
  fun testPushToScreenNavigationStack() {
    navigationActions.pushToScreenNavigationStack("folderId1")
    navigationActions.pushToScreenNavigationStack("folderId2")
    val stack = navigationActions.getScreenNavigationStack()
    assertEquals(2, stack.size)
    assertEquals("folderId2", stack.last())
  }

  @Test
  fun testPopFromScreenNavigationStack() {
    navigationActions.pushToScreenNavigationStack("userProfileId1")
    navigationActions.pushToScreenNavigationStack("userProfileId2")
    val poppedId = navigationActions.popFromScreenNavigationStack()
    val stack = navigationActions.getScreenNavigationStack()
    assertEquals("userProfileId2", poppedId)
    assertEquals(1, stack.size)
    assertEquals("userProfileId1", stack.last())
  }

  @Test
  fun testPopFromScreenNavigationStackWhenEmpty() {
    val poppedId = navigationActions.popFromScreenNavigationStack()
    assertNull(poppedId)
  }

  @Test
  fun testClearScreenNavigationStack() {
    navigationActions.pushToScreenNavigationStack("folderId1")
    navigationActions.pushToScreenNavigationStack("folderId2")
    val stackNotCleared = navigationActions.getScreenNavigationStack()
    assertEquals(2, stackNotCleared.size)
    navigationActions.clearScreenNavigationStack()
    val stackCleared = navigationActions.getScreenNavigationStack()
    assertEquals(0, stackCleared.size)
  }

  @Test
  fun getScreenNavigationStackReturnsCopy() {
    navigationActions.pushToScreenNavigationStack("folderId1")
    navigationActions.pushToScreenNavigationStack("folderId2")
    val stack = navigationActions.getScreenNavigationStack()
    stack.add("folderId3")
    val stackAfterModification = navigationActions.getScreenNavigationStack()
    assertEquals(3, stack.size)
    assertEquals(2, stackAfterModification.size)
    assertEquals("folderId1", stackAfterModification[0])
    assertEquals("folderId2", stackAfterModification[1])
  }
}
