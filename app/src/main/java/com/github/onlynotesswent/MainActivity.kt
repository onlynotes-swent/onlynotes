package com.github.onlynotesswent

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    // Retrieve the server client ID from resources
    //val serverClientId = getString(R.string.default_web_client_id)


    setContent {
        Surface(modifier = Modifier.fillMaxSize()) {
            BlankScreen()
          //OnlyNotesApp(scanner, profilePictureTaker, serverClientId)
        }

    }
  }
}

@Composable
fun BlankScreen() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Welcome to OnlyNotes!"
        )
    }
}
