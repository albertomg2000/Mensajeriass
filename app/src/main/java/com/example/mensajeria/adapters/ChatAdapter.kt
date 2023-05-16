package com.example.mensajeria.adapters
import com.example.mensajeria.models.Chat
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.ImageView
import android.widget.TextView
import com.example.mensajeria.R
import com.google.firebase.storage.FirebaseStorage
import com.squareup.picasso.Picasso

class ChatAdapter(var miUser : String ,var context:Context?, var textViewResourceId: Int, var elementos: MutableList<Chat>?): BaseAdapter() {
    var chats: MutableList<Chat> =  mutableListOf()
    var listachats: MutableList<Chat> = mutableListOf()
    fun setData(list: MutableList<Chat>) {

        chats = list
        listachats = chats
        notifyDataSetChanged()
    }
    fun getData(): MutableList<Chat> {
        chats = listachats
        notifyDataSetChanged()
        return chats
    }
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View? {
        var vista=convertView
        val holder: ViewHolder
        if (vista == null) {
            val vi = context?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            vista = vi.inflate(R.layout.item_chat, null)
            holder = ViewHolder()
            holder.texto = vista.findViewById<View>(R.id.usersTextView) as TextView
            holder.texto2 = vista.findViewById<View>(R.id.chatNameText) as TextView
            holder.perfil = vista.findViewById<View>(R.id.perfil) as ImageView
            vista.tag = holder
        } else {
            holder = vista.tag as ViewHolder
        }
        val emp = elementos!![position]
        if (emp != null) {
            if (miUser==emp.users[1]){
                holder.texto2.text=emp.users[0]
                val storageRef = FirebaseStorage.getInstance().getReference().child("images/users/" + emp.users[0] + "/profile.png")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri).into(holder.perfil)
                }.addOnFailureListener {
                    // Manejar la falla si no se puede obtener la URL de la imagen
                }
            }else{
                holder.texto2.text=emp.users[1]
                val storageRef = FirebaseStorage.getInstance().getReference().child("images/users/" + emp.users[1] + "/profile.png")
                storageRef.downloadUrl.addOnSuccessListener { uri ->
                    Picasso.get().load(uri).into(holder.perfil)
                }.addOnFailureListener {
                    // Manejar la falla si no se puede obtener la URL de la imagen
                }
            }
            holder.texto.text=emp.name
        }
        return vista
    }

    override fun getCount(): Int {
        return elementos!!.size
    }

    override fun getItem(position: Int): Chat {
        return elementos!![position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    internal class ViewHolder {
        lateinit var texto2: TextView
        lateinit var texto: TextView
        lateinit var perfil: ImageView
    }
}