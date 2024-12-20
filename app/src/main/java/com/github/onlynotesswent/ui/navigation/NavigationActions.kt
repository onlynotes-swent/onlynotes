package com.github.onlynotesswent.ui.navigation

import android.util.Log
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Comment
import androidx.compose.material.icons.automirrored.outlined.LibraryBooks
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
import com.github.onlynotesswent.model.user.User

object Route {
  const val NOTE_OVERVIEW = "Note Overview"
  const val DECK_OVERVIEW = "Deck Overview"
  const val AUTH = "Auth"
  const val SEARCH = "Search"
  const val PROFILE = "Profile"
}

object Screen {
  const val AUTH = "Auth Screen"
  const val NOTE_OVERVIEW = "Note Overview Screen"
  const val DECK_OVERVIEW = "Deck Overview Screen"
  const val CREATE_USER = "Create User Screen"
  const val EDIT_NOTE = "Edit Note Screen"
  const val EDIT_NOTE_COMMENT = "Comment Note Screen"
  const val EDIT_NOTE_PDF = "Edit Note PDF Screen"
  const val EDIT_NOTE_MARKDOWN = "Edit Note Markdown Screen"
  const val SEARCH = "Search Screen"
  const val USER_PROFILE = "User Profile Screen"
  const val PUBLIC_PROFILE = "Public Profile Screen/{userId}"
  const val EDIT_PROFILE = "Edit Profile Screen"
  const val NOTIFICATIONS = "Notifications Screen"
  const val FOLDER_CONTENTS = "Folder Contents Screen/{folderId}"
  const val DECK_MENU = "Deck Menu Screen/{deckId}"
  const val DECK_PLAY = "Deck Play Screen/{deckId}/{mode}"
}

data class Destination(
    val route: String? = null,
    val screen: String? = null,
    val icon: ImageVector,
    val textId: String
)

object TopLevelDestinations {
  val NOTE_OVERVIEW =
      Destination(route = Route.NOTE_OVERVIEW, icon = Icons.Outlined.Home, textId = "Notes")
  val DECK_OVERVIEW =
      Destination(
          route = Route.DECK_OVERVIEW,
          icon = Icons.AutoMirrored.Outlined.LibraryBooks,
          textId = "Decks")
  val SEARCH = Destination(route = Route.SEARCH, icon = Icons.Outlined.Search, textId = "Search")
  val PROFILE = Destination(route = Route.PROFILE, icon = Icons.Outlined.Person, textId = "Profile")
}

val LIST_TOP_LEVEL_DESTINATION =
    listOf(
        TopLevelDestinations.NOTE_OVERVIEW,
        TopLevelDestinations.DECK_OVERVIEW,
        TopLevelDestinations.SEARCH,
        TopLevelDestinations.PROFILE)

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

  companion object {
    const val TAG = "NavigationActions"
  }

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
  }

  /**
   * Navigate to the specified screen and clear the navigation stack when navigating to search
   * screen.
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
   * Handle navigation when inside a folder.
   *
   * @param folder The current folder to navigate back from
   * @param currentUser The current user
   * @param isDeckView Whether the current view is a deck view
   */
  open fun goBackFolderContents(folder: Folder, currentUser: User, isDeckView: Boolean = false) {
    goBack()
    //      if (!folder.isOwner(currentUser.uid) || folder.parentFolderId != null) {
    //
    //    } else {
    //      if (isDeckView) {
    //        navigateTo(TopLevelDestinations.DECK_OVERVIEW)
    //      } else {
    //        navigateTo(TopLevelDestinations.NOTE_OVERVIEW)
    //      }
    //    }
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
   * Get the previous screen from the navigation controller.
   *
   * @return The previous screen
   */
  open fun getPreviousScreen(): String? {
    return navController.previousBackStackEntry?.destination?.route
  }

  /**
   * Navigate to the specified screen and immediately clears it from the back stack.
   *
   * @param screen The screen to navigate to
   */
  open fun navigateToAndPop(screen: String) {
    navController.navigate(screen) {
      val currentRoute = navController.currentBackStackEntry?.destination?.route
      if (currentRoute != null) {
        popUpTo(currentRoute) { inclusive = true }
      } else {
        Log.e(TAG, "Current route is null")
      }
    }
  }
}
