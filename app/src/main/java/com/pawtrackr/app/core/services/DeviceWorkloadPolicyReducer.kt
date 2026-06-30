package com.pawtrackr.app.core.services

object DeviceWorkloadPolicyReducer {
    fun reduce(
        thermalLevel: ThermalLevel,
        batteryPercent: Int?,
        isCharging: Boolean
    ): DeviceOperatingPolicy {
        val battery = batteryPercent?.coerceIn(0, 100)

        return when {
            thermalLevel.severity >= ThermalLevel.CRITICAL.severity ->
                DeviceOperatingPolicy.CRITICAL_SUSPEND

            battery != null && !isCharging && battery <= CriticalBatteryPercent ->
                DeviceOperatingPolicy.CRITICAL_SUSPEND

            thermalLevel.severity >= ThermalLevel.SEVERE.severity ->
                DeviceOperatingPolicy.THROTTLED

            battery != null && !isCharging && battery <= LowBatteryPercent ->
                DeviceOperatingPolicy.THROTTLED

            thermalLevel.severity >= ThermalLevel.LIGHT.severity ->
                DeviceOperatingPolicy.BALANCED

            battery != null && !isCharging && battery <= BalancedBatteryPercent ->
                DeviceOperatingPolicy.BALANCED

            else -> DeviceOperatingPolicy.PERFORMANCE
        }
    }

    private const val CriticalBatteryPercent = 5
    private const val LowBatteryPercent = 15
    private const val BalancedBatteryPercent = 30
}
