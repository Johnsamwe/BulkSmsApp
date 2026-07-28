package com.jsdev.bulksms

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.ContactsContract
import android.provider.OpenableColumns
import android.telephony.SmsManager
import android.telephony.SubscriptionManager
import android.view.View
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Kurasa ya "Tuma" — hii ndiyo moyo wa app: kupakia namba, kuandika ujumbe,
 * kuchagua SIM, na kutuma SMS kwa wote. Ilikuwa MainActivity nzima hapo awali;
 * sasa ni Fragment moja kati ya tano, ikibaki na uwezo wake wote bila kupunguzwa.
 */
class ComposeFragment : Fragment(R.layout.fragment_compose), SmsSendTracker.Listener {

    private lateinit var tvFileInfo: TextView
    private lateinit var etMessage: TextInputEditText
    private lateinit var tvCharCount: TextView
    private lateinit var etDelay: EditText
    private lateinit var etBatchSize: EditText
    private lateinit var spinnerSim: Spinner
    private lateinit var btnSend: MaterialButton
    private lateinit var btnPauseResume: MaterialButton
    private lateinit var btnStop: MaterialButton
    private lateinit var btnRetryFailed: MaterialButton
    private lateinit var progressBar: ProgressBar
    private lateinit var tvProgress: TextView
    private lateinit var statValid: TextView
    private lateinit var statSent: TextView
    private lateinit var statDelivered: TextView
    private lateinit var statFailed: TextView
    private lateinit var etSearch: EditText
    private lateinit var rvStatus: RecyclerView
    private lateinit var btnExportReport: MaterialButton

    private var recipients: List<Recipient> = emptyList()
    private lateinit var statusRows: MutableList<StatusRow>
    private lateinit var statusAdapter: StatusAdapter

    private val simSubscriptionIds = ArrayList<Int?>()

    private var isSending = false
    private var isPaused = false
    private var isStopped = false
    private var sendQueue: List<Recipient> = emptyList()
    private var sendIndex = 0

    private val handler = Handler(Looper.getMainLooper())
    private val prefs by lazy { requireContext().getSharedPreferences("bulksms_prefs", Context.MODE_PRIVATE) }

    private val filePicker = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { loadFile(it) }
    }

    private val smsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) beginCampaign(recipients) else
            Toast.makeText(requireContext(), "Ruhusa ya SMS inahitajika ili kutuma", Toast.LENGTH_LONG).show()
    }

    private val phoneStatePermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { loadSimOptions() }

    private val contactsPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) openContactsPicker() else
            Toast.makeText(requireContext(), "Ruhusa ya Anwani inahitajika ili kuagiza namba", Toast.LENGTH_LONG).show()
    }

    private var scheduledRunnable: Runnable? = null
    private var scheduledTimeMillis: Long = 0L

    private val exportCsvLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        uri?.let { writeReportTo(it) }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvFileInfo = view.findViewById(R.id.tvFileInfo)
        etMessage = view.findViewById(R.id.etMessage)
        tvCharCount = view.findViewById(R.id.tvCharCount)
        etDelay = view.findViewById(R.id.etDelay)
        etBatchSize = view.findViewById(R.id.etBatchSize)
        spinnerSim = view.findViewById(R.id.spinnerSim)
        btnSend = view.findViewById(R.id.btnSend)
        btnPauseResume = view.findViewById(R.id.btnPauseResume)
        btnStop = view.findViewById(R.id.btnStop)
        btnRetryFailed = view.findViewById(R.id.btnRetryFailed)
        progressBar = view.findViewById(R.id.progressBar)
        tvProgress = view.findViewById(R.id.tvProgress)
        statValid = view.findViewById(R.id.statValid)
        statSent = view.findViewById(R.id.statSent)
        statDelivered = view.findViewById(R.id.statDelivered)
        statFailed = view.findViewById(R.id.statFailed)
        etSearch = view.findViewById(R.id.etSearch)
        rvStatus = view.findViewById(R.id.rvStatus)
        btnExportReport = view.findViewById(R.id.btnExportReport)

        statusRows = mutableListOf()
        statusAdapter = StatusAdapter(statusRows)
        rvStatus.layoutManager = LinearLayoutManager(requireContext())
        rvStatus.adapter = statusAdapter
        rvStatus.isNestedScrollingEnabled = false

        view.findViewById<MaterialButton>(R.id.btnPickFile).setOnClickListener { filePicker.launch("*/*") }
        view.findViewById<MaterialButton>(R.id.btnPickContacts).setOnClickListener { onPickContactsClicked() }

        etMessage.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) {
                val len = s?.length ?: 0
                val parts = if (len == 0) 1 else ((len - 1) / 160) + 1
                tvCharCount.text = "Herufi: $len (SMS $parts)"
            }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        etSearch.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { statusAdapter.filter(s?.toString().orEmpty()) }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        btnSend.setOnClickListener { onSendClicked() }
        view.findViewById<MaterialButton>(R.id.btnSchedule).setOnClickListener { onScheduleClicked() }
        btnPauseResume.setOnClickListener { togglePause() }
        btnStop.setOnClickListener { stopCampaign() }
        btnRetryFailed.setOnClickListener { retryFailed() }
        btnExportReport.setOnClickListener { exportCsvLauncher.launch("ripoti_kampeni.csv") }
        view.findViewById<MaterialButton>(R.id.btnSaveTemplate).setOnClickListener { saveTemplate() }
        view.findViewById<MaterialButton>(R.id.btnLoadTemplate).setOnClickListener { loadTemplateDialog() }

        loadSimOptions()
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_PHONE_STATE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            phoneStatePermissionLauncher.launch(android.Manifest.permission.READ_PHONE_STATE)
        }

        // Kama tayari kuna namba zilizopakiwa kwenye AppState (mfano mtumiaji alitoka na kurudi), zirudishe
        if (AppState.recipients.isNotEmpty() && recipients.isEmpty()) {
            recipients = AppState.recipients
            statusRows.addAll(recipients.map { StatusRow(it, "Inasubiri") })
            statusAdapter.notifyDataSetChanged()
            tvFileInfo.text = "${AppState.lastFileName} — Sahihi: ${recipients.size}"
            updateStatsUi()
        }
    }

    override fun onResume() {
        super.onResume()
        SmsSendTracker.setListener(this)
    }

    override fun onPause() {
        super.onPause()
        SmsSendTracker.setListener(null)
    }

    // ---------- SIM / laini ----------

    private fun loadSimOptions() {
        val labels = ArrayList<String>()
        simSubscriptionIds.clear()
        labels.add("Chaguo-msingi la simu")
        simSubscriptionIds.add(null)

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_PHONE_STATE)
            == PackageManager.PERMISSION_GRANTED
        ) {
            try {
                val sm = requireContext().getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE) as SubscriptionManager
                val list = sm.activeSubscriptionInfoList
                list?.forEach { info ->
                    val carrier = info.carrierName?.toString()?.takeIf { it.isNotBlank() }
                        ?: "SIM ${info.simSlotIndex + 1}"
                    labels.add("SIM ${info.simSlotIndex + 1} — $carrier")
                    simSubscriptionIds.add(info.subscriptionId)
                }
            } catch (e: SecurityException) {
                // Ruhusa haitoshi — tutabaki na "Chaguo-msingi" pekee
            }
        }
        spinnerSim.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, labels)
    }

    private fun smsManagerForSelection(): SmsManager {
        val subId = simSubscriptionIds.getOrNull(spinnerSim.selectedItemPosition)
        return if (subId != null && android.os.Build.VERSION.SDK_INT >= 22) {
            try {
                SmsManager.getSmsManagerForSubscriptionId(subId)
            } catch (e: Exception) {
                SmsManager.getDefault()
            }
        } else {
            SmsManager.getDefault()
        }
    }

    // ---------- Kusoma faili ----------

    private fun loadFile(uri: Uri) {
        val name = queryFileName(uri) ?: "faili"
        tvFileInfo.text = "Inasoma: $name ..."
        try {
            val result = RecipientParser.parse(requireContext(), uri, name)
            recipients = result.recipients

            statusRows.clear()
            statusRows.addAll(result.recipients.map { StatusRow(it, "Inasubiri") })
            statusAdapter.notifyDataSetChanged()
            updateStatsUi()

            val warn = if (result.flagged.isNotEmpty()) " · Zisizoeleweka: ${result.flagged.size}" else ""
            val nameNote = if (result.recipients.any { it.name != null }) " · majina yamegunduliwa (personalization tayari)" else ""
            tvFileInfo.text = "$name — Sahihi: ${result.recipients.size}$warn$nameNote"

            // Shiriki na tab ya Wasiliani
            AppState.recipients = result.recipients
            AppState.flaggedCount = result.flagged.size
            AppState.lastFileName = name

            if (result.flagged.isNotEmpty()) {
                android.util.Log.w("BulkSms", "Zilizoshindikana: ${result.flagged.joinToString(" | ")}")
            }
            if (result.recipients.isEmpty()) {
                Toast.makeText(requireContext(), "Hakuna namba sahihi zilizopatikana kwenye faili hili", Toast.LENGTH_LONG).show()
            }
        } catch (e: Exception) {
            tvFileInfo.text = "Hitilafu: ${e.message}"
            Toast.makeText(requireContext(), e.message ?: "Imeshindwa kusoma faili", Toast.LENGTH_LONG).show()
        }
    }

    private fun queryFileName(uri: Uri): String? {
        var name: String? = null
        requireContext().contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { c ->
            if (c.moveToFirst()) {
                val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (idx >= 0) name = c.getString(idx)
            }
        }
        return name
    }

    // ---------- Kuagiza kutoka Anwani za Simu ----------

    private fun onPickContactsClicked() {
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.READ_CONTACTS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            contactsPermissionLauncher.launch(android.Manifest.permission.READ_CONTACTS)
        } else {
            openContactsPicker()
        }
    }

    private fun openContactsPicker() {
        val contacts = ArrayList<Recipient>()
        val seen = HashSet<String>()
        try {
            val cursor = requireContext().contentResolver.query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                arrayOf(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME, ContactsContract.CommonDataKinds.Phone.NUMBER),
                null, null,
                "${ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME} ASC"
            )
            cursor?.use { c ->
                val nameIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
                val numIdx = c.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
                while (c.moveToNext()) {
                    val rawName = if (nameIdx >= 0) c.getString(nameIdx) else null
                    val rawNumber = if (numIdx >= 0) c.getString(numIdx) else null ?: continue
                    val res = PhoneNormalizer.normalize(rawNumber ?: "")
                    if (res.ok && seen.add(res.value)) {
                        contacts.add(Recipient(rawName, res.value))
                    }
                }
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Imeshindwa kusoma anwani: ${e.message}", Toast.LENGTH_LONG).show()
            return
        }

        if (contacts.isEmpty()) {
            Toast.makeText(requireContext(), "Hakuna namba sahihi za Tanzania zilizopatikana kwenye anwani zako", Toast.LENGTH_LONG).show()
            return
        }

        val labels = contacts.map { "${it.name ?: "(Bila jina)"} — ${it.phone}" }.toTypedArray()
        val checked = BooleanArray(contacts.size)

        AlertDialog.Builder(requireContext())
            .setTitle("Chagua Anwani (${contacts.size} zimepatikana)")
            .setMultiChoiceItems(labels, checked) { _, which, isChecked -> checked[which] = isChecked }
            .setPositiveButton("Ongeza Zilizochaguliwa") { _, _ ->
                val selected = contacts.filterIndexed { i, _ -> checked[i] }
                if (selected.isEmpty()) {
                    Toast.makeText(requireContext(), "Hujachagua namba yoyote", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                mergeRecipients(selected, "Anwani za Simu")
            }
            .setNegativeButton("Ghairi", null)
            .show()
    }

    private fun mergeRecipients(newOnes: List<Recipient>, sourceLabel: String) {
        val seen = recipients.map { it.phone }.toHashSet()
        val merged = recipients.toMutableList()
        var added = 0
        newOnes.forEach {
            if (seen.add(it.phone)) {
                merged.add(it)
                added++
            }
        }
        recipients = merged
        statusRows.clear()
        statusRows.addAll(merged.map { StatusRow(it, "Inasubiri") })
        statusAdapter.notifyDataSetChanged()
        updateStatsUi()

        AppState.recipients = merged
        AppState.lastFileName = sourceLabel
        tvFileInfo.text = "Jumla: ${merged.size} namba (ziliongezwa $added kutoka $sourceLabel)"

        Toast.makeText(requireContext(), "Zimeongezwa namba $added", Toast.LENGTH_SHORT).show()
    }

    // ---------- Scheduler (kupanga muda wa kutuma baadaye) ----------

    private fun onScheduleClicked() {
        if (scheduledRunnable != null) {
            AlertDialog.Builder(requireContext())
                .setTitle("Kampeni Tayari Imepangwa")
                .setMessage("Kuna kampeni iliyopangwa tayari. Ungependa kuighairi?")
                .setPositiveButton("Ghairi Ratiba") { _, _ -> cancelSchedule() }
                .setNegativeButton("Acha Iendelee", null)
                .show()
            return
        }
        if (recipients.isEmpty()) {
            Toast.makeText(requireContext(), "Pakia faili la namba kwanza", Toast.LENGTH_SHORT).show()
            return
        }
        if (etMessage.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Andika ujumbe kwanza", Toast.LENGTH_SHORT).show()
            return
        }

        val now = Calendar.getInstance()
        DatePickerDialog(requireContext(), { _, year, month, day ->
            val timePicker = TimePickerDialog(requireContext(), { _, hour, minute ->
                val target = Calendar.getInstance().apply {
                    set(year, month, day, hour, minute, 0)
                }
                if (target.timeInMillis <= System.currentTimeMillis()) {
                    Toast.makeText(requireContext(), "Chagua muda ujao, si uliopita", Toast.LENGTH_LONG).show()
                } else {
                    scheduleCampaign(target.timeInMillis)
                }
            }, now.get(Calendar.HOUR_OF_DAY), now.get(Calendar.MINUTE), true)
            timePicker.show()
        }, now.get(Calendar.YEAR), now.get(Calendar.MONTH), now.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun scheduleCampaign(timeMillis: Long) {
        scheduledTimeMillis = timeMillis
        val delay = timeMillis - System.currentTimeMillis()

        val runnable = Runnable {
            scheduledRunnable = null
            val tvSchedule = view?.findViewById<TextView>(R.id.tvScheduleStatus)
            tvSchedule?.visibility = View.GONE
            if (recipients.isNotEmpty() && !etMessage.text.isNullOrBlank()) {
                onSendClicked()
            }
        }
        scheduledRunnable = runnable
        handler.postDelayed(runnable, delay)

        val fmt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(java.util.Date(timeMillis))
        val tvSchedule = view?.findViewById<TextView>(R.id.tvScheduleStatus)
        tvSchedule?.visibility = View.VISIBLE
        tvSchedule?.text = "🕒 Kampeni imepangwa: $fmt — bofya 'Panga Muda' tena kughairi"
        Toast.makeText(requireContext(), "Kampeni imepangwa kwa $fmt", Toast.LENGTH_LONG).show()
    }

    private fun cancelSchedule() {
        scheduledRunnable?.let { handler.removeCallbacks(it) }
        scheduledRunnable = null
        view?.findViewById<TextView>(R.id.tvScheduleStatus)?.visibility = View.GONE
        Toast.makeText(requireContext(), "Ratiba imeghairiwa", Toast.LENGTH_SHORT).show()
    }

    // ---------- Templates za ujumbe ----------

    private fun saveTemplate() {
        val body = etMessage.text?.toString().orEmpty()
        if (body.isBlank()) {
            Toast.makeText(requireContext(), "Andika ujumbe kwanza", Toast.LENGTH_SHORT).show()
            return
        }
        val input = EditText(requireContext())
        input.hint = "Jina la template (mfano: Ukumbusho wa Deni)"
        AlertDialog.Builder(requireContext())
            .setTitle("Hifadhi Template")
            .setView(input)
            .setPositiveButton("Hifadhi") { _, _ ->
                val label = input.text.toString().trim().ifBlank { "Template ${System.currentTimeMillis()}" }
                val names = (prefs.getStringSet("template_names", emptySet()) ?: emptySet()).toMutableSet()
                names.add(label)
                prefs.edit()
                    .putStringSet("template_names", names)
                    .putString("template_$label", body)
                    .apply()
                Toast.makeText(requireContext(), "Template imehifadhiwa", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Ghairi", null)
            .show()
    }

    private fun loadTemplateDialog() {
        val names = (prefs.getStringSet("template_names", emptySet()) ?: emptySet()).toList()
        if (names.isEmpty()) {
            Toast.makeText(requireContext(), "Hakuna template zilizohifadhiwa bado", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Chagua Template")
            .setItems(names.toTypedArray()) { _, which ->
                val body = prefs.getString("template_${names[which]}", "")
                etMessage.setText(body)
            }
            .show()
    }

    // ---------- Kuanza / kusimamisha kampeni ----------

    private fun onSendClicked() {
        if (isSending) {
            Toast.makeText(requireContext(), "Kampeni tayari inaendelea", Toast.LENGTH_SHORT).show()
            return
        }
        if (!requireContext().packageManager.hasSystemFeature(android.content.pm.PackageManager.FEATURE_TELEPHONY)) {
            Toast.makeText(
                requireContext(),
                "Kifaa hiki hakina uwezo wa kutuma SMS (SIM). App hii inahitaji simu yenye SIM na mtandao wa GSM.",
                Toast.LENGTH_LONG
            ).show()
            return
        }
        if (recipients.isEmpty()) {
            Toast.makeText(requireContext(), "Pakia faili la namba kwanza", Toast.LENGTH_SHORT).show()
            return
        }
        if (etMessage.text.isNullOrBlank()) {
            Toast.makeText(requireContext(), "Andika ujumbe kwanza", Toast.LENGTH_SHORT).show()
            return
        }

        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.SEND_SMS)
            != PackageManager.PERMISSION_GRANTED
        ) {
            smsPermissionLauncher.launch(android.Manifest.permission.SEND_SMS)
        } else {
            beginCampaign(recipients)
        }
    }

    private fun beginCampaign(list: List<Recipient>) {
        isSending = true
        isPaused = false
        isStopped = false
        sendQueue = list
        sendIndex = 0
        SmsSendTracker.reset()

        statusRows.clear()
        statusRows.addAll(list.map { StatusRow(it, "Inasubiri") })
        statusAdapter.notifyDataSetChanged()
        updateStatsUi()

        btnSend.isEnabled = false
        btnSend.text = "Inatuma..."
        btnPauseResume.isEnabled = true
        btnPauseResume.text = "Simamisha"
        btnStop.isEnabled = true
        btnRetryFailed.isEnabled = false

        sendNext()
    }

    private fun togglePause() {
        isPaused = !isPaused
        btnPauseResume.text = if (isPaused) "Endelea" else "Simamisha"
        if (!isPaused) sendNext()
    }

    private fun stopCampaign() {
        isStopped = true
        isSending = false
        isPaused = false
        handler.removeCallbacksAndMessages(null)
        btnSend.isEnabled = true
        btnSend.text = "Anza Kutuma SMS Kwa Wote"
        btnPauseResume.isEnabled = false
        btnPauseResume.text = "Simamisha"
        btnStop.isEnabled = false
        tvProgress.text = "Kampeni imekomeshwa na mtumiaji ($sendIndex kati ya ${sendQueue.size})"
        if (statusAdapter.failedPhones().isNotEmpty()) btnRetryFailed.isEnabled = true
    }

    private fun retryFailed() {
        val failed = statusAdapter.failedPhones()
        if (failed.isEmpty()) return
        val retryList = recipients.filter { it.phone in failed }
        beginCampaign(retryList)
    }

    private fun sendNext() {
        if (isStopped || isPaused) return

        if (sendIndex >= sendQueue.size) {
            isSending = false
            btnSend.isEnabled = true
            btnSend.text = "Anza Kutuma SMS Kwa Wote"
            btnPauseResume.isEnabled = false
            btnPauseResume.text = "Simamisha"
            btnStop.isEnabled = false
            tvProgress.text = "Imekamilika: ${sendQueue.size} kati ya ${sendQueue.size}"
            if (statusAdapter.failedPhones().isNotEmpty()) btnRetryFailed.isEnabled = true
            saveCampaignToHistory()
            Toast.makeText(requireContext(), "Kampeni imekamilika!", Toast.LENGTH_LONG).show()
            return
        }

        val recipient = sendQueue[sendIndex]
        val template = etMessage.text?.toString().orEmpty()
        val personalized = template.replace("{jina}", recipient.name ?: "Mteja", ignoreCase = true)
        sendSingleSms(recipient.phone, personalized)

        sendIndex++
        progressBar.progress = (sendIndex * 100) / sendQueue.size
        tvProgress.text = "Kutuma: $sendIndex kati ya ${sendQueue.size}"
        updateStatsUi()

        val delaySec = etDelay.text.toString().toIntOrNull()?.coerceAtLeast(1) ?: 2
        val batchSize = etBatchSize.text.toString().toIntOrNull()?.coerceAtLeast(1) ?: 20
        val extraPause = if (sendIndex % batchSize == 0) 8_000L else 0L

        handler.postDelayed({ sendNext() }, (delaySec * 1000).toLong() + extraPause)
    }

    private fun sendSingleSms(phone: String, message: String) {
        val ctx = requireContext()
        val smsManager = smsManagerForSelection()
        val parts: ArrayList<String> = smsManager.divideMessage(message)
        val totalParts = parts.size

        val sentIntents = ArrayList<PendingIntent>()
        val deliveredIntents = ArrayList<PendingIntent>()

        for (i in 0 until totalParts) {
            val sentIntent = Intent(ctx, SmsSentReceiver::class.java).apply {
                putExtra("phone", phone); putExtra("partIndex", i); putExtra("totalParts", totalParts)
            }
            val deliveredIntent = Intent(ctx, SmsDeliveredReceiver::class.java).apply {
                putExtra("phone", phone); putExtra("partIndex", i); putExtra("totalParts", totalParts)
            }
            val flags = PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            sentIntents.add(PendingIntent.getBroadcast(ctx, "$phone-s-$i".hashCode(), sentIntent, flags))
            deliveredIntents.add(PendingIntent.getBroadcast(ctx, "$phone-d-$i".hashCode(), deliveredIntent, flags))
        }

        try {
            smsManager.sendMultipartTextMessage(phone, null, parts, sentIntents, deliveredIntents)
            statusAdapter.updateStatus(phone, "Inatumwa...")
        } catch (e: Exception) {
            statusAdapter.updateStatus(phone, "IMESHINDWA: ${e.message}")
        }
        updateStatsUi()
    }

    // ---------- Stats na status live ----------

    private fun updateStatsUi() {
        val counts = statusAdapter.counts()
        statValid.text = sendQueue.size.takeIf { it > 0 }?.toString() ?: recipients.size.toString()
        statSent.text = ((counts[StatusCategory.SENT] ?: 0) + (counts[StatusCategory.DELIVERED] ?: 0)).toString()
        statDelivered.text = (counts[StatusCategory.DELIVERED] ?: 0).toString()
        statFailed.text = (counts[StatusCategory.FAILED] ?: 0).toString()
    }

    override fun onSentUpdate(phone: String, status: String) {
        activity?.runOnUiThread {
            statusAdapter.updateStatus(phone, status)
            updateStatsUi()
        }
    }

    override fun onDeliveredUpdate(phone: String, status: String) {
        activity?.runOnUiThread {
            statusAdapter.updateStatus(phone, status)
            updateStatsUi()
        }
    }

    // ---------- Historia na Ripoti ----------

    private fun saveCampaignToHistory() {
        val counts = statusAdapter.counts()
        val sent = (counts[StatusCategory.SENT] ?: 0) + (counts[StatusCategory.DELIVERED] ?: 0)
        val delivered = counts[StatusCategory.DELIVERED] ?: 0
        val failed = counts[StatusCategory.FAILED] ?: 0
        try {
            DbHelper(requireContext()).insertCampaign(sendQueue.size, sent, delivered, failed)
        } catch (e: Exception) {
            android.util.Log.w("BulkSms", "Imeshindwa kuhifadhi historia: ${e.message}")
        }
    }

    private fun writeReportTo(uri: Uri) {
        try {
            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                val sb = StringBuilder("namba,status\n")
                statusAdapter.snapshotForExport().forEach { (phone, status) ->
                    sb.append(phone).append(",").append(status.replace(",", ";")).append("\n")
                }
                out.write(sb.toString().toByteArray())
            }
            Toast.makeText(requireContext(), "Ripoti imehifadhiwa", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Imeshindwa kuhifadhi ripoti: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
