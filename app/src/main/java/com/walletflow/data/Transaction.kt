package com.walletflow.data

import java.io.Serializable

data class Transaction(
    val amount: Double? = 0.0,
    val category: String? = null,
    var note: String? = null,
    val type: String? = null,
    val user: String? = null,
    var date: String? = null
) : Serializable