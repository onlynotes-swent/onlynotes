package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp

/**
 * A composable function that displays the top app bar for the screen. It is composed of an icon
 * button and a title.
 *
 * @param title The title to be displayed in the top app bar.
 * @param titleTestTag The test tag for the title.
 * @param onBackClick The callback to be invoked when the back button is clicked.
 * @param icon The icon to be displayed in the top app bar.
 * @param iconTestTag The test tag for the icon.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScreenTopBar(
    title: String,
    titleTestTag: String,
    onBackClick: () -> Unit,
    icon: @Composable () -> Unit,
    iconTestTag: String
) {
  Column {
    TopAppBar(
        colors =
            TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.surface),
        title = {
          Row(
              modifier = Modifier.fillMaxWidth(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center) {
                Spacer(modifier = Modifier.weight(1.4f))
                Text(
                    title,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.testTag(titleTestTag))
                Spacer(modifier = Modifier.weight(2f))
              }
        },
        navigationIcon = {
          IconButton(onClick = onBackClick, Modifier.testTag(iconTestTag), content = icon)
        })
    HorizontalDivider(
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f), thickness = 0.5.dp)
  }
}
