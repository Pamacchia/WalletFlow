package com.walletflow.data

import android.os.Parcel
import android.os.Parcelable

data class Transaction(
    val amount: Double? = 0.0,
    val category: String? = null,
    var note: String? = null,
    val type: String? = null,
    val user: String? = null,
    val date: String? = null
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Double::class.java.classLoader) as? Double,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(amount)
        parcel.writeString(category)
        parcel.writeString(note)
        parcel.writeString(type)
        parcel.writeString(user)
        parcel.writeString(date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Transaction> {
        override fun createFromParcel(parcel: Parcel): Transaction {
            return Transaction(parcel)
        }

        override fun newArray(size: Int): Array<Transaction?> {
            return arrayOfNulls(size)
        }
    }
}