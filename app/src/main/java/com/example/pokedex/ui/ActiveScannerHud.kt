package com.example.pokedex.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.pokedex.data.PokemonEntity
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
    onGalleryClick: () -> Unit,
    onManualLock: () -> Unit = {},
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
    val isLocked = scanProgress >= 1.0f || scanStreak >= 10 || scanStatusText.contains("LOCKED", ignoreCase = true)

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

    Box(modifier = modifier.fillMaxSize()) {
        // --- CENTER HUD VIEWPORT (280.dp matching the 70% camera analysis bounds) ---
        Box(
            modifier = Modifier
                .size(280.dp)
                .align(Alignment.Center)
                .clickable { onManualLock() }
        ) {
            // A. Canvas Layer: Corner Brackets, Poké Ball Reticle, Radar Rings, and Laser Scanline
            Canvas(modifier = Modifier.fillMaxSize()) {
                val w = size.width
                val h = size.height
                val center = Offset(w / 2f, h / 2f)
                val outerRadius = w / 2f * 0.92f
                val pokeRadius = w / 2f * 0.82f
                val buttonRadius = 24.dp.toPx()
                val innerButtonRadius = 13.dp.toPx()

                // 1. Chaotic Sci-Fi Corner Brackets
                val bracketLen = 32.dp.toPx()
                val bracketStroke = 3.dp.toPx()
                val bracketColor = reticleColor.copy(alpha = 0.85f)

                // Top-Left
                drawLine(bracketColor, Offset(0f, 0f), Offset(bracketLen, 0f), bracketStroke)
                drawLine(bracketColor, Offset(0f, 0f), Offset(0f, bracketLen), bracketStroke)
                // Top-Right
                drawLine(bracketColor, Offset(w, 0f), Offset(w - bracketLen, 0f), bracketStroke)
                drawLine(bracketColor, Offset(w, 0f), Offset(w, bracketLen), bracketStroke)
                // Bottom-Left
                drawLine(bracketColor, Offset(0f, h), Offset(bracketLen, h), bracketStroke)
                drawLine(bracketColor, Offset(0f, h), Offset(0f, h - bracketLen), bracketStroke)
                // Bottom-Right
                drawLine(bracketColor, Offset(w, h), Offset(w - bracketLen, h), bracketStroke)
                drawLine(bracketColor, Offset(w, h), Offset(w, h - bracketLen), bracketStroke)

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
                            style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
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
                    style = Stroke(width = 3.dp.toPx())
                )

                // Poké Ball Equator Horizontal Band (with gap for center button)
                val gap = buttonRadius * 1.25f
                // Left Equator
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x - pokeRadius, center.y),
                    end = Offset(center.x - gap, center.y),
                    strokeWidth = 3.dp.toPx()
                )
                // Right Equator
                drawLine(
                    color = reticleColor,
                    start = Offset(center.x + gap, center.y),
                    end = Offset(center.x + pokeRadius, center.y),
                    strokeWidth = 3.dp.toPx()
                )

                // Poké Ball Center Button Outer Ring
                drawCircle(
                    color = reticleColor,
                    radius = buttonRadius,
                    center = center,
                    style = Stroke(width = 2.5.dp.toPx())
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
                    startY = laserY - 14.dp.toPx(),
                    endY = laserY + 14.dp.toPx()
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
                        topLeft = Offset(0f, laserY - 12.dp.toPx()),
                        size = Size(w, 24.dp.toPx())
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
                            .size(120.dp)
                            .align(Alignment.Center)
                            .graphicsLayer {
                                alpha = if (isLocked) 1.0f else holoAlpha
                            }
                    )
                }
            }

            // C. Chaotic Telemetry Corner Readouts (Floating outside reticle corners)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .align(Alignment.TopCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "SYS: SCAN 894MHz",
                    color = reticleColor.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = if (isLockingOn) "LOCK: [${minOf(18, scanStreak)}/18]" else "RADAR: ACTIVE",
                    color = reticleColor.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 6.dp, vertical = 6.dp)
                    .align(Alignment.BottomCenter),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = candidatePokemon?.let { "#%04d".format(it.id) } ?: "TARGET: --",
                    color = reticleColor.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
                Text(
                    text = "FOV: 70% CROP",
                    color = reticleColor.copy(alpha = 0.75f),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = FontFamily.Monospace
                )
            }
        }

        // --- BOTTOM STATUS & PROGRESS BADGE ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 28.dp)
        ) {
            // Invizimals 3-Pip Countdown Tracker (Lights up 3... 2... 1...)
            if (isLockingOn) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(bottom = 8.dp)
                ) {
                    val pip1 = scanProgress >= 0.33f
                    val pip2 = scanProgress >= 0.66f
                    val pip3 = scanProgress >= 0.95f

                    LockPip(isActive = pip1, color = reticleColor)
                    LockPip(isActive = pip2, color = reticleColor)
                    LockPip(isActive = pip3, color = reticleColor)
                }
            }

            // High-Contrast Pill Status Badge (Tap to lock immediately)
            Surface(
                color = Color.Black.copy(alpha = 0.88f),
                shape = RoundedCornerShape(24.dp),
                border = androidx.compose.foundation.BorderStroke(2.dp, reticleColor),
                shadowElevation = 8.dp,
                modifier = Modifier.clickable { onManualLock() }
            ) {
                Text(
                    text = scanStatusText,
                    color = reticleColor,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 10.dp)
                )
            }

            // Live Telemetry Readout
            if (scannerTelemetry.isNotBlank()) {
                Text(
                    text = scannerTelemetry,
                    color = reticleColor.copy(alpha = 0.85f),
                    fontSize = 11.sp,
                    fontFamily = FontFamily.Monospace,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }

        // --- GALLERY FAB ---
        FloatingActionButton(
            onClick = onGalleryClick,
            containerColor = Color(0xFF00FFCC),
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp)
        ) {
            Text(
                "GALLERY",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
            )
        }

        // --- 1-TAP MANUAL LOCK FAB ---
        FloatingActionButton(
            onClick = onManualLock,
            containerColor = Color(0xFFFFCC00),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Text(
                "LOCK",
                color = Color.Black,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
            )
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
