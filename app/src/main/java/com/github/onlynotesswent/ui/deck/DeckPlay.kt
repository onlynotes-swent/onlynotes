package com.github.onlynotesswent.ui.deck

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absoluteOffset
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerDefaults
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedButton
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.lerp
import com.github.onlynotesswent.model.deck.Deck
import com.github.onlynotesswent.model.deck.DeckViewModel
import com.github.onlynotesswent.model.deck.PlayDeckHistory
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.UserFlashcard
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.FlashcardPlayItem
import com.github.onlynotesswent.ui.common.LoadingIndicator
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import kotlin.math.absoluteValue
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
  val playMode = deckViewModel.selectedPlayMode.collectAsState()
  val isFinished = remember { mutableStateOf(false) }
  val currentFlashcardIndex = remember { mutableIntStateOf(0) }
  val userFlashcardList: MutableState<List<UserFlashcard>> = remember { mutableStateOf(listOf()) }
  val flashcardList = flashcardViewModel.deckFlashcards.collectAsState()
  val userViewModelFlashcards = userViewModel.deckUserFlashcards.collectAsState()
  val flashcardMap = flashcardList.value.associateBy { it.id }

  // this code will load the flashcards from the deck,
  // select the current flashcard
  // get all the  user flashcards from the user
  // if the user flashcard is not in the user flashcard list, it will add it
  deck.value?.let {
    flashcardViewModel.fetchFlashcardsFromDeck(
        it, onSuccess = { flashcards -> flashcardViewModel.selectFlashcard(flashcards.first()) })
    userViewModel.getUserFlashcardFromDeck(
        it,
        onSuccess = {
          for (id in it.flashcardIds) {
            if (userFlashcardList.value.contains(userViewModel.deckUserFlashcards.value[id])) {
              continue
            }
            if (userViewModel.deckUserFlashcards.value[id] == null) {
              val userFlashcard = UserFlashcard(id)
              userViewModel.addUserFlashcard(userFlashcard)
              userFlashcardList.value += userFlashcard
            } else {
              userFlashcardList.value += userViewModel.deckUserFlashcards.value[id]!!
            }
          }
        })
  }

  Scaffold(
      modifier = Modifier.testTag("DeckPlayScreen"),
      topBar = {
        ScreenTopBar(
            playMode.value?.toReadableString() ?: "No mode selected",
            "DeckPlayScreenTopBar",
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
            modifier = Modifier.padding(innerPadding).fillMaxSize().testTag("DeckPlayScreenColumn"),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally) {
              if (deck.value == null || userFlashcardList.value.isEmpty()) {
                LoadingIndicator("Loading deck...")
              } else {
                val score = remember { mutableIntStateOf(0) }
                val answers: Map<String, MutableState<Int?>> =
                    remember(deck.value?.flashcardIds) {
                      deck.value!!.flashcardIds.associateWith { mutableStateOf(null) }
                    }
                if (isFinished.value) {
                  FinishedScreen(
                      score,
                      flashcardList,
                      isFinished,
                      currentFlashcardIndex,
                      userViewModel,
                      deck,
                      userFlashcardList,
                      flashcardViewModel,
                      answers)
                } else {
                  if (playMode.value == Deck.PlayMode.REVIEW) {
                    ReviewMode(
                        fileViewModel,
                        userViewModel,
                        userViewModelFlashcards,
                        userFlashcardList,
                        answers,
                        flashcardMap)
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
  val playDeckHistory = remember {
    mutableStateOf(
        PlayDeckHistory(
            currentFlashcardId =
                UserFlashcard.selectRandomFlashcardLinear(userFlashcardList.value).id))
  }
  Box(modifier = Modifier.testTag("ReviewModeColumn").fillMaxSize()) {
    val listOfPagerFlashcards = remember {
      derivedStateOf { playDeckHistory.value.listOfAllFlashcard }
    }
    val pagerState = rememberPagerState { listOfPagerFlashcards.value.size }
    val scrollScope = rememberCoroutineScope()

    HorizontalPager(
        pagerState,
        modifier =
            Modifier.testTag("Pager")
                .heightIn(min = 250.dp, max = 500.dp)
                .align(Alignment.Center)
                .padding(bottom = 150.dp)) { pageIndex ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(16.dp)
                      .testTag("flashcardColumn")
                      .graphicsLayer {
                        val pageOffset =
                            ((pagerState.currentPage - pageIndex) +
                                    pagerState.currentPageOffsetFraction)
                                .absoluteValue
                        alpha =
                            lerp(
                                start = 0.5f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f))
                      }
                      .animateContentSize(tween(100)),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {
                val flashcardState = remember {
                  derivedStateOf {
                    flashcardMap[
                        listOfPagerFlashcards.value[
                                if (pageIndex == PlayDeckHistory.MAX_LIST_LENGTH - 1) 1
                                else if (pageIndex == 0 && listOfPagerFlashcards.value[0] == null)
                                    PlayDeckHistory.MAX_LIST_LENGTH - 2
                                else pageIndex]]
                  }
                }
                FlashcardPlayItem(
                    flashcardState,
                    fileViewModel,
                    choice = answers[playDeckHistory.value.currentFlashcardId]!!,
                    isReview = true)
              }
        }

    // this a listener for the pager state that will be triggered when the current page changes
    // it will handle all the cases when the user goes back or forward

    LaunchedEffect(pagerState.settledPage) {
      scrollScope.launch {
        val diff = pagerState.settledPage - playDeckHistory.value.indexOfCurrentFlashcard
        if (diff > 0) {
          if (playDeckHistory.value.canGoTwiceForward()) {
            playDeckHistory.value = playDeckHistory.value.goForward()
          } else {
            while (playDeckHistory.value.canGoForward() &&
                pagerState.settledPage > playDeckHistory.value.indexOfCurrentFlashcard) {
              val nextCardId = listOfPagerFlashcards.value[playDeckHistory.value.getIndexForward()]
              val withoutNext = userFlashcardList.value.filter { it.id != nextCardId }
              val twiceNextFlashcardId = UserFlashcard.selectRandomFlashcardLinear(withoutNext).id
              answers[nextCardId]!!.value = null
              playDeckHistory.value =
                  playDeckHistory.value.goForwardWithTwiceNextFlashcard(twiceNextFlashcardId)
              if (pagerState.settledPage == PlayDeckHistory.MAX_LIST_LENGTH - 1) {
                break
              }
            }
          }
          if (pagerState.settledPage == PlayDeckHistory.MAX_LIST_LENGTH - 1) {
            pagerState.scrollToPage(1)
          }
        } else if (diff < 0) {
          if (playDeckHistory.value.canGoBack()) {
            playDeckHistory.value = playDeckHistory.value.goBack()
            if (pagerState.settledPage == 0 && listOfPagerFlashcards.value[0] == null) {
              pagerState.scrollToPage(listOfPagerFlashcards.value.size - 2)
            }
          } else {
            // When going back to before the first flashcard and size=max
            // this prevents the user from going back further
            pagerState.scrollToPage(playDeckHistory.value.indexOfCurrentFlashcard)
          }
        } else if (!playDeckHistory.value.canGoForward()) {
          // this is the case when the user opens the deck and the user can't go forward
          val withoutCurrent =
              userFlashcardList.value.filter { it.id != playDeckHistory.value.currentFlashcardId }
          val nextFlashcardId = UserFlashcard.selectRandomFlashcardLinear(withoutCurrent).id
          playDeckHistory.value = playDeckHistory.value.stayWithNewFlashcard(nextFlashcardId)
        }
      }
    }
    SelectWrongRight(
        answers,
        playDeckHistory.value.currentFlashcardId,
        onCorrect = {
          userViewModel.updateUserFlashcard(
              userViewModelFlashcards.value[playDeckHistory.value.currentFlashcardId]!!
                  .increaseLevel(),
              onSuccess = {
                answers[playDeckHistory.value.currentFlashcardId]!!.value = 0
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
              userViewModelFlashcards.value[playDeckHistory.value.currentFlashcardId]!!
                  .decreaseLevel(),
              onSuccess = {
                answers[playDeckHistory.value.currentFlashcardId]!!.value = 1
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
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 100.dp))
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
  Box(contentAlignment = Alignment.Center) {
    val pagerState = rememberPagerState { flashcardList.value.size }
    val fling = PagerDefaults.flingBehavior(pagerState, snapPositionalThreshold = 0.3f)
    HorizontalPager(
        state = pagerState,
        modifier =
            Modifier.testTag("Pager")
                .fillMaxWidth()
                .heightIn(250.dp, 700.dp)
                .align(Alignment.Center)
                .padding(bottom = 80.dp),
        flingBehavior = fling) { pageIndex ->
          Column(
              modifier =
                  Modifier.fillMaxWidth()
                      .padding(10.dp)
                      .testTag("flashcardColumn")
                      .animateContentSize(tween(100))
                      .graphicsLayer {
                        val pageOffset =
                            ((pagerState.currentPage - pageIndex) +
                                    pagerState.currentPageOffsetFraction)
                                .absoluteValue
                        alpha =
                            lerp(
                                start = 0.4f,
                                stop = 1f,
                                fraction = 1f - pageOffset.coerceIn(0f, 1f))
                      },
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center) {
                val flashcardState = remember { derivedStateOf { flashcardList.value[pageIndex] } }
                FlashcardPlayItem(
                    flashcardState,
                    fileViewModel,
                    onCorrect = { score.value += 1 },
                    choice = answers[flashcardList.value[pageIndex].id]!!)
              }
        }
    Column(
        modifier =
            Modifier.testTag("TestModeColumn")
                .fillMaxSize()
                .padding(5.dp)
                .align(Alignment.BottomCenter),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally) {
          AnimatedVisibility(pagerState.currentPage == flashcardList.value.size - 1) {
            ElevatedButton(
                modifier = Modifier.padding(20.dp).testTag("submitButton"),
                onClick = { isFinished.value = true },
                enabled = pagerState.currentPage == flashcardList.value.size - 1) {
                  Text("Finish Test", style = MaterialTheme.typography.headlineSmall)
                }
          }
          AnimatedVisibility(!flashcardList.value[pagerState.currentPage].isMCQ()) {
            SelectWrongRight(
                answers,
                flashcardList.value[pagerState.currentPage].id,
                onCorrect = {
                  score.value += 1
                  answers[flashcardList.value[pagerState.currentPage].id]!!.value = 0
                },
                onIncorrect = {
                  answers[flashcardList.value[pagerState.currentPage].id]!!.value = 1
                })
          }
          Spacer(modifier = Modifier.height(25.dp))
          Row(
              Modifier.wrapContentHeight().fillMaxWidth().padding(15.dp),
              horizontalArrangement = Arrangement.Center) {
                repeat(pagerState.pageCount) { iteration ->
                  val color =
                      if (pagerState.currentPage == iteration) Color.DarkGray else Color.LightGray
                  Box(
                      modifier =
                          Modifier.padding(2.dp).clip(CircleShape).background(color).size(8.dp))
                }
              }
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
    modifier: Modifier = Modifier,
) {
  Row(modifier = modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
    Button(
        onClick = { onIncorrect() },
        modifier = Modifier.padding(3.dp).testTag("incorrectButton"),
        enabled = answers[selectedFlashcardId]!!.value == null) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close Icon",
                    tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("I got it wrong", style = MaterialTheme.typography.bodyLarge, maxLines = 1)
              }
        }
    Button(
        onClick = { onCorrect() },
        modifier = Modifier.padding(3.dp).testTag("correctButton"),
        enabled = answers[selectedFlashcardId]!!.value == null) {
          Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.SpaceBetween) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "Close Icon",
                    tint = MaterialTheme.colorScheme.onSurface)
                Spacer(modifier = Modifier.width(8.dp))
                Text("I got it right", style = MaterialTheme.typography.bodyLarge, maxLines = 1)
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
  var animatedScore by remember { mutableFloatStateOf(0f) }
  val targetScore = if (!isFinished.value) 0 else score.intValue * 100 / flashcardList.value.size
  Column(
      modifier = Modifier.testTag("FinishedScreenColumn").fillMaxSize(),
      verticalArrangement = Arrangement.Center,
      horizontalAlignment = Alignment.CenterHorizontally) {
        // wait for the user to finish the test before starting animation

        LaunchedEffect(isFinished.value) {
          if (isFinished.value) {
            animate(
                initialValue = 0f,
                targetValue = targetScore.toFloat(),
                animationSpec = tween(durationMillis = 1300, delayMillis = 300)) { value, _ ->
                  animatedScore = value
                }
          }
        }
        Box(contentAlignment = Alignment.Center) {
          Text(
              "${animatedScore.toInt()}%",
              style = MaterialTheme.typography.headlineLarge,
              modifier = Modifier.absoluteOffset(x = 6.dp, y = 1.dp))
          CircularProgressIndicator(
              progress = { animatedScore / 100f }, modifier = Modifier.size(180.dp).padding(30.dp))
        }

        Text(
            "You have finished the deck with a score of ${animatedScore.toInt()}% !",
            style = MaterialTheme.typography.bodyLarge)
        Spacer(modifier = Modifier.height(20.dp))
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
                    flashcardViewModel.selectFlashcard(flashcardList.value.first())
                  })
              for (flashcard in flashcardList.value) {
                answers[flashcard.id]!!.value = null
              }
            },
            modifier = Modifier.padding(16.dp).testTag("retryButton")) {
              Row(
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Close Icon",
                        tint = MaterialTheme.colorScheme.onSurface)
                    Text("Retry", style = MaterialTheme.typography.headlineSmall)
                  }
            }
      }
}
