package com.github.onlynotesswent.ui.deck

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.UserFlashcard
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.flashcard.deck.PlayDeckHistory
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.FlashcardPlayItem
import com.github.onlynotesswent.ui.common.LoadingIndicator
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions

@Composable
fun DeckPlayScreen(
    navigationActions: NavigationActions,
    userViewModel: UserViewModel,
    deckViewModel: DeckViewModel,
    flashcardViewModel: FlashcardViewModel,
    fileViewModel: FileViewModel
) {

  val deck = deckViewModel.selectedDeck.collectAsState()
  val selectedFlashcard = flashcardViewModel.selectedFlashcard.collectAsState()
  val playMode = deckViewModel.selectedPlayMode.collectAsState()
  val isFinished = remember { mutableStateOf(false) }
  val currentFlashcardIndex = remember { mutableIntStateOf(0) }
  val userFlashcardList: MutableState<List<UserFlashcard>> = remember { mutableStateOf(listOf()) }
  val flashcardList = flashcardViewModel.deckFlashcards.collectAsState()
  val userViewModelFlashcards = userViewModel.deckUserFlashcards.collectAsState()
   deck.value?.let{
       flashcardViewModel.fetchFlashcardsFromDeck(
        it,
        onSuccess = { flashcards ->
            flashcardViewModel.selectFlashcard(flashcards.first())
        })
       userViewModel.getUserFlashcardFromDeck(
           it,
           onSuccess = {
               for (id in it.flashcardIds) {
                   if (userViewModel.deckUserFlashcards.value[id] == null) {
                       val userFlashcard= UserFlashcard(id)
                       userViewModel.addUserFlashcard(userFlashcard)
                       userFlashcardList.value += userFlashcard
                   }else{
                          userFlashcardList.value += userViewModel.deckUserFlashcards.value[id]!!
                   }
               }

           })
   }




  // deckViewModel.selectedDeck.value?.let { userViewModel.getUserFlashcardFromDeck(it) }
  // val flashcardUserInfo= userViewModel.deckUserFlashcards
  Scaffold(
      topBar = {
        ScreenTopBar(
            playMode.value?.toString() ?: "No mode selected",
            "DeckPlayScreen",
            { navigationActions.goBack() },
            {
              Icon(
                  imageVector = Icons.Filled.Close,
                  contentDescription = "Exit Play Deck",
                  tint = MaterialTheme.colorScheme.onSurface)
            },
            "DeckPlayIcon")
      }) { innerPadding ->
        Column(
            modifier = Modifier.padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              if (deck.value == null || selectedFlashcard.value == null || userFlashcardList.value.isEmpty()) {
                LoadingIndicator("Loading deck...")
              }
              else {
                  val score = remember { mutableIntStateOf(0) }
                  val answers: Map<String, MutableState<Int?>> =
                      remember(deck.value?.flashcardIds) {
                          deck.value!!.flashcardIds.associateWith { mutableStateOf(null) }
                      }

                  if(isFinished.value){
                      FinishedScreen(
                          score,
                          flashcardList,
                          isFinished,
                          currentFlashcardIndex,
                          userViewModel,
                          deck,
                          userFlashcardList,
                          flashcardViewModel,
                          answers
                      )
                  }else {
                      if (selectedFlashcard.value != null) {
                          val nonNullableFlashcard: State<Flashcard> =
                              remember(selectedFlashcard) { derivedStateOf { selectedFlashcard.value!! } }
                          FlashcardPlayItem(
                              nonNullableFlashcard,
                              fileViewModel,
                              onCorrect = { score.value += 1 },
                              choice = answers[selectedFlashcard.value!!.id]!!,
                              isReview = playMode.value == Deck.PlayMode.REVIEW
                          )


                          if (playMode.value == Deck.PlayMode.REVIEW) {
                              ReviewMode(
                                  flashcardViewModel,
                                  flashcardList,
                                  userViewModel,
                                  userViewModelFlashcards,
                                  nonNullableFlashcard,
                                  userFlashcardList,
                                  answers
                              )
                          } else {
                              TestMode(
                                  flashcardViewModel,
                                  flashcardList,
                                  nonNullableFlashcard,
                                  score,
                                  isFinished,
                                  answers,
                                  currentFlashcardIndex
                              )
                          }
                      }
                  }
              }
            }
      }
}


/**
 * This composable is used to handle the logic for the review mode of the deck play screen.
 *
 * @param flashcardViewModel The view model for flashcards.
 * @param flashcardList The list of flashcards in the deck.
 * @param userViewModel The view model for the user.
 * @param userViewModelFlashcards The user flashcards.
 * @param selectedFlashcard The selected flashcard.
 * @param userFlashcardList The list of user flashcards.
 * @param answers The answers for the flashcards.
 */
