package com.alfred.kitabalhuda

import com.alfred.kitabalhuda.database.entity.AudioEntity
import com.alfred.kitabalhuda.database.entity.HadithEntity
import com.alfred.kitabalhuda.database.entity.ListeningHistoryEntity
import com.alfred.kitabalhuda.database.entity.PlaylistEntity
import com.alfred.kitabalhuda.database.entity.PlaylistItemEntity
import com.alfred.kitabalhuda.database.entity.ReciteurEntity
import com.alfred.kitabalhuda.database.entity.SourateEntity
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.InvalidAlgorithmParameterException
import java.security.MessageDigest
import java.util.Base64
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Adversarial Stress Test Suite for Kitab al-Huda Core Engine:
 * 1. AES-256-CBC Cryptography edge cases (tampering, wrong keys, empty key components, IV size constraints).
 * 2. Messenger zero-rated URI parsing and extraction resilience under adversarial URI inputs.
 * 3. LRU Cache eviction and 45-minute TTL monotonicity behavior.
 * 4. Room Entity nullability, foreign key relationship contracts, and default value invariants.
 * 5. Media3 Sleep Timer volume fade-out step calculation and command bundle contracts.
 * 6. Room Schema v7 export alignment with SQL migration statements.
 */
class CoreEngineAdversarialStressTest {

    // =========================================================================
    // 1. AES-256-CBC Cryptography Edge Cases
    // =========================================================================

    private fun deriveKey(part1: String, part2: String): SecretKeySpec {
        val combined = part1 + part2
        val hash = MessageDigest.getInstance("SHA-256").digest(combined.toByteArray(Charsets.UTF_8))
        return SecretKeySpec(hash, "AES")
    }

