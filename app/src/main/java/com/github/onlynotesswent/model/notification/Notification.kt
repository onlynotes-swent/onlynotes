package com.github.onlynotesswent.model.notification

import com.google.firebase.Timestamp

data class Notification(
    val id: String,
    val senderId: String?,
    val receiverId: String,
    val timestamp: Timestamp,
    val read: Boolean,
    val type: NotificationType = NotificationType.DEFAULT
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
}
