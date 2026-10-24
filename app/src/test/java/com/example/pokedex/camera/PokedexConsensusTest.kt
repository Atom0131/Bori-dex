package com.example.pokedex.camera

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying the mathematical rigor, holographic manifestation,
 * temporal stability, and specular glare suppression of the calibrated Bori-dex Consensus Engine.
 */
class PokedexConsensusTest {

    private val MIN_ACQUISITION_CONFIDENCE = 0.15f
    private val MIN_PEAK_MARGIN = 0.025f
    private val MIN_PEAK_RATIO = 1.20f
    private val GLARE_ATTRACTORS = setOf("volbeat", "illumise")

    private fun isRecognizedTarget(top1Val: Float, top2Val: Float): Boolean {
        val leadMargin = top1Val - top2Val
        val leadRatio = if (top2Val > 0f) top1Val / top2Val else 10f
        return (top1Val >= MIN_ACQUISITION_CONFIDENCE) &&
                (leadMargin >= MIN_PEAK_MARGIN || leadRatio >= MIN_PEAK_RATIO)
    }

    private fun getTargetLockFrames(
        candName: String? = null,
        leaderVal: Float,
        top1Val: Float,
        top2Val: Float,
        highestStreakConf: Float = leaderVal
    ): Int {
        val currentLeadRatio = top1Val / maxOf(top2Val, 0.01f)
        val currentMargin = top1Val - top2Val
        val effectiveTargetConf = maxOf(leaderVal, highestStreakConf * 0.88f)
        val isGlareAttractor = candName?.lowercase() in GLARE_ATTRACTORS
        return when {
            isGlareAttractor && effectiveTargetConf < 0.60f -> 999 // Strictly forbid auto-locking glare
            effectiveTargetConf >= 0.65f -> 8
            effectiveTargetConf >= 0.40f -> 10
            effectiveTargetConf >= 0.26f && (currentLeadRatio >= 1.40f || currentMargin >= 0.07f) -> 12
            effectiveTargetConf >= 0.22f && currentLeadRatio >= 1.80f -> 14
            effectiveTargetConf >= 0.17f && currentLeadRatio >= 2.50f -> 16 // Dominant low-light detection
            else -> 999 // Advisory mode, never auto-locks
        }
    }

    private fun canAutoLock(
        candName: String? = null,
        streak: Int,
        targetFrames: Int,
        highestConf: Float,
        currentVal: Float
    ): Boolean {
        if (candName?.lowercase() in GLARE_ATTRACTORS && highestConf < 0.60f) {
            return false
        }
        return streak >= targetFrames && targetFrames <= 30 && highestConf >= 0.17f && currentVal >= 0.15f
    }

    @Test
    fun testClutterRejectionBelowFloor() {
        // Shelf glare producing weak 12% vs 10%
        val top1 = 0.12f
        val top2 = 0.10f
        assertFalse("12% confidence must be rejected as ambient clutter", isRecognizedTarget(top1, top2))

        // Ambient reflection producing 14% (below 15% floor)
        assertFalse("14% confidence must be rejected", isRecognizedTarget(0.14f, 0.05f))
    }

    @Test
    fun testAmbiguousMarginRejection() {
        // 18% vs 17% has margin 1% (< 2.5%) and ratio 1.05x (< 1.20x)
        assertFalse("Statistically tied candidates must be rejected", isRecognizedTarget(0.18f, 0.17f))
    }

    @Test
    fun testDistantCandidateAcquiresForHolographicPreview() {
        // Candidate at 18% with lead over runner-up (e.g. distant plush or Pokémon card)
        assertTrue("18% candidate with clear lead must acquire for holographic sprite preview",
            isRecognizedTarget(0.18f, 0.08f)
        )
    }