@Composable
fun ReviewMode(
    flashcardViewModel: FlashcardViewModel,
    flashcardList: State<List<Flashcard>>,
    userViewModel: UserViewModel,
    userViewModelFlashcards: State<Map<String, UserFlashcard>>,
    selectedFlashcard: State<Flashcard>,
    userFlashcardList: MutableState<List<UserFlashcard>>,
    answers: Map<String, MutableState<Int?>>
) {
  val playDeckHistory = remember { mutableStateOf(PlayDeckHistory(
        currentFlashcardId =selectedFlashcard.value.id
  )) }
  Column {
     PreviousNextButton(
        onNext = {
           if(playDeckHistory.value.canGoForward()){
               playDeckHistory.value=playDeckHistory.value.goForward()
                flashcardViewModel.selectFlashcard(
                     flashcardList.value.first { it.id == playDeckHistory.value.currentFlashcardId }
                )
           }else{
                val withoutCurrent = userFlashcardList.value.filter { it.id != selectedFlashcard.value.id }
                val selectedUserFlashcard = UserFlashcard.selectRandomFlashcardLinear(withoutCurrent)
                answers[selectedUserFlashcard.id]!!.value = null
               playDeckHistory.value = playDeckHistory.value.goForwardWithNewFlashcard(selectedUserFlashcard.id)
               flashcardViewModel.selectFlashcard(
                   flashcardList.value.first { it.id == playDeckHistory.value.currentFlashcardId })
           }
        },
        onPrevious = {
            playDeckHistory.value=playDeckHistory.value.goBack()
              flashcardViewModel.selectFlashcard(
                  flashcardList.value.first { it.id == playDeckHistory.value.currentFlashcardId }
              )
        },
        enabledPrevious = playDeckHistory.value.canGoBack(),
        enabledNext = userFlashcardList.value.size > 1
     )
    SelectWrongRight(
        answers,
        selectedFlashcard,
        onCorrect = {
          userViewModel.updateUserFlashcard(
              userViewModelFlashcards.value[selectedFlashcard.value.id]!!.increaseLevel(),
              onSuccess = {
                  answers[selectedFlashcard.value.id]!!.value = 0
                userFlashcardList.value =
                    userFlashcardList.value.map { userFlashcard ->
                      if (userFlashcard.id == selectedFlashcard.value.id) {
                        userViewModelFlashcards.value[selectedFlashcard.value.id]!!
                      } else {
                        userFlashcard
                      }
                    }
              })
        },
        onIncorrect = {
          userViewModel.updateUserFlashcard(
              userViewModelFlashcards.value[selectedFlashcard.value.id]!!.decreaseLevel(),
              onSuccess = {
                  answers[selectedFlashcard.value.id]!!.value = 1
                userFlashcardList.value =
                    userFlashcardList.value.map { userFlashcard ->
                      if (userFlashcard.id == selectedFlashcard.value.id) {
                        userViewModelFlashcards.value[selectedFlashcard.value.id]!!
                      } else {
                        userFlashcard
                      }
                    }
              })
        })
  }
}

/**
 * This composable is used to handle the logic for the test mode of the deck play screen.
 *
 * @param flashcardViewModel The view model for flashcards.
 * @param flashcardList The list of flashcards in the deck.
 * @param selectedFlashcard The selected flashcard.
 * @param score The score of the user.
 * @param isFinished The state of the test.
 * @param answers The answers for the flashcards.
 * @param currentFlashcardIndex The index of the current flashcard.
 */
@Composable
private fun TestMode(
    flashcardViewModel: FlashcardViewModel,
    flashcardList: State<List<Flashcard>>,
    selectedFlashcard: State<Flashcard>,
    score: MutableIntState,
    isFinished: MutableState<Boolean>,
    answers: Map<String, MutableState<Int?>>,
    currentFlashcardIndex: MutableIntState
) {
  Column {
      PreviousNextButton(
            onNext = {
                if (currentFlashcardIndex.intValue == flashcardList.value.size - 1) {
                    isFinished.value = true
                } else {
                    currentFlashcardIndex.intValue++
                    if (currentFlashcardIndex.intValue >= flashcardList.value.size) {
                        currentFlashcardIndex.intValue = flashcardList.value.size - 1
                    } else {
                        flashcardViewModel.selectFlashcard(
                            flashcardList.value[currentFlashcardIndex.intValue]
                        )
                    }
                }
            },
            onPrevious = {
                currentFlashcardIndex.intValue--
                if (currentFlashcardIndex.intValue < 0) {
                    currentFlashcardIndex.intValue = 0
                } else {
                    flashcardViewModel.selectFlashcard(
                        flashcardList.value[currentFlashcardIndex.intValue]
                    )
                }
            },
            enabledPrevious = currentFlashcardIndex.intValue > 0,
            nextBecomeSummit = currentFlashcardIndex.intValue == flashcardList.value.size - 1
          )

    if (!selectedFlashcard.value.isMCQ()) {
      SelectWrongRight(
          answers,
          selectedFlashcard,
          onCorrect = {
            score.value += 1
            answers[selectedFlashcard.value.id]!!.value = 0
          },
          onIncorrect = { answers[selectedFlashcard.value.id]!!.value = 1 })
    }
  }
}

