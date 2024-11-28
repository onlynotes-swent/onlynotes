package com.github.onlynotesswent.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import com.github.onlynotesswent.model.folder.Folder
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
  const val EDIT_NOTE = "Edit Note Screen"
  const val EDIT_NOTE_COMMENT = "Comment Note Screen"
  const val EDIT_NOTE_PDF = "Edit Note PDF Screen"
  const val EDIT_NOTE_MARKDOWN = "Edit Note Markdown Screen"
  const val SEARCH = "Search Screen"
  const val USER_PROFILE = "User Profile Screen"
  const val PUBLIC_PROFILE = "Public Profile Screen"
  const val EDIT_PROFILE = "Edit Profile Screen"
  const val NOTIFICATIONS = "Notifications Screen"
  const val FOLDER_CONTENTS = "Folder Contents Screen"
}

data class Destination(
    val route: String? = null,
    val screen: String? = null,
    val icon: ImageVector,
    val textId: String
)

object TopLevelDestinations {
  val OVERVIEW = Destination(route = Route.OVERVIEW, icon = Icons.Outlined.Home, textId = "Notes")
  val SEARCH = Destination(route = Route.SEARCH, icon = Icons.Outlined.Search, textId = "Search")
  val PROFILE = Destination(route = Route.PROFILE, icon = Icons.Outlined.Person, textId = "Profile")
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(TopLevelDestinations.OVERVIEW, TopLevelDestinations.SEARCH, TopLevelDestinations.PROFILE)

object EditNoteDestinations {
  val DETAIL =
      Destination(screen = Screen.EDIT_NOTE, icon = Icons.Filled.Settings, textId = "Detail")
  val COMMENT =
      Destination(
          screen = Screen.EDIT_NOTE_COMMENT,
          icon = Icons.AutoMirrored.Filled.Comment,
          textId = "Comments")
  val PDF =
      Destination(screen = Screen.EDIT_NOTE_PDF, icon = Icons.Filled.PictureAsPdf, textId = "PDF")
  val MARKDOWN =
      Destination(
          screen = Screen.EDIT_NOTE_MARKDOWN, icon = Icons.Filled.EditNote, textId = "Content")
}

val LIST_EDIT_NOTE_DESTINATION =
    listOf(
        EditNoteDestinations.DETAIL,
        EditNoteDestinations.COMMENT,
        EditNoteDestinations.PDF,
        EditNoteDestinations.MARKDOWN)

open class NavigationActions(
    private val navController: NavHostController,
) {

  private val screenNavigationStack = Stack<String>()
  /**
   * Navigate to the specified [Destination] and clear the navigation stack.
   *
   * @param destination The top level destination to navigate to Clear the back stack when
   *   navigating to a new destination This is useful when navigating to a new screen from the
   *   bottom navigation bar as we don't want to keep the previous screen in the back stack
   */
  open fun navigateTo(destination: Destination) {

    destination.route?.let {
      navController.navigate(it) {
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
    clearScreenNavigationStack()
  }

  /**
   * Navigate to the specified screen and clear the navigation stack when navigating to search
   * screen.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateTo(screen: String) {
    navController.navigate(screen)
    if (screen == Screen.SEARCH) {
      clearScreenNavigationStack()
      pushToScreenNavigationStack(screen)
    }
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

  /**
   * A function that handles the navigation to the folder content screen by using the screen
   * navigation stack.
   *
   * @param folder The folder to navigate to. If it is not a root folder, its parent id will be
   *   pushed to the screen navigation stack to properly go back.
   */
  open fun navigateToFolderContents(folder: Folder) {
    if (folder.parentFolderId == null) {
      // Don't add to the screen navigation stack as we are at the root folder
      navigateTo(Screen.FOLDER_CONTENTS)
    } else {
      val poppedId = popFromScreenNavigationStack()
      if (poppedId == Screen.SEARCH) {
        // If we come from search, don't push the folderId to the stack
        pushToScreenNavigationStack(poppedId)
      } else {
        if (poppedId != null) {
          pushToScreenNavigationStack(poppedId)
        }
        // Add the previously visited folder Id (parent) to the screen navigation stack
        pushToScreenNavigationStack(folder.parentFolderId)
      }
      navigateTo(Screen.FOLDER_CONTENTS)
    }
  }
}
