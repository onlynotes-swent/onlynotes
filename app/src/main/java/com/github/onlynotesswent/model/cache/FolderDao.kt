package com.github.onlynotesswent.model.cache

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.github.onlynotesswent.model.folder.Folder

@Dao
interface FolderDao {
  @Query("SELECT * FROM folder WHERE id = :folderId") fun getFolderById(folderId: String): Folder?

  @Query("SELECT * FROM folder WHERE userId = :userId")
  fun getFoldersFromUserId(userId: String): List<Folder>

  @Query(
      "SELECT * FROM folder WHERE parentFolderId IS NULL AND isDeckFolder = 0 AND userId = :userId")
  fun getRootNoteFoldersFromUserId(userId: String): List<Folder>

  @Query("SELECT * FROM folder WHERE id IN(:folderIds)")
  fun getFoldersByIds(folderIds: List<String>): List<Folder>

  @Query(
      "SELECT * FROM folder WHERE parentFolderId IS NULL AND isDeckFolder = 1 AND userId = :userId")
  fun getRootDeckFoldersFromUserId(userId: String): List<Folder>

  @Query("SELECT * FROM folder WHERE name = :folderName AND isDeckFolder = 1")
  fun getDeckFolderByName(folderName: String): List<Folder>

  @Query("SELECT * FROM folder WHERE parentFolderId = :parentFolderId")
  fun getSubfoldersOf(parentFolderId: String): List<Folder>

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addFolder(folder: Folder)

  @Insert(onConflict = OnConflictStrategy.REPLACE) fun addFolders(folders: List<Folder>)

  @Query("DELETE FROM folder WHERE id = :folderId") fun deleteFolderById(folderId: String)

  @Query("DELETE FROM folder WHERE id IN(:folderIds)")
  fun deleteFoldersByIds(folderIds: List<String>)

  @Query("DELETE FROM folder WHERE userId = :userId") fun deleteFoldersFromUid(userId: String)
}
