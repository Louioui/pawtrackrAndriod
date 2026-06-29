package com.example.pawtrackr.data.local.converters

import androidx.room.TypeConverter
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * Stores monetary values as plain decimal strings (SQLite TEXT) so currency math
 * stays exact. This mirrors the iOS mandate: never persist money as Double/Float.
 * All amounts are normalised to scale 2 (HALF_UP) on the way in.
 */
class FinancialConverter {
    @TypeConverter
    fun fromString(value: String?): BigDecimal? =
        value?.let { BigDecimal(it).setScale(2, RoundingMode.HALF_UP) }

    @TypeConverter
    fun toAmountString(amount: BigDecimal?): String? =
        amount?.setScale(2, RoundingMode.HALF_UP)?.toPlainString()
}
