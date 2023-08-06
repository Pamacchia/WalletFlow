package com.walletflow.objectives

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walletflow.R
import com.walletflow.data.Participant

class ObjectiveDetailFriendsAdapter (private val friendList: ArrayList<Participant>)
    : RecyclerView.Adapter<ObjectiveDetailFriendsAdapter.FriendViewHolder>() {

    private var mRecyclerView : RecyclerView? = null

    class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val username: TextView = itemView.findViewById(R.id.tvFriendUsername)
        private val savings: TextView = itemView.findViewById(R.id.tvFriendSavings)
        var currentParticipant : Participant? = null

        fun bind(participant: Participant) {
            currentParticipant = participant
            username.text = participant.participant
            savings.text="Has saved ${participant.saved} over ${participant.quote}"
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.participant_detail_item, parent, false)
        return FriendViewHolder(view)
    }


    override fun getItemCount(): Int {
        return friendList.size
    }


    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentUser = friendList[position]
        holder.bind(currentUser)
    }
}