package com.transitline.istanbul.ui.metro

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextMeasurer
import androidx.compose.ui.text.drawText
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.dp
import com.transitline.istanbul.domain.model.MetroNetwork
import com.transitline.istanbul.domain.model.MetroStation
import kotlin.math.atan2
import kotlin.math.hypot

/**
 * Schematic metro map on a single Compose Canvas.
 *
 * Performance strategy:
 *  - The data carries normalized (0..1) coordinates; only the cheap affine
 *    map to pixels happens per frame, for the ~visible nodes (offscreen nodes
 *    are culled).
 *  - Pan/zoom live in plain state read ONLY inside the draw lambda, so a gesture
 *    invalidates the draw phase, never recomposition of the tree.
 *  - Glyph paths are built from primitives (no shadows, no blur, no layers) and
 *    label layouts are measured once and cached, so there is no per-frame text
 *    measuring or allocation churn. This keeps it smooth at 90/120 Hz.
 */
@Composable
fun MetroCanvas(
    network: MetroNetwork,
    selectedStationId: String?,
    originId: String?,
    destinationId: String?,
    nearestStationId: String?,
    onStationTap: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = MaterialTheme.colorScheme
    val textMeasurer = rememberTextMeasurer()
    val labelStyle = MaterialTheme.typography.labelMedium
    val labelCache = remember(network, labelStyle) { HashMap<String, TextLayoutResult>() }

    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }

    Canvas(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(network) {
                detectTapGestures(
                    onDoubleTap = {
                        scale = 1f
                        offset = Offset.Zero
                    },
                    onTap = { tap ->
                        val margin = 56.dp.toPx()
                        val touch = 22.dp.toPx()
                        hitTest(tap, network, size.width.toFloat(), size.height.toFloat(), margin, scale, offset, touch)
                            ?.let(onStationTap)
                    },
                )
            }
            .pointerInput(Unit) {
                detectTransformGestures { centroid, pan, zoom, _ ->
                    val newScale = (scale * zoom).coerceIn(0.6f, 6f)
                    val z = newScale / scale
                    offset = centroid - (centroid - offset) * z + pan
                    scale = newScale
                }
            },
    ) {
        val margin = 56.dp.toPx()
        val contentW = size.width - 2 * margin
        val contentH = size.height - 2 * margin
        val stationR = 6.5.dp.toPx()
        val strokeW = 8.dp.toPx()
        val diamondR = 11.dp.toPx()
        val pillThickness = 13.dp.toPx()

        fun screenOf(st: MetroStation): Offset {
            val base = Offset(margin + st.x * contentW, margin + st.y * contentH)
            return base * scale + offset
        }

        // 1) Lines — rounded caps and joins, official colors.
        network.lines.forEach { line ->
            val pts = line.stationIds.mapNotNull { network.stationsById[it] }.map { screenOf(it) }
            if (pts.size >= 2) {
                val path = Path().apply {
                    moveTo(pts[0].x, pts[0].y)
                    for (i in 1 until pts.size) lineTo(pts[i].x, pts[i].y)
                }
                drawPath(
                    path = path,
                    color = Color(line.colorArgb),
                    style = Stroke(width = strokeW, cap = androidx.compose.ui.graphics.StrokeCap.Round, join = androidx.compose.ui.graphics.StrokeJoin.Round),
                )
            }
        }

        // 2) Walking transfers — pill with three dots.
        network.walkTransfers.forEach { wt ->
            val a = network.stationsById[wt.fromStationId]?.let(::screenOf) ?: return@forEach
            val b = network.stationsById[wt.toStationId]?.let(::screenOf) ?: return@forEach
            drawWalkPill(a, b, pillThickness, colors.surface, colors.outline)
        }

        // 3) Station nodes + highlights + labels.
        network.stations.forEach { st ->
            val c = screenOf(st)
            if (c.x < -64f || c.y < -64f || c.x > size.width + 64f || c.y > size.height + 64f) return@forEach

            val onLines = network.linesFor(st.id)
            val interchange = onLines.size >= 2

            // Selection / nearest / route rings, drawn under the glyph.
            if (st.id == nearestStationId) drawRing(c, diamondR + 9.dp.toPx(), colors.tertiary, 3.dp.toPx())
            if (st.id == originId) drawRing(c, diamondR + 6.dp.toPx(), colors.primary, 4.dp.toPx())
            if (st.id == destinationId) drawRing(c, diamondR + 6.dp.toPx(), colors.error, 4.dp.toPx())
            if (st.id == selectedStationId) drawRing(c, diamondR + 12.dp.toPx(), colors.primary, 2.5.dp.toPx())

            if (interchange) {
                drawRoundedDiamond(c, diamondR, colors.surface, colors.onSurface, 3.dp.toPx())
            } else {
                val ringColor = onLines.firstOrNull()?.let { Color(it.colorArgb) } ?: colors.onSurface
                drawStationDot(c, stationR, ringColor, colors.surface)
            }

            // Labels: measured once, cached, culled with the node.
            val layout = labelCache.getOrPut(st.id) { textMeasurer.measure(st.name, labelStyle) }
            drawText(
                textLayoutResult = layout,
                color = colors.onSurface,
                topLeft = Offset(c.x + diamondR + 6.dp.toPx(), c.y - layout.size.height / 2f),
            )
        }
    }
}

