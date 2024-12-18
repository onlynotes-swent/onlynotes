package com.github.onlynotesswent.ui.common

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Public
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.common.Visibility

/**
 * Composable function to display a row of visibility options. The user can cycle through the
 * visibility options by clicking the left and right arrows.
 *
 * @param visibility The current visibility option.
 * @param isOwner Whether the current user is the owner of the note.
 * @param onVisibilityChange The action to perform when the visibility option is changed.
 */
@Composable
fun SelectVisibility(
    visibility: Visibility?,
    isOwner: Boolean,
    onVisibilityChange: (Visibility) -> Unit
) {
  var selectedIndex by remember {
    mutableIntStateOf(Visibility.entries.indexOf(visibility ?: Visibility.DEFAULT))
  }

  // Track the current visibility based on index
  val currentVisibility = Visibility.entries[selectedIndex]

  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement =
        if (isOwner) Arrangement.SpaceBetween else Arrangement.Center) {
        // Left arrow to scroll backward
        if (isOwner) {
          IconButton(
              onClick = {
                selectedIndex =
                    if (selectedIndex > 0) selectedIndex - 1 else Visibility.entries.lastIndex
                onVisibilityChange(Visibility.entries[selectedIndex])
              },
              modifier = Modifier.testTag("previousVisibility")) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Visibility")
              }
        }
        // Crossfade animation for smooth visibility transitions
        Crossfade(targetState = currentVisibility, label = "VisibilityCrossfade") { targetVisibility
          ->
          Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.testTag("currentVisibilityOption")) {
                // Smooth scaling animation
                val scale by
                    animateFloatAsState(
                        targetValue = if (visibility == targetVisibility) 1.5f else 1.0f,
                        animationSpec =
                            androidx.compose.animation.core.spring(
                                dampingRatio = 0.5f, stiffness = 100f),
                        label = "ScaleAnimation")

                Icon(
                    imageVector = getVisibilityIcon(targetVisibility),
                    contentDescription = targetVisibility.toReadableString(),
                    modifier = Modifier.scale(scale).padding(8.dp))
                Text(
                    text = targetVisibility.toReadableString(),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = 4.dp))
              }
        }

        // Right arrow to scroll forward
        if (isOwner) {
          IconButton(
              onClick = {
                selectedIndex =
                    if (selectedIndex < Visibility.entries.lastIndex) selectedIndex + 1 else 0
                onVisibilityChange(Visibility.entries[selectedIndex])
              },
              modifier = Modifier.testTag("nextVisibility")) {
                Icon(
                    imageVector = Icons.Default.ChevronRight,
                    contentDescription = "Next Visibility")
              }
        }
      }
}

// Helper function to retrieve the correct icon for a visibility option
@Composable
private fun getVisibilityIcon(visibility: Visibility): ImageVector {
  return when (visibility) {
    Visibility.PUBLIC -> Icons.Default.Public
    Visibility.FRIENDS -> Icons.Default.Group
    Visibility.PRIVATE -> Icons.Default.Lock
  }
}
