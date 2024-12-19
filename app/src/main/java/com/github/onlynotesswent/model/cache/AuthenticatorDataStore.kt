package com.github.onlynotesswent.model.cache

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "auth")

class AuthManager(private val context: Context) {
  companion object {
    private val USER_EMAIL = stringPreferencesKey("user_email")
  }

  // Get User Email
  val userEmailFlow: Flow<String?> =
      context.dataStore.data.map { preferences -> preferences[USER_EMAIL] }

  /** Save the user's email in the data store */
  suspend fun saveCredentials(userEmail: String) {
    context.dataStore.edit { preferences ->
      preferences[USER_EMAIL] = userEmail
    }
  }

  /** Delete saved credentials from the data store */
  suspend fun deleteCredentials() {
    context.dataStore.edit { preferences -> preferences.clear() }
  }
}
