package com.github.onlynotesswent.utils

import android.content.Context
import android.util.Log
import com.github.onlynotesswent.model.file.FileType
import com.github.onlynotesswent.model.file.FileViewModel
import com.github.onlynotesswent.model.flashcard.Flashcard
import com.github.onlynotesswent.model.flashcard.FlashcardViewModel
import com.github.onlynotesswent.model.flashcard.deck.Deck
import com.github.onlynotesswent.model.flashcard.deck.DeckViewModel
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.folder.FolderViewModel
import com.github.onlynotesswent.model.note.Note
import com.github.onlynotesswent.model.note.NoteViewModel
import com.google.firebase.Timestamp
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
    private val noteViewModel: NoteViewModel,
    private val folderViewModel: FolderViewModel,
    private val openAIClient: OpenAI,
    private val context: Context,
) {
  companion object {
    private const val TAG = "NotesToFlashcard"
  }

  private val promptPrefix =
      """Convert the following notes into a JSON array of flashcards. 
       Each flashcard should include the fields:
       - 'question' (front side of the flashcard),
       - 'answer' (back side of the flashcard),
       - 'latexFormula' (a LaTeX formula in LaTeX format, if applicable, can be empty otherwise),
       - 'fakeBacks' (a list of incorrect answers for MCQs, can be empty for non-MCQs).
       You can create flashcards as either:
       1. Regular flashcards with 'question' and 'answer', or
       2. MCQs with 'question', 'answer', and at least two 'fakeBacks'.
       Return only the JSON array with no additional text. If the note content is empty, return an 
       empty JSON array and no additional text. Here is the note content: """

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
          val latexFormula = flashcardObject.get("latexFormula")?.asString ?: ""
          val fakeBacks =
              flashcardObject.getAsJsonArray("fakeBacks")?.map { it.asString } ?: emptyList()

          val flashcard =
              Flashcard(
                  id = flashcardViewModel.getNewUid(),
                  front = question,
                  back = answer,
                  latexFormula = latexFormula,
                  hasImage = false,
                  fakeBacks = fakeBacks,
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
            lastModified = Timestamp.now(),
            flashcardIds = flashcards.map { it.id })
    deckViewModel.updateDeck(deck)
    onSuccess(deck)
  }

  /**
   * Converts a folder and its subfolders into decks of flashcards.
   *
   * This method starts the conversion process for the selected folder and ensures all its
   * subfolders are processed recursively. Each note in the folder is converted into a deck of
   * flashcards, and combined decks are created for a folder if it contains multiple notes.
   *
   * @param onSuccess Callback invoked when the conversion is successful with the final deck.
   * @param onFailure Callback invoked when the conversion process fails with an exception.
   */
  fun convertFolderToDecks(onSuccess: (Deck) -> Unit, onFailure: (Exception) -> Unit) {
    val currentFolder = folderViewModel.selectedFolder.value
    if (currentFolder == null) {
      onFailure(IllegalStateException("No folder selected"))
      return
    }

    getOrCreateDeckSubFolder(
        currentFolder,
        null,
        onSuccess = { deckFolder ->
          processFolderRecursively(currentFolder, deckFolder, onSuccess, onFailure)
          // Reset sub folder list
          folderViewModel.getSubFoldersOf(parentFolderId = currentFolder.id)
        },
        onFailure = { error ->
          Log.e(
              TAG, "Failed to create or retrieve root deck folder for ${currentFolder.name}", error)
          onFailure(error)
        })
  }

  /**
   * Processes a folder and its subfolders recursively to convert notes into decks of flashcards.
   *
   * This method retrieves notes and subfolders within a given folder. Each note is converted into a
   * deck of flashcards, and each subfolder is recursively processed. Combined decks are created if
   * a folder contains multiple notes or subfolders.
   *
   * @param folder The folder to process.
   * @param deckFolder The deck folder where decks will be stored.
   * @param onSuccess Callback invoked when the processing is successful with the final deck.
   * @param onFailure Callback invoked when the processing fails with an exception.
   */
  private fun processFolderRecursively(
      folder: Folder,
      deckFolder: Folder,
      onSuccess: (Deck) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    noteViewModel.getNotesFromFolder(
        folder.id,
        onSuccess = { notes ->
          val folderFlashcardIds = mutableListOf<String>()

          // Convert notes to flashcards
          notes.forEach { note ->
            convertNoteToDeck(
                note,
                onSuccess = { deck -> folderFlashcardIds.addAll(deck.flashcardIds) },
                onFileNotFoundException = { Log.e(TAG, "File not found for note ${note.id}") },
                onFailure = { Log.e(TAG, "Failed to convert note ${note.id} to deck", it) })
          }

          // Process subfolders recursively
          folderViewModel.getSubFoldersOf(
              parentFolderId = folder.id,
              onSuccess = { subfolders ->
                subfolders.forEach { subfolder ->
                  getOrCreateDeckSubFolder(
                      subfolder,
                      deckFolder,
                      onSuccess = { deckSubFolder ->
                        processFolderRecursively(subfolder, deckSubFolder, onSuccess, onFailure)
                      },
                      onFailure = { error ->
                        Log.e(
                            TAG,
                            "Failed to create or retrieve deck subfolder for ${subfolder.name}",
                            error)
                        onFailure(error)
                      })
                }

                // Create a combined deck if the folder contains multiple notes and flashcards are
                // generated
                if (folderFlashcardIds.isNotEmpty() && notes.size > 1) {
                  val combinedDeck =
                      Deck(
                          id = deckViewModel.getNewUid(),
                          name = folder.name,
                          userId = folder.userId,
                          folderId = deckFolder.id,
                          visibility = folder.visibility,
                          lastModified = Timestamp.now(),
                          flashcardIds = folderFlashcardIds)
                  deckViewModel.updateDeck(combinedDeck)
                  onSuccess(combinedDeck)
                }
              },
              onFailure = {
                Log.e(TAG, "Failed to retrieve subfolders of folder ${folder.id}", it)
                onFailure(it)
              })
        },
        onFailure = {
          Log.e(TAG, "Failed to retrieve notes in folder ${folder.id}", it)
          onFailure(it)
        })
  }

  /**
   * Retrieves an existing deck subfolder or creates a new one if it does not exist.
   *
   * This method ensures that a deck subfolder matching the given subfolder name exists in the
   * specified parent deck folder. If no matching folder exists, a new one is created and returned.
   *
   * @param subfolder The subfolder to check or create.
   * @param parentDeckFolder The parent deck folder under which the subfolder resides.
   * @param onSuccess Callback invoked when the subfolder is successfully retrieved or created.
   * @param onFailure Callback invoked when the retrieval or creation process fails with an
   *   exception.
   */
  private fun getOrCreateDeckSubFolder(
      subfolder: Folder,
      parentDeckFolder: Folder?,
      onSuccess: (Folder) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    folderViewModel.getDeckFoldersByName(
        subfolder.name,
        subfolder.userId,
        {
          val newDeckFolder =
              Folder(
                  id = folderViewModel.getNewFolderId(),
                  name = subfolder.name,
                  userId = subfolder.userId,
                  visibility = subfolder.visibility,
                  parentFolderId = parentDeckFolder?.id,
                  isDeckFolder = true)

          folderViewModel.addFolder(
              newDeckFolder, onSuccess = { onSuccess(newDeckFolder) }, onFailure = onFailure)
        },
        { existingFolders ->
          if (parentDeckFolder == null) {
            // Use the first folder if no parentDeckFolder exists
            onSuccess(existingFolders.first())
          } else {
            // Find or create a subfolder with the correct parentFolderId
            val matchingFolder =
                existingFolders.firstOrNull { it.parentFolderId == parentDeckFolder.id }
            if (matchingFolder != null) {
              onSuccess(matchingFolder)
            } else {
              val newDeckFolder =
                  Folder(
                      id = folderViewModel.getNewFolderId(),
                      name = subfolder.name,
                      userId = subfolder.userId,
                      visibility = subfolder.visibility,
                      parentFolderId = parentDeckFolder.id,
                      isDeckFolder = true)
              folderViewModel.addFolder(
                  newDeckFolder, onSuccess = { onSuccess(newDeckFolder) }, onFailure = onFailure)
            }
          }
        },
        onFailure = { error ->
          Log.e(TAG, "Failed to get existing deck folders for ${subfolder.name}", error)
          onFailure(error)
        })
  }
}
