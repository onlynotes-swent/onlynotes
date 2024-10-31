package com.github.onlynotesswent.model.flashcard

import com.google.firebase.Timestamp

data class Flashcard(
    val id: String,
    val front: String, // The front side of the flashcard which contains the question.
    val back: String, // The back side of the flashcard which contains the answer.
    val nextReview: Timestamp,
    val userId: String,
    val folderId: String
)
