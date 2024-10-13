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
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Route
import com.github.onlynotesswent.ui.navigation.Screen
import com.github.onlynotesswent.ui.screen.AddNoteScreen
import com.github.onlynotesswent.ui.screen.AuthenticationScreen
import com.github.onlynotesswent.ui.screen.EditNoteScreen
import com.github.onlynotesswent.ui.screen.OverviewScreen
import com.github.onlynotesswent.ui.theme.SampleAppTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContent { SampleAppTheme { Surface(modifier = Modifier.fillMaxSize()) { OnlyNotesApp() } } }
  }
}

@Composable
fun OnlyNotesApp() {
  val navController = rememberNavController()
  val navigationActions = NavigationActions(navController)
  val noteViewModel: NoteViewModel = viewModel(factory = NoteViewModel.Factory)

  NavHost(navController = navController, startDestination = Route.AUTH) {
    navigation(
        startDestination = Screen.AUTH,
        route = Route.AUTH,
    ) {
      composable(Screen.AUTH) { AuthenticationScreen(navigationActions) }
    }

    navigation(
        startDestination = Screen.OVERVIEW,
        route = Route.OVERVIEW,
    ) {
      composable(Screen.OVERVIEW) { OverviewScreen(navigationActions, noteViewModel) }
      composable(Screen.ADD_NOTE) { AddNoteScreen(navigationActions, noteViewModel) }
      composable(Screen.EDIT_NOTE) { EditNoteScreen(navigationActions, noteViewModel) }
    }
    navigation(
        startDestination = Screen.SEARCH_NOTE,
        route = Route.SEARCH,
    ) {
      composable(Screen.SEARCH_NOTE) { // SearchScreen(navigationActions, noteViewModel)
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
}
