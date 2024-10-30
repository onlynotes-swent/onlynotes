package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import com.google.firebase.Timestamp

data class Note(
    val id: String,
    val type: Type,
    val title: String,
    val content: String,
    val date: Timestamp,
    val public: Boolean,
    val noteClass: Class,
    val userId: String,
    val image: Bitmap
)

enum class Type {
  JPEG,
  PNG,
  PDF,
  NORMAL_TEXT
}

/**
 * Represents a class that a note belongs to.
 *
 * @param classCode The code of the class.
 * @param className The name of the class.
 * @param classYear The year of the class.
 * @param publicPath The public path of the class.
 */
data class Class(
    val classCode: String,
    val className: String,
    val classYear: Int,
    val publicPath: String
)
