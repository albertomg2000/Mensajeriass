package com.example.mensajeria.adapters

import com.example.mensajeria.R
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import com.squareup.picasso.Picasso
import androidx.recyclerview.widget.RecyclerView
import com.example.mensajeria.activities.OnItemClickListener

class HorizontalImageAdapter(
    private val imageUrls: List<String>,
    private val context: Context,
    private val listener: OnItemClickListener
) : RecyclerView.Adapter<HorizontalImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.horizontal_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val imageUrl = imageUrls[position]
        Picasso.get().load(imageUrl).into(holder.imageView)
        holder.itemView.setOnClickListener { listener.OnItemClick(it, position) }
    }

    override fun getItemCount(): Int {
        return imageUrls.size
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val imageView: ImageView = itemView.findViewById(R.id.horizontalImageView)

        init {
            itemView.setOnClickListener(this)
        }

        override fun onClick(view: View) {
            val position = adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                listener.OnItemClick(view, position)
            }
        }
    }
}

