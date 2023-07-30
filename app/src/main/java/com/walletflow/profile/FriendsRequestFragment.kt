package com.walletflow.profile

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.walletflow.R

class FriendsRequestFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_request, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val friendCollection = db.collection("friends")

        filterRequestedFriends(friendCollection, false, userID)

    }

    private fun filterRequestedFriends(
        queryRef: Query,
        type: Boolean,
        userID: String?,
    ) {

        val rootView = requireView().findViewById<LinearLayout>(R.id.layoutFriendRequests)
        rootView.removeAllViews()

        queryRef
            .whereEqualTo("accepted", type)
            .get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    for (document in task.result) {

                        val inflater = LayoutInflater.from(requireContext())
                        val cardView = inflater.inflate(R.layout.friend_request_cardview, rootView, false) as CardView
                        val tvUsername = cardView.findViewById<TextView>(R.id.tvFriendRequestUsername)
                        val tvEmail = cardView.findViewById<TextView>(R.id.tvFriendRequestEmail)
                        val tvDate = cardView.findViewById<TextView>(R.id.tvFriendRequestDate)

                        val sender = document.getString("sender")
                        val receiver = document.getString("receiver")

                        if(userID == sender){
                            tvUsername.text = receiver
                        } else {
                            tvUsername.text = sender
                        }

                        tvEmail.text = "fake@email.com"
                        tvDate.text = "01/01/1990"

                        val rejectButton = cardView.findViewById<Button>(R.id.btnFriendRequestReject)
                        rejectButton.setOnClickListener {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    println("Document deleted successfully.")
                                }
                                .addOnFailureListener { e ->
                                    println("Error deleting document: $e")
                                }
                        }

                        val acceptButton = cardView.findViewById<Button>(R.id.btnFriendRequestAccept)
                        acceptButton.setOnClickListener {

                            val updatedFields = mapOf(
                                "accepted" to true
                            )

                            document.reference
                                .update(updatedFields)
                                .addOnSuccessListener {
                                    println("Document updated successfully.")
                                }
                                .addOnFailureListener { e ->
                                    println("Error updating document: $e")
                                }
                        }

                        rootView.addView(cardView)
                        Log.w(context.toString(), document.data.toString())
                    }

                } else {
                    Log.w(requireContext().toString(), "Error getting transactions")
                }
            }
    }

}