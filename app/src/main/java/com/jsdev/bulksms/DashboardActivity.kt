package com.jsdev.bulksms

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        val db = DbHelper(this)
        val agg = db.getAggregate()

        findViewById<TextView>(R.id.tvTotalCampaigns).text = agg.campaigns.toString()
        findViewById<TextView>(R.id.tvTotalContacts).text = agg.total.toString()
        findViewById<TextView>(R.id.tvTotalSent).text = agg.sent.toString()
        findViewById<TextView>(R.id.tvTotalDelivered).text = agg.delivered.toString()
        findViewById<TextView>(R.id.tvTotalFailed).text = agg.failed.toString()
        findViewById<TextView>(R.id.tvSuccessRate).text = "${agg.successRate}%"

        val recent = db.getRecent()
        val listView = findViewById<ListView>(R.id.lvHistory)
        val emptyView = findViewById<TextView>(R.id.tvEmptyHistory)

        if (recent.isEmpty()) {
            listView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            val items = recent.map {
                "${it.date}\nJumla ${it.total} · Tumwa ${it.sent} · Fika ${it.delivered} · Shindwa ${it.failed}"
            }
            listView.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, items)
        }
    }
}
