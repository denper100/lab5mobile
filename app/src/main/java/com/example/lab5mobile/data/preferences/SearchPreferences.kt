package com.example.lab5mobile.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore by preferencesDataStore(name = "flight_search_preferences")

class SearchPreferences(private val context: Context) {

    private val lastSearchKey: Preferences.Key<String> =
        stringPreferencesKey("last_successful_search")

    val lastSearchCode: Flow<String?> = context.dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences -> preferences[lastSearchKey] }

    suspend fun saveLastSearchCode(iataCode: String?) {
        context.dataStore.edit { preferences ->
            if (iataCode.isNullOrBlank()) {
                preferences.remove(lastSearchKey)
            } else {
                preferences[lastSearchKey] = iataCode
            }
        }
    }
}
