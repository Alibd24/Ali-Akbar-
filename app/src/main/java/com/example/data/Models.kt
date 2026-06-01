package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

data class FormProduct(
    val name: String,
    val group: String,
    val batch: String = "",
    val mfg: String = "",
    val exp: String = "",
    val pack: Int = 1,
    val qty: Double = 0.0,
    val tp: Double = 0.0
) {
    val value: Double
        get() = if (pack > 0) (tp / pack) * qty else 0.0
}

data class GroupMeta(
    val mpo: String = "",
    val asm: String = "",
    val action: String = ""
)

@Entity(tableName = "withdrawal_forms")
data class WithdrawalForm(
    @PrimaryKey val id: String,
    val createdAt: Long,
    val salesCenter: String = "",
    val formDate: String = "",
    val chemistDetails: String = "",
    val filledBy: String = "",
    val filledByMobile: String = "",
    val handedBy: String = "",
    val handedByMobile: String = "",
    val takenBy: String = "",
    val takenByMobile: String = "",
    val invoiceNo: String = "",
    val invoiceDate: String = "",
    val groupMetaMap: Map<String, GroupMeta> = emptyMap(),
    val products: List<FormProduct> = emptyList()
) {
    val totalValue: Double
        get() = products.sumOf { it.value }
}