private fun DrawScope.drawStationDot(center: Offset, radius: Float, ring: Color, fill: Color) {
    drawCircle(color = ring, radius = radius, center = center)
    drawCircle(color = fill, radius = radius * 0.46f, center = center)
}

private fun DrawScope.drawRoundedDiamond(
    center: Offset,
    half: Float,
    fill: Color,
    stroke: Color,
    strokeWidth: Float,
) {
    // A rounded square rotated 45° reads as a "rounded diamond" interchange.
    val side = half * 1.5f
    rotate(degrees = 45f, pivot = center) {
        val topLeft = Offset(center.x - side, center.y - side)
        val size = androidx.compose.ui.geometry.Size(side * 2, side * 2)
        val corner = androidx.compose.ui.geometry.CornerRadius(side * 0.45f, side * 0.45f)
        drawRoundRect(color = fill, topLeft = topLeft, size = size, cornerRadius = corner)
        drawRoundRect(
            color = stroke,
            topLeft = topLeft,
            size = size,
            cornerRadius = corner,
            style = Stroke(width = strokeWidth),
        )
    }
}

private fun DrawScope.drawWalkPill(a: Offset, b: Offset, thickness: Float, fill: Color, stroke: Color) {
    val length = hypot((b.x - a.x), (b.y - a.y))
    val angle = Math.toDegrees(atan2((b.y - a.y), (b.x - a.x)).toDouble()).toFloat()
    val mid = Offset((a.x + b.x) / 2f, (a.y + b.y) / 2f)
    val pad = thickness
    rotate(degrees = angle, pivot = mid) {
        val topLeft = Offset(mid.x - length / 2f - pad, mid.y - thickness / 2f)
        val size = androidx.compose.ui.geometry.Size(length + pad * 2, thickness)
        val corner = androidx.compose.ui.geometry.CornerRadius(thickness / 2f, thickness / 2f)
        drawRoundRect(color = fill, topLeft = topLeft, size = size, cornerRadius = corner)
        drawRoundRect(color = stroke, topLeft = topLeft, size = size, cornerRadius = corner, style = Stroke(width = 2.5.dp.toPx()))
        // Three dots indicating a short walk.
        val dotR = thickness * 0.14f
        for (i in -1..1) {
            drawCircle(color = stroke, radius = dotR, center = Offset(mid.x + i * thickness * 0.7f, mid.y))
        }
    }
}

private fun DrawScope.drawRing(center: Offset, radius: Float, color: Color, width: Float) {
    drawCircle(color = color, radius = radius, center = center, style = Stroke(width = width))
}

/** Returns the id of the station nearest the tap within [touchRadius], else null. */
private fun hitTest(
    tap: Offset,
    network: MetroNetwork,
    width: Float,
    height: Float,
    margin: Float,
    scale: Float,
    offset: Offset,
    touchRadius: Float,
): String? {
    val contentW = width - 2 * margin
    val contentH = height - 2 * margin
    var bestId: String? = null
    var bestDist = touchRadius
    network.stations.forEach { st ->
        val base = Offset(margin + st.x * contentW, margin + st.y * contentH)
        val screen = base * scale + offset
        val d = hypot(tap.x - screen.x, tap.y - screen.y)
        if (d < bestDist) {
            bestDist = d
            bestId = st.id
        }
    }
    return bestId
}
