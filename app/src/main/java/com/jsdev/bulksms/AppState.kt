package com.jsdev.bulksms

/**
 * Hali inayoshirikiwa kati ya fragments zote (Compose ↔ Wasiliani).
 * Tumetumia object rahisi badala ya ViewModel/LiveData kamili ili kupunguza
 * dependencies mpya na hatari ya build kushindwa — kwa ukubwa wa app hii,
 * hii inatosha kabisa kiutendaji.
 */
object AppState {
    var recipients: List<Recipient> = emptyList()
    var flaggedCount: Int = 0
    var lastFileName: String = ""
    var lastMessage: String = ""
}
