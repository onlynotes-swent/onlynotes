package com.github.onlynotesswent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.notification.NotificationViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.authentication.SignInScreen
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.FolderContentScreen
import com.github.onlynotesswent.ui.overview.OverviewScreen
import com.github.onlynotesswent.ui.overview.editnote.CommentsScreen
import com.github.onlynotesswent.ui.overview.editnote.EditMarkdownScreen
import com.github.onlynotesswent.ui.overview.editnote.EditNoteScreen
import com.github.onlynotesswent.ui.overview.editnote.PdfViewerScreen
import com.github.onlynotesswent.ui.search.SearchScreen
import com.github.onlynotesswent.ui.theme.AppTheme
import com.github.onlynotesswent.ui.user.CreateUserScreen
import com.github.onlynotesswent.ui.user.EditProfileScreen
import com.github.onlynotesswent.ui.user.NotificationScreen
import com.github.onlynotesswent.ui.user.PublicProfileScreen
import com.github.onlynotesswent.ui.user.UserProfileScreen
import com.github.onlynotesswent.utils.ProfilePictureTaker
import com.github.onlynotesswent.utils.Scanner

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Retrieve the server client ID from resources
    val serverClientId = getString(R.string.default_web_client_id)

    val scanner = Scanner(this).apply { init() }
    val profilePictureTaker = ProfilePictureTaker(this).apply { init() }

    setContent {
      AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          OnlyNotesApp(scanner, profilePictureTaker, serverClientId)
        }
      }
    }
  }
}

@Composable
fun OnlyNotesApp(
    scanner: Scanner,
    profilePictureTaker: ProfilePictureTaker,
    serverClientId: String
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
  val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModel.Factory)
  val fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory)
  val folderViewModel: FolderViewModel = viewModel(factory = FolderViewModel.Factory)
  val notificationViewModel: NotificationViewModel =
      viewModel(factory = NotificationViewModel.Factory)

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel, serverClientId) }
      composable(Screen.CREATE_USER) { CreateUserScreen(navigationActions, userViewModel) }
    }

    navigation(
        startDestination = Screen.OVERVIEW,
        route = Route.OVERVIEW,
    ) {
      composable(Screen.OVERVIEW) {
        OverviewScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
      }
      composable(Screen.EDIT_NOTE) {
        EditNoteScreen(navigationActions, noteViewModel, userViewModel)
      }
      composable(Screen.EDIT_NOTE_COMMENT) {
        CommentsScreen(navigationActions, noteViewModel, userViewModel)
      }
      composable(Screen.EDIT_NOTE_PDF) {
        PdfViewerScreen(noteViewModel, fileViewModel, scanner, navigationActions)
      }
      composable(Screen.EDIT_NOTE_MARKDOWN) {
        EditMarkdownScreen(navigationActions, noteViewModel, fileViewModel)
      }
      composable(
          route = Screen.FOLDER_CONTENTS,
          enterTransition = { scaleIn(animationSpec = tween(300, easing = EaseIn)) }) {
              navBackStackEntry ->
            val folderId = navBackStackEntry.arguments?.getString("folderId")
            val selectedFolder by folderViewModel.selectedFolder.collectAsState()
            // Update the selected folder when the folder ID changes
            LaunchedEffect(folderId) {
              if (folderId != null && folderId != "{folderId}") {
                folderViewModel.getFolderById(folderId)
              }
            }
            // Wait until selected folder is updated to display the screen
            if (selectedFolder != null) {
              FolderContentScreen(navigationActions, folderViewModel, noteViewModel, userViewModel)
            }
          }
    }

    navigation(
        startDestination = Screen.SEARCH,
        route = Route.SEARCH,
    ) {
      composable(Screen.SEARCH) {
        SearchScreen(
            navigationActions, noteViewModel, userViewModel, folderViewModel, fileViewModel)
      }
    }

    navigation(
        startDestination = Screen.USER_PROFILE,
        route = Route.PROFILE,
    ) {
      composable(Screen.USER_PROFILE) {
        UserProfileScreen(navigationActions, userViewModel, fileViewModel, notificationViewModel)
      }
      composable(Screen.PUBLIC_PROFILE) {
        PublicProfileScreen(navigationActions, userViewModel, fileViewModel, notificationViewModel)
        composable(Screen.PUBLIC_PROFILE) { navBackStackEntry ->
          val userId = navBackStackEntry.arguments?.getString("userId")

          // Refresh the user profile when the user Id changes
          LaunchedEffect(userId) {
            if (userId != null && userId != "{userId}") {
              userViewModel.refreshProfileUser(userId)
            }
          }
          PublicProfileScreen(
              navigationActions, userViewModel, fileViewModel, notificationViewModel)
        }
      }
      composable(Screen.EDIT_PROFILE) {
        EditProfileScreen(
            navigationActions,
            userViewModel,
            profilePictureTaker,
            fileViewModel,
            noteViewModel,
            folderViewModel,
            notificationViewModel)
      }
      composable(Screen.NOTIFICATIONS) {
        NotificationScreen(userViewModel, navigationActions, fileViewModel, notificationViewModel)
      }
    }
  }
}
