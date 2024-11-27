package com.github.onlynotesswent.ui.common

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.common.Visibility

@Composable
fun SelectVisibility(visibility: Visibility?, onVisibilityChange: (Visibility) -> Unit) {
  Column(modifier = Modifier.fillMaxWidth()) {
    Text(
        text = "Visibility",
        style = MaterialTheme.typography.titleMedium,
        modifier = Modifier.padding(bottom = 8.dp))

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
      Visibility.entries.forEach { visibilityOption ->
        val isSelected = visibility == visibilityOption
        val animatedScale = animateFloatAsState(if (isSelected) 1.1f else 1.0f, label = "")

        Button(
            onClick = { onVisibilityChange(visibilityOption) },
            colors =
                ButtonDefaults.buttonColors(
                    containerColor =
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surface,
                    contentColor =
                        if (isSelected) MaterialTheme.colorScheme.onPrimary
                        else MaterialTheme.colorScheme.onSurface),
            modifier =
                Modifier.weight(1f)
                    .padding(horizontal = 7.dp)
                    .scale(animatedScale.value)
                    .testTag("VisibilityEditMenu" + visibilityOption.toReadableString())) {
              Icon(
                  imageVector =
                      when (visibilityOption) {
                        Visibility.PUBLIC -> Icons.Default.Public
                        Visibility.FRIENDS -> Icons.Default.Group
                        Visibility.PRIVATE -> Icons.Default.Lock
                      },
                  contentDescription = visibilityOption.toReadableString(),
                  modifier =
                      Modifier.padding(end = 4.dp).testTag(visibilityOption.toReadableString()))
              Text(visibilityOption.toReadableString())
            }
      }
    }
  }
}
