package com.github.onlynotesswent.ui.common

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.common.Visibility
import kotlinx.coroutines.launch

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
  val pagerState =
      rememberPagerState(
          initialPage = Visibility.entries.indexOf(visibility ?: Visibility.DEFAULT),
          pageCount = { Visibility.entries.size },
      )
  val coroutineScope = rememberCoroutineScope()

  Row(
      modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = if (isOwner) Arrangement.SpaceBetween else Arrangement.Center) {
        if (isOwner) {
          IconButton(
              onClick = {
                coroutineScope.launch {
                  val previousPage =
                      if (pagerState.currentPage > 0) {
                        pagerState.currentPage - 1
                      } else {
                        Visibility.entries.lastIndex
                      }
                  pagerState.animateScrollToPage(previousPage)
                  onVisibilityChange(Visibility.entries[previousPage])
                }
              },
              modifier = Modifier.testTag("previousVisibility")) {
                Icon(
                    imageVector = Icons.Default.ChevronLeft,
                    contentDescription = "Previous Visibility")
              }
        }

        // Horizontal Pager
        HorizontalPager(
            userScrollEnabled = isOwner,
            state = pagerState,
            modifier = Modifier.weight(1f).testTag("visibilityPager").fillMaxWidth()) { page ->
              val targetVisibility = Visibility.entries[page]
              Column(
                  horizontalAlignment = Alignment.CenterHorizontally,
                  modifier = Modifier.testTag("currentVisibilityOption").fillMaxWidth()) {
                    Icon(
                        imageVector = getVisibilityIcon(targetVisibility),
                        contentDescription = targetVisibility.toReadableString(),
                        modifier = Modifier.padding(8.dp))
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
                coroutineScope.launch {
                  val nextPage =
                      if (pagerState.currentPage < Visibility.entries.lastIndex) {
                        pagerState.currentPage + 1
                      } else {
                        0
                      }
                  pagerState.animateScrollToPage(nextPage)
                  onVisibilityChange(Visibility.entries[nextPage])
                }
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
