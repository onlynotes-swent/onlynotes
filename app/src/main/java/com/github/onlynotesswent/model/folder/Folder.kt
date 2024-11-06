package com.github.onlynotesswent.model.folder

/**
 * Represents a folder that contains notes.
 *
 * @param id The ID of the folder.
 * @param name The name of the folder.
 * @param userId The ID of the user that owns the folder.
 */
data class Folder(
    val id: String,
    val name: String,
    val userId: String,
    val parentFolderId: String? = null // if note not assigned to a folder, folderId is null
)
