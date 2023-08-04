package com.walletflow.models

import android.os.Parcel
import android.os.Parcelable

data class Participant(
    val participant : String?=null,
    var quote : Double=0.0,
    var saved : Double?=0.0,
    var objectiveId : String?=null) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readDouble(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(participant)
        parcel.writeDouble(quote)
        parcel.writeValue(saved)
        parcel.writeString(objectiveId)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Participant> {
        override fun createFromParcel(parcel: Parcel): Participant {
            return Participant(parcel)
        }

        override fun newArray(size: Int): Array<Participant?> {
            return arrayOfNulls(size)
        }
    }
}