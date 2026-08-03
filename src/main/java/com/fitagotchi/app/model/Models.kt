package com.fitagotchi.app.model

import kotlinx.serialization.Serializable


@Serializable enum class Gender { MALE, FEMALE }

@Serializable enum class Goal(val label: String, val blurb: String) {
    LOSE_WEIGHT("Lose Weight", "Burn calories and lean out."),
    BUILD_MUSCLE("Build Muscle", "Gain strength and bulk up."),
    IMPROVE_ENDURANCE("Improve Endurance", "Boost stamina and cardio."),
    STAY_ACTIVE("Stay Active", "Maintain general health.")
}

@Serializable enum class BodyShape(val label: String, val tag: String) {
    SLIM("Slim", "Agility"),
    STANDARD("Standard", "Balanced"),
    ATHLETIC("Athletic", "Strength")
}

/** Focus areas selectable in onboarding (multi-select). */
@Serializable enum class Focus(val label: String) {
    ARMS("Arms"), LEGS("Legs"), CORE("Core"),
    CHEST("Chest"), BACK("Back"), CARDIO("Cardio")
}

@Serializable enum class PetType(val label: String, val trait: String) {
    DOG("Dog", "Loyal"),
    CAT("Cat", "Agile"),
    CAPYBARA("Capybara", "Chill"),
    RABBIT("Rabbit", "Fast"),
    /** EPIC gacha exclusive - NOT selectable in onboarding (filtered out
     *  in EggScreen). Only obtainable via the Gacha machine. */
    DRAGON("Dragon", "Mythic")
}


object Assets {
    const val LOGO = "logo.png" // app logo (splash + dashboard header)


    fun petSprite(pet: PetType, evolved: Boolean, habitat: HabitatId?): String {
        val base = pet.name.lowercase()
        val evo = if (evolved) "1" else "" // "1" suffix = evolved variant
        return when (habitat) {
            HabitatId.BEACH -> "$base-beach$evo.png"
            HabitatId.NEON  -> "$base-neon$evo.png"
            HabitatId.GYM   -> "$base-gym$evo.png"
            // forest/space have no front-chibi variants (Pet Box uses
            // poseSprite); fall back to the plain front sprite.
            HabitatId.FOREST, HabitatId.SPACE -> "$base$evo.png"
            null -> "$base$evo.png"
        }
    }
    fun egg(pet: PetType) = "egg-${pet.name.lowercase()}.png"


    fun walkSprite(pet: PetType, evolved: Boolean, habitat: HabitatId?, frame: Int): String =
        poseSprite(pet, evolved, habitat, "walk$frame")


    fun poseSprite(pet: PetType, evolved: Boolean, habitat: HabitatId?, pose: String): String {
        val base = pet.name.lowercase() + if (evolved) "1" else ""
        val hab = when (habitat) {
            HabitatId.BEACH -> "_beach"; HabitatId.NEON -> "_neon"
            HabitatId.GYM -> "_gym"; HabitatId.FOREST -> "_forest"
            HabitatId.SPACE -> "_space"; null -> ""
        }
        return "$base${hab}_$pose.png"
    }
    const val BACKPACK = "backpack.png"
}


object Tuning {
    /** Coins a brand-new save starts with. */
    const val START_COINS = 250
    /** XP granted per successfully completed exercise in a session. */
    const val XP_PER_EXERCISE = 10
    /** XP needed to gain one level. */
    const val XP_PER_LEVEL = 100
    /** Pet evolves the first time it reaches this level. */
    const val EVOLVE_AT_LEVEL = 5

    /* ---------------- GACHA ---------------- */
    const val GACHA_SINGLE_COST = 50
    const val GACHA_TEN_COST = 450          // 10 pulls, save 50
    const val GACHA_EPIC_RATE = 0.01
    const val GACHA_RARE_RATE = 0.09
    const val GACHA_UNCOMMON_RATE = 0.20    // remainder (0.70) = COMMON
    const val GACHA_DUP_HABITAT_COINS = 100
    const val GACHA_DUP_PET_COINS = 250
    const val GACHA_DUP_DRAGON_COINS = 500
}

