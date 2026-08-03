package com.fitagotchi.app.ui.hub

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Star
import com.fitagotchi.app.ui.gacha.GachaTab
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.model.*
import com.fitagotchi.app.ui.components.*
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal
import com.fitagotchi.app.vm.AppViewModel
import com.fitagotchi.app.vm.Screen
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

/* =====================================================================
 * HUB: Home / Calendar / Shop tabs with a persistent bottom nav.
 * ===================================================================== */
enum class HubTab(val label: String, val icon: ImageVector) {
    HOME("Home", Icons.Filled.Home),
    CALENDAR("Calendar", Icons.Filled.DateRange),
    SHOP("Shop", Icons.Filled.ShoppingCart),
    GACHA("Gacha", Icons.Filled.Star) // v13.7 lucky-draw machine
}

@Composable
fun HubScreen(vm: AppViewModel) {
    var tab by rememberSaveable { mutableStateOf(HubTab.HOME) }
    Column(Modifier.fillMaxSize().background(Brutal.Cream)) {
        /* ---- Top bar: logo | title | coins (always visible) | settings ---- */
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AssetImage(Assets.LOGO, Modifier.size(30.dp)) // logo.png beside the title
            Spacer(Modifier.width(8.dp))
            Text(
                "Fitagotchi",
                fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 22.sp,
                color = Color(0xFF6B5B00)
            )
            Spacer(Modifier.weight(1f))
            CoinPill(vm.state.coins) // gold disc + white number on black pill
            Spacer(Modifier.width(10.dp))
            // v15: profile avatar replaces the settings gear; leads to Me page
            Box(
                Modifier.size(34.dp)
                    .brutal(fill = Brutal.White, corner = 17.dp, stroke = 2.dp, shadowOffset = 2.dp)
                    .clickable(remember { MutableInteractionSource() }, null) { vm.navigate(Screen.ME) },
                contentAlignment = Alignment.Center
            ) { AssetImage("avatar_${vm.state.avatarId}", Modifier.size(28.dp)) }
        }

        Box(Modifier.weight(1f)) {
            when (tab) {
                HubTab.HOME -> HomeTab(vm)
                HubTab.CALENDAR -> CalendarTab(vm)
                HubTab.SHOP -> ShopTab(vm)
                HubTab.GACHA -> GachaTab(vm)
            }
        }

        /* ---- Bottom nav (per mock): plain icons + labels, selected tab
           gets a mint brutal chip around its icon ---- */
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 32.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            HubTab.entries.forEach { t ->
                val sel = t == tab
                Column(
                    Modifier.clickable(remember { MutableInteractionSource() }, null) { tab = t },
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        if (sel)
                            Modifier.brutal(fill = Brutal.Mint, corner = 10.dp, shadowOffset = 3.dp)
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                        else Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(t.icon, contentDescription = t.label, tint = Brutal.Ink,
                            modifier = Modifier.size(22.dp))
                    }
                    Spacer(Modifier.height(2.dp))
                    Text(t.label, fontFamily = Brutal.MonoFont, fontSize = 10.sp, color = Brutal.Ink)
                }
            }
        }
    }
}

/* --------------------------- HOME TAB --------------------------- */
@Composable
private fun HomeTab(vm: AppViewModel) {
    val st = vm.state
    Column(Modifier.fillMaxSize().padding(horizontal = 20.dp)) {
        // Scrollable content; the workout button below stays pinned.
        Column(
            Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(14.dp)) {
                BrutalBar("Hunger", st.pet.hunger, Brutal.Pink, Modifier.weight(1f))
                // bar fill = progress toward the next level (Tuning.XP_PER_LEVEL)
                BrutalBar("XP", st.pet.xp * 100 / Tuning.XP_PER_LEVEL, Brutal.Mint,
                    Modifier.weight(1f), trailing = "LVL ${st.pet.level}")
            }
            Spacer(Modifier.height(20.dp))
            PetBox(vm)
            Spacer(Modifier.height(16.dp))
            PetNamePill(vm)
            Spacer(Modifier.height(16.dp))
        }
        // Pinned just above the bottom nav; taller, with pixel icon on the left.
        Row(
            Modifier.fillMaxWidth()
                .brutal(fill = Brutal.Mint, corner = 12.dp)
                .clickable(remember { MutableInteractionSource() }, null) { vm.startWorkout() }
                .padding(vertical = 20.dp, horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            AssetImage("icon_workout", Modifier.size(34.dp)) // pixel running shoe
            Spacer(Modifier.width(14.dp))
            Text("START WORKOUT", fontFamily = Brutal.UiFont,
                fontWeight = FontWeight.Bold, fontSize = 19.sp, color = Brutal.Ink)
        }
        Spacer(Modifier.height(12.dp))
    }
}
/* THE LIVING PET BOX */
private enum class PetMode { WALKING, LYING }

