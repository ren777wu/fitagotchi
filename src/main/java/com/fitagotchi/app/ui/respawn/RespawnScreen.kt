package com.fitagotchi.app.ui.onboarding

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.snapping.rememberSnapFlingBehavior
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.fitagotchi.app.ui.theme.Brutal
import com.fitagotchi.app.ui.theme.brutal
import kotlinx.coroutines.flow.distinctUntilChanged

private const val ROW_HEIGHT_DP = 44

/** Vertical wheel used by the Birth Year screen. */
@Composable
fun VerticalWheel(
    values: List<Int>,
    selected: Int,
    onSelected: (Int) -> Unit,
    modifier: Modifier = Modifier,
    format: (Int) -> String = { it.toString() }
) {
    val startIndex = values.indexOf(selected).coerceAtLeast(0)
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIndex)
    val fling = rememberSnapFlingBehavior(lazyListState = listState)

    // Center item = firstVisible + half-offset correction after snapping.
    LaunchedEffect(listState) {
        snapshotFlow {
            val i = listState.firstVisibleItemIndex
            val off = listState.firstVisibleItemScrollOffset
            if (off > with(listState.layoutInfo) { (visibleItemsInfo.firstOrNull()?.size ?: 1) / 2 }) i + 1 else i
        }.distinctUntilChanged().collect { idx ->
            values.getOrNull(idx)?.let(onSelected)
        }
    }

    Box(modifier.height((ROW_HEIGHT_DP * 5).dp), contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState,
            flingBehavior = fling,
            modifier = Modifier.fillMaxSize(),
            // Pad by 2 rows so first/last values can reach the center line.
            contentPadding = PaddingValues(vertical = (ROW_HEIGHT_DP * 2).dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            items(values.size) { i ->
                val v = values[i]
                val isSel = v == selected
                Box(Modifier.height(ROW_HEIGHT_DP.dp), contentAlignment = Alignment.Center) {
                    if (isSel) {
                        Box(
                            Modifier
                                .padding(horizontal = 40.dp)
                                .brutal(fill = Brutal.Cream, corner = 8.dp, stroke = 3.dp, shadowOffset = 3.dp)
                                .padding(horizontal = 24.dp, vertical = 6.dp)
                        ) {
                            Text(format(v), fontFamily = Brutal.MonoFont, fontSize = 22.sp,
                                fontWeight = FontWeight.Bold, color = Brutal.Ink)
                        }
                    } else {
                        Text(
                            format(v), fontFamily = Brutal.MonoFont, fontSize = 18.sp,
                            color = Brutal.Ink.copy(alpha = 0.30f)
                        )
                    }
                }
            }
        }
        // Fixed black selection arrow on the left, pointing at the center row.
        Canvas(Modifier.align(Alignment.CenterStart).padding(start = 10.dp).size(16.dp)) {
            val p = Path().apply {
                moveTo(0f, 0f); lineTo(size.width, size.height / 2f); lineTo(0f, size.height); close()
            }
            drawPath(p, Brutal.Ink)
        }
    }
}

@Composable
private fun rememberTickSync(
    listState: LazyListState,
    ticks: List<Double>,
    onTick: (Double) -> Unit
) {
    LaunchedEffect(listState, ticks) {
        snapshotFlow {
            val info = listState.layoutInfo
            val center = (info.viewportStartOffset + info.viewportEndOffset) / 2
            info.visibleItemsInfo.minByOrNull {
                kotlin.math.abs((it.offset + it.size / 2) - center)
            }?.index
        }.distinctUntilChanged().collect { idx ->
            if (idx != null) ticks.getOrNull(idx)?.let(onTick)
        }
    }
}

@Composable
private fun SyncRulerToValue(
    listState: LazyListState,
    ticks: List<Double>,
    step: Double,
    value: Double,
    lastEmitted: () -> Double,
    markSynced: (Double) -> Unit
) {
    LaunchedEffect(value, ticks) {
        if (kotlin.math.abs(value - lastEmitted()) > step / 2) {
            val idx = ticks.indices.minByOrNull { kotlin.math.abs(ticks[it] - value) }
                ?: return@LaunchedEffect
            markSynced(ticks[idx])
            listState.scrollToItem(idx) // instant jump to the typed value
        }
    }
}