/* --------------------------- GACHA --------------------------- */

@Serializable enum class GachaRarity(val label: String, val pct: String) {
    COMMON("COMMON", "70%"),
    UNCOMMON("UNCOMMON", "20%"),
    RARE("RARE", "9%"),
    EPIC("EPIC", "1%")
}

/* ------------------------- WORKOUT DOMAIN ------------------------- */

@Serializable enum class Metric { REPS, SECONDS }

/**
 * Exercise Library (Blueprint §5 - no equipment).
 * baseAmount = baseline reps (strength) or seconds (cardio/isometric = 30s standard).
 */
@Serializable
data class Exercise(
    val name: String,
    val focus: Focus,
    val metric: Metric,
    val baseAmount: Int,
    /** Muscle chips shown in the exercise info sheet. */
    val muscles: List<String> = emptyList(),
    /** Short how-to shown in the exercise info sheet. */
    val instructions: String = ""
) {
    /**
     * Illustration asset for this exercise, derived from its name:
     *   "Skaters"               -> skaters.png
     *   "Plank-to-Pushup"       -> plank_to_pushup.png
     *   "Bulgarian Split Squats"-> bulgarian_split_squats.png
     * Drop your custom pixel demo art into res/drawable using these
     * names (see README_ASSETS.txt for the full list of 21).
     */
    val art: String
        get() = name.lowercase().replace("-", "_").replace(" ", "_") + ".png"
}

