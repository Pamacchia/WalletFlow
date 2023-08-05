package com.walletflow.models

import android.os.Parcel
import android.os.Parcelable
import java.util.Date

data class Objective (
    val name :String? = null,
    val amount : Double? = 0.0,
    val date :  String? = null,
    val admin : String? = null
    ) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeValue(amount)
        parcel.writeString(date)
        parcel.writeString(admin)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Objective> {
        override fun createFromParcel(parcel: Parcel): Objective {
            return Objective(parcel)
        }

        override fun newArray(size: Int): Array<Objective?> {
            return arrayOfNulls(size)
        }
    }
}