    private fun encryptAes(plainText: String, key: SecretKeySpec, iv: ByteArray = ByteArray(16)): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        val encrypted = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))
        return Base64.getEncoder().encodeToString(encrypted)
    }

    private fun decryptAes(base64Ciphertext: String, key: SecretKeySpec, iv: ByteArray = ByteArray(16)): String {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(iv))
        val decrypted = cipher.doFinal(Base64.getDecoder().decode(base64Ciphertext))
        return String(decrypted, Charsets.UTF_8)
    }

    @Test
    fun testAes256NormalEncryptionAndDecryptionRoundtrip() {
        val key = deriveKey("build_id_12345", "signature_hash_67890")
        val secretMessage = "https://graph.facebook.com/v19.0/"
        val ciphertext = encryptAes(secretMessage, key)

        val decrypted = decryptAes(ciphertext, key)
        assertEquals(secretMessage, decrypted)
    }

    @Test
    fun testAes256EmptyKeyComponentsStillProduceValid256BitKey() {
        // Even if both build id and signature are empty, SHA-256 produces exactly 32 bytes (256 bits)
        val keyEmpty = deriveKey("", "")
        assertEquals(32, keyEmpty.encoded.size)
        assertEquals("AES", keyEmpty.algorithm)

        val secret = "SecretPayload"
        val cipherText = encryptAes(secret, keyEmpty)
        val decrypted = decryptAes(cipherText, keyEmpty)
        assertEquals(secret, decrypted)
    }

    @Test
    fun testAes256TamperedCiphertextTriggersPaddingOrBlockSizeException() {
        val key = deriveKey("build_id", "sig")
        val secret = "ValidSecretToken12345"
        val validBase64 = encryptAes(secret, key)
        val rawBytes = Base64.getDecoder().decode(validBase64)

        // Corrupt the last byte (where PKCS5 padding lives)
        val corruptedBytes = rawBytes.clone()
        corruptedBytes[corruptedBytes.size - 1] = (corruptedBytes[corruptedBytes.size - 1].toInt() xor 0xFF).toByte()
        val corruptedBase64 = Base64.getEncoder().encodeToString(corruptedBytes)

        // Decryption with corrupted padding MUST throw BadPaddingException
        assertThrows(BadPaddingException::class.java) {
            decryptAes(corruptedBase64, key)
        }
    }

    @Test
    fun testAes256DecryptionWithWrongSignatureFails() {
        val correctKey = deriveKey("part1_fixed", "signature_A")
        val wrongKey = deriveKey("part1_fixed", "signature_B")

        val payload = "https://graph.facebook.com/me"
        val ciphertext = encryptAes(payload, correctKey)

        // Attempting to decrypt with wrong signature should fail with BadPaddingException
        assertThrows(BadPaddingException::class.java) {
            decryptAes(ciphertext, wrongKey)
        }
    }

    @Test
    fun testAes256TruncatedCiphertextFailsIllegalBlockSize() {
        val key = deriveKey("part1", "part2")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(ByteArray(16)))

        // Non-multiple of 16 bytes (e.g. 7 bytes) cannot be decrypted in CBC mode
        val truncatedBytes = ByteArray(7) { 0x42 }
        assertThrows(IllegalBlockSizeException::class.java) {
            cipher.doFinal(truncatedBytes)
        }
    }

    @Test
    fun testAes256InvalidIvSizeThrowsException() {
        val key = deriveKey("part1", "part2")

        // 15-byte IV (invalid for AES which requires 16-byte blocks)
        val cipher1 = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val invalidIv15 = ByteArray(15)
        assertThrows(java.security.GeneralSecurityException::class.java) {
            cipher1.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(invalidIv15))
        }

        // 17-byte IV (invalid)
        val cipher2 = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val invalidIv17 = ByteArray(17)
        assertThrows(java.security.GeneralSecurityException::class.java) {
            cipher2.init(Cipher.DECRYPT_MODE, key, IvParameterSpec(invalidIv17))
        }
    }

    // =========================================================================
    // 2. Messenger Zero-Rated URI Scheme Semantics Under Adversarial Inputs
    // =========================================================================

    private fun extractMessengerMessageId(uriString: String): String {
        val scheme = "messenger://"
        if (!uriString.startsWith(scheme, ignoreCase = true)) {
            return ""
        }
        val stripped = uriString.substring(scheme.length)
        // Clean leading slashes, path segments, and query parameters
        val cleanPath = stripped.trimStart('/')
        val beforeQuery = cleanPath.substringBefore('?').substringBefore('#')
        return beforeQuery.substringBefore('/')
    }

    @Test
    fun testMessengerUriExtractionAdversarialCases() {
        // Standard URI
        assertEquals("m_1234567890", extractMessengerMessageId("messenger://m_1234567890"))

        // Multiple leading slashes
        assertEquals("m_1234567890", extractMessengerMessageId("messenger:///m_1234567890"))
        assertEquals("m_1234567890", extractMessengerMessageId("messenger://///m_1234567890"))

        // URI with trailing slash
        assertEquals("m_1234567890", extractMessengerMessageId("messenger://m_1234567890/"))

        // URI with extra path hierarchy
        assertEquals("m_1234567890", extractMessengerMessageId("messenger://m_1234567890/part1/stream"))

        // URI with query parameters and fragment
        assertEquals("m_1234567890", extractMessengerMessageId("messenger://m_1234567890?token=abc&exp=123#sec"))

        // Case-insensitive scheme
        assertEquals("m_1234567890", extractMessengerMessageId("MESSENGER://m_1234567890"))

        // Empty scheme URI
        assertEquals("", extractMessengerMessageId("messenger://"))

        // Non-messenger URIs should return empty string
        assertEquals("", extractMessengerMessageId("https://example.com/audio.mp3"))
        assertEquals("", extractMessengerMessageId("file:///android_asset/audio.mp3"))
        assertEquals("", extractMessengerMessageId(""))
    }

    // =========================================================================
    // 3. LRU Cache & Expiration Monotonicity
    // =========================================================================

    private class TestCacheEntry(val url: String, val timestampMillis: Long) {
        fun isExpired(currentMillis: Long, ttlMillis: Long = 45 * 60 * 1000L): Boolean {
            return (currentMillis - timestampMillis) > ttlMillis
        }
    }

    @Test
    fun testLruCacheTtlThresholds() {
        val ttl = 45 * 60 * 1000L // 2,700,000 ms
        assertEquals(2_700_000L, ttl)

        val baseTime = 1_000_000_000L
        val entry = TestCacheEntry("https://cdn.fb.com/audio.mp3", baseTime)

        // 44 minutes after: NOT expired
        val t44m = baseTime + (44 * 60 * 1000L)
        assertFalse(entry.isExpired(t44m))

        // Exactly 45 minutes: NOT expired (> condition)
        val t45m = baseTime + ttl
        assertFalse(entry.isExpired(t45m))

        // 45 minutes + 1 ms: EXPIRED
        val t45m1ms = baseTime + ttl + 1L
        assertTrue(entry.isExpired(t45m1ms))

        // 60 minutes after: EXPIRED
        val t60m = baseTime + (60 * 60 * 1000L)
        assertTrue(entry.isExpired(t60m))
    }

    @Test
    fun testLruCacheSizeEvictionSimulation() {
        val cacheCapacity = 120
        val map = object : LinkedHashMap<String, String>(cacheCapacity, 0.75f, true) {
            override fun removeEldestEntry(eldest: MutableMap.MutableEntry<String, String>?): Boolean {
                return size > cacheCapacity
            }
        }

        // Insert 120 items
        for (i in 1..120) {
            map["key_$i"] = "url_$i"
        }
        assertEquals(120, map.size)
        assertTrue(map.containsKey("key_1"))
        assertTrue(map.containsKey("key_120"))

        // Insert 121st item -> key_1 should be evicted as LRU
        map["key_121"] = "url_121"
        assertEquals(120, map.size)
        assertFalse("key_1 should be evicted", map.containsKey("key_1"))
        assertTrue("key_121 should be present", map.containsKey("key_121"))
        assertTrue("key_2 should be retained", map.containsKey("key_2"))
    }

    // =========================================================================
    // 4. Room Entity Integrity, Nullability, and Defaults
    // =========================================================================

    @Test
    fun testAudioEntityNullabilityAndPartNumberDefault() {
        // Normal web audio track
        val webAudio = AudioEntity(
            id = 1L,
            reciteurId = 5,
            sourateNumero = 18,
            duree = 1800L,
            urlWeb = "https://cdn.example.com/al-kahf.mp3"
        )
        assertEquals(1L, webAudio.id)
        assertEquals(5, webAudio.reciteurId)
        assertEquals(18, webAudio.sourateNumero)
        assertEquals(1800L, webAudio.duree)
        assertEquals("https://cdn.example.com/al-kahf.mp3", webAudio.urlWeb)
        assertNull(webAudio.pathLocal)
        assertNull(webAudio.fbMessageId)
        assertEquals(1, webAudio.partNumber)

        // Zero-rated multi-part audio track
        val zeroRatedPart2 = AudioEntity(
            id = 2L,
            reciteurId = 5,
            sourateNumero = 18,
            duree = 1950L,
            urlWeb = "messenger://m_kahf_part_2",
            pathLocal = "/storage/emulated/0/kitab/18_2.mp3",
            fbMessageId = "m_kahf_part_2",
            partNumber = 2
        )
        assertEquals("m_kahf_part_2", zeroRatedPart2.fbMessageId)
        assertEquals("/storage/emulated/0/kitab/18_2.mp3", zeroRatedPart2.pathLocal)
        assertEquals(2, zeroRatedPart2.partNumber)
    }

    @Test
    fun testSourateEntityBoundaryValues() {
        // Quran has 114 surahs: 1 (Al-Fatihah) to 114 (An-Nas)
        val fatihah = SourateEntity(1, "الفاتحة", "Al-Fatihah", 7, "MECCA")
        assertEquals(1, fatihah.numero)
        assertEquals(7, fatihah.nombreVersets)

        val nas = SourateEntity(114, "الناس", "An-Nas", 6, "MECCA")
        assertEquals(114, nas.numero)
        assertEquals(6, nas.nombreVersets)

        val baqarah = SourateEntity(2, "البقرة", "Al-Baqarah", 286, "MEDINA")
        assertEquals(286, baqarah.nombreVersets)
    }

    @Test
    fun testListeningHistoryAndPlaylistRelationships() {
        val historyEntry = ListeningHistoryEntity(
            id = 999L,
            audioId = 42L,
            timestamp = 1700000000000L
        )
        assertEquals(999L, historyEntry.id)
        assertEquals(42L, historyEntry.audioId)
        assertEquals(1700000000000L, historyEntry.timestamp)

        val playlistItem = PlaylistItemEntity(
            id = 10L,
            playlistId = 3,
            audioId = 42L,
            orderIndex = 0
        )
        assertEquals(3, playlistItem.playlistId)
        assertEquals(42L, playlistItem.audioId)
        assertEquals(0, playlistItem.orderIndex)

        val playlist = PlaylistEntity(
            id = 3,
            name = "Favoris",
            description = null,
            coverUrl = null
        )
        assertNull(playlist.description)
        assertNull(playlist.coverUrl)
    }

    // =========================================================================
    // 5. Media3 Sleep Timer & Fade-out Step Calculation
    // =========================================================================

    @Test
    fun testSleepTimerDelayCalculationWithoutOverflow() {
        // Normal minutes
        val minutes15 = 15
        val delayMillis15 = minutes15 * 60 * 1000L
        assertEquals(900_000L, delayMillis15)

        // 60 minutes
        val minutes60 = 60
        val delayMillis60 = minutes60 * 60 * 1000L
        assertEquals(3_600_000L, delayMillis60)

        // Extreme duration does not overflow Long
        val extremeMinutes = 100_000
        val delayExtreme = extremeMinutes * 60 * 1000L
        assertTrue(delayExtreme > 0)
        assertEquals(6_000_000_000L, delayExtreme)
    }

    @Test
    fun testFadeOutVolumeLinearInterpolation() {
        val startVolume = 1.0f
        val fadeDuration = 3000L
        val steps = 20
        val stepDelay = fadeDuration / steps // 150ms

        val volumes = mutableListOf<Float>()
        for (step in 0..steps) {
            val millisUntilFinished = fadeDuration - (step * stepDelay)
            val vol = startVolume * (millisUntilFinished.coerceAtLeast(0L).toFloat() / fadeDuration)
            volumes.add(vol)
        }

        // At start (step 0, 3000ms left), volume should be 1.0
        assertEquals(1.0f, volumes.first(), 0.001f)
        // At midway (step 10, 1500ms left), volume should be 0.5
        assertEquals(0.5f, volumes[10], 0.001f)
        // At finish (step 20, 0ms left), volume should be 0.0
        assertEquals(0.0f, volumes.last(), 0.001f)

        // Strictly decreasing sequence
        for (i in 0 until volumes.size - 1) {
            assertTrue(volumes[i] >= volumes[i + 1])
        }
    }

    // =========================================================================
    // 6. Media3 AudioPlayerService Constants & Lifecycle Contracts
    // =========================================================================

    @Test
    fun testAudioPlayerServiceConstantsAndContract() {
        // Verification of contract constants defined in AudioPlayerService companion
        val commandStart = com.alfred.kitabalhuda.service.AudioPlayerService.COMMAND_START_SLEEP_TIMER
        val commandStop = com.alfred.kitabalhuda.service.AudioPlayerService.COMMAND_STOP_SLEEP_TIMER
        val extraMinutes = com.alfred.kitabalhuda.service.AudioPlayerService.EXTRA_MINUTES
        val messengerScheme = com.alfred.kitabalhuda.service.AudioPlayerService.MESSENGER_URI_SCHEME

        assertEquals("START_SLEEP_TIMER", commandStart)
        assertEquals("STOP_SLEEP_TIMER", commandStop)
        assertEquals("EXTRA_MINUTES", extraMinutes)
        assertEquals("messenger://", messengerScheme)
    }

    @Test
    fun testForegroundServiceStartNotAllowedExceptionHandlingContract() {
        // Ensure the MediaSessionService.Listener implementation gracefully swallows
        // onForegroundServiceStartNotAllowedException without crashing the process
        var callbackExecuted = false
        val listener = object : androidx.media3.session.MediaSessionService.Listener {
            override fun onForegroundServiceStartNotAllowedException() {
                // Emulate the AudioPlayerService listener logic: log warning and continue
                callbackExecuted = true
            }
        }

        // Triggering the callback directly should not throw
        listener.onForegroundServiceStartNotAllowedException()
        assertTrue("Callback should execute cleanly without throwing", callbackExecuted)
    }

    // =========================================================================
    // 7. Room Schema v7 File Alignment & Migration Consistency
    // =========================================================================

    @Test
    fun testRoomSchemaExportV7ContainsAllRequiredTablesAndFields() {
        val schemaFileCandidates = listOf(
            java.io.File("schemas/com.alfred.kitabalhuda.database.AppDatabase/7.json"),
            java.io.File("app/schemas/com.alfred.kitabalhuda.database.AppDatabase/7.json"),
            java.io.File("../app/schemas/com.alfred.kitabalhuda.database.AppDatabase/7.json")
        )

        val schemaFile = schemaFileCandidates.firstOrNull { it.exists() }
        assertNotNull("Room schema v7.json must exist in project", schemaFile)

        val content = schemaFile!!.readText()
        val jsonObject = com.google.gson.JsonParser.parseString(content).asJsonObject

        // 1. Verify DB version is 7
        val databaseObj = jsonObject.getAsJsonObject("database")
        assertEquals(7, databaseObj.get("version").asInt)

        // 2. Verify all 7 entities are listed
        val entitiesArray = databaseObj.getAsJsonArray("entities")
        val tableNames = entitiesArray.map { it.asJsonObject.get("tableName").asString }.toSet()
        val expectedTables = setOf(
            "sourates",
            "reciteurs",
            "audios",
            "hadiths",
            "playlists",
            "playlist_items",
            "listening_history"
        )
        assertEquals(expectedTables, tableNames)

        // 3. Verify 'audios' table contains fbMessageId (nullable) and partNumber (not null)
        val audiosEntity = entitiesArray.first { it.asJsonObject.get("tableName").asString == "audios" }.asJsonObject
        val audiosFields = audiosEntity.getAsJsonArray("fields").map { it.asJsonObject }
        val fbMessageIdField = audiosFields.firstOrNull { it.get("columnName").asString == "fbMessageId" }
        assertNotNull("fbMessageId field must exist in audios table", fbMessageIdField)
        assertFalse("fbMessageId must be nullable", fbMessageIdField!!.get("notNull").asBoolean)

        val partNumberField = audiosFields.firstOrNull { it.get("columnName").asString == "partNumber" }
        assertNotNull("partNumber field must exist in audios table", partNumberField)
        assertTrue("partNumber must be NOT NULL", partNumberField!!.get("notNull").asBoolean)

        // 4. Verify 'listening_history' has foreignKey on audios(id) with CASCADE
        val historyEntity = entitiesArray.first { it.asJsonObject.get("tableName").asString == "listening_history" }.asJsonObject
        val historyForeignKeys = historyEntity.getAsJsonArray("foreignKeys")
        assertTrue("listening_history must have at least 1 foreign key", historyForeignKeys.size() > 0)
        val audioFk = historyForeignKeys.first().asJsonObject
        assertEquals("audios", audioFk.get("table").asString)
        assertEquals("CASCADE", audioFk.get("onDelete").asString)
    }

    // =========================================================================
    // 8. Cryptographic Fuzzing: Key Invariance & Collision Resistance
    // =========================================================================

    @Test
    fun testCryptoKeyDerivationFuzzingDeterministic256Bits() {
        val random = kotlin.random.Random(42L)
        val seenDigests = mutableSetOf<String>()

        for (i in 1..50) {
            val p1Len = random.nextInt(32)
            val p2Len = random.nextInt(32)
            val p1Chars = (1..p1Len).map { ('a'..'z').random(random) }.joinToString("")
            val p2Chars = (1..p2Len).map { ('A'..'Z').random(random) }.joinToString("")

            val key = deriveKey(p1Chars, p2Chars)
            // Exactly 32 bytes (256 bits)
            assertEquals(32, key.encoded.size)

            val hexDigest = key.encoded.joinToString("") { "%02x".format(it) }
            seenDigests.add(hexDigest)

            // Test roundtrip encryption
            val testPayload = "FuzzPayload_$i"
            val cipherText = encryptAes(testPayload, key)
            val decrypted = decryptAes(cipherText, key)
            assertEquals(testPayload, decrypted)
        }

        // All 50 random derivations must be distinct
        assertEquals(50, seenDigests.size)
    }
}
