package com.fitagotchi.app.vm

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.fitagotchi.app.data.StateRepository
import com.fitagotchi.app.data.UserDatabase
import com.fitagotchi.app.engine.WorkoutEngine
import com.fitagotchi.app.model.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import kotlin.math.roundToInt

/* ------------------------- SCREEN GRAPH ------------------------- */
enum class Screen {
    SPLASH, OB_GENDER, OB_GOAL, OB_SHAPE, OB_FOCUS, OB_YEAR, OB_HEIGHT, OB_WEIGHT, OB_BMI, OB_EGG,
    HATCHING,
    HUB, SETTINGS, ME, PROFILE, LOGIN, REGISTER,
    WORKOUT_ACTIVE, REST_TIMER, WORKOUT_REVIEW, REWARDS, EVOLUTION,
    RESPAWN_EGG // logged-in pet died: pick a replacement (keeps coins/inventory)
}

class AppViewModel(app: Application) : AndroidViewModel(app) {

    private val repo = StateRepository(app)
    private val userDb = UserDatabase(app)

    var state by mutableStateOf(AppState())
        private set
    var screen by mutableStateOf(Screen.SPLASH)
        private set
    var loaded by mutableStateOf(false)
        private set

    // Onboarding scratch (committed into Profile as user advances)
    var draft by mutableStateOf(Profile())
    var draftPet by mutableStateOf(PetType.DOG)

    // Live workout session
    var session by mutableStateOf<List<WorkoutItem>>(emptyList()); private set
    var sessionIndex by mutableStateOf(0); private set
    var sessionCompleted by mutableStateOf(0); private set
    var lastCoinReward by mutableStateOf(0); private set
    var justEvolved by mutableStateOf(false); private set


    data class DeathNotice(val petName: String, val daysGone: Int)
    var deathNotice by mutableStateOf<DeathNotice?>(null)
        private set

    init {
        viewModelScope.launch {
            val saved = repo.stateFlow.first()
            if (saved != null) {
                val survivor = applyStarvation(saved)
                if (survivor != null) {
                    state = survivor
                    if (survivor.onboarded) screen = Screen.HUB
                }

            }
            loaded = true
        }
    }

    /** Returns the decayed state, or null if the pet starved to death. */
    private suspend fun applyStarvation(saved: AppState): AppState? {
        if (!saved.onboarded) return saved
        // Offset-aware: demo day-skips count as real elapsed days here too.
        val today = LocalDate.now().plusDays(saved.dateOffsetDays).toEpochDay()
        val last = if (saved.lastActiveEpochDay == 0L) today else saved.lastActiveEpochDay
        val daysGone = (today - last).toInt().coerceAtLeast(0)
        if (daysGone == 0) return saved

        // HUNGER DECAY MATH: hunger -= days * 25 (full 100 -> dead in 4 days)
        val newHunger = saved.pet.hunger - daysGone * HUNGER_DECAY_PER_DAY
        return if (newHunger <= 0) {
            // DEATH. Logged-in accounts DON'T wipe - the player keeps their
            // coins/inventory and respawns a new pet (see handleDeath()).
            // Guests fall back to the old permadeath -> onboarding wipe.
            if (saved.loggedIn && saved.username != null) {
                handleDeath(saved); return null
            }
            deathNotice = DeathNotice(saved.pet.name, daysGone)
            repo.clear()
            null
        } else {
            val decayed = saved.copy(
                pet = saved.pet.copy(hunger = newHunger),
                lastActiveEpochDay = today
            )
            repo.save(decayed)
            decayed
        }
    }

    fun dismissDeathNotice() { deathNotice = null }


    var respawnNotice by mutableStateOf<DeathNotice?>(null)
        private set

    /** Called for a logged-in death. Sets up the respawn flow WITHOUT
     *  wiping the account, and persists the "pet is dead" interim state. */
    private fun handleDeath(saved: AppState) {
        val deadType = saved.pet.type
        val survivors = saved.ownedPets - deadType   // other pets you own
        respawnNotice = DeathNotice(saved.pet.name, 0)
        // Park the account in a dead-but-alive state: keep coins/inventory,
        // drop the dead pet from ownership, route to the respawn screen.
        state = saved.copy(
            ownedPets = survivors,
            pet = saved.pet.copy(hunger = 0) // marker; replaced on respawn
        )
        // Persist so a mid-respawn app close doesn't resurrect the dead pet.
        val u = saved.username
        viewModelScope.launch {
            repo.save(state)
            if (u != null) userDb.saveState(u, state)
        }
        screen = Screen.RESPAWN_EGG
    }

