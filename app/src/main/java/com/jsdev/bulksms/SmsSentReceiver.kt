package com.jsdev.bulksms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.telephony.SmsManager

/**
 * Inapokea taarifa kutoka mfumo wa Android kuwa SMS "imetumwa" (imeondoka simu kuelekea network).
 * Hii SI uthibitisho kuwa mlengwa amepokea — ni uthibitisho kuwa simu imeituma kwenye mtandao.
 */
class SmsSentReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val phone = intent.getStringExtra("phone") ?: return
        val partIndex = intent.getIntExtra("partIndex", 0)
        val totalParts = intent.getIntExtra("totalParts", 1)

        val status = when (resultCode) {
            android.app.Activity.RESULT_OK -> "IMETUMWA"
            SmsManager.RESULT_ERROR_GENERIC_FAILURE -> "IMESHINDWA (hitilafu ya jumla)"
            SmsManager.RESULT_ERROR_NO_SERVICE -> "IMESHINDWA (hakuna huduma ya mtandao)"
            SmsManager.RESULT_ERROR_NULL_PDU -> "IMESHINDWA (data batili)"
            SmsManager.RESULT_ERROR_RADIO_OFF -> "IMESHINDWA (simu haina mtandao)"
            else -> "IMESHINDWA (kosa: $resultCode)"
        }
        SmsSendTracker.reportSentStatus(phone, partIndex, totalParts, status)
    }
}
