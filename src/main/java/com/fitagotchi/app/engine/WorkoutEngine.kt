package com.fitagotchi.app.engine

import com.fitagotchi.app.model.*
import kotlin.math.min
import kotlin.math.roundToInt

object WorkoutEngine {

    /**
     * WORKOUT SIZE MATH :
     *   - Global bounds: min 5, max 13 exercises.
     *   - "Athletic" + Full Body (all 6 focus areas, or >= 5) -> 10..13.
     *   - "Slim" body shape OR BMI category UNDER/OVER/OBESE  -> 5..8,
     *     concentrated on the user's chosen goals.
     *   - Everyone else -> 7..10.
     */
    private fun targetCount(profile: Profile): Int {
        val fullBody = profile.focusAreas.size >= 5
        return when {
            profile.bodyShape == BodyShape.ATHLETIC && fullBody -> (10..13).random()
            profile.bodyShape == BodyShape.SLIM ||
                profile.bmiCategory != "NORMAL"                 -> (5..8).random()
            else                                                -> (7..10).random()
        }.coerceIn(5, 13)
    }

    /**
     * CARDIO / STRENGTH RATIO MATH:
     * Start from the goal's base cardio share, then nudge by BMI and by
     * whether the user explicitly selected the CARDIO focus tile.
     *
     *   base:   LOSE_WEIGHT 0.55 | IMPROVE_ENDURANCE 0.60
     *           BUILD_MUSCLE 0.20 | STAY_ACTIVE 0.40
     *   BMI:    OVER/OBESE +0.10, UNDER -0.10
     *   Focus:  CARDIO selected +0.10
     *   Clamped to 0.10 .. 0.75 so no circuit is ever 100% one type.
     */
    private fun cardioRatio(profile: Profile): Double {
        var r = when (profile.goal) {
            Goal.LOSE_WEIGHT -> 0.55
            Goal.IMPROVE_ENDURANCE -> 0.60
            Goal.BUILD_MUSCLE -> 0.20
            Goal.STAY_ACTIVE -> 0.40
        }
        when (profile.bmiCategory) {
            "OVER", "OBESE" -> r += 0.10
            "UNDER" -> r -= 0.10
        }
        if (Focus.CARDIO in profile.focusAreas) r += 0.10
        return r.coerceIn(0.10, 0.75)
    }

    /**
     * Generates a 5-13 exercise circuit.
     *
     * Strength slots are distributed ROUND-ROBIN across the user's selected
     * (non-cardio) focus areas so every chosen area gets fair coverage; if
     * an area's pool is exhausted we keep cycling the remaining areas.
     *
     * REP SCALING MATH:
     *   amount = round(baseAmount * intensityMultiplier)
     *   (floored at 5 reps / 10 seconds so nothing degenerates to zero)
     */
    fun generate(profile: Profile, intensityMultiplier: Double): List<WorkoutItem> {
        val total = targetCount(profile)
        val cardioCount = (total * cardioRatio(profile)).roundToInt().coerceIn(1, total - 1)
        val strengthCount = total - cardioCount

        val picked = mutableListOf<Exercise>()

        // --- Cardio slots: sample without replacement, refill if needed ---
        val cardioPool = ExerciseLibrary.byFocus(Focus.CARDIO).shuffled().toMutableList()
        repeat(cardioCount) {
            if (cardioPool.isEmpty()) cardioPool += ExerciseLibrary.byFocus(Focus.CARDIO).shuffled()
            picked += cardioPool.removeAt(0)
        }

        // --- Strength slots: round-robin over selected focus areas ---
        val strengthAreas = (profile.focusAreas - Focus.CARDIO)
            .ifEmpty { setOf(Focus.CHEST, Focus.LEGS, Focus.CORE) } // sensible default
            .toList().shuffled()
        val pools = strengthAreas.associateWith { ExerciseLibrary.byFocus(it).shuffled().toMutableList() }
        var i = 0
        var placed = 0
        var guard = 0
        while (placed < strengthCount && guard < 200) {
            val area = strengthAreas[i % strengthAreas.size]
            val pool = pools.getValue(area)
            if (pool.isEmpty()) pool += ExerciseLibrary.byFocus(area).shuffled() // recycle
            val candidate = pool.removeAt(0)
            if (picked.count { it.name == candidate.name } < 2) { // avoid triple repeats
                picked += candidate
                placed++
            }
            i++; guard++
        }

        // --- Scale each slot by the adaptive intensity multiplier ---
        return picked.shuffled().map { ex ->
            val scaled = (ex.baseAmount * intensityMultiplier).roundToInt()
            val floor = if (ex.metric == Metric.REPS) 5 else 10
            WorkoutItem(
                exercise = ex,
                sets = if (ex.metric == Metric.REPS) 3 else 1,
                amount = scaled.coerceAtLeast(floor)
            )
        }
    }

    /**
     * ADAPTIVE DIFFICULTY MATH (post-workout review):
     *   TOO_HARD   -> multiplier *= 0.8   (back off 20%)
     *   JUST_RIGHT -> multiplier *= 1.0   (hold)
     *   TOO_EASY   -> multiplier *= 1.2   (push 20%)
     * Clamped to [0.4, 3.0] so difficulty can never run away.
     */
    fun adjustIntensity(current: Double, feedback: Feedback): Double {
        val factor = when (feedback) {
            Feedback.TOO_HARD -> 0.8
            Feedback.JUST_RIGHT -> 1.0
            Feedback.TOO_EASY -> 1.2
        }
        return (current * factor).coerceIn(0.4, 3.0)
    }

    /**
     * COIN REWARD MATH (streak bonus):
     *   coins = 50 * min(2.0, 1 + streak * 0.1)
     * i.e. +10% per consecutive day, capped at a 2x payout (100 coins).
     */
    fun coinReward(streak: Int): Int =
        (50 * min(2.0, 1.0 + streak * 0.1)).roundToInt()

    /** XP per session = exercises completed x Tuning.XP_PER_EXERCISE.
     *  (Level pacing + evolution threshold also live in model/Tuning.) */
    fun xpReward(exercisesDone: Int): Int = exercisesDone * Tuning.XP_PER_EXERCISE
}
