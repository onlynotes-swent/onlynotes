package com.github.onlynotesswent.model.cache

import androidx.room.TypeConverter
import com.github.onlynotesswent.model.note.Note
import com.google.firebase.Timestamp
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/** Converts a [Timestamp] object to a [Long] for storage in the database. */
class TimestampConverter {
  @TypeConverter
  fun fromTimestamp(value: Long?): Timestamp? {
    return value?.let { Timestamp(it, 0) }
  }

  @TypeConverter
  fun timestampToLong(timestamp: Timestamp?): Long? {
    return timestamp?.seconds
  }
}

/** Converts a [Note.CommentCollection] object to a [String] for storage in the database. */
class CommentCollectionConverter {
  private val gson = Gson()

  @TypeConverter
  fun fromCommentCollection(comments: Note.CommentCollection?): String? {
    return comments?.let { gson.toJson(it) }
  }

  @TypeConverter
  fun toCommentCollection(commentsString: String?): Note.CommentCollection {
    return commentsString?.let {
      gson.fromJson(it, object : TypeToken<Note.CommentCollection>() {}.type)
    } ?: Note.CommentCollection()
  }
}
