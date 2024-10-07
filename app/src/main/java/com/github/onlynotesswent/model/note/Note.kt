package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import java.util.Date

enum class Type {
    JPEG, PNG, NORMALTEXT
}

data class  Note(

    val id: String,
    val type: Type,
    val name: String,
    val title: String,
    val content: String,
    val date: Date,
    val userId: String,
    val image: Bitmap
)
