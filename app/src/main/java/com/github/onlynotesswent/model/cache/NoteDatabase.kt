package com.github.onlynotesswent.model.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.onlynotesswent.model.note.Note

@Database(entities = [Note::class], version = 1, exportSchema = false)
@TypeConverters(TimestampConverter::class, CommentCollectionConverter::class)
abstract class NoteDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao

  companion object {
    @Volatile private var INSTANCE: NoteDatabase? = null

    fun getNoteDatabase(context: Context): NoteDatabase {
      return INSTANCE
          ?: synchronized(this) {
            Room.databaseBuilder(context, NoteDatabase::class.java, "note_database").build().also {
              INSTANCE = it
            }
          }
    }
  }
}
