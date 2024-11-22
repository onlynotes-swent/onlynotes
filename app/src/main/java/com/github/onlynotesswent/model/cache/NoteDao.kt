package com.github.onlynotesswent.model.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.github.onlynotesswent.model.note.Note

@Dao
interface NoteDao {
  @Query("SELECT * FROM note WHERE id = :uid") fun getNoteById(uid: String): Note?

  @Query("SELECT * FROM note") fun getNotes(): List<Note>

  @Query("SELECT * FROM note WHERE folderid IS NULL") fun getRootNotes(): List<Note>

  @Query("SELECT * FROM note WHERE folderid = :folderId")
  fun getNotesFromFolder(folderId: String): List<Note>

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertNote(note: Note)

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertNotes(notes: List<Note>)

  @Update fun updateNote(note: Note)

  @Query("DELETE FROM note WHERE id = :uid") fun deleteNoteById(uid: String)

  @Query("DELETE FROM note") fun deleteNotes()
}
