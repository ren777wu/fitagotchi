package com.fitagotchi.app.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.text.font.Font
import com.fitagotchi.app.R

object Brutal {
    val Cream = Color(0xFFF5F7F2)
    val Ink = Color(0xFF000000)
    val Yellow = Color(0xFFFFD23D)
    val Mint = Color(0xFF7BEEB8)
    val Pink = Color(0xFFFF8B8B)
    val Lavender = Color(0xFFA3B5FF)
    val Gold = Color(0xFFD4AF37)
    val White = Color(0xFFFFFFFF)
    val DarkGreen = Color(0xFF0F5132)
    val DangerRed = Color(0xFF8B2E2E)

    val UiFont: FontFamily = FontFamily(Font(R.font.quicksand_bold, FontWeight.Bold))
    val MonoFont: FontFamily = FontFamily(Font(R.font.space_mono), Font(R.font.space_mono_bold, FontWeight.Bold))

    val HeavyWeight = FontWeight.Bold
}

fun Modifier.brutal(
    fill: Color = Brutal.White,
    corner: Dp = 12.dp,
    stroke: Dp = 3.dp,
    shadowOffset: Dp = 4.dp
): Modifier {
    val shape: Shape = if (corner > 0.dp) RoundedCornerShape(corner) else RectangleShape
    return this
        .drawBehind {
            val off = shadowOffset.toPx()
            val r = corner.toPx()
            val path = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = off, top = off,
                        right = size.width + off, bottom = size.height + off,
                        cornerRadius = CornerRadius(r, r)
                    )
                )
            }
            drawPath(path, Brutal.Ink) // hard shadow: 100% black, 0 blur
        }
        .background(fill, shape)
        .border(stroke, Brutal.Ink, shape)
}
