package com.github.onlynotesswent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import java.util.Stack

object Route {
  const val OVERVIEW = "Overview"
  const val AUTH = "Auth"
  const val SEARCH = "Search"
  const val PROFILE = "Profile"
}

object Screen {
  const val AUTH = "Auth Screen"
  const val OVERVIEW = "Overview Screen"
  const val CREATE_USER = "Create User Screen"
  const val ADD_NOTE = "Add Note Screen"
  const val EDIT_NOTE = "Edit Note Screen"
  const val SEARCH_NOTE = "Search Note Screen"
  const val USER_PROFILE = "User Profile Screen"
  const val PUBLIC_PROFILE = "Public Profile Screen"
  const val EDIT_PROFILE = "Edit Profile Screen"
  const val FOLDER_CONTENTS = "Folder Contents Screen"
}

data class TopLevelDestination(val route: String, val icon: ImageVector, val textId: String)

object TopLevelDestinations {
  val OVERVIEW =
      TopLevelDestination(
          route = Route.OVERVIEW, icon = Icons.Outlined.Home, textId = "Notes")
  val SEARCH =
      TopLevelDestination(
          route = Route.SEARCH, icon = Icons.Outlined.Search, textId = "Search")
  val PROFILE =
      TopLevelDestination(
          route = Route.PROFILE, icon = Icons.Outlined.Person, textId = "Profile")
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(TopLevelDestinations.OVERVIEW, TopLevelDestinations.SEARCH, TopLevelDestinations.PROFILE)

open class NavigationActions(
    private val navController: NavHostController,
) {

  private val screenNavigationStack = Stack<String>()
  /**
   * Navigate to the specified [TopLevelDestination]
   *
   * @param destination The top level destination to navigate to Clear the back stack when
   *   navigating to a new destination This is useful when navigating to a new screen from the
   *   bottom navigation bar as we don't want to keep the previous screen in the back stack
   */
  open fun navigateTo(destination: TopLevelDestination) {

    navController.navigate(destination.route) {
      // Pop up to the start destination of the graph to
      // avoid building up a large stack of destinations
      popUpTo(navController.graph.findStartDestination().id) {
        saveState = true
        inclusive = true
      }

      // Avoid multiple copies of the same destination when reselecting same item
      launchSingleTop = true

      // Restore state when reselecting a previously selected item
      if (destination.route != Route.AUTH) {
        restoreState = true
      }
    }
  }

  /**
   * Navigate to the specified screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
  }

  /** Navigate back to the previous screen. */
  open fun goBack() {
    navController.popBackStack()
  }

  /**
   * Get the current route of the navigation controller.
   *
   * @return The current route
   */
  open fun currentRoute(): String {
    return navController.currentDestination?.route ?: ""
  }

  /**
   * Pushes an Id to the screen navigation stack (either folder id or user id)
   *
   * @param id The Id to push.
   */
  open fun pushToScreenNavigationStack(id: String) {
    screenNavigationStack.push(id)
  }

  /**
   * Pops an Id from the screen navigation stack.
   *
   * @return The popped Id.
   */
  open fun popFromScreenNavigationStack(): String? {
    return if (screenNavigationStack.isEmpty()) null else screenNavigationStack.pop()
  }

  /** Clears the screen navigation stack. */
  open fun clearScreenNavigationStack() {
    screenNavigationStack.clear()
  }

  /** Returns the top of the screen navigation stack. */
  open fun retrieveTopElementOfScreenNavigationStack(): String {
    return screenNavigationStack.peek()
  }
}
