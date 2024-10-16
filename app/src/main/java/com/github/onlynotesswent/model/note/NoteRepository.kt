package com.github.onlynotesswent.model.note

interface NoteRepository {

  fun getNewUid(): String

  fun init(onSuccess: () -> Unit)

  fun getNotes(userId: String, onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit)

  fun getNoteById(id: String, onSuccess: (Note) -> Unit, onFailure: (Exception) -> Unit)

  fun addNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun updateNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  fun deleteNoteById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

  // later on we will have to add scanNote, convertNote, downloadNote, etc...

}
