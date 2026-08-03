package com.fitagotchi.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.ui.components.*
import com.fitagotchi.app.ui.onboarding.EditableReadout
import com.fitagotchi.app.ui.onboarding.UnitToggle
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal
import com.fitagotchi.app.vm.AppViewModel
import com.fitagotchi.app.vm.Screen
import kotlin.math.roundToInt

@Composable
fun SettingsScreen(vm: AppViewModel) {
    val st = vm.state

    Column(Modifier.fillMaxSize().background(Brutal.Cream)
        .verticalScroll(rememberScrollState()).padding(20.dp)) {

        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(36.dp).brutal(fill = Brutal.White, corner = 8.dp, shadowOffset = 2.dp)
                .clickable(remember { MutableInteractionSource() }, null) { vm.navigate(Screen.ME) },
                contentAlignment = Alignment.Center) { Text("\u2190", fontWeight = FontWeight.Bold) }
            Spacer(Modifier.weight(1f))
            Text("Settings", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
                fontSize = 24.sp, color = androidx.compose.ui.graphics.Color(0xFF6B5B00))
            Spacer(Modifier.weight(1f))
            Spacer(Modifier.width(36.dp))
        }
        Spacer(Modifier.height(24.dp))

        // (v15: Health Profile moved to Me > My Profile)
        BrutalCard(fill = Brutal.White, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssetImage("icon_prefs", Modifier.size(24.dp)) // pixel sliders
                Spacer(Modifier.width(8.dp))
                Text("Preferences", fontFamily = Brutal.UiFont,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
            }
            Spacer(Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssetImage("icon_timer", Modifier.size(20.dp)) // pixel stopwatch
                Spacer(Modifier.width(6.dp))
                Text("Rest Timer", fontFamily = Brutal.UiFont,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                BrutalPill("${st.restTimerSec}s", fill = Brutal.Yellow, fontSize = 11)
            }
            Slider(
                value = st.restTimerSec.toFloat(),
                onValueChange = { vm.updatePrefs(restSec = it.roundToInt()) },
                valueRange = 10f..120f, steps = 21,
                colors = SliderDefaults.colors(
                    thumbColor = Brutal.Yellow, activeTrackColor = Brutal.Ink,
                    inactiveTrackColor = Brutal.Ink.copy(alpha = 0.25f)
                )
            )
            Spacer(Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                AssetImage("icon_volume", Modifier.size(20.dp)) // pixel speaker
                Spacer(Modifier.width(6.dp))
                Text("Sound Volume", fontFamily = Brutal.UiFont,
                    fontWeight = FontWeight.Bold, fontSize = 14.sp)
                Spacer(Modifier.weight(1f))
                BrutalPill("${st.soundVolume}%", fill = Brutal.Yellow, fontSize = 11)
            }
            Slider(
                value = st.soundVolume.toFloat(),
                onValueChange = { vm.updatePrefs(volume = it.roundToInt()) },
                valueRange = 0f..100f,
                colors = SliderDefaults.colors(
                    thumbColor = Brutal.Yellow, activeTrackColor = Brutal.Ink,
                    inactiveTrackColor = Brutal.Ink.copy(alpha = 0.25f)
                )
            )

        }
        Spacer(Modifier.height(20.dp))

        /* ================= DEMO TOOLS (v13.8) ================= */
        BrutalCard(fill = Brutal.Lavender, modifier = Modifier.fillMaxWidth()) {
            Text("\uD83C\uDFAF Demo Tools", fontFamily = Brutal.UiFont,
                fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("For presentation only", fontFamily = Brutal.MonoFont, fontSize = 9.sp)
            Spacer(Modifier.height(14.dp))

            // ---- Gift coins ----
            Text("Gift Coins", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                BrutalButton("+1,000 G", fill = Brutal.Yellow, fontSize = 12,
                    modifier = Modifier.weight(1f)) { vm.giftCoins(1000) }
                BrutalButton("+10,000 G", fill = Brutal.Gold, fontSize = 12,
                    modifier = Modifier.weight(1f)) { vm.giftCoins(10000) }
            }
            Spacer(Modifier.height(16.dp))

            // ---- Live hunger decay ----
            Text("Live Hunger Decay", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Points drained per tick while the app is open. 0 = off. Hunger hitting 0 kills the pet!",
                fontFamily = Brutal.MonoFont, fontSize = 9.sp)
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(Modifier.weight(1f)) {
                    EditableReadout("${st.demoDecayAmount}", "pts",
                        onCommit = { vm.setDemoDecay(amount = it.roundToInt()) },
                        modifier = Modifier.fillMaxWidth())
                }
                UnitToggle("MIN", "SEC", st.demoDecayPerMinute) { vm.setDemoDecay(perMinute = it) }
            }
            Spacer(Modifier.height(16.dp))

            // ---- XP per exercise ----
            Text("XP per Completed Exercise", fontFamily = Brutal.UiFont,
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Session XP = exercises completed \u00D7 this value (default 10).",
                fontFamily = Brutal.MonoFont, fontSize = 9.sp)
            Spacer(Modifier.height(8.dp))
            EditableReadout("${st.xpPerExercise}", "xp",
                onCommit = { vm.setXpPerExercise(it.roundToInt()) },
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))

            // ---- Date skipper (streak demo) ----
            Text("In-App Date: ${vm.today()}", fontFamily = Brutal.UiFont,
                fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text("Skip a day, then work out again - watch coins/session climb with the streak. Each skip drains one day of hunger (25), so feed your pet!",
                fontFamily = Brutal.MonoFont, fontSize = 9.sp)
            Spacer(Modifier.height(8.dp))
            BrutalButton("SKIP +1 DAY \u23E9", fill = Brutal.Mint, fontSize = 13,
                modifier = Modifier.fillMaxWidth()) { vm.skipDay() }
            if (st.dateOffsetDays > 0) {
                Spacer(Modifier.height(8.dp))
                BrutalButton("BACK TO REAL DATE (offset: +${st.dateOffsetDays}d)",
                    fill = Brutal.White, fontSize = 11,
                    modifier = Modifier.fillMaxWidth()) { vm.resetDateOffset() }
            }
        }
        Spacer(Modifier.height(28.dp))

        Spacer(Modifier.height(24.dp))
    }
}
