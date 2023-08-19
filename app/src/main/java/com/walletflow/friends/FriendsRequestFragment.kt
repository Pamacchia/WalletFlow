package com.walletflow.friends

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.Gravity
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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.QuerySnapshot
import com.walletflow.BaseActivity
import com.walletflow.R

//TODO: Reload when deleting/accepting/canceling/rejecting
//TODO: Filter requests send/receive

class FriendsRequestFragment(
    private val listener : (Query, (List<DocumentSnapshot>)->(Unit)) -> Unit
) : Fragment() {

    lateinit var fragmentActivity : BaseActivity
    lateinit var btnAddFriend: Button
    lateinit var etAddFriend: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_request, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fragmentActivity = activity as BaseActivity
        btnAddFriend = view.findViewById(R.id.btnAddFriend)
        etAddFriend = view.findViewById(R.id.etAddFriend)

        val friendCollection = fragmentActivity.db.collection("friends")

        friendCollection.whereEqualTo("accepted", false).get().addOnSuccessListener {
            listener(it.query) {
                    documentSnapshots ->  filterRequestedFriends(documentSnapshots)
            }
        }

        btnAddFriend.setOnClickListener {

            val friendRequest: MutableMap<String, Any?> = HashMap()
            friendRequest["sender"] = fragmentActivity.userID
            friendRequest["receiver"] = etAddFriend.text.toString()
            friendRequest["accepted"] = false

            if (fragmentActivity.userID == etAddFriend.text.toString().lowercase()) {
                Toast.makeText(
                    context,
                    "We are happy for you, but please don't.",
                    Toast.LENGTH_LONG
                ).show()
            } else {
                checkFriends(fragmentActivity.db, friendRequest, friendCollection)
            }

        }

    }

    private fun filterRequestedFriends(
        documents : List<DocumentSnapshot>
    ) {

        val rootView = requireView().findViewById<LinearLayout>(R.id.layoutFriendRequests)
        rootView.removeAllViews()

        documents.forEach { document ->
                val inflater = LayoutInflater.from(requireContext())
                val cardView = inflater.inflate(
                    R.layout.friend_request_cardview,
                    rootView,
                    false
                ) as CardView
                val tvUsername =
                    cardView.findViewById<TextView>(R.id.tvFriendRequestUsername)

                val sender = document.getString("sender")
                val receiver = document.getString("receiver")

                val contentLayoutView =
                    cardView.findViewById<LinearLayout>(R.id.contentLayoutFriendRequestCard)

                val factor: Float = this.resources.displayMetrics.density

                val layoutParams = LinearLayout.LayoutParams(
                    60 * factor.toInt(), // width in pixels
                    60 * factor.toInt()  // height in pixels
                )
                layoutParams.gravity = Gravity.CENTER
                layoutParams.leftMargin = 10 * factor.toInt()


                if (fragmentActivity.userID == sender) {
                    tvUsername.text = receiver

                    val cancelButton = Button(cardView.context)
                    cancelButton.background =
                        resources.getDrawable(R.drawable.baseline_delete_24)
                    contentLayoutView.addView(cancelButton)

                    layoutParams.leftMargin = 80 * factor.toInt()
                    cancelButton.layoutParams = layoutParams

                    cancelButton.setOnClickListener {
                        document.reference.delete()
                    }
                    rootView.addView(cardView)
                } else if(fragmentActivity.userID == receiver){
                    tvUsername.text = sender

                    val acceptButton = Button(cardView.context)
                    acceptButton.background =
                        resources.getDrawable(R.drawable.baseline_thumb_up_24)
                    layoutParams.rightMargin = 10 * factor.toInt()
                    acceptButton.layoutParams = layoutParams
                    contentLayoutView.addView(acceptButton)

                    val rejectButton = Button(cardView.context)
                    rejectButton.background =
                        resources.getDrawable(R.drawable.baseline_thumb_down_24)
                    rejectButton.layoutParams = layoutParams
                    contentLayoutView.addView(rejectButton)

                    rejectButton.setOnClickListener {
                        document.reference.delete()
                    }

                    acceptButton.setOnClickListener {

                        val updatedFields = mapOf(
                            "accepted" to true
                        )

                        document.reference
                            .update(updatedFields)
                    }
                    rootView.addView(cardView)
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

}