package com.github.onlynotesswent.ui.deck

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
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
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.UserFlashcard
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.user.UserViewModel
import com.github.onlynotesswent.ui.common.ScreenTopBar
import com.github.onlynotesswent.ui.navigation.NavigationActions
import androidx.compose.ui.Modifier
import com.github.onlynotesswent.ui.common.FlashcardPlayItem
import com.github.onlynotesswent.ui.common.LoadingIndicator

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
    val userFlashcards = userViewModel.deckUserFlashcards.collectAsState()
    val playMode=deckViewModel.selectedPlayMode.collectAsState()
    val isFinished= remember{ mutableStateOf(false) }

    val userFlashcardList: MutableState<List<UserFlashcard>> = remember{ mutableStateOf(listOf()) }

    deck.value?.let {
        flashcardViewModel.fetchFlashcardsFromDeck(it,
            onSuccess = {
                flashcards ->
                flashcardViewModel.selectFlashcard(flashcards.firstOrNull()!!)
                for (flashcard in flashcards) {
                    if(userViewModel.deckUserFlashcards.value[flashcard.id] == null){
                        userViewModel.addUserFlashcard(UserFlashcard(flashcard.id))
                    }
                }
                userViewModel.getUserFlashcardFromDeck(it, onSuccess = {
                    if(playMode.value== Deck.PlayMode.REVIEW){
                        userFlashcardList.value= flashcards.mapNotNull { fc-> userViewModel.deckUserFlashcards.value[fc.id] }
                        val selectedUserFlashcard = UserFlashcard.selectRandomFlashcardLinear(userFlashcardList.value)
                        flashcardViewModel.selectFlashcard(flashcards.first {fc-> fc.id==selectedUserFlashcard.id })
                    }
                })

            }
        )
    }
    flashcardViewModel.fetchFlashcardsFromDeck(deck.value!!)

    val flashcardList = flashcardViewModel.deckFlashcards.collectAsState()
    val userViewModelFlashcards = userViewModel.deckUserFlashcards.collectAsState()

    //deckViewModel.selectedDeck.value?.let { userViewModel.getUserFlashcardFromDeck(it) }
   // val flashcardUserInfo= userViewModel.deckUserFlashcards
  Scaffold(
        topBar = {
            ScreenTopBar(playMode.value?.let{"Deck $it"}?:"Deck: No mode selected", "DeckPlayScreen",
                { navigationActions.goBack() },
                {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Exit Play Deck",
                        tint = MaterialTheme.colorScheme.onSurface)
                },"DeckPlayIcon")
        }
  ) { innerPadding ->
    Column(
        modifier =Modifier.padding(innerPadding),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if(deck.value==null){
           LoadingIndicator("Loading deck...")
        }
        else{
            val answers: Map<String, MutableState<Int?>> = remember(deck.value?.flashcardIds) {
                deck.value!!.flashcardIds.associateWith { mutableStateOf<Int?>(null) }
            }
        val score = remember { mutableIntStateOf(0) }
        if(!isFinished.value){
            selectedFlashcard.value?.let {
            FlashcardPlayItem(it, fileViewModel,
                onCorrect = {score.value+=1})}
        }

        if(playMode.value== Deck.PlayMode.REVIEW){
            ReviewMode(
                flashcardViewModel,
                flashcardList,
                userViewModel,
                userViewModelFlashcards,
                selectedFlashcard,
                userFlashcardList,
                answers
            )
        }else{
            if(isFinished.value){
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .height(200.dp)
                            .fillMaxWidth()
                    ) {
                        val scorePercent= score.intValue*100/flashcardList.value.size
                        Column {
                            Text("You have finished the deck")
                            Text("Your score is $scorePercent%")
                            Button(
                                onClick = {
                                    isFinished.value=false
                                    score.intValue=0
                                    userViewModel.getUserFlashcardFromDeck(deck.value!!, onSuccess = {
                                        userFlashcardList.value= flashcardList.value.mapNotNull { fc-> userViewModel.deckUserFlashcards.value[fc.id] }
                                        val selectedUserFlashcard = UserFlashcard.selectRandomFlashcardLinear(userFlashcardList.value)
                                        flashcardViewModel.selectFlashcard(flashcardList.value.first {fc-> fc.id==selectedUserFlashcard.id })
                                    })
                                },
                                modifier = Modifier.padding(16.dp)
                            ) {
                                Text("test again")
                            }
                        }
                    }
            }else{
            TestMode(
                flashcardViewModel,
                flashcardList,
                userViewModel,
                userViewModelFlashcards,
                selectedFlashcard,
                score,
                isFinished,
                answers
            )
            }
        }
        }


    }
  }
}

