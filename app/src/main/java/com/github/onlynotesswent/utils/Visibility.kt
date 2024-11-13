package com.github.onlynotesswent.utils

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
    }
  }
}
