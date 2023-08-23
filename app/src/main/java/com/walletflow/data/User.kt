package com.walletflow.data

import java.io.Serializable

data class User(
    val username: String? = null,
    var email: String? = null,
    val balance: Double = 0.0,
) : Serializable