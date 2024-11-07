package com.github.onlynotesswent.utils

import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.note.Note
import com.google.firebase.Timestamp
import com.google.gson.JsonParser
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotesToFlashcard(
    private val flashcardViewModel: FlashcardViewModel,
    private val fileViewModel: FileViewModel,
    private val openAIClient: OpenAI
) {
  companion object {
    const val PROMPT_PREFIX =
        "Convert the following notes into a JSON array of flashcards with 'question' and 'answer' fields, only return the json array with no additional text. Here is the note content in mark down: "
  }

  /**
   * Converts a note into a list of flashcards by making a request to OpenAI API. It is a suspend
   * function to allow for asynchronous execution.
   *
   * @param note the note to be converted into flashcards
   * @return a list of flashcards generated from the note
   */
  suspend fun convertNoteToFlashcards(note: Note): List<Flashcard> {
    return withContext(Dispatchers.IO) { // Run the API request on the IO thread
      try {
        val responseJson =
            withContext(Dispatchers.IO) {
              // Fetch the markdown file content for the note
              val noteContent =
                  suspendCoroutine<String> { continuation ->
                    fileViewModel.getFile(
                        note.id,
                        FileType.NOTE_TEXT,
                        onSuccess = { byteArray -> continuation.resume(String(byteArray)) },
                        onFailure = { exception -> continuation.resumeWithException(exception) })
                  }

              // Use the content to create the prompt for OpenAI
              // TODO: modify note.content to markdown format when we implement markdown support
              openAIClient.sendRequest(PROMPT_PREFIX + note.content)
            }

        parseFlashcardsFromJson(responseJson, note)
      } catch (e: Exception) {
        // TODO: Handle exceptions
        emptyList<Flashcard>()
      }
    }
  }

  /**
   * Parses a JSON string response from the OpenAI API to extract flashcard data and converts it
   * into a list of Flashcard objects.
   *
   * @param jsonResponse the JSON response from OpenAI API containing flashcard data
   * @return a list of Flashcard objects
   */
  private fun parseFlashcardsFromJson(jsonResponse: String, note: Note): List<Flashcard> {
    val flashcards = mutableListOf<Flashcard>()
    try {
      val json = JsonParser.parseString(jsonResponse).asJsonObject
      val choices = json.getAsJsonArray("choices")

      if (choices != null && choices.size() > 0) {
        val content = choices[0].asJsonObject.get("text").asString
        val flashcardArray = JsonParser.parseString(content).asJsonArray

        flashcardArray.forEach { element ->
          val flashcardObject = element.asJsonObject
          val question = flashcardObject.get("question").asString
          val answer = flashcardObject.get("answer").asString

          flashcards.add(
              Flashcard(
                  id = flashcardViewModel.getNewUid(),
                  front = question,
                  back = answer,
                  nextReview = Timestamp.now(),
                  userId = note.userId,
                  folderId = "",
                  noteId = note.id))
        }
      }
    } catch (e: Exception) {
      //TODO: Handle exceptions
    }
    return flashcards
  }
}
