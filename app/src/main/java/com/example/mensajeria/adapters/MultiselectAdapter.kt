package com.example.mensajeria.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mensajeria.models.Chat
import com.example.mensajeria.R

class MultiselectAdapter(private var xat: List<Chat>,
private val showMenuDelete:(Boolean)-> Unit
):RecyclerView.Adapter<MultiselectAdapter.MultiselectViewHolder>() {
    private var isEnable= false
    private val itemSelectedList= mutableListOf<Int>()

    class MultiselectViewHolder(private val view: View): RecyclerView.ViewHolder(view){
        val tv: TextView = view.findViewById(R.id.chatNameText)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MultiselectViewHolder {
        val adapterLayout = LayoutInflater.from(parent.context).inflate(R.layout.item_chat,parent,false)
        return MultiselectViewHolder(adapterLayout)
    }

    override fun onBindViewHolder(holder: MultiselectViewHolder, position: Int) {
        val item = xat[position]
        holder.tv.text = item.name.toString()


        holder.tv.setOnLongClickListener() {
            selectItem(holder, item, position)

            true
        }
        holder.tv.setOnClickListener() {
            if (itemSelectedList.contains(position)) {
                itemSelectedList.removeAt(position)

                item.selected = false
                if (itemSelectedList.isEmpty()) {
                    showMenuDelete(false)
                    isEnable = false
                }
                } else if (isEnable) {
                    selectItem(holder, item, position)
                }

            }
        }



    private fun selectItem(holder: MultiselectViewHolder, item: Chat, position: Int) {
        isEnable=true
        itemSelectedList.add(position)
        item.selected=true
        showMenuDelete(true)
    }

    override fun getItemCount(): Int {
        return xat.size
    }

        }


