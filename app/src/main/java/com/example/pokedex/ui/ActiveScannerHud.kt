package com.example.pokedex.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokedex.camera.CameraXController
import com.example.pokedex.camera.FocusState
import com.example.pokedex.camera.PokemonFormDetector
import com.example.pokedex.data.PokemonEntity
import kotlin.math.abs
import kotlin.math.sin

/**
 * High-tech active scanner HUD inspired by:
 * - Chaotic animated series scanner (vertical oscillating laser scanline, sci-fi telemetry brackets)
 * - Invizimals (PSP) (dynamic rotating radar rings, concentric capture ring acceleration)
 * - Authentic Holographic Poké Ball motif with center core pulse & creature manifestation
 */
@Composable
fun ActiveScannerHud(
    scanStatusText: String,
    scanStreak: Int,
    scanProgress: Float = 0f,
    scannerTelemetry: String = "",
    candidatePokemon: PokemonEntity?,
    candidateForms: List<PokemonEntity> = emptyList(),
    isCandidateShiny: Boolean = false,
    onSelectForm: ((PokemonEntity) -> Unit)? = null,
    onToggleShiny: (() -> Unit)? = null,
    onGalleryClick: () -> Unit,
    onManualLock: () -> Unit = {},
    controller: CameraXController? = null,
    modifier: Modifier = Modifier
) {
    // 1. Dynamic Status Colors
    val reticleColor = when {
        scanStatusText.contains("LOCKED", ignoreCase = true) -> Color(0xFF00FF66) // Neon Green
        scanStatusText.contains("LOCKING", ignoreCase = true) -> Color(0xFFFF9900) // Vivid Orange
        scanStatusText.contains("ACQUIRING", ignoreCase = true) -> Color(0xFFFFCC00) // Amber Gold
        else -> Color(0xFF00FFCC) // Cyber Cyan
    }

    val isLockingOn = scanStreak > 0
    val isLocked = scanProgress >= 1.0f || scanStatusText.contains("LOCKED", ignoreCase = true)

    // 2. Animated Transitions
    val infiniteTransition = rememberInfiniteTransition(label = "scanner_animations")

    // Chaotic Laser Scanline: Sweeps up & down
    val laserPos by infiniteTransition.animateFloat(
        initialValue = 0.05f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "chaotic_laser"
    )

    // Invizimals Outer Radar Rotation: baseline rotation
    val baseRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = if (isLockingOn) 2200 else 6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "radar_rotation"
    )

    // Invizimals Concentric Ring Contraction (pulses inward when locking on)
    val innerRingPulse by infiniteTransition.animateFloat(
        initialValue = 0.98f,
        targetValue = 0.88f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    scanStreak >= 15 -> 220
                    scanStreak >= 10 -> 360
                    scanStreak >= 5 -> 500
                    else -> 800
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "inner_ring_pulse"
    )

    // Core Poké Ball Button Pulse
    val corePulse by infiniteTransition.animateFloat(
        initialValue = 0.82f,
        targetValue = 1.18f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when {
                    isLocked -> 150
                    scanStreak >= 15 -> 220
                    scanStreak >= 10 -> 380
                    scanStreak >= 5 -> 550
                    else -> 950
                },
                easing = FastOutSlowInEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    // Holographic shimmer for detected Pokémon
    val holoAlpha by infiniteTransition.animateFloat(
        initialValue = 0.60f,
        targetValue = 0.95f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "holo_shimmer"
    )

    val context = LocalContext.current

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(controller) {
                if (controller != null) {
                    detectTransformGestures { _, _, zoomDelta, _ ->
                        if (zoomDelta != 1.0f) {
                            controller.applyZoomDelta(zoomDelta)
                        }
                    }
                }
            }
            .pointerInput(controller) {
                if (controller != null) {
                    detectTapGestures(
                        onTap = { offset ->
                            controller.startFocusAndMetering(offset, context)
                        }
                    )
                }
            }
    ) {
        // --- TOP-RIGHT TORCH / FLASHLIGHT TOGGLE ---
        if (controller != null && controller.hasFlashUnit) {
            TorchButton(
                isTorchOn = controller.isTorchOn,
                onToggle = { controller.toggleTorch() },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 16.dp, end = 16.dp)
            )
        }
        // --- CENTER HUD VIEWPORT (240.dp enlarged target centered in open camera viewport) ---
        Box(
            modifier = Modifier
                .size(240.dp)
                .align(Alignment.Center)
                .offset(y = (-32).dp)
                .background(Color.Black.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
                .border(1.5.dp, reticleColor.copy(alpha = 0.50f), RoundedCornerShape(14.dp))
                .clickable { onManualLock() }
        ) {
            // A. Canvas Layer: Corner Brackets, Poké Ball Reticle, Radar Rings, and Laser Scanline
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val outerRadius = w / 2f * 0.82f
                val pokeRadius = w / 2f * 0.70f
                val buttonRadius = 16.dp.toPx()
                val innerButtonRadius = 9.dp.toPx()

                // 1. Inset Sci-Fi Corner Brackets (Smoothly Rounded, Concentric Curves)
                val inset = 5.dp.toPx()
                val bracketLen = 24.dp.toPx()
                val bracketStroke = 2.5.dp.toPx()
                val bracketColor = reticleColor.copy(alpha = 0.85f)
                val cornerRadius = 9.dp.toPx()
                val bracketStyle = Stroke(
                    width = bracketStroke,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                )

                // Top-Left Rounded Bracket
                val pathTL = Path().apply {
                    moveTo(inset, inset + bracketLen)
                    lineTo(inset, inset + cornerRadius)
                    arcTo(
                        rect = Rect(inset, inset, inset + 2 * cornerRadius, inset + 2 * cornerRadius),
                        startAngleDegrees = 180f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(inset + bracketLen, inset)
                }
                drawPath(pathTL, bracketColor, style = bracketStyle)

                // Top-Right Rounded Bracket
                val pathTR = Path().apply {
                    moveTo(w - inset - bracketLen, inset)
                    lineTo(w - inset - cornerRadius, inset)
                    arcTo(
                        rect = Rect(w - inset - 2 * cornerRadius, inset, w - inset, inset + 2 * cornerRadius),
                        startAngleDegrees = 270f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(w - inset, inset + bracketLen)
                }
                drawPath(pathTR, bracketColor, style = bracketStyle)

                // Bottom-Right Rounded Bracket
                val pathBR = Path().apply {
                    moveTo(w - inset, h - inset - bracketLen)
                    lineTo(w - inset, h - inset - cornerRadius)
                    arcTo(
                        rect = Rect(w - inset - 2 * cornerRadius, h - inset - 2 * cornerRadius, w - inset, h - inset),
                        startAngleDegrees = 0f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(w - inset - bracketLen, h - inset)
                }
                drawPath(pathBR, bracketColor, style = bracketStyle)

                // Bottom-Left Rounded Bracket
                val pathBL = Path().apply {
                    moveTo(inset + bracketLen, h - inset)
                    lineTo(inset + cornerRadius, h - inset)
                    arcTo(
                        rect = Rect(inset, h - inset - 2 * cornerRadius, inset + 2 * cornerRadius, h - inset),
                        startAngleDegrees = 90f,
                        sweepAngleDegrees = 90f,
                        forceMoveTo = false
                    )
                    lineTo(inset, h - inset - bracketLen)
                }
                drawPath(pathBL, bracketColor, style = bracketStyle)

                // 2. Invizimals Outer Rotating Radar Ticks
                rotate(degrees = baseRotation, pivot = center) {
                    val segments = 8
                    val sweepAngle = 24f
                    for (i in 0 until segments) {
                        val startAngle = i * (360f / segments)
                        drawArc(
                            color = reticleColor.copy(alpha = 0.50f),
                            startAngle = startAngle,
                            sweepAngle = sweepAngle,
                            useCenter = false,
                            topLeft = Offset(center.x - outerRadius, center.y - outerRadius),
                            size = Size(outerRadius * 2, outerRadius * 2),
                            style = Stroke(width = 2.25.dp.toPx(), cap = StrokeCap.Round)
                        )
                    }
                }

                // 3. Concentric Inner Targeting Ring (Contracts when acquiring)
                if (isLockingOn) {
                    val dynamicInnerRadius = pokeRadius * innerRingPulse
                    rotate(degrees = -baseRotation * 1.5f, pivot = center) {
                        drawCircle(
                            color = reticleColor.copy(alpha = 0.35f),
                            radius = dynamicInnerRadius,
                            center = center,
                            style = Stroke(
                                width = 1.5.dp.toPx(),
                                pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 12f), 0f)
                            )
                        )
                    }
                }

                // 4. Holographic Poké Ball Reticle
                // Top hemisphere cyber-glow (reddish/warm tint)
                val topPath = Path().apply {
                    addArc(
                        androidx.compose.ui.geometry.Rect(
                            center.x - pokeRadius,
                            center.y - pokeRadius,
                            center.x + pokeRadius,
                            center.y + pokeRadius
                        ),
                        180f,
                        180f
                    )
                    close()
                }
                drawPath(
                    path = topPath,
                    color = Color(0xFFFF3B30).copy(alpha = if (isLockingOn) 0.12f else 0.05f)
                )

                // Bottom hemisphere cyber-glow (cool cyan/white tint)
                val bottomPath = Path().apply {
                    addArc(
                        androidx.compose.ui.geometry.Rect(
                            center.x - pokeRadius,
                            center.y - pokeRadius,
                            center.x + pokeRadius,
                            center.y + pokeRadius
                        ),
                        0f,
                        180f
                    )
                    close()
                }
                drawPath(
                    path = bottomPath,
                    color = Color.White.copy(alpha = 0.04f)
                )

                // Poké Ball Outer Circular Frame
                drawCircle(
                    color = reticleColor,
                    radius = pokeRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
                )

                // Poké Ball Equator Horizontal Band (with gap for center button)
                val gap = buttonRadius * 1.25f
                // Left Equator
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x - pokeRadius, center.y),
                    end = Offset(center.x - gap, center.y),
                    strokeWidth = 2.5.dp.toPx()
                )
                // Right Equator
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x + gap, center.y),
                    end = Offset(center.x + pokeRadius, center.y),
                    strokeWidth = 2.5.dp.toPx()
                )

                // Poké Ball Center Button Outer Ring
                drawCircle(
                    color = reticleColor,
                    radius = buttonRadius,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Poké Ball Center Core Button with dynamic pulse
                val animatedCoreRadius = innerButtonRadius * if (isLocked) 1.25f else corePulse
                drawCircle(
                    color = reticleColor.copy(alpha = 0.25f),
                    radius = animatedCoreRadius * 1.35f,
                    center = center
                )
                drawCircle(
                    color = if (isLocked) Color(0xFF00FF66) else reticleColor,
                    radius = animatedCoreRadius,
                    center = center
                )
                drawCircle(
                    color = Color.White.copy(alpha = 0.85f),
                    radius = animatedCoreRadius * 0.45f,
                    center = center
                )

                // 5. Chaotic Laser Scanline Sweep (Oscillates vertically through the reticle)
                val laserY = h * laserPos
                val scanlineGradient = Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        reticleColor.copy(alpha = 0.50f),
                        reticleColor,
                        reticleColor.copy(alpha = 0.50f),
                        Color.Transparent
                    ),
                    startY = laserY - 12.dp.toPx(),
                    endY = laserY + 12.dp.toPx()
                )

                // Clip laser within the circular Poké Ball viewfinder
                val clipCircle = Path().apply {
                    addOval(
                        androidx.compose.ui.geometry.Rect(
                            center.x - pokeRadius,
                            center.y - pokeRadius,
                            center.x + pokeRadius,
                            center.y + pokeRadius
                        )
                    )
                }

                clipPath(clipCircle) {
                    drawRect(
                        brush = scanlineGradient,
                        topLeft = Offset(0f, laserY - 10.dp.toPx()),
                        size = Size(w, 20.dp.toPx())
                    )
                    // Core high-intensity bright laser line
                    drawLine(
                        color = Color.White.copy(alpha = 0.90f),
                        start = Offset(center.x - pokeRadius, laserY),
                        end = Offset(center.x + pokeRadius, laserY),
                        strokeWidth = 1.5.dp.toPx()
                    )
                }
            }

            // B. Holographic Pokémon Sprite Manifestation (Translucent holographic preview)
            if (candidatePokemon != null && isLockingOn) {
                Box(
                    modifier = Modifier
                        .size(140.dp)
                        .align(Alignment.Center)
                ) {
                    // Soft holographic back-glow aura
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                alpha = holoAlpha * 0.5f
                                scaleX = corePulse
                                scaleY = corePulse
                            }
                            .background(
                                Brush.radialGradient(
                                    listOf(reticleColor.copy(alpha = 0.40f), Color.Transparent)
                                ),
                                shape = CircleShape
                            )
                    )

                    // Translucent Holographic Sprite
                    AsyncImage(
                        model = candidatePokemon.spriteUrl,
                        contentDescription = candidatePokemon.name,
                        modifier = Modifier
                            .size(125.dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                alpha = if (isLocked) 1.0f else holoAlpha
                            }
                    )
                }
            }

            // C. Chaotic Telemetry Corner Readouts (Floating inside reticle corners with ZERO overlap)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SYS: SCAN 894MHz",
                    color = reticleColor.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isLockingOn) "LOCK: [${minOf(18, scanStreak)}/18]" else "RADAR: ACTIVE",
                    color = reticleColor.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val candLabel = candidatePokemon?.let {
                    val formTitle = PokemonFormDetector.formatFormDisplayName(it.name)
                    val shinyTag = if (isCandidateShiny) "✨" else ""
                    "$shinyTag#%04d $formTitle".format(it.id).uppercase()
                } ?: "TARGET: --"
                Text(
                    text = candLabel,
                    color = reticleColor.copy(alpha = 0.90f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace,
                    maxLines = 1,
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false)
                )
                Text(
                    text = "FOV: 70% CROP",
                    color = reticleColor.copy(alpha = 0.85f),
                    fontSize = 8.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- INTERMEDIATE ZONE: FORMS & COUNTDOWN PIPS (Cleanly placed above Zoom Bar) ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 124.dp)
        ) {
            // Quick-Form & Shiny Selector Bar (when forms exist or a candidate is acquired)
            if (candidatePokemon != null && (candidateForms.isNotEmpty() || onToggleShiny != null)) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .padding(bottom = 6.dp)
                        .horizontalScroll(rememberScrollState())
                ) {
                    if (onToggleShiny != null) {
                        Surface(
                            color = if (isCandidateShiny) Color(0xFFFFCC00).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isCandidateShiny) 1.5.dp else 1.dp,
                                color = if (isCandidateShiny) Color(0xFFFFCC00) else Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable { onToggleShiny() }
                        ) {
                            Text(
                                text = if (isCandidateShiny) "✨ SHINY" else "✨ SHINY OFF",
                                color = if (isCandidateShiny) Color(0xFFFFCC00) else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }

                    candidateForms.forEach { form ->
                        val isSelected = candidatePokemon.id == form.id
                        val formName = PokemonFormDetector.formatFormDisplayName(form.name).uppercase()
                        Surface(
                            color = if (isSelected) Color(0xFF00FFCC).copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.65f),
                            shape = RoundedCornerShape(12.dp),
                            border = androidx.compose.foundation.BorderStroke(
                                width = if (isSelected) 1.5.dp else 1.dp,
                                color = if (isSelected) Color(0xFF00FFCC) else Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.clickable { onSelectForm?.invoke(form) }
                        ) {
                            Text(
                                text = formName,
                                color = if (isSelected) Color(0xFF00FFCC) else Color.LightGray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }
            // Invizimals 3-Pip Countdown Tracker (Lights up 3... 2... 1...)
            if (isLockingOn) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val pip1 = scanProgress >= 0.25f
                    val pip2 = scanProgress >= 0.50f
                    val pip3 = scanProgress >= 0.75f

                    LockPip(isActive = pip1, color = reticleColor)
                    LockPip(isActive = pip2, color = reticleColor)
                    LockPip(isActive = pip3, color = reticleColor)
                }
            }
        }

        // --- QUICK-ZOOM BAR (Cleanly floating below reticle with ZERO overlap) ---
        if (controller != null) {
            QuickZoomBar(
                currentZoom = controller.zoomRatio,
                minZoom = controller.minZoomRatio,
                maxZoom = controller.maxZoomRatio,
                onZoomSelected = { targetRatio -> controller.setZoom(targetRatio) },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 72.dp)
            )
        }

        // --- UNIFIED BOTTOM ACTION DOCK (GALLERY, STATUS CAPSULE, LOCK) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 12.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Sleek Compact Gallery Button
            Surface(
                color = Color(0xFF00FFCC),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable { onGalleryClick() }
            ) {
                Text(
                    text = "GALLERY",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                )
            }

            // 2. High-Contrast Center Status Badge
            Surface(
                color = Color.Black.copy(alpha = 0.88f),
                shape = RoundedCornerShape(20.dp),
                border = androidx.compose.foundation.BorderStroke(1.5.dp, reticleColor),
                shadowElevation = 6.dp,
                modifier = Modifier
                    .weight(1f, fill = false)
                    .padding(horizontal = 6.dp)
                    .clickable { onManualLock() }
            ) {
                Text(
                    text = scanStatusText,
                    color = reticleColor,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    maxLines = 1,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }

            // 3. Sleek Compact Manual Lock Button
            Surface(
                color = Color(0xFFFFCC00),
                shape = RoundedCornerShape(12.dp),
                shadowElevation = 4.dp,
                modifier = Modifier.clickable { onManualLock() }
            ) {
                Text(
                    text = "LOCK",
                    color = Color.Black,
                    fontWeight = FontWeight.Black,
                    fontSize = 11.sp,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
                )
            }
        }

        // --- TAP-TO-FOCUS ANIMATED HOLOGRAPHIC RETICLE ---
        val focusPt = controller?.focusPoint
        if (focusPt != null && controller != null) {
            FocusReticle(
                point = focusPt,
                focusState = controller.focusState
            )
            LaunchedEffect(focusPt) {
                kotlinx.coroutines.delay(1600)
                controller.clearFocusIndicator()
            }
        }
    }
}

