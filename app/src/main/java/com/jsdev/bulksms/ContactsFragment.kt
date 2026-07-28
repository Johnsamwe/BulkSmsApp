package com.jsdev.bulksms

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import android.widget.TextView
import androidx.fragment.app.Fragment

class ContactsFragment : Fragment(R.layout.fragment_contacts) {

    private var fullList: List<Recipient> = emptyList()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val search = view.findViewById<EditText>(R.id.etContactSearch)
        search.addTextChangedListener(object : android.text.TextWatcher {
            override fun afterTextChanged(s: android.text.Editable?) { applyFilter(view, s?.toString().orEmpty()) }
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
        })

        refreshFromState(view)
    }

    override fun onResume() {
        super.onResume()
        view?.let { refreshFromState(it) }
    }

    private fun refreshFromState(view: View) {
        fullList = AppState.recipients
        val countText = view.findViewById<TextView>(R.id.tvContactCount)
        val emptyView = view.findViewById<TextView>(R.id.tvContactsEmpty)
        val listView = view.findViewById<ListView>(R.id.lvContacts)

        if (fullList.isEmpty()) {
            countText.text = "Hakuna namba zilizopakiwa bado"
            emptyView.visibility = View.VISIBLE
            listView.visibility = View.GONE
        } else {
            val warn = if (AppState.flaggedCount > 0) " · Zisizoeleweka: ${AppState.flaggedCount}" else ""
            countText.text = "${AppState.lastFileName}: ${fullList.size} namba sahihi$warn"
            emptyView.visibility = View.GONE
            listView.visibility = View.VISIBLE
            applyFilter(view, "")
        }
    }

    private fun applyFilter(view: View, query: String) {
        val q = query.trim().lowercase()
        val filtered = if (q.isEmpty()) fullList else fullList.filter {
            it.phone.contains(q) || (it.name?.lowercase()?.contains(q) == true)
        }
        val items = filtered.map { r -> if (!r.name.isNullOrBlank()) "${r.name}\n${r.phone}" else r.phone }
        view.findViewById<ListView>(R.id.lvContacts).adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, items)
    }
}
