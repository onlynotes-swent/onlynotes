package com.github.onlynotesswent.ui.testScreenForNavigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

// simple empty screen for testing navigation
@Composable
fun AuthenticationScreen(navigationActions: NavigationActions) {
  Column {
    Text("Authentication Screen")
    Button(onClick = { navigationActions.navigateTo(Screen.OVERVIEW) }) { Text("Go to Overview") }
  }
}
