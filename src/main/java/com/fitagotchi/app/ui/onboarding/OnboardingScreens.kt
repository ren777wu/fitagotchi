package com.fitagotchi.app.ui.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.model.*
import com.fitagotchi.app.ui.components.*
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal
import com.fitagotchi.app.vm.AppViewModel
import com.fitagotchi.app.vm.Screen
import java.time.LocalDate
import kotlin.math.roundToInt

@Composable
private fun ObScaffold(
    step: Int?,
    onBack: (() -> Unit)? = null,
    bottomButton: @Composable () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(Modifier.fillMaxSize().background(Brutal.Cream).padding(24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (onBack != null) {
                Box(
                    Modifier.size(40.dp)
                        .brutal(fill = Brutal.White, corner = 10.dp, shadowOffset = 3.dp)
                        .clickable(remember { MutableInteractionSource() }, null) { onBack() },
                    contentAlignment = Alignment.Center
                ) {
                    Text("\u2190", fontFamily = Brutal.MonoFont,
                        fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
                Spacer(Modifier.width(14.dp))
            }
            if (step != null) {
                Box(Modifier.weight(1f)) { StepProgress(step = step, total = 9) }
            }
        }
        Spacer(Modifier.height(20.dp))
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) { content() }
        Spacer(Modifier.height(16.dp))
        bottomButton()
    }
}

/* ------------------------- 0. SPLASH ------------------------- */
@Composable
fun SplashScreen(vm: AppViewModel) {
    Column(Modifier.fillMaxSize().background(Brutal.Cream).padding(32.dp)) {
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Fitagotchi", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
                fontSize = 34.sp, color = Color(0xFF6B5B00))
            Spacer(Modifier.height(40.dp))
            Box(
                Modifier.size(160.dp).brutal(fill = Brutal.White, corner = 80.dp),
                contentAlignment = Alignment.Center
            ) { AssetImage(Assets.LOGO, Modifier.size(110.dp)) } // logo.png
        }
        BrutalButton("BEGIN JOURNEY \u2192", fill = Brutal.Yellow,
            modifier = Modifier.fillMaxWidth()) { vm.navigate(Screen.OB_GENDER) }
        Spacer(Modifier.height(12.dp))
        // v15.5: returning players (e.g. after logout) log straight back in
        BrutalButton("I ALREADY HAVE AN ACCOUNT", fill = Brutal.White, fontSize = 13,
            modifier = Modifier.fillMaxWidth()) { vm.navigate(Screen.LOGIN) }
    }
}