@Composable
private fun TorchButton(
    isTorchOn: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = if (isTorchOn) Color(0xFFFFCC00) else Color.Black.copy(alpha = 0.75f)
    val borderCol = if (isTorchOn) Color.White else Color(0xFF00FFCC)
    val textCol = if (isTorchOn) Color.Black else Color(0xFF00FFCC)

    Surface(
        color = bg,
        shape = RoundedCornerShape(20.dp),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, borderCol),
        shadowElevation = if (isTorchOn) 8.dp else 2.dp,
        modifier = modifier.clickable { onToggle() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
        ) {
            Text(
                text = "⚡",
                fontSize = 15.sp
            )
            Text(
                text = if (isTorchOn) "TORCH ON" else "TORCH",
                color = textCol,
                fontSize = 12.sp,
                fontWeight = FontWeight.Black
            )
        }
    }
}

@Composable
private fun QuickZoomBar(
    currentZoom: Float,
    minZoom: Float,
    maxZoom: Float,
    onZoomSelected: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val presetSteps = listOf(1.0f, 2.0f, 3.0f, 5.0f).filter { it in minZoom..(maxZoom + 0.1f) }

    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .background(Color.Black.copy(alpha = 0.80f), shape = RoundedCornerShape(24.dp))
            .border(1.2.dp, Color(0xFF00FFCC).copy(alpha = 0.45f), shape = RoundedCornerShape(24.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp)
    ) {
        presetSteps.forEach { step ->
            val isSelected = abs(currentZoom - step) < 0.25f
            val pillBg = if (isSelected) Color(0xFF00FFCC) else Color(0x33FFFFFF)
            val pillTextColor = if (isSelected) Color.Black else Color.White

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 44.dp, height = 30.dp)
                    .background(pillBg, shape = RoundedCornerShape(15.dp))
                    .clickable { onZoomSelected(step) }
            ) {
                Text(
                    text = "${step.toInt()}x",
                    color = pillTextColor,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }

        // If user is at an intermediate pinch zoom (e.g. 2.4x), show exact badge
        val matchesPreset = presetSteps.any { abs(currentZoom - it) < 0.25f }
        if (!matchesPreset) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .height(30.dp)
                    .background(Color(0xFFFFCC00), shape = RoundedCornerShape(15.dp))
                    .padding(horizontal = 8.dp)
            ) {
                Text(
                    text = String.format(java.util.Locale.US, "%.1fx", currentZoom),
                    color = Color.Black,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black
                )
            }
        }
    }
}

