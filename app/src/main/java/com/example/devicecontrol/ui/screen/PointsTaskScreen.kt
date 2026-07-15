package com.example.devicecontrol.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.devicecontrol.ui.AppUiState
import com.example.devicecontrol.ui.AppViewModel
import com.example.devicecontrol.ui.LogEntry
import com.example.devicecontrol.ui.LogLevel
import com.example.devicecontrol.ui.theme.AppColors
import com.example.devicecontrol.ui.theme.LogColors
import com.example.devicecontrol.ui.theme.TimelineColors

private data class TaskPhase(val key: String, val label: String)

private val PHASES = listOf(
    TaskPhase("signin", "签到"),
    TaskPhase("tasklist", "任务列表"),
    TaskPhase("app_video", "APP 广告"),
    TaskPhase("ali_video", "支付宝广告"),
)

@Composable
fun PointsTaskScreen(state: AppUiState, vm: AppViewModel) {
    val ctx = LocalContext.current
    val listState = rememberLazyListState()
    val haptic = LocalHapticFeedback.current
    var dialogSuppressChecked by remember { mutableStateOf(false) }
    val prefs = remember { ctx.getSharedPreferences("points_task_state", android.content.Context.MODE_PRIVATE) }
    var suppressWarning by remember { mutableStateOf(prefs.getBoolean("suppress_warning", false)) }
    var contentVisible by remember { mutableStateOf(false) }
    val phaseResults = state.pointsPhaseResults
    val isFinished = !state.runningPointsTask && phaseResults.isNotEmpty()

    LaunchedEffect(state.pointsLogs.size) {
        if (state.pointsLogs.isNotEmpty()) {
            listState.animateScrollToItem(state.pointsLogs.lastIndex)
        }
    }
    LaunchedEffect(Unit) { contentVisible = true }

    Column(modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp, vertical = 14.dp)) {
        // ═══ 状态卡片 ═══
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(400)) + slideInVertically(tween(400), initialOffsetY = { -it / 4 })
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = CardDefaults.cardColors(
                    containerColor = when {
                        state.runningPointsTask -> Color(0xFF1A2A1A)
                        isFinished -> Color(0xFF1A2A1A)
                        else -> MaterialTheme.colorScheme.surface
                    }
                ),
                elevation = CardDefaults.cardElevation(defaultElevation = if (state.runningPointsTask || isFinished) 0.dp else 1.dp)
            ) {
                if (state.runningPointsTask) {
                    Box(Modifier.fillMaxWidth().height(3.dp).background(Brush.horizontalGradient(listOf(Color(0xFF4CAF50), Color(0xFF81C784)))))
                }
                if (isFinished) {
                    // 完成摘要
                    val failed = phaseResults.filter { it.value == "fail" }
                    val doneColor = if (failed.isEmpty()) Color(0xFF4CAF50) else Color(0xFFE6A817)
                    Box(Modifier.fillMaxWidth().height(3.dp).background(doneColor))
                }
                Column(modifier = Modifier.padding(14.dp)) {
                    if (isFinished) {
                        val failed = phaseResults.filter { it.value == "fail" }
                        val summary = if (failed.isEmpty()) "今日任务全部完成" else "任务结束（${failed.size} 项失败）"
                        val ptsInfo = state.pointsLogs
                            .findLast { it.message.startsWith("本次获得") || it.message.startsWith("积分未变化") }
                            ?.message ?: ""
                        Text(summary, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold,
                            color = if (failed.isEmpty()) Color(0xFF4CAF50) else Color(0xFFE6A817))
                        if (ptsInfo.isNotEmpty()) {
                            Spacer(Modifier.height(4.dp))
                            Text(ptsInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    } else if (state.runningPointsTask && state.pointsProgress != null) {
                        val pg = state.pointsProgress!!
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Column {
                                Text(pg.phase, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold, color = Color(0xFF4CAF50))
                                Text("${pg.step} / ${pg.total}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                            val pa by rememberInfiniteTransition(label = "pulse").animateFloat(0.3f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "pa")
                            Box(Modifier.size(10.dp).alpha(pa).background(Color(0xFF4CAF50), CircleShape))
                        }
                        Spacer(Modifier.height(10.dp))
                        LinearProgressIndicator(
                            progress = { pg.step.toFloat() / pg.total.toFloat() },
                            modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(3.dp)),
                            color = Color(0xFF4CAF50), trackColor = Color(0xFF4CAF50).copy(alpha = 0.15f), strokeCap = StrokeCap.Round,
                        )
                    } else if (state.runningPointsTask) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            val pa by rememberInfiniteTransition(label = "ps").animateFloat(0.3f, 1f, infiniteRepeatable(tween(900), RepeatMode.Reverse), label = "ps")
                            Box(Modifier.size(8.dp).alpha(pa).background(Color(0xFF4CAF50), CircleShape))
                            Spacer(Modifier.width(8.dp))
                            Text("正在准备...", style = MaterialTheme.typography.bodyMedium, color = Color(0xFF4CAF50))
                        }
                    } else {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                            Text("积分任务", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                            val ptsInfo = state.pointsLogs.findLast { it.message.startsWith("初始积分：") || it.message.startsWith("最终积分：") }?.message
                            if (ptsInfo != null) Text(ptsInfo, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ═══ 阶段时间线 ═══
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(450, delayMillis = 100)) + slideInVertically(tween(450, delayMillis = 100), initialOffsetY = { it / 5 })
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.surface, RoundedCornerShape(10.dp)).padding(horizontal = 12.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                PHASES.forEachIndexed { index, phase ->
                    val result = phaseResults[phase.key]
                    val status = when {
                        state.runningPointsTask && result == null -> "pending"
                        result == "done" -> "done"
                        result == "fail" -> "fail"
                        state.runningPointsTask && phase.key == currentActivePhase(state.pointsProgress) -> "active"
                        else -> "pending"
                    }
                    val color by animateColorAsState(
                        targetValue = when (status) {
                            "done" -> TimelineColors.completed
                            "fail" -> Color(0xFFE06C75)
                            "active" -> TimelineColors.active
                            else -> TimelineColors.pending
                        }, animationSpec = tween(300, easing = FastOutSlowInEasing), label = "pc$index"
                    )
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier.size(if (status == "active") 28.dp else 22.dp)
                                .then(if (status == "active") Modifier.background(TimelineColors.activeSurface, CircleShape) else Modifier),
                            contentAlignment = Alignment.Center
                        ) {
                            when (status) {
                                "done" -> Icon(Icons.Outlined.Check, null, tint = color, modifier = Modifier.size(15.dp))
                                "fail" -> Text("✗", color = color, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                                "active" -> {
                                    val pa by rememberInfiniteTransition(label = "pa$index").animateFloat(0.4f, 1f, infiniteRepeatable(tween(800), RepeatMode.Reverse), label = "pai")
                                    Box(Modifier.size(10.dp).alpha(pa).background(color, CircleShape))
                                }
                                else -> Box(Modifier.size(7.dp).background(color, CircleShape))
                            }
                        }
                        Spacer(Modifier.height(4.dp))
                        Text(phase.label, style = MaterialTheme.typography.labelSmall, fontSize = 9.sp,
                            fontWeight = if (status == "active") FontWeight.Bold else FontWeight.Normal,
                            color = color, maxLines = 1)
                        if (status == "active" && state.pointsProgress != null) {
                            Text("${state.pointsProgress!!.step}/${state.pointsProgress!!.total}",
                                style = MaterialTheme.typography.labelSmall, fontSize = 8.sp, color = color.copy(alpha = 0.7f))
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        // ═══ 日志面板 ═══
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(400, delayMillis = 200)) + slideInVertically(tween(400, delayMillis = 200), initialOffsetY = { it / 4 }),
            modifier = Modifier.weight(1f)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("执行日志", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Medium)
                    if (state.pointsLogs.isNotEmpty()) {
                        OutlinedButton(
                            onClick = { if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress); vm.clearPointsLogs() },
                            modifier = Modifier.height(32.dp)
                        ) { Text("清空", style = MaterialTheme.typography.labelSmall, fontSize = 11.sp) }
                    }
                }
                Spacer(Modifier.height(6.dp))
                Surface(
                    Modifier.fillMaxWidth().weight(1f),
                    color = LogColors.background, shape = RoundedCornerShape(10.dp)
                ) {
                    LazyColumn(state = listState, modifier = Modifier.fillMaxSize().padding(horizontal = 10.dp, vertical = 8.dp)) {
                        if (state.pointsLogs.isEmpty()) {
                            item {
                                Row(Modifier.fillMaxWidth().padding(vertical = 24.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Outlined.Info, null, tint = LogColors.info.copy(alpha = 0.4f), modifier = Modifier.size(14.dp))
                                    Spacer(Modifier.width(6.dp))
                                    Text("等待执行任务...", color = LogColors.info.copy(alpha = 0.4f), fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, fontSize = 12.sp)
                                }
                            }
                        } else {
                            items(state.pointsLogs, key = { "${it.timestamp}_${it.id}" }) { entry -> LogRow(entry) }
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))

        // ═══ 按钮 ═══
        AnimatedVisibility(
            visible = contentVisible,
            enter = fadeIn(tween(350, delayMillis = 300)) + slideInVertically(tween(350, delayMillis = 300), initialOffsetY = { it / 3 })
        ) {
            if (state.runningPointsTask) {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = {
                            if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            if (state.pointsTaskPaused) vm.resumePointsTask() else vm.pausePointsTask()
                        },
                        modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = if (state.pointsTaskPaused) AppColors.resume else AppColors.pause, contentColor = AppColors.white)
                    ) { Text(if (state.pointsTaskPaused) "▶ 继续" else "⏸ 暂停", fontWeight = FontWeight.Medium) }
                    OutlinedButton(
                        onClick = { if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress); vm.stopPointsTask() },
                        modifier = Modifier.weight(1f).height(48.dp), shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = AppColors.stop)
                    ) { Text("⏹ 结束", fontWeight = FontWeight.Medium) }
                }
            } else {
                Button(
                    onClick = {
                        if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        if (suppressWarning) { val ua = android.webkit.WebSettings.getDefaultUserAgent(ctx); vm.startPointsTask(ua) }
                        else vm.showPointsTaskWarning()
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp), enabled = !state.runningPointsTask,
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AppColors.start, contentColor = AppColors.white)
                ) { Text("开始执行自动化任务", fontWeight = FontWeight.Medium) }
            }
        }
    }

    // ═══ 警告弹窗 ═══
    if (state.showPointsTaskWarning) {
        var countdown by remember { mutableStateOf(5) }
        LaunchedEffect(state.showPointsTaskWarning) { countdown = 5; while (countdown > 0) { kotlinx.coroutines.delay(1000); countdown-- } }
        AlertDialog(
            onDismissRequest = { },
            title = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.Top) {
                    Column {
                        Text("提示", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(4.dp))
                        Text("必读！防止积分清零及封号风险", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.error)
                    }
                    IconButton(onClick = { vm.dismissPointsTaskWarning() }, modifier = Modifier.offset(x = 12.dp, y = (-12).dp)) { Icon(Icons.Default.Close, "关闭", modifier = Modifier.size(20.dp)) }
                }
            },
            text = {
                Column {
                    Text("执行自动化任务前请注意：", fontWeight = FontWeight.SemiBold); Spacer(Modifier.height(8.dp))
                    Text("• 任务持续失败时，重启 APP 再试。仍失败需在官方 APP 手动观看一条广告"); Spacer(Modifier.height(4.dp))
                    Text("• 尽量避免多账号在同一设备、同一网络 IP 下执行刷积分任务"); Spacer(Modifier.height(4.dp))
                    Text("• 即使在不同设备的不同账户下，也尽量避免同时执行刷积分任务"); Spacer(Modifier.height(4.dp))
                    Text("• 即使在同一固定设备下，也尽量避免每天在同一时间段执行刷积分任务"); Spacer(Modifier.height(4.dp))
                    Text("• 建议在 WiFi 稳定的环境下执行任务"); Spacer(Modifier.height(12.dp))
                    Text("免责声明", fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.error); Spacer(Modifier.height(4.dp))
                    Text("本项目为个人兴趣开发，仅供学习和测试使用。自动化积分功能模拟正常用户操作流程，可能违反相关平台服务条款。"); Spacer(Modifier.height(4.dp))
                    Text("• 请自行承担账号、设备、接口变更和平台规则风险")
                    Text("• 可能面临账户积分清零、永久无法使用积分甚至封号的风险")
                    Text("• 本人概不承担因此产生的任何责任")
                }
            },
            confirmButton = {
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(checked = dialogSuppressChecked, onCheckedChange = { dialogSuppressChecked = it }, colors = CheckboxDefaults.colors(checkedColor = AppColors.start))
                        Text("不再提示", style = MaterialTheme.typography.bodySmall)
                    }
                    TextButton(onClick = {
                        if (dialogSuppressChecked) { suppressWarning = true; prefs.edit().putBoolean("suppress_warning", true).apply(); android.widget.Toast.makeText(ctx, "后续不再弹出，可在设置中查看提示", android.widget.Toast.LENGTH_SHORT).show() }
                        val ua = android.webkit.WebSettings.getDefaultUserAgent(ctx); vm.startPointsTask(ua); vm.dismissPointsTaskWarning()
                    }, enabled = countdown == 0) { Text(if (countdown > 0) "${countdown}秒后可执行" else "开始执行") }
                }
            },
            shape = RoundedCornerShape(8.dp),
        )
    }
}

