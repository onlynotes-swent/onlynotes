package com.github.onlynotesswent.model.cache

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.github.onlynotesswent.model.folder.Folder

@Database(entities = [Folder::class], version = 1)
abstract class FolderDatabase : RoomDatabase() {
  abstract fun folderDao(): FolderDao
}

private lateinit var INSTANCE: FolderDatabase

fun getFolderDatabase(context: Context): FolderDatabase {
  if (!::INSTANCE.isInitialized) {
    INSTANCE =
        Room.databaseBuilder(
                context.applicationContext, FolderDatabase::class.java, "folder_database")
            .fallbackToDestructiveMigration()
            .build()
  }
  return INSTANCE
}
