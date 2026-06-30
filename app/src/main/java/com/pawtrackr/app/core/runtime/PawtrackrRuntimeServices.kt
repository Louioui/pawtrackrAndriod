package com.pawtrackr.app.core.runtime

import java.io.Closeable

class PawtrackrRuntimeServices(
    private val services: List<PawtrackrRuntimeService>
) : Closeable {
    private var started = false

    fun start() {
        if (started) return
        started = true
        services.forEach { service -> service.start() }
    }

    fun stop() {
        if (!started) return
        services.asReversed().forEach { service -> service.stop() }
        started = false
    }

    override fun close() {
        stop()
    }
}
