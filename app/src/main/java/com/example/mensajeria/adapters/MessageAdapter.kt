package com.example.mensajeria.adapters

import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mensajeria.models.Message
import com.example.mensajeria.R
import kotlinx.android.synthetic.main.item_message.view.*

class MessageAdapter(private val user: String): RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private var messages: List<Message> = emptyList()

    fun setData(list: List<Message>){
        messages = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        return MessageViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_message,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        val message = messages[position]
        holder.itemView.myMessageTextView.minWidth=100
        holder.itemView.myMessageTextView.maxWidth=900
        holder.itemView.myMessageTextView.gravity=(Gravity.LEFT)

        holder.itemView.othersMessageTextView.minWidth=100
        holder.itemView.othersMessageTextView.maxWidth=900
        holder.itemView.othersMessageTextView.gravity=(Gravity.LEFT)

        if (message.message.length==1 || message.message.length==2 ){
            holder.itemView.myMessageTextView.gravity=(Gravity.CENTER)
        }
        if (message.message.length<=0){

        }
        if(user == message.from){
            holder.itemView.myMessageTextView.visibility = View.VISIBLE
            holder.itemView.othersMessageTextView.visibility = View.GONE

            holder.itemView.myMessageTextView.text = message.message
        } else {
            holder.itemView.myMessageTextView.visibility = View.GONE
            holder.itemView.othersMessageTextView.visibility = View.VISIBLE
            holder.itemView.othersMessageTextView.text = message.message
        }

    }

    override fun getItemCount(): Int {
        return messages.size
    }

    class MessageViewHolder(itemView: View): RecyclerView.ViewHolder(itemView)
}