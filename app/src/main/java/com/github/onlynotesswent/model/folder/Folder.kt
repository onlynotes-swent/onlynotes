package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.model.common.Visibility

/**
 * Represents a folder that contains notes.
 *
 * @param id The ID of the folder.
 * @param name The name of the folder.
 * @param userId The ID of the user that owns the folder.
 * @param parentFolderId The ID of the parent folder. Has default value null.
 */
data class Folder(
    val id: String,
    val name: String,
    val userId: String,
    val parentFolderId: String? = null,
    val visibility: Visibility = Visibility.DEFAULT
)
