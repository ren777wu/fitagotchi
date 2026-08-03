package com.fitagotchi.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.ui.components.BrutalButton
import com.fitagotchi.app.ui.theme.brutal
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.fitagotchi.app.ui.hub.HubScreen
import com.fitagotchi.app.ui.onboarding.*
import com.fitagotchi.app.ui.settings.SettingsScreen
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.workout.*
import com.fitagotchi.app.vm.AppViewModel
import com.fitagotchi.app.vm.Screen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            Box(Modifier.fillMaxSize().background(Brutal.Cream).safeDrawingPadding()) {
                FitagotchiApp()
            }
        }
    }
}

/** Single-activity router. State + persistence live in AppViewModel. */
@Composable
fun FitagotchiApp(vm: AppViewModel = viewModel()) {
    if (!vm.loaded) return // brief blank while DataStore restores state
    when (vm.screen) {
        Screen.SPLASH -> SplashScreen(vm)
        Screen.OB_GENDER -> GenderScreen(vm)
        Screen.OB_GOAL -> GoalScreen(vm)
        Screen.OB_SHAPE -> BodyShapeScreen(vm)
        Screen.OB_FOCUS -> FocusScreen(vm)
        Screen.OB_YEAR -> BirthYearScreen(vm)
        Screen.OB_HEIGHT -> HeightScreen(vm)
        Screen.OB_WEIGHT -> WeightScreen(vm)
        Screen.OB_BMI -> BmiScreen(vm)
        Screen.OB_EGG -> EggScreen(vm)
        Screen.HATCHING -> HatchingScreen(vm)
        Screen.HUB -> HubScreen(vm)
        Screen.SETTINGS -> SettingsScreen(vm)
        Screen.ME -> com.fitagotchi.app.ui.me.MeScreen(vm)
        Screen.PROFILE -> com.fitagotchi.app.ui.me.ProfileScreen(vm)
        Screen.LOGIN -> com.fitagotchi.app.ui.me.LoginScreen(vm)
        Screen.REGISTER -> com.fitagotchi.app.ui.me.RegisterScreen(vm)
        Screen.WORKOUT_ACTIVE -> ActiveExerciseScreen(vm)
        Screen.REST_TIMER -> RestTimerScreen(vm)
        Screen.WORKOUT_REVIEW -> WorkoutReviewScreen(vm)
        Screen.REWARDS -> RewardsScreen(vm)
        Screen.EVOLUTION -> EvolutionScreen(vm)
        Screen.RESPAWN_EGG -> com.fitagotchi.app.ui.respawn.RespawnScreen(vm)
    }
    vm.deathNotice?.let { DeathNoticeDialog(it) { vm.dismissDeathNotice() } }
}


@Composable
fun DeathNoticeDialog(notice: AppViewModel.DeathNotice, onDismiss: () -> Unit) {
    Box(
        Modifier.fillMaxSize().background(Brutal.Ink.copy(alpha = 0.65f)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(28.dp).fillMaxWidth()
                .brutal(fill = Brutal.Cream, corner = 16.dp)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                Modifier.size(72.dp).brutal(fill = Brutal.Pink, corner = 36.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("\u2715", fontFamily = Brutal.MonoFont,
                    fontWeight = FontWeight.Bold, fontSize = 30.sp, color = Brutal.Ink)
            }
            Spacer(Modifier.height(16.dp))
            Text("OH NO...", fontFamily = Brutal.UiFont,
                fontWeight = FontWeight.Bold, fontSize = 24.sp, color = Brutal.Ink)
            Spacer(Modifier.height(10.dp))
            Text(
                "${notice.petName} starved after ${notice.daysGone} days without care. " +
                        "Your save has been reset - hatch a new companion and keep " +
                        "their hunger bar fed by visiting (and working out!) regularly.",
                fontFamily = Brutal.MonoFont, fontSize = 12.sp,
                textAlign = TextAlign.Center, color = Brutal.Ink
            )
            Spacer(Modifier.height(20.dp))
            BrutalButton("START OVER \u2192", fill = Brutal.Yellow,
                modifier = Modifier.fillMaxWidth()) { onDismiss() }
        }
    }
}
