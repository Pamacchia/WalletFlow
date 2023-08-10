package com.walletflow.data

import java.io.Serializable

data class User(
    val username : String,
    var email : String,
    val balance : Double,
) : Serializable