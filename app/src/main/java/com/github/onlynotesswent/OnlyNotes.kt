package com.github.onlynotesswent

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.core.EaseIn
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.onlynotesswent.model.authentication.Authenticator
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.notification.NotificationViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.authentication.SignInScreen
import com.github.onlynotesswent.ui.deck.DeckPlayScreen
import com.github.onlynotesswent.ui.deck.DeckScreen
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.DeckOverviewScreen
import com.github.onlynotesswent.ui.overview.FolderContentScreen
import com.github.onlynotesswent.ui.overview.NoteOverviewScreen
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
import com.github.onlynotesswent.utils.NotesToFlashcard
import com.github.onlynotesswent.utils.OpenAI
import com.github.onlynotesswent.utils.PictureTaker
import com.github.onlynotesswent.utils.Scanner
import com.github.onlynotesswent.utils.TextExtractor
import kotlinx.coroutines.flow.firstOrNull

class OnlyNotes : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    val scanner = Scanner(this).apply { init() }
    val pictureTaker = PictureTaker(this).apply { init() }
    val textExtractor = TextExtractor(this)

    setContent {
      AppTheme {
        Surface(modifier = Modifier.fillMaxSize()) {
          OnlyNotesApp(scanner, pictureTaker, textExtractor)
        }
      }
    }
  }
}

