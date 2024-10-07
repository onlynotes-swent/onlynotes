package com.github.onlynotesswent.model.note

import android.graphics.Bitmap
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore

class ImplementationNoteRepository(private val db: FirebaseFirestore) : NoteRepository {

    private val collectionPath = "notes/"

    override fun getNewUid(): String {
        return db.collection(collectionPath).document().id
    }

    override fun init(onSuccess: () -> Unit) {
        Firebase.auth.addAuthStateListener {
            if (it.currentUser != null) {
                onSuccess()
            }
        }
    }

    override fun getNotes(userId:String , onSuccess: (List<Note>) -> Unit, onFailure: (Exception) -> Unit) {
        Log.d("ImplementationNoteRepository", "getNotes")
        db.collection(collectionPath).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val notes = task.result?.mapNotNull { document -> documentToNote(document) } ?: emptyList()
                val userNotes=notes.filter { it.userId==userId }
                onSuccess(userNotes)
            } else {
                task.exception?.let { e ->
                    Log.e("ImplementationNoteRepository", "Error getting documents", e)
                    onFailure(e)
                }
            }
        }
    }

    override fun getNoteById(id: String, onSuccess: (Note) -> Unit, onFailure: (Exception) -> Unit) {
        val notes =db.collection(collectionPath).document(id).get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val note = task.result?.let { documentToNote(it) }
                if (note != null) {
                    onSuccess(note)
                } else {
                    onFailure(Exception("Note not found"))
                }
            } else {
                task.exception?.let { e ->
                    Log.e("ImplementationNoteRepository", "Error getting document", e)
                    onFailure(e)
                }
            }
        }
    }

    override fun insertNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        performFirestoreOperation(db.collection(collectionPath).document(note.id).set(note), onSuccess, onFailure)
    }

    override fun updateNote(note: Note, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        performFirestoreOperation(db.collection(collectionPath).document(note.id).set(note), onSuccess, onFailure)
    }

    override fun deleteNoteById(id: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        performFirestoreOperation(db.collection(collectionPath).document(id).delete(), onSuccess, onFailure) }



    private fun performFirestoreOperation(
        task: Task<Void>,
        onSuccess: () -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        task.addOnCompleteListener { result ->
            if (result.isSuccessful) {
                onSuccess()
            } else {
                result.exception?.let { e ->
                    Log.e("TodosRepositoryFirestore", "Error performing Firestore operation", e)
                    onFailure(e)
                }
            }
        }
      }




    private fun documentToNote(document: DocumentSnapshot): Note? {
        return try {
            val id = document.id
            val type = Type.valueOf(document.getString("type") ?: return null)
            val name = document.getString("name") ?: return null
            val title = document.getString("title") ?: return null
            val content = document.getString("content") ?: return null
            val date = document.getDate("date") ?: return null
            val user = document.getString("user") ?: return null
            val image = document.get("image") as? Bitmap ?: return null

            Note(
                id = id,
                type = type,
                name = name,
                title = title,
                content = content,
                date = date,
                userId = user,
                image = image
            )
        } catch (e: Exception) {
            Log.e("NotesRepositoryFirestore", "Error converting document to Note", e)
            null
        }
   }
}


