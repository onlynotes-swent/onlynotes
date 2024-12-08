package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Composable function to display a loading indicator.
 *
 * @param text The text to display below the loading indicator.
 * @param modifier The modifier to apply to the column containing the loading indicator, defaults to
 *   Modifier.fillMaxSize().
 * @param style The style to apply to the text, defaults to MaterialTheme.typography.bodyMedium.
 * @param loadingIndicatorSize The size of the loading indicator, defaults to 48.dp.
 * @param spacerHeight The height of the spacer between the loading indicator and the text, defaults
 *   to 16.dp.
 */
@Composable
fun LoadingIndicator(
    text: String,
    modifier: Modifier = Modifier.fillMaxSize(),
    style: TextStyle = MaterialTheme.typography.bodyMedium,
    loadingIndicatorSize: Dp = 48.dp,
    spacerHeight: Dp = 16.dp
) {
  Column(
      modifier = modifier,
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(
            modifier = Modifier.size(loadingIndicatorSize),
            color = MaterialTheme.colorScheme.primary)
        Spacer(modifier = Modifier.height(spacerHeight))
        Text(text = text, style = style)
      }
}
