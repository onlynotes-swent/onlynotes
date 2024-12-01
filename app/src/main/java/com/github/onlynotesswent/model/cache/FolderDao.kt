package com.github.onlynotesswent.model.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.onlynotesswent.model.folder.Folder

@Dao
interface FolderDao {
  @Query("SELECT * FROM folder WHERE id = :folderId") fun getFolderById(folderId: String): Folder?

  @Query("SELECT * FROM folder") fun getFolders(): List<Folder>

  @Query("SELECT * FROM folder WHERE parentFolderId IS NULL") fun getRootFolders(): List<Folder>

  @Query("SELECT * FROM folder WHERE parentFolderId = :parentFolderId")
  fun getSubfolders(parentFolderId: String): List<Folder>

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertFolder(folder: Folder)

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun insertFolders(folders: List<Folder>)

  @Query("DELETE FROM folder WHERE id = :folderId") fun deleteFolderById(folderId: String)

  @Query("DELETE FROM folder") fun deleteFolders()

  @Query("DELETE FROM folder WHERE parentFolderId = :parentFolderId")
  fun deleteSubfolders(parentFolderId: String)
}
