package com.jsdev.bulksms

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

/**
 * Inapokea taarifa kuwa mlengwa (simu ya mpokeaji) IMEPOKEA ujumbe kwa hakika.
 * Baadhi ya mitandao ya TZ (hasa wakati mpokeaji simu imezimwa/haina mtandao)
 * inaweza kuchelewa au kutoripoti hii kabisa — si dosari ya app.
 */
class SmsDeliveredReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val phone = intent.getStringExtra("phone") ?: return
        val partIndex = intent.getIntExtra("partIndex", 0)
        val totalParts = intent.getIntExtra("totalParts", 1)

        val status = when (resultCode) {
            Activity.RESULT_OK -> "IMEFIKA KWA MLENGWA"
            Activity.RESULT_CANCELED -> "HAIJAFIKA (imekataliwa/imeshindikana)"
            else -> "HAIJATHIBITISHWA"
        }
        SmsSendTracker.reportDeliveryStatus(phone, partIndex, totalParts, status)
    }
}
