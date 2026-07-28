package com.jsdev.bulksms

import android.content.Context
import java.io.PrintWriter
import java.io.StringWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Inakamata hitilafu yoyote isiyoshughulikiwa (uncaught exception) kabla app
 * haijaanguka, na kuihifadhi kwenye SharedPreferences. Mara ya pili
 * unapofungua app, MainActivity itaonyesha taarifa hii KABLA ya kujaribu
 * kuanzisha UI ya kawaida — hivyo hata kama chanzo cha crash ni sehemu ya
 * mpangilio wa kawaida wa app, bado utaona ujumbe halisi wa hitilafu badala
 * ya app kuanguka kimya kimya.
 */
class CrashHandler private constructor(
    private val appContext: Context,
    private val defaultHandler: Thread.UncaughtExceptionHandler?
) : Thread.UncaughtExceptionHandler {

    override fun uncaughtException(thread: Thread, throwable: Throwable) {
        try {
            val sw = StringWriter()
            throwable.printStackTrace(PrintWriter(sw))
            val time = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
            val report = "Muda: $time\nThread: ${thread.name}\n\n$sw"
            appContext.getSharedPreferences("bulksms_crash", Context.MODE_PRIVATE)
                .edit()
                .putString("last_crash", report)
                .apply()
        } catch (e: Exception) {
            // Usiruhusu hitilafu ndani ya kikamataji chenyewe kuzuia utaratibu wa kawaida wa crash
        }
        defaultHandler?.uncaughtException(thread, throwable)
    }

    companion object {
        fun install(context: Context) {
            val current = Thread.getDefaultUncaughtExceptionHandler()
            if (current !is CrashHandler) {
                Thread.setDefaultUncaughtExceptionHandler(
                    CrashHandler(context.applicationContext, current)
                )
            }
        }

        fun getLastCrash(context: Context): String? =
            context.getSharedPreferences("bulksms_crash", Context.MODE_PRIVATE)
                .getString("last_crash", null)

        fun clearLastCrash(context: Context) {
            context.getSharedPreferences("bulksms_crash", Context.MODE_PRIVATE)
                .edit().remove("last_crash").apply()
        }
    }
}
