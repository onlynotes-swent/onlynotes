package com.github.onlynotesswent.utils

import android.util.Log
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.note.Note
import com.google.firebase.Timestamp
import com.google.gson.JsonParser

class NotesToFlashcard(
    private val flashcardViewModel: FlashcardViewModel,
    private val fileViewModel: FileViewModel,
    private val openAIClient: OpenAI
) {
  companion object {
    private const val TAG = "NotesToFlashcard"
  }

  private val promptPrefix =
      "Convert the following notes into a JSON array of flashcards with 'question' and 'answer' fields, only return the json array with no additional text. Here is the note content: "

  /**
   * Converts a note into a list of flashcards by making a request to OpenAI API.
   *
   * @param note the note to be converted into flashcards
   */
  fun convertNoteToFlashcards(
      note: Note,
      onSuccess: (List<Flashcard>) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    try {
      // TODO: When markdown support is implemented, fetch the markdown file content using
      // fileViewModel
      // Example:
      // var markdownContent = ""
      // fileViewModel.downloadFile(
      //         uid = note?.id ?: "errorNoId",
      //         fileType = FileType.NOTE_TEXT,
      //         context = context,
      //         onSuccess = { downloadedFile ->
      //             markdownContent = downloadedFile?.readText() ?: "" // Read the markdown content
      //         },
      //         onFailure = { e ->
      //         Log.e(TAG, "Error downloading file", e)
      //         onFailure(e)
      //         }
      //     )

      // TODO: modify note.content to markdown format when we implement markdown support
      openAIClient.sendRequest(
          promptPrefix + note.content,
          { parseFlashcardsFromJson(it, note, onSuccess, onFailure) },
          { onFailure(it) })
    } catch (e: Exception) {
      Log.e(TAG, "Unexpected error in convertNoteToFlashcards", e)
      onFailure(e)
    }
  }

  /**
   * Parses a JSON string response from the OpenAI API to extract flashcard data and converts it
   * into a list of Flashcard objects.
   *
   * @param jsonResponse the JSON response from OpenAI API containing flashcard data
   */
  private fun parseFlashcardsFromJson(
      jsonResponse: String,
      note: Note,
      onSuccess: (List<Flashcard>) -> Unit,
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
                  nextReview = Timestamp.now(),
                  userId = note.userId,
                  folderId = note.folderId?: "",
                  noteId = note.id)
          flashcards.add(flashcard)
          flashcardViewModel.addFlashcard(flashcard)
        }
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error parsing flashcards from JSON", e)
      onFailure(e)
      return
    }
    onSuccess(flashcards)
  }
}