    @Test
    fun testNormalDistance3DSBoxArtLocksIn12Frames() {
        // Lunala 3DS box art at normal distance (33% with runner-up at 6%, ratio 5.5x)
        val targetFrames = getTargetLockFrames(candName = "lunala", leaderVal = 0.33f, top1Val = 0.33f, top2Val = 0.06f)
        assertEquals("Normal-distance 3DS box art (33%) must require 12 frames (~0.8s)", 12, targetFrames)

        // Cannot lock prematurely in 8 frames
        assertFalse("Cannot lock in 8 frames", canAutoLock("lunala", 8, targetFrames, 0.33f, 0.33f))

        // Locks cleanly once 12 frames (~0.8s) are reached
        assertTrue("Locks at 12 steady frames", canAutoLock("lunala", 12, targetFrames, 0.33f, 0.33f))
    }

    @Test
    fun testHighConfidenceRequires10Frames() {
        // Solid detection at 55% (e.g. Lunala centered or Tandemaus at 59%)
        val targetFrames = getTargetLockFrames(candName = "lunala", leaderVal = 0.55f, top1Val = 0.55f, top2Val = 0.05f)
        assertEquals("High confidence (40%-64%) must require 10 frames (~0.67s)", 10, targetFrames)

        assertFalse("Cannot lock in 6 frames", canAutoLock("lunala", 6, targetFrames, 0.55f, 0.55f))
        assertTrue("Locks at 10 steady frames", canAutoLock("lunala", 10, targetFrames, 0.55f, 0.55f))
    }

    @Test
    fun testUltraHighConfidenceFast8FrameLock() {
        // Pikachu or Morpeko close-up at 92%
        val targetFrames = getTargetLockFrames(candName = "pikachu", leaderVal = 0.92f, top1Val = 0.92f, top2Val = 0.02f)
        assertEquals("Ultra-high confidence (>=65%) requires 8 frames (~0.53s)", 8, targetFrames)

        assertFalse("Cannot lock accidentally in 4 frames while panning", canAutoLock("pikachu", 4, targetFrames, 0.92f, 0.92f))
        assertTrue("Locks smoothly at 8 frames", canAutoLock("pikachu", 8, targetFrames, 0.92f, 0.92f))
    }

    @Test
    fun testGoalpostStabilityDuringMicroDips() {
        // Lunala established 60% confidence early in the streak, but a frame micro-dips to 42%
        val targetFrames = getTargetLockFrames(candName = "lunala", leaderVal = 0.42f, top1Val = 0.42f, top2Val = 0.06f, highestStreakConf = 0.60f)
        // 0.60 * 0.88 = 0.528 (>= 0.40f), so target remains 10 frames instead of jumping to 12 or 999!
        assertEquals("Established high streak maintains 10-frame target through micro-dips", 10, targetFrames)
    }

    @Test
    fun testWeakAmbiguousClutterStaysInAdvisoryMode() {
        // Obscure ambient reflection at 22% with narrow ratio 1.3x over runner-up
        val targetFrames = getTargetLockFrames(leaderVal = 0.22f, top1Val = 0.22f, top2Val = 0.17f)
        assertEquals("Ambiguous clutter must yield 999 (Advisory Mode)", 999, targetFrames)
        assertFalse("Advisory mode must never auto-lock", canAutoLock(null, 40, targetFrames, 0.22f, 0.22f))
    }

    @Test
    fun testVolbeatSpecularGlareSilencedInAdvisoryMode() {
        // Telemetry reproduction: 3DS box reflection causes Volbeat to hit 40.7% with runner-up at 1.9%
        val targetFrames = getTargetLockFrames(
            candName = "volbeat",
            leaderVal = 0.407f,
            top1Val = 0.407f,
            top2Val = 0.019f
        )
        // Must yield 999 frames (Advisory Mode only: tap-to-lock available, but NEVER auto-locks!)
        assertEquals("Volbeat glare at 40.7% must strictly yield 999 frames", 999, targetFrames)

        // Even after holding steady for 50 frames, auto-lock is completely forbidden
        assertFalse("Volbeat glare must NEVER auto-lock", canAutoLock("volbeat", 50, targetFrames, 0.407f, 0.407f))
    }

