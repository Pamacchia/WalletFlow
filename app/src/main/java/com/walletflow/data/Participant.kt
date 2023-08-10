package com.walletflow.data

import java.io.Serializable

data class Participant(
    val objectiveId : String,
    var participant : String,
    val quote : Double,
    var saved : Double,
) : Serializable {
    fun toMap(): Map<String, Any> {
        return mapOf(
            "objectiveId" to objectiveId,
            "participant" to participant,
            "quote" to quote,
            "saved" to saved
        )
    }
}