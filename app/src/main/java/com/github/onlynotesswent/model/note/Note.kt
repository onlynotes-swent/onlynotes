package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import com.google.firebase.Timestamp

data class Note(
    val id: String,
    val type: Type,
    val name: String,
    val title: String,
    val content: String,
    val date: Timestamp,
    val userId: String,
    val image: Bitmap
)

enum class Type {
  JPEG,
  PNG,
  PDF,
  NORMAL_TEXT
}
