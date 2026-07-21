package com.inonvation.lightlife.data.water

import android.content.ContentUris
import android.content.ContentValues
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import java.util.TimeZone

/**
 * 日历提醒管理器。
 * 通过 CalendarContract 在系统日历创建重复事件实现喝水提醒。
 */
class CalendarReminderManager(private val context: Context) {
    
    companion object {
        private const val CALENDAR_ACCOUNT_NAME = "lightlife_water_reminder"
        private const val CALENDAR_DISPLAY_NAME = "喝水提醒"
        private const val EVENT_TITLE = "💧 该喝水啦"
        private const val EVENT_DURATION_MINUTES = 5
    }
    
    private val store = WaterReminderStore(context)
    
    /**
     * 检查是否有日历权限
     */
    fun hasCalendarPermission(): Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED &&
               ContextCompat.checkSelfPermission(context, android.Manifest.permission.WRITE_CALENDAR) == PackageManager.PERMISSION_GRANTED
    }
    
    // ── 定时提醒 ──
    
    /**
     * 启用单个时间点的提醒
     * @return 事件ID，失败返回null
     */
    fun enableTimeSlot(slot: ReminderTimeSlot): Long? {
        if (!hasCalendarPermission()) return null
        
        val calId = getOrCreateCalendarId() ?: return null
        
        // 创建单次重复事件
        val eventId = createDailyEvent(calId, slot) ?: return null
        
        // 保存映射
        store.addEventMapping(slot.id, eventId)
        
        return eventId
    }
    
    /**
     * 禁用单个时间点的提醒
     */
    fun disableTimeSlot(slotId: String) {
        if (!hasCalendarPermission()) return
        
        val mappings = store.getEventMappings()
        val eventId = mappings[slotId] ?: return
        
        deleteEvent(eventId)
        store.removeEventMapping(slotId)
    }
    
    /**
     * 更新单个时间点的提醒
     */
    fun updateTimeSlot(slot: ReminderTimeSlot) {
        // 先删除旧的
        disableTimeSlot(slot.id)
        
        // 如果启用，创建新的
        if (slot.enabled) {
            enableTimeSlot(slot)
        }
    }
    
    /**
     * 创建每天重复的日历事件
     */
    private fun createDailyEvent(calId: Long, slot: ReminderTimeSlot): Long? {
        val now = java.time.LocalDate.now()
        val startTime = java.time.LocalDateTime.of(now, java.time.LocalTime.of(slot.hour, slot.minute))
        var startMillis = startTime.atZone(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()
        
        // 如果时间已过，调整到明天
        if (startMillis <= System.currentTimeMillis()) {
            startMillis += 24 * 60 * 60 * 1000
        }
        
        val values = ContentValues().apply {
            put(CalendarContract.Events.CALENDAR_ID, calId)
            put(CalendarContract.Events.TITLE, EVENT_TITLE)
            put(CalendarContract.Events.DESCRIPTION, slot.label.ifEmpty { "保持水分，健康生活" })
            put(CalendarContract.Events.DTSTART, startMillis)
            put(CalendarContract.Events.DURATION, "PT${EVENT_DURATION_MINUTES}M")
            put(CalendarContract.Events.EVENT_TIMEZONE, TimeZone.getDefault().id)
            put(CalendarContract.Events.RRULE, "FREQ=DAILY;INTERVAL=1")
            put(CalendarContract.Events.HAS_ALARM, 1)
            put(CalendarContract.Events.AVAILABILITY, CalendarContract.Events.AVAILABILITY_FREE)
        }
        
        val uri = context.contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        val eventId = uri?.lastPathSegment?.toLongOrNull()
        
        if (eventId != null) {
            insertReminder(eventId)
        }
        
        return eventId
    }
    
    // ── 间隔提醒 ──
    
    /**
     * 创建间隔提醒
     * @param intervalMinutes 间隔（分钟）
     * @param quietStartHour 免打扰开始小时
     * @param quietEndHour 免打扰结束小时
     */
    fun createIntervalReminders(intervalMinutes: Int, quietStartHour: Int, quietEndHour: Int): List<Long> {
        if (!hasCalendarPermission()) return emptyList()
        
        val calId = getOrCreateCalendarId() ?: return emptyList()
        
        // 计算今天剩余的提醒时间点
        val reminderTimes = calculateReminderTimes(intervalMinutes, quietStartHour, quietEndHour)
        
        if (reminderTimes.isEmpty()) return emptyList()
        
        // 为每个时间点创建独立的日历事件
        val eventIds = mutableListOf<Long>()
        
        for (time in reminderTimes) {
            val eventId = createDailyEvent(calId, ReminderTimeSlot("", time.first, time.second, true, ""))
            if (eventId != null) {
                eventIds.add(eventId)
            }
        }
        
        // 保存事件ID列表
        store.setIntervalEventIds(eventIds)
        
        return eventIds
    }
    
    /**
     * 删除间隔提醒
     */
    fun deleteIntervalReminders() {
        if (!hasCalendarPermission()) return
        
        val eventIds = store.getIntervalEventIds()
        
        for (eventId in eventIds) {
            deleteEvent(eventId)
        }
        
        store.setIntervalEventIds(emptyList())
    }
    
    /**
     * 计算今天的提醒时间点（排除免打扰时段）
     */
    private fun calculateReminderTimes(intervalMinutes: Int, quietStartHour: Int, quietEndHour: Int): List<Pair<Int, Int>> {
        val times = mutableListOf<Pair<Int, Int>>()
        val now = java.time.LocalTime.now()
        
        var currentTime = now.plusMinutes(intervalMinutes.toLong())
        
        while (currentTime.hour < 24) {
            val hour = currentTime.hour
            val minute = currentTime.minute
            
            // 检查是否在免打扰时段
            if (!store.isQuietTime(hour, minute)) {
                times.add(Pair(hour, minute))
            }
            
            currentTime = currentTime.plusMinutes(intervalMinutes.toLong())
        }
        
        return times
    }
    
    // ── 通用方法 ──
    
    /**
     * 删除指定事件
     */
    private fun deleteEvent(eventId: Long) {
        val deleteUri = ContentUris.withAppendedId(CalendarContract.Events.CONTENT_URI, eventId)
        context.contentResolver.delete(deleteUri, null, null)
    }
    
    /**
     * 插入事件提醒
     */
    private fun insertReminder(eventId: Long) {
        val values = ContentValues().apply {
            put(CalendarContract.Reminders.EVENT_ID, eventId)
            put(CalendarContract.Reminders.MINUTES, 0)
            put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
        }
        
        context.contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, values)
    }
    
    /**
     * 获取或创建日历账户
     */
    private fun getOrCreateCalendarId(): Long? {
        val accounts = CalendarContract.Calendars.CONTENT_URI
        
        val projection = arrayOf(CalendarContract.Calendars._ID)
        val selection = "${CalendarContract.Calendars.ACCOUNT_NAME} = ?"
        val selectionArgs = arrayOf(CALENDAR_ACCOUNT_NAME)
        
        context.contentResolver.query(accounts, projection, selection, selectionArgs, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                return cursor.getLong(0)
            }
        }
        
        return createCalendarAccount()
    }
    
    /**
     * 创建日历账户
     */
    private fun createCalendarAccount(): Long? {
        val values = ContentValues().apply {
            put(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT_NAME)
            put(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            put(CalendarContract.Calendars.NAME, CALENDAR_DISPLAY_NAME)
            put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, CALENDAR_DISPLAY_NAME)
            put(CalendarContract.Calendars.CALENDAR_COLOR, 0xFF2196F3.toInt())
            put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_OWNER)
            put(CalendarContract.Calendars.OWNER_ACCOUNT, CALENDAR_ACCOUNT_NAME)
            put(CalendarContract.Calendars.VISIBLE, 1)
            put(CalendarContract.Calendars.SYNC_EVENTS, 1)
            put(CalendarContract.Calendars.CALENDAR_TIME_ZONE, TimeZone.getDefault().id)
        }
        
        val uri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()
        
        val result = context.contentResolver.insert(uri, values)
        return result?.lastPathSegment?.toLongOrNull()
    }
    
    /**
     * 删除所有喝水提醒日历事件和日历账户
     */
    fun deleteAllEventsAndCalendar(): Boolean {
        if (!hasCalendarPermission()) return false
        
        val calId = getOrCreateCalendarId() ?: return false
        
        // 删除该日历下的所有事件
        val eventsUri = CalendarContract.Events.CONTENT_URI
        val selection = "${CalendarContract.Events.CALENDAR_ID} = ?"
        val selectionArgs = arrayOf(calId.toString())
        context.contentResolver.delete(eventsUri, selection, selectionArgs)
        
        // 删除日历账户
        val calUri = CalendarContract.Calendars.CONTENT_URI.buildUpon()
            .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER, "true")
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, CALENDAR_ACCOUNT_NAME)
            .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, CalendarContract.ACCOUNT_TYPE_LOCAL)
            .build()
        context.contentResolver.delete(calUri, null, null)
        
        // 清除存储的映射
        store.clearEventMappings()
        store.setIntervalEventIds(emptyList())
        
        return true
    }
}
