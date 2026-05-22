package com.alfred.kitabalhuda.utils

import android.content.Context
import android.util.Base64
import com.alfred.kitabalhuda.R
import com.alfred.kitabalhuda.BuildConfig
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

/**
 * Utilitaire de déchiffrement AES-256-CBC pour les valeurs chiffrées stockées dans BuildConfig.
 * La clé est composée de deux parties :
 * - ENCRYPTION_KEY_PART1 (BuildConfig / gradle.properties)
 * - encryption_key_part2 (strings.xml)
 * Combinées et hashées en SHA-256 pour obtenir une clé AES-256.
 */
object CryptoUtils {

    private var cachedKey: SecretKeySpec? = null
    private val iv = IvParameterSpec(ByteArray(16)) // 16-byte zero IV

    /**
     * Déchiffre une chaîne Base64 chiffrée en AES-256-CBC.
     */
    fun decrypt(encryptedBase64: String, context: Context): String {
        val key = getOrCreateKey(context)
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        val decryptedBytes = cipher.doFinal(Base64.decode(encryptedBase64, Base64.DEFAULT))
        return String(decryptedBytes, Charsets.UTF_8)
    }

    private fun getOrCreateKey(context: Context): SecretKeySpec {
        cachedKey?.let { return it }

        val part1 = BuildConfig.ENCRYPTION_KEY_PART1
        val part2 = context.getString(R.string.encryption_key_part2)
        val fullKey = part1 + part2

        val keyBytes = MessageDigest.getInstance("SHA-256").digest(fullKey.toByteArray(Charsets.UTF_8))
        val spec = SecretKeySpec(keyBytes, "AES")
        cachedKey = spec
        return spec
    }
}
