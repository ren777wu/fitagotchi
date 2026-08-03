package com.fitagotchi.app.ui.me

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.model.*
import com.fitagotchi.app.ui.components.*
import com.fitagotchi.app.ui.onboarding.EditableReadout
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal
import com.fitagotchi.app.vm.AppViewModel
import com.fitagotchi.app.vm.Screen
import kotlin.math.roundToInt


private val AVATARS = listOf("dog", "cat", "capybara", "rabbit", "dragon")

@Composable
private fun BackHeader(vm: AppViewModel, title: String, backTo: Screen) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(36.dp).brutal(fill = Brutal.White, corner = 8.dp, shadowOffset = 2.dp)
            .clickable(remember { MutableInteractionSource() }, null) { vm.navigate(backTo) },
            contentAlignment = Alignment.Center) { Text("\u2190", fontWeight = FontWeight.Bold) }
        Spacer(Modifier.weight(1f))
        Text(title, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
            fontSize = 24.sp, color = Color(0xFF6B5B00))
        Spacer(Modifier.weight(1f))
        Spacer(Modifier.width(36.dp))
    }
}

@Composable
fun MeScreen(vm: AppViewModel) {
    val st = vm.state
    var pickAvatar by remember { mutableStateOf(false) }
    var confirmWipe by remember { mutableStateOf(false) }
    var showWipeAuth by remember { mutableStateOf(false) }
    var wipePass by remember { mutableStateOf("") }
    var wipeError by remember { mutableStateOf(false) }

    Column(Modifier.fillMaxSize().background(Brutal.Cream)
        .verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {

        BackHeader(vm, "Me", Screen.HUB)
        Spacer(Modifier.height(20.dp))

        /* ---- Avatar (tap to change) + welcome ---- */
        Box(
            Modifier.size(84.dp).brutal(fill = Brutal.White, corner = 42.dp)
                .clickable(remember { MutableInteractionSource() }, null) { pickAvatar = true },
            contentAlignment = Alignment.Center
        ) { AssetImage("avatar_${st.avatarId}", Modifier.size(68.dp)) }
        Text("tap to change", fontFamily = Brutal.MonoFont, fontSize = 9.sp,
            color = Brutal.Ink.copy(alpha = 0.5f), modifier = Modifier.padding(top = 4.dp))
        Spacer(Modifier.height(8.dp))
        Text(
            if (st.loggedIn && st.username != null) "Welcome, ${st.username}!"
            else "Welcome, my friend!",
            fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 20.sp
        )
        Spacer(Modifier.height(16.dp))

        /* ---- Lifetime stats: Records | Calories | Duration ---- */
        val totalKcal = st.sessionLog.sumOf { it.kcal }
        val totalMin = st.sessionLog.sumOf { it.minutes }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
            StatCol("${st.sessionLog.size}", "Records")
            StatCol("${totalKcal.roundToInt()}", "Calories")
            StatCol("${totalMin.roundToInt()}", "Duration (min)")
        }
        Spacer(Modifier.height(18.dp))

        /* ---- Auth button (replaces "Go Premium") ---- */
        if (st.loggedIn) {
            BrutalButton("LOGOUT", fill = Brutal.Yellow, modifier = Modifier.fillMaxWidth()) {
                vm.logout()
            }
        } else {
            BrutalButton("LOGIN / REGISTER", fill = Brutal.Yellow, modifier = Modifier.fillMaxWidth()) {
                vm.clearAuthError(); vm.navigate(Screen.LOGIN)
            }
        }
        Spacer(Modifier.height(18.dp))

        /* ---- Menu card ---- */
        BrutalCard(fill = Brutal.White, modifier = Modifier.fillMaxWidth(), padding = 6.dp) {
            MenuRow("icon_prefs", "Settings") { vm.navigate(Screen.SETTINGS) }
            MenuDivider()
            MenuRow("avatar_${st.avatarId}", "My Profile") { vm.navigate(Screen.PROFILE) }
            MenuDivider()
            // Wipe Data (red) replaces Backup & Restore - double-tap confirm.
            Row(
                Modifier.fillMaxWidth()
                    .clickable(remember { MutableInteractionSource() }, null) {
                        if (!confirmWipe) confirmWipe = true
                        else if (st.loggedIn) { showWipeAuth = true; wipePass = ""; wipeError = false }
                        else vm.resetAll() // guest: no password to verify
                    }
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(if (confirmWipe) "TAP AGAIN TO WIPE EVERYTHING" else "Wipe Data",
                    fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
                    fontSize = 15.sp, color = Brutal.DangerRed)
                Spacer(Modifier.weight(1f))
                Text("\u203A", fontSize = 18.sp, color = Brutal.DangerRed)
            }
        }
        if (confirmWipe) Text("Deletes account, pet, coins and all history. Cannot be undone.",
            fontFamily = Brutal.MonoFont, fontSize = 9.sp, color = Brutal.DangerRed,
            modifier = Modifier.padding(top = 6.dp))
        Spacer(Modifier.height(22.dp))

        /* ---- This Week ---- */
        Text("Last 7 Days", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
            fontSize = 20.sp, modifier = Modifier.align(Alignment.Start))
        Spacer(Modifier.height(12.dp))
        val week = weekData(vm)
        val labels = week.map { it.first }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
            WeekCard("Duration", "${week.sumOf { it.second }.roundToInt()}", "min",
                week.map { it.second }, labels, Brutal.Lavender, Modifier.weight(1f))
            WeekCard("Calories", "${week.sumOf { it.third }.roundToInt()}", "kcal",
                week.map { it.third }, labels, Color(0xFFE8A24A), Modifier.weight(1f))
        }
        Text("Estimates: kcal/min = MET \u00D7 3.5 \u00D7 weight \u00F7 200 (rest time excluded)",
            fontFamily = Brutal.MonoFont, fontSize = 8.sp, color = Brutal.Ink.copy(alpha = 0.5f),
            modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
    }

    /* ---- Password-verified wipe (logged-in accounts only) ---- */
    if (showWipeAuth) {
        Box(Modifier.fillMaxSize().background(Brutal.Ink.copy(alpha = 0.65f))
            .clickable(remember { MutableInteractionSource() }, null) { },
            contentAlignment = Alignment.Center) {
            Column(Modifier.padding(28.dp).fillMaxWidth()
                .brutal(fill = Brutal.Cream, corner = 16.dp).padding(22.dp)) {
                Text("Confirm Wipe", fontFamily = Brutal.UiFont,
                    fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Brutal.DangerRed)
                Text("Enter your password to permanently delete the '${st.username}' account and all its data.",
                    fontFamily = Brutal.MonoFont, fontSize = 10.sp,
                    modifier = Modifier.padding(top = 6.dp, bottom = 14.dp))
                AuthField("PASSWORD", wipePass, { wipePass = it }, password = true)
                if (wipeError) {
                    Text("Wrong password - nothing was deleted.",
                        fontFamily = Brutal.MonoFont, fontSize = 11.sp, color = Brutal.DangerRed)
                    Spacer(Modifier.height(10.dp))
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    BrutalButton("CANCEL", fill = Brutal.White, fontSize = 13,
                        modifier = Modifier.weight(1f)) {
                        showWipeAuth = false; confirmWipe = false
                    }
                    BrutalButton("WIPE", fill = Brutal.Pink, fontSize = 13,
                        modifier = Modifier.weight(1f)) {
                        if (vm.wipeAccount(wipePass)) { showWipeAuth = false }
                        else wipeError = true
                    }
                }
            }
        }
    }

    /* ---- Avatar picker: 5 pixel pet portraits, no uploads ---- */
    if (pickAvatar) {
        Box(Modifier.fillMaxSize().background(Brutal.Ink.copy(alpha = 0.6f))
            .clickable(remember { MutableInteractionSource() }, null) { pickAvatar = false },
            contentAlignment = Alignment.Center) {
            Column(Modifier.padding(28.dp).brutal(fill = Brutal.Cream, corner = 16.dp).padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Choose Avatar", fontFamily = Brutal.UiFont,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(14.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    AVATARS.forEach { id ->
                        val sel = vm.state.avatarId == id
                        Box(Modifier.size(52.dp)
                            .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 26.dp,
                                stroke = 2.dp, shadowOffset = 2.dp)
                            .clickable(remember { MutableInteractionSource() }, null) {
                                vm.setAvatar(id); pickAvatar = false
                            },
                            contentAlignment = Alignment.Center) {
                            AssetImage("avatar_$id", Modifier.size(42.dp))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCol(value: String, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(value, fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Text(label, fontFamily = Brutal.MonoFont, fontSize = 10.sp,
            color = Brutal.Ink.copy(alpha = 0.6f))
    }
}

@Composable
private fun MenuRow(icon: String, label: String, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth()
            .clickable(remember { MutableInteractionSource() }, null) { onClick() }
            .padding(horizontal = 12.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AssetImage(icon, Modifier.size(22.dp))
        Spacer(Modifier.width(10.dp))
        Text(label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(Modifier.weight(1f))
        Text("\u203A", fontSize = 18.sp, color = Brutal.Ink.copy(alpha = 0.5f))
    }
}

@Composable
private fun MenuDivider() {
    Box(Modifier.fillMaxWidth().height(2.dp).padding(horizontal = 8.dp)
        .background(Brutal.Ink.copy(alpha = 0.1f)))
}


private fun weekData(vm: AppViewModel): List<Triple<String, Double, Double>> {
    val today = vm.today()
    return (6 downTo 0).map { back ->
        val d = today.minusDays(back.toLong())
        val recs = vm.state.sessionLog.filter { it.date == d.toString() }
        Triple(d.dayOfWeek.name.take(1), recs.sumOf { it.minutes }, recs.sumOf { it.kcal })
    }
}

@Composable
private fun WeekCard(
    title: String, big: String, unit: String,
    values: List<Double>, labels: List<String>, barColor: Color, modifier: Modifier
) {
    Column(modifier.brutal(fill = Brutal.White, corner = 14.dp).padding(14.dp)) {
        Text(title, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Row(verticalAlignment = Alignment.Bottom) {
            Text(big, fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold, fontSize = 30.sp)
            Text(unit, fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                modifier = Modifier.padding(bottom = 5.dp, start = 2.dp))
        }
        Spacer(Modifier.height(10.dp))
        val maxV = (values.maxOrNull() ?: 0.0).coerceAtLeast(1.0)
        Row(Modifier.fillMaxWidth().height(56.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Bottom) {
            values.forEach { v ->
                val frac = (v / maxV).toFloat().coerceIn(0.08f, 1f)
                Box(Modifier.width(9.dp).fillMaxHeight(frac)
                    .background(if (v > 0) barColor else Brutal.Ink.copy(alpha = 0.12f),
                        RoundedCornerShape(3.dp)))
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            labels.forEachIndexed { i, l ->
                val isToday = i == labels.lastIndex
                Text(l, fontFamily = Brutal.MonoFont, fontSize = 9.sp,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                    color = if (isToday) barColor else Brutal.Ink)
            }
        }
    }
}

/* ========================= MY PROFILE ========================= */
@Composable
fun ProfileScreen(vm: AppViewModel) {
    val p = vm.state.profile
    Column(Modifier.fillMaxSize().background(Brutal.Cream)
        .verticalScroll(rememberScrollState()).padding(20.dp)) {
        BackHeader(vm, "My Profile", Screen.ME)
        Spacer(Modifier.height(24.dp))

        BrutalCard(fill = Brutal.White, modifier = Modifier.fillMaxWidth()) {
            Text("Gender", fontFamily = Brutal.MonoFont, fontSize = 11.sp)
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Gender.entries.forEach { g ->
                    val sel = p.gender == g
                    Box(Modifier.weight(1f)
                        .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 10.dp,
                            stroke = 2.dp, shadowOffset = 2.dp)
                        .clickable(remember { MutableInteractionSource() }, null) {
                            vm.updateProfileInfo(gender = g)
                        }.padding(vertical = 10.dp),
                        contentAlignment = Alignment.Center) {
                        Text(if (g == Gender.MALE) "Male" else "Female",
                            fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text("Year of Birth", fontFamily = Brutal.MonoFont, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            EditableReadout("${p.birthYear}", "",
                onCommit = { vm.updateProfileInfo(birthYear = it.roundToInt().coerceIn(1900, 2020)) },
                modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text("Height", fontFamily = Brutal.MonoFont, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            EditableReadout("${p.heightCm.roundToInt()}", "cm",
                onCommit = { vm.updateHealth(heightCm = it) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(16.dp))
            Text("Weight", fontFamily = Brutal.MonoFont, fontSize = 11.sp)
            Spacer(Modifier.height(6.dp))
            EditableReadout(String.format("%.1f", p.weightKg), "kg",
                onCommit = { vm.updateHealth(weightKg = it) }, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(14.dp))
            Text("BMI: ${String.format("%.1f", p.bmi)} (${p.bmiCategory})",
                fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                color = Brutal.Ink.copy(alpha = 0.6f))
        }
        Spacer(Modifier.height(20.dp))

        /* ---------------- ACCOUNT MANAGEMENT (v15.5) ---------------- */
        if (vm.state.loggedIn) {
            var newName by remember { mutableStateOf("") }
            var nameMsg by remember { mutableStateOf<String?>(null) }
            var nameOk by remember { mutableStateOf(false) }
            var curPass by remember { mutableStateOf("") }
            var newPass by remember { mutableStateOf("") }
            var newPass2 by remember { mutableStateOf("") }
            var passMsg by remember { mutableStateOf<String?>(null) }
            var passOk by remember { mutableStateOf(false) }

            BrutalCard(fill = Brutal.White, modifier = Modifier.fillMaxWidth()) {
                Text("Account", fontFamily = Brutal.UiFont,
                    fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.height(14.dp))

                Text("Change Username (current: ${vm.state.username})",
                    fontFamily = Brutal.MonoFont, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                AuthField("NEW USERNAME", newName, { newName = it })
                nameMsg?.let {
                    Text(it, fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                        color = if (nameOk) Brutal.DarkGreen else Brutal.DangerRed)
                    Spacer(Modifier.height(8.dp))
                }
                BrutalButton("SAVE USERNAME", fill = Brutal.Yellow, fontSize = 12,
                    modifier = Modifier.fillMaxWidth()) {
                    val err = vm.changeUsername(newName)
                    nameOk = err == null
                    nameMsg = err ?: "Username updated!"
                    if (nameOk) newName = ""
                }
                Spacer(Modifier.height(20.dp))

                Text("Change Password", fontFamily = Brutal.MonoFont, fontSize = 11.sp)
                Spacer(Modifier.height(8.dp))
                AuthField("CURRENT PASSWORD", curPass, { curPass = it }, password = true)
                AuthField("NEW PASSWORD", newPass, { newPass = it }, password = true)
                AuthField("RE-ENTER NEW PASSWORD", newPass2, { newPass2 = it }, password = true)
                passMsg?.let {
                    Text(it, fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                        color = if (passOk) Brutal.DarkGreen else Brutal.DangerRed)
                    Spacer(Modifier.height(8.dp))
                }
                BrutalButton("SAVE PASSWORD", fill = Brutal.Yellow, fontSize = 12,
                    modifier = Modifier.fillMaxWidth()) {
                    val err = vm.changePassword(curPass, newPass, newPass2)
                    passOk = err == null
                    passMsg = err ?: "Password updated!"
                    if (passOk) { curPass = ""; newPass = ""; newPass2 = "" }
                }
            }
        } else {
            Text("Log in to manage your username and password.",
                fontFamily = Brutal.MonoFont, fontSize = 10.sp,
                color = Brutal.Ink.copy(alpha = 0.5f))
        }
        Spacer(Modifier.height(24.dp))
    }
}

/* ====================== LOGIN / REGISTER ====================== */
@Composable
private fun AuthField(
    label: String, value: String, onChange: (String) -> Unit,
    password: Boolean = false
) {
    var visible by remember { mutableStateOf(false) } // eye toggle state
    Text(label, fontFamily = Brutal.MonoFont, fontSize = 11.sp,
        modifier = Modifier.padding(bottom = 6.dp))
    Row(Modifier.fillMaxWidth().brutal(fill = Brutal.White, corner = 10.dp).padding(14.dp),
        verticalAlignment = Alignment.CenterVertically) {
        BasicTextField(
            value = value, onValueChange = { if (it.length <= 24) onChange(it) },
            textStyle = TextStyle(fontFamily = Brutal.MonoFont, fontSize = 16.sp,
                fontWeight = FontWeight.Bold),
            singleLine = true,
            visualTransformation = if (password && !visible) PasswordVisualTransformation()
                else androidx.compose.ui.text.input.VisualTransformation.None,
            keyboardOptions = if (password) KeyboardOptions(keyboardType = KeyboardType.Password)
                else KeyboardOptions.Default,
            modifier = Modifier.weight(1f)
        )
        if (password) {
            /* EYE ICON: tap to show/hide the password. Open eye = visible;
             * a slash is drawn across it while the password is hidden. */
            Canvas(Modifier.size(22.dp)
                .clickable(remember { MutableInteractionSource() }, null) { visible = !visible }) {
                val w = size.width; val h = size.height
                drawOval(Brutal.Ink, topLeft = Offset(0f, h * 0.28f),
                    size = androidx.compose.ui.geometry.Size(w, h * 0.44f),
                    style = Stroke(width = 5f))
                drawCircle(Brutal.Ink, radius = w * 0.14f)
                if (!visible) drawLine(Brutal.Ink,
                    Offset(w * 0.1f, h * 0.9f), Offset(w * 0.9f, h * 0.1f), strokeWidth = 5f)
            }
        }
    }
    Spacer(Modifier.height(14.dp))
}

@Composable
fun LoginScreen(vm: AppViewModel) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Brutal.Cream).padding(24.dp)) {
        BackHeader(vm, "Login", if (vm.state.onboarded) Screen.ME else Screen.SPLASH)
        Spacer(Modifier.height(30.dp))
        AssetImage("avatar_${vm.state.avatarId}",
            Modifier.size(64.dp).align(Alignment.CenterHorizontally))
        Spacer(Modifier.height(24.dp))
        AuthField("USERNAME", user, { user = it })
        AuthField("PASSWORD", pass, { pass = it }, password = true)
        vm.authError?.let {
            Text(it, fontFamily = Brutal.MonoFont, fontSize = 11.sp, color = Brutal.DangerRed)
            Spacer(Modifier.height(10.dp))
        }
        BrutalButton("LOGIN \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.login(user, pass)
        }
        Spacer(Modifier.height(14.dp))
        Text("No account? Register here",
            fontFamily = Brutal.MonoFont, fontSize = 12.sp, textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
                .clickable(remember { MutableInteractionSource() }, null) {
                    vm.clearAuthError(); vm.navigate(Screen.REGISTER)
                })
    }
}

@Composable
fun RegisterScreen(vm: AppViewModel) {
    var user by remember { mutableStateOf("") }
    var pass by remember { mutableStateOf("") }
    var pass2 by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Brutal.Cream)
        .verticalScroll(rememberScrollState()).padding(24.dp)) {
        BackHeader(vm, "Register", Screen.LOGIN)
        Spacer(Modifier.height(24.dp))
        AuthField("USERNAME", user, { user = it })
        AuthField("PASSWORD", pass, { pass = it }, password = true)
        AuthField("RE-ENTER PASSWORD", pass2, { pass2 = it }, password = true)
        vm.authError?.let {
            Text(it, fontFamily = Brutal.MonoFont, fontSize = 11.sp, color = Brutal.DangerRed)
            Spacer(Modifier.height(10.dp))
        }
        BrutalButton("CREATE ACCOUNT \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.register(user, pass, pass2)
        }
        Spacer(Modifier.height(10.dp))
        Text("Stored on this device only (SHA-256 hashed).",
            fontFamily = Brutal.MonoFont, fontSize = 9.sp,
            color = Brutal.Ink.copy(alpha = 0.5f), textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth())
    }
}
