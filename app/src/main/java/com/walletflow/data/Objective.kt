package com.walletflow.data

import java.io.Serializable

data class Objective(
    val name : String = "",
    val amount : Double? = 0.0,
    val date : String = "",
    val admin : String = "",
    val completed : Boolean? = false,
    val category : String? = "",
) : Serializable