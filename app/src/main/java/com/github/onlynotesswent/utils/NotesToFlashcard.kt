package com.github.onlynotesswent.utils

import android.content.Context
import android.util.Log
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.note.Note
import com.google.gson.JsonParser

/**
 * A utility class that converts notes into flashcards using the OpenAI API.
 *
 * @property flashcardViewModel The FlashcardViewModel instance to interact with flashcard data.
 * @property fileViewModel The FileViewModel instance to interact with file data.
 * @property openAIClient The OpenAI client to send requests to the OpenAI API.
 * @property context The application context.
 */
class NotesToFlashcard(
    private val flashcardViewModel: FlashcardViewModel,
    private val fileViewModel: FileViewModel,
    private val deckViewModel: DeckViewModel,
    private val openAIClient: OpenAI,
    private val context: Context,
) {
  companion object {
    private const val TAG = "NotesToFlashcard"
  }

  private val promptPrefix =
      "Convert the following notes into a JSON array of flashcards with 'question' and 'answer' fields, only return the json array with no additional text. Here is the note content: "

  /**
   * Converts a note into a deck of flashcards using the OpenAI API.
   *
   * @param note the note to be converted into flashcards
   * @param onSuccess the callback function to invoke on a successful conversion
   * @param onFileNotFoundException the callback function to invoke when the note text file is not
   *   found
   * @param onFailure the callback function to invoke on a failed conversion
   */
  fun convertNoteToDeck(
      note: Note,
      onSuccess: (Deck) -> Unit,
      onFileNotFoundException: () -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    // Download the note text file,
    fileViewModel.downloadFile(
        uid = note.id,
        fileType = FileType.NOTE_TEXT,
        context = context,
        onSuccess = { downloadedFile ->
          openAIClient.sendRequest(
              promptPrefix + downloadedFile.readText(),
              { parseFlashcardsFromJson(it, note, onSuccess, onFailure) },
              { onFailure(it) })
        },
        onFileNotFound = onFileNotFoundException,
        onFailure = onFailure)
  }

  /**
   * Parses a JSON string response from the OpenAI API to extract flashcard data and converts it
   * into a deck of flashcards.
   *
   * @param jsonResponse the JSON response from OpenAI API containing flashcard data
   * @param note the note to be converted into flashcards
   * @param onSuccess the callback function to invoke on a successful conversion
   * @param onFailure the callback function to invoke on a failed conversion
   */
  private fun parseFlashcardsFromJson(
      jsonResponse: String,
      note: Note,
      onSuccess: (Deck) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val flashcards = mutableListOf<Flashcard>()
    try {
      val json = JsonParser.parseString(jsonResponse).asJsonObject
      val choices = json.getAsJsonArray("choices")

      if (choices != null && choices.size() > 0) {
        // Extract the "content" field from the first choice
        val content = choices[0].asJsonObject.get("message").asJsonObject.get("content").asString
        val flashcardArray = JsonParser.parseString(content).asJsonArray

        flashcardArray.forEach { element ->
          val flashcardObject = element.asJsonObject
          val question = flashcardObject.get("question").asString
          val answer = flashcardObject.get("answer").asString

          val flashcard =
              Flashcard(
                  id = flashcardViewModel.getNewUid(),
                  front = question,
                  back = answer,
                  lastReviewed = null,
                  userId = note.userId,
                  folderId = note.folderId,
                  noteId = note.id)
          flashcards.add(flashcard)
          flashcardViewModel.addFlashcard(flashcard)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing flashcards from JSON", e)
      println("Error parsing flashcards from JSON $e")
      onFailure(e)
      return
    }
    val deck =
        Deck(
            id = deckViewModel.getNewUid(),
            name = note.title,
            userId = note.userId,
            folderId = note.folderId,
            visibility = note.visibility,
            lastModified = note.date,
            flashcardIds = flashcards.map { it.id })
    deckViewModel.updateDeck(deck)
    onSuccess(deck)
  }
}
