package com.example.pawtrackr.ui.checkout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.pawtrackr.data.repository.CheckoutRepository
import com.example.pawtrackr.data.repository.ServiceRepository
import com.example.pawtrackr.domain.checkout.CheckoutMath
import com.example.pawtrackr.domain.checkout.CheckoutRequest
import com.example.pawtrackr.domain.checkout.LineInput
import com.example.pawtrackr.domain.model.PaymentMethod
import com.example.pawtrackr.domain.model.Service
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.math.BigDecimal

val TIP_PRESETS = listOf(0, 15, 18, 20)

data class CheckoutUiState(
    val services: List<Service> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val method: PaymentMethod = PaymentMethod.CASH,
    val reference: String = "",
    val manualAmount: String = "",
    /** Effective services amount = manual override (if valid) else the selected-services sum. */
    val subtotal: BigDecimal = BigDecimal.ZERO.setScale(2),
    val tip: BigDecimal = BigDecimal.ZERO.setScale(2),
    val tipPercent: Int = 0,
    val total: BigDecimal = BigDecimal.ZERO.setScale(2),
    val overridden: Boolean = false,
    val hasBeforePhoto: Boolean = false,
    val hasAfterPhoto: Boolean = false,
    val processing: Boolean = false,
    val done: Boolean = false,
    val error: String? = null
) {
    val canConfirm: Boolean get() = selectedIds.isNotEmpty() && !processing && !done
    val needsReference: Boolean get() = method.requiresExternalReference
}

/** Parse a user-typed amount; null when blank or not a non-negative number. */
internal fun parseManualAmount(text: String): BigDecimal? =
    text.trim().takeIf { it.isNotEmpty() }
        ?.let { runCatching { BigDecimal(it) }.getOrNull() }
        ?.takeIf { it.signum() >= 0 }
        ?.let { CheckoutMath.round(it) }

/**
 * Drives one checkout for [visitId]. The "Confirm & Pay" guard ([processing]) plus the
 * repository's idempotency key make a double-tap a no-op — never a double charge.
 */
class CheckoutViewModel(
    private val serviceRepository: ServiceRepository,
    private val checkoutRepository: CheckoutRepository,
    private val visitId: String,
    private val petId: String,
    private val clientId: String?,
    private val userId: String?
) : ViewModel() {

    private val selected = MutableStateFlow<Set<String>>(emptySet())
    private val method = MutableStateFlow(PaymentMethod.CASH)
    private val reference = MutableStateFlow("")
    private val tipPercent = MutableStateFlow(0)
    private val manualAmount = MutableStateFlow("")
    private val photos = MutableStateFlow(Photos())
    private val status = MutableStateFlow(Status())

    private data class Status(val processing: Boolean = false, val done: Boolean = false, val error: String? = null)
    private data class Controls(val method: PaymentMethod, val reference: String, val tipPercent: Int, val manualAmount: String)
    private class Photos(
        val beforeFull: ByteArray? = null, val beforeThumb: ByteArray? = null,
        val afterFull: ByteArray? = null, val afterThumb: ByteArray? = null
    )

    val uiState: StateFlow<CheckoutUiState> =
        combine(
            serviceRepository.watchServices(),
            selected,
            combine(method, reference, tipPercent, manualAmount) { m, r, t, a -> Controls(m, r, t, a) },
            photos,
            status
        ) { services, sel, controls, photoState, st ->
            val chosen = services.filter { it.id in sel }
            val natural = CheckoutMath.persistedSubtotal(chosen.map { LineInput(it.basePrice, 1) })
            val override = parseManualAmount(controls.manualAmount)
            val services2 = override ?: natural
            val tip = CheckoutMath.tipAmount(services2, controls.tipPercent)
            CheckoutUiState(
                services = services,
                selectedIds = sel,
                method = controls.method,
                reference = controls.reference,
                manualAmount = controls.manualAmount,
                subtotal = services2,
                tip = tip,
                tipPercent = controls.tipPercent,
                total = CheckoutMath.round(services2 + tip),
                overridden = override != null && override.compareTo(natural) != 0,
                hasBeforePhoto = photoState.beforeThumb != null,
                hasAfterPhoto = photoState.afterThumb != null,
                processing = st.processing,
                done = st.done,
                error = st.error
            )
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CheckoutUiState())

    fun toggle(serviceId: String) {
        selected.value = selected.value.toMutableSet().apply { if (!add(serviceId)) remove(serviceId) }
    }

    fun setMethod(m: PaymentMethod) { method.value = m }
    fun setReference(value: String) { reference.value = value }
    fun setTipPercent(p: Int) { tipPercent.value = p }
    fun setManualAmount(value: String) { manualAmount.value = value }

    fun setBeforePhoto(full: ByteArray?, thumb: ByteArray?) {
        photos.value = Photos(full, thumb, photos.value.afterFull, photos.value.afterThumb)
    }

    fun setAfterPhoto(full: ByteArray?, thumb: ByteArray?) {
        photos.value = Photos(photos.value.beforeFull, photos.value.beforeThumb, full, thumb)
    }

    fun confirm() {
        val current = uiState.value
        if (!current.canConfirm) return
        status.value = Status(processing = true)
        val pics = photos.value
        viewModelScope.launch {
            try {
                checkoutRepository.processCheckout(
                    CheckoutRequest(
                        visitId = visitId,
                        petId = petId,
                        clientId = clientId,
                        userId = userId,
                        selectedServiceIds = current.selectedIds.toList(),
                        amount = parseManualAmount(current.manualAmount), // null => services use natural subtotal
                        tip = current.tip,
                        method = current.method,
                        externalReference = current.reference.ifBlank { null },
                        beforePhoto = pics.beforeFull,
                        beforeThumb = pics.beforeThumb,
                        afterPhoto = pics.afterFull,
                        afterThumb = pics.afterThumb
                    )
                )
                status.value = Status(done = true)
            } catch (e: Exception) {
                status.value = Status(error = e.message ?: "Checkout failed")
            }
        }
    }

    class Factory(
        private val serviceRepository: ServiceRepository,
        private val checkoutRepository: CheckoutRepository,
        private val visitId: String,
        private val petId: String,
        private val clientId: String?,
        private val userId: String?
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CheckoutViewModel(serviceRepository, checkoutRepository, visitId, petId, clientId, userId) as T
    }
}
