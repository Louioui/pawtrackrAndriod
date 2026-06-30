package com.pawtrackr.app.core.services

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import com.pawtrackr.app.core.runtime.PawtrackrRuntimeService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.io.Closeable
import java.util.concurrent.Executor
import javax.inject.Inject

class DeviceWorkloadPolicyManager @Inject constructor(
    private val context: Context
) : Closeable, PawtrackrRuntimeService {
    private val appContext = context.applicationContext
    private val powerManager: PowerManager? = appContext.getSystemService(PowerManager::class.java)
    private val mainThreadExecutor = Executor { command ->
        Handler(Looper.getMainLooper()).post(command)
    }

    private val _workloadState = MutableStateFlow(DeviceWorkloadState())
    val workloadState: StateFlow<DeviceWorkloadState> = _workloadState.asStateFlow()

    private val _operatingPolicy = MutableStateFlow(DeviceOperatingPolicy.PERFORMANCE)
    val operatingPolicy: StateFlow<DeviceOperatingPolicy> = _operatingPolicy.asStateFlow()

    private var started = false
    private var thermalListener: PowerManager.OnThermalStatusChangedListener? = null

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            if (intent?.action == Intent.ACTION_BATTERY_CHANGED) {
                applyBatteryIntent(intent)
            }
        }
    }

    override fun start() {
        if (started) return
        started = true

        registerBatteryReceiver()
        registerThermalListener()
    }

    override fun close() {
        stop()
    }

    override fun stop() {
        if (!started) return
        started = false

        runCatching { appContext.unregisterReceiver(batteryReceiver) }
        unregisterThermalListener()
    }

    private fun registerBatteryReceiver() {
        val filter = IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        val stickyIntent = appContext.registerReceiver(batteryReceiver, filter)
        stickyIntent?.let(::applyBatteryIntent)
    }

    private fun registerThermalListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val manager = powerManager ?: return

        val listener = PowerManager.OnThermalStatusChangedListener { status ->
            updateThermalLevel(mapThermalStatus(status))
        }
        thermalListener = listener
        manager.addThermalStatusListener(mainThreadExecutor, listener)
        updateThermalLevel(mapThermalStatus(manager.currentThermalStatus))
    }

    private fun unregisterThermalListener() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return
        val listener = thermalListener ?: return
        powerManager?.removeThermalStatusListener(listener)
        thermalListener = null
    }

    private fun applyBatteryIntent(intent: Intent) {
        val level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1)
        val scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1)
        val status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, BatteryManager.BATTERY_STATUS_UNKNOWN)
        val percent = if (level >= 0 && scale > 0) {
            ((level.toFloat() / scale.toFloat()) * 100f).toInt().coerceIn(0, 100)
        } else {
            null
        }
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL

        updateState { current ->
            current.copy(
                batteryPercent = percent,
                isCharging = isCharging
            )
        }
    }

    private fun updateThermalLevel(thermalLevel: ThermalLevel) {
        updateState { current -> current.copy(thermalLevel = thermalLevel) }
    }

    private fun updateState(transform: (DeviceWorkloadState) -> DeviceWorkloadState) {
        _workloadState.update { current ->
            val next = transform(current)
            val policy = DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = next.thermalLevel,
                batteryPercent = next.batteryPercent,
                isCharging = next.isCharging
            )
            _operatingPolicy.value = policy
            next.copy(policy = policy)
        }
    }

    private fun mapThermalStatus(status: Int): ThermalLevel {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) return ThermalLevel.UNKNOWN

        return when (status) {
            PowerManager.THERMAL_STATUS_NONE -> ThermalLevel.NONE
            PowerManager.THERMAL_STATUS_LIGHT -> ThermalLevel.LIGHT
            PowerManager.THERMAL_STATUS_MODERATE -> ThermalLevel.MODERATE
            PowerManager.THERMAL_STATUS_SEVERE -> ThermalLevel.SEVERE
            PowerManager.THERMAL_STATUS_CRITICAL -> ThermalLevel.CRITICAL
            PowerManager.THERMAL_STATUS_EMERGENCY -> ThermalLevel.EMERGENCY
            PowerManager.THERMAL_STATUS_SHUTDOWN -> ThermalLevel.SHUTDOWN
            else -> ThermalLevel.UNKNOWN
        }
    }
}
