package com.fitagotchi.app.ui.respawn

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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


@Composable
fun RespawnScreen(vm: AppViewModel) {
    val choices = vm.respawnChoices()
    val ownsOthers = vm.state.ownedPets.isNotEmpty()
    var selected by remember { mutableStateOf(choices.firstOrNull() ?: PetType.DOG) }
    var name by remember { mutableStateOf("") }
    val deadName = vm.respawnNotice?.petName ?: "Your pet"

    Column(
        Modifier.fillMaxSize().background(Brutal.Cream)
            .verticalScroll(rememberScrollState()).padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(8.dp))
        Box(Modifier.size(72.dp).brutal(fill = Brutal.Pink, corner = 36.dp),
            contentAlignment = Alignment.Center) {
            Text("\uD83D\uDC94", fontSize = 30.sp)
        }
        Spacer(Modifier.height(16.dp))
        Text("$deadName has passed away", fontFamily = Brutal.UiFont,
            fontWeight = FontWeight.Bold, fontSize = 22.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(8.dp))
        Text(
            if (ownsOthers)
                "Your coins and items are safe. Choose one of your other companions to continue with."
            else
                "Your coins and items are safe. Hatch a new companion to continue your journey.",
            fontFamily = Brutal.MonoFont, fontSize = 11.sp, textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        Text(if (ownsOthers) "Choose a Pet" else "Choose Your New Egg",
            fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(Modifier.height(16.dp))

        val eggFill = mapOf(
            PetType.DOG to Brutal.Yellow, PetType.CAT to Brutal.Mint,
            PetType.CAPYBARA to Brutal.Pink, PetType.RABBIT to Color(0xFFD9D9D9),
            PetType.DRAGON to Brutal.Mint
        )
        choices.chunked(2).forEach { row ->
            Row(Modifier.fillMaxWidth().padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                row.forEach { pet ->
                    val sel = selected == pet
                    Column(
                        Modifier.weight(1f)
                            .brutal(fill = if (sel) Brutal.Yellow else Brutal.White, corner = 14.dp)
                            .clickable(remember { MutableInteractionSource() }, null) { selected = pet }
                            .padding(vertical = 18.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(Modifier.size(64.dp)
                            .background(eggFill[pet] ?: Brutal.White, CircleShape),
                            contentAlignment = Alignment.Center) {
                            // Owned pets show their sprite; fresh hatches show the egg.
                            if (ownsOthers) AssetImage("${pet.name.lowercase()}.png", Modifier.size(48.dp))
                            else AssetImage(Assets.egg(pet), Modifier.size(44.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text(pet.label, fontFamily = Brutal.UiFont,
                            fontWeight = FontWeight.Bold, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        BrutalPill(pet.trait, fill = Brutal.White, fontSize = 9)
                    }
                }
                if (row.size == 1) Spacer(Modifier.weight(1f))
            }
        }
        Spacer(Modifier.height(20.dp))

        Text("Name your ${selected.label}:", fontFamily = Brutal.MonoFont, fontSize = 12.sp)
        Spacer(Modifier.height(10.dp))
        Box(Modifier.fillMaxWidth().brutal(fill = Brutal.White, corner = 10.dp).padding(14.dp)) {
            BasicTextField(
                value = name, onValueChange = { if (it.length <= 14) name = it },
                textStyle = TextStyle(fontFamily = Brutal.UiFont, fontSize = 18.sp,
                    fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                singleLine = true, modifier = Modifier.fillMaxWidth()
            )
            if (name.isEmpty()) Text("Sparky", fontFamily = Brutal.UiFont, fontSize = 18.sp,
                color = Brutal.Ink.copy(alpha = 0.3f), fontWeight = FontWeight.Bold,
                modifier = Modifier.align(Alignment.Center))
        }
        Spacer(Modifier.height(20.dp))

        BrutalButton("CONTINUE \u2192", fill = Brutal.Mint, modifier = Modifier.fillMaxWidth()) {
            vm.respawnAs(selected, name)
        }
        Spacer(Modifier.height(24.dp))
    }
}
