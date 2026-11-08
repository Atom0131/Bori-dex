package com.example.pokedex

import com.example.pokedex.ui.TUTORIAL_CARDS
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TutorialStateTest {

    @Test
    fun testTutorialCardsIntegrityAndCount() {
        assertEquals("Tutorial must have exactly 5 structured cards", 5, TUTORIAL_CARDS.size)

        TUTORIAL_CARDS.forEachIndexed { index, card ->
            assertEquals("Step number must be sequential (1-based)", index + 1, card.stepNumber)
            assertTrue("Badge label must not be empty", card.badgeLabel.isNotBlank())
            assertTrue("Title must not be empty", card.title.isNotBlank())
            assertTrue("Subtitle must not be empty", card.subtitle.isNotBlank())
            assertTrue("Card must have at least 2 informative bullet points", card.bullets.size >= 2)

            card.bullets.forEach { (heading, explanation) ->
                assertTrue("Bullet heading must not be empty", heading.isNotBlank())
                assertTrue("Bullet explanation must not be empty", explanation.isNotBlank())
            }
        }
    }

    @Test
    fun testVoiceChangerStepHasInteractiveVoiceTest() {
        val voiceCard = TUTORIAL_CARDS.find { it.stepNumber == 3 }
        assertNotNull("Step 3 (Voice Changer) must exist", voiceCard)
        assertTrue("Step 3 must feature interactive voice engine testing", voiceCard!!.isVoiceTestStep)
        assertTrue("Step 3 must reference 1997 Hardware DSP", voiceCard.bullets.any { it.first.contains("DSP") })
    }

    @Test
    fun testCoverageOfAllRequestedBoriDexFeatures() {
        val card1 = TUTORIAL_CARDS[0]
        assertTrue("Card 1 must cover camera scanner and battle overlay", 
            card1.bullets.any { it.first.contains("Lens") } && card1.bullets.any { it.first.contains("Overlay") })

        val card2 = TUTORIAL_CARDS[1]
        assertTrue("Card 2 must cover Strategy Guide and 13 domains", 
            card2.title.contains("GUIDE") && card2.bullets.any { it.first.contains("Guide") })

        val card3 = TUTORIAL_CARDS[2]
        assertTrue("Card 3 must cover Dexter voice and voice changer", 
            card3.title.contains("VOICE") && card3.bullets.any { it.first.contains("Changer") })

        val card4 = TUTORIAL_CARDS[3]
        assertTrue("Card 4 must cover 37 retro cartridges and dynamic lore", 
            card4.title.contains("CARTRIDGE") && card4.bullets.any { it.first.contains("Switcher") })

        val card5 = TUTORIAL_CARDS[4]
        assertTrue("Card 5 must cover settings, foldable displays, and tutorial replay", 
            card5.title.contains("SETTINGS") && card5.bullets.any { it.first.contains("Replay") })
    }

    @Test
    fun testInitialLaunchAndFirstStepAreSilent() {
        val card1 = TUTORIAL_CARDS[0]
        assertFalse("First part of the tutorial (Step 1) must not trigger voice test to avoid overstimulating user on open", card1.isVoiceTestStep)
        val card2 = TUTORIAL_CARDS[1]
        assertFalse("Step 2 must not force automatic speech", card2.isVoiceTestStep)

        // Verify only Step 3 has interactive voice test after Step 1 has been completed
        val voiceSteps = TUTORIAL_CARDS.filter { it.isVoiceTestStep }
        assertEquals("Only Step 3 may expose the interactive voice engine test", 1, voiceSteps.size)
        assertEquals("Voice test must occur on Step 3 (after Step 1 is done)", 3, voiceSteps.first().stepNumber)
    }
}
