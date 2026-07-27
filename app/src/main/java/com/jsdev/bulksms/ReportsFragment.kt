package com.jsdev.bulksms

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton

class ReportsFragment : Fragment(R.layout.fragment_reports) {

    private val exportLauncher = registerForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? -> uri?.let { writeHistoryCsv(it) } }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<MaterialButton>(R.id.btnExportHistory).setOnClickListener {
            exportLauncher.launch("historia_kampeni.csv")
        }
        refresh(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { refresh(it) }
    }

    private fun refresh(view: View) {
        val db = DbHelper(requireContext())
        val agg = db.getAggregate()

        view.findViewById<TextView>(R.id.tvTotalCampaigns).text = agg.campaigns.toString()
        view.findViewById<TextView>(R.id.tvTotalContacts).text = agg.total.toString()
        view.findViewById<TextView>(R.id.tvTotalSent).text = agg.sent.toString()
        view.findViewById<TextView>(R.id.tvTotalDelivered).text = agg.delivered.toString()
        view.findViewById<TextView>(R.id.tvTotalFailed).text = agg.failed.toString()
        view.findViewById<TextView>(R.id.tvSuccessRate).text = "${agg.successRate}%"

        populatePieChart(view, agg)
        populateBarChart(view, agg)

        val recent = db.getRecent()
        populateLineChart(view, recent)

        val listView = view.findViewById<ListView>(R.id.lvHistory)
        val emptyView = view.findViewById<TextView>(R.id.tvEmptyHistory)

        if (recent.isEmpty()) {
            listView.visibility = View.GONE
            emptyView.visibility = View.VISIBLE
        } else {
            listView.visibility = View.VISIBLE
            emptyView.visibility = View.GONE
            val items = recent.map {
                "${it.date}\nJumla ${it.total} · Tumwa ${it.sent} · Fika ${it.delivered} · Shindwa ${it.failed}"
            }
            listView.adapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
        }
    }

    private fun populatePieChart(view: View, agg: DbHelper.Aggregate) {
        val delivered = agg.delivered
        val sentOnly = (agg.sent - agg.delivered).coerceAtLeast(0)
        val failed = agg.failed
        val pending = (agg.total - agg.sent - agg.failed).coerceAtLeast(0)

        val colorDelivered = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_delivered)
        val colorSent = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_sending)
        val colorFailed = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_failed)
        val colorPending = androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_waiting)

        val slices = listOf(
            PieChartView.Slice("Zimefika", delivered.toFloat(), colorDelivered),
            PieChartView.Slice("Zimetumwa (bila uthibitisho)", sentOnly.toFloat(), colorSent),
            PieChartView.Slice("Zimeshindwa", failed.toFloat(), colorFailed),
            PieChartView.Slice("Zinazosubiri", pending.toFloat(), colorPending)
        )
        view.findViewById<PieChartView>(R.id.pieChart).setData(slices, "${agg.successRate}%")

        val legend = view.findViewById<LinearLayout>(R.id.pieLegend)
        legend.removeAllViews()
        slices.filter { it.value > 0f }.forEach { slice ->
            val row = LinearLayout(requireContext()).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = android.view.Gravity.CENTER_VERTICAL
                setPadding(0, 4, 0, 4)
            }
            val dot = View(requireContext()).apply {
                val size = (10 * resources.displayMetrics.density).toInt()
                layoutParams = LinearLayout.LayoutParams(size, size)
                setBackgroundColor(slice.color)
            }
            val label = TextView(requireContext()).apply {
                text = "  ${slice.label}: ${slice.value.toInt()}"
                textSize = 12.5f
                setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.brand_muted))
            }
            row.addView(dot)
            row.addView(label)
            legend.addView(row)
        }
    }

    private fun populateBarChart(view: View, agg: DbHelper.Aggregate) {
        val bars = listOf(
            BarChartView.Bar("Jumla", agg.total.toFloat(), androidx.core.content.ContextCompat.getColor(requireContext(), R.color.brand_primary_dark)),
            BarChartView.Bar("Tumwa", agg.sent.toFloat(), androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_sending)),
            BarChartView.Bar("Fika", agg.delivered.toFloat(), androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_delivered)),
            BarChartView.Bar("Shindwa", agg.failed.toFloat(), androidx.core.content.ContextCompat.getColor(requireContext(), R.color.status_failed))
        )
        view.findViewById<BarChartView>(R.id.barChart).setData(bars)
    }

    private fun populateLineChart(view: View, recent: List<DbHelper.CampaignRow>) {
        // getRecent() inarudisha kutoka mpya kwenda zamani — tunaigeuza ili chati ionyeshe
        // mwenendo kutoka zamani kwenda mpya (kushoto kwenda kulia, kama chati za kawaida)
        val chronological = recent.reversed().takeLast(10)
        val rates = chronological.map { row ->
            if (row.sent > 0) (row.delivered.toFloat() / row.sent.toFloat()) * 100f else 0f
        }
        view.findViewById<LineChartView>(R.id.lineChart).setData(rates)
    }

    private fun writeHistoryCsv(uri: Uri) {
        try {
            val recent = DbHelper(requireContext()).getRecent(1000)
            requireContext().contentResolver.openOutputStream(uri)?.use { out ->
                val sb = StringBuilder("tarehe,jumla,zimetumwa,zimefika,zimeshindwa\n")
                recent.forEach {
                    sb.append(it.date).append(",").append(it.total).append(",")
                        .append(it.sent).append(",").append(it.delivered).append(",").append(it.failed).append("\n")
                }
                out.write(sb.toString().toByteArray())
            }
            Toast.makeText(requireContext(), "Historia imehifadhiwa", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Imeshindwa: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
}
