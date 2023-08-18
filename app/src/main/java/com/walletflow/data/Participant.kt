package com.walletflow.data

import java.io.Serializable

data class Participant(
    val objectiveId : String = "",
    var participant : String = "",
    val quote : Double = 0.0,
    var saved : Double= 0.0,
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