@Composable
fun ReviewMode(
    flashcardViewModel: FlashcardViewModel,
    flashcardList: State<List<Flashcard>>,
    userViewModel: UserViewModel,
    userViewModelFlashcards: State<Map<String, UserFlashcard>>,
    selectedFlashcard: State<Flashcard?>,
    userFlashcardList: MutableState<List<UserFlashcard>>,
    answers: Map<String, MutableState<Int?>>

) {
    val maxListSize=100
    val listOfPreviousFlashcardsId= remember{mutableStateOf(listOf<String>())}
    val listOfNextFlashcardsId=remember{mutableStateOf(listOf<String>())}
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    if(listOfNextFlashcardsId.value.size>maxListSize){
                        listOfNextFlashcardsId.value= listOfNextFlashcardsId.value.drop(1)
                    }
                    listOfNextFlashcardsId.value +=  selectedFlashcard.value!!.id
                    val previousFlashcardId= listOfPreviousFlashcardsId.value.last()
                    listOfPreviousFlashcardsId.value= listOfPreviousFlashcardsId.value.dropLast(1)
                    flashcardViewModel.selectFlashcard(flashcardList.value.first { it.id==previousFlashcardId })
                },
                enabled = listOfPreviousFlashcardsId.value.isNotEmpty(),
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Previous")
            }
            Button(
                onClick = {
                    if(listOfPreviousFlashcardsId.value.size>maxListSize){
                        listOfPreviousFlashcardsId.value= listOfPreviousFlashcardsId.value.drop(1)
                    }
                    listOfPreviousFlashcardsId.value += selectedFlashcard.value!!.id
                    if(listOfNextFlashcardsId.value.isNotEmpty()){
                        val nextFlashcardId= listOfNextFlashcardsId.value.last()
                        listOfNextFlashcardsId.value= listOfNextFlashcardsId.value.dropLast(1)
                        flashcardViewModel.selectFlashcard(flashcardList.value.first { it.id==nextFlashcardId })
                    } else{
                        val withoutCurrent=userFlashcardList.value.filter { it.id!=selectedFlashcard.value!!.id }
                        val selectedUserFlashcard = UserFlashcard.selectRandomFlashcardLinear(withoutCurrent)
                        flashcardViewModel.selectFlashcard(flashcardList.value.first { it.id==selectedUserFlashcard.id })
                    }

                },
                enabled = userFlashcardList.value.size>1,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("next")
            }
        }
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        )
        {
            Button(
                onClick = {
                    userViewModel.updateUserFlashcard(
                        userViewModelFlashcards.value[selectedFlashcard.value!!.id]!!.increaseLevel(),
                        onSuccess = {
                            userFlashcardList.value= userFlashcardList.value.map { userFlashcard ->
                                if(userFlashcard.id==selectedFlashcard.value!!.id){
                                    userViewModelFlashcards.value[selectedFlashcard.value!!.id]!!
                                }else{
                                    userFlashcard
                                }
                            }
                        }

                    )
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("I got it right")
            }
            Button(
                onClick = {
                    userViewModel.updateUserFlashcard(
                        userViewModelFlashcards.value[selectedFlashcard.value!!.id]!!.decreaseLevel(),
                        onSuccess =
                        {
                            userFlashcardList.value= userFlashcardList.value.map { userFlashcard ->
                                if(userFlashcard.id==selectedFlashcard.value!!.id){
                                    userViewModelFlashcards.value[selectedFlashcard.value!!.id]!!
                                }else{
                                    userFlashcard
                                }
                            }
                        }
                    )
                },
                modifier = Modifier.padding(16.dp)
            ) {
                Text("I got it wrong")
            }
        }
    }

}

@Composable
private fun TestMode(
    flashcardViewModel: FlashcardViewModel,
    flashcardList: State<List<Flashcard>>,
    userViewModel: UserViewModel,
    userViewModelFlashcards: State<Map<String, UserFlashcard>>,
    selectedFlashcard: State<Flashcard?>,
    score: MutableIntState,
    isFinished: MutableState<Boolean>,
    answers: Map<String, MutableState<Int?>>
) {
    val currentFlashcardIndex = remember { mutableIntStateOf(0) }
    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.CenterHorizontally)
        ) {
            Button(
                onClick = {
                    currentFlashcardIndex.intValue--
                    if (currentFlashcardIndex.intValue < 0) {
                        currentFlashcardIndex.intValue = 0
                    } else {
                        flashcardViewModel.selectFlashcard(flashcardList.value[currentFlashcardIndex.intValue])
                    }
                },
                enabled = currentFlashcardIndex.intValue > 0,
                modifier = Modifier.padding(16.dp)
            ) {
                Text("Previous")
            }
            Button(
                onClick = {
                    if(currentFlashcardIndex.intValue == flashcardList.value.size - 1
                    ) {
                        isFinished.value=true

                    }else {
                        currentFlashcardIndex.intValue++
                        if (currentFlashcardIndex.intValue >= flashcardList.value.size) {
                            currentFlashcardIndex.intValue = flashcardList.value.size - 1
                        } else {
                            flashcardViewModel.selectFlashcard(flashcardList.value[currentFlashcardIndex.intValue])
                        }
                    }
                },
                enabled = true,
                modifier = Modifier.padding(16.dp)
            ) {
                if(
                    currentFlashcardIndex.intValue == flashcardList.value.size - 1
                ) {
                    Text("Submit")
                } else {
                Text("next")
                }
            }
        }

        if(selectedFlashcard.value?.isMCQ() == false) {
            SelectWrongRight(score,answers,selectedFlashcard)
        }
    }
}

@Composable
private fun SelectWrongRight(
    score: MutableIntState,
    answers: Map<String, MutableState<Int?>>,
    selectedFlashcard: State<Flashcard?>,) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
    )
    {
        Button(
            onClick = {
                score.value += 1;
                answers[selectedFlashcard.value!!.id]!!.value = 0
                Log.e("enableF", answers.toString())
                Log.e("enableF", selectedFlashcard.value!!.id)
            },
            modifier = Modifier.padding(16.dp),
            enabled = answers[selectedFlashcard.value!!.id]!!.value == null
        ) {
            Text("I got it right")
        }
        Button(
            onClick = {
                answers[selectedFlashcard.value!!.id]!!.value =1
                Log.e("enableF", answers.toString())
                Log.e("enableF", selectedFlashcard.value!!.id)

            },
            modifier = Modifier.padding(16.dp),
            enabled = answers[selectedFlashcard.value!!.id]!!.value == null
        ) {
            Text("I got it wrong")
        }
    }
}


