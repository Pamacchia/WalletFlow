package com.walletflow.objectives

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.widget.CheckBox
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.card.MaterialCardView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.QuerySnapshot
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.data.User

class AddFriendsToObjectiveActivity : BaseActivity() {

    var selectedFriends: ArrayList<String> = arrayListOf()
    private lateinit var fab: FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fab = findViewById(R.id.confirmGroupObjectiveFriends)
        val friends = db.collection("friends").whereEqualTo("accepted", true)
        val senderQuery = friends.whereEqualTo("sender", userID).get()
        val receiverQuery = friends.whereEqualTo("receiver", userID).get()

        Tasks.whenAllSuccess<QuerySnapshot>(senderQuery, receiverQuery)
            .addOnSuccessListener { requestQueryList ->
                var userQueryList: ArrayList<Task<QuerySnapshot>> =
                    userQueryList(requestQueryList[0], false)
                userQueryList.addAll(userQueryList(requestQueryList[1], true))
                getFriends(userQueryList)
            }

        fab.setOnClickListener { returnGroup() }

    }

    private fun returnGroup() {
        val intent = Intent(this, AddObjectiveActivity::class.java)

        if (selectedFriends.isNullOrEmpty()) {
            Toast.makeText(this, "You need to select at least a friend!", Toast.LENGTH_LONG).show()
        } else {
            selectedFriends.add(0,userID)
            intent.putStringArrayListExtra("group", selectedFriends)
            finish()
            startActivity(intent)
        }
    }

    private fun userQueryList(
        requestQuery: QuerySnapshot,
        receiver: Boolean
    ): ArrayList<Task<QuerySnapshot>> {
        val userQueryList = arrayListOf<Task<QuerySnapshot>>()
        requestQuery.forEach { documentSnapshot ->
            val users = db.collection("users")
            val username =
                if (receiver) documentSnapshot["sender"] else documentSnapshot["receiver"]
            userQueryList.add(users.whereEqualTo("username", username).get())
        }
        return userQueryList
    }

    private fun getFriends(userQueryList: ArrayList<Task<QuerySnapshot>>) {
        Tasks.whenAllSuccess<QuerySnapshot>(userQueryList)
            .addOnSuccessListener { queryList ->
                val friendsList = arrayListOf<User>()
                queryList.forEach { result ->

                    val friend = User(
                        result.first().getString("username")!!,
                        result.first().getString("email")!!,
                        result.first().getDouble("balance")!!
                    )

                    friendsList.add(
                        friend
                    )
                }
                setupFriendsList(friendsList)
            }
    }

    private fun setupFriendsList(friendsList: ArrayList<User>) {
        val cardContainer = findViewById<LinearLayout>(R.id.cardContainer)

        for (friend in friendsList) {
            val cardView = (LayoutInflater.from(this).inflate(R.layout.select_friend_layout, null) as MaterialCardView)
            val friendNameTextView = cardView.findViewById<TextView>(R.id.friendNameTextView)

            friendNameTextView.text = friend.username
            cardView.isCheckable = true
            cardView.isChecked =
                selectedFriends.contains(friend.username)

            cardView.setOnClickListener {
                val isSelected = cardView.isChecked
                if (isSelected) {
                    cardView.isChecked = false
                    selectedFriends.remove(friend.username)
                    Log.w(this.localClassName, selectedFriends.toString())
                } else {
                    cardView.isChecked = true
                    selectedFriends.add(friend.username)
                    Log.w(this.localClassName, selectedFriends.toString())
                }
            }

            cardContainer.addView(cardView)
            val factor: Float = this.resources.displayMetrics.density
            cardView.layoutParams.width = 320 * factor.toInt()
            cardView.layoutParams.height = 80 * factor.toInt()
        }
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_add_friends_to_objective
    }
}