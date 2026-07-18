package com.inonvation.lightlife.data

import android.content.Context

data class QuickLink(val name: String = "", val url: String = "", val packageName: String = "")

class QuickLinkStore(context: Context) {
    private val prefs = context.getSharedPreferences("quick_links", Context.MODE_PRIVATE)
    private val count = 9

    fun isEnabled(): Boolean = prefs.getBoolean("enabled", true)
    fun setEnabled(v: Boolean) { prefs.edit().putBoolean("enabled", v).apply() }

    fun getLinks(): List<QuickLink> {
        return (0 until count).map { i ->
            QuickLink(
                name = prefs.getString("name_$i", "") ?: "",
                url = prefs.getString("url_$i", "") ?: "",
                packageName = prefs.getString("pkg_$i", "") ?: "",
            )
        }
    }

    fun updateLink(index: Int, name: String, url: String, packageName: String) {
        prefs.edit()
            .putString("name_$index", name)
            .putString("url_$index", url)
            .putString("pkg_$index", packageName)
            .apply()
    }
}
