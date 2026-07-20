package com.inonvation.lightlife.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inonvation.lightlife.ui.AppUiState
import com.inonvation.lightlife.ui.AppViewModel
import com.inonvation.lightlife.ui.theme.CardShapes
import com.inonvation.lightlife.ui.theme.Spacings

@Composable
fun TaskSettingsScreen(state: AppUiState, vm: AppViewModel) {
    val ctx = LocalContext.current
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        // 顶栏
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress); vm.dismissTaskSettings() }) {
                        Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = "返回", tint = MaterialTheme.colorScheme.onSurface)
                    }
                    Text("任务设置", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
                }
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 18.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth(), shape = CardShapes.cardCorner, colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)) {
                Column(modifier = Modifier.padding(16.dp).animateContentSize(tween(300))) {
                    // 启动自动执行任务
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp).alpha(if (state.safeModeEnabled) 0.5f else 1f)) {
                            Text("启动自动执行任务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("打开 App 时自动检测并执行未完成的积分任务", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                        Switch(
                            checked = state.autoStartTaskEnabled && !state.safeModeEnabled,
                            onCheckedChange = {
                                if (state.safeModeEnabled) {
                                    android.widget.Toast.makeText(ctx, "请先关闭保险模式", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.toggleAutoStartTask()
                                }
                            },
                            modifier = Modifier.alpha(if (state.safeModeEnabled) 0.5f else 1f),
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 后台刷积分
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp).alpha(if (state.safeModeEnabled) 0.5f else 1f)) {
                            Text("后台刷积分", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("退出应用后任务仍在通知栏持续执行，需开启通知权限", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                        Switch(
                            checked = state.backgroundTaskEnabled && !state.safeModeEnabled,
                            onCheckedChange = {
                                if (state.safeModeEnabled) {
                                    android.widget.Toast.makeText(ctx, "请先关闭保险模式", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.toggleBackgroundTask()
                                }
                            },
                            modifier = Modifier.alpha(if (state.safeModeEnabled) 0.5f else 1f),
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 随机延迟
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp).alpha(if (state.safeModeEnabled) 0.5f else 1f)) {
                            Text("随机延迟", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Text("执行步骤间随机等待几秒，降低风控风险，会略微增加总耗时", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                        Switch(
                            checked = state.randomDelayEnabled && !state.safeModeEnabled,
                            onCheckedChange = {
                                if (state.safeModeEnabled) {
                                    android.widget.Toast.makeText(ctx, "请先关闭保险模式", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.toggleRandomDelay()
                                }
                            },
                            modifier = Modifier.alpha(if (state.safeModeEnabled) 0.5f else 1f),
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

                    // 定时任务
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                        Column(modifier = Modifier.weight(1f).padding(end = 8.dp).alpha(if (state.safeModeEnabled) 0.5f else 1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("定时任务", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                                IconButton(onClick = { vm.showScheduleInfoDialog() }, modifier = Modifier.size(24.dp)) {
                                    Icon(Icons.Outlined.Info, contentDescription = "定时任务说明", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                            Text("设定时间段，每天随机选择时间自动执行", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant, maxLines = 2)
                        }
                        Switch(
                            checked = state.scheduleEnabled && !state.safeModeEnabled,
                            onCheckedChange = {
                                if (state.safeModeEnabled) {
                                    android.widget.Toast.makeText(ctx, "请先关闭保险模式", android.widget.Toast.LENGTH_SHORT).show()
                                } else {
                                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.toggleScheduleEnabled()
                                }
                            },
                            modifier = Modifier.alpha(if (state.safeModeEnabled) 0.5f else 1f),
                            colors = SwitchDefaults.colors(checkedTrackColor = MaterialTheme.colorScheme.primary)
                        )
                    }

                    // 管理时间段入口
                    if (state.scheduleEnabled && !state.safeModeEnabled) {
                        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth().clickable {
                                if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                vm.showScheduleSettings()
                            },
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("管理时间段", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.primary)
                                Text(
                                    if (state.scheduleTimeSlots.isEmpty()) "点击添加执行时间段"
                                    else "已设置 ${state.scheduleTimeSlots.size} 个时间段",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp).rotate(180f), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }

                    // 电池优化入口
                    AnimatedVisibility(
                        visible = state.backgroundTaskEnabled && !state.safeModeEnabled,
                        enter = fadeIn() + slideInVertically { -it / 4 },
                        exit = fadeOut() + slideOutVertically { -it / 4 },
                    ) {
                        Column {
                            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth().clickable {
                                    if (state.hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    vm.openBatteryOptimizationSettings()
                                },
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("电池优化", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Medium)
                                    Text("建议改为无限制，避免被系统省电策略杀掉。执行期间可能耗电加快、轻微发烫，完成后杀掉进程即可", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                Icon(Icons.AutoMirrored.Outlined.ArrowBack, contentDescription = null, modifier = Modifier.size(18.dp).rotate(180f), tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacings.xxl))
        }
    }
}
