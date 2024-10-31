package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import com.google.firebase.Timestamp

data class Note(
    val id: String,
    val type: Type,
    val title: String,
    val content: String,
    val date: Timestamp,
    val visibility: Visibility,
    val userId: String,
    val image: Bitmap
) {
  enum class Visibility {
    PUBLIC,
    FRIENDS,
    PRIVATE;

    companion object {
      val DEFAULT = PUBLIC
      val READABLE_STRINGS = Visibility.values().map { it.toReadableString() }

      fun fromReadableString(readableString: String): Visibility {
        return values().find { it.toReadableString() == readableString }
            ?: throw IllegalArgumentException("Invalid visibility string")
      }

      fun fromString(string: String): Visibility {
        return values().find { it.toString() == string }
            ?: throw IllegalArgumentException("Invalid visibility string")
      }
    }

    fun toReadableString(): String {
      return when (this) {
        PUBLIC -> "Public"
        FRIENDS -> "Friends Only"
        PRIVATE -> "Private"
        else -> "$this (not implemented)" // keep for maintainability
      }
    }
  }

  enum class Type {
    JPEG,
    PNG,
    PDF,
    NORMAL_TEXT
  }
}
