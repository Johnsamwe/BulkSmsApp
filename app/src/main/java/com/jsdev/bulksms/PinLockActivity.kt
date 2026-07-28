package com.jsdev.bulksms

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class PinLockActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pin_lock)

        val etPin = findViewById<EditText>(R.id.etPin)
        val btnUnlock = findViewById<MaterialButton>(R.id.btnUnlock)

        btnUnlock.setOnClickListener { attemptUnlock(etPin) }
        etPin.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                attemptUnlock(etPin); true
            } else false
        }
    }

    private fun attemptUnlock(etPin: EditText) {
        val pin = etPin.text.toString()
        if (PinManager.verifyPin(this, pin)) {
            PinManager.markUnlocked()
            setResult(RESULT_OK)
            finish()
        } else {
            Toast.makeText(this, "PIN si sahihi, jaribu tena", Toast.LENGTH_SHORT).show()
            etPin.text.clear()
        }
    }

    // Hairuhusiwi kutoka kwenye lock screen kwa 'back' — funga app nzima badala yake
    override fun onBackPressed() {
        finishAffinity()
    }
}