    /** Pets available to respawn as. If you own others, that's the list;
     *  otherwise it's the 4 starters for a fresh hatch. */
    fun respawnChoices(): List<PetType> {
        val owned = state.ownedPets.toList()
        return if (owned.isNotEmpty()) owned
        else PetType.entries.filter { it != PetType.DRAGON }
    }

    /** Complete the respawn. The dead pet's progress is discarded. A
     *  respawned-into surviving pet restores its OWN saved level/xp; a
     *  freshly hatched starter begins at level 1. Coins/inventory kept. */
    fun respawnAs(type: PetType, name: String) {
        val deadType = state.pet.type
        val bank = state.petProgress.toMutableMap()
        bank.remove(deadType.name)     // dead pet's progress gone for good
        val saved = bank[type.name]
        bank.remove(type.name)         // chosen pet becomes active
        val remaining = state.ownedPets - type
        val newPet = if (saved != null)
            PetState(type = type, name = name.ifBlank { saved.name },
                hunger = 88, xp = saved.xp, level = saved.level, evolved = saved.evolved)
        else
            PetState(type = type, name = name.ifBlank { "Sparky" },
                hunger = 88, xp = 0, level = 1, evolved = false)
        commit(state.copy(
            ownedPets = remaining,
            pet = newPet,
            petProgress = bank
        ))
        respawnNotice = null
        screen = Screen.HUB
    }



    /** The app's notion of "today" = real date + demo offset. */
    fun today(): LocalDate = LocalDate.now().plusDays(state.dateOffsetDays)

    /** Gift coins (presentation only). */
    fun giftCoins(amount: Int) {
        if (amount > 0) commit(state.copy(coins = state.coins + amount))
    }

    fun setXpPerExercise(v: Int) {
        if (v in 1..1000) commit(state.copy(xpPerExercise = v))
    }

    /** Configure LIVE hunger decay: `amount` points per tick, 0 = off. */
    fun setDemoDecay(amount: Int? = null, perMinute: Boolean? = null) {
        commit(state.copy(
            demoDecayAmount = (amount ?: state.demoDecayAmount).coerceIn(0, 100),
            demoDecayPerMinute = perMinute ?: state.demoDecayPerMinute
        ))
    }

    /** Shared death path. Logged in -> respawn flow (keep coins/inventory).
     *  Guest -> permadeath wipe back to onboarding. */
    private fun petDies(daysGone: Int) {
        if (state.loggedIn && state.username != null) {
            handleDeath(state); return
        }
        deathNotice = DeathNotice(state.pet.name, daysGone)
        viewModelScope.launch { repo.clear() }
        state = AppState()
        draft = Profile()
        screen = Screen.SPLASH
    }


    fun skipDay() {
        if (!state.onboarded) return
        val newHunger = state.pet.hunger - HUNGER_DECAY_PER_DAY
        if (newHunger <= 0) { petDies(1); return }
        commit(state.copy(
            dateOffsetDays = state.dateOffsetDays + 1,
            pet = state.pet.copy(hunger = newHunger)
        ))
    }

    /** Reset the date offset back to the real clock. */
    fun resetDateOffset() { commit(state.copy(dateOffsetDays = 0)) }


    private var tickSeconds = 0
    init {
        viewModelScope.launch {
            while (true) {
                kotlinx.coroutines.delay(1000)
                tickSeconds++
                val amt = state.demoDecayAmount
                if (amt > 0 && state.onboarded && loaded) {
                    val fire = if (state.demoDecayPerMinute) tickSeconds % 60 == 0 else true
                    if (fire) {
                        val newHunger = state.pet.hunger - amt
                        if (newHunger <= 0) petDies(0)
                        else commit(state.copy(pet = state.pet.copy(hunger = newHunger)))
                    }
                }
            }
        }
    }

