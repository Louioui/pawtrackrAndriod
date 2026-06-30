package com.pawtrackr.app.core.services

import org.junit.Assert.assertEquals
import org.junit.Test

class DeviceWorkloadPolicyReducerTest {

    @Test
    fun reduce_returnsPerformanceForHealthyChargingDevice() {
        val state = DeviceWorkloadPolicyReducer.reduce(
            thermalLevel = ThermalLevel.NONE,
            batteryPercent = 82,
            isCharging = true
        )

        assertEquals(DeviceOperatingPolicy.PERFORMANCE, state)
    }

    @Test
    fun reduce_returnsBalancedForLightThermalOrModeratelyLowBattery() {
        assertEquals(
            DeviceOperatingPolicy.BALANCED,
            DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = ThermalLevel.LIGHT,
                batteryPercent = 90,
                isCharging = true
            )
        )
        assertEquals(
            DeviceOperatingPolicy.BALANCED,
            DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = ThermalLevel.NONE,
                batteryPercent = 25,
                isCharging = false
            )
        )
    }

    @Test
    fun reduce_returnsThrottledForSevereThermalOrLowBattery() {
        assertEquals(
            DeviceOperatingPolicy.THROTTLED,
            DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = ThermalLevel.SEVERE,
                batteryPercent = 80,
                isCharging = true
            )
        )
        assertEquals(
            DeviceOperatingPolicy.THROTTLED,
            DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = ThermalLevel.NONE,
                batteryPercent = 12,
                isCharging = false
            )
        )
    }

    @Test
    fun reduce_returnsCriticalSuspendForCriticalThermalOrBatteryReserve() {
        assertEquals(
            DeviceOperatingPolicy.CRITICAL_SUSPEND,
            DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = ThermalLevel.CRITICAL,
                batteryPercent = 80,
                isCharging = true
            )
        )
        assertEquals(
            DeviceOperatingPolicy.CRITICAL_SUSPEND,
            DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = ThermalLevel.NONE,
                batteryPercent = 4,
                isCharging = false
            )
        )
    }

    @Test
    fun reduce_ignoresLowBatteryWhenCharging() {
        assertEquals(
            DeviceOperatingPolicy.PERFORMANCE,
            DeviceWorkloadPolicyReducer.reduce(
                thermalLevel = ThermalLevel.NONE,
                batteryPercent = 3,
                isCharging = true
            )
        )
    }
}
