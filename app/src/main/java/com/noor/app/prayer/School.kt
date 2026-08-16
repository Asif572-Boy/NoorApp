package com.noor.app.prayer

/** The three schools offered. Affects prayer-time calculation only. */
enum class School(val display: String, val subtitle: String) {
    HANAFI("Hanafi", "Later Asr (Karachi method)"),
    SHAFI("Shafi", "Standard Asr (Karachi method)"),
    JAFFRI("Jaffri", "Shia Ithna-Ashari timing");

    companion object {
        fun from(name: String?): School =
            entries.firstOrNull { it.name == name } ?: HANAFI
    }
}