/* ------------------------- 1. GENDER ------------------------- */
@Composable
fun GenderScreen(vm: AppViewModel) {
    ObScaffold(step = 1, onBack = { vm.navigate(Screen.SPLASH) }, bottomButton = {
        BrutalButton("NEXT STEP \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.navigate(Screen.OB_GOAL)
        }
    }) {
        Spacer(Modifier.height(12.dp))
        Text("Who are you?", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Spacer(Modifier.height(32.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            Gender.entries.forEach { g ->
                val sel = vm.draft.gender == g
                Column(
                    Modifier.weight(1f)
                        .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 14.dp)
                        .clickable(remember { MutableInteractionSource() }, null) {
                            vm.draft = vm.draft.copy(gender = g)
                        }
                        .padding(vertical = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // gender_male.png / gender_female.png (pixel art, no emoji)
                    AssetImage(if (g == Gender.MALE) "gender_male" else "gender_female",
                        Modifier.size(56.dp))
                    Spacer(Modifier.height(10.dp))
                    Text(if (g == Gender.MALE) "Male" else "Female",
                        fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
            }
        }
    }
}

/* -------------------------- 2. GOAL -------------------------- */
@Composable
fun GoalScreen(vm: AppViewModel) {
    ObScaffold(step = 2, onBack = { vm.navigate(Screen.OB_GENDER) }, bottomButton = {
        BrutalButton("NEXT STEP \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.navigate(Screen.OB_SHAPE)
        }
    }) {
        Text("What's your primary goal?", fontFamily = Brutal.UiFont,
            fontWeight = FontWeight.Bold, fontSize = 24.sp, textAlign = TextAlign.Center)
        Text("This helps us tune your Fitagotchi's evolution path and daily quests.",
            fontFamily = Brutal.MonoFont, fontSize = 11.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(20.dp))
        val icons = mapOf(
            Goal.LOSE_WEIGHT to "goal_lose", Goal.BUILD_MUSCLE to "goal_muscle",
            Goal.IMPROVE_ENDURANCE to "goal_endurance", Goal.STAY_ACTIVE to "goal_active"
        )
        Goal.entries.forEach { g ->
            val sel = vm.draft.goal == g
            Row(
                Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 12.dp)
                    .clickable(remember { MutableInteractionSource() }, null) {
                        vm.draft = vm.draft.copy(goal = g)
                    }
                    .padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssetImage(icons[g] ?: "", Modifier.size(34.dp))
                Spacer(Modifier.width(12.dp))
                Column {
                    Text(g.label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(g.blurb, fontFamily = Brutal.MonoFont, fontSize = 10.sp)
                }
            }
        }
    }
}

/* ----------------------- 3. BODY SHAPE ----------------------- */
@Composable
fun BodyShapeScreen(vm: AppViewModel) {
    ObScaffold(step = 3, onBack = { vm.navigate(Screen.OB_GOAL) }, bottomButton = {
        BrutalButton("NEXT STEP \u2192", fill = Brutal.Mint,
            modifier = Modifier.fillMaxWidth()) { vm.navigate(Screen.OB_FOCUS) }
    }) {
        Text("Select Body Shape", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Text("Choose your initial build. This will affect your starting stats and aesthetics.",
            fontFamily = Brutal.MonoFont, fontSize = 11.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(20.dp))
        val icons = mapOf(
            BodyShape.SLIM to "shape_slim", BodyShape.STANDARD to "shape_standard",
            BodyShape.ATHLETIC to "shape_athletic"
        )
        BodyShape.entries.forEach { s ->
            val sel = vm.draft.bodyShape == s
            Column(
                Modifier.fillMaxWidth().padding(vertical = 8.dp)
                    .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 14.dp)
                    .clickable(remember { MutableInteractionSource() }, null) {
                        vm.draft = vm.draft.copy(bodyShape = s)
                    }
                    .padding(vertical = 18.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AssetImage(icons[s] ?: "", Modifier.size(56.dp))
                Spacer(Modifier.height(8.dp))
                Text(s.label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(6.dp))
                BrutalPill(s.tag, fill = Brutal.White, fontSize = 10)
            }
        }
    }
}

/* ---------------------- 4. FOCUS AREAS ----------------------- */
@Composable
fun FocusScreen(vm: AppViewModel) {
    ObScaffold(step = 4, onBack = { vm.navigate(Screen.OB_SHAPE) }, bottomButton = {
        BrutalButton("NEXT STEP \u2192", fill = Brutal.Mint,
            enabled = vm.draft.focusAreas.isNotEmpty(),
            modifier = Modifier.fillMaxWidth()) { vm.navigate(Screen.OB_YEAR) }
    }) {
        Text("What's your focus?", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Text("Select the areas you want to prioritize. This helps customize your Fitagotchi's evolution paths.",
            fontFamily = Brutal.MonoFont, fontSize = 11.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        val icons = mapOf(
            Focus.ARMS to "focus_arms", Focus.LEGS to "focus_legs", Focus.CORE to "focus_core",
            Focus.CHEST to "focus_chest", Focus.BACK to "focus_back", Focus.CARDIO to "focus_cardio"
        )
        Focus.entries.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { f ->
                    val sel = f in vm.draft.focusAreas
                    Column(
                        Modifier.weight(1f)
                            .brutal(fill = if (sel) Brutal.Mint else Brutal.White, corner = 12.dp)
                            .clickable(remember { MutableInteractionSource() }, null) {
                                vm.draft = vm.draft.copy(
                                    focusAreas = if (sel) vm.draft.focusAreas - f else vm.draft.focusAreas + f
                                )
                            }
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AssetImage(icons[f] ?: "", Modifier.size(36.dp))
                        Spacer(Modifier.height(6.dp))
                        Text(f.label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

/* ---------------------- 5. BIRTH YEAR ------------------------ */
@Composable
fun BirthYearScreen(vm: AppViewModel) {
    ObScaffold(step = 5, onBack = { vm.navigate(Screen.OB_FOCUS) }, bottomButton = {
        BrutalButton("NEXT STEP \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.navigate(Screen.OB_HEIGHT)
        }
    }) {
        Text("When were you born?", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
            fontSize = 26.sp, textAlign = TextAlign.Center)
        Text("We use this to calculate your goal fitness baseline.",
            fontFamily = Brutal.MonoFont, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        val years = remember { (1940..LocalDate.now().year - 10).toList() }
        VerticalWheel(
            values = years,
            selected = vm.draft.birthYear,
            onSelected = { vm.draft = vm.draft.copy(birthYear = it) },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/* --------------- Editable numeric readout (dual input) --------------- */
@Composable
fun EditableReadout(
    text: String,
    unitLabel: String,
    onCommit: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var editing by remember { mutableStateOf(false) }
    var buf by remember(text) { mutableStateOf(text) }
    Box(
        modifier.brutal(fill = Brutal.White, corner = 10.dp)
            .clickable(remember { MutableInteractionSource() }, null) { editing = true }
            .padding(horizontal = 22.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (editing) {
            BasicTextField(
                value = buf,
                onValueChange = { buf = it.filter { c -> c.isDigit() || c == '.' } },
                textStyle = TextStyle(fontFamily = Brutal.MonoFont, fontSize = 26.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.width(120.dp)
            )
            Text("OK", fontFamily = Brutal.MonoFont, fontSize = 14.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .clickable {
                        buf.toDoubleOrNull()?.let(onCommit)
                        editing = false
                    })
        } else {
            Text("$text $unitLabel", fontFamily = Brutal.MonoFont, fontSize = 26.sp,
                fontWeight = FontWeight.Bold)
        }
    }
}

/* Unit toggle chip pair (CM/FT, KG/LB). */
@Composable
fun UnitToggle(left: String, right: String, leftSelected: Boolean, onChange: (Boolean) -> Unit) {
    Row(Modifier.brutal(fill = Brutal.White, corner = 50.dp, shadowOffset = 3.dp).padding(3.dp)) {
        listOf(left to true, right to false).forEach { (label, isLeft) ->
            val sel = leftSelected == isLeft
            Box(
                Modifier
                    .background(if (sel) Brutal.Ink else Color.Transparent, CircleShape)
                    .clickable(remember { MutableInteractionSource() }, null) { onChange(isLeft) }
                    .padding(horizontal = 14.dp, vertical = 4.dp)
            ) {
                Text(label, fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sel) Brutal.White else Brutal.Ink)
            }
        }
    }
}

/* ------------------------- 6. HEIGHT ------------------------ */
@Composable
fun HeightScreen(vm: AppViewModel) {
    var metric by remember { mutableStateOf(true) } // true = CM
    val cm = vm.draft.heightCm
    ObScaffold(step = 6, onBack = { vm.navigate(Screen.OB_YEAR) }, bottomButton = {
        BrutalButton("NEXT STEP \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.navigate(Screen.OB_WEIGHT)
        }
    }) {
        Text("How tall are you?", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Text("We use this to customize your virtual pet's habitat.",
            fontFamily = Brutal.MonoFont, fontSize = 11.sp, modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(16.dp))
        UnitToggle("CM", "FT", metric) { metric = it }
        Spacer(Modifier.height(16.dp))

        val display = if (metric) "${cm.roundToInt()}" else String.format("%.1f", AppViewModel.cmToFt(cm))
        EditableReadout(display, if (metric) "cm" else "ft", onCommit = { typed ->
            vm.draft = vm.draft.copy(heightCm = if (metric) typed else AppViewModel.ftToCm(typed))
        })
        Spacer(Modifier.height(16.dp))

        if (metric) {
            VerticalRuler(min = 120.0, max = 220.0, step = 1.0, value = cm,
                onValue = { vm.draft = vm.draft.copy(heightCm = it) })
        } else {
            VerticalRuler(min = 4.0, max = 7.2, step = 0.1, value = AppViewModel.cmToFt(cm),
                onValue = { vm.draft = vm.draft.copy(heightCm = AppViewModel.ftToCm(it)) })
        }
    }
}

/* ------------------------- 7. WEIGHT ------------------------- */
@Composable
fun WeightScreen(vm: AppViewModel) {
    var metric by remember { mutableStateOf(true) } // true = KG
    val kg = vm.draft.weightKg
    ObScaffold(step = 7, onBack = { vm.navigate(Screen.OB_HEIGHT) }, bottomButton = {
        BrutalButton("CALCULATE BMI \u2192", fill = Brutal.Mint,
            modifier = Modifier.fillMaxWidth()) { vm.navigate(Screen.OB_BMI) }
    }) {
        Text("What's your weight?", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 26.sp)
        Spacer(Modifier.height(16.dp))
        UnitToggle("KG", "LB", metric) { metric = it }
        Spacer(Modifier.height(20.dp))

        val display = if (metric) String.format("%.1f", kg) else String.format("%.1f", AppViewModel.kgToLb(kg))
        EditableReadout(display, if (metric) "kg" else "lb", onCommit = { typed ->
            vm.draft = vm.draft.copy(weightKg = if (metric) typed else AppViewModel.lbToKg(typed))
        })
        Spacer(Modifier.height(8.dp))
        Text("\u25BC", color = Brutal.Pink, fontSize = 20.sp)
        Spacer(Modifier.height(8.dp))

        Box(Modifier.fillMaxWidth().brutal(fill = Color(0xFFE8E8E0), corner = 12.dp).padding(vertical = 6.dp)) {
            if (metric) {
                HorizontalRuler(min = 30.0, max = 200.0, step = 0.5, value = kg,
                    onValue = { vm.draft = vm.draft.copy(weightKg = it) })
            } else {
                HorizontalRuler(min = 66.0, max = 440.0, step = 1.0, value = AppViewModel.kgToLb(kg),
                    onValue = { vm.draft = vm.draft.copy(weightKg = AppViewModel.lbToKg(it)) })
            }
        }
    }
}

/* ------------------------ 8. BMI RESULT ----------------------- */
@Composable
fun BmiScreen(vm: AppViewModel) {
    val p = vm.draft
    ObScaffold(step = 8, onBack = { vm.navigate(Screen.OB_WEIGHT) }, bottomButton = {
        BrutalButton("PROCEED TO HATCH \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.navigate(Screen.OB_EGG)
        }
    }) {
        Text("Analysis Complete", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 24.sp)
        Spacer(Modifier.height(28.dp))
        Text("TARGET METRIC", fontFamily = Brutal.MonoFont, fontSize = 11.sp)
        // BMI = kg / m^2 (see Profile.bmi)
        Text(String.format("%.1f", p.bmi), fontFamily = Brutal.MonoFont,
            fontWeight = FontWeight.Bold, fontSize = 56.sp)
        Spacer(Modifier.height(10.dp))
        BrutalPill("YOUR BMI: ${p.bmiCategory}", fill = Brutal.Mint, fontSize = 12)
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().height(16.dp)
            .brutal(fill = Brutal.White, corner = 8.dp, stroke = 2.dp, shadowOffset = 0.dp)) {
            Box(Modifier.fillMaxSize().padding(2.dp).background(
                Brush.horizontalGradient(listOf(Color(0xFF6BAF6B), Brutal.Mint, Brutal.Yellow, Brutal.Pink))
            ))
            val frac = (((p.bmi - 14.0) / 22.0).coerceIn(0.0, 1.0)).toFloat()
            Box(Modifier.fillMaxWidth(frac)) {
                Text("\u25BC", modifier = Modifier.align(Alignment.CenterEnd), fontSize = 14.sp)
            }
        }
        Row(Modifier.fillMaxWidth().padding(top = 4.dp), horizontalArrangement = Arrangement.SpaceBetween) {
            Text("Under", fontFamily = Brutal.MonoFont, fontSize = 9.sp)
            Text("Optimal", fontFamily = Brutal.MonoFont, fontSize = 9.sp)
            Text("Over", fontFamily = Brutal.MonoFont, fontSize = 9.sp)
        }
        Spacer(Modifier.height(24.dp))
        Text("Your Fitagotchi is ready to help you improve!",
            fontFamily = Brutal.MonoFont, fontSize = 12.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(12.dp))
        AssetImage(Assets.LOGO, Modifier.size(80.dp))
    }
}

/* ------------------------ 9. CHOOSE EGG ----------------------- */
@Composable
fun EggScreen(vm: AppViewModel) {
    ObScaffold(step = 9, onBack = { vm.navigate(Screen.OB_BMI) }, bottomButton = {
        BrutalButton("HATCH! \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.navigate(Screen.HATCHING)
        }
    }) {
        AssetImage(Assets.LOGO, Modifier.size(44.dp))
        Text("Choose Your Egg", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Text("Select your companion to begin your fitness journey.",
            fontFamily = Brutal.MonoFont, fontSize = 11.sp, textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 8.dp))
        Spacer(Modifier.height(24.dp))
        val eggFill = mapOf(
            PetType.DOG to Brutal.Yellow, PetType.CAT to Brutal.Mint,
            PetType.CAPYBARA to Brutal.Pink, PetType.RABBIT to Color(0xFFD9D9D9)
        )
        // DRAGON is gacha-exclusive - never offered as a starter egg.
        PetType.entries.filter { it != PetType.DRAGON }.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { pet ->
                    val sel = vm.draftPet == pet
                    Column(
                        Modifier.weight(1f)
                            .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 14.dp)
                            .clickable(remember { MutableInteractionSource() }, null) { vm.draftPet = pet }
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(64.dp).background(eggFill[pet] ?: Brutal.White, CircleShape),
                            contentAlignment = Alignment.Center) {
                            // egg-dog.png / egg-cat.png / egg-capybara.png / egg-rabbit.png
                            AssetImage(Assets.egg(pet), Modifier.size(44.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(pet.label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        BrutalPill(pet.trait, fill = Brutal.White, fontSize = 9)
                    }
                }
            }
        }
    }
}

/* -------------------- HATCHING / NAME PROMPT ------------------- */
@Composable
fun HatchingScreen(vm: AppViewModel) {
    var name by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Brutal.Cream).padding(32.dp)) {
        Box(
            Modifier.size(40.dp)
                .brutal(fill = Brutal.White, corner = 10.dp, shadowOffset = 3.dp)
                .clickable(remember { MutableInteractionSource() }, null) { vm.navigate(Screen.OB_EGG) },
            contentAlignment = Alignment.Center
        ) {
            Text("\u2190", fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
        Column(
            Modifier.weight(1f).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("IT'S HATCHING!", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 28.sp)
            Spacer(Modifier.height(24.dp))
            Box(Modifier.size(150.dp).brutal(fill = Brutal.White, corner = 75.dp),
                contentAlignment = Alignment.Center) {
                AssetImage(Assets.egg(vm.draftPet), Modifier.size(96.dp))
            }
            Spacer(Modifier.height(32.dp))
            Text("Name your ${vm.draftPet.label}:", fontFamily = Brutal.MonoFont, fontSize = 13.sp)
            Spacer(Modifier.height(12.dp))
            Box(Modifier.fillMaxWidth().brutal(fill = Brutal.White, corner = 10.dp).padding(14.dp)) {
                BasicTextField(
                    value = name,
                    onValueChange = { if (it.length <= 14) name = it },
                    textStyle = TextStyle(fontFamily = Brutal.UiFont, fontSize = 20.sp,
                        fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                if (name.isEmpty()) Text("Sparky", fontFamily = Brutal.UiFont, fontSize = 20.sp,
                    color = Brutal.Ink.copy(alpha = 0.3f), fontWeight = FontWeight.Bold,
                    modifier = Modifier.align(Alignment.Center))
            }
        }
        BrutalButton("LET'S GO! \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.finishOnboarding(name)
        }
    }
}
