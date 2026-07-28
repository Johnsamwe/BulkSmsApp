package com.jsdev.bulksms

object PhoneNormalizer {

    data class Result(val ok: Boolean, val value: String, val reason: String = "")

    /**
     * Inasafisha namba moja ya TZ:
     * - Inaondoa nafasi, dash, mabano, "+"
     * - 255XXXXXXXXX (tarakimu 12) -> 0XXXXXXXXX
     * - XXXXXXXXX (tarakimu 9, inayoanza na 6/7) -> 0XXXXXXXXX
     * - 0XXXXXXXXX (tarakimu 10, tayari sahihi) -> inabaki
     * - Nyingine yoyote -> imekataliwa (ok=false) ili isitumwe kimakosa
     */
    fun normalize(raw: String): Result {
        var s = raw.trim().replace(Regex("[\\s\\-()]"), "")
        if (s.startsWith("+")) s = s.substring(1)
        s = s.replace(Regex("\\.0+$"), "")

        if (s.isEmpty()) return Result(false, raw, "tupu")
        if (!s.all { it.isDigit() }) return Result(false, raw, "ina herufi zisizo namba")

        if (s.startsWith("255") && s.length == 12) {
            s = "0" + s.substring(3)
        } else if (s.length == 9 && (s[0] == '6' || s[0] == '7')) {
            s = "0$s"
        }

        if (s.length != 10 || s[0] != '0' || !(s[1] == '6' || s[1] == '7')) {
            return Result(false, raw, "haielekiki baada ya kusafisha: $s")
        }
        return Result(true, s)
    }
}
