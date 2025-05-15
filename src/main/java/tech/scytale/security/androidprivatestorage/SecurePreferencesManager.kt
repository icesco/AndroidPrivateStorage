package net.aliaslab.authenticatedrequests.tokenpersistence

import android.content.Context
import tech.scytale.security.androidprivatestorage.SecurePreferencesHelper

/**
 * SecurePreferencesManager - manages multiple SecurePreferencesHelper instances,
 * one per clientId (or any namespace string).
 */
object SecurePreferencesManager {

    private val instances = mutableMapOf<String, SecurePreferencesHelper>()

    fun getHelper(context: Context, clientId: String): SecurePreferencesHelper {
        return instances.getOrPut(clientId) {
            SecurePreferencesHelper(
                context = context,
                prefsName = "secure_prefs_$clientId"
            )
        }
    }

    fun clearHelper(clientId: String) {
        instances.remove(clientId)
    }

    fun clearAll() {
        instances.clear()
    }
}