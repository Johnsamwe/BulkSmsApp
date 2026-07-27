package com.jsdev.bulksms

import android.content.Context
import android.net.Uri
import java.io.BufferedReader
import java.io.InputStreamReader

data class Recipient(
    val name: String?,
    val phone: String
)

data class ParseResult(
    val recipients: List<Recipient>,
    val flagged: List<String>,
    val totalRowsRead: Int
)

/**
 * Inasoma faili la namba za simu — inakubali .xlsx, .csv, na .txt.
 * Inatambua yenyewe kama kuna safu ya "jina" pembeni ya namba (kwa ajili ya
 * ujumbe wa kibinafsi wenye {jina}), na inasafisha kila namba kwa kutumia
 * PhoneNormalizer. Namba zisizoeleweka HAZIPOTEI kimya kimya — zinarudishwa
 * kwenye orodha ya 'flagged' ili mtumiaji aziangalie.
 */
object RecipientParser {

    fun parse(context: Context, uri: Uri, fileName: String): ParseResult {
        val lower = fileName.lowercase()
        val rows: List<List<String>> = when {
            lower.endsWith(".xlsx") -> XlsxReader.readFirstSheetRows(context, uri)
            lower.endsWith(".xls") -> throw IllegalArgumentException(
                "Muundo wa zamani wa .xls hauungwi mkono. Tafadhali hifadhi faili kama .xlsx au .csv kisha jaribu tena."
            )
            else -> readDelimitedRows(context, uri)
        }
        return buildRecipients(rows)
    }

    private fun readDelimitedRows(context: Context, uri: Uri): List<List<String>> {
        val rows = ArrayList<List<String>>()
        context.contentResolver.openInputStream(uri)?.use { input ->
            BufferedReader(InputStreamReader(input)).use { reader ->
                val text = reader.readText()
                val hasCommaLines = text.contains(",")
                if (hasCommaLines) {
                    text.split(Regex("[\\r\\n]+")).forEach { line ->
                        if (line.isNotBlank()) rows.add(line.split(",").map { it.trim() })
                    }
                } else {
                    // Namba zilizotenganishwa kwa koma/semicolon/mstari mpya bila muundo wa column
                    val tokens = text.split(Regex("[,;\\n\\r]+")).map { it.trim() }.filter { it.isNotEmpty() }
                    tokens.forEach { rows.add(listOf(it)) }
                }
            }
        }
        return rows
    }

    private fun buildRecipients(rows: List<List<String>>): ParseResult {
        if (rows.isEmpty()) return ParseResult(emptyList(), emptyList(), 0)

        val colCount = rows.maxOf { it.size }
        var phoneCol = 0
        var bestScore = -1.0
        for (c in 0 until colCount) {
            val score = phoneColumnScore(rows, c)
            if (score > bestScore) {
                bestScore = score
                phoneCol = c
            }
        }

        var nameCol: Int? = null
        var bestNameScore = 0.35 // kiwango cha chini ili kukubali kama safu ya jina
        for (c in 0 until colCount) {
            if (c == phoneCol) continue
            val score = nameColumnScore(rows, c)
            if (score > bestNameScore) {
                bestNameScore = score
                nameCol = c
            }
        }

        val valid = ArrayList<Recipient>()
        val flagged = ArrayList<String>()
        val seen = HashSet<String>()
        var totalRead = 0

        // Kama mstari wa kwanza unaonekana kama header (jina za column, si namba), tunauruka
        val startIndex = if (looksLikeHeaderRow(rows.first(), phoneCol)) 1 else 0

        for (i in startIndex until rows.size) {
            val row = rows[i]
            val rawPhone = row.getOrNull(phoneCol)?.trim().orEmpty()
            if (rawPhone.isEmpty()) continue
            totalRead++

            val res = PhoneNormalizer.normalize(rawPhone)
            if (res.ok) {
                if (seen.add(res.value)) {
                    val name = nameCol?.let { row.getOrNull(it)?.trim() }?.takeIf { !it.isNullOrEmpty() }
                    valid.add(Recipient(name, res.value))
                }
            } else {
                flagged.add(res.reason)
            }
        }

        return ParseResult(valid, flagged, totalRead)
    }

    private fun looksLikeHeaderRow(row: List<String>, phoneCol: Int): Boolean {
        val cell = row.getOrNull(phoneCol) ?: return false
        return !PhoneNormalizer.normalize(cell).ok && cell.any { it.isLetter() }
    }

    private fun phoneColumnScore(rows: List<List<String>>, col: Int): Double {
        var hits = 0
        var total = 0
        for (row in rows.take(30)) {
            val v = row.getOrNull(col)?.trim().orEmpty()
            if (v.isEmpty()) continue
            total++
            if (Regex("[0-9]{7,}").containsMatchIn(v.replace(Regex("[\\s\\-()+]"), ""))) hits++
        }
        return if (total == 0) 0.0 else hits.toDouble() / total
    }

    private fun nameColumnScore(rows: List<List<String>>, col: Int): Double {
        var hits = 0
        var total = 0
        for (row in rows.take(30)) {
            val v = row.getOrNull(col)?.trim().orEmpty()
            if (v.isEmpty()) continue
            total++
            val letters = v.count { it.isLetter() }
            if (letters >= v.length / 2 && letters >= 2) hits++
        }
        return if (total == 0) 0.0 else hits.toDouble() / total
    }
}
