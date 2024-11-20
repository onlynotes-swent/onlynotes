package com.github.onlynotesswent.model.folder

import com.github.onlynotesswent.utils.Visibility

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
) {
    companion object {
        // folder name max length
        private const val FOLDER_NAME_MAX_LENGTH = 28

        /**
         * Formats the folder name by trimming leading whitespace and truncating it to the maximum
         * allowed length.
         *
         * @param name The folder name to format.
         * @return The formatted folder name.
         */
        fun formatName(name: String): String {
            return name.trimStart().take(FOLDER_NAME_MAX_LENGTH)
        }
    }
}