    private fun commit(newState: AppState) {

        val stamped = newState.copy(
            lastActiveEpochDay = LocalDate.now().plusDays(newState.dateOffsetDays).toEpochDay()
        )
        state = stamped
        viewModelScope.launch {
            repo.save(stamped)

            val u = stamped.username
            if (stamped.loggedIn && u != null) userDb.saveState(u, stamped)
        }
    }

    fun navigate(to: Screen) { screen = to }

    /* -------------------- UNIT CONVERSION MATH --------------------
     * Height:  1 ft = 30.48 cm      ->  ft = cm / 30.48
     *          (displayed as decimal feet, e.g. 180cm -> 5.9ft)
     * Weight:  1 lb = 0.45359237 kg ->  lb = kg / 0.45359237
     * Values are ALWAYS stored metric (cm / kg); toggles only convert
     * the display + ruler scale, never the stored truth.
     * -------------------------------------------------------------- */
    companion object {
        /** Hunger lost per real day away (see STARVATION / DEATH SYSTEM). */
        const val HUNGER_DECAY_PER_DAY = 25
        const val CM_PER_FT = 30.48
        const val KG_PER_LB = 0.45359237
        fun cmToFt(cm: Double) = cm / CM_PER_FT
        fun ftToCm(ft: Double) = ft * CM_PER_FT
        fun kgToLb(kg: Double) = kg / KG_PER_LB
        fun lbToKg(lb: Double) = lb * KG_PER_LB
    }

    /* ------------------------ ONBOARDING ------------------------ */
    fun finishOnboarding(petName: String) {
        commit(
            state.copy(
                onboarded = true,
                profile = draft,
                pet = PetState(type = draftPet, name = petName.ifBlank { "Sparky" })
            )
        )
        screen = Screen.HUB
    }

    /* --------------------------- PET ---------------------------- */
    fun renamePet(name: String) {
        if (name.isNotBlank()) commit(state.copy(pet = state.pet.copy(name = name.trim())))
    }

    fun feedPet(food: FoodId) {
        val count = state.backpack[food.name] ?: 0
        if (count <= 0) return
        val pet = state.pet
        commit(
            state.copy(
                backpack = state.backpack.toMutableMap().apply {
                    if (count == 1) remove(food.name) else put(food.name, count - 1)
                },
                pet = pet.copy(hunger = (pet.hunger + food.health).coerceAtMost(100))
            )
        )
    }

    /* --------------------------- SHOP --------------------------- */
    fun buyFood(food: FoodId): Boolean {
        if (state.coins < food.price) return false
        commit(
            state.copy(
                coins = state.coins - food.price,
                backpack = state.backpack.toMutableMap()
                    .apply { put(food.name, (get(food.name) ?: 0) + 1) }
            )
        )
        return true
    }

    fun buyHabitat(h: HabitatId): Boolean {
        if (h in state.ownedHabitats) return true
        if (state.coins < h.price) return false
        commit(state.copy(coins = state.coins - h.price, ownedHabitats = state.ownedHabitats + h))
        return true
    }

    /** Buy-to-Equip: equipping swaps dashboard background AND pet sprite. */
    fun equipHabitat(h: HabitatId?) {
        if (h == null || h in state.ownedHabitats) commit(state.copy(equippedHabitat = h))
    }

    /* ------------------------- WORKOUT -------------------------- */
    fun startWorkout() {
        session = WorkoutEngine.generate(state.profile, state.intensityMultiplier)
        sessionIndex = 0
        sessionCompleted = 0
        screen = Screen.WORKOUT_ACTIVE
    }

    fun currentItem(): WorkoutItem? = session.getOrNull(sessionIndex)

    /** DONE pressed -> count it, then rest or review. FAIL skips the count. */
    fun completeExercise(succeeded: Boolean) {
        if (succeeded) sessionCompleted++
        if (sessionIndex >= session.size - 1) {
            screen = Screen.WORKOUT_REVIEW
        } else {
            sessionIndex++
            screen = Screen.REST_TIMER
        }
    }

    fun restFinished() { screen = Screen.WORKOUT_ACTIVE }

