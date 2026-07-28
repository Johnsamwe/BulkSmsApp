package com.jsdev.bulksms

/**
 * Kiratibu cha hali cha namba zote. MainActivity inajisajili hapa
 * ili kupokea taarifa live za "imetumwa" na "imefika" kwa kila namba,
 * hata baada ya ujumbe kugawanywa vipande (parts) kwa ujumbe mrefu.
 */
object SmsSendTracker {

    interface Listener {
        fun onSentUpdate(phone: String, status: String)
        fun onDeliveredUpdate(phone: String, status: String)
    }

    private var listener: Listener? = null

    // Inafuatilia sehemu (parts) ngapi za ujumbe mmoja zimeripoti, kwa kila namba
    private val sentPartsDone = HashMap<String, Int>()
    private val deliveredPartsDone = HashMap<String, Int>()

    fun setListener(l: Listener?) {
        listener = l
    }

    fun reset() {
        sentPartsDone.clear()
        deliveredPartsDone.clear()
    }

    fun reportSentStatus(phone: String, partIndex: Int, totalParts: Int, status: String) {
        if (!status.startsWith("IMETUMWA")) {
            listener?.onSentUpdate(phone, status)
            return
        }
        val done = (sentPartsDone[phone] ?: 0) + 1
        sentPartsDone[phone] = done
        if (done >= totalParts) {
            listener?.onSentUpdate(phone, "IMETUMWA")
        }
    }

    fun reportDeliveryStatus(phone: String, partIndex: Int, totalParts: Int, status: String) {
        if (status != "IMEFIKA KWA MLENGWA") {
            listener?.onDeliveredUpdate(phone, status)
            return
        }
        val done = (deliveredPartsDone[phone] ?: 0) + 1
        deliveredPartsDone[phone] = done
        if (done >= totalParts) {
            listener?.onDeliveredUpdate(phone, "IMEFIKA")
        }
    }
}
