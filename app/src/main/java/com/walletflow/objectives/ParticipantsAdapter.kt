package com.walletflow.objectives

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.walletflow.R
import com.walletflow.data.Participant
import com.walletflow.data.User

class ParticipantsAdapter() : RecyclerView.Adapter<ParticipantsAdapter.ParticipantViewHolder>() {

    private var participants: ArrayList<Participant> = arrayListOf()

    constructor(participants: ArrayList<User>) : this() {
        participants.forEach { participant ->
            this.participants.add(Participant(participant.username))
        }
    }

    class ParticipantViewHolder(view: View) : RecyclerView.ViewHolder(view){
        private val tvUser : TextView = view.findViewById(R.id.username)
        private val etQuote : EditText = view.findViewById(R.id.quote)
        private var currentParticipant : Participant? = null

        fun bind(participant: Participant) {
            currentParticipant = participant
            tvUser.text = participant.participant
            etQuote.setText(participant.quote.toString())
            etQuote.setOnFocusChangeListener { view, hasFocus ->
                if (!hasFocus && (view as EditText).text.isNotEmpty()){
                    currentParticipant!!.quote = (view as EditText).text.toString().toDouble()
                }
            }
        }

        fun clearFocusEtQuote(){
            etQuote.clearFocus()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParticipantViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.friend_quote, parent, false)
        return ParticipantViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParticipantViewHolder, position: Int) {
        val currentParticipant = participants[position]
        holder.bind(currentParticipant)
    }

    override fun getItemCount(): Int {
        return participants.size
    }

    fun setQuotes(amount : Double?){
        if (amount!=null){
           participants.forEach { participant ->
               participant.quote = amount/(participants.size+1)
           }
            notifyDataSetChanged()
        }
    }

    fun getTotalOfTheQuotes() : Double{
        var total = 0.0
        participants.forEach { participant ->
            total += participant.quote
        }
        return total
    }

    fun getParticipantsList() : ArrayList<Participant>{
        return participants
    }

    fun setParticipantsListFromUsers(list : ArrayList<User>){
        list.forEach { participant ->
            this.participants.add(Participant(participant.username))
        }
    }
}