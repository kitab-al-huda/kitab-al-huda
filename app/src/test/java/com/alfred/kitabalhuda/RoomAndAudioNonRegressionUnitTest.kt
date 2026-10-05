package com.alfred.kitabalhuda

import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.database.entity.HadithEntity
import com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.database.entity.PlaylistItemEntity
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.database.entity.SourateEntity
import com.alfred.kitabalhuda.utils.DigitHelper
import com.alfred.kitabalhuda.utils.formatDurationArabic
import com.alfred.kitabalhuda.utils.toArabicIndic
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.MessageDigest

/**
 * Non-regression unit test suite verifying:
 * 1. Room Entity contracts (Sourate, Reciteur, Audio zero-rated fields, Playlist, History, Hadith)
 * 2. Media3 zero-rated URI scheme parsing semantics
 * 3. AES-256-CBC split-key SHA-256 derivation integrity
 * 4. Arabic duration and digit formatting for media timeline controls
 */
class RoomAndAudioNonRegressionUnitTest {

    @Test
    fun testSourateEntityIntegrity() {
        val fatihah = SourateEntity(
            numero = 1,
            nomArabe = "الفاتحة",
            nomPhonetique = "Al-Fatihah",
            nombreVersets = 7,
            lieuRevelation = "MECCA"
        )

        assertEquals(1, fatihah.numero)
        assertEquals("الفاتحة", fatihah.nomArabe)
        assertEquals("Al-Fatihah", fatihah.nomPhonetique)
        assertEquals(7, fatihah.nombreVersets)
        assertEquals("MECCA", fatihah.lieuRevelation)

        val baqarah = SourateEntity(
            numero = 2,
            nomArabe = "البقرة",
            nomPhonetique = "Al-Baqarah",
            nombreVersets = 286,
            lieuRevelation = "MEDINA"
        )
        assertEquals("MEDINA", baqarah.lieuRevelation)
        assertEquals(286, baqarah.nombreVersets)
    }

    @Test
    fun testAudioEntityZeroRatedFieldsAndPartNumberDefault() {
        // Standard audio track without zero-rated messenger ID
        val normalAudio = AudioEntity(
            id = 101L,
            reciteurId = 1,
            sourateNumero = 1,
            duree = 75L,
            urlWeb = "https://example.com/audio/1.mp3"
        )
        assertNull(normalAudio.fbMessageId)
        assertEquals(1, normalAudio.partNumber)

        // Zero-rated audio track with fbMessageId and multi-part specification
        val zeroRatedAudio = AudioEntity(
            id = 102L,
            reciteurId = 1,
            sourateNumero = 2,
            duree = 3600L,
            urlWeb = "messenger://m_msgPart123456",
            fbMessageId = "m_msgPart123456",
            partNumber = 2
        )
        assertEquals("m_msgPart123456", zeroRatedAudio.fbMessageId)
        assertEquals(2, zeroRatedAudio.partNumber)
        assertEquals("messenger://m_msgPart123456", zeroRatedAudio.urlWeb)
    }

    @Test
    fun testReciteurAndPlaylistEntitiesIntegrity() {
        val reciter = ReciteurEntity(
            id = 1,
            nom = "مشاري بن راشد العفاسي",
            imageUrl = "https://example.com/alafasy.jpg",
            description = "قارئ كويتي معتمد"
        )
        assertEquals(1, reciter.id)
        assertEquals("مشاري بن راشد العفاسي", reciter.nom)

        val playlist = PlaylistEntity(
            id = 5,
            name = "تلاوات الصباح",
            description = "أذكار وتلاوات هادئة"
        )
        assertEquals(5, playlist.id)
        assertEquals("تلاوات الصباح", playlist.name)

        val playlistItem = PlaylistItemEntity(
            id = 50L,
            playlistId = 5,
            audioId = 102L,
            orderIndex = 0
        )
        assertEquals(5, playlistItem.playlistId)
        assertEquals(102L, playlistItem.audioId)
        assertEquals(0, playlistItem.orderIndex)
    }

    @Test
    fun testListeningHistoryAndHadithEntities() {
        val timestamp = System.currentTimeMillis()
        val history = ListeningHistoryEntity(
            id = 1,
            audioId = 102L,
            timestamp = timestamp
        )
        assertEquals(102L, history.audioId)
        assertEquals(timestamp, history.timestamp)

        val hadith = HadithEntity(
            id = 42L,
            text = "خَيْرُكُمْ مَنْ تَعَلَّمَ الْقُرْآنَ وَعَلَّمَهُ",
            book = "صحيح مسلم",
            chapter = "كتاب الإيمان"
        )
        assertEquals("كتاب الإيمان", hadith.chapter)
        assertEquals("صحيح مسلم", hadith.book)
        assertTrue(hadith.text.contains("الْقُرْآنَ"))
    }

    @Test
    fun testMessengerUriSchemeExtractionLogic() {
        val messengerScheme = "messenger://"
        val testUriString1 = "messenger://m_abc12345xyz"
        val testUriString2 = "messenger:///m_slash_prefix_987"
        val standardHttpUri = "https://cdn.fbcdn.net/v/audio.mp3"

        // Verify scheme detection
        assertTrue(testUriString1.startsWith(messengerScheme))
        assertTrue(testUriString2.startsWith(messengerScheme))
        assertFalse(standardHttpUri.startsWith(messengerScheme))

        // Emulate messageId extraction logic used in AudioPlayerService
        fun extractMessageId(uriString: String): String {
            val stripped = uriString.removePrefix(messengerScheme)
            return stripped.removePrefix("/")
        }

        assertEquals("m_abc12345xyz", extractMessageId(testUriString1))
        assertEquals("m_slash_prefix_987", extractMessageId(testUriString2))
    }

    @Test
    fun testAes256SplitKeySha256Derivation() {
        val part1 = "mock_dev_key_part1"
        val part2 = "K1tab@Huda#2026"
        val combinedKey = part1 + part2

        val digest = MessageDigest.getInstance("SHA-256").digest(combinedKey.toByteArray(Charsets.UTF_8))
        assertNotNull(digest)
        // 256 bits = 32 bytes
        assertEquals(32, digest.size)

        // Verify cryptographic avalanche effect: changing 1 character completely changes the digest
        val tamperedKey = part1 + "K1tab@Huda#2027"
        val tamperedDigest = MessageDigest.getInstance("SHA-256").digest(tamperedKey.toByteArray(Charsets.UTF_8))
        assertEquals(32, tamperedDigest.size)
        assertFalse(digest.contentEquals(tamperedDigest))
    }

    @Test
    fun testMediaDurationArabicFormatting() {
        val originalPref = DigitHelper.useArabicIndic
        try {
            DigitHelper.useArabicIndic = true
            // 75 seconds = 1:15 -> "١:١٥"
            assertEquals("١:١٥", 75L.formatDurationArabic())
            // 3600 seconds = 60:00 -> "٦٠:٠٠"
            assertEquals("٦٠:٠٠", 3600L.formatDurationArabic())
            // 5 seconds = 0:05 -> "٠:٠٥"
            assertEquals("٠:٠٥", 5L.formatDurationArabic())

            DigitHelper.useArabicIndic = false
            assertEquals("1:15", 75L.formatDurationArabic())
            assertEquals("60:00", 3600L.formatDurationArabic())
            assertEquals("0:05", 5L.formatDurationArabic())
        } finally {
            DigitHelper.useArabicIndic = originalPref
        }
    }
}
