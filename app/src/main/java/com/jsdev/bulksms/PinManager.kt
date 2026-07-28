package com.jsdev.bulksms

import android.content.Context
import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom

/**
 * Inasimamia PIN lock ya app. PIN yenyewe HAIHIFADHIWI kama maandishi wazi —
 * inahifadhiwa kama hash (SHA-256 + salt), kama inavyofanywa kwenye apps za
 * kibenki. Hali ya "je app iko wazi sasa hivi" inahifadhiwa kwenye kumbukumbu
 * ya muda (session) tu, si kwenye faili — ili app ifunge upya PIN inapofunguliwa
 * upya kabisa (process mpya), hata kama muda wa auto-lock haujafika.
 */
object PinManager {
    private const val PREFS = "bulksms_security"
    private const val KEY_HASH = "pin_hash"
    private const val KEY_SALT = "pin_salt"
    private const val KEY_TIMEOUT = "lock_timeout_ms"

    // Hali ya kikao (session) — haihifadhiwi kwenye faili, inarudi kwenye default
    // (imefungwa) kila app inapoanzishwa upya kabisa.
    private var hasUnlockedOnce = false
    private var lastBackgroundTime = 0L

    fun isPinEnabled(ctx: Context): Boolean = prefs(ctx).contains(KEY_HASH)

    fun setPin(ctx: Context, pin: String) {
        val salt = generateSalt()
        prefs(ctx).edit()
            .putString(KEY_SALT, salt)
            .putString(KEY_HASH, hashPin(pin, salt))
            .apply()
    }

    fun clearPin(ctx: Context) {
        prefs(ctx).edit().remove(KEY_HASH).remove(KEY_SALT).apply()
        hasUnlockedOnce = true
    }

    fun verifyPin(ctx: Context, pin: String): Boolean {
        val salt = prefs(ctx).getString(KEY_SALT, null) ?: return false
        val stored = prefs(ctx).getString(KEY_HASH, null) ?: return false
        return hashPin(pin, salt) == stored
    }

    fun markUnlocked() {
        hasUnlockedOnce = true
        lastBackgroundTime = 0L
    }

    fun recordBackgroundTime() {
        lastBackgroundTime = System.currentTimeMillis()
    }

    fun setTimeoutMs(ctx: Context, ms: Long) {
        prefs(ctx).edit().putLong(KEY_TIMEOUT, ms).apply()
    }

    fun getTimeoutMs(ctx: Context): Long = prefs(ctx).getLong(KEY_TIMEOUT, 0L)

    fun shouldLock(ctx: Context): Boolean {
        if (!isPinEnabled(ctx)) return false
        if (!hasUnlockedOnce) return true
        if (lastBackgroundTime == 0L) return false
        val elapsed = System.currentTimeMillis() - lastBackgroundTime
        return elapsed >= getTimeoutMs(ctx)
    }

    private fun generateSalt(): String {
        val bytes = ByteArray(16)
        SecureRandom().nextBytes(bytes)
        return Base64.encodeToString(bytes, Base64.NO_WRAP)
    }

    private fun hashPin(pin: String, salt: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        digest.update(salt.toByteArray())
        return Base64.encodeToString(digest.digest(pin.toByteArray()), Base64.NO_WRAP)
    }

    private fun prefs(ctx: Context) = ctx.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
}
