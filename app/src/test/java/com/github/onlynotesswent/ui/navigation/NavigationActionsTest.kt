package com.github.onlynotesswent.ui.navigation

import androidx.navigation.NavDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptionsBuilder
import com.github.onlynotesswent.model.folder.Folder
import java.util.EmptyStackException
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Assert.assertThrows
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.verifyNoMoreInteractions
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
  fun testScreenNavigationStack() {
    navigationActions.pushToScreenNavigationStack("folderId1")
    navigationActions.pushToScreenNavigationStack("folderId2")
    navigationActions.pushToScreenNavigationStack("folderId3")
    val topElement = navigationActions.retrieveTopElementOfScreenNavigationStack()
    assertEquals(topElement, "folderId3")

    navigationActions.popFromScreenNavigationStack()
    navigationActions.popFromScreenNavigationStack()
    val topElementAfterPop = navigationActions.retrieveTopElementOfScreenNavigationStack()
    assertEquals(topElementAfterPop, "folderId1")

    navigationActions.pushToScreenNavigationStack("folderId4")
    navigationActions.clearScreenNavigationStack()
    assertThrows(EmptyStackException::class.java) {
      navigationActions.retrieveTopElementOfScreenNavigationStack()
    }
  }

  @Test
  fun testPopFromScreenNavigationStackWhenEmpty() {
    val poppedId = navigationActions.popFromScreenNavigationStack()
    assertNull(poppedId)
  }

  @Test
  fun navigateToSearchScreenCallsSetsStack() {
    navigationActions.navigateTo(Screen.SEARCH)
    val searchId = navigationActions.popFromScreenNavigationStack()
    assertEquals(searchId, Screen.SEARCH)
    val nullStackElement = navigationActions.popFromScreenNavigationStack()
    assertNull(nullStackElement)
  }

  @Test
  fun navigateToTopLevelDestinationClearsStack() {
    navigationActions.pushToScreenNavigationStack("folderId1")
    navigationActions.pushToScreenNavigationStack("folderId2")
    navigationActions.navigateTo(TopLevelDestinations.OVERVIEW)
    val overviewId = navigationActions.popFromScreenNavigationStack()
    assertNull(overviewId)
  }

  @Test
  fun testNavigateToFolderContentsWithRootFolder() {

    val rootFolder = Folder(id = "1", name = "Root Folder", userId = "1", parentFolderId = null)

    navigationActions.navigateToFolderContents(rootFolder)
    // Since root folder, dont push id to stack
    val nullStackElement = navigationActions.popFromScreenNavigationStack()
    assertNull(nullStackElement)

    verify(navHostController).navigate(Screen.FOLDER_CONTENTS)
    verifyNoMoreInteractions(navHostController)
  }

  @Test
  fun testNavigateToFolderContentsWithSubFolder() {
    val subFolder = Folder(id = "2", name = "Sub Folder", userId = "1", parentFolderId = "1")

    navigationActions.navigateToFolderContents(subFolder)

    val poppedId = navigationActions.popFromScreenNavigationStack()
    assertEquals(poppedId, subFolder.parentFolderId)

    verify(navHostController).navigate(Screen.FOLDER_CONTENTS)
    verifyNoMoreInteractions(navHostController)
  }

  @Test
  fun testNavigateToFolderContentsFromSearch() {

    val folder = Folder(id = "1", name = "Test Folder", userId = "1", parentFolderId = null)
    navigationActions.pushToScreenNavigationStack(Screen.SEARCH)

    navigationActions.navigateToFolderContents(folder)

    val poppedId = navigationActions.popFromScreenNavigationStack()
    assertEquals(poppedId, Screen.SEARCH)

    verify(navHostController).navigate(Screen.FOLDER_CONTENTS)
    verifyNoMoreInteractions(navHostController)
  }
}
