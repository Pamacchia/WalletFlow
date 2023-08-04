package com.walletflow.objectives

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.walletflow.BaseActivity
import com.walletflow.R
import com.walletflow.models.FriendRequest
import com.walletflow.models.User

class AddFriendsToObjectiveActivity : BaseActivity() {

    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var groupRecyclerView: RecyclerView
    private lateinit var fab : FloatingActionButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        fab = findViewById(R.id.fab)

        val sharedPreferences = this.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE)
        val userID = sharedPreferences.getString("userID", "")
        val db = FirebaseFirestore.getInstance()
        val friends = db.collection("friends").whereEqualTo("accepted", true)

        val senderQuery = friends.whereEqualTo("sender", userID).get()
        val receiverQuery = friends.whereEqualTo("receiver", userID).get()

        Tasks.whenAllSuccess<QuerySnapshot>(senderQuery, receiverQuery)
            .addOnSuccessListener { requestQueryList ->
                var userQueryList : ArrayList<Task<QuerySnapshot>> = userQueryList(requestQueryList[0], db, false)
                userQueryList.addAll(userQueryList(requestQueryList[1], db, true))
                getFriends(userQueryList)
            }
            .addOnFailureListener {
                Log.w(this.localClassName, "Or request query error")
            }

        fab.setOnClickListener {
            returnGroup()
        }
    }

    private fun returnGroup(){
        val intent = Intent(this, AddObjectiveActivity::class.java)
        val friendGroup = (groupRecyclerView.adapter as FriendsGroupAdapter).getGroupList()

        if (friendGroup.isNullOrEmpty()) {
            Toast.makeText(this, "You need to select at least a friend!", Toast.LENGTH_LONG).show()
        } else {
            intent.putExtra("group", friendGroup)
            finish()
            startActivity(intent)
        }
    }

    private fun userQueryList ( requestQuery: QuerySnapshot, db: FirebaseFirestore, receiver : Boolean): ArrayList<Task<QuerySnapshot>> {
        val userQueryList = arrayListOf<Task<QuerySnapshot>>()
        requestQuery.forEach { documentSnapshot ->
            val users = db.collection("users")
            val request = documentSnapshot.toObject(FriendRequest::class.java)
            val username = if(receiver) request.sender else request.receiver
            userQueryList.add(users.whereEqualTo("username", username).get())
        }
        return userQueryList
    }

    private fun getFriends(userQueryList: ArrayList<Task<QuerySnapshot>>) {
        Tasks.whenAllSuccess<QuerySnapshot>(userQueryList).addOnSuccessListener { queryList ->
            val friendsList = arrayListOf<User>()
            queryList.forEach { result ->
                friendsList.add(result.documents.first().toObject(User::class.java)!!)
            }
            setUpRecyclerViews(friendsList)
        }
        .addOnFailureListener {
            Log.w(this.localClassName, "Or user query error")
        }
    }

    private fun setUpRecyclerViews(friendsList: ArrayList<User>) {
        lateinit var groupAdapter: FriendsGroupAdapter
        val friendsAdapter = FriendsListAdapter(
            friendsList,
            { user -> groupAdapter.addFriendToGroup(user) },
            { user -> groupAdapter.removeFriendFromGroup(user) })
        groupAdapter = FriendsGroupAdapter { friend -> friendsAdapter.deselectCard(friend) }

        friendsRecyclerView = findViewById(R.id.friendRecycleView)
        groupRecyclerView = findViewById(R.id.groupRecycleView)
        friendsRecyclerView.adapter = friendsAdapter
        groupRecyclerView.adapter = groupAdapter
    }

    override fun getLayoutResourceId(): Int {
        return R.layout.activity_add_friends_to_objective
    }
}