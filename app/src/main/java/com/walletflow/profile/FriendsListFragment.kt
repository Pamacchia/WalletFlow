package com.walletflow.profile

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.walletflow.R

class FriendsListFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

    }

    override fun onResume() {
        super.onResume()

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val friendCollection = db.collection("friends")

        filterAcceptedFriends(friendCollection, true, userID)
    }

    private fun filterAcceptedFriends(
        queryRef: Query,
        type: Boolean,
        userID: String?,
    ) {

        val rootView = requireView().findViewById<LinearLayout>(R.id.layoutFriendsList)
        rootView.removeAllViews()

        queryRef
            .whereEqualTo("accepted", type)
            .get().addOnCompleteListener { task ->

                if (task.isSuccessful) {
                    for (document in task.result) {


                        val inflater = LayoutInflater.from(requireContext())
                        val cardView = inflater.inflate(R.layout.friend_cardview, rootView, false) as CardView
                        val tvUsername = cardView.findViewById<TextView>(R.id.tvFriendUsername)
                        val tvEmail = cardView.findViewById<TextView>(R.id.tvFriendEmail)

                        val sender = document.getString("sender")
                        val receiver = document.getString("receiver")

                        if(userID == sender){
                            tvUsername.text = receiver
                        } else {
                            tvUsername.text = sender
                        }

                        tvEmail.text = "fake@email.com"

                        val deleteButton = cardView.findViewById<Button>(R.id.btFriendElimination)
                        deleteButton.setOnClickListener {
                            document.reference.delete()
                                .addOnSuccessListener {
                                    filterAcceptedFriends(queryRef, true, userID)
                                    println("Document deleted successfully.")
                                }
                                .addOnFailureListener { e ->
                                    println("Error deleting document: $e")
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