@Composable
private fun PetBox(vm: AppViewModel) {
    val st = vm.state
    var drawerOpen by remember { mutableStateOf(false) }
    val habitat = st.equippedHabitat

    Box(Modifier.fillMaxWidth().aspectRatio(1f).brutal(fill = Brutal.White, corner = 14.dp)) {
        Box(
            Modifier.fillMaxSize().padding(6.dp).clip(RoundedCornerShape(10.dp)),
            contentAlignment = Alignment.Center
        ) {
            if (habitat != null) {
                AssetImage(habitat.asset, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
            } else {
                Box(Modifier.fillMaxSize().background(Color(0xFFF7EFE2)))
            }

            /* ---------------- PET BEHAVIOR STATE ---------------- */
            var mode by remember { mutableStateOf(PetMode.LYING) }
            var xFrac by remember { mutableStateOf(0.5f) }   // 0..1 across the box
            var dir by remember { mutableStateOf(1f) }        // 1 = right, -1 = left
            var gait by remember { mutableStateOf(0f) }       // advances while moving
            var interactions by remember { mutableStateOf(0) }
            var lastTap by remember { mutableStateOf(0L) }

            // MOVEMENT LOOP: ~60fps while walking; frozen while lying,
            // so the pet lies down exactly where it stopped.
            LaunchedEffect(mode) {
                while (mode == PetMode.WALKING) {
                    kotlinx.coroutines.delay(16)
                    xFrac += dir * 0.0025f
                    gait += 0.09f
                    if (xFrac >= 1f) { xFrac = 1f; dir = -1f }
                    if (xFrac <= 0f) { xFrac = 0f; dir = 1f }
                }
            }
            // RANDOM AI TIMER: restarts whenever the user double-taps.
            LaunchedEffect(interactions) {
                while (true) {
                    kotlinx.coroutines.delay((6000L..16000L).random())
                    mode = if (mode == PetMode.WALKING) PetMode.LYING else PetMode.WALKING
                }
            }
            val inf = rememberInfiniteTransition(label = "petLife")
            val breath by inf.animateFloat(
                initialValue = 0f, targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    tween(1800, easing = FastOutSlowInEasing), RepeatMode.Reverse
                ), label = "breath"
            )

            val type = st.pet.type
            val walking = mode == PetMode.WALKING
            val frame1 = (gait % 2f) < 1f                 // alternates the 2 frames
            val airborne = walking && type == PetType.DRAGON
            val hopY = if (walking && type == PetType.RABBIT)
                kotlin.math.abs(kotlin.math.sin(gait * 1.7f)) * 16f else 0f
            val flyY = if (airborne) 42f + kotlin.math.sin(gait * 0.8f) * 7f else 0f

            BoxWithConstraints(Modifier.fillMaxSize()) {
                val travel = maxWidth - 150.dp // walkable width
                Box(
                    Modifier.align(Alignment.Center).fillMaxWidth().height(190.dp),
                    contentAlignment = Alignment.BottomCenter
                ) {
                    // Ground shadow: follows the pet; fades/shrinks in the air.
                    Canvas(Modifier.width(96.dp).height(16.dp).graphicsLayer {
                        translationX = (xFrac - 0.5f) * travel.toPx()
                    }) {
                        val airFade = if (airborne) 0.45f else 1f
                        val shrink = (1f - (hopY + flyY) / 110f).coerceIn(0.4f, 1f)
                        drawOval(
                            color = Brutal.Ink.copy(alpha = 0.20f * shrink * airFade),
                            topLeft = Offset(size.width * (1f - shrink) / 2f, size.height * 0.35f),
                            size = Size(size.width * shrink, size.height * 0.6f)
                        )
                    }
                    AssetImage(
                        Assets.poseSprite(
                            type, st.pet.evolved, habitat,
                            if (!walking) "lie" else if (frame1) "walk1" else "walk2"
                        ),
                        Modifier
                            .size(130.dp)
                            .graphicsLayer {
                                translationX = (xFrac - 0.5f) * travel.toPx()
                                translationY = -(hopY + flyY).dp.toPx()
                                scaleX = if (dir >= 0f) 1f else -1f
                                if (!walking) scaleY = 1f - breath * 0.035f // breathing
                            }
                            .clickable(remember { MutableInteractionSource() }, null) {
                                val now = System.currentTimeMillis()
                                if (now - lastTap < 300L) { // DOUBLE-TAP toggle
                                    mode = if (walking) PetMode.LYING else PetMode.WALKING
                                    interactions++ // restart the random AI timer
                                }
                                lastTap = now
                            }
                    )
                }
                if (st.pet.evolved && habitat == null) {
                    Text("EVOLVED", fontFamily = Brutal.MonoFont, fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp))
                }
            }
        }

        /* ---- BACKPACK DRAWER: expands right-to-left from the icon ---- */
        val targetWidth = if (drawerOpen) 264.dp else 48.dp
        val drawerWidth by animateDpAsState(targetWidth, tween(durationMillis = 450), label = "drawer")
        Row(
            Modifier
                .align(Alignment.TopEnd)
                .padding(10.dp)
                .width(drawerWidth)
                .brutal(fill = Brutal.Yellow, corner = 22.dp, shadowOffset = 3.dp)
                .padding(horizontal = 6.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            if (drawerOpen) {
                Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically) {
                    val owned = FoodId.entries.filter { (vm.state.backpack[it.name] ?: 0) > 0 }
                    if (owned.isEmpty()) {
                        Text("  Backpack empty - visit the Shop!",
                            fontFamily = Brutal.MonoFont, fontSize = 10.sp, maxLines = 1)
                    } else owned.forEach { food ->
                        Row(
                            Modifier
                                .brutal(fill = Brutal.White, corner = 8.dp, stroke = 2.dp, shadowOffset = 2.dp)
                                .clickable(remember { MutableInteractionSource() }, null) { vm.feedPet(food) }
                                .padding(horizontal = 5.dp, vertical = 2.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            AssetImage(food.asset, Modifier.size(18.dp))
                            Text("x${vm.state.backpack[food.name]}",
                                fontFamily = Brutal.MonoFont, fontSize = 11.sp)
                        }
                    }
                }
            }
            AssetImage(Assets.BACKPACK, Modifier.size(26.dp)
                .clickable(remember { MutableInteractionSource() }, null) { drawerOpen = !drawerOpen })
        }
    }
}