    /** Review submitted -> adjust intensity, pay coins/XP, log calendar day. */
    fun submitFeedback(feedback: Feedback) {
        val todayDate = today() // offset-aware "today" (see Demo Tools)
        val todayStr = todayDate.toString()
        val alreadyWorkedOutToday = todayStr in state.workoutHistory
        val history = if (alreadyWorkedOutToday) state.workoutHistory
        else state.workoutHistory + todayStr

        /* STREAK MATH: only the FIRST completed session of a
         * day counts toward the streak. Extra sessions the same day keep
         * the streak as-is (previously each one incremented it).
         *   - already logged today  -> streak unchanged
         *   - yesterday in history  -> streak + 1 (consecutive day)
         *   - otherwise             -> reset to 1 (chain broken)       */
        val yesterday = todayDate.minusDays(1).toString()
        val newStreak = when {
            alreadyWorkedOutToday -> state.streak
            yesterday in state.workoutHistory -> state.streak + 1
            else -> 1
        }

        /* COMPLETION PENALTY MATH:
         * Every FAILed exercise shrinks the payout proportionally:
         *   reward = round(baseReward * completed / total)
         * e.g. 7 exercises, 2 failed -> 5/7 of the streak-scaled base.
         * Failing everything pays 0 coins. */
        val baseReward = WorkoutEngine.coinReward(newStreak)
        val completionRatio =
            if (session.isNotEmpty()) sessionCompleted.toDouble() / session.size else 1.0
        lastCoinReward = (baseReward * completionRatio).roundToInt()
        // XP per exercise is demo-tunable (Settings > Demo Tools).
        val xpGain = sessionCompleted * state.xpPerExercise
        var pet = state.pet
        var xp = pet.xp + xpGain
        var level = pet.level
        // level-up every Tuning.XP_PER_LEVEL xp (see model/Models.kt -> Tuning)
        while (xp >= Tuning.XP_PER_LEVEL) { xp -= Tuning.XP_PER_LEVEL; level++ }
        val evolveNow = !pet.evolved && level >= Tuning.EVOLVE_AT_LEVEL
        pet = pet.copy(xp = xp, level = level, evolved = pet.evolved || evolveNow)
        justEvolved = evolveNow


        var activeSec = 0.0; var kcal = 0.0
        session.forEachIndexed { idx, item ->
            // count an item if it was completed (approximation: the first
            // sessionCompleted items in completion order)
            if (idx < sessionCompleted) {
                activeSec += CalorieEngine.seconds(item)
                kcal += CalorieEngine.kcal(item, state.profile.weightKg)
            }
        }
        val record = SessionRecord(
            date = todayStr, minutes = activeSec / 60.0,
            kcal = kcal, exercisesDone = sessionCompleted
        )

        commit(
            state.copy(
                intensityMultiplier = WorkoutEngine.adjustIntensity(state.intensityMultiplier, feedback),
                coins = state.coins + lastCoinReward,
                workoutHistory = history,
                streak = newStreak,
                pet = pet,
                sessionLog = state.sessionLog + record
            )
        )
        screen = Screen.REWARDS
    }

    fun rewardsAcknowledged() {
        screen = if (justEvolved) Screen.EVOLUTION else Screen.HUB
    }

    fun evolutionAcknowledged() {
        justEvolved = false
        screen = Screen.HUB
    }


    data class GachaPull(
        val rarity: GachaRarity,
        val label: String,
        val asset: String,
        val desc: String,
        val isDuplicate: Boolean = false
    )

    var lastGachaResults by mutableStateOf<List<GachaPull>>(emptyList())
        private set

    /** All pets the player owns (active pet is implicitly owned). */
    fun ownedPets(): Set<PetType> = state.ownedPets + state.pet.type

    /** Roll one rarity tier. forceUncommonPlus = 10-pull pity guarantee. */
    private fun rollRarity(forceUncommonPlus: Boolean): GachaRarity {
        while (true) {
            val r = Math.random()
            val rarity = when {
                r < Tuning.GACHA_EPIC_RATE -> GachaRarity.EPIC
                r < Tuning.GACHA_EPIC_RATE + Tuning.GACHA_RARE_RATE -> GachaRarity.RARE
                r < Tuning.GACHA_EPIC_RATE + Tuning.GACHA_RARE_RATE +
                        Tuning.GACHA_UNCOMMON_RATE -> GachaRarity.UNCOMMON
                else -> GachaRarity.COMMON
            }
            if (!forceUncommonPlus || rarity != GachaRarity.COMMON) return rarity
            // pity active and we rolled COMMON -> reroll until better
        }
    }

