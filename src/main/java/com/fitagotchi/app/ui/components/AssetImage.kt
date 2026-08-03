package com.fitagotchi.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal

@Composable
fun AssetImage(
    asset: String,
    modifier: Modifier = Modifier,
    contentScale: ContentScale = ContentScale.Fit
) {
    val ctx = LocalContext.current
    val resName = asset.removeSuffix(".png").replace('-', '_')
    val id = remember(resName) {
        ctx.resources.getIdentifier(resName, "drawable", ctx.packageName)
    }
    if (id != 0) {
        Image(
            painter = painterResource(id),
            contentDescription = resName,
            modifier = modifier,
            contentScale = contentScale
        )
    } else {
        Box(
            modifier.background(Color(0xFFE5E2D8), RoundedCornerShape(6.dp)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                "$resName.png",
                fontFamily = Brutal.MonoFont, fontSize = 8.sp,
                textAlign = TextAlign.Center,
                color = Brutal.Ink.copy(alpha = 0.45f),
                modifier = Modifier.padding(2.dp)
            )
        }
    }
}

@Composable
fun CoinPill(amount: Int, modifier: Modifier = Modifier, fontSize: Int = 12) {
    Row(
        modifier
            .brutal(fill = Brutal.Ink, corner = 50.dp, stroke = 3.dp, shadowOffset = 2.dp)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Canvas(Modifier.size((fontSize + 1).dp)) {
            drawCircle(Brutal.Gold)
            drawCircle(Color(0xFF8A6D1A), radius = size.minDimension / 2.9f)
        }
        Spacer(Modifier.width(6.dp))
        Text(
            "$amount",
            fontFamily = Brutal.MonoFont, fontWeight = FontWeight.Bold,
            fontSize = fontSize.sp, color = Brutal.White
        )
    }
}
