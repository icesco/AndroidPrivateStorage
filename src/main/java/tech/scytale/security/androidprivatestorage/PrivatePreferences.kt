package tech.scytale.security.androidprivatestorage

import androidx.core.content.edit
import android.content.Context
import android.content.SharedPreferences
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import android.util.Base64
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json

/**
 * A secure wrapper around SharedPreferences that uses AES encryption with keys stored in the AndroidKeyStore.
 *
 * Supports primitives (String, Int, Long, Float, Boolean) and complex objects (via Kotlin Serialization).
 *
 * @param context the application context
 * @param prefsName the name of the SharedPreferences file
 * @param json the Json instance used for object serialization
 */
class PrivatePreferences(
    context: Context,
    prefsName: String = "secure_prefs",
    private val json: Json = Json { ignoreUnknownKeys = true }
) {

    private val prefs: SharedPreferences = context.getSharedPreferences(prefsName, Context.MODE_PRIVATE)
    private val keyAlias = "SecurePrefsKey"
    private val keyStore: KeyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun getSecretKey(): SecretKey {
        if (keyStore.containsAlias(keyAlias)) {
            val key = keyStore.getKey(keyAlias, null)
            if (key is SecretKey) {
                return key
            }
        }

        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(
                keyAlias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    private fun encrypt(data: ByteArray): Pair<String, String> {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(data)

        val encryptedValue = Base64.encodeToString(encryptedBytes, Base64.DEFAULT)
        val encodedIv = Base64.encodeToString(iv, Base64.DEFAULT)

        return encryptedValue to encodedIv
    }

    private fun decrypt(encryptedValue: String, encodedIv: String): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        val iv = Base64.decode(encodedIv, Base64.DEFAULT)
        val encryptedBytes = Base64.decode(encryptedValue, Base64.DEFAULT)

        val spec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)

        return cipher.doFinal(encryptedBytes)
    }

    /**
     * Stores an encrypted string value in SharedPreferences.
     *
     * @param key the key under which the value is stored
     * @param value the string value to store
     */
    fun putString(key: String, value: String) {
        val (encryptedValue, encodedIv) = encrypt(value.toByteArray(Charsets.UTF_8))
        prefs.edit {
            putString("${key}_data", encryptedValue)
                .putString("${key}_iv", encodedIv)
        }
    }

    /**
     * Retrieves a decrypted string value from SharedPreferences.
     *
     * @param key the key associated with the value
     * @return the decrypted string, or null if not found
     */
    fun getString(key: String): String? {
        val encryptedValue = prefs.getString("${key}_data", null) ?: return null
        val encodedIv = prefs.getString("${key}_iv", null) ?: return null

        val decryptedBytes = decrypt(encryptedValue, encodedIv)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    /**
     * Stores an integer value securely.
     *
     * @param key the key under which the value is stored
     * @param value the integer value to store
     */
    fun putInt(key: String, value: Int) {
        putString(key, value.toString())
    }

    /**
     * Stores a long value securely.
     *
     * @param key the key under which the value is stored
     * @param value the long value to store
     */
    fun putLong(key: String, value: Long) {
        putString(key, value.toString())
    }

    /**
     * Stores a float value securely.
     *
     * @param key the key under which the value is stored
     * @param value the float value to store
     */
    fun putFloat(key: String, value: Float) {
        putString(key, value.toString())
    }

    /**
     * Stores a boolean value securely.
     *
     * @param key the key under which the value is stored
     * @param value the boolean value to store
     */
    fun putBoolean(key: String, value: Boolean) {
        putString(key, value.toString())
    }

    /**
     * Retrieves an integer value securely.
     *
     * @param key the key associated with the value
     * @param defaultValue the value to return if the key is not found or the data is invalid
     * @return the decrypted integer value or defaultValue
     */
    fun getInt(key: String, defaultValue: Int = 0): Int {
        return getString(key)?.toIntOrNull() ?: defaultValue
    }

    /**
     * Retrieves a long value securely.
     *
     * @param key the key associated with the value
     * @param defaultValue the value to return if the key is not found or the data is invalid
     * @return the decrypted long value or defaultValue
     */
    fun getLong(key: String, defaultValue: Long = 0L): Long {
        return getString(key)?.toLongOrNull() ?: defaultValue
    }

    /**
     * Retrieves a float value securely.
     *
     * @param key the key associated with the value
     * @param defaultValue the value to return if the key is not found or the data is invalid
     * @return the decrypted float value or defaultValue
     */
    fun getFloat(key: String, defaultValue: Float = 0f): Float {
        return getString(key)?.toFloatOrNull() ?: defaultValue
    }

    /**
     * Retrieves a boolean value securely.
     *
     * @param key the key associated with the value
     * @param defaultValue the value to return if the key is not found or the data is invalid
     * @return the decrypted boolean value or defaultValue
     */
    fun getBoolean(key: String, defaultValue: Boolean = false): Boolean {
        return getString(key)?.toBooleanStrictOrNull() ?: defaultValue
    }

    /**
     * Stores a complex object securely by serializing it to JSON.
     *
     * @param key the key under which the value is stored
     * @param obj the object to store
     * @param serializer the serializer for the object type
     */
    fun <T> putObject(key: String, obj: T, serializer: KSerializer<T>) {
        val jsonString = json.encodeToString(serializer, obj)
        putString(key, jsonString)
    }

    /**
     * Retrieves a complex object securely by deserializing it from JSON.
     *
     * @param key the key associated with the value
     * @param serializer the serializer for the object type
     * @return the deserialized object, or null if not found
     */
    fun <T> getObject(key: String, serializer: KSerializer<T>): T? {
        val jsonString = getString(key) ?: return null
        return json.decodeFromString(serializer, jsonString)
    }

    /**
     * Removes a value (and its IV) from the secure preferences.
     *
     * @param key the key associated with the value to remove
     */
    fun remove(key: String) {
        prefs.edit {
            remove("${key}_data")
                .remove("${key}_iv")
        }
    }

    /**
     * Clears all values from the secure preferences.
     */
    fun clear() {
        prefs.edit { clear() }
    }
}