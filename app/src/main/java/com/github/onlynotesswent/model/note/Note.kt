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
    val userId: String,
    val image: Bitmap
)

enum class Type {
  JPEG,
  PNG,
  PDF,
  NORMAL_TEXT
}
