package com.example.pawtrackr.data.repository

import com.example.pawtrackr.data.local.dao.ServiceDao
import com.example.pawtrackr.data.local.entities.ServiceEntity
import com.example.pawtrackr.data.mapper.toDomain
import com.example.pawtrackr.domain.model.Service
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.math.BigDecimal

/** Catalog of services that can be added to a visit. Seeds a default catalog on first run. */
class ServiceRepository(
    private val serviceDao: ServiceDao,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {
    fun watchServices(): Flow<List<Service>> =
        serviceDao.watchEnabledServices()
            .map { list -> list.map { it.toDomain() } }
            .flowOn(ioDispatcher)

    suspend fun seedDefaultsIfEmpty() = withContext(ioDispatcher) {
        if (serviceDao.count() == 0) serviceDao.upsertServices(DEFAULTS)
    }

    private companion object {
        // Ported from iOS DefaultServiceCatalog (names/categories), with example prices so
        // checkout has something to total. Category raw values mirror Service.Category.
        private fun svc(name: String, category: String, price: String, pkg: Boolean = false) =
            ServiceEntity(name = name, categoryRaw = category, basePrice = BigDecimal(price), isPackage = pkg)

        val DEFAULTS = listOf(
            svc("Full Package", "Package", "85.00", pkg = true),
            svc("Basic Package", "Package", "55.00", pkg = true),
            svc("Bath", "Grooming", "30.00"),
            svc("Haircut", "Grooming", "40.00"),
            svc("De-shedding", "Add-on", "20.00"),
            svc("Paw Trim", "Add-on", "15.00"),
            svc("Flea & Ticks Treatment", "Add-on", "25.00"),
            svc("Face Grooming", "Add-on", "10.00")
        )
    }
}
