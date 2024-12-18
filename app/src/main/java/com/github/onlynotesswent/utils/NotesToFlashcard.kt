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
import java.io.FileNotFoundException
import java.util.concurrent.atomic.AtomicInteger
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

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

  private val noteSemaphore = Semaphore(10) // Allow 10 concurrent note processing tasks
  private val subfolderSemaphore = Semaphore(4) // Allow 4 concurrent subfolder processing tasks

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
      folderId: String? = null,
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
              { parseFlashcardsFromJson(it, note, folderId, onSuccess, onFailure) },
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
   * @param folderId the ID of the folder containing the note
   * @param onSuccess the callback function to invoke on a successful conversion
   * @param onFailure the callback function to invoke on a failed conversion
   */
  private fun parseFlashcardsFromJson(
      jsonResponse: String,
      note: Note,
      folderId: String? = null,
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
                  userId = note.userId,
                  folderId = folderId,
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
            folderId = folderId,
            visibility = note.visibility,
            lastModified = Timestamp.now(),
            flashcardIds = flashcards.map { it.id })
    deckViewModel.updateDeck(deck)
    onSuccess(deck)
  }

  /**
   * Converts a note into a deck of flashcards using the OpenAI API. This method is a suspend
   * function that can be called from a coroutine.
   *
   * @param note the note to be converted into flashcards
   * @param folderId the ID of the folder containing the note
   * @return the deck of flashcards created from the note
   */
  private suspend fun convertNoteToDeckSuspend(note: Note, folderId: String): Deck =
      suspendCoroutine { continuation ->
        convertNoteToDeck(
            note,
            folderId,
            onSuccess = { continuation.resume(it) },
            onFailure = { continuation.resumeWithException(it) },
            onFileNotFoundException = {
              continuation.resumeWithException(
                  FileNotFoundException("Text file not found for note: ${note.title}"))
            })
      }

  /**
   * Converts a folder and its subfolders into decks of flashcards.
   *
   * This method starts the conversion process for the selected folder and ensures all its
   * subfolders are processed recursively. Each note in the folder is converted into a deck of
   * flashcards, and combined decks are created for a folder if it contains multiple notes.
   *
   * @param onProgress Callback invoked when a note is converted with the current progress.
   * @param onSuccess Callback invoked when the conversion is successful with the final deck.
   * @param onFailure Callback invoked when the conversion process fails with an exception.
   */
  suspend fun convertFolderToDecks(
      onProgress: (Int, Int, Exception?) -> Unit,
      onSuccess: (Deck) -> Unit,
      onFailure: (Exception) -> Unit
  ) {
    val currentFolder = folderViewModel.selectedFolder.value
    if (currentFolder == null) {
      onFailure(IllegalStateException("No folder selected"))
      return
    }

    val notesProcessedCounter = AtomicInteger(0)
    val foldersProcessedCounter = AtomicInteger(0)

    try {
      val deckFolder = getOrCreateDeckSubFolder(currentFolder, null)
      val finalDeck =
          processFolderRecursively(
              folder = currentFolder,
              deckFolder = deckFolder,
              notesProcessed = notesProcessedCounter,
              foldersProcessed = foldersProcessedCounter,
              onProgress = onProgress)

      // Reset note lists
      noteViewModel.getNotesFromFolder(folderId = currentFolder.id, null)

      if (finalDeck != null) {
        onSuccess(finalDeck)
      } else {
        onFailure(IllegalStateException("No deck was created"))
      }
    } catch (e: Exception) {
      Log.e(TAG, "Error during folder conversion: ${currentFolder.name}", e)
      onFailure(e)
    }
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
   * @param notesProcessed The counter to track the number of notes processed.
   * @param foldersProcessed The counter to track the number of folders processed.
   * @param onProgress Callback invoked when processing a note or subfolder with the current
   *   progress.
   */
  private suspend fun processFolderRecursively(
      folder: Folder,
      deckFolder: Folder,
      notesProcessed: AtomicInteger,
      foldersProcessed: AtomicInteger,
      onProgress: (Int, Int, Exception?) -> Unit
  ): Deck? {
    var finalDeck: Deck? = null
    val notes = suspendCoroutine { continuation ->
      noteViewModel.getNotesFromFolder(
          folder.id,
          null,
          onSuccess = { continuation.resume(it) },
          onFailure = { continuation.resumeWithException(it) })
    }

    val folderFlashcardIds = mutableListOf<String>()
    val noteDeferreds =
        notes.map { note ->
          CoroutineScope(Dispatchers.IO).async {
            noteSemaphore.withPermit {
              try {
                val deck = convertNoteToDeckSuspend(note, deckFolder.id)
                finalDeck = deck
                folderFlashcardIds.addAll(deck.flashcardIds)
                onProgress(notesProcessed.incrementAndGet(), foldersProcessed.get(), null)
              } catch (e: Exception) {
                // Log the error and continue processing other notes
                Log.e(TAG, "Failed to convert note ${note.id} to deck", e)
                onProgress(notesProcessed.get(), foldersProcessed.get(), e)
              }
            }
          }
        }

    val subfolders = suspendCoroutine { continuation ->
      folderViewModel.getSubFoldersOfNoStateUpdate(
          parentFolderId = folder.id,
          userViewModel = null,
          onSuccess = { continuation.resume(it) },
          onFailure = { continuation.resumeWithException(it) })
    }

    val subfolderDeferreds =
        subfolders.map { subfolder ->
          CoroutineScope(Dispatchers.IO).async {
            subfolderSemaphore.withPermit {
              try {
                val subDeckFolder = getOrCreateDeckSubFolder(subfolder, deckFolder)
                processFolderRecursively(
                    subfolder, subDeckFolder, notesProcessed, foldersProcessed, onProgress)
                onProgress(notesProcessed.get(), foldersProcessed.incrementAndGet(), null)
              } catch (e: Exception) {
                // Log the error and continue processing other subfolders
                Log.e(TAG, "Failed to process subfolder ${subfolder.id}", e)
                onProgress(notesProcessed.get(), foldersProcessed.get(), e)
              }
            }
          }
        }

    noteDeferreds.awaitAll()
    subfolderDeferreds.awaitAll()

    return if (folderFlashcardIds.isNotEmpty() && notes.size > 1) {
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
      combinedDeck
    } else {
      finalDeck
    }
  }

  /**
   * Retrieves an existing deck subfolder or creates a new one if it does not exist.
   *
   * This method ensures that a deck subfolder matching the given subfolder name exists in the
   * specified parent deck folder. If no matching folder exists, a new one is created and returned.
   *
   * @param subfolder The subfolder to check or create.
   * @param parentDeckFolder The parent deck folder under which the subfolder resides.
   */
  private suspend fun getOrCreateDeckSubFolder(
      subfolder: Folder,
      parentDeckFolder: Folder?
  ): Folder = suspendCoroutine { continuation ->
    folderViewModel.getDeckFoldersByName(
        subfolder.name,
        subfolder.userId,
        onFolderNotFound = {
          val newDeckFolder =
              Folder(
                  id = folderViewModel.getNewFolderId(),
                  name = subfolder.name,
                  userId = subfolder.userId,
                  visibility = subfolder.visibility,
                  parentFolderId = parentDeckFolder?.id,
                  isDeckFolder = true,
                  lastModified = Timestamp.now())
          folderViewModel.addFolder(
              newDeckFolder,
              onSuccess = { continuation.resume(newDeckFolder) },
              onFailure = { continuation.resumeWithException(it) })
        },
        onSuccess = { existingFolders ->
          if (parentDeckFolder == null) {
            continuation.resume(existingFolders.first())
          } else {
            val matchingFolder =
                existingFolders.firstOrNull { it.parentFolderId == parentDeckFolder.id }
            if (matchingFolder != null) {
              continuation.resume(matchingFolder)
            } else {
              val newDeckFolder =
                  Folder(
                      id = folderViewModel.getNewFolderId(),
                      name = subfolder.name,
                      userId = subfolder.userId,
                      visibility = subfolder.visibility,
                      parentFolderId = parentDeckFolder.id,
                      isDeckFolder = true,
                      lastModified = Timestamp.now())
              folderViewModel.addFolder(
                  newDeckFolder,
                  onSuccess = { continuation.resume(newDeckFolder) },
                  onFailure = { continuation.resumeWithException(it) })
            }
          }
        },
        onFailure = { continuation.resumeWithException(it) })
  }
}
