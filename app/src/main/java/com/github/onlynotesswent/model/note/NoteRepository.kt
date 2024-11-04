package com.github.onlynotesswent.model.note

interface NoteRepository {

  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  fun getPublicNotes(onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNotesFrom(userId: String, onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNoteById(id: String, onSuccess: (Note) -> Unit, onFailure: (Exception) -> Unit)

  fun addNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteNoteById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  /**
   * Retrieves all notes from a folder.
   *
   * @param folderId The ID of the folder to retrieve notes for.
   * @param onSuccess Callback to be invoked with the retrieved notes.
   * @param onFailure Callback to be invoked if an error occurs.
   */
  fun getNotesFromFolder(folderId: String, onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit)

  // later on we will have to add scanNote, convertNote, downloadNote, etc...

}
