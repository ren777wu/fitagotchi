package com.fitagotchi.app.ui.gacha

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.model.*
import com.fitagotchi.app.ui.components.*
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal
import com.fitagotchi.app.vm.AppViewModel


private val RarityFill = mapOf(
    GachaRarity.COMMON to Brutal.White,
    GachaRarity.UNCOMMON to Brutal.Mint,
    GachaRarity.RARE to Brutal.Lavender,
    GachaRarity.EPIC to Brutal.Yellow
)

/** The six dome items + what tapping each one explains. */
private data class DomeItem(
    val asset: String, val title: String,
    val rarity: GachaRarity, val desc: String
)

private val DOME_ITEMS = listOf(
    DomeItem("apple.png", "Crisp Apple", GachaRarity.COMMON,
        "Consumable - +20 Health, +5 Happiness for your pet."),
    DomeItem("ramen.png", "Power Ramen", GachaRarity.COMMON,
        "Consumable - fully restores your pet's Health & Energy."),
    DomeItem("shake.png", "Protein Shake", GachaRarity.COMMON,
        "Consumable - grants an XP Boost for 1 hour."),
    DomeItem("wallpaper-beach.png", "Habitat Wallpaper", GachaRarity.UNCOMMON,
        "Unlocks one of the 5 habitat wallpapers at random. Duplicates refund ${Tuning.GACHA_DUP_HABITAT_COINS} G."),
    DomeItem("egg-cat.png", "Starter Pet", GachaRarity.RARE,
        "A second chance at egg choice! Unlocks a random starter you don't own (Dog/Cat/Capybara/Rabbit)."),
    DomeItem("dragon.png", "Dragon", GachaRarity.EPIC,
        "MYTHIC companion - only obtainable here. Duplicates refund ${Tuning.GACHA_DUP_DRAGON_COINS} G.")
)

