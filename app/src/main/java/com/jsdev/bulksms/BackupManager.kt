package com.jsdev.bulksms

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Backup/Restore ya templates za ujumbe na historia ya kampeni, kwa muundo wa
 * JSON. Tumetumia org.json iliyomo ndani ya Android SDK yenyewe — hakuna
 * maktaba ya nje inayohitajika.
 *
 * KWA MAKUSUDI: PIN HAIHIFADHIWI kwenye backup, hata kama imewekwa — ni
 * uamuzi wa kiusalama. Mtumiaji akibadilisha kifaa, ataweka PIN mpya.
 */
object BackupManager {

    fun exportToJson(context: Context): String {
        val root = JSONObject()
        root.put("app", "BulkSmsApp - JS.Dev")
        root.put("backupVersion", 1)
        root.put("exportedAt", SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()))

        // Templates
        val prefs = context.getSharedPreferences("bulksms_prefs", Context.MODE_PRIVATE)
        val names = prefs.getStringSet("template_names", emptySet()) ?: emptySet()
        val templatesObj = JSONObject()
        names.forEach { name ->
            templatesObj.put(name, prefs.getString("template_$name", "") ?: "")
        }
        root.put("templates", templatesObj)

        // Historia ya kampeni
        val campaignsArr = JSONArray()
        DbHelper(context).getRecent(100000).forEach { row ->
            val obj = JSONObject()
            obj.put("date", row.date)
            obj.put("total", row.total)
            obj.put("sent", row.sent)
            obj.put("delivered", row.delivered)
            obj.put("failed", row.failed)
            campaignsArr.put(obj)
        }
        root.put("campaigns", campaignsArr)

        return root.toString(2)
    }

    data class RestoreResult(val templatesRestored: Int, val campaignsRestored: Int)

    /** @throws Exception kama faili si JSON sahihi la backup hili */
    fun restoreFromJson(context: Context, json: String): RestoreResult {
        val root = JSONObject(json)
        if (!root.has("templates") && !root.has("campaigns")) {
            throw IllegalArgumentException("Faili hili halionekani kuwa nakala rudufu sahihi ya app hii")
        }

        var templatesRestored = 0
        val prefs = context.getSharedPreferences("bulksms_prefs", Context.MODE_PRIVATE)
        val existingNames = (prefs.getStringSet("template_names", emptySet()) ?: emptySet()).toMutableSet()
        val editor = prefs.edit()

        root.optJSONObject("templates")?.let { templatesObj ->
            val keys = templatesObj.keys()
            while (keys.hasNext()) {
                val name = keys.next()
                editor.putString("template_$name", templatesObj.getString(name))
                existingNames.add(name)
                templatesRestored++
            }
        }
        editor.putStringSet("template_names", existingNames)
        editor.apply()

        var campaignsRestored = 0
        val db = DbHelper(context)
        root.optJSONArray("campaigns")?.let { arr ->
            for (i in 0 until arr.length()) {
                val obj = arr.getJSONObject(i)
                db.insertCampaignWithDate(
                    obj.optString("date", "—"),
                    obj.optInt("total", 0),
                    obj.optInt("sent", 0),
                    obj.optInt("delivered", 0),
                    obj.optInt("failed", 0)
                )
                campaignsRestored++
            }
        }

        return RestoreResult(templatesRestored, campaignsRestored)
    }
}