private fun currentActivePhase(progress: com.example.devicecontrol.ui.PointsProgress?): String? {
    if (progress == null) return null
    return when {
        progress.phase.startsWith("APP") -> "app_video"
        progress.phase.startsWith("支付宝") -> "ali_video"
        else -> null
    }
}

@Composable
private fun LogRow(entry: LogEntry) {
    val levelColor = when (entry.level) {
        LogLevel.SUCCESS -> LogColors.success
        LogLevel.ERROR -> LogColors.error
        LogLevel.WARN -> LogColors.warn
        LogLevel.INFO -> LogColors.info
    }
    val leftBorder = when {
        entry.message.contains("✗") || entry.level == LogLevel.ERROR -> levelColor
        entry.message.contains("✓") || entry.message.startsWith("  └") -> levelColor
        entry.message.startsWith("▶") -> levelColor
        entry.level == LogLevel.SUCCESS -> levelColor
        else -> Color.Transparent
    }
    Row(Modifier.fillMaxWidth().padding(vertical = 2.dp), verticalAlignment = Alignment.Top) {
        if (leftBorder != Color.Transparent) {
            Box(Modifier.width(2.dp).height(16.dp).background(leftBorder, RoundedCornerShape(1.dp)))
            Spacer(Modifier.width(6.dp))
        } else { Spacer(Modifier.width(8.dp)) }
        if (entry.timestamp.isNotEmpty()) {
            Text(entry.timestamp, color = LogColors.timestamp, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, modifier = Modifier.width(46.dp))
            Spacer(Modifier.width(4.dp))
        }
        Text(entry.message.trimStart(), color = levelColor, fontFamily = FontFamily.Monospace, style = MaterialTheme.typography.bodySmall, fontSize = 11.5.sp, lineHeight = 17.sp, modifier = Modifier.weight(1f))
    }
}
