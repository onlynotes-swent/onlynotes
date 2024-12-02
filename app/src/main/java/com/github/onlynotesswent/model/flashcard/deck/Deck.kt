package com.github.onlynotesswent.model.flashcard.deck

import com.github.onlynotesswent.model.common.Visibility

/**
 * Represents a flashcard deck.
 *
 * @property id The ID of the deck.
 * @property name The name of the deck.
 * @property userId The ID of the user who created the deck.
 * @property folderId The ID of the folder that the deck belongs to.
 * @property visibility The visibility of the deck.
 * @property flashcardIds The IDs of the flashcards that the deck contains.
 */
data class Deck(
    val id: String,
    val name: String,
    val userId: String,
    val folderId: String?,
    val visibility: Visibility,
    val flashcardIds: List<String> = emptyList()
)
