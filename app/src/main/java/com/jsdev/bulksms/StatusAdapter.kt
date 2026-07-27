package com.jsdev.bulksms

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

data class StatusRow(val recipient: Recipient, var status: String)

enum class StatusCategory { WAITING, SENDING, SENT, DELIVERED, FAILED }

fun categorize(status: String): StatusCategory = when {
    status.startsWith("IMEFIKA") -> StatusCategory.DELIVERED
    status == "IMETUMWA" -> StatusCategory.SENT
    status.startsWith("IMESHINDWA") || status.startsWith("HAIJAFIKA") -> StatusCategory.FAILED
    status.contains("inasubiri", ignoreCase = true) -> StatusCategory.WAITING
    else -> StatusCategory.SENDING
}

class StatusAdapter(
    private val fullList: MutableList<StatusRow>
) : RecyclerView.Adapter<StatusAdapter.ViewHolder>() {

    private var visibleList: MutableList<StatusRow> = fullList

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvRecipientName)
        val phone: TextView = view.findViewById(R.id.tvRecipientPhone)
        val badge: TextView = view.findViewById(R.id.tvStatusBadge)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_status, parent, false)
        return ViewHolder(v)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val row = visibleList[position]
        if (!row.recipient.name.isNullOrBlank()) {
            holder.name.visibility = View.VISIBLE
            holder.name.text = row.recipient.name
        } else {
            holder.name.visibility = View.GONE
        }
        holder.phone.text = row.recipient.phone
        holder.badge.text = row.status

        val ctx = holder.itemView.context
        when (categorize(row.status)) {
            StatusCategory.WAITING -> {
                holder.badge.setBackgroundResource(R.drawable.bg_status_waiting)
                holder.badge.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.status_waiting))
            }
            StatusCategory.SENDING -> {
                holder.badge.setBackgroundResource(R.drawable.bg_status_sending)
                holder.badge.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.status_sending))
            }
            StatusCategory.SENT -> {
                holder.badge.setBackgroundResource(R.drawable.bg_status_sent)
                holder.badge.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.status_sent))
            }
            StatusCategory.DELIVERED -> {
                holder.badge.setBackgroundResource(R.drawable.bg_status_delivered)
                holder.badge.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.status_delivered))
            }
            StatusCategory.FAILED -> {
                holder.badge.setBackgroundResource(R.drawable.bg_status_failed)
                holder.badge.setTextColor(androidx.core.content.ContextCompat.getColor(ctx, R.color.status_failed))
            }
        }
    }

    override fun getItemCount(): Int = visibleList.size

    fun updateStatus(phone: String, status: String) {
        fullList.firstOrNull { it.recipient.phone == phone }?.status = status
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        visibleList = if (query.isBlank()) {
            fullList
        } else {
            val q = query.trim().lowercase()
            fullList.filter {
                it.recipient.phone.contains(q) || (it.recipient.name?.lowercase()?.contains(q) == true)
            }.toMutableList()
        }
        notifyDataSetChanged()
    }

    fun failedPhones(): List<String> =
        fullList.filter { categorize(it.status) == StatusCategory.FAILED }.map { it.recipient.phone }

    fun counts(): Map<StatusCategory, Int> =
        fullList.groupingBy { categorize(it.status) }.eachCount()

    fun snapshotForExport(): List<Pair<String, String>> =
        fullList.map { it.recipient.phone to it.status }
}
