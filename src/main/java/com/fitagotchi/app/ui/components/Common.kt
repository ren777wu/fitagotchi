package com.fitagotchi.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal

/** Big chunky button with the hard shadow. */
@Composable
fun BrutalButton(
    text: String,
    modifier: Modifier = Modifier,
    fill: Color = Brutal.Yellow,
    textColor: Color = Brutal.Ink,
    enabled: Boolean = true,
    fontSize: Int = 16,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .brutal(fill = if (enabled) fill else Color(0xFFCFC9A8), corner = 10.dp)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) { onClick() }
            .padding(horizontal = 20.dp, vertical = 14.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = Brutal.UiFont,
            fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp,
            color = textColor,
            textAlign = TextAlign.Center
        )
    }
}

/** Rounded pill with stroke + shadow (tags, pet name, unit chips...). */
@Composable
fun BrutalPill(
    text: String,
    modifier: Modifier = Modifier,
    fill: Color = Brutal.Mint,
    fontSize: Int = 12,
    textColor: Color = Brutal.Ink,
    onClick: (() -> Unit)? = null
) {
    var m = modifier.brutal(fill = fill, corner = 50.dp, shadowOffset = 3.dp)
    if (onClick != null) m = m.clickable(
        interactionSource = remember { MutableInteractionSource() }, indication = null
    ) { onClick() }
    Box(m.padding(horizontal = 14.dp, vertical = 6.dp), contentAlignment = Alignment.Center) {
        Text(text, fontFamily = Brutal.MonoFont, fontSize = fontSize.sp,
            fontWeight = FontWeight.Bold, color = textColor)
    }
}

/** Card container. */
@Composable
fun BrutalCard(
    modifier: Modifier = Modifier,
    fill: Color = Brutal.White,
    corner: Dp = 14.dp,
    padding: Dp = 16.dp,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(modifier.brutal(fill = fill, corner = corner).padding(padding), content = content)
}

/** Stat bar (Hunger / XP / Energy) with striped-look fill. */
@Composable
fun BrutalBar(
    label: String,
    percent: Int,
    barColor: Color,
    modifier: Modifier = Modifier,
    trailing: String = "$percent%"
) {
    Column(modifier.brutal(fill = Brutal.White, corner = 10.dp, shadowOffset = 3.dp).padding(10.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(label, fontFamily = Brutal.UiFont, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Spacer(Modifier.weight(1f))
            Text(trailing, fontFamily = Brutal.MonoFont, fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(6.dp))
        Box(
            Modifier.fillMaxWidth().height(12.dp)
                .brutal(fill = Brutal.White, corner = 6.dp, stroke = 2.dp, shadowOffset = 0.dp)
        ) {
            Box(
                Modifier
                    .fillMaxWidth(percent.coerceIn(0, 100) / 100f)
                    .fillMaxHeight()
                    .padding(2.dp)
                    .background(barColor, RoundedCornerShape(4.dp))
            )
        }
    }
}

/** Screen-top step progress strip used across onboarding. */
@Composable
fun StepProgress(step: Int, total: Int, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        Text("STEP $step OF $total", fontFamily = Brutal.MonoFont, fontSize = 10.sp,
            fontWeight = FontWeight.Bold, color = Brutal.Ink)
        Spacer(Modifier.height(6.dp))
        Box(Modifier.fillMaxWidth().height(10.dp)
            .brutal(fill = Brutal.White, corner = 5.dp, stroke = 2.dp, shadowOffset = 0.dp)) {
            Box(
                Modifier.fillMaxWidth(step / total.toFloat()).fillMaxHeight()
                    .padding(2.dp).background(Brutal.Yellow, RoundedCornerShape(3.dp))
            )
        }
    }
}