object ExerciseLibrary {
    val ALL: List<Exercise> = listOf(
        // Chest
        Exercise("Standard Pushups", Focus.CHEST, Metric.REPS, 12,
            listOf("Chest", "Triceps", "Core"),
            "Start in a high plank with hands under your shoulders. Lower your chest to just above the floor, keeping your body in a straight line, then press back up."),
        Exercise("Wide Pushups", Focus.CHEST, Metric.REPS, 10,
            listOf("Chest", "Shoulders"),
            "Place your hands wider than shoulder width. Lower your chest between your hands with elbows flaring slightly, then push back to the start."),
        Exercise("Diamond Pushups", Focus.CHEST, Metric.REPS, 8,
            listOf("Triceps", "Chest"),
            "Form a diamond with thumbs and index fingers under your chest. Lower until your chest touches your hands, elbows tight to your ribs, then press up."),
        // Arms
        Exercise("Tricep Dips", Focus.ARMS, Metric.REPS, 12,
            listOf("Triceps", "Shoulders"),
            "Sit on the edge of a sturdy chair, hands beside your hips. Slide off, bend your elbows to lower your body, then press back up without locking out."),
        Exercise("Inchworms", Focus.ARMS, Metric.REPS, 8,
            listOf("Arms", "Core", "Hamstrings"),
            "From standing, hinge down and walk your hands forward into a plank. Pause, then walk your hands back to your feet and stand tall."),
        Exercise("Plank-to-Pushup", Focus.ARMS, Metric.REPS, 10,
            listOf("Arms", "Core", "Chest"),
            "Start in a forearm plank. Press up onto one hand, then the other, into a full pushup position, and lower back down one arm at a time."),
        // Legs
        Exercise("Squats", Focus.LEGS, Metric.REPS, 15,
            listOf("Quads", "Glutes", "Core"),
            "Stand with feet shoulder width apart. Push your hips back and bend your knees until your thighs are parallel to the floor, then drive back up through your heels."),
        Exercise("Lunges", Focus.LEGS, Metric.REPS, 12,
            listOf("Quads", "Glutes", "Balance"),
            "Step one foot forward and lower until both knees are at 90 degrees, keeping your torso upright. Push off the front foot to return, then switch legs."),
        Exercise("Glute Bridges", Focus.LEGS, Metric.REPS, 15,
            listOf("Glutes", "Hamstrings", "Core"),
            "Lie on your back with knees bent, feet flat. Squeeze your glutes and lift your hips until your body forms a straight line, pause, then lower slowly."),
        Exercise("Bulgarian Split Squats", Focus.LEGS, Metric.REPS, 8,
            listOf("Quads", "Glutes", "Balance"),
            "Rest one foot behind you on a chair or couch. Lower your back knee toward the floor while keeping your front shin vertical, then press back up. Do both sides."),
        // Core
        Exercise("Planks", Focus.CORE, Metric.SECONDS, 30,
            listOf("Core", "Shoulders", "Back"),
            "Rest on your forearms and toes, body in one straight line from head to heels. Brace your abs and breathe steadily - do not let your hips sag or pike."),
        Exercise("Bicycle Crunches", Focus.CORE, Metric.REPS, 16,
            listOf("Abs", "Obliques"),
            "Lie on your back, hands behind your head. Bring one knee toward the opposite elbow while extending the other leg, then alternate sides in a pedaling motion."),
        Exercise("Leg Raises", Focus.CORE, Metric.REPS, 12,
            listOf("Lower Abs", "Hip Flexors"),
            "Lie flat with legs extended. Keeping them straight, raise your legs until vertical, then lower slowly without letting your heels touch the floor."),
        Exercise("Mountain Climbers", Focus.CORE, Metric.SECONDS, 30,
            listOf("Core", "Shoulders", "Cardio"),
            "From a high plank, drive one knee toward your chest, then quickly switch legs, as if running in place while keeping your hips low and level."),
        // Back
        Exercise("Supermans", Focus.BACK, Metric.REPS, 12,
            listOf("Lower Back", "Glutes"),
            "Lie face down with arms extended forward. Lift your arms, chest, and legs off the floor at the same time, hold for a beat, then lower with control."),
        Exercise("Bird-Dogs", Focus.BACK, Metric.REPS, 10,
            listOf("Back", "Core", "Balance"),
            "From all fours, extend your right arm and left leg until parallel to the floor. Hold briefly, return, then switch to the opposite arm and leg."),
        Exercise("Reverse Snow Angels", Focus.BACK, Metric.REPS, 10,
            listOf("Upper Back", "Shoulders", "Traps"),
            "Lie face down with arms at your sides. Lift your arms and chest slightly, then sweep your arms overhead and back down in a wide arc, like a snow angel."),
        // Cardio (timed, standard 30s)
        Exercise("Jumping Jacks", Focus.CARDIO, Metric.SECONDS, 30,
            listOf("Full Body", "Cardio"),
            "Jump your feet out wide while raising your arms overhead, then jump back to the start with arms at your sides. Keep a light, steady rhythm."),
        Exercise("Burpees", Focus.CARDIO, Metric.SECONDS, 30,
            listOf("Full Body", "Cardio", "Chest"),
            "From standing, drop into a squat, kick your feet back to a plank, do a pushup, jump your feet back in, and explode up with a jump. Repeat smoothly."),
        Exercise("High Knees", Focus.CARDIO, Metric.SECONDS, 30,
            listOf("Legs", "Core", "Cardio"),
            "Run in place, driving each knee up to hip height. Stay on the balls of your feet and pump your arms to keep the pace quick."),
        Exercise("Skaters", Focus.CARDIO, Metric.SECONDS, 30,
            listOf("Legs", "Glutes", "Balance"),
            "Leap sideways onto one foot, sweeping the other leg behind you like a speed skater, then bound to the other side. Stay low and land softly.")
    )
    fun byFocus(f: Focus) = ALL.filter { it.focus == f }
}

/** One generated slot inside a workout circuit. */
@Serializable
data class WorkoutItem(
    val exercise: Exercise,
    val sets: Int,
    val amount: Int // reps or seconds after intensity scaling
)

