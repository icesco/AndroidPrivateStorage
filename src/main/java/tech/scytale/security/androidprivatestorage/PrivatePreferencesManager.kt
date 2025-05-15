package tech.scytale.security.androidprivatestorage

import android.content.Context

/**
 * Manages a set of [PrivatePreferences] instances scoped by clientId.
 * This allows separation of secure preferences between different logical users or environments.
 */
object PrivatePreferencesManager {

    private val instances = mutableMapOf<String, PrivatePreferences>()

    /**
     * Retrieves or creates a [PrivatePreferences] instance for the given clientId.
     *
     * @param context the application context
     * @param clientId a unique identifier for the preference scope
     * @return the corresponding [PrivatePreferences] instance
     */
    fun getHelper(context: Context, clientId: String): PrivatePreferences {
        return instances.getOrPut(clientId) {
            PrivatePreferences(
                context = context,
                prefsName = "secure_prefs_$clientId"
            )
        }
    }

    /**
     * Removes the cached [PrivatePreferences] instance for the given clientId.
     * Note: this does not delete persisted data from disk.
     *
     * @param clientId the identifier whose instance should be cleared from memory
     */
    fun clearHelper(clientId: String) {
        instances.remove(clientId)
    }

    /**
     * Clears all cached [PrivatePreferences] instances.
     * Note: this only affects memory; persistent data remains untouched.
     */
    fun clearAll() {
        instances.clear()
    }
}