@Composable
private fun PetNamePill(vm: AppViewModel) {
    var editing by remember { mutableStateOf(false) }
    var buf by remember { mutableStateOf(vm.state.pet.name) }
    var lastTap by remember { mutableStateOf(0L) }

    if (editing) {
        Row(
            Modifier.brutal(fill = Brutal.White, corner = 50.dp).padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            BasicTextField(
                value = buf, onValueChange = { if (it.length <= 14) buf = it },
                textStyle = TextStyle(fontFamily = Brutal.UiFont, fontSize = 16.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                singleLine = true, modifier = Modifier.width(120.dp)
            )
            Text(" OK", fontFamily = Brutal.MonoFont, fontSize = 13.sp, fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable(remember { MutableInteractionSource() }, null) {
                        vm.renamePet(buf); editing = false
                    })
        }
    } else {
        Box(
            Modifier
                .brutal(fill = Brutal.Yellow, corner = 50.dp)
                .clickable(remember { MutableInteractionSource() }, null) {
                    val now = System.currentTimeMillis()
                    if (now - lastTap < 300L) { // DOUBLE-CLICK detected
                        buf = vm.state.pet.name
                        editing = true
                    }
                    lastTap = now
                }
                .padding(horizontal = 26.dp, vertical = 8.dp)
        ) {
            Text(vm.state.pet.name, fontFamily = Brutal.UiFont,
                fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}

/* ------------------------- CALENDAR TAB ------------------------ */
@Composable
private fun CalendarTab(vm: AppViewModel) {
    // Offset-aware date (Demo Tools can skip days): golden box follows it.
    val today = vm.today()
    var month by remember(today) { mutableStateOf(YearMonth.from(today)) }
    val done = vm.state.workoutHistory.toSet()

    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally) {
        BrutalCard(fill = Brutal.White, modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                BrutalPill("<", fill = Brutal.Yellow, fontSize = 16) { month = month.minusMonths(1) }
                Spacer(Modifier.weight(1f))
                Text("${month.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${month.year}",
                    fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(Modifier.weight(1f))
                BrutalPill(">", fill = Brutal.Yellow, fontSize = 16) { month = month.plusMonths(1) }
            }
            Spacer(Modifier.height(14.dp))
            Row(Modifier.fillMaxWidth()) {
                listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                    Text(it, Modifier.weight(1f), textAlign = TextAlign.Center,
                        fontFamily = Brutal.MonoFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(8.dp))
            // DayOfWeek: SUNDAY=7 in java.time -> convert so Sunday = column 0.
            val firstCol = month.atDay(1).dayOfWeek.value % 7
            val days = month.lengthOfMonth()
            val cells: List<Int?> = List(firstCol) { null } + (1..days).toList()
            cells.chunked(7).forEach { week ->
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
                    week.forEach { d -> DayCell(d, month, today, done, Modifier.weight(1f)) }
                    repeat(7 - week.size) { Spacer(Modifier.weight(1f)) }
                }
            }
        }
        Spacer(Modifier.height(20.dp))
        Box(Modifier.fillMaxWidth().brutal(fill = Brutal.Yellow, corner = 50.dp).padding(vertical = 10.dp),
            contentAlignment = Alignment.Center) {
            Text("MONTHLY STREAK: ${vm.state.streak} DAYS",
                fontFamily = Brutal.MonoFont, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DayCell(
    day: Int?, month: YearMonth, today: LocalDate, done: Set<String>, modifier: Modifier
) {
    if (day == null) { Spacer(modifier); return }
    val date = month.atDay(day)
    val isToday = date == today
    val isDone = date.toString() in done
    Box(
        modifier.padding(2.dp).aspectRatio(1f).then(
            when {
                isToday -> Modifier.brutal(fill = Brutal.Gold, corner = 6.dp, stroke = 3.dp, shadowOffset = 2.dp)
                isDone -> Modifier.brutal(fill = Brutal.Mint, corner = 6.dp, stroke = 3.dp, shadowOffset = 2.dp)
                else -> Modifier.background(Color.Transparent)
            }
        ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("$day", fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                fontWeight = if (isToday || isDone) FontWeight.Bold else FontWeight.Normal,
                color = if (date.isAfter(today)) Brutal.Ink.copy(alpha = 0.35f) else Brutal.Ink)
            if (isDone) Text("\u2713", fontFamily = Brutal.MonoFont, fontSize = 9.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/* --------------------------- SHOP TAB -------------------------- */
@Composable
private fun ShopTab(vm: AppViewModel) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp)) {
        Text("Item Shop", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 28.sp)
        Spacer(Modifier.height(16.dp))

        BrutalPill("Nutrition", fill = Brutal.DarkGreen, fontSize = 12, textColor = Brutal.White)
        Spacer(Modifier.height(12.dp))
        FoodId.entries.forEach { food ->
            BrutalCard(fill = Brutal.White, modifier = Modifier.fillMaxWidth().padding(bottom = 14.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(Modifier.size(52.dp).brutal(fill = Brutal.Pink, corner = 10.dp, shadowOffset = 2.dp),
                        contentAlignment = Alignment.Center) {
                        AssetImage(food.asset, Modifier.size(34.dp))
                    }
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(food.label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Spacer(Modifier.weight(1f))
                            CoinPill(food.price, fontSize = 10)
                        }
                        Text(food.desc, fontFamily = Brutal.MonoFont, fontSize = 10.sp)
                        val owned = vm.state.backpack[food.name] ?: 0
                        if (owned > 0) Text("In backpack: x$owned",
                            fontFamily = Brutal.MonoFont, fontSize = 9.sp, color = Color(0xFF0F5132))
                    }
                }
                Spacer(Modifier.height(10.dp))
                BrutalButton("Buy Now", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth(), fontSize = 13,
                    enabled = vm.state.coins >= food.price) { vm.buyFood(food) }
            }
        }

        Spacer(Modifier.height(10.dp))
        BrutalPill("Habitats", fill = Brutal.DangerRed, fontSize = 12, textColor = Brutal.White)
        Spacer(Modifier.height(12.dp))
        HabitatId.entries.forEach { hab ->
            val owned = hab in vm.state.ownedHabitats
            val equipped = vm.state.equippedHabitat == hab
            BrutalCard(fill = Brutal.White, modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp)) {
                // Wallpaper banner (wallpaper-beach.png etc.) with price overlaid.
                Box(Modifier.fillMaxWidth().height(140.dp)
                    .brutal(fill = Color(0xFFDDD8CB), corner = 10.dp, shadowOffset = 2.dp)) {
                    Box(Modifier.fillMaxSize().padding(3.dp).clip(RoundedCornerShape(8.dp))) {
                        AssetImage(hab.asset, Modifier.fillMaxSize(), contentScale = ContentScale.Crop)
                    }
                    Box(Modifier.align(Alignment.TopEnd).padding(8.dp)) {
                        CoinPill(hab.price, fontSize = 11)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Text(hab.label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Text(hab.desc, fontFamily = Brutal.MonoFont, fontSize = 10.sp)
                Spacer(Modifier.height(10.dp))
                when {
                    // Equipping swaps the Pet Box wallpaper AND the pet sprite
                    // (e.g. dog.png -> dog-beach.png) - see PetBox + Assets.petSprite.
                    equipped -> BrutalButton("Equipped", fill = Brutal.Gold,
                        modifier = Modifier.fillMaxWidth(), fontSize = 13) { vm.equipHabitat(null) }
                    owned -> BrutalButton("Equip", fill = Brutal.Yellow,
                        modifier = Modifier.fillMaxWidth(), fontSize = 13) { vm.equipHabitat(hab) }
                    else -> BrutalButton("Buy Now", fill = Brutal.Mint,
                        modifier = Modifier.fillMaxWidth(), fontSize = 13,
                        enabled = vm.state.coins >= hab.price) { vm.buyHabitat(hab) }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
    }
}