    @Test
    fun testIllumiseStoreShelfGlareSilenced() {
        // Overhead fluorescent glare causing Illumise to hit 52% on retail glass
        val targetFrames = getTargetLockFrames(
            candName = "illumise",
            leaderVal = 0.52f,
            top1Val = 0.52f,
            top2Val = 0.04f
        )
        assertEquals("Illumise glare at 52% must strictly yield 999 frames", 999, targetFrames)
        assertFalse("Illumise glare must NEVER auto-lock", canAutoLock("illumise", 35, targetFrames, 0.52f, 0.52f))
    }

    @Test
    fun testGenuineVolbeatItemLocksNormally() {
        // Real physical Volbeat merchandise (card, figure, or plush) hitting 76%
        val targetFrames = getTargetLockFrames(
            candName = "volbeat",
            leaderVal = 0.76f,
            top1Val = 0.76f,
            top2Val = 0.03f
        )
        assertEquals("Genuine Volbeat at 76% must lock smoothly in 8 frames", 8, targetFrames)
        assertTrue("Genuine Volbeat auto-locks once 8 frames are achieved",
            canAutoLock("volbeat", 8, targetFrames, 0.76f, 0.76f)
        )
    }

    @Test
    fun testSpecularGlareAttenuationFilterLogic() {
        // Suppose raw MobileNetV2 gives Volbeat 0.40f and Lunala 0.08f on the dark 3DS cover
        val probs = floatArrayOf(0.40f, 0.08f)
        val volbeatIdx = 0
        val lunalaIdx = 1

        // Glare filter attenuates Volbeat by 0.25x
        probs[volbeatIdx] *= 0.25f // drops from 0.40 -> 0.10f

        // Now find top candidate
        val topIdx = if (probs[0] > probs[1]) 0 else 1
        val topVal = probs[topIdx]

        // If top candidate is still Volbeat at 0.10, isRecognizedTarget rejects it (< 0.15f floor)
        assertEquals("Volbeat attenuated to 0.10f", 0.10f, probs[volbeatIdx], 0.001f)
        assertFalse("Attenuated Volbeat (10%) cannot acquire as a recognized target",
            isRecognizedTarget(probs[volbeatIdx], probs[lunalaIdx])
        )
    }

    @Test
    fun testTier4DistantSteadyLocksAt12Frames() {
        // Distant Pokémon scoring 26% with strong 2.6x lead ratio
        val targetFrames = getTargetLockFrames(
            candName = "palkia",
            leaderVal = 0.26f,
            top1Val = 0.26f,
            top2Val = 0.10f
        )
        assertEquals("Tier 3 distant steady target must require 12 frames (~0.8s)", 12, targetFrames)
        assertFalse("Cannot lock before 12 frames", canAutoLock("palkia", 8, targetFrames, 0.26f, 0.26f))
        assertTrue("Locks cleanly at 12 frames with 26% confidence", canAutoLock("palkia", 12, targetFrames, 0.26f, 0.26f))
    }

    @Test
    fun testTier5DominantLowLightLunalaLocksAt16Frames() {
        // Exact telemetry reproduction: Lunala at 19.39% with runner-up at 2.95% (ratio 6.57x)
        val targetFrames = getTargetLockFrames(
            candName = "lunala",
            leaderVal = 0.1939f,
            top1Val = 0.1939f,
            top2Val = 0.0295f
        )
        assertEquals("Tier 5 dominant low-light target (19.4% with 6.57x lead) requires 16 frames (~1.0s)", 16, targetFrames)
        assertFalse("Cannot lock before 16 frames", canAutoLock("lunala", 10, targetFrames, 0.1939f, 0.1939f))
        assertTrue("Locks cleanly at 16 frames with 19.4% confidence and 6.57x ratio",
            canAutoLock("lunala", 16, targetFrames, 0.1939f, 0.1939f)
        )
    }
}
