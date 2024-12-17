package com.github.onlynotesswent.model.flashcard.deck


data class PlayDeckHistory(
    val listOfPreviousFlashcardsId: List<String> = emptyList(),
    val listOfNextFlashcardsId: List<String> = emptyList(),
    val currentFlashcardId: String
){
    companion object{
        const val MAX_LIST_LENGTH = 10
    }

    /**
     * Adds a flashcard to the list of previous flashcards.
     *
     * @param flashcardId The ID of the flashcard to add.
     */
    fun goForwardWithNewFlashcard(flashcardId: String): PlayDeckHistory {
        if (listOfPreviousFlashcardsId.size >= MAX_LIST_LENGTH) {
            return PlayDeckHistory(
                listOfPreviousFlashcardsId.drop(1) + currentFlashcardId,
                listOfNextFlashcardsId,
                flashcardId
            )
        }
        return PlayDeckHistory(
            listOfPreviousFlashcardsId + currentFlashcardId,
            listOfNextFlashcardsId,
            flashcardId
        )
    }

    /**
     * Goes back to the previous flashcard.
     *
     * @return The updated PlayDeckHistory.
     */
    fun goBack(): PlayDeckHistory {
        if (listOfPreviousFlashcardsId.isEmpty()) {
            throw IllegalStateException("The list of previous flashcards is empty")
        }
        if (listOfNextFlashcardsId.size >= MAX_LIST_LENGTH) {
            return PlayDeckHistory(
                listOfPreviousFlashcardsId.dropLast(1),
                listOf(currentFlashcardId) + listOfNextFlashcardsId.dropLast(1),
                listOfPreviousFlashcardsId.last()
            )
        }
        return PlayDeckHistory(
            listOfPreviousFlashcardsId.dropLast(1),
            listOf(currentFlashcardId) + listOfNextFlashcardsId,
            listOfPreviousFlashcardsId.last()
        )

    }

    /**
     * Goes forward to the next flashcard.
     *
     * @return The updated PlayDeckHistory.
     */
    fun goForward(): PlayDeckHistory {
        if (listOfNextFlashcardsId.isEmpty()) {
           throw IllegalStateException("The list of next flashcards is empty")
        }
        if(listOfPreviousFlashcardsId.size >= MAX_LIST_LENGTH){
            return PlayDeckHistory(
                listOfPreviousFlashcardsId.drop(1) +listOf(currentFlashcardId),
                listOfNextFlashcardsId.drop(1),
                listOfNextFlashcardsId.first()
            )
        }
        return PlayDeckHistory(
            listOfPreviousFlashcardsId + listOf(currentFlashcardId),
            listOfNextFlashcardsId.drop(1),
            listOfNextFlashcardsId.first()
        )
    }

    /**
     * Checks if the user can go back to the previous flashcard.
     *
     * @return true if the user can go back, false otherwise.
     */
    fun canGoBack(): Boolean {
        return listOfPreviousFlashcardsId.isNotEmpty()
    }

    /**
     * Checks if the user can go forward to the next flashcard.
     */
    fun canGoForward(): Boolean {
        return listOfNextFlashcardsId.isNotEmpty()
    }

}
