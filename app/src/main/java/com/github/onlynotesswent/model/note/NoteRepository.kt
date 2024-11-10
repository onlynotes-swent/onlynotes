package com.github.onlynotesswent.model.note

interface NoteRepository {

  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  /**
   * Retrieves all public notes.
   *
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getPublicNotes(onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all notes from a user (irrespective of folderId value).
   *
   * @param userId The ID of the user to retrieve notes for.
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNotesFrom(userId: String, onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all root notes from a user (folderId == null).
   *
   * @param userId The ID of the user to retrieve root notes for.
   * @param onSuccess Callback to be invoked with the retrieved root notes.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getRootNotesFrom(
      userId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit
  )

  /**
   * Retrieves a note by its ID.
   *
   * @param id The ID of the note to retrieve.
   * @param onSuccess Callback to be invoked with the retrieved note.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNoteById(id: String, onSuccess: (Note) -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Adds a note.
   *
   * @param note The note to add.
   * @param onSuccess Callback to be invoked if the note is added successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun addNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Updates a note.
   *
   * @param note The note to update.
   * @param onSuccess Callback to be invoked if the note is updated successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun updateNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Deletes a note by its ID.
   *
   * @param id The ID of the note to delete.
   * @param onSuccess Callback to be invoked if the note is deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun deleteNoteById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)


  /**
   * Deletes all notes from a user.
   *
   * @param userId The ID of the user to delete notes for.
   * @param onSuccess Callback to be invoked if the notes are deleted successfully.
   * @param onFailure Callback to be invoked if an error occurs.
   */

  fun deleteNotesByUserId(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes for.
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNotesFromFolder(
      folderId: String,
      onSuccess: (List<Note>) -> Unit,
      onFailure: (Exception) -> Unit
  )
}
