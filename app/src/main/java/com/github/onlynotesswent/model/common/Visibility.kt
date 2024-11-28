package com.github.onlynotesswent.model.common

enum class Visibility {
  PUBLIC,
  FRIENDS,
  PRIVATE;

  companion object {
    val DEFAULT = PRIVATE
    val READABLE_STRINGS = entries.map { it.toReadableString() }

    fun fromReadableString(readableString: String): Visibility {
      return entries.find { it.toReadableString() == readableString }
          ?: throw IllegalArgumentException("Invalid visibility string")
    }

    fun fromString(string: String): Visibility {
      return entries.find { it.toString() == string }
          ?: throw IllegalArgumentException("Invalid visibility string")
    }
  }

  fun toReadableString(): String {
    return when (this) {
      PUBLIC -> "Public"
      FRIENDS -> "Friends Only"
      PRIVATE -> "Private"
    }
  }
}
