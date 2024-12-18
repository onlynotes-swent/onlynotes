package com.github.onlynotesswent.ui.deck

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableIntState
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import kotlinx.coroutines.launch


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
  val flashcardMap= flashcardList.value.associateBy { it.id }




    //this code will load the flashcards from the deck,
    //select the current flashcard
    //get all the  user flashcards from the user
    //if the user flashcard is not in the user flashcard list, it will add it
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
                   if(userFlashcardList.value.contains(userViewModel.deckUserFlashcards.value[id])){
                       continue
                   }
                   if (userViewModel.deckUserFlashcards.value[id] == null) {
                       val userFlashcard= UserFlashcard(id)
                       userViewModel.addUserFlashcard(userFlashcard)
                       userFlashcardList.value += userFlashcard
                   }else{
                          userFlashcardList.value += userViewModel.deckUserFlashcards.value[id]!!
                   }
               }
               Log.e("DeckPlayScreen", "userFlashcardList: ${userFlashcardList.value.size }}")
           })
   }




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
                          if (playMode.value == Deck.PlayMode.REVIEW) {
                              ReviewMode(
                                  fileViewModel,
                                  userViewModel,
                                  userViewModelFlashcards,
                                  userFlashcardList,
                                  answers,
                                  flashcardMap
                              )
                          } else {
                              TestMode(
                                  fileViewModel,
                                  flashcardList,
                                  score,
                                  isFinished,
                                  answers,
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
 * @param userViewModel The view model for the user.
 * @param userViewModelFlashcards The user flashcards.
 * @param userFlashcardList The list of user flashcards.
 * @param answers The answers for the flashcards.
 * @param flashcardMap The map of flashcards.
 */
@Composable
fun ReviewMode(
    fileViewModel: FileViewModel,
    userViewModel: UserViewModel,
    userViewModelFlashcards: State<Map<String, UserFlashcard>>,
    userFlashcardList: MutableState<List<UserFlashcard>>,
    answers: Map<String, MutableState<Int?>>,
    flashcardMap: Map<String, Flashcard>
) {
  val playDeckHistory = remember { mutableStateOf(PlayDeckHistory(
        currentFlashcardId = UserFlashcard.selectRandomFlashcardLinear(userFlashcardList.value).id
  )) }
  Column {
      val listOfPagerFlashcards= remember{
          mutableStateOf( playDeckHistory.value.listOfAllFlashcard)}
      val pagerState = rememberPagerState(initialPage = 1) { listOfPagerFlashcards.value.size }
      val scrollScope = rememberCoroutineScope()

      HorizontalPager(pagerState) { pageIndex ->

          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
              ,
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
              if(pageIndex==0){
                   pagerState.requestScrollToPage(playDeckHistory.value.indexOfCurrentFlashcard)
              }else{
                  val flashcardState = remember { derivedStateOf { flashcardMap[listOfPagerFlashcards.value[pageIndex]] } }
                  FlashcardPlayItem(flashcardState, fileViewModel,
                        onCorrect = {
                            userViewModel.updateUserFlashcard(
                                userViewModelFlashcards.value[playDeckHistory.value.currentFlashcardId]!!.increaseLevel(),
                                onSuccess = {
                                    userFlashcardList.value =
                                        userFlashcardList.value.map { userFlashcard ->
                                            if (userFlashcard.id == playDeckHistory.value.currentFlashcardId) {
                                                userViewModelFlashcards.value[playDeckHistory.value.currentFlashcardId]!!
                                            } else {
                                                userFlashcard
                                            }
                                        }
                                })
                        },
                      onIncorrect = {
                          userViewModel.updateUserFlashcard(
                              userViewModelFlashcards.value[playDeckHistory.value.currentFlashcardId]!!.decreaseLevel(),
                              onSuccess = {
                                  userFlashcardList.value =
                                      userFlashcardList.value.map { userFlashcard ->
                                          if (userFlashcard.id == playDeckHistory.value.currentFlashcardId) {
                                              userViewModelFlashcards.value[playDeckHistory.value.currentFlashcardId]!!
                                          } else {
                                              userFlashcard
                                          }
                                      }
                              })},
                        choice = answers[playDeckHistory.value.currentFlashcardId]!!
                  )
              }
          }
      }

      //this a listener for the pager state that will be triggered when the current page changes
      //it will handle all the cases when the user goes back or forward

      LaunchedEffect(pagerState.currentPage) {
          scrollScope.launch {
              val diff = pagerState.currentPage - playDeckHistory.value.indexOfCurrentFlashcard
              if (diff > 0) {
                  if (playDeckHistory.value.canGoForward()) {
                      playDeckHistory.value = playDeckHistory.value.goForward()
                  } else {
                      val withoutCurrent =
                          userFlashcardList.value.filter { it.id != playDeckHistory.value.currentFlashcardId }
                      val selectedUserFlashcard =
                          UserFlashcard.selectRandomFlashcardLinear(withoutCurrent)
                      answers[selectedUserFlashcard.id]!!.value = null
                      playDeckHistory.value =
                          playDeckHistory.value.goForwardWithNewFlashcard(selectedUserFlashcard.id)
                  }
                  listOfPagerFlashcards.value = playDeckHistory.value.listOfAllFlashcard
                  if (pagerState.currentPage == listOfPagerFlashcards.value.size - 1) {
                      pagerState.scrollToPage(1)
                  }
              } else if (diff < 0) {
                  if (playDeckHistory.value.canGoBack()) {
                      playDeckHistory.value = playDeckHistory.value.goBack()
                      if (pagerState.currentPage == 0) {
                          pagerState.requestScrollToPage(listOfPagerFlashcards.value.size - 2)
                      }
                      listOfPagerFlashcards.value = playDeckHistory.value.listOfAllFlashcard
                  } else {
                      pagerState.scrollToPage(playDeckHistory.value.indexOfCurrentFlashcard)
                  }
              }
              if (pagerState.currentPage == 0) {
                  pagerState.requestScrollToPage(playDeckHistory.value.indexOfCurrentFlashcard)
              }
          }
      }
if(!flashcardMap[playDeckHistory.value.currentFlashcardId]!!.isMCQ()){
    SelectWrongRight(
        answers,
        playDeckHistory.value.currentFlashcardId,
        onCorrect = {
          userViewModel.updateUserFlashcard(
              userViewModelFlashcards.value[  playDeckHistory.value.currentFlashcardId]!!.increaseLevel(),
              onSuccess = {
                  answers[  playDeckHistory.value.currentFlashcardId]!!.value = 0
                userFlashcardList.value =
                    userFlashcardList.value.map { userFlashcard ->
                      if (userFlashcard.id ==   playDeckHistory.value.currentFlashcardId) {
                        userViewModelFlashcards.value[ playDeckHistory.value.currentFlashcardId]!!
                      } else {
                        userFlashcard
                      }
                    }
              })
        },
        onIncorrect = {
          userViewModel.updateUserFlashcard(
              userViewModelFlashcards.value[  playDeckHistory.value.currentFlashcardId]!!.decreaseLevel(),
              onSuccess = {
                  answers[  playDeckHistory.value.currentFlashcardId]!!.value = 1
                userFlashcardList.value =
                    userFlashcardList.value.map { userFlashcard ->
                      if (userFlashcard.id ==  playDeckHistory.value.currentFlashcardId) {
                        userViewModelFlashcards.value[  playDeckHistory.value.currentFlashcardId]!!
                      } else {
                        userFlashcard
                      }
                    }
              })
        })
  }
  }
}

/**
 * This composable is used to handle the logic for the test mode of the deck play screen.
 *
 * @param flashcardList The list of flashcards in the deck.
 * @param score The score of the user.
 * @param isFinished The state of the test.
 * @param answers The answers for the flashcards.
 */
@Composable
private fun TestMode(
    fileViewModel: FileViewModel,
    flashcardList: State<List<Flashcard>>,
    score: MutableIntState,
    isFinished: MutableState<Boolean>,
    answers: Map<String, MutableState<Int?>>,
    ) {
  Column {
      val pagerState = rememberPagerState { flashcardList.value.size }
      HorizontalPager(pagerState) { pageIndex ->
          Column(
              modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(16.dp)) {
              val flashcardState = remember { derivedStateOf { flashcardList.value[pageIndex] } }
              FlashcardPlayItem(flashcardState, fileViewModel,
                  onCorrect = {
                      score.value += 1
                  },
                  choice = answers[flashcardList.value[pageIndex].id]!!
                  )
          }
      }

    if (!flashcardList.value[pagerState.currentPage].isMCQ()) {
      SelectWrongRight(
          answers,
          flashcardList.value[pagerState.currentPage].id,
          onCorrect = {
            score.value += 1
            answers[flashcardList.value[pagerState.currentPage].id]!!.value = 0
          },
          onIncorrect = { answers[flashcardList.value[pagerState.currentPage].id]!!.value = 1 })
    }
      Button(
          modifier = Modifier.padding(16.dp),
            onClick = {
                    isFinished.value = true
            },
            enabled = pagerState.currentPage == flashcardList.value.size - 1
        ) {
            Text("Submit")
        }
  }
}

/**
 * This composable is used to display the wrong and right buttons.
 *
 * @param answers The answers for the flashcards.
 * @param selectedFlashcardId The selected flashcard.
 * @param onCorrect The function to be called when the correct button is clicked.
 * @param onIncorrect The function to be called when the incorrect button is clicked.
 */
@Composable
private fun SelectWrongRight(
    answers: Map<String, MutableState<Int?>>,
    selectedFlashcardId: String,
    onCorrect: () -> Unit,
    onIncorrect: () -> Unit,
) {
  Row(modifier = Modifier.fillMaxWidth()) {
    Button(
        onClick = { onIncorrect() },
        modifier = Modifier.padding(16.dp),
        enabled = answers[selectedFlashcardId]!!.value == null) {
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
        enabled = answers[selectedFlashcardId]!!.value == null) {
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