    /** Spend coins, roll `count` pulls, apply every prize, save once. */
    fun gachaPull(count: Int): Boolean {
        val cost = if (count >= 10) Tuning.GACHA_TEN_COST else Tuning.GACHA_SINGLE_COST
        if (state.coins < cost) return false

        var coins = state.coins - cost
        var backpack = state.backpack.toMutableMap()
        var habitats = state.ownedHabitats
        var pets = state.ownedPets
        val results = mutableListOf<GachaPull>()
        var gotUncommonPlus = false

        repeat(count) { i ->
            val pity = count >= 10 && i == count - 1 && !gotUncommonPlus
            val rarity = rollRarity(pity)
            if (rarity != GachaRarity.COMMON) gotUncommonPlus = true

            when (rarity) {
                GachaRarity.COMMON -> {
                    val food = FoodId.entries.random()
                    backpack[food.name] = (backpack[food.name] ?: 0) + 1
                    results += GachaPull(rarity, food.label, food.asset,
                        "Consumable - ${food.desc} Added to your backpack.")
                }
                GachaRarity.UNCOMMON -> {
                    val hab = HabitatId.entries.random()
                    if (hab in habitats) {
                        coins += Tuning.GACHA_DUP_HABITAT_COINS
                        results += GachaPull(rarity, hab.label, hab.asset,
                            "Duplicate wallpaper -> refunded ${Tuning.GACHA_DUP_HABITAT_COINS} G.", true)
                    } else {
                        habitats = habitats + hab
                        results += GachaPull(rarity, hab.label, hab.asset,
                            "Wallpaper unlocked! Equip it from the Shop tab.")
                    }
                }
                GachaRarity.RARE -> {
                    // one of the 4 starters - a second chance at egg choice
                    val pool = PetType.entries.filter {
                        it != PetType.DRAGON && it !in (pets + state.pet.type)
                    }
                    if (pool.isEmpty()) {
                        coins += Tuning.GACHA_DUP_PET_COINS
                        results += GachaPull(rarity, "Pet Token", "egg-dog.png",
                            "All starter pets owned -> refunded ${Tuning.GACHA_DUP_PET_COINS} G.", true)
                    } else {
                        val won = pool.random()
                        pets = pets + won
                        results += GachaPull(rarity, won.label, Assets.egg(won),
                            "New companion unlocked! Switch pets at the bottom of the Gacha page.")
                    }
                }
                GachaRarity.EPIC -> {
                    if (PetType.DRAGON in (pets + state.pet.type)) {
                        coins += Tuning.GACHA_DUP_DRAGON_COINS
                        results += GachaPull(rarity, "Dragon", "dragon.png",
                            "Duplicate dragon -> refunded ${Tuning.GACHA_DUP_DRAGON_COINS} G.", true)
                    } else {
                        pets = pets + PetType.DRAGON
                        results += GachaPull(rarity, "Dragon", "dragon.png",
                            "MYTHIC! The Dragon joins your roster. Switch pets below.")
                    }
                }
            }
        }

        lastGachaResults = results
        commit(state.copy(
            coins = coins, backpack = backpack,
            ownedHabitats = habitats, ownedPets = pets
        ))
        return true
    }

    fun dismissGachaResults() { lastGachaResults = emptyList() }


    fun switchPet(type: PetType) {
        if (type !in ownedPets() || type == state.pet.type) return
        val cur = state.pet
        // Bank the outgoing pet's progress under its own type key.
        val banked = state.petProgress.toMutableMap().apply {
            put(cur.type.name, PetProgress(cur.name, cur.xp, cur.level, cur.evolved))
        }
        // Restore the incoming pet's saved progress, or start fresh.
        val saved = banked[type.name]
        banked.remove(type.name) // it's the active pet now, not in the bank
        val newPet = if (saved != null)
            PetState(type = type, name = saved.name, hunger = cur.hunger,
                xp = saved.xp, level = saved.level, evolved = saved.evolved)
        else
            PetState(type = type, name = type.label, hunger = cur.hunger,
                xp = 0, level = 1, evolved = false)
        commit(state.copy(
            ownedPets = ownedPets() - type,   // old active pet joins the pool
            pet = newPet,
            petProgress = banked
        ))
    }


