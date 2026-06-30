package com.example.pawtrackr.data.repository

import androidx.room.withTransaction
import com.example.pawtrackr.data.local.PawtrackrDatabase
import com.example.pawtrackr.data.local.entities.CheckoutTransactionEntity
import com.example.pawtrackr.data.local.entities.PaymentEntity
import com.example.pawtrackr.data.local.entities.VisitEntity
import com.example.pawtrackr.data.local.entities.VisitItemEntity
import com.example.pawtrackr.domain.checkout.CheckoutMath
import com.example.pawtrackr.domain.checkout.CheckoutRequest
import com.example.pawtrackr.domain.checkout.CheckoutResult
import com.example.pawtrackr.domain.checkout.LineInput
import com.example.pawtrackr.domain.loyalty.LoyaltyEngine
import com.pawtrackr.app.core.storage.CheckoutAuditBuffer
import java.math.BigDecimal

/**
 * Atomic, idempotent checkout — ports `CheckoutTransactionActor`.
 *
 * The whole operation runs inside a single Room transaction (SQLite's single-writer lock
 * makes the read-check-write genuinely atomic). Idempotency: a durable
 * [CheckoutTransactionEntity] keyed by `"checkout:<visitId>"`. If it is already `succeeded`,
 * the call is a no-op replay — no double charge.
 *
 * Money discipline: `Payment.amount` and `Visit.total` are set to the authoritative
 * `finalTotal`; line-item unit prices are a best-effort breakdown (see [CheckoutMath]).
 *
 * After the transaction commits, [SummaryRepository.rebuildAll] refreshes the analytics
 * rollups (DaySummary/ServiceDaySummary/…) that Insights reads.
 *
 * DEFERRED PARITY (not yet ported, matching iOS): before/after visit photos, manual-amount
 * override. (Loyalty-point accrual and tips ARE ported.)
 */
class CheckoutRepository(
    private val db: PawtrackrDatabase,
    private val summaryRepository: SummaryRepository,
    private val checkoutAuditBuffer: CheckoutAuditBuffer = CheckoutAuditBuffer.NoOp
) {
    suspend fun processCheckout(request: CheckoutRequest): CheckoutResult {
        val execution = runCheckout(request)
        execution.auditTransaction?.let { transaction ->
            runCatching { checkoutAuditBuffer.offer(transaction) }
        }
        // Outside the checkout transaction: rebuild analytics from the committed visit data.
        summaryRepository.rebuildAll()
        return execution.result
    }

    private suspend fun runCheckout(request: CheckoutRequest): CheckoutExecution = db.withTransaction {
        val key = "checkout:${request.visitId}"
        val existing = db.checkoutTransactionDao().getByIdempotencyKey(key)

        // Idempotency short-circuit: already done -> replay the result, charge nothing again.
        if (existing?.statusRaw == "succeeded" && existing.completedAt != null) {
            val visit = db.visitDao().getVisitById(request.visitId)
            return@withTransaction CheckoutExecution(
                result = CheckoutResult(
                    visitId = request.visitId,
                    total = visit?.total ?: existing.amount,
                    endedAt = existing.completedAt,
                    wasAlreadyComplete = true
                ),
                auditTransaction = null
            )
        }

        val now = System.currentTimeMillis()

        // Order selected services per the request, snapshot price + category into line items.
        val byId = db.serviceDao().getByIds(request.selectedServiceIds).associateBy { it.id }
        val ordered = request.selectedServiceIds.mapNotNull { byId[it] }
        val lines = ordered.map { LineInput(it.basePrice ?: BigDecimal.ZERO, 1) }
        val naturalSubtotal = CheckoutMath.persistedSubtotal(lines)
        // Services reconcile to this; the tip rides on top of the grand total only.
        val servicesTotal = CheckoutMath.round(request.amount ?: naturalSubtotal)
        val finalTotal = CheckoutMath.round(servicesTotal + request.tip)
        val unitPrices = CheckoutMath.reconcileUnitPrices(lines, servicesTotal)
        val loyaltyPoints = LoyaltyEngine.calculatePoints(finalTotal)

        // Replace line items with the current selection's snapshots.
        db.visitDao().deleteItemsForVisit(request.visitId)
        ordered.forEachIndexed { i, svc ->
            db.visitDao().upsertItem(
                VisitItemEntity(
                    visitId = request.visitId,
                    serviceId = svc.id,
                    name = svc.name,
                    serviceCategoryRaw = svc.categoryRaw,
                    unitPrice = unitPrices[i],
                    quantity = 1,
                    lastModifiedBy = request.userId.orEmpty()
                )
            )
        }

        // Payment — authoritative amount is finalTotal, never the summed lines.
        val payment = (db.visitDao().getPaymentForVisit(request.visitId) ?: PaymentEntity(visitId = request.visitId))
            .copy(
                visitId = request.visitId,
                amount = finalTotal,
                methodRaw = request.method.raw,
                externalReference = request.externalReference,
                paidAt = now,
                updatedAt = now,
                lastModifiedAt = now,
                lastModifiedBy = request.userId.orEmpty()
            )
        db.visitDao().upsertPayment(payment)

        // Close the visit: total = finalTotal (authoritative), endedAt = now.
        val visit = db.visitDao().getVisitById(request.visitId)
            ?: VisitEntity(id = request.visitId, petId = request.petId, userId = request.userId)
        db.visitDao().upsertVisit(
            visit.copy(
                petId = visit.petId ?: request.petId,
                total = finalTotal,
                endedAt = now,
                note = request.note ?: visit.note,
                loyaltyPointsChange = loyaltyPoints,
                beforePhotoData = request.beforePhoto ?: visit.beforePhotoData,
                beforeThumbnailData = request.beforeThumb ?: visit.beforeThumbnailData,
                afterPhotoData = request.afterPhoto ?: visit.afterPhotoData,
                afterThumbnailData = request.afterThumb ?: visit.afterThumbnailData,
                updatedAt = now,
                lastModifiedAt = now,
                lastModifiedBy = request.userId.orEmpty()
            )
        )

        // Accrue loyalty points to the owning client (1 per whole dollar). Runs only on the
        // non-replay path (idempotency short-circuits above), so points are never double-counted.
        request.clientId?.let { clientId ->
            db.clientDao().getClientById(clientId)?.let { client ->
                db.clientDao().upsertClient(
                    client.copy(loyaltyPoints = client.loyaltyPoints + loyaltyPoints, updatedAt = now)
                )
            }
        }

        // Mark the durable transaction succeeded (create or update).
        val txn = (existing ?: CheckoutTransactionEntity(idempotencyKey = key))
            .copy(
                idempotencyKey = key,
                visitUUID = request.visitId,
                petUUID = request.petId,
                clientUUID = request.clientId,
                amount = finalTotal,
                methodRaw = request.method.raw,
                externalReference = request.externalReference,
                statusRaw = "succeeded",
                completedAt = now,
                updatedAt = now,
                attemptCount = (existing?.attemptCount ?: 0) + 1
            )
        db.checkoutTransactionDao().upsert(txn)

        CheckoutExecution(
            result = CheckoutResult(visitId = request.visitId, total = finalTotal, endedAt = now, wasAlreadyComplete = false),
            auditTransaction = txn
        )
    }

    private data class CheckoutExecution(
        val result: CheckoutResult,
        val auditTransaction: CheckoutTransactionEntity?
    )
}
