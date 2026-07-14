package com.example.devicecontrol.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.devicecontrol.ui.DeviceTab
import com.example.devicecontrol.ui.theme.Spacings

@Composable
fun TopBar(
    currentTab: DeviceTab,
    hasToken: Boolean,
    hapticEnabled: Boolean,
    onSettingsClick: () -> Unit,
) {
    val haptic = LocalHapticFeedback.current
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
            AnimatedContent(
                targetState = currentTab,
                transitionSpec = {
                    (fadeIn(tween(200)) + slideInVertically(tween(200)) { -it / 4 }) togetherWith
                    (fadeOut(tween(150)) + slideOutVertically(tween(150)) { it / 4 })
                },
                label = "topBarTitle"
            ) { tab ->
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = when (tab) {
                            DeviceTab.Control -> "首页"
                            DeviceTab.Points -> "积分任务"
                            DeviceTab.Me -> "我的"
                        },
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(Modifier.width(Spacings.sm))
                    Text(
                        text = when (tab) {
                            DeviceTab.Control -> "历史设备"
                            DeviceTab.Points -> "自动化刷积分"
                            DeviceTab.Me -> if (hasToken) "已登录" else "未登录"
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                }
            }
            IconButton(onClick = {
                if (hapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onSettingsClick()
            }) {
                Icon(Icons.Outlined.Settings, contentDescription = "设置")
            }
        }
    }
}
