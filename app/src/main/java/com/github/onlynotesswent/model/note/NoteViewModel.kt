package com.github.onlynotesswent.model.note

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NoteViewModel(private val repository: NoteRepository) : ViewModel() {

   private val notes_ = MutableStateFlow<List<Note>>(emptyList())
    val note: StateFlow<List<Note>> = notes_.asStateFlow()

    init {
        repository.init {  }
      }

      companion object {
        val Factory: ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
              @Suppress("UNCHECKED_CAST")
              override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return NoteViewModel(ImplementationNoteRepository(Firebase.firestore)) as T
              }
            }
      }

    fun getNewUid(): String {
        return repository.getNewUid()
    }

    fun getNotes(userID: String) {
     repository.getNotes(userID, onSuccess = { notes_.value = it }, onFailure = {})
    }

    fun getNoteById(noteId: String, userID: String) {
        repository.deleteNoteById(id = noteId, onSuccess = { getNotes(userID)}, onFailure = {})
    }

    fun insertNote(note: Note, userID: String) {
        repository.insertNote(note = note, onSuccess = { getNotes(userID) }, onFailure = {})
    }

    fun updateNote(note: Note, userID: String) {
        repository.updateNote(note = note, onSuccess = { getNotes(userID) }, onFailure = {})
    }

    fun deleteNoteById(noteId: String, userID: String) {
        repository.deleteNoteById(id = noteId, onSuccess = { getNotes(userID) }, onFailure = {})
    }

}