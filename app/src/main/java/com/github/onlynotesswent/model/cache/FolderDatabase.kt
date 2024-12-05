package com.github.onlynotesswent.model.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.github.onlynotesswent.model.folder.Folder

@Database(entities = [Folder::class], version = 1)
@TypeConverters(TimestampConverter::class)
abstract class FolderDatabase : RoomDatabase() {
  abstract fun folderDao(): FolderDao

  companion object {
    @Volatile private var INSTANCE: FolderDatabase? = null

    fun getFolderDatabase(context: Context): FolderDatabase {
      return INSTANCE
          ?: synchronized(this) {
            Room.databaseBuilder(context, FolderDatabase::class.java, "folder_database")
                .build()
                .also { INSTANCE = it }
          }
    }
  }
}
