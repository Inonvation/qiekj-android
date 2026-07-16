package com.inonvation.lightlife.ui.screen

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.inonvation.lightlife.ui.DeviceTab
import com.inonvation.lightlife.ui.theme.CardShapes
import com.inonvation.lightlife.ui.theme.Spacings

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

@Composable
fun StatCard(
    icon: ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = CardShapes.smallCardCorner,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(horizontal = Spacings.md, vertical = 14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, modifier = Modifier.size(16.dp), tint = accentColor)
            }
            Spacer(Modifier.height(Spacings.sm))
            Text(text = value, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
            Spacer(Modifier.height(2.dp))
            Text(text = label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
