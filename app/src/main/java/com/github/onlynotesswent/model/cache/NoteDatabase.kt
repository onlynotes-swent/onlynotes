package com.github.onlynotesswent.model.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.onlynotesswent.model.note.Note

@Database(entities = [Note::class], version = 1)
@TypeConverters(TimestampConverter::class, CommentCollectionConverter::class)
abstract class NoteDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao
}

private lateinit var INSTANCE: NoteDatabase

fun getNoteDatabase(context: Context): NoteDatabase {
  if (!::INSTANCE.isInitialized) {
    INSTANCE =
        Room.databaseBuilder(context.applicationContext, NoteDatabase::class.java, "note_database")
            .fallbackToDestructiveMigration()
            .build()
  }
  return INSTANCE
}
