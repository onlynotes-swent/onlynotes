package com.github.onlynotesswent.model.notification

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.google.firebase.Timestamp

data class Notification (
    val id: String,
    val title: String,
    val body: String,
    val senderId: String?,
    val receiverId: String,
    val timestamp: Timestamp,
    val read: Boolean,
    val type: Type = Type.DEFAULT
){
    enum class Type {
        FRIEND_REQUEST,
        FRIEND_REQUEST_ACCEPTED,
        FRIEND_REQUEST_REJECTED,
        CHAT_MESSAGE,
        COMMENT,
        LIKE,
        FOLLOW,
        POST,
        MENTION,
        SYSTEM,
        DEFAULT;
    }

}
