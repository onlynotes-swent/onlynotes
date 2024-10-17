package com.github.onlynotesswent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.github.onlynotesswent.model.note.NoteViewModel
import com.github.onlynotesswent.model.scanner.Scanner
import com.github.onlynotesswent.model.users.UserViewModel
import com.github.onlynotesswent.ui.authentication.SignInScreen
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.overview.AddNoteScreen
import com.github.onlynotesswent.ui.overview.EditNoteScreen
import com.github.onlynotesswent.ui.overview.OverviewScreen
import com.github.onlynotesswent.ui.search.SearchScreen
import com.github.onlynotesswent.ui.theme.SampleAppTheme
import com.github.onlynotesswent.ui.user.CreateUserScreen

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    val scanner = Scanner(this).apply { init() }
    setContent {
      SampleAppTheme { Surface(modifier = Modifier.fillMaxSize()) { OnlyNotesApp(scanner) } }
    }
  }
}

@Composable
fun OnlyNotesApp(scanner: Scanner) {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val userViewModel: UserViewModel = viewModel(factory = UserViewModel.Factory)
  val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModel.Factory)

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { SignInScreen(navigationActions, userViewModel) }
      composable(Screen.CREATE_USER) { CreateUserScreen(navigationActions, userViewModel) }
    }

    navigation(
        startDestination = Screen.OVERVIEW,
        route = Route.OVERVIEW,
    ) {
      composable(Screen.OVERVIEW) { OverviewScreen(navigationActions, noteViewModel) }
      composable(Screen.ADD_NOTE) { AddNoteScreen(navigationActions, scanner, noteViewModel) }
      composable(Screen.EDIT_NOTE) { EditNoteScreen(navigationActions, noteViewModel) }
    }

    navigation(
        startDestination = Screen.SEARCH_NOTE,
        route = Route.SEARCH,
    ) {
      composable(Screen.SEARCH_NOTE) { SearchScreen(navigationActions, noteViewModel) }
    }

    navigation(
        startDestination = Screen.PROFILE,
        route = Route.PROFILE,
    ) {
      composable(Screen.PROFILE) { // ProfileScreen(navigationActions, ViewModel)
      }
    }
  }
}
