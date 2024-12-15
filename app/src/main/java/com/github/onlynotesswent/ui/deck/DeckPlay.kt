package com.github.onlynotesswent.ui.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.FlashcardPlayItem
import com.github.onlynotesswent.ui.navigation.NavigationActions

@Composable
fun DeckPlayScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel
) {
  val deckState = deckViewModel.selectedDeck.collectAsState()
  deckState.value?.let { deck -> flashcardViewModel.fetchFlashcardsFromDeck(deck) }

  val flashcards = flashcardViewModel.deckFlashcards.collectAsState().value

  val pagerState = rememberPagerState { flashcards.size }

    HorizontalPager(pagerState) { pageIndex ->
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            FlashcardPlayItem(
                flashcard = flashcards[pageIndex],
                userViewModel = userViewModel,
                deckViewModel = deckViewModel,
                flashcardViewModel = flashcardViewModel,
                fileViewModel = fileViewModel,
            )
        }

    }

}
