package com.jsdev.bulksms

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Database nyepesi ya SQLite (imejengwa ndani ya Android SDK yenyewe) kuhifadhi
 * historia ya kila kampeni ya SMS. Tumeepuka Room kwa makusudi hapa ili kupunguza
 * idadi ya dependencies mpya na hatari ya build kushindwa wakati wa ujenzi wa cloud.
 */
class DbHelper(context: Context) : SQLiteOpenHelper(context, "bulksms.db", null, 1) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE campaigns (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                date TEXT NOT NULL,
                total INTEGER NOT NULL,
                sent INTEGER NOT NULL,
                delivered INTEGER NOT NULL,
                failed INTEGER NOT NULL
            )
            """.trimIndent()
        )
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS campaigns")
        onCreate(db)
    }

    fun insertCampaign(total: Int, sent: Int, delivered: Int, failed: Int) {
        insertCampaignWithDate(
            SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date()),
            total, sent, delivered, failed
        )
    }

    fun insertCampaignWithDate(date: String, total: Int, sent: Int, delivered: Int, failed: Int) {
        val values = ContentValues().apply {
            put("date", date)
            put("total", total)
            put("sent", sent)
            put("delivered", delivered)
            put("failed", failed)
        }
        writableDatabase.use { it.insert("campaigns", null, values) }
    }

    data class Aggregate(
        val campaigns: Int, val total: Int, val sent: Int, val delivered: Int, val failed: Int
    ) {
        val successRate: Int get() = if (sent > 0) (delivered * 100 / sent) else 0
    }

    fun getAggregate(): Aggregate {
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT COUNT(*), COALESCE(SUM(total),0), COALESCE(SUM(sent),0), COALESCE(SUM(delivered),0), COALESCE(SUM(failed),0) FROM campaigns",
                null
            ).use { c ->
                if (c.moveToFirst()) {
                    return Aggregate(c.getInt(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4))
                }
            }
        }
        return Aggregate(0, 0, 0, 0, 0)
    }

    data class CampaignRow(val date: String, val total: Int, val sent: Int, val delivered: Int, val failed: Int)

    fun getRecent(limit: Int = 20): List<CampaignRow> {
        val list = ArrayList<CampaignRow>()
        readableDatabase.use { db ->
            db.rawQuery(
                "SELECT date, total, sent, delivered, failed FROM campaigns ORDER BY id DESC LIMIT ?",
                arrayOf(limit.toString())
            ).use { c ->
                while (c.moveToNext()) {
                    list.add(CampaignRow(c.getString(0), c.getInt(1), c.getInt(2), c.getInt(3), c.getInt(4)))
                }
            }
        }
        return list
    }
}
