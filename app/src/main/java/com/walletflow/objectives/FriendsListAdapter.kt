package com.walletflow.objectives

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.walletflow.R
import com.walletflow.models.User

class FriendsListAdapter (private val friendList: ArrayList<User>,
                          private val onClickAdd: (User) -> Boolean,
                          private val onClickRemove : (User)->Boolean): RecyclerView.Adapter<FriendsListAdapter.UserViewHolder>() {

    private var mRecyclerView : RecyclerView? = null

    class UserViewHolder(itemView: MaterialCardView, val onClick: (User) -> Boolean,
                         val onClickRemover: (User) -> Boolean) : RecyclerView.ViewHolder(itemView) {
        val username = itemView.findViewById<TextView>(R.id.tvFriendUsername)
        val email = itemView.findViewById<TextView>(R.id.tvFriendEmail)
        var currentUser : User? = null
        init {
            itemView.setOnClickListener {
                currentUser?.let {
                    if(!itemView.isChecked) {
                        itemView.isChecked = onClick(it)
                    }
                    else{
                        itemView.isChecked = !onClickRemover(it)
                    }
                }
            }
        }

        fun bind(user: User) {
            currentUser = user
            username.text = user.username
            email.text=user.email
        }
    }


    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)
        mRecyclerView = recyclerView
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view: MaterialCardView = LayoutInflater.from(parent.context).inflate(R.layout.friend_cardview, parent, false) as MaterialCardView
        view.isCheckable=true
        return UserViewHolder(view, onClickAdd, onClickRemove)
    }


    override fun getItemCount(): Int {
        return friendList.size
    }


    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = friendList[position]
        holder.bind(currentUser)
    }

    fun deselectCard(user : User){
        val card = mRecyclerView?.layoutManager?.findViewByPosition(friendList.indexOf(user)) as MaterialCardView
        card.isChecked = false
        card.isCheckable = !card.isChecked
    }
}