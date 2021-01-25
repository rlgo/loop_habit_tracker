/*
 * Copyright (C) 2016-2020 linson Santos Xavier <isoron@gmail.com>
 *
 * This file is part of Loop Habit Tracker.
 *
 * Loop Habit Tracker is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by the
 * Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version.
 *
 * Loop Habit Tracker is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY
 * or FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

@file:Suppress("UnstableApiUsage")

package org.isoron.uhabits.core.sync

import com.google.common.io.*
import kotlinx.coroutines.*
import org.apache.commons.codec.binary.*
import java.io.*
import java.nio.*
import java.util.zip.*
import javax.crypto.*
import javax.crypto.spec.*

/**
 * Encryption key which can be used with [File.encryptToString], [String.decryptToFile],
 * [ByteArray.encrypt] and [ByteArray.decrypt].
 *
 * To randomly generate a new key, use [EncryptionKey.generate]. To load a key from a
 * Base64-encoded string, use [EncryptionKey.fromBase64].
 */
class EncryptionKey private constructor(
        val base64: String,
        val secretKey: SecretKey
) {
    companion object {

        fun fromBase64(base64: String): EncryptionKey {
            val keySpec = SecretKeySpec(base64.decodeBase64(), "AES")
            return EncryptionKey(base64, keySpec)
        }

        private fun fromSecretKey(spec: SecretKey): EncryptionKey {
            val base64 = spec.encoded.encodeBase64().trim()
            return EncryptionKey(base64, spec)
        }

        suspend fun generate(): EncryptionKey = Dispatchers.IO {
            try {
                val generator = KeyGenerator.getInstance("AES").apply { init(256) }
                return@IO fromSecretKey(generator.generateKey())
            } catch (e: Exception) {
                throw RuntimeException(e)
            }
        }
    }
}

/**
 * Encrypts the byte stream using the provided symmetric encryption key.
 *
 * The initialization vector (16 bytes) is prepended to the cipher text. To decrypt the result, use
 * [ByteArray.decrypt], providing the same key.
 */
fun ByteArray.encrypt(key: EncryptionKey): ByteArray {
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.ENCRYPT_MODE, key.secretKey)
    val encrypted = cipher.doFinal(this)
    return ByteBuffer
            .allocate(16 + encrypted.size)
            .put(cipher.iv)
            .put(encrypted)
            .array()
}

/**
 * Decrypts a byte stream generated by [ByteArray.encrypt].
 */
fun ByteArray.decrypt(key: EncryptionKey): ByteArray {
    val buffer = ByteBuffer.wrap(this)
    val iv = ByteArray(16)
    buffer.get(iv)
    val encrypted = ByteArray(buffer.remaining())
    buffer.get(encrypted)
    val cipher = Cipher.getInstance("AES/CBC/PKCS5PADDING")
    cipher.init(Cipher.DECRYPT_MODE, key.secretKey, IvParameterSpec(iv))
    return cipher.doFinal(encrypted)
}

/**
 * Takes a string produced by [File.encryptToString], decodes it with Base64, decompresses it with
 * gzip, decrypts it with the provided key, then writes the output to the specified file.
 */
fun String.decryptToFile(key: EncryptionKey, output: File) {
    val bytes = this.decodeBase64().decrypt(key)
    ByteArrayInputStream(bytes).use { bytesInputStream ->
        GZIPInputStream(bytesInputStream).use { gzipInputStream ->
            FileOutputStream(output).use { fileOutputStream ->
                ByteStreams.copy(gzipInputStream, fileOutputStream)
            }
        }
    }
}

/**
 * Compresses the file with gzip, encrypts it using the the provided key, then returns a string
 * containing the Base64-encoded cipher bytes.
 *
 * To decrypt and decompress the cipher text back into a file, use [String.decryptToFile].
 */
fun File.encryptToString(key: EncryptionKey): String {
    ByteArrayOutputStream().use { bytesOutputStream ->
        FileInputStream(this).use { inputStream ->
            GZIPOutputStream(bytesOutputStream).use { gzipOutputStream ->
                ByteStreams.copy(inputStream, gzipOutputStream)
                gzipOutputStream.close()
                val bytes = bytesOutputStream.toByteArray()
                return bytes.encrypt(key).encodeBase64()
            }
        }
    }
}

fun ByteArray.encodeBase64(): String = Base64.encodeBase64(this).decodeToString()
fun String.decodeBase64(): ByteArray = Base64.decodeBase64(this.toByteArray())

