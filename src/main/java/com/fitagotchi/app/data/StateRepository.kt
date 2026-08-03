package com.fitagotchi.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.fitagotchi.app.model.AppState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore(name = "fitagotchi_state")
private val KEY_STATE = stringPreferencesKey("app_state_json")

class StateRepository(private val context: Context) {

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    val stateFlow: Flow<AppState?> = context.dataStore.data.map { prefs ->
        prefs[KEY_STATE]?.let {
            runCatching { json.decodeFromString<AppState>(it) }.getOrNull()
        }
    }

    suspend fun save(state: AppState) {
        context.dataStore.edit { it[KEY_STATE] = json.encodeToString(AppState.serializer(), state) }
    }

    suspend fun clear() {
        context.dataStore.edit { it.clear() }
    }
}
