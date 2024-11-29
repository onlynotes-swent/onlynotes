package com.github.onlynotesswent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.onlynotesswent.model.authentication.Authenticator
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.authentication.SignInScreen
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.AddNoteScreen
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
import com.github.onlynotesswent.ui.user.PublicProfileScreen
import com.github.onlynotesswent.ui.user.UserProfileScreen
import com.github.onlynotesswent.utils.ProfilePictureTaker
import com.github.onlynotesswent.utils.Scanner

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val scanner = Scanner(this).apply { init() }
    val profilePictureTaker = ProfilePictureTaker(this).apply { init() }

    setContent {
      AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          OnlyNotesApp(scanner, profilePictureTaker)
        }
      }
    }
  }
}

@Composable
fun OnlyNotesApp(
    scanner: Scanner,
    profilePictureTaker: ProfilePictureTaker,
) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val authenticator = Authenticator(LocalContext.current)
  val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
  val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModel.Factory)
  val fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory)
  val folderViewModel: FolderViewModel = viewModel(factory = FolderViewModel.Factory)

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel, authenticator) }
      composable(Screen.CREATE_USER) { CreateUserScreen(navigationActions, userViewModel) }
    }

    navigation(
        startDestination = Screen.OVERVIEW,
        route = Route.OVERVIEW,
    ) {
      composable(Screen.OVERVIEW) {
        OverviewScreen(navigationActions, noteViewModel, userViewModel, folderViewModel)
      }
      composable(Screen.ADD_NOTE) {
        AddNoteScreen(navigationActions, scanner, noteViewModel, userViewModel, fileViewModel)
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
      composable(Screen.FOLDER_CONTENTS) {
        FolderContentScreen(navigationActions, folderViewModel, noteViewModel, userViewModel)
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
        UserProfileScreen(navigationActions, userViewModel, fileViewModel, authenticator)
      }
      composable(Screen.PUBLIC_PROFILE) {
        PublicProfileScreen(navigationActions, userViewModel, fileViewModel, authenticator)
      }
      composable(Screen.EDIT_PROFILE) {
        EditProfileScreen(
            navigationActions,
            userViewModel,
            profilePictureTaker,
            fileViewModel,
            noteViewModel,
            folderViewModel,
            authenticator,)
      }
    }
  }
}
