package com.example.pawtrackr.domain.checkout

import com.example.pawtrackr.domain.model.PaymentMethod
import java.math.BigDecimal

/**
 * Inputs to an atomic checkout. Ports the iOS `CheckoutRequest`. Identity is by
 * [visitId] (string UUID) — the same visit checked out twice is a no-op (idempotent).
 *
 * [amount] is the authoritative charged total (BigDecimal). When null, the repository
 * uses the natural subtotal of the selected services.
 */
data class CheckoutRequest(
    val visitId: String,
    val petId: String,
    val clientId: String?,
    val userId: String?,
    val selectedServiceIds: List<String>,
    val amount: BigDecimal? = null,
    /** Added on top of the services total; lands in Payment/Visit total, not in line items. */
    val tip: BigDecimal = BigDecimal.ZERO,
    val method: PaymentMethod = PaymentMethod.CASH,
    val externalReference: String? = null,
    val note: String? = null
)

data class CheckoutResult(
    val visitId: String,
    val total: BigDecimal,
    val endedAt: Long,
    /** True when the visit was already checked out and this call was a no-op replay. */
    val wasAlreadyComplete: Boolean
)