    var authError by mutableStateOf<String?>(null); private set

    private fun sha256(s: String): String =
        java.security.MessageDigest.getInstance("SHA-256")
            .digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    fun register(username: String, pass: String, confirm: String) {
        val u = username.trim()
        authError = when {
            u.isBlank() || u.length < 3 -> "Username must be 3+ characters"
            pass.length < 4 -> "Password must be 4+ characters"
            pass != confirm -> "Passwords do not match"
            userDb.exists(u) -> "Username already taken"
            else -> null
        }
        if (authError != null) return

        val newState = state.copy(username = u, loggedIn = true, passwordHash = null)
        if (!userDb.register(u, sha256(pass), newState)) {
            authError = "Could not create account"; return
        }
        commit(newState)
        screen = Screen.ME
    }

    fun login(username: String, pass: String) {
        val u = username.trim()
        val storedHash = userDb.credentials(u)
        authError = when {
            storedHash == null -> "Unknown username"
            sha256(pass) != storedHash -> "Wrong password"
            else -> null
        }
        if (authError != null) return

        val loaded = userDb.loadState(u)
        if (loaded == null) { authError = "Account data corrupted"; return }
        state = loaded.copy(username = u, loggedIn = true)
        commit(state)
        screen = Screen.ME
    }

    /** Logout: final save to the DB, then reset to a fresh guest session.
     *  Log back in any time to restore everything from SQLite. */
    fun logout() {
        val u = state.username
        if (u != null) userDb.saveState(u, state.copy(loggedIn = false))
        viewModelScope.launch { repo.clear() }
        state = AppState()
        draft = Profile()
        screen = Screen.SPLASH
    }

    /** Change the account username. Returns an error message or null. */
    fun changeUsername(newName: String): String? {
        val u = state.username ?: return "Not logged in"
        val n = newName.trim()
        if (n.length < 3) return "Username must be 3+ characters"
        if (n == u) return "That is already your username"
        if (!userDb.renameUser(u, n)) return "Username already taken"
        commit(state.copy(username = n)) // write-through re-saves state under the new name
        return null
    }

    /** Change password: verifies the CURRENT password against the DB
     *  hash, requires the new password entered twice. Null = success. */
    fun changePassword(current: String, newPass: String, confirm: String): String? {
        val u = state.username ?: return "Not logged in"
        val hash = userDb.credentials(u) ?: return "Account not found"
        if (sha256(current) != hash) return "Current password is wrong"
        if (newPass.length < 4) return "New password must be 4+ characters"
        if (newPass != confirm) return "New passwords do not match"
        userDb.updatePassword(u, sha256(newPass))
        return null
    }

    /** Password-verified wipe. Logged in: checks the password against the
     *  DB hash, deletes the account row, then resets the device session.
     *  Returns false (and leaves everything intact) on a wrong password. */
    fun wipeAccount(pass: String): Boolean {
        val u = state.username
        if (state.loggedIn && u != null) {
            val hash = userDb.credentials(u) ?: return false
            if (sha256(pass) != hash) return false
            userDb.delete(u)
        }
        resetAll()
        return true
    }

    fun clearAuthError() { authError = null }
    fun setAvatar(id: String) { commit(state.copy(avatarId = id)) }

    fun updateProfileInfo(gender: Gender? = null, birthYear: Int? = null) {
        commit(state.copy(profile = state.profile.copy(
            gender = gender ?: state.profile.gender,
            birthYear = birthYear ?: state.profile.birthYear
        )))
    }

    /* ------------------------- SETTINGS ------------------------- */
    fun updateHealth(heightCm: Double? = null, weightKg: Double? = null) {
        commit(
            state.copy(
                profile = state.profile.copy(
                    heightCm = heightCm ?: state.profile.heightCm,
                    weightKg = weightKg ?: state.profile.weightKg
                )
            )
        )
    }

    fun updatePrefs(restSec: Int? = null, volume: Int? = null) {
        commit(state.copy(
            restTimerSec = restSec ?: state.restTimerSec,
            soundVolume = volume ?: state.soundVolume
        ))
    }

    fun resetAll() {
        viewModelScope.launch { repo.clear() }
        state = AppState()
        draft = Profile()
        screen = Screen.SPLASH
    }
}
