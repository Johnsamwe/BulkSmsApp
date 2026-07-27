package com.jsdev.bulksms

import android.content.Context
import android.net.Uri
import android.util.Xml
import org.xmlpull.v1.XmlPullParser
import java.util.zip.ZipInputStream

/**
 * Inasoma faili la .xlsx moja kwa moja (bila Apache POI, ambayo mara nyingi
 * husababisha migogoro ya build kwenye Android/Gradle). .xlsx ni ZIP yenye
 * XML ndani, hivyo tunatumia ZipInputStream + XmlPullParser vilivyomo kwenye
 * Android SDK yenyewe — hakuna dependency ya nje inayohitajika.
 */
object XlsxReader {

    /** Inarudisha mistari (rows) ya sheet ya kwanza, kila mstari ukiwa orodha ya maandishi ya kila kiini (cell). */
    fun readFirstSheetRows(context: Context, uri: Uri): List<List<String>> {
        var sharedStrings: List<String> = emptyList()
        val sheetBytes = HashMap<String, ByteArray>()

        context.contentResolver.openInputStream(uri)?.use { input ->
            ZipInputStream(input).use { zip ->
                var entry = zip.nextEntry
                while (entry != null) {
                    val name = entry.name
                    when {
                        name == "xl/sharedStrings.xml" -> {
                            sharedStrings = parseSharedStrings(zip.readBytes())
                        }
                        name.startsWith("xl/worksheets/sheet") && name.endsWith(".xml") -> {
                            sheetBytes[name] = zip.readBytes()
                        }
                    }
                    zip.closeEntry()
                    entry = zip.nextEntry
                }
            }
        }

        val sheetKey = sheetBytes.keys.sortedBy { it }
            .firstOrNull { it.contains("sheet1") } ?: sheetBytes.keys.sortedBy { it }.firstOrNull()

        return sheetKey?.let { parseSheet(sheetBytes[it]!!, sharedStrings) } ?: emptyList()
    }

    private fun parseSharedStrings(bytes: ByteArray): List<String> {
        val result = ArrayList<String>()
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), "UTF-8")

        var event = parser.eventType
        var insideSi = false
        val current = StringBuilder()

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    if (parser.name == "si") {
                        insideSi = true
                        current.setLength(0)
                    }
                }
                XmlPullParser.TEXT -> {
                    if (insideSi) current.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    if (parser.name == "si") {
                        insideSi = false
                        result.add(current.toString())
                    }
                }
            }
            event = parser.next()
        }
        return result
    }

    private fun parseSheet(bytes: ByteArray, sharedStrings: List<String>): List<List<String>> {
        val rows = ArrayList<List<String>>()
        val parser: XmlPullParser = Xml.newPullParser()
        parser.setInput(bytes.inputStream(), "UTF-8")

        var event = parser.eventType
        var currentRow: MutableMap<Int, String>? = null
        var maxCol = -1
        var cellType: String? = null
        var cellCol = -1
        var readingValue = false
        val valueBuilder = StringBuilder()

        while (event != XmlPullParser.END_DOCUMENT) {
            when (event) {
                XmlPullParser.START_TAG -> {
                    when (parser.name) {
                        "row" -> {
                            currentRow = LinkedHashMap()
                            maxCol = -1
                        }
                        "c" -> {
                            cellType = parser.getAttributeValue(null, "t")
                            val ref = parser.getAttributeValue(null, "r") // e.g. "B3"
                            cellCol = columnIndexFromRef(ref)
                            if (cellCol > maxCol) maxCol = cellCol
                        }
                        "v", "t" -> {
                            readingValue = true
                            valueBuilder.setLength(0)
                        }
                    }
                }
                XmlPullParser.TEXT -> {
                    if (readingValue) valueBuilder.append(parser.text)
                }
                XmlPullParser.END_TAG -> {
                    when (parser.name) {
                        "v", "t" -> {
                            readingValue = false
                            val raw = valueBuilder.toString()
                            val resolved = if (cellType == "s") {
                                raw.toIntOrNull()?.let { sharedStrings.getOrNull(it) } ?: ""
                            } else raw
                            if (cellCol >= 0) currentRow?.put(cellCol, resolved)
                        }
                        "row" -> {
                            val row = currentRow
                            if (row != null) {
                                val list = ArrayList<String>()
                                for (i in 0..maxCol) list.add(row[i] ?: "")
                                rows.add(list)
                            }
                            currentRow = null
                        }
                    }
                }
            }
            event = parser.next()
        }
        return rows
    }

    /** "B3" -> column index 1 (0-based). Inashughulikia herufi nyingi (mfano "AA1"). */
    private fun columnIndexFromRef(ref: String?): Int {
        if (ref.isNullOrEmpty()) return 0
        var col = 0
        for (ch in ref) {
            if (ch.isLetter()) {
                col = col * 26 + (ch.uppercaseChar() - 'A' + 1)
            } else break
        }
        return if (col > 0) col - 1 else 0
    }
}
