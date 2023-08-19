package com.walletflow.friends

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
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.walletflow.BaseActivity
import com.walletflow.R

class FriendsListFragment(
    private val listener : (Query, (List<DocumentSnapshot>)->(Unit)) -> Unit
) : Fragment() {

    private lateinit var fragmentActivity : BaseActivity

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_friends_list, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        fragmentActivity = (activity as BaseActivity)
    }

    override fun onResume() {
        super.onResume()
        filterAcceptedFriends(
            fragmentActivity.db.collection("friends"),
            true,
            fragmentActivity.userID
        )
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
                        val cardView =
                            inflater.inflate(R.layout.friend_cardview, rootView, false) as CardView
                        val tvUsername = cardView.findViewById<TextView>(R.id.tvFriendUsername)
                        val tvEmail = cardView.findViewById<TextView>(R.id.tvFriendEmail)

                        val sender = document.getString("sender")
                        val receiver = document.getString("receiver")

                        if (userID == sender || userID == receiver) {

                            if (userID == sender) {
                                tvUsername.text = receiver
                            } else {
                                tvUsername.text = sender
                            }

                            tvEmail.text = "fake@email.com"

                            val deleteButton =
                                cardView.findViewById<Button>(R.id.btFriendElimination)
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
                    }

                } else {
                    Log.w(requireContext().toString(), "Error getting transactions")
                }
            }
    }

}