@Serializable enum class Feedback { TOO_HARD, JUST_RIGHT, TOO_EASY }

/* ---------------------------- SHOP -------------------------------- */

@Serializable enum class FoodId(
    val label: String, val price: Int, val desc: String,
    val health: Int, val happiness: Int, val asset: String
) {
    APPLE("Crisp Apple", 50, "+20 Health, +5 Happiness", 20, 5, "apple.png"),
    RAMEN("Power Ramen", 150, "Full Restore Health & Energy", 100, 20, "ramen.png"),
    SHAKE("Protein Shake", 80, "+XP Boost for 1 Hour", 10, 10, "shake.png")
}

@Serializable enum class HabitatId(
    val label: String, val price: Int, val desc: String, val asset: String
) {
    BEACH("Pixel Beach", 500, "A relaxing sandy getaway for your companion to chill.", "wallpaper-beach.png"),
    NEON("Neon City", 800, "High-energy urban vibes for the nocturnal athlete.", "wallpaper-neon.png"),
    GYM("Retro Gym", 650, "Old-school iron paradise. Chalk included.", "wallpaper-gym.png"),
    // v14.2 additions
    FOREST("Mossy Forest", 600, "Sun-dappled woods with mushrooms and fireflies.", "wallpaper-forest.png"),
    SPACE("Deep Space", 1000, "Stargaze from the lunar surface. Helmet included.", "wallpaper-space.png")
}

/* ----------------------- CALORIE ENGINE (v15) ----------------------- */
object CalorieEngine {
    private const val SEC_PER_REP = 2.5
    /** MET per exercise (Compendium-based, conservative). */
    private val MET: Map<String, Double> = mapOf(
        "Standard Pushups" to 8.0, "Wide Pushups" to 8.0, "Diamond Pushups" to 8.0,
        "Tricep Dips" to 5.0, "Inchworms" to 5.0, "Plank-to-Pushup" to 6.5,
        "Squats" to 5.0, "Lunges" to 5.5, "Glute Bridges" to 3.5,
        "Bulgarian Split Squats" to 6.0,
        "Planks" to 3.5, "Bicycle Crunches" to 4.5, "Leg Raises" to 3.5,
        "Mountain Climbers" to 8.0,
        "Supermans" to 3.0, "Bird-Dogs" to 3.0, "Reverse Snow Angels" to 3.0,
        "Jumping Jacks" to 8.0, "Burpees" to 9.0, "High Knees" to 8.5, "Skaters" to 7.5
    )
    /** Active seconds for one workout item (rest time excluded). */
    fun seconds(item: WorkoutItem): Double =
        if (item.exercise.metric == Metric.SECONDS) item.amount.toDouble()
        else item.sets * item.amount * SEC_PER_REP
    /** kcal for one item at the given body weight. */
    fun kcal(item: WorkoutItem, weightKg: Double): Double {
        val met = MET[item.exercise.name] ?: 5.0
        return met * 3.5 * weightKg / 200.0 * (seconds(item) / 60.0)
    }
}

/** One completed workout session (drives the Me-page stats & charts). */
@Serializable
data class SessionRecord(
    val date: String,        // ISO date (offset-aware "today")
    val minutes: Double,     // active minutes (rest excluded)
    val kcal: Double,        // CalorieEngine estimate at session weight
    val exercisesDone: Int
)

/* ------------------------- GLOBAL STATE ---------------------------- */