@Composable
fun OnlyNotesApp(scanner: Scanner, pictureTaker: PictureTaker, textExtractor: TextExtractor) {
  val context = LocalContext.current

  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val authenticator = Authenticator(LocalContext.current)

  val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
  val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModel.factory(context))
  val fileViewModel: FileViewModel = viewModel(factory = FileViewModel.Factory)
  val folderViewModel: FolderViewModel = viewModel(factory = FolderViewModel.factory(context))
  val notificationViewModel: NotificationViewModel =
      viewModel(factory = NotificationViewModel.Factory)
  val deckViewModel: DeckViewModel = viewModel(factory = DeckViewModel.Factory)
  val flashcardViewModel: FlashcardViewModel = viewModel(factory = FlashcardViewModel.Factory)
  val openAI = OpenAI()
  val notesToFlashcard =
      NotesToFlashcard(
          flashcardViewModel = flashcardViewModel,
          fileViewModel = fileViewModel,
          deckViewModel = deckViewModel,
          noteViewModel = noteViewModel,
          folderViewModel = folderViewModel,
          openAIClient = openAI,
          context = context)

  val isInitialized = remember { mutableStateOf(false) }
  val startDestination = remember { mutableStateOf(Route.AUTH) }

  LaunchedEffect(Unit) {
    val authEmail = authenticator.authManager.userEmailFlow.firstOrNull()
    if (authEmail != null) {
      userViewModel.getCurrentUserByEmail(
          email = authEmail,
          onSuccess = {
            startDestination.value = Route.NOTE_OVERVIEW
            isInitialized.value = true
          },
          onUserNotFound = {
            /* Stay in the auth screen */
            isInitialized.value = true
          },
          onFailure = {
            /* Stay in the auth screen */
            Toast.makeText(context, "Error fetching user", Toast.LENGTH_LONG).show()
            isInitialized.value = true
          })
    } else {
      isInitialized.value = true
    }
  }

  if (isInitialized.value) {
    NavHost(navController = navController, startDestination = startDestination.value) {
      navigation(
          startDestination = Screen.AUTH,
          route = Route.AUTH,
      ) {
        composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel, authenticator) }
        composable(Screen.CREATE_USER) { CreateUserScreen(navigationActions, userViewModel) }
      }

      navigation(
          startDestination = Screen.NOTE_OVERVIEW,
          route = Route.NOTE_OVERVIEW,
      ) {
        composable(Screen.NOTE_OVERVIEW) {
          val user = userViewModel.currentUser.collectAsState().value
          val currentBackStackEntry = navController.currentBackStackEntryAsState().value

          LaunchedEffect(currentBackStackEntry) {
            if (user != null) {
              folderViewModel.getRootNoteFoldersFromUserId(user.uid)
              noteViewModel.getRootNotesFromUid(user.uid)
            }
          }

          NoteOverviewScreen(
              navigationActions, noteViewModel, userViewModel, folderViewModel, notesToFlashcard)
        }
        composable(Screen.EDIT_NOTE) {
          EditNoteScreen(navigationActions, noteViewModel, userViewModel)
        }
        composable(Screen.EDIT_NOTE_COMMENT) {
          CommentsScreen(navigationActions, noteViewModel, userViewModel, fileViewModel)
        }
        composable(Screen.EDIT_NOTE_PDF) {
          PdfViewerScreen(
              navigationActions,
              noteViewModel,
              fileViewModel,
              userViewModel,
              scanner,
              textExtractor)
        }
        composable(Screen.EDIT_NOTE_MARKDOWN) {
          EditMarkdownScreen(navigationActions, noteViewModel, fileViewModel, userViewModel)
        }
        composable(
            route = Screen.FOLDER_CONTENTS,
            enterTransition = { scaleIn(animationSpec = tween(300, easing = EaseIn)) },
            popExitTransition = {
              fadeOut(animationSpec = tween(300, easing = LinearEasing)) +
                  slideOutOfContainer(
                      animationSpec = tween(300, easing = EaseOut),
                      towards = AnimatedContentTransitionScope.SlideDirection.End)
            },
            popEnterTransition = { null }) { navBackStackEntry ->
              val folderId = navBackStackEntry.arguments?.getString("folderId")
              val selectedFolder by folderViewModel.selectedFolder.collectAsState()
              // Update the selected folder when the folder ID changes
              LaunchedEffect(folderId) {
                if (folderId != null && folderId != "{folderId}") {
                  folderViewModel.getFolderById(folderId)
                  noteViewModel.getNotesFromFolder(folderId, userViewModel)
                  folderViewModel.getSubFoldersOf(folderId, userViewModel)
                }
              }
              // Wait until selected folder is updated to display the screen
              if (selectedFolder != null) {
                FolderContentScreen(
                    navigationActions = navigationActions,
                    folderViewModel = folderViewModel,
                    userViewModel = userViewModel,
                    noteViewModel = noteViewModel,
                    notesToFlashcard = notesToFlashcard)
              }
            }
      }

      navigation(
          startDestination = Screen.DECK_OVERVIEW,
          route = Route.DECK_OVERVIEW,
      ) {
        composable(Screen.DECK_OVERVIEW) {
          val user = userViewModel.currentUser.collectAsState().value
          val currentBackStackEntry = navController.currentBackStackEntryAsState().value

          LaunchedEffect(currentBackStackEntry) {
            if (user != null) {
              folderViewModel.getRootDeckFoldersFromUserId(user.uid)
              deckViewModel.getRootDecksFromUserId(user.uid)
            }
          }

          DeckOverviewScreen(navigationActions, deckViewModel, userViewModel, folderViewModel)
        }
        composable(Screen.DECK_MENU) { navBackStackEntry ->
          val deckId = navBackStackEntry.arguments?.getString("deckId")
          deckId?.let { deckViewModel.getDeckById(it) }
          DeckScreen(
              userViewModel,
              deckViewModel,
              flashcardViewModel,
              fileViewModel,
              folderViewModel,
              pictureTaker,
              navigationActions)
        }
        composable(Screen.DECK_PLAY) { navBackStackEntry ->
          val deckId = navBackStackEntry.arguments?.getString("deckId")
          val mode = navBackStackEntry.arguments?.getString("mode")

          // Refresh deck if it is not null
          LaunchedEffect(deckId) {
            if (deckId != null && deckId != "{deckId}")
                deckViewModel.getDeckById(
                    deckId, { deckViewModel.playDeckWithMode(it, Deck.PlayMode.fromString(mode)) })
          }
          DeckPlayScreen(
              navigationActions, userViewModel, deckViewModel, flashcardViewModel, fileViewModel)
        }
        composable(
            route = Screen.FOLDER_CONTENTS,
            enterTransition = { scaleIn(animationSpec = tween(300, easing = EaseIn)) },
            popExitTransition = {
              fadeOut(animationSpec = tween(300, easing = LinearEasing)) +
                  slideOutOfContainer(
                      animationSpec = tween(300, easing = EaseOut),
                      towards = AnimatedContentTransitionScope.SlideDirection.End)
            },
            popEnterTransition = { null }) { navBackStackEntry ->
              val folderId = navBackStackEntry.arguments?.getString("folderId")
              val selectedFolder by folderViewModel.selectedFolder.collectAsState()
              // Update the selected folder when the folder ID changes
              LaunchedEffect(folderId) {
                if (folderId != null && folderId != "{folderId}") {
                  folderViewModel.getFolderById(folderId)
                  deckViewModel.getDecksByFolder(folderId)
                  folderViewModel.getSubFoldersOf(folderId, userViewModel)
                }
              }
              // Wait until selected folder is updated to display the screen
              if (selectedFolder != null) {
                FolderContentScreen(
                    navigationActions = navigationActions,
                    folderViewModel = folderViewModel,
                    userViewModel = userViewModel,
                    deckViewModel = deckViewModel,
                    isDeckView = true)
              }
            }
      }

      navigation(
          startDestination = Screen.SEARCH,
          route = Route.SEARCH,
      ) {
        composable(Screen.SEARCH) {
          SearchScreen(
              navigationActions,
              noteViewModel,
              userViewModel,
              folderViewModel,
              deckViewModel,
              fileViewModel)
        }
      }

      navigation(
          startDestination = Screen.USER_PROFILE,
          route = Route.PROFILE,
      ) {
        composable(Screen.USER_PROFILE) {
          UserProfileScreen(
              navigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
        }

        composable(Screen.PUBLIC_PROFILE) { navBackStackEntry ->
          val userId = navBackStackEntry.arguments?.getString("userId")

          // Refresh the user profile when the user Id changes
          LaunchedEffect(userId) {
            if (userId != null && userId != "{userId}") {
              userViewModel.refreshProfileUser(userId)
            }
          }
          PublicProfileScreen(
              navigationActions, userViewModel, fileViewModel, notificationViewModel, authenticator)
        }

        composable(Screen.EDIT_PROFILE) {
          EditProfileScreen(
              navigationActions,
              userViewModel,
              pictureTaker,
              fileViewModel,
              noteViewModel,
              folderViewModel,
              deckViewModel,
              flashcardViewModel,
              notificationViewModel,
          )
        }
        composable(Screen.NOTIFICATIONS) {
          NotificationScreen(userViewModel, navigationActions, fileViewModel, notificationViewModel)
        }
      }
    }
  }
}
