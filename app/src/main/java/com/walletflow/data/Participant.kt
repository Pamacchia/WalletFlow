package com.walletflow.data

import java.io.Serializable

data class Participant(
    val objectiveId : String,
    var participant : String,
    val quote : Double,
    var saved : Double,
) : Serializable