@Serializable
data class Profile(
    val gender: Gender = Gender.MALE,
    val goal: Goal = Goal.STAY_ACTIVE,
    val bodyShape: BodyShape = BodyShape.STANDARD,
    val focusAreas: Set<Focus> = emptySet(),
    val birthYear: Int = 1995,
    val heightCm: Double = 175.0,
    val weightKg: Double = 70.0
) {
    /**
     * BMI MATH:  BMI = weight(kg) / height(m)^2
     * e.g. 70.5kg, 1.75m -> 70.5 / (1.75*1.75) = 23.02
     */
    val bmi: Double get() = weightKg / ((heightCm / 100.0) * (heightCm / 100.0))

    val bmiCategory: String get() = when {
        bmi < 18.5 -> "UNDER"
        bmi < 25.0 -> "NORMAL"
        bmi < 30.0 -> "OVER"
        else -> "OBESE"
    }
}

@Serializable
data class PetState(
    val type: PetType = PetType.DOG,
    val name: String = "Sparky",
    val hunger: Int = 88,          // 0..100
    val xp: Int = 0,
    val level: Int = 1,
    val evolved: Boolean = false
)

/** Per-pet progress (v15.7): each pet keeps its OWN level/xp/evolved and
 *  name, banked here when it's not the active pet. Hunger is shared (it's
 *  the *player's* care habit, not the sprite's), so it lives on PetState
 *  only. Keyed by PetType.name in AppState.petProgress. */
@Serializable
data class PetProgress(
    val name: String = "Sparky",
    val xp: Int = 0,
    val level: Int = 1,
    val evolved: Boolean = false
)

@Serializable
data class AppState(
    val onboarded: Boolean = false,
    val profile: Profile = Profile(),
    val pet: PetState = PetState(),
    val coins: Int = Tuning.START_COINS,
    /** Adaptive difficulty knob. See WorkoutEngine for the math. */
    val intensityMultiplier: Double = 1.0,
    /** ISO dates ("2026-07-12") of days with a completed workout. */
    val workoutHistory: List<String> = emptyList(),
    val streak: Int = 0,
    /** FoodId.name -> owned count (Backpack inventory). */
    val backpack: Map<String, Int> = emptyMap(),
    val ownedHabitats: Set<HabitatId> = emptySet(),
    val equippedHabitat: HabitatId? = null,
    /** Pets unlocked via Gacha. The active pet (pet.type) is always
     *  implicitly owned, so old saves (empty set) migrate cleanly. */
    val ownedPets: Set<PetType> = emptySet(),
    /** Per-pet level/xp/evolved for NON-active pets (v15.7). Keyed by
     *  PetType.name. The active pet's progress lives on `pet` itself and
     *  is banked here on switch/respawn. Empty on old saves = fine. */
    val petProgress: Map<String, PetProgress> = emptyMap(),
    // Settings
    val restTimerSec: Int = 30,
    val soundVolume: Int = 75,
    /* ---- DEMO / PRESENTATION TOOLS (v13.8, Settings > Demo Tools) ----
     * These exist so features can be demonstrated live without waiting
     * real days: tunable XP, live hunger decay, and a skippable date. */
    /** XP granted per completed exercise (default Tuning.XP_PER_EXERCISE). */
    val xpPerExercise: Int = Tuning.XP_PER_EXERCISE,
    /** LIVE hunger decay for demos: points drained per tick. 0 = off. */
    val demoDecayAmount: Int = 0,
    /** Tick unit for live decay: true = per minute, false = per second. */
    val demoDecayPerMinute: Boolean = true,
    /** Days the in-app "today" is skipped ahead of the real clock.
     *  Every date feature (calendar, streaks, starvation) uses
     *  realToday + dateOffsetDays as its notion of "today". */
    val dateOffsetDays: Long = 0,

    val username: String? = null,
    val passwordHash: String? = null,
    val loggedIn: Boolean = false,
    /** Profile avatar: one of "dog","cat","capybara","rabbit","dragon". */
    val avatarId: String = "dog",
    /** Completed sessions log (date, minutes, kcal) for Me-page stats. */
    val sessionLog: List<SessionRecord> = emptyList(),
    /** Epoch day of the last time the app saved state - drives the
     *  starvation clock in AppViewModel (0 = not yet set). */
    val lastActiveEpochDay: Long = 0
)