/**
 * This composable is used to display the previous and next buttons.
 *
 * @param onNext The function to be called when the next button is clicked.
 * @param onPrevious The function to be called when the previous button is clicked.
 * @param enabledPrevious The state of the previous button.
 * @param enabledNext The state of the next button.
 * @param nextBecomeSummit The state of the next button.
 */
@Composable
private fun PreviousNextButton(
    onNext: () -> Unit,
    onPrevious: () -> Unit,
    enabledPrevious: Boolean,
    enabledNext: Boolean = true,
    nextBecomeSummit: Boolean = false,
    ) {
    Row(modifier = Modifier.fillMaxWidth()) {
        Button(
            onClick = onPrevious,
            enabled = enabledPrevious,
            modifier = Modifier.padding(16.dp)
        ) {
            Text("Previous")
        }
        Button(
            onClick = {
                onNext()
            },
            enabled = enabledNext,
            modifier = Modifier.padding(16.dp)
        ) {
            if ( nextBecomeSummit) {
                Text("Submit")
            } else {
                Text("next")
            }
        }
    }
}

/**
 * This composable is used to display the wrong and right buttons.
 *
 * @param answers The answers for the flashcards.
 * @param selectedFlashcard The selected flashcard.
 * @param onCorrect The function to be called when the correct button is clicked.
 * @param onIncorrect The function to be called when the incorrect button is clicked.
 */
@Composable
private fun SelectWrongRight(
    answers: Map<String, MutableState<Int?>>,
    selectedFlashcard: State<Flashcard?>,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = { onIncorrect() },
        modifier = Modifier.padding(16.dp),
        enabled = answers[selectedFlashcard.value!!.id]!!.value == null) {
          Row {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Close Icon",
                tint = MaterialTheme.colorScheme.onSurface)
            Text("I got it wrong")
          }
        }
    Button(
        onClick = { onCorrect() },
        modifier = Modifier.padding(16.dp),
        enabled = answers[selectedFlashcard.value!!.id]!!.value == null) {
          Row {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "Close Icon",
                tint = MaterialTheme.colorScheme.onSurface)
            Text("I got it right")
          }
        }
  }
}

/**
 * This composable is used to display the finished screen.
 *
 * @param score The score of the user.
 * @param flashcardList The list of flashcards in the deck.
 * @param isFinished The state of the test.
 * @param currentFlashcardIndex The index of the current flashcard.
 * @param userViewModel The view model for the user.
 * @param deck The selected deck.
 * @param userFlashcardList The list of user flashcards.
 * @param flashcardViewModel The view model for flashcards.
 * @param answers The answers for the flashcards.
 */
@Composable
private fun FinishedScreen(
    score: MutableIntState,
    flashcardList: State<List<Flashcard>>,
    isFinished: MutableState<Boolean>,
    currentFlashcardIndex: MutableIntState,
    userViewModel: UserViewModel,
    deck: State<Deck?>,
    userFlashcardList: MutableState<List<UserFlashcard>>,
    flashcardViewModel: FlashcardViewModel,
    answers: Map<String, MutableState<Int?>>
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .height(200.dp)
            .fillMaxWidth()
    ) {
        val scorePercent = score.intValue * 100 / flashcardList.value.size
        Column {
            Text("You have finished the deck")
            Text("Your score is $scorePercent%")
            Button(
                onClick = {
                    isFinished.value = false
                    score.intValue = 0
                    currentFlashcardIndex.intValue = 0
                    userViewModel.getUserFlashcardFromDeck(
                        deck.value!!,
                        onSuccess = {
                            userFlashcardList.value =
                                flashcardList.value.mapNotNull { fc ->
                                    userViewModel.deckUserFlashcards.value[fc.id]
                                }
                            flashcardViewModel.selectFlashcard(
                                flashcardList.value.first()
                            )
                        })
                    for (flashcard in flashcardList.value) {
                        answers[flashcard.id]!!.value = null
                    }
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("test again")
            }
        }
    }
}
