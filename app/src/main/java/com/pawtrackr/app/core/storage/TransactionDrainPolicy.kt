package com.pawtrackr.app.core.storage

import com.pawtrackr.app.core.services.DeviceOperatingPolicy

object TransactionDrainPolicy {
    fun directiveFor(
        policy: DeviceOperatingPolicy,
        baseIntervalMillis: Long
    ): TransactionDrainDirective {
        val base = baseIntervalMillis.coerceAtLeast(1L)
        val multiplier = when (policy) {
            DeviceOperatingPolicy.PERFORMANCE -> 1L
            DeviceOperatingPolicy.BALANCED -> 2L
            DeviceOperatingPolicy.THROTTLED -> 4L
            DeviceOperatingPolicy.CRITICAL_SUSPEND -> 8L
        }

        return TransactionDrainDirective(
            shouldDrain = policy != DeviceOperatingPolicy.CRITICAL_SUSPEND,
            intervalMillis = base.safeTimes(multiplier)
        )
    }

    private fun Long.safeTimes(multiplier: Long): Long =
        if (this > Long.MAX_VALUE / multiplier) Long.MAX_VALUE else this * multiplier
}

data class TransactionDrainDirective(
    val shouldDrain: Boolean,
    val intervalMillis: Long
)
