package com.github.onlynotesswent.model.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.onlynotesswent.model.folder.Folder
import com.github.onlynotesswent.model.note.Note

@Database(entities = [Note::class, Folder::class], version = 1, exportSchema = false)
@TypeConverters(TimestampConverter::class, CommentCollectionConverter::class)
abstract class CacheDatabase : RoomDatabase() {
  abstract fun noteDao(): NoteDao

  abstract fun folderDao(): FolderDao

  companion object {
    @Volatile private var INSTANCE: CacheDatabase? = null

    fun getDatabase(context: Context): CacheDatabase {
      return INSTANCE
          ?: synchronized(this) {
            Room.databaseBuilder(context, CacheDatabase::class.java, "onlynotes_database")
                .build()
                .also { INSTANCE = it }
          }
    }
  }
}
