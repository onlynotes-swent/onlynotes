package com.github.onlynotesswent.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import com.github.onlynotesswent.model.scanner.Scanner
import com.github.onlynotesswent.ui.navigation.NavigationActions
import com.github.onlynotesswent.ui.navigation.Screen

// simple empty screen for testing navigation
@Composable
fun OverviewScreen(navigationActions: NavigationActions, scanner: Scanner) {
  Column {
    Text("Overview Screen")
    Button(onClick = { navigationActions.navigateTo(Screen.AUTH) }) { Text("Go to Auth") }
    Button(onClick = { scanner.scan() }) { Text("Scan Documents") }
  }
}
