package com.jsdev.bulksms

import android.content.Intent
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/**
 * MainActivity sasa ni "shell" tu inayosimamia Bottom Navigation na kubadilisha
 * kati ya fragments tano. Kila fragment inabaki hai (hide/show, si replace) ili
 * hali yake isipotee ukibadilisha tab — muhimu hasa kwa ComposeFragment ambayo
 * huenda ikawa katikati ya kutuma kampeni ya SMS.
 */
class MainActivity : AppCompatActivity() {

    private val homeFragment = HomeFragment()
    private val composeFragment = ComposeFragment()
    private val contactsFragment = ContactsFragment()
    private val reportsFragment = ReportsFragment()
    private val settingsFragment = SettingsFragment()

    private var activeFragment: Fragment = homeFragment

    private val pinLockLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode != RESULT_OK) finishAffinity()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fm = supportFragmentManager
        if (savedInstanceState == null) {
            fm.beginTransaction()
                .add(R.id.fragmentContainer, settingsFragment, "settings").hide(settingsFragment)
                .add(R.id.fragmentContainer, reportsFragment, "reports").hide(reportsFragment)
                .add(R.id.fragmentContainer, contactsFragment, "contacts").hide(contactsFragment)
                .add(R.id.fragmentContainer, composeFragment, "compose").hide(composeFragment)
                .add(R.id.fragmentContainer, homeFragment, "home")
                .commit()
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

        checkLock()
    }

    private fun switchTo(fragment: Fragment) {
        if (activeFragment === fragment) return
        supportFragmentManager.beginTransaction()
            .hide(activeFragment)
            .show(fragment)
            .commit()
        activeFragment = fragment
    }

    override fun onResume() {
        super.onResume()
        checkLock()
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
