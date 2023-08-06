package com.walletflow.data

import android.os.Parcel
import android.os.Parcelable

data class FriendRequest(
    val sender : String?=null,
    val receiver : String?=null,
    val accepted : Boolean=false) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(sender)
        parcel.writeString(receiver)
        parcel.writeByte(if (accepted) 1 else 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FriendRequest> {
        override fun createFromParcel(parcel: Parcel): FriendRequest {
            return FriendRequest(parcel)
        }

        override fun newArray(size: Int): Array<FriendRequest?> {
            return arrayOfNulls(size)
        }
    }
}
