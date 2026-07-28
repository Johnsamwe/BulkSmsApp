package com.jsdev.bulksms

import android.app.AlertDialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MainActivity ni "shell" inayosimamia Bottom Navigation na kubadilisha kati
 * ya fragments tano. Fragments zinajengwa kwa 'by lazy' kwa MAKUSUDI — hii
 * inahakikisha ukaguzi wa ripoti ya crash uliopita unafanyika KABLA ya
 * kujaribu kuunda fragment yoyote, hata kama chanzo cha crash kilikuwa ndani
 * ya ujenzi wa fragment yenyewe (mfano ComposeFragment).
 */
class MainActivity : AppCompatActivity() {

    private val homeFragment by lazy { HomeFragment() }
    private val composeFragment by lazy { ComposeFragment() }
    private val contactsFragment by lazy { ContactsFragment() }
    private val reportsFragment by lazy { ReportsFragment() }
    private val settingsFragment by lazy { SettingsFragment() }

    private var activeFragment: Fragment? = null

    private val pinLockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ukaguzi wa kwanza kabisa, kabla ya kugusa kitu kingine chochote:
        // je kulikuwa na crash mara ya mwisho? Kama ndiyo, onyesha taarifa
        // hiyo badala ya kujaribu UI ya kawaida (ambayo inaweza kuanguka tena).
        val crashReport = CrashHandler.getLastCrash(this)
        if (crashReport != null) {
            showCrashReportAndWait(crashReport)
            return
        }

        setContentView(R.layout.activity_main)
        setupFragmentsAndNav(savedInstanceState)
        checkLock()
    }

    private fun setupFragmentsAndNav(savedInstanceState: Bundle?) {
        val fm = supportFragmentManager
        if (savedInstanceState == null) {
            fm.beginTransaction()
                .add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragmentContainer, reportsFragment, "reports").hide(reportsFragment)
                .add(R.id.fragmentContainer, contactsFragment, "contacts").hide(contactsFragment)
                .add(R.id.fragmentContainer, composeFragment, "compose").hide(composeFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit()
            activeFragment = homeFragment
        }

        findViewById<BottomNavigationView>(R.id.bottomNav).setOnItemSelectedListener { item ->
            val target = when (item.itemId) {
                R.id.nav_home -> homeFragment
                R.id.nav_compose -> composeFragment
                R.id.nav_contacts -> contactsFragment
                R.id.nav_reports -> reportsFragment
                R.id.nav_settings -> settingsFragment
                else -> homeFragment
            }
            switchTo(target)
            true
        }
    }

    private fun showCrashReportAndWait(report: String) {
        AlertDialog.Builder(this)
            .setTitle("⚠️ App ilianguka mara ya mwisho")
            .setMessage("Hii ndiyo sababu halisi. Bofya 'Nakili Error' kisha bandika ujumbe kwa msaidizi wako ili arekebishe.\n\n$report")
            .setCancelable(false)
            .setPositiveButton("Nakili Error") { _, _ ->
                val cm = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                cm.setPrimaryClip(ClipData.newPlainText("crash_report", report))
                Toast.makeText(this, "Imenakiliwa", Toast.LENGTH_LONG).show()
            }
            .setNegativeButton("Futa na Endelea") { _, _ ->
                CrashHandler.clearLastCrash(this)
                recreate()
            }
            .show()
    }

    private fun switchTo(fragment: Fragment) {
        val current = activeFragment
        if (current === fragment) return
        val tx = supportFragmentManager.beginTransaction()
        current?.let { tx.hide(it) }
        tx.show(fragment)
        tx.commit()
        activeFragment = fragment
    }

    override fun onResume() {
        super.onResume()
        if (CrashHandler.getLastCrash(this) == null) {
            checkLock()
        }
    }

    override fun onPause() {
        super.onPause()
        PinManager.recordBackgroundTime()
    }

    private fun checkLock() {
        if (PinManager.isPinEnabled(this) && PinManager.shouldLock(this)) {
            pinLockLauncher.launch(Intent(this, PinLockActivity::class.java))
        }
    }
}
