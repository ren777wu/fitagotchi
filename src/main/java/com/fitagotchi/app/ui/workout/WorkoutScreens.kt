package com.fitagotchi.app.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.model.Assets
import com.fitagotchi.app.model.Feedback
import com.fitagotchi.app.model.Metric
import com.fitagotchi.app.ui.components.AssetImage
import com.fitagotchi.app.ui.components.BrutalButton
import com.fitagotchi.app.ui.components.BrutalPill
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal
import com.fitagotchi.app.vm.AppViewModel
import com.fitagotchi.app.vm.Screen
import kotlinx.coroutines.delay

@Composable
fun ActiveExerciseScreen(vm: AppViewModel) {
    val item = vm.currentItem() ?: run { vm.navigate(Screen.HUB); return }
    val isTimed = item.exercise.metric == Metric.SECONDS

    var infoOpen by remember { mutableStateOf(false) }
    var paused by remember(vm.sessionIndex) { mutableStateOf(false) }
    var remaining by remember(vm.sessionIndex) { mutableStateOf(item.amount) }

    /* TIMED COUNTDOWN: ticks once per second unless paused or the info
     * sheet is open; auto-completes the exercise as DONE at 0:00. */
    LaunchedEffect(vm.sessionIndex) {
        if (isTimed) {
            remaining = item.amount
            while (remaining > 0) {
                delay(1000)
                if (!paused && !infoOpen) remaining--
            }
            vm.completeExercise(true)
        }
    }

    Box(Modifier.fillMaxSize().background(Brutal.Cream)) {
        Column(Modifier.fillMaxSize()) {
            Spacer(Modifier.height(14.dp))
            /* --- segmented progress: one bar per exercise --- */
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                repeat(vm.session.size) { i ->
                    val fill = when {
                        i < vm.sessionIndex -> Brutal.Ink
                        i == vm.sessionIndex -> Brutal.Yellow
                        else -> Brutal.Ink.copy(alpha = 0.15f)
                    }
                    Box(
                        Modifier.weight(1f).height(7.dp)
                            .background(fill, RoundedCornerShape(3.dp))
                    )
                }
            }
            Spacer(Modifier.height(14.dp))

            /* --- header: close X | counter + live time --- */
            Row(
                Modifier.fillMaxWidth().padding(horizontal = 20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    Modifier.size(40.dp)
                        .brutal(fill = Brutal.White, corner = 10.dp, shadowOffset = 3.dp)
                        .clickable(remember { MutableInteractionSource() }, null) {
                            vm.navigate(Screen.HUB)
                        },
                    contentAlignment = Alignment.Center
                ) { Text("\u2715", fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold) }
                Spacer(Modifier.weight(1f))
                Column(horizontalAlignment = Alignment.End) {
                    Text("Exercises ${vm.sessionIndex + 1}/${vm.session.size}",
                        fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                    if (isTimed) Text(
                        String.format("%02d:%02d", remaining / 60, remaining % 60),
                        fontFamily = Brutal.MonoFont, fontSize = 13.sp,
                        color = Brutal.Ink.copy(alpha = 0.55f)
                    )
                }
            }
            Spacer(Modifier.height(16.dp))

            Box(
                Modifier.weight(1f).fillMaxWidth().padding(horizontal = 20.dp)
                    .brutal(fill = Color(0xFFF7EFE2), corner = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                AssetImage(
                    item.exercise.art,
                    Modifier.fillMaxWidth(0.9f).fillMaxHeight(0.85f)
                )
            }
            Spacer(Modifier.height(18.dp))

            /* --- black control panel --- */
            Column(
                Modifier.fillMaxWidth()
                    .brutal(fill = Brutal.Ink, corner = 22.dp, shadowOffset = 0.dp)
                    .padding(horizontal = 24.dp, vertical = 22.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(item.exercise.name, fontFamily = Brutal.UiFont,
                        fontWeight = FontWeight.Bold, fontSize = 21.sp, color = Brutal.White)
                    Spacer(Modifier.width(10.dp))
                    // [?] -> exercise info bottom sheet
                    Box(
                        Modifier.size(26.dp)
                            .brutal(fill = Brutal.Lavender, corner = 13.dp,
                                stroke = 2.dp, shadowOffset = 2.dp)
                            .clickable(remember { MutableInteractionSource() }, null) {
                                infoOpen = true
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Text("?", fontFamily = Brutal.MonoFont,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Brutal.Ink)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(
                    if (isTimed) String.format("%02d:%02d", remaining / 60, remaining % 60)
                    else "${item.sets} \u00D7 ${item.amount}",
                    fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold,
                    fontSize = 56.sp, color = Brutal.White
                )
                Spacer(Modifier.height(18.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // FAIL: skipped/failed -> reduces the coin payout
                    Box(
                        Modifier.size(52.dp)
                            .brutal(fill = Brutal.Pink, corner = 26.dp, shadowOffset = 3.dp)
                            .clickable(remember { MutableInteractionSource() }, null) {
                                vm.completeExercise(false)
                            },
                        contentAlignment = Alignment.Center
                    ) { Text("\u2715", fontFamily = Brutal.MonoFont,
                            fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                    Spacer(Modifier.width(18.dp))

                    // CENTER: pause/play for timed, DONE for rep-based
                    if (isTimed) {
                        Box(
                            Modifier.width(120.dp).height(56.dp)
                                .brutal(fill = Brutal.Yellow, corner = 28.dp, shadowOffset = 3.dp)
                                .clickable(remember { MutableInteractionSource() }, null) {
                                    paused = !paused
                                },
                            contentAlignment = Alignment.Center
                        ) { PausePlayGlyph(paused) }
                    } else {
                        Box(
                            Modifier.width(150.dp).height(56.dp)
                                .brutal(fill = Brutal.Yellow, corner = 28.dp, shadowOffset = 3.dp)
                                .clickable(remember { MutableInteractionSource() }, null) {
                                    vm.completeExercise(true)
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Text("DONE", fontFamily = Brutal.UiFont,
                                fontWeight = FontWeight.Bold, fontSize = 18.sp)
                        }
                    }
                    Spacer(Modifier.width(18.dp))

                    // DONE-EARLY / NEXT (timed only; reps use the center DONE)
                    if (isTimed) {
                        Box(
                            Modifier.size(52.dp)
                                .brutal(fill = Brutal.Mint, corner = 26.dp, shadowOffset = 3.dp)
                                .clickable(remember { MutableInteractionSource() }, null) {
                                    vm.completeExercise(true)
                                },
                            contentAlignment = Alignment.Center
                        ) { Text("\u2713", fontFamily = Brutal.MonoFont,
                                fontWeight = FontWeight.Bold, fontSize = 20.sp) }
                    } else {
                        Spacer(Modifier.size(52.dp))
                    }
                }
            }
        }

        if (infoOpen) {
            ExerciseInfoSheet(item = item, onClose = { infoOpen = false })
        }
    }
}

@Composable
private fun PausePlayGlyph(paused: Boolean) {
    Canvas(Modifier.size(22.dp)) {
        if (paused) { // show play triangle
            val path = androidx.compose.ui.graphics.Path().apply {
                moveTo(size.width * 0.15f, 0f)
                lineTo(size.width, size.height / 2f)
                lineTo(size.width * 0.15f, size.height)
                close()
            }
            drawPath(path, Brutal.Ink)
        } else { // show pause bars
            drawRect(Brutal.Ink, size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height))
            drawRect(Brutal.Ink,
                topLeft = Offset(size.width * 0.62f, 0f),
                size = androidx.compose.ui.geometry.Size(size.width * 0.32f, size.height))
        }
    }
}

/* ==================== EXERCISE INFO SHEET ===================== */
@Composable
fun ExerciseInfoSheet(item: com.fitagotchi.app.model.WorkoutItem, onClose: () -> Unit) {
    val dark = Color(0xFF1E1B18)
    Box(
        Modifier.fillMaxSize()
            .background(Brutal.Ink.copy(alpha = 0.55f))
            .clickable(remember { MutableInteractionSource() }, null) { onClose() }
    ) {
        Column(
            Modifier.align(Alignment.BottomCenter).fillMaxWidth()
                .brutal(fill = dark, corner = 24.dp, shadowOffset = 0.dp)
                // swallow taps so the scrim-close doesn't trigger inside
                .clickable(remember { MutableInteractionSource() }, null) {}
                .padding(horizontal = 22.dp, vertical = 18.dp)
        ) {
            // drag-handle
            Box(Modifier.align(Alignment.CenterHorizontally).width(44.dp).height(5.dp)
                .background(Brutal.White.copy(alpha = 0.35f), RoundedCornerShape(3.dp)))
            Spacer(Modifier.height(12.dp))
            Text(item.exercise.name, fontFamily = Brutal.UiFont,
                fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Brutal.White)
            Spacer(Modifier.height(14.dp))

            Column(Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())) {
                // illustration card (white, like the reference)
                Box(
                    Modifier.fillMaxWidth().height(150.dp)
                        .brutal(fill = Brutal.White, corner = 14.dp, shadowOffset = 3.dp),
                    contentAlignment = Alignment.Center
                ) { AssetImage(item.exercise.art, Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.85f)) }
                Spacer(Modifier.height(16.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        if (item.exercise.metric == Metric.SECONDS) "DURATION (SECONDS)" else "SETS \u00D7 REPS",
                        fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold,
                        fontSize = 12.sp, color = Brutal.Mint
                    )
                    Spacer(Modifier.weight(1f))
                    Text(
                        if (item.exercise.metric == Metric.SECONDS)
                            String.format("%02d:%02d", item.amount / 60, item.amount % 60)
                        else "${item.sets} \u00D7 ${item.amount}",
                        fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold,
                        fontSize = 15.sp, color = Brutal.White
                    )
                }
                Spacer(Modifier.height(14.dp))

                Text("INSTRUCTIONS", fontFamily = Brutal.MonoFont,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Brutal.Mint)
                Spacer(Modifier.height(6.dp))
                Text(item.exercise.instructions.ifBlank { "Perform with controlled form and steady breathing." },
                    fontFamily = Brutal.MonoFont, fontSize = 13.sp,
                    color = Brutal.White, lineHeight = 19.sp)
                Spacer(Modifier.height(14.dp))

                Text("FOCUS AREA", fontFamily = Brutal.MonoFont,
                    fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Brutal.Mint)
                Spacer(Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    (item.exercise.muscles.ifEmpty { listOf(item.exercise.focus.label) })
                        .take(4).forEach { m ->
                            Box(
                                Modifier.brutal(fill = Color(0xFF35302B), corner = 50.dp,
                                    stroke = 2.dp, shadowOffset = 2.dp)
                                    .padding(horizontal = 12.dp, vertical = 5.dp)
                            ) {
                                Text(m, fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold, color = Brutal.White)
                            }
                        }
                }
                Spacer(Modifier.height(20.dp))
            }

            BrutalButton("Close", fill = Brutal.Yellow, modifier = Modifier.fillMaxWidth()) { onClose() }
        }
    }
}

/* ------------------------- REST TIMER ------------------------ */
@Composable
fun RestTimerScreen(vm: AppViewModel) {
    var remaining by remember { mutableStateOf(vm.state.restTimerSec) }
    LaunchedEffect(Unit) {
        while (remaining > 0) { delay(1000); remaining-- }
        vm.restFinished()
    }
    Column(
        Modifier.fillMaxSize().background(Brutal.Lavender).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("Rest Timer", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(Modifier.weight(1f))
        Box(
            Modifier.size(220.dp).brutal(fill = Brutal.White, corner = 110.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(String.format("%d:%02d", remaining / 60, remaining % 60),
                    fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold, fontSize = 48.sp)
                Text("RECOVER", fontFamily = Brutal.MonoFont, fontSize = 11.sp)
            }
        }
        Spacer(Modifier.weight(1f))
        BrutalButton("SKIP \u2192", fill = Brutal.White) { vm.restFinished() }
        Spacer(Modifier.height(32.dp))
    }
}

/* ----------------------- WORKOUT REVIEW ----------------------- */
@Composable
fun WorkoutReviewScreen(vm: AppViewModel) {
    var choice by remember { mutableStateOf(Feedback.JUST_RIGHT) }
    Column(
        Modifier.fillMaxSize().background(Brutal.Cream).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Text("How was that?", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 30.sp)
        Text("Help Fitagotchi learn your pacing!", fontFamily = Brutal.MonoFont, fontSize = 12.sp,
            modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(48.dp))
        val rows = listOf(
            Triple(Feedback.TOO_HARD, "face_hard", "Too Hard"),
            Triple(Feedback.JUST_RIGHT, "face_right", "Just Right"),
            Triple(Feedback.TOO_EASY, "face_easy", "Too Easy")
        )
        rows.forEach { (f, iconAsset, label) ->
            val sel = choice == f
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 12.dp)
                    .clickable(remember { MutableInteractionSource() }, null) { choice = f }
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssetImage(iconAsset, Modifier.size(30.dp)) // pixel face, no emoji
                Spacer(Modifier.width(12.dp))
                Text(label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                Box(Modifier.size(20.dp).brutal(
                    fill = if (sel) Brutal.DarkGreen else Brutal.White,
                    corner = 10.dp, stroke = 2.dp, shadowOffset = 0.dp))
            }
        }
        Spacer(Modifier.weight(1f))
        BrutalButton("CONTINUE", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.submitFeedback(choice)
        }
        Spacer(Modifier.height(24.dp))
    }
}

/* ------------------------ REWARDS SUMMARY ---------------------- */
@Composable
fun RewardsScreen(vm: AppViewModel) {
    Column(
        Modifier.fillMaxSize().background(Brutal.Cream).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(32.dp))
        Column(
            Modifier.fillMaxWidth().brutal(fill = Brutal.White, corner = 16.dp).padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(Modifier.size(90.dp).brutal(fill = Brutal.Mint, corner = 45.dp),
                contentAlignment = Alignment.Center) {
                Text("\u2713", fontSize = 44.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(20.dp))
            // Streak-scaled payout, reduced proportionally by failed exercises.
            Text("+${vm.lastCoinReward} COINS", fontFamily = Brutal.MonoFont,
                fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color(0xFF6B5B00))
            Spacer(Modifier.height(4.dp))
            Text("${vm.sessionCompleted} of ${vm.session.size} exercises completed",
                fontFamily = Brutal.MonoFont, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Text("Quest Completed! Your\nFitagotchi is thrilled.",
                fontFamily = Brutal.MonoFont, fontSize = 12.sp, textAlign = TextAlign.Center)
            Spacer(Modifier.height(20.dp))
            BrutalButton("BACK TO HUB \u2192", fill = Brutal.Yellow) { vm.rewardsAcknowledged() }
        }
    }
}

/* -------------------------- EVOLUTION -------------------------- */
@Composable
fun EvolutionScreen(vm: AppViewModel) {
    Column(
        Modifier.fillMaxSize().background(Brutal.White).padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(40.dp))
        Text("EVOLUTION\nCOMPLETE!", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
            fontSize = 32.sp, textAlign = TextAlign.Center, color = Brutal.Yellow,
            modifier = Modifier.background(Brutal.Ink, RoundedCornerShape(6.dp)).padding(12.dp))
        Spacer(Modifier.height(20.dp))
        Text("\u2728 ${vm.state.pet.name.uppercase()} \u2728", fontFamily = Brutal.MonoFont,
            fontWeight = FontWeight.Bold, fontSize = 18.sp, color = Brutal.Gold)
        Spacer(Modifier.height(24.dp))
        // Evolved sprite (asset convention: dog1.png etc.)
        Box(Modifier.size(200.dp).brutal(fill = Color(0xFFDDDDDD), corner = 12.dp),
            contentAlignment = Alignment.Center) {
            // Evolved sprite per asset convention: dog1.png / cat1.png / ...
            AssetImage(Assets.petSprite(vm.state.pet.type, evolved = true, habitat = null),
                Modifier.size(150.dp))
        }
        Spacer(Modifier.weight(1f))
        BrutalButton("AMAZING!", fill = Brutal.Yellow, modifier = Modifier.fillMaxWidth()) {
            vm.evolutionAcknowledged()
        }
        Spacer(Modifier.height(40.dp))
    }
}
