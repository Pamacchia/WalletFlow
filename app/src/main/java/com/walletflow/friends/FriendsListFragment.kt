package com.walletflow.friends

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
        val friendCollection = fragmentActivity.db.collection("friends")

        friendCollection.whereEqualTo("accepted", true).get().addOnSuccessListener {
            listener(it.query) {
                    documentSnapshots ->  filterAcceptedFriends(documentSnapshots)
            }
        }
    }

    private fun filterAcceptedFriends(
        documents: List<DocumentSnapshot>,
    ) {

        val rootView = requireView().findViewById<LinearLayout>(R.id.layoutFriendsList)
        rootView.removeAllViews()

        documents.forEach { document->
            val inflater = LayoutInflater.from(requireContext())
            val cardView =
                inflater.inflate(R.layout.friend_cardview, rootView, false) as CardView
            val tvUsername = cardView.findViewById<TextView>(R.id.tvFriendUsername)

            val sender = document.getString("sender")
            val receiver = document.getString("receiver")

            if (fragmentActivity.userID == sender ||
                fragmentActivity.userID == receiver) {

                if (fragmentActivity.userID == sender) {
                    tvUsername.text = receiver
                } else {
                    tvUsername.text = sender
                }

                val deleteButton =
                    cardView.findViewById<Button>(R.id.btFriendElimination)
                deleteButton.setOnClickListener {
                    document.reference.delete()
                }

                rootView.addView(cardView)
                Log.w(context.toString(), document.data.toString())
            }
        }
    }

}