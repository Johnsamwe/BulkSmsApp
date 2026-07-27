package com.jsdev.bulksms

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    private val exportBackupLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? -> uri?.let { writeBackup(it) } }

    private val importBackupLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? -> uri?.let { readBackup(it) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btnSecurity).setOnClickListener { openSecuritySettings(view) }
        view.findViewById<MaterialButton>(R.id.btnManageTemplates).setOnClickListener { manageTemplates() }
        view.findViewById<MaterialButton>(R.id.btnExportBackup).setOnClickListener {
            val filename = "bulksms_backup_${System.currentTimeMillis()}.json"
            exportBackupLauncher.launch(filename)
        }
        view.findViewById<MaterialButton>(R.id.btnImportBackup).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Rudisha Nakala")
                .setMessage("Hii itaongeza templates na historia kutoka kwenye faili la backup — haitafuta kilichopo sasa. Endelea?")
                .setPositiveButton("Endelea") { _, _ -> importBackupLauncher.launch("application/json") }
                .setNegativeButton("Ghairi", null)
                .show()
        }

        updatePinStatus(view)
    }

    private fun writeBackup(uri: Uri) {
        try {
            val json = BackupManager.exportToJson(requireContext())
            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                out.write(json.toByteArray())
            }
            Toast.makeText(requireContext(), "Nakala rudufu imehifadhiwa", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Imeshindwa kuhifadhi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun readBackup(uri: Uri) {
        try {
            val text = requireContext().contentResolver.openInputStream(uri)?.use { it.readBytes().toString(Charsets.UTF_8) }
                ?: throw IllegalStateException("Faili haliwezi kusomwa")
            val result = BackupManager.restoreFromJson(requireContext(), text)
            Toast.makeText(
                requireContext(),
                "Imerudishwa: templates ${result.templatesRestored}, kampeni ${result.campaignsRestored}",
                Toast.LENGTH_LONG
            ).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Faili si nakala rudufu sahihi: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onResume() {
        super.onResume()
        view?.let { updatePinStatus(it) }
    }

    private fun updatePinStatus(view: View) {
        val tv = view.findViewById<TextView>(R.id.tvPinStatus)
        tv.text = if (PinManager.isPinEnabled(requireContext())) {
            "PIN imewekwa — app itafungwa kila unapoifungua"
        } else {
            "PIN haijawekwa — app iko wazi kwa yeyote anayeigusa"
        }
    }

    private fun openSecuritySettings(view: View) {
        if (!PinManager.isPinEnabled(requireContext())) {
            promptSetPin(view)
            return
        }
        val options = arrayOf("Badilisha PIN", "Muda wa Auto-lock", "Zima PIN")
        AlertDialog.Builder(requireContext())
            .setTitle("Mipangilio ya Usalama")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> promptSetPin(view)
                    1 -> promptTimeout()
                    2 -> {
                        PinManager.clearPin(requireContext())
                        Toast.makeText(requireContext(), "PIN imezimwa", Toast.LENGTH_SHORT).show()
                        updatePinStatus(view)
                    }
                }
            }
            .show()
    }

    private fun promptSetPin(view: View) {
        val container = LinearLayout(requireContext()).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(48, 24, 48, 0)
        }
        val pin1 = EditText(requireContext()).apply {
            hint = "PIN mpya (tarakimu 4-6)"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        val pin2 = EditText(requireContext()).apply {
            hint = "Thibitisha PIN"
            inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD
        }
        container.addView(pin1)
        container.addView(pin2)

        AlertDialog.Builder(requireContext())
            .setTitle("Weka PIN")
            .setView(container)
            .setPositiveButton("Hifadhi") { _, _ ->
                val p1 = pin1.text.toString()
                val p2 = pin2.text.toString()
                when {
                    p1.length < 4 || p1.length > 6 ->
                        Toast.makeText(requireContext(), "PIN iwe na tarakimu 4 hadi 6", Toast.LENGTH_SHORT).show()
                    p1 != p2 ->
                        Toast.makeText(requireContext(), "PIN hazifanani", Toast.LENGTH_SHORT).show()
                    else -> {
                        PinManager.setPin(requireContext(), p1)
                        PinManager.markUnlocked()
                        Toast.makeText(requireContext(), "PIN imewekwa", Toast.LENGTH_SHORT).show()
                        updatePinStatus(view)
                    }
                }
            }
            .setNegativeButton("Ghairi", null)
            .show()
    }

    private fun promptTimeout() {
        val labels = arrayOf("Mara moja", "Sekunde 30", "Dakika 1", "Dakika 5", "Dakika 15")
        val values = longArrayOf(0L, 30_000L, 60_000L, 300_000L, 900_000L)
        AlertDialog.Builder(requireContext())
            .setTitle("Muda wa Auto-lock")
            .setItems(labels) { _, which ->
                PinManager.setTimeoutMs(requireContext(), values[which])
                Toast.makeText(requireContext(), "Imewekwa: ${labels[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun manageTemplates() {
        val prefs = requireContext().getSharedPreferences("bulksms_prefs", android.content.Context.MODE_PRIVATE)
        val names = (prefs.getStringSet("template_names", emptySet()) ?: emptySet()).toMutableList()
        if (names.isEmpty()) {
            Toast.makeText(requireContext(), "Hakuna template zilizohifadhiwa", Toast.LENGTH_SHORT).show()
            return
        }
        AlertDialog.Builder(requireContext())
            .setTitle("Templates Zilizohifadhiwa")
            .setItems(names.toTypedArray()) { _, which ->
                AlertDialog.Builder(requireContext())
                    .setTitle(names[which])
                    .setMessage(prefs.getString("template_${names[which]}", ""))
                    .setPositiveButton("Futa") { _, _ ->
                        val updated = names.toMutableSet().apply { remove(names[which]) }
                        prefs.edit()
                            .putStringSet("template_names", updated)
                            .remove("template_${names[which]}")
                            .apply()
                        Toast.makeText(requireContext(), "Imefutwa", Toast.LENGTH_SHORT).show()
                    }
                    .setNegativeButton("Funga", null)
                    .show()
            }
            .show()
    }
}
