package com.pawtrackr.app.core.services

enum class DeviceOperatingPolicy {
    PERFORMANCE,
    BALANCED,
    THROTTLED,
    CRITICAL_SUSPEND
}

enum class ThermalLevel(val severity: Int) {
    UNKNOWN(severity = 0),
    NONE(severity = 0),
    LIGHT(severity = 1),
    MODERATE(severity = 2),
    SEVERE(severity = 3),
    CRITICAL(severity = 4),
    EMERGENCY(severity = 5),
    SHUTDOWN(severity = 6)
}

data class DeviceWorkloadState(
    val policy: DeviceOperatingPolicy = DeviceOperatingPolicy.PERFORMANCE,
    val thermalLevel: ThermalLevel = ThermalLevel.NONE,
    val batteryPercent: Int? = null,
    val isCharging: Boolean = true
)