@Composable
private fun FocusReticle(
    point: Offset,
    focusState: FocusState,
    modifier: Modifier = Modifier
) {
    var animTrigger by remember(point) { mutableStateOf(false) }
    LaunchedEffect(point) {
        animTrigger = true
    }

    val targetSize by animateDpAsState(
        targetValue = if (animTrigger) 50.dp else 75.dp,
        animationSpec = tween(durationMillis = 250, easing = FastOutSlowInEasing),
        label = "focus_size"
    )

    val color = when (focusState) {
        FocusState.SUCCESS -> Color(0xFF00FF66) // Neon Green
        FocusState.FAILED -> Color(0xFFFF3333) // Vivid Red
        else -> Color(0xFF00FFCC) // Neon Cyan
    }

    val density = LocalDensity.current
    val pxSize = with(density) { targetSize.toPx() }

    Box(
        modifier = modifier
            .offset {
                IntOffset(
                    (point.x - pxSize / 2f).toInt(),
                    (point.y - pxSize / 2f).toInt()
                )
            }
            .size(targetSize)
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 2.5.dp.toPx()
            val bracketLen = size.width * 0.28f

            // Top-Left corner
            drawLine(color, Offset(0f, 0f), Offset(bracketLen, 0f), strokeWidth)
            drawLine(color, Offset(0f, 0f), Offset(0f, bracketLen), strokeWidth)

            // Top-Right corner
            drawLine(color, Offset(size.width, 0f), Offset(size.width - bracketLen, 0f), strokeWidth)
            drawLine(color, Offset(size.width, 0f), Offset(size.width, bracketLen), strokeWidth)

            // Bottom-Left corner
            drawLine(color, Offset(0f, size.height), Offset(bracketLen, size.height), strokeWidth)
            drawLine(color, Offset(0f, size.height), Offset(0f, size.height - bracketLen), strokeWidth)

            // Bottom-Right corner
            drawLine(color, Offset(size.width, size.height), Offset(size.width - bracketLen, size.height), strokeWidth)
            drawLine(color, Offset(size.width, size.height), Offset(size.width, size.height - bracketLen), strokeWidth)

            // Center target crosshair dot
            drawCircle(color, radius = 3.dp.toPx(), center = Offset(size.width / 2f, size.height / 2f))
        }
    }
}

@Composable
private fun LockPip(isActive: Boolean, color: Color) {
    Box(
        modifier = Modifier
            .size(10.dp)
            .background(
                color = if (isActive) color else color.copy(alpha = 0.20f),
                shape = CircleShape
            )
            .border(
                width = 1.5.dp,
                color = if (isActive) Color.White else color.copy(alpha = 0.50f),
                shape = CircleShape
            )
    )
}
