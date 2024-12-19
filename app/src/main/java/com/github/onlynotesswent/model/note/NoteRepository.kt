package com.github.onlynotesswent.model.note

import com.github.onlynotesswent.model.user.User
import com.github.onlynotesswent.model.user.UserViewModel

interface NoteRepository {

  /**
   * Generates a new note ID.
   *
   * @return The new note ID.
   */
  fun getNewUid(): String

  /**
   * Initializes the repository.
   *
   * @param onSuccess Callback to be invoked when initialization is successful.
   */
  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves all public notes.
   *
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getPublicNotes(onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all friends only notes from a list of following users.
   *
   * @param followingListIds The list of users Ids to retrieve friends only notes from.
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNotesFromFollowingList(
      followingListIds: List<String>,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves all notes from a user (irrespective of folderId value).
   *
   * @param userId The ID of the user to retrieve notes for.
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  suspend fun getNotesFromUid(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves all root notes from a user (folderId == null).
   *
   * @param userId The ID of the user to retrieve root notes for.
   * @param onSuccess Callback to be invoked with the retrieved root notes.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  suspend fun getRootNotesFromUid(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves a note by its ID.
   *
   * @param id The ID of the note to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved note.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  suspend fun getNoteById(
      id: String,
      onSuccess: (Note) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Adds a note.
   *
   * @param note The note to add.
   * @param onSuccess Callback to be invoked if the note is added successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  suspend fun addNote(
      note: Note,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Adds a list of notes.
   *
   * @param notes The notes to add.
   * @param onSuccess Callback to be invoked if the notes are added successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the notes
   *   is the current user.
   */
  suspend fun addNotes(
      notes: List<Note>,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Updates a note.
   *
   * @param note The note to update.
   * @param onSuccess Callback to be invoked if the note is updated successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  suspend fun updateNote(
      note: Note,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Deletes a note by its ID.
   *
   * @param id The ID of the note to delete.
   * @param onSuccess Callback to be invoked if the note is deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the note is
   *   the current user.
   */
  suspend fun deleteNoteById(
      id: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Deletes all notes from a user.
   *
   * @param userId The ID of the user to delete notes for.
   * @param onSuccess Callback to be invoked if the notes are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if [userId] is the
   *   current user.
   */
  suspend fun deleteNotesFromUid(
      userId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes for.
   * @param userViewModel The user view model. If the function can only be called by a user that is
   *   the owner of the note/folder, this parameter should be null.
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  suspend fun getNotesFromFolder(
      folderId: String,
      userViewModel: UserViewModel?,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Deletes all notes from a folder.
   *
   * @param folderId The ID of the folder to delete notes from.
   * @param onSuccess Callback to be invoked if the notes are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the folder
   *   is the current user.
   */
  suspend fun deleteNotesFromFolder(
      folderId: String,
      onSuccess: () -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )

  /**
   * Retrieves a list of saved notes by their IDs. This only returns notes that are visible to the
   * current user. The list of currently saved notes that don't comply is also returned.
   *
   * @param savedNotesIds The list of note IDs to retrieve.
   * @param currentUser The current user.
   * @param onSuccess Callback to be invoked with the retrieved notes and the list of missing notes.
   * @param onFailure Callback to be invoked if an error occurs.
   * @param useCache Whether to update data from cache. Should be true only if userId of the notes
   *   is the current user.
   */
  suspend fun getSavedNotesByIds(
      savedNotesIds: List<String>,
      currentUser: User,
      onSuccess: (List<Note>, List<String>) -> Unit,
      onFailure: (Exception) -> Unit,
      useCache: Boolean
  )
}