@Composable
fun HorizontalRuler(
    min: Double, max: Double, step: Double,
    value: Double,
    onValue: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    val ticks = remember(min, max, step) {
        generateSequence(min) { it + step }.takeWhile { it <= max + 1e-9 }.toList()
    }
    val startIdx = remember(ticks, value) {
        ticks.indices.minByOrNull { kotlin.math.abs(ticks[it] - value) } ?: 0
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIdx)
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    var lastEmitted by remember { mutableStateOf(value) }
    rememberTickSync(listState, ticks) { tick -> lastEmitted = tick; onValue(tick) }
    SyncRulerToValue(listState, ticks, step, value, { lastEmitted }, { lastEmitted = it })

    Box(modifier.height(90.dp), contentAlignment = Alignment.Center) {
        LazyRow(
            state = listState, flingBehavior = fling,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 150.dp),
            verticalAlignment = Alignment.Bottom
        ) {
            items(ticks.size) { i ->
                val major = i % 5 == 0
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    if (major) Text(
                        "${ticks[i].toInt()}", fontFamily = Brutal.MonoFont,
                        fontSize = 10.sp, color = Brutal.Ink
                    )
                    Canvas(Modifier.width(12.dp).height(if (major) 40.dp else 24.dp)) {
                        drawLine(
                            Brutal.Ink,
                            Offset(size.width / 2, 0f),
                            Offset(size.width / 2, size.height),
                            strokeWidth = if (major) 6f else 3f
                        )
                    }
                }
            }
        }
        // Fixed red-triangle style indicator (black per design) above center.
        Canvas(Modifier.align(Alignment.TopCenter).size(18.dp)) {
            val p = Path().apply {
                moveTo(0f, 0f); lineTo(size.width, 0f); lineTo(size.width / 2f, size.height); close()
            }
            drawPath(p, Brutal.Pink)
        }
    }
}

@Composable
fun VerticalRuler(
    min: Double, max: Double, step: Double,
    value: Double,
    onValue: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    // Descending so bigger numbers sit at the top like a wall chart.
    val ticks = remember(min, max, step) {
        generateSequence(max) { it - step }.takeWhile { it >= min - 1e-9 }.toList()
    }
    val startIdx = remember(ticks, value) {
        ticks.indices.minByOrNull { kotlin.math.abs(ticks[it] - value) } ?: 0
    }
    val listState = rememberLazyListState(initialFirstVisibleItemIndex = startIdx)
    val fling = rememberSnapFlingBehavior(lazyListState = listState)
    var lastEmitted by remember { mutableStateOf(value) }
    rememberTickSync(listState, ticks) { tick -> lastEmitted = tick; onValue(tick) }
    SyncRulerToValue(listState, ticks, step, value, { lastEmitted }, { lastEmitted = it })

    Box(modifier.width(120.dp).height(240.dp), contentAlignment = Alignment.Center) {
        LazyColumn(
            state = listState, flingBehavior = fling,
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(vertical = 110.dp),
            horizontalAlignment = Alignment.End
        ) {
            items(ticks.size) { i ->
                val major = i % 5 == 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (major) Text(
                        "${ticks[i].toInt()} ", fontFamily = Brutal.MonoFont,
                        fontSize = 10.sp, color = Brutal.Ink
                    )
                    Canvas(Modifier.height(12.dp).width(if (major) 40.dp else 24.dp)) {
                        drawLine(
                            Brutal.Ink,
                            Offset(0f, size.height / 2),
                            Offset(size.width, size.height / 2),
                            strokeWidth = if (major) 6f else 3f
                        )
                    }
                }
            }
        }
        // Fixed arrow at center-left pointing right at the selected tick.
        Canvas(Modifier.align(Alignment.CenterStart).size(14.dp)) {
            val p = Path().apply {
                moveTo(0f, 0f); lineTo(size.width, size.height / 2f); lineTo(0f, size.height); close()
            }
            drawPath(p, Brutal.Mint)
        }
    }
}
