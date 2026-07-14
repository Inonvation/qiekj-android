package com.example.devicecontrol.data

import android.content.Context

class PointsStatsStore(context: Context) {
    private val prefs = context.getSharedPreferences("points_stats", Context.MODE_PRIVATE)

    fun getTotalEarned(): Int = prefs.getInt(KEY_EARNED, 0)

    fun getTotalDeductedAmount(): String = prefs.getString(KEY_DEDUCTED, "0.00") ?: "0.00"

    fun addEarned(pointsEarned: Int) {
        prefs.edit()
            .putInt(KEY_EARNED, getTotalEarned() + pointsEarned)
            .apply()
    }

    fun addDeducted(amount: String) {
        val current = getTotalDeductedAmount()
        val newTotal = (current.toDoubleOrNull() ?: 0.0) + (amount.toDoubleOrNull() ?: 0.0)
        prefs.edit()
            .putString(KEY_DEDUCTED, String.format("%.2f", newTotal))
            .apply()
    }

    fun clearAll() {
        prefs.edit().clear().apply()
    }

    private companion object {
        private const val KEY_EARNED = "total_earned"
        private const val KEY_DEDUCTED = "total_deducted"
    }
}
