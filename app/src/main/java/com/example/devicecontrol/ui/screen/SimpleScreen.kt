package com.example.devicecontrol.ui.screen

import android.content.Context
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.devicecontrol.ui.AppUiState
import com.example.devicecontrol.ui.AppViewModel
import com.example.devicecontrol.ui.UnlockFlowState
import com.example.devicecontrol.ui.theme.CardShapes
import com.example.devicecontrol.ui.theme.Spacings

@Composable
fun SimpleScreen(state: AppUiState, vm: AppViewModel) {
    val ctx = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(WindowInsets.statusBars.asPaddingValues())
    ) {
        // 顶栏：标题 + 设置入口
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("LightLife", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.SemiBold)
            IconButton(onClick = { vm.showSettings() }) {
                Icon(Icons.Outlined.Settings, contentDescription = "设置")
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
        ) {
            // 未登录提示
            if (!state.hasToken) {
                Spacer(Modifier.height(Spacings.xxl))
                Text(
                    "请先切换到普通模式登录后再使用简洁版",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(Spacings.sm))
                Text(
                    "在设置中关闭简洁版即可回到普通模式",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                return@Column
            }

            // 余额卡片
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = CardShapes.cardCorner,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("积分余额", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        IconButton(onClick = { vm.refreshBalance() }) {
                            Icon(Icons.Outlined.Refresh, contentDescription = "刷新余额", modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceEvenly) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("当前积分", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${state.balance?.pointsText ?: "-"}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("可抵扣金额", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Text(
                                "${state.balance?.integralAmount?.let { "¥$it" } ?: "-"}",
                                style = MaterialTheme.typography.headlineMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(Spacings.xxl))

            // 开水按钮
            Button(
                onClick = {
                    val devices = state.devices
                    if (devices.isEmpty()) {
                        vm.refreshDevices()
                        android.widget.Toast.makeText(ctx, "设备列表为空，正在刷新", android.widget.Toast.LENGTH_SHORT).show()
                    } else if (devices.size == 1) {
                        vm.unlock(devices.first())
                    } else {
                        android.widget.Toast.makeText(ctx, "请在普通模式下选择设备开水", android.widget.Toast.LENGTH_SHORT).show()
                        vm.showSettings()
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = state.hasToken && !state.unlocking
            ) {
                Text(
                    if (state.unlocking) "正在开水…" else "开水",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(Spacings.md))

            // 积分任务按钮
            Button(
                onClick = {
                    if (state.runningPointsTask) {
                        android.widget.Toast.makeText(ctx, "任务正在运行，请在普通模式下查看进度", android.widget.Toast.LENGTH_SHORT).show()
                    } else {
                        val ua = android.webkit.WebSettings.getDefaultUserAgent(ctx)
                        vm.startPointsTask(ua)
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = state.hasToken && !state.runningPointsTask,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                )
            ) {
                Text(
                    if (state.runningPointsTask) "正在执行积分任务…" else "刷积分",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // 运行中状态提示
            if (state.runningPointsTask) {
                Spacer(Modifier.height(Spacings.sm))
                Text(
                    "任务正在后台执行，切换到普通模式可查看详细进度",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(Modifier.height(Spacings.xxl))
        }
    }

    // 开水流状态提示
    if (state.unlockFlowState !is UnlockFlowState.Idle) {
        Spacer(Modifier.height(Spacings.sm))
        val statusText = when (val flow = state.unlockFlowState) {
            is UnlockFlowState.PreChecking -> "正在检查…"
            is UnlockFlowState.Working -> flow.step
            is UnlockFlowState.Success -> "开水成功！"
            is UnlockFlowState.Failed -> "开水失败：${flow.message}"
            else -> ""
        }
        Text(
            statusText,
            style = MaterialTheme.typography.bodyMedium,
            color = if (state.unlockFlowState is UnlockFlowState.Success)
                MaterialTheme.colorScheme.primary
            else if (state.unlockFlowState is UnlockFlowState.Failed)
                MaterialTheme.colorScheme.error
            else MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )
        if (state.unlockFlowState !is UnlockFlowState.Idle
            && state.unlockFlowState !is UnlockFlowState.PreChecking
            && state.unlockFlowState !is UnlockFlowState.Working) {
            Spacer(Modifier.height(Spacings.sm))
            Button(
                onClick = { vm.dismissUnlockFlow() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            ) { Text("关闭") }
        }
    }
}
