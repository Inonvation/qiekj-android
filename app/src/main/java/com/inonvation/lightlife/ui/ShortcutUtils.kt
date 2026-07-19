package com.inonvation.lightlife.ui

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.drawable.Icon
import android.os.Build
import android.widget.Toast
import com.inonvation.lightlife.MainActivity
import com.inonvation.lightlife.R
import com.inonvation.lightlife.data.DeviceItem
import com.inonvation.lightlife.data.QuickLink
import com.inonvation.lightlife.data.QuickLinkStore
import java.io.File

const val ACTION_OPEN_DEVICE_SHORTCUT = "com.inonvation.lightlife.OPEN_DEVICE_SHORTCUT"
const val EXTRA_GOODS_ID = "goods_id"
const val EXTRA_DEVICE_ID = "device_id"
const val EXTRA_GOODS_NAME = "goods_name"
const val PROJECT_URL = "https://github.com/Inonvation/light-life"

fun shortcutRequestFromIntent(intent: Intent?): DeviceShortcutRequest? {
    if (intent?.action != ACTION_OPEN_DEVICE_SHORTCUT) return null
    return DeviceShortcutRequest(
        goodsId = intent.getStringExtra(EXTRA_GOODS_ID),
        id = intent.getStringExtra(EXTRA_DEVICE_ID),
        goodsName = intent.getStringExtra(EXTRA_GOODS_NAME),
    )
}

fun pinDeviceShortcut(context: Context, device: DeviceItem) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Toast.makeText(context, "当前系统不支持添加桌面快捷方式", Toast.LENGTH_LONG).show()
        return
    }
    val shortcutManager = context.getSystemService(ShortcutManager::class.java)
    if (shortcutManager?.isRequestPinShortcutSupported != true) {
        Toast.makeText(context, "当前桌面不支持添加快捷方式", Toast.LENGTH_LONG).show()
        return
    }
    val label = device.goodsName.ifBlank { "历史设备" }
    val shortcutIntent = Intent(context, MainActivity::class.java).apply {
        action = ACTION_OPEN_DEVICE_SHORTCUT
        putExtra(EXTRA_GOODS_ID, device.goodsId)
        putExtra(EXTRA_DEVICE_ID, device.id)
        putExtra(EXTRA_GOODS_NAME, device.goodsName)
    }
    val shortcut = ShortcutInfo.Builder(context, "device-${device.goodsId ?: device.id ?: device.goodsName.hashCode()}")
        .setShortLabel(label.take(10))
        .setLongLabel(label)
        .setIcon(Icon.createWithResource(context, R.mipmap.ic_launcher))
        .setIntent(shortcutIntent)
        .build()
    shortcutManager.requestPinShortcut(shortcut, null)
    Toast.makeText(context, "已尝试添加桌面快捷方式，若失败请检查是否已授权软件添加快捷方式的权限", Toast.LENGTH_LONG).show()
}

fun pinQuickLinkShortcut(context: Context, link: QuickLink, index: Int, store: QuickLinkStore? = null) {
    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        Toast.makeText(context, "当前系统不支持添加桌面快捷方式", Toast.LENGTH_LONG).show()
        return
    }
    val shortcutManager = context.getSystemService(ShortcutManager::class.java)
    if (shortcutManager?.isRequestPinShortcutSupported != true) {
        Toast.makeText(context, "当前桌面不支持添加快捷方式", Toast.LENGTH_LONG).show()
        return
    }
    val label = link.name.ifBlank { "快捷方式" }.take(10)
    val shortcutIntent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(link.url)).apply {
        if (link.packageName.isNotBlank()) {
            `package` = link.packageName
        }
        flags = Intent.FLAG_ACTIVITY_NEW_TASK
    }

    // 获取图标：优先使用自定义图标，否则生成首字图标
    val icon = getQuickLinkIcon(context, link, index, store)

    val shortcut = ShortcutInfo.Builder(context, "quicklink-$index-${link.url.hashCode()}")
        .setShortLabel(label)
        .setLongLabel(link.name.ifBlank { label })
        .setIcon(icon)
        .setIntent(shortcutIntent)
        .build()
    shortcutManager.requestPinShortcut(shortcut, null)
    Toast.makeText(context, "已尝试添加「${link.name.ifBlank { "快捷方式" }}」到桌面", Toast.LENGTH_SHORT).show()
}

/** 获取快捷方式图标：优先使用自定义图标，否则生成首字图标 */
private fun getQuickLinkIcon(context: Context, link: QuickLink, index: Int, store: QuickLinkStore?): Icon {
    // 尝试加载自定义图标
    val iconFile = store?.getIconFile(index)
    if (iconFile != null && iconFile.exists()) {
        try {
            val bitmap = BitmapFactory.decodeFile(iconFile.absolutePath)
            if (bitmap != null) {
                return Icon.createWithBitmap(bitmap)
            }
        } catch (_: Exception) {}
    }

    // 生成首字图标
    val firstChar = link.name.firstOrNull()?.toString() ?: "?"
    val size = 128
    val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
    val canvas = Canvas(bitmap)

    // 背景色：根据名称生成不同颜色
    val hue = (link.name.hashCode() % 360).toFloat().let { if (it < 0) it + 360 else it }
    val bgColor = Color.HSVToColor(255, floatArrayOf(hue, 0.6f, 0.85f))

    // 绘制圆形背景
    val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = bgColor
        style = Paint.Style.FILL
    }
    canvas.drawCircle(size / 2f, size / 2f, size / 2f, bgPaint)

    // 绘制文字
    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = size * 0.55f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }
    val textY = size / 2f - (textPaint.descent() + textPaint.ascent()) / 2f
    canvas.drawText(firstChar, size / 2f, textY, textPaint)

    return Icon.createWithBitmap(bitmap)
}
