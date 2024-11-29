package com.github.onlynotesswent.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
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
}
