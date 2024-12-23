package com.github.onlynotesswent.model.notification

import com.google.firebase.Timestamp

data class Notification(
    val id: String,
    val senderId: String?,
    val receiverId: String,
    val timestamp: Timestamp,
    val read: Boolean,
    val type: NotificationType = NotificationType.DEFAULT,
    val content: String? = null
) {

  enum class NotificationType {
    FOLLOW_REQUEST,
    FOLLOW_REQUEST_ACCEPTED,
    FOLLOW_REQUEST_REJECTED,
    CHAT_MESSAGE,
    COMMENT,
    LIKE,
    FOLLOW,
    POST,
    MENTION,
    SYSTEM,
    DEFAULT
  }

  companion object {
    private const val CHAT_MESSAGE_MAX_LENGTH = 140

    fun formatChatMessage(text: String): String {
      return text.trimStart().take(CHAT_MESSAGE_MAX_LENGTH)
    }
  }
}
