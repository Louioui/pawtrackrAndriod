package com.pawtrackr.app.core.runtime

import org.junit.Assert.assertEquals
import org.junit.Test

class PawtrackrRuntimeServicesTest {

    @Test
    fun start_startsServicesInRegistrationOrderOnce() {
        val events = mutableListOf<String>()
        val first = RecordingRuntimeService(name = "workload", events = events)
        val second = RecordingRuntimeService(name = "ring-buffer", events = events)
        val runtimeServices = PawtrackrRuntimeServices(listOf(first, second))

        runtimeServices.start()
        runtimeServices.start()

        assertEquals(listOf("start:workload", "start:ring-buffer"), events)
    }

    @Test
    fun stop_stopsServicesInReverseOrderOnce() {
        val events = mutableListOf<String>()
        val first = RecordingRuntimeService(name = "workload", events = events)
        val second = RecordingRuntimeService(name = "ring-buffer", events = events)
        val runtimeServices = PawtrackrRuntimeServices(listOf(first, second))

        runtimeServices.start()
        runtimeServices.stop()
        runtimeServices.stop()

        assertEquals(
            listOf(
                "start:workload",
                "start:ring-buffer",
                "stop:ring-buffer",
                "stop:workload"
            ),
            events
        )
    }

    @Test
    fun close_delegatesToStop() {
        val events = mutableListOf<String>()
        val service = RecordingRuntimeService(name = "ring-buffer", events = events)
        val runtimeServices = PawtrackrRuntimeServices(listOf(service))

        runtimeServices.start()
        runtimeServices.close()

        assertEquals(listOf("start:ring-buffer", "stop:ring-buffer"), events)
    }
}

private class RecordingRuntimeService(
    private val name: String,
    private val events: MutableList<String>
) : PawtrackrRuntimeService {
    override fun start() {
        events += "start:$name"
    }

    override fun stop() {
        events += "stop:$name"
    }
}
