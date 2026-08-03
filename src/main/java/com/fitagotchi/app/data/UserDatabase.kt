package com.fitagotchi.app.data

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.fitagotchi.app.model.AppState
import kotlinx.serialization.json.Json

class UserDatabase(context: Context) :
    SQLiteOpenHelper(context, DB_NAME, null, 1) {

    companion object { const val DB_NAME = "fitagotchi_users.db" }

    private val json = Json { ignoreUnknownKeys = true; encodeDefaults = true }

    init { importPrepackagedIfPresent(context) }

    private fun importPrepackagedIfPresent(ctx: Context) {
        val dbFile = ctx.getDatabasePath(DB_NAME)
        if (dbFile.exists()) return // device already has data - never overwrite
        for (name in listOf(DB_NAME, "$DB_NAME-wal", "$DB_NAME-shm")) {
            try {
                ctx.assets.open(name).use { input ->
                    val out = java.io.File(dbFile.parentFile, name)
                    out.parentFile?.mkdirs()
                    out.outputStream().use { input.copyTo(it) }
                }
            } catch (e: java.io.IOException) {
                // that asset isn't packaged - fine (wal/shm are optional,
                // and no main file at all just means: fresh DB + seed)
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """CREATE TABLE users(
                username TEXT PRIMARY KEY,
                password_hash TEXT NOT NULL,
                state_json TEXT NOT NULL,
                updated_at INTEGER NOT NULL
            )"""
        )
        seedDemoAccount(db)
    }

    private fun sha256(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    private fun seedDemoAccount(db: SQLiteDatabase) {
        val today = java.time.LocalDate.now()
        val demoState = AppState(
            onboarded = true,
            username = "demo",
            profile = com.fitagotchi.app.model.Profile(
                heightCm = 172.0, weightKg = 68.0, birthYear = 2003
            ),
            pet = com.fitagotchi.app.model.PetState(
                type = com.fitagotchi.app.model.PetType.DOG,
                name = "Sparky", hunger = 80, xp = 40, level = 5, evolved = true
            ),
            coins = 3200,
            streak = 3,
            workoutHistory = listOf(
                today.minusDays(2).toString(),
                today.minusDays(1).toString(),
                today.toString()
            ),
            backpack = mapOf("APPLE" to 2, "RAMEN" to 1),
            ownedHabitats = setOf(com.fitagotchi.app.model.HabitatId.BEACH),
            sessionLog = listOf(
                com.fitagotchi.app.model.SessionRecord(today.minusDays(2).toString(), 11.5, 74.0, 7),
                com.fitagotchi.app.model.SessionRecord(today.minusDays(1).toString(), 9.0, 61.0, 6),
                com.fitagotchi.app.model.SessionRecord(today.toString(), 12.5, 82.0, 8)
            ),
            lastActiveEpochDay = today.toEpochDay()
        )
        val v = ContentValues().apply {
            put("username", "demo")
            put("password_hash", sha256("1234"))
            put("state_json", json.encodeToString(AppState.serializer(), demoState))
            put("updated_at", System.currentTimeMillis())
        }
        db.insert("users", null, v)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldV: Int, newV: Int) {

    }

    /** Is this username taken? */
    fun exists(username: String): Boolean =
        readableDatabase.rawQuery(
            "SELECT 1 FROM users WHERE username=?", arrayOf(username)
        ).use { it.moveToFirst() }

    /** Create the account, seeding it with the player's CURRENT state -
     *  this is how guest progress becomes account progress. */
    fun register(username: String, passwordHash: String, state: AppState): Boolean {
        if (exists(username)) return false
        val v = ContentValues().apply {
            put("username", username)
            put("password_hash", passwordHash)
            put("state_json", json.encodeToString(AppState.serializer(), state))
            put("updated_at", System.currentTimeMillis())
        }
        return writableDatabase.insert("users", null, v) != -1L
    }

    /** SHA-256 hash for the username, or null if no such account. */
    fun credentials(username: String): String? =
        readableDatabase.rawQuery(
            "SELECT password_hash FROM users WHERE username=?", arrayOf(username)
        ).use { if (it.moveToFirst()) it.getString(0) else null }

    /** Load the account's full game state. */
    fun loadState(username: String): AppState? =
        readableDatabase.rawQuery(
            "SELECT state_json FROM users WHERE username=?", arrayOf(username)
        ).use { c ->
            if (!c.moveToFirst()) null
            else runCatching { json.decodeFromString<AppState>(c.getString(0)) }.getOrNull()
        }

    /** Write-through save of the account's game state. */
    fun saveState(username: String, state: AppState) {
        val v = ContentValues().apply {
            put("state_json", json.encodeToString(AppState.serializer(), state))
            put("updated_at", System.currentTimeMillis())
        }
        writableDatabase.update("users", v, "username=?", arrayOf(username))
    }

    /** Rename an account. Fails if the new name is taken. */
    fun renameUser(oldName: String, newName: String): Boolean {
        if (exists(newName)) return false
        val v = ContentValues().apply { put("username", newName) }
        return writableDatabase.update("users", v, "username=?", arrayOf(oldName)) > 0
    }

    /** Replace the stored password hash (verified in the ViewModel). */
    fun updatePassword(username: String, newHash: String) {
        val v = ContentValues().apply { put("password_hash", newHash) }
        writableDatabase.update("users", v, "username=?", arrayOf(username))
    }

    /** Delete the account row (password-verified Wipe Data). */
    fun delete(username: String) {
        writableDatabase.delete("users", "username=?", arrayOf(username))
    }
}