@Composable
fun GachaTab(vm: AppViewModel) {
    var selected by remember { mutableStateOf<DomeItem?>(null) }
    var showRates by remember { mutableStateOf(false) }

    Box(Modifier.fillMaxSize()) {
        Column(
            Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("GACHA SHOP", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold,
                fontSize = 26.sp, color = Color(0xFF6B5B00))
            Spacer(Modifier.height(16.dp))

            /* ------------------- THE MACHINE ------------------- */
            Column(Modifier.fillMaxWidth().brutal(fill = Brutal.White, corner = 14.dp).padding(10.dp)) {
                // Glass dome: light grey panel holding the 6 drop items.
                Column(
                    Modifier.fillMaxWidth()
                        .brutal(fill = Color(0xFFE9E9E4), corner = 10.dp, shadowOffset = 0.dp)
                        .padding(vertical = 22.dp, horizontal = 14.dp)
                ) {
                    DOME_ITEMS.chunked(4).forEach { row ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 6.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)
                        ) {
                            row.forEach { item ->
                                val sel = selected == item
                                Box(
                                    Modifier.size(52.dp)
                                        .brutal(
                                            fill = if (sel) RarityFill.getValue(item.rarity) else Brutal.White,
                                            corner = 8.dp, stroke = 2.dp, shadowOffset = 2.dp
                                        )
                                        .clickable(remember { MutableInteractionSource() }, null) {
                                            selected = if (sel) null else item
                                        },
                                    contentAlignment = Alignment.Center
                                ) { AssetImage(item.asset, Modifier.size(36.dp)) }
                            }
                        }
                    }
                }

                /* TAP-TOOLTIP: description card for the selected dome item  */
                selected?.let { item ->
                    Spacer(Modifier.height(8.dp))
                    Row(
                        Modifier.fillMaxWidth()
                            .brutal(fill = RarityFill.getValue(item.rarity), corner = 10.dp, shadowOffset = 2.dp)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssetImage(item.asset, Modifier.size(30.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${item.title} - ${item.rarity.label} (${item.rarity.pct})",
                                fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(item.desc, fontFamily = Brutal.MonoFont, fontSize = 10.sp, lineHeight = 14.sp)
                        }
                    }
                }
                Spacer(Modifier.height(10.dp))

                // Pink machine body: coin slot + dispenser flap.
                Column(
                    Modifier.fillMaxWidth()
                        .brutal(fill = Brutal.Pink, corner = 10.dp, shadowOffset = 0.dp)
                        .padding(vertical = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        Modifier.size(64.dp).brutal(fill = Brutal.White, corner = 8.dp, shadowOffset = 2.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Box(Modifier.width(30.dp).height(8.dp)
                            .background(Brutal.Ink, RoundedCornerShape(2.dp)))
                    }
                    Spacer(Modifier.height(18.dp))
                    Box(Modifier.width(120.dp).height(40.dp)
                        .background(Brutal.Ink, RoundedCornerShape(6.dp)))
                }
            }
            Spacer(Modifier.height(10.dp))
            BrutalPill("GACHA", fill = Brutal.White, fontSize = 10)
            Spacer(Modifier.height(18.dp))

            /* ------------------- PULL BUTTONS ------------------- */
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Column(
                    Modifier.weight(1f).brutal(fill = Brutal.Yellow, corner = 12.dp)
                        .clickable(remember { MutableInteractionSource() }, null) { vm.gachaPull(1) }
                        .padding(vertical = 18.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("SINGLE PULL", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Spacer(Modifier.height(4.dp))
                    Text("${Tuning.GACHA_SINGLE_COST} G", fontFamily = Brutal.MonoFont,
                        fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
                Box(Modifier.weight(1f)) {
                    Column(
                        Modifier.fillMaxWidth().brutal(fill = Brutal.Mint, corner = 12.dp)
                            .clickable(remember { MutableInteractionSource() }, null) { vm.gachaPull(10) }
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("10 PULLS", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(4.dp))
                        Text("${Tuning.GACHA_TEN_COST} G", fontFamily = Brutal.MonoFont,
                            fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    }
                    // SAVE 50 corner badge, slightly rotated like the mock.
                    Box(
                        Modifier.align(Alignment.TopEnd).rotate(8f)
                            .brutal(fill = Brutal.DangerRed, corner = 4.dp, stroke = 2.dp, shadowOffset = 2.dp)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text("SAVE 50!", fontFamily = Brutal.MonoFont, fontSize = 9.sp,
                            fontWeight = FontWeight.Bold, color = Brutal.White)
                    }
                }
            }
            if (vm.state.coins < Tuning.GACHA_SINGLE_COST) {
                Spacer(Modifier.height(8.dp))
                Text("Not enough coins - complete workouts to earn G!",
                    fontFamily = Brutal.MonoFont, fontSize = 10.sp, color = Brutal.DangerRed)
            }
            Spacer(Modifier.height(12.dp))

            /* ------------------- DROP RATES ------------------- */
            Text("\u24D8 VIEW DROP RATES", fontFamily = Brutal.MonoFont, fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.clickable(remember { MutableInteractionSource() }, null) {
                    showRates = !showRates
                })
            if (showRates) {
                Spacer(Modifier.height(10.dp))
                Column(Modifier.fillMaxWidth().brutal(fill = Brutal.White, corner = 10.dp).padding(12.dp)) {
                    listOf(
                        Triple(GachaRarity.COMMON, "Food (Apple / Ramen / Shake)", ""),
                        Triple(GachaRarity.UNCOMMON, "Wallpaper (one of 5 habitats)", ""),
                        Triple(GachaRarity.RARE, "Starter Pet (2nd chance!)", ""),
                        Triple(GachaRarity.EPIC, "DRAGON - gacha exclusive", "")
                    ).forEach { (r, what, _) ->
                        Row(Modifier.padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                            BrutalPill(r.label, fill = RarityFill.getValue(r), fontSize = 9)
                            Spacer(Modifier.width(8.dp))
                            Text(what, fontFamily = Brutal.MonoFont, fontSize = 10.sp, modifier = Modifier.weight(1f))
                            Text(r.pct, fontFamily = Brutal.MonoFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                    Spacer(Modifier.height(6.dp))
                    Text("10-pull guarantees at least one UNCOMMON or better. Duplicates auto-refund coins.",
                        fontFamily = Brutal.MonoFont, fontSize = 9.sp, color = Brutal.Ink.copy(alpha = 0.6f))
                }
            }
            Spacer(Modifier.height(20.dp))

            /* ------------------- FEATURED ------------------- */
            Text("FEATURED ITEMS", fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold,
                fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(10.dp))
            Row(
                Modifier.fillMaxWidth().brutal(fill = Brutal.White, corner = 12.dp).padding(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(Modifier.size(56.dp).brutal(fill = Brutal.Yellow, corner = 10.dp, shadowOffset = 2.dp),
                    contentAlignment = Alignment.Center) {
                    AssetImage("dragon.png", Modifier.size(40.dp))
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("DRAGON (EPIC)", fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                    Text("A mythic companion that can only hatch from the Gacha. Evolves like any other pet.",
                        fontFamily = Brutal.MonoFont, fontSize = 10.sp, lineHeight = 14.sp)
                }
                Spacer(Modifier.width(8.dp))
                Text(GachaRarity.EPIC.pct, fontFamily = Brutal.MonoFont, fontSize = 12.sp,
                    fontWeight = FontWeight.Bold, color = Brutal.Ink.copy(alpha = 0.55f))
            }
            Spacer(Modifier.height(20.dp))

            /* ---------------- YOUR PETS (switcher) ---------------- */
            Text("YOUR PETS", fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold,
                fontSize = 12.sp, modifier = Modifier.align(Alignment.Start))
            Spacer(Modifier.height(10.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                vm.ownedPets().sortedBy { it.ordinal }.forEach { p ->
                    val active = p == vm.state.pet.type
                    Column(
                        Modifier.weight(1f)
                            .brutal(fill = if (active) Brutal.Yellow else Brutal.White, corner = 12.dp)
                            .clickable(remember { MutableInteractionSource() }, null) { vm.switchPet(p) }
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AssetImage(Assets.petSprite(p, vm.state.pet.evolved, null), Modifier.size(40.dp))
                        Spacer(Modifier.height(4.dp))
                        Text(if (active) "ACTIVE" else p.label, fontFamily = Brutal.MonoFont,
                            fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }

        /* ---------------- PULL RESULTS OVERLAY ---------------- */
        if (vm.lastGachaResults.isNotEmpty()) {
            GachaResultsOverlay(vm)
        }
    }
}

@Composable
private fun GachaResultsOverlay(vm: AppViewModel) {
    Box(
        Modifier.fillMaxSize().background(Brutal.Ink.copy(alpha = 0.7f))
            .clickable(remember { MutableInteractionSource() }, null) { },
        contentAlignment = Alignment.Center
    ) {
        Column(
            Modifier.padding(24.dp).fillMaxWidth()
                .brutal(fill = Brutal.Cream, corner = 16.dp)
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(if (vm.lastGachaResults.size > 1) "10-PULL RESULTS!" else "YOU GOT...",
                fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 22.sp)
            Spacer(Modifier.height(14.dp))
            Column(
                Modifier.weight(1f, fill = false).verticalScroll(rememberScrollState())
            ) {
                vm.lastGachaResults.forEach { pull ->
                    Row(
                        Modifier.fillMaxWidth().padding(vertical = 5.dp)
                            .brutal(fill = RarityFill.getValue(pull.rarity), corner = 10.dp, shadowOffset = 2.dp)
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AssetImage(pull.asset, Modifier.size(34.dp))
                        Spacer(Modifier.width(10.dp))
                        Column(Modifier.weight(1f)) {
                            Text("${pull.label}${if (pull.isDuplicate) " (DUP)" else ""}",
                                fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text(pull.desc, fontFamily = Brutal.MonoFont, fontSize = 9.sp, lineHeight = 12.sp)
                        }
                        Spacer(Modifier.width(6.dp))
                        Text(pull.rarity.label, fontFamily = Brutal.MonoFont, fontSize = 8.sp,
                            fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(Modifier.height(14.dp))
            BrutalButton("COLLECT \u2192", fill = Brutal.Yellow, modifier = Modifier.fillMaxWidth()) {
                vm.dismissGachaResults()
            }
        }
    }
}
