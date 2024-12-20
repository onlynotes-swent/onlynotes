package com.github.onlynotesswent.model.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.onlynotesswent.model.note.Note

@Dao
interface NoteDao {
  @Query("SELECT * FROM note WHERE id = :noteId") fun getNoteById(noteId: String): Note?

  @Query("SELECT * FROM note WHERE userid = :userId")
  fun getNotesFromUid(userId: String): List<Note>

  @Query("SELECT * FROM note WHERE folderid IS NULL AND userid = :userId")
  fun getRootNotesFromUid(userId: String): List<Note>

  @Query("SELECT * FROM note WHERE id IN(:noteIds)")
  fun getNotesByIds(noteIds: List<String>): List<Note>

  @Query("SELECT * FROM note WHERE folderid = :folderId")
  fun getNotesFromFolder(folderId: String): List<Note>

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addNote(note: Note)

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addNotes(notes: List<Note>)

  @Query("DELETE FROM note WHERE id = :noteId") fun deleteNoteById(noteId: String)

  @Query("DELETE FROM note WHERE id IN(:noteIds)") fun deleteNotesByIds(noteIds: List<String>)

  @Query("DELETE FROM note WHERE userid = :userId") fun deleteNotesFromUid(userId: String)

  @Query("DELETE FROM note WHERE folderid = :folderId") fun deleteNotesFromFolder(folderId: String)
}
