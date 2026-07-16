package com.example.devicecontrol.ui.backup

import com.example.devicecontrol.data.AppRepository
import com.example.devicecontrol.data.BackupData
import com.example.devicecontrol.data.BackupManager
import com.example.devicecontrol.data.DebugLogStore
import com.example.devicecontrol.data.PointsStatsStore
import com.example.devicecontrol.data.PointsTaskStateStore
import com.example.devicecontrol.data.RestoreCounts
import com.example.devicecontrol.data.TaskLogStore
import com.example.devicecontrol.data.TokenExpiredException
import com.example.devicecontrol.ui.AppUiState
import com.example.devicecontrol.ui.theme.ThemeMode
import com.example.devicecontrol.ui.theme.ThemePreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BackupController(
    private val state: MutableStateFlow<AppUiState>,
    private val scope: CoroutineScope,
    private val repository: AppRepository,
    private val backupManager: BackupManager?,
    private val pointsStatsStore: PointsStatsStore?,
    private val taskStateStore: PointsTaskStateStore?,
    private val logStore: TaskLogStore?,
    private val themePreferences: ThemePreferences?,
    private val debugLogStore: DebugLogStore?,
    private val onRestoreFinished: () -> Unit,
    private val showToast: (String) -> Unit,
) {
    private var pendingBackup: BackupData? = null

    fun prepareBackupJson(): String {
        val s = state.value
        val backup = backupManager?.collect(
            appVersion = s.appVersion,
            token = repository.localToken(),
            orderHistory = s.orderHistory,
            pointsStats = pointsStatsStore,
            taskLogStore = logStore,
            themeMode = s.themeMode.name,
            hapticEnabled = s.hapticEnabled,
            logCompactEnabled = s.logCompactEnabled,
            autoCleanLogsEnabled = s.autoCleanLogsEnabled,
            simpleModeEnabled = s.simpleModeEnabled,
            userAgent = s.userAgent,
            taskStateStore = taskStateStore,
        ) ?: return ""
        return backupManager.toJson(backup)
    }

    fun restoreFromBackupJson(json: String) {
        debugLogStore?.d("VM", "restoreFromBackupJson: length=${json.length}")
        scope.launch {
            runCatching {
                var parseError: String? = null
                val backup = backupManager?.fromJson(json) { parseError = it }
                if (backup == null) {
                    showToast(parseError ?: "备份文件格式不匹配，请选择有效的 .lif 备份文件")
                    return@launch
                }
                val hasTokenNow = repository.localToken() != null
                val backupToken = backup.data.token
                val originalToken = repository.localToken()

                // 如果备份有 token，先校验是否有效
                if (!backupToken.isNullOrBlank()) {
                    repository.saveToken(backupToken)
                    val tokenExpired = try {
                        repository.validateToken(); false
                    } catch (e: TokenExpiredException) { true }
                    catch (e: Exception) {
                        if (originalToken != null) repository.saveToken(originalToken)
                        else repository.clearToken()
                        throw e
                    }

                    if (tokenExpired) {
                        // 恢复原来的 token
                        if (originalToken != null) repository.saveToken(originalToken)
                        else repository.clearToken()
                        if (hasTokenNow) {
                            pendingBackup = backup
                            state.update { it.copy(showBackupTokenExpiredDialog = true) }
                        } else {
                            state.update { it.copy(hasToken = false) }
                            showToast("备份文件登录凭证已过期，请使用验证码登录")
                        }
                        return@launch
                    }
                }

                // token 有效或备份没有 token，正常恢复
                val counts = doRestoreBackup(backup)
                showToast("已恢复 ${counts.orders} 条订单、${counts.logs} 条任务记录")
            }.onFailure { e ->
                showToast("导入失败：${e.message ?: "未知错误"}")
            }
        }
    }

    fun confirmBackupImportOrdersOnly() {
        debugLogStore?.d("VM", "confirmBackupImportOrdersOnly")
        val backup = pendingBackup ?: return
        pendingBackup = null
        state.update { it.copy(showBackupTokenExpiredDialog = false) }
        val originalToken = repository.localToken()
        doRestoreBackup(backup)
        // 恢复原来的 token（覆盖备份中的过期 token）
        if (originalToken != null) repository.saveToken(originalToken)
        state.update { s -> s.copy(hasToken = repository.localToken() != null) }
        onRestoreFinished()
        showToast("已导入订单和日志")
    }

    fun dismissBackupTokenExpiredDialog() {
        pendingBackup = null
        state.update { it.copy(showBackupTokenExpiredDialog = false) }
    }

    private fun doRestoreBackup(backup: BackupData): RestoreCounts {
        val counts = backupManager?.restore(backup) ?: RestoreCounts()
        state.update { it.copy(
            hasToken = repository.localToken() != null,
            orderHistory = repository.orderHistory(),
            totalPointsEarned = pointsStatsStore?.getTotalEarned() ?: 0,
            totalPointsDeducted = pointsStatsStore?.getTotalDeductedAmount() ?: "0.00",
        )}
        backup.data.themeMode?.let { modeName ->
            try {
                val mode = ThemeMode.valueOf(modeName)
                themePreferences?.setThemeMode(mode)
                state.update { it.copy(themeMode = mode) }
            } catch (_: IllegalArgumentException) {}
        }
        backup.data.hapticEnabled?.let { enabled ->
            taskStateStore?.setHapticEnabled(enabled)
            state.update { s -> s.copy(hapticEnabled = enabled) }
        }
        backup.data.logCompactEnabled?.let { compact ->
            taskStateStore?.setLogCompactEnabled(compact)
            state.update { s -> s.copy(logCompactEnabled = compact) }
        }
        backup.data.autoCleanLogsEnabled?.let { autoClean ->
            taskStateStore?.setAutoCleanLogsEnabled(autoClean)
            state.update { s -> s.copy(autoCleanLogsEnabled = autoClean) }
        }
        backup.data.userAgent?.let { ua ->
            if (ua.isNotBlank()) {
                taskStateStore?.setUserAgent(ua)
                state.update { s -> s.copy(userAgent = ua) }
            }
        }
        backup.data.simpleModeEnabled?.let { enabled ->
            taskStateStore?.setSimpleModeEnabled(enabled)
            state.update { s -> s.copy(simpleModeEnabled = enabled) }
        }
        return counts
    }
}
