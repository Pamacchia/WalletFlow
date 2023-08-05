package com.walletflow.objectives

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.walletflow.R
import com.walletflow.models.User

class FriendsGroupAdapter(private val onClickDeselect : (User)->Unit) : RecyclerView.Adapter<FriendsGroupAdapter.FriendViewHolder>() {
    private var friendsGroup: ArrayList<User> = arrayListOf()

    /* ViewHolder for displaying header. */
    class FriendViewHolder(view: View) : ViewHolder(view){
        private val button : Button = view.findViewById(R.id.addedFriend)

        fun addButton(friend: User, id : Int, onClick: (User) -> Unit, onClickDeselect: (User) -> Unit) {
            button.id = id
            button.text=friend.username
            button.setOnClickListener{
                friend?.let{
                    onClick(it)
                    onClickDeselect(friend)
                }
            }
        }
    }

    /* Inflates view and returns HeaderViewHolder. */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.add_friend_token, parent, false)
        return FriendViewHolder(view)
    }

    /* Binds number of flowers to the header. */
    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val currentUser = friendsGroup[position]
        holder.addButton(currentUser, position, { friend -> removeFriendFromGroup(friend)}, onClickDeselect)
    }

    /* Returns number of items, since there is only one item in the header return one  */
    override fun getItemCount(): Int {
        return friendsGroup.size
    }

    /* Updates header to display number of flowers when a flower is added or subtracted. */
    fun addFriendToGroup(newGroupMember : User) : Boolean{
        var result = false
        if (!friendsGroup.contains(newGroupMember)){
            result = friendsGroup.add(newGroupMember)
            notifyDataSetChanged()
        }
        return result
    }

    fun removeFriendFromGroup(friend : User) : Boolean{
        var result = friendsGroup.remove(friend)
        notifyDataSetChanged()
        return result
    }

    fun getGroupList() : ArrayList<User>{
        return friendsGroup
    }
}