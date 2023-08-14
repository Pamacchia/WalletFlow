package com.walletflow.data

import java.io.Serializable

data class Transaction(val amount : Double?, val category : String?, var note : String?,
val type : String?, val user : String?, val date : String? = null) : Serializable