package com.example.pokedex.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.pokedex.viewmodel.PokedexViewModel

data class TutorialCard(
    val stepNumber: Int,
    val iconEmoji: String,
    val badgeLabel: String,
    val title: String,
    val subtitle: String,
    val bullets: List<Pair<String, String>>,
    val highlightColor: Color,
    val isVoiceTestStep: Boolean = false
)

val TUTORIAL_CARDS = listOf(
    TutorialCard(
        stepNumber = 1,
        iconEmoji = "📸",
        badgeLabel = "AI VISION SCANNER & OVERLAY",
        title = "HOW TO SCAN POKÉMON",
        subtitle = "Identify any Pokémon with 100% offline edge neural vision:",
        bullets = listOf(
            "🔵 Blue Header Lens" to "Tap the pulsing blue Pokédex lens at the top of the screen to activate the 240dp live camera scanner. Point your camera at any Pokémon sprite, trading card, or drawing to lock target consensus.",
            "🎯 Floating Battle Overlay" to "Tap '🎯 OVERLAY' in the top header to float the animated Invizimals scanner reticle over emulators (GBA, NDS, Switch). Tap 'SCAN' in-game for instant 4× / 2× vulnerabilities and STAB strengths!",
            "⚡ 100% Offline AI" to "Powered by an unquantized Float32 neural network trained on over 1,000,000 authentic in-game sprites across all 9 console generations. No internet required."
        ),
        highlightColor = Color(0xFF00FFCC)
    ),
    TutorialCard(
        stepNumber = 2,
        iconEmoji = "🧭",
        badgeLabel = "AI QUEST NAVIGATOR",
        title = "ULTIMATE STRATEGY GUIDE",
        subtitle = "Never get lost with comprehensive offline game walkthroughs:",
        bullets = listOf(
            "🧭 Header Guide Hub" to "Tap the cyan '🧭 GUIDE' button in the toolbar anytime to open the AI Quest Navigator and search any game mechanic, item, or route.",
            "📚 13 Strategy Domains" to "Explore 28 Poké Balls, Master Ball targets (with FRLG Roam/Roar bug alerts), and 10 Regional Encyclopedias (such as Area Zero Paradox spawns).",
            "🗣 Dexter Tactical Briefings" to "Tap '🗣 READ GUIDE' inside any guide topic to hear Dexter narrate walkthrough steps and battle advice out loud."
        ),
        highlightColor = Color(0xFF00BFFF)
    ),
    TutorialCard(
        stepNumber = 3,
        iconEmoji = "🎙️",
        badgeLabel = "1997 HARDWARE DSP",
        title = "DEXTER VOICE & VOICE CHANGER",
        subtitle = "Authentic anime voice synthesis with real-time audio filters:",
        bullets = listOf(
            "🔊 Read Aloud" to "Tap '🔊 READ' on any Pokédex entry or guide page to hear Dexter speak canonical lore with phonetic pronunciation.",
            "🎛 Voice Changer" to "Open 'SETTINGS' ➔ 'VOICE' to switch between Original 90s Dexter, Retro 90s Anime Female, Modern HD, or device system voices, with granular Pitch and Speed sliders.",
            "📻 1997 Hardware DSP" to "Toggle the authentic 1997 Hardware DSP filter to apply dual-tap comb echo, bandpass EQ, and tanh saturation for that metallic anime announcer sound."
        ),
        highlightColor = Color(0xFFFFCC00),
        isVoiceTestStep = true
    ),
    TutorialCard(
        stepNumber = 4,
        iconEmoji = "🎮",
        badgeLabel = "37 RETRO CARTRIDGES",
        title = "TACTILE GAME CARTRIDGES",
        subtitle = "Switch cartridges to adapt lore, pixel art, and evolution rules:",
        bullets = listOf(
            "🕹 Tactile Switcher" to "Tap the Cartridge badge (e.g. 'AUTO / DEBUT', 'RED', 'EMERALD', 'SCARLET') at the top of any Pokémon entry to switch versions.",
            "👾 Authentic Pixel Sprites" to "Artwork transforms to that generation's authentic in-game pixel art, from 8-bit Game Boy to 3D Nintendo Switch models.",
            "📜 Version-Specific Evolutions" to "Evolution requirements dynamically update based on the active cartridge (e.g., Feebas Beauty Pokéblocks vs. Prism Scale)."
        ),
        highlightColor = Color(0xFFFF5252)
    ),
    TutorialCard(
        stepNumber = 5,
        iconEmoji = "⚙️",
        badgeLabel = "CUSTOMIZATION & REPLAY",
        title = "SETTINGS & PRO CONTROLS",
        subtitle = "Fine-tune your Pokédex and review this guide anytime:",
        bullets = listOf(
            "⚙️ Settings Menu" to "Tap 'SETTINGS' in the top header to configure scan fanfare, 'Who's That Pokémon?' bumpers, auto-read behavior, and standard game versions.",
            "📱 Foldable Dual-Screen Mode" to "On Samsung Galaxy Fold and wide displays, Bori-Dex automatically expands into a dual-monitor Pokédex with a central metallic hinge.",
            "📖 Replay Tutorial" to "Need a refresher? You can reopen this complete guide anytime by tapping '📖 TRAINER TUTORIAL' inside Settings or the '❓' header button."
        ),
        highlightColor = Color(0xFFB388FF)
    )
)

@Composable
fun TrainerTutorialModal(
    viewModel: PokedexViewModel,
    onDismiss: () -> Unit
) {
    var currentStepIndex by remember { mutableIntStateOf(0) }
    val currentCard = TUTORIAL_CARDS[currentStepIndex]
    val isSpeaking by viewModel.isSpeaking.collectAsState()

    LaunchedEffect(currentStepIndex) {
        viewModel.onTutorialStepChanged(currentStepIndex)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false,
            dismissOnClickOutside = false,
            dismissOnBackPress = true
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.92f),
            shape = RoundedCornerShape(16.dp),
            color = Color(0xFF12161C),
            border = BorderStroke(2.dp, Color(0xFF00FFCC).copy(alpha = 0.85f))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(14.dp)
            ) {
                // 1. Top Header Bar with Indicators and Skip Button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mini glowing status lights
                        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                            Box(modifier = Modifier.size(10.dp).background(Color.Red, CircleShape))
                            Box(modifier = Modifier.size(10.dp).background(Color.Yellow, CircleShape))
                            Box(modifier = Modifier.size(10.dp).background(Color.Green, CircleShape))
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "TRAINER FIELD MANUAL",
                                color = Color(0xFF00FFCC),
                                fontWeight = FontWeight.Black,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "BORI-DEX v2.0.0 • STEP ${currentStepIndex + 1} OF ${TUTORIAL_CARDS.size}",
                                color = Color.Gray,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        TextButton(
                            onClick = onDismiss,
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "SKIP",
                                color = Color.LightGray,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(28.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Text("✕", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 2. Animated Card Body
                AnimatedContent(
                    targetState = currentCard,
                    transitionSpec = {
                        if (targetState.stepNumber > initialState.stepNumber) {
                            (slideInHorizontally { width -> width / 3 } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> -width / 3 } + fadeOut())
                        } else {
                            (slideInHorizontally { width -> -width / 3 } + fadeIn())
                                .togetherWith(slideOutHorizontally { width -> width / 3 } + fadeOut())
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    label = "tutorialCardAnimation"
                ) { card ->
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color(0xFF181E26), RoundedCornerShape(12.dp))
                            .border(1.dp, card.highlightColor.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        // Badge Tag
                        Box(
                            modifier = Modifier
                                .background(card.highlightColor.copy(alpha = 0.20f), RoundedCornerShape(6.dp))
                                .border(1.dp, card.highlightColor, RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(card.iconEmoji, fontSize = 12.sp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = card.badgeLabel,
                                    color = card.highlightColor,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Title & Subtitle
                        Text(
                            text = card.title,
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Black
                        )
                        Spacer(modifier = Modifier.height(3.dp))
                        Text(
                            text = card.subtitle,
                            color = Color.LightGray,
                            fontSize = 11.5.sp,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        // Bullet Features
                        card.bullets.forEach { (heading, explanation) ->
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp)
                                    .background(Color.Black.copy(alpha = 0.35f), RoundedCornerShape(8.dp))
                                    .border(0.5.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = heading,
                                    color = card.highlightColor,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = explanation,
                                    color = Color.White.copy(alpha = 0.90f),
                                    fontSize = 11.sp,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        // Interactive Voice Engine Test Button (Step 3)
                        if (card.isVoiceTestStep) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Button(
                                onClick = { viewModel.testTutorialVoice() },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFCC00)),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(42.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(if (isSpeaking) "🔊" else "🗣️", fontSize = 15.sp)
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = if (isSpeaking) "DEXTER SPEAKING..." else "TEST DEXTER VOICE ENGINE",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.5.sp
                                    )
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Bottom Controls: Progress Dots & Next / Previous Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Back Button
                    Button(
                        onClick = {
                            if (currentStepIndex > 0) {
                                currentStepIndex--
                                viewModel.onTutorialStepChanged(currentStepIndex)
                            }
                        },
                        enabled = currentStepIndex > 0,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF263238),
                            disabledContainerColor = Color.Transparent
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = "◀ PREV",
                            color = if (currentStepIndex > 0) Color.White else Color.Transparent,
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp
                        )
                    }

                    // Progress Dots (Clickable for Direct Navigation)
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        TUTORIAL_CARDS.forEachIndexed { idx, _ ->
                            val isCurrent = idx == currentStepIndex
                            Box(
                                modifier = Modifier
                                    .size(if (isCurrent) 10.dp else 8.dp)
                                    .clip(CircleShape)
                                    .background(if (isCurrent) Color(0xFF00FFCC) else Color.Gray.copy(alpha = 0.5f))
                                    .clickable {
                                        currentStepIndex = idx
                                        viewModel.onTutorialStepChanged(idx)
                                    }
                            )
                        }
                    }

                    // Next / Finish Button
                    val isLastStep = currentStepIndex == TUTORIAL_CARDS.size - 1
                    Button(
                        onClick = {
                            if (isLastStep) {
                                onDismiss()
                            } else {
                                currentStepIndex++
                                viewModel.onTutorialStepChanged(currentStepIndex)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isLastStep) Color(0xFF00FFCC) else currentCard.highlightColor
                        ),
                        shape = RoundedCornerShape(8.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 6.dp),
                        modifier = Modifier.height(38.dp)
                    ) {
                        Text(
                            text = if (isLastStep) "⚡ FINISH" else "NEXT ▶",
                            color = Color.Black,
                            fontWeight = FontWeight.Black,
                            fontSize = 11.5.sp
                        )
                    }
                }
            }
        }
    }
}
