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

    lateinit var btnAddFriend : Button
    lateinit var etAddFriend : EditText

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAddFriend = view.findViewById(R.id.btnAddFriend)
        etAddFriend = view.findViewById(R.id.etAddFriend)

        val sharedPreferences = requireContext().getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val friendCollection = db.collection("friends")

        filterAcceptedFriends(friendCollection, true, userID)

        btnAddFriend.setOnClickListener {

            val friendRequest: MutableMap<String, Any?> = HashMap()
            friendRequest["sender"] = userID
            friendRequest["receiver"] = etAddFriend.text.toString()
            friendRequest["accepted"] = false

            if (userID == etAddFriend.text.toString().lowercase()) {
                Toast.makeText(
                    context,
                    "We are happy for you, but please don't.",
                    Toast.LENGTH_LONG
                ).show()
            } else {

                checkFriends(db, friendRequest, friendCollection)

            }
        }

    }

    private fun checkFriends(
        db: FirebaseFirestore,
        friendRequest: MutableMap<String, Any?>,
        friendRequestsCollection: CollectionReference
    ) {
        db.collection("users")
            .whereEqualTo("username", friendRequest["receiver"])
            .get().addOnSuccessListener { task ->
                if (!task.isEmpty) {

                    val query1 = friendRequestsCollection
                        .whereEqualTo("sender", friendRequest["sender"])
                        .whereEqualTo("receiver", friendRequest["receiver"])

                    val query2 = friendRequestsCollection
                        .whereEqualTo("sender", friendRequest["receiver"])
                        .whereEqualTo("receiver", friendRequest["sender"])

                    val combinedQuery =
                        Tasks.whenAllSuccess<QuerySnapshot>(query1.get(), query2.get())

                    combinedQuery.addOnSuccessListener { querySnapshots ->
                        var recordExists = false

                        for (querySnapshot in querySnapshots) {
                            if (!querySnapshot.isEmpty) {
                                // There is at least one document that matches the conditions
                                Toast.makeText(
                                    context,
                                    "You are already friend with this user or you already sent a request.",
                                    Toast.LENGTH_LONG
                                ).show()
                                recordExists = true
                                break
                            }
                        }

                        if (!recordExists) {
                            addFriend(db, friendRequest)
                        } else {
                            Log.d("FriendRequest", "The record is already in the list.")
                        }
                    }

                } else {
                    Toast.makeText(
                        context,
                        "This user doesn't exist.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun addFriend(
        db: FirebaseFirestore,
        friendRequest: MutableMap<String, Any?>
    ) {
        db.collection("friends")
            .add(friendRequest)
            .addOnSuccessListener { documentReference ->
                Log.d(
                    "FriendRequest",
                    "DocumentSnapshot added with ID: " + documentReference.id
                )
            }
            .addOnFailureListener { e ->
                Log.w(
                    "FriendRequest",
                    "Error adding document",
                    e
                )
            }
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