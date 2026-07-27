package com.jsdev.bulksms

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.bottomnavigation.BottomNavigationView

class HomeFragment : Fragment(R.layout.fragment_home) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btnGoCompose).setOnClickListener {
            navigateTo(R.id.nav_compose)
        }
        view.findViewById<MaterialButton>(R.id.btnGoReports).setOnClickListener {
            navigateTo(R.id.nav_reports)
        }
        refreshStats(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { refreshStats(it) }
    }

    private fun refreshStats(view: View) {
        val db = DbHelper(requireContext())
        val agg = db.getAggregate()
        view.findViewById<TextView>(R.id.tvHomeCampaigns).text = agg.campaigns.toString()
        view.findViewById<TextView>(R.id.tvHomeSent).text = agg.sent.toString()
        view.findViewById<TextView>(R.id.tvHomeSuccessRate).text = "${agg.successRate}%"

        val recent = db.getRecent(1)
        val tvLast = view.findViewById<TextView>(R.id.tvLastCampaign)
        tvLast.text = if (recent.isEmpty()) {
            "Bado hujatuma kampeni yoyote"
        } else {
            val c = recent.first()
            "${c.date}\nJumla ${c.total} · Tumwa ${c.sent} · Fika ${c.delivered} · Shindwa ${c.failed}"
        }
    }

    private fun navigateTo(itemId: Int) {
        requireActivity().findViewById<BottomNavigationView>(R.id.bottomNav).selectedItemId = itemId
    }
}
