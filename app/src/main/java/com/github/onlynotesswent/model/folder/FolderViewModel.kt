package com.github.onlynotesswent.model.folder

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FolderViewModel(private val repository: FolderRepository) : ViewModel() {

    private val _publicFolders = MutableStateFlow<List<Folder>>(emptyList())
    val publicFolders: StateFlow<List<Folder>> = _publicFolders.asStateFlow()

    private val _userFolders = MutableStateFlow<List<Folder>>(emptyList())
    val userFolders: StateFlow<List<Folder>> = _userFolders.asStateFlow()

    private val _parentSubFolders = MutableStateFlow<List<Folder>>(emptyList())
    val parentSubFolders: StateFlow<List<Folder>> = _parentSubFolders.asStateFlow()

    private val _parentFolderId = MutableStateFlow<String?>(null)
    val parentFolderId: StateFlow<String?> = _parentFolderId.asStateFlow()

    private val _selectedFolder = MutableStateFlow<Folder?>(null)
    val selectedFolder: StateFlow<Folder?> = _selectedFolder.asStateFlow()

    init {
        repository.init { }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer { FolderViewModel(FolderRepositoryFirestore(Firebase.firestore)) }
        }
    }

    /**
     * Sets the parent folder ID.
     *
     * @param parentFolderId The parent folder ID.
     */
    fun selectedParentFolderId(parentFolderId: String?) {
        _parentFolderId.value = parentFolderId
    }

    /**
     * Sets the selected folder.
     *
     * @param folder The selected folder.
     */
    fun selectedFolder(folder: Folder) {
        _selectedFolder.value = folder
    }

    /**
     * Generates a new folder ID.
     *
     * @return The new folder ID.
     */
    fun getNewFolderId(): String {
        return repository.getNewFolderId()
    }

    /**
     * Adds a Folder to the repository.
     *
     * @param folder The folder to add.
     *
     */
    fun addFolder(folder: Folder, userId: String) {
        repository.addFolder(folder, onSuccess = {
            //getFoldersFrom(userId)
            getFoldersByParentFolderId(folder.parentFolderId!!)
                                                 }
            , onFailure = {})
    }

    /**
     * Deletes a folder by its ID.
     *
     * @param folderId The ID of the folder to delete.
     */
    fun deleteFolderById(folderId: String ,userId: String) {
        repository.deleteFolderById(folderId, onSuccess = { getFoldersFrom(userId) }, onFailure = {})
    }

    /**
     * Retrieves all folders owned by a user.
     *
     * @param userId The ID of the user to retrieve folders for.
     */
    fun getFoldersFrom(userId: String) {
        repository.getFoldersFrom(userId, onSuccess = { _userFolders.value = it }, onFailure = {})
    }

    /**
     * Retrieves a folder by its ID.
     *
     * @param folderId The ID of the folder to retrieve.
     */
    fun getFolderById(folderId: String) {
        repository.getFolderById(folderId, onSuccess = { _selectedFolder.value = it }, onFailure = {})
    }

    /**
     * Updates an existing folder.
     *
     * @param folder The folder with updated information.
     */
    fun updateFolder(folder: Folder, userId: String) {
        repository.updateFolder(folder, onSuccess = { getFoldersFrom(userId)}, onFailure = {})
    }

    /**
     * Retrieves all children folders of a parent folder.
     *
     * @param parentId The ID of the parent folder.
     */
    fun getFoldersByParentFolderId(parentId: String) {
        repository.getFoldersByParentFolderId(parentId, onSuccess = { _parentSubFolders.value = it }, onFailure = {})
    }
}