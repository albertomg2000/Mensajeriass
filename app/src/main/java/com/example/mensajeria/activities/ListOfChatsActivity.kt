package com.example.mensajeria.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.ContextMenu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ListView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import com.example.mensajeria.R
import com.example.mensajeria.adapters.ChatAdapter
import com.example.mensajeria.models.Chat
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_list_of_chats.*
import java.util.*

// la clase mas importante, donde se recogen todos mis chats
class ListOfChatsActivity : AppCompatActivity() {
    private var user = ""
    private var miUsuario= ""
    private var username=""
    private var db = Firebase.firestore
    private val mHandler = Handler()
    var NumeroDeChats=0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_of_chats)
        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        mHandler.postDelayed(mRunnable, 10000)
        intent.getStringExtra("user")?.let { user = it }
        miUsuario=user
        val fullText = user
        val atIndex = fullText.lastIndexOf("@")
        // hago que se vea mi imagen de perfil en el toolbar a la derecha, si clickeo me lleva a mi perfil
        foto.setOnClickListener{
            if (atIndex != -1) {
                 username = fullText.substring(0, atIndex)
            }
            val intent = Intent(this, nuestroPerfil::class.java)
            intent.putExtra("user", user)
            intent.putExtra("username", username)
            startActivity(intent)
            true
            finish()
        }
        val storageRef = FirebaseStorage.getInstance().getReference().child("images/users/$user/profile.png")
        val foto = findViewById<ImageButton>(R.id.foto)
// Definir las dimensiones máximas de la imagen
        val maxWidth = 100
        val maxHeight = 100
// Descargar imagen del Storage y convertirla a Bitmap
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            // Decodificar los bytes en un Bitmap
            val options = BitmapFactory.Options().apply {
                // Calcular la escala de la imagen para que se ajuste al máximo ancho y altura
                inJustDecodeBounds = true
                BitmapFactory.decodeByteArray(bytes, 0, bytes.size, this)
                inSampleSize = calculateInSampleSize(this, maxWidth, maxHeight)
                inJustDecodeBounds = false
            }
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            val drawable = RoundedBitmapDrawableFactory.create(resources, bmp)
            drawable.isCircular = true
            foto.setImageDrawable(drawable)
            foto.background = resources.getDrawable(R.drawable.rounded_image)
        }.addOnFailureListener { exception ->
        }
            insertarDatos(user)

        if (user.isNotEmpty()) {
            initViews()
        }
        //lista con los chats
        val listaaa = findViewById<View>(R.id.ListViews) as ListView
        registerForContextMenu(listaaa)
        listaaa.setOnItemClickListener { parent, view, position, id ->
            //obtengo todos los chats que tengo en mi usuario
            var Seleccion = (ListViews.adapter as ChatAdapter).getData()
            chatSelected( Seleccion.get(position))

        }

    }

    //para ver cada 10 segundos si tenemos un nuevo chat
    private val mRunnable = object : Runnable {
        override fun run() {
            val userRef = db.collection("users").document(user)
            userRef.collection("chats")
                .get()
                .addOnSuccessListener { chats ->
                    val listChats = chats.toObjects(Chat::class.java)
                    if(listChats.size!==NumeroDeChats){
                        initViews()
                    }
                    mHandler.postDelayed(this, 10000) // se llama a si mismo cada 10 segundos
                }
        }
    }

    //agrego el nuevo perfil a mi base de datos (solo si es nuevo)
    private fun insertarDatos(usuario:String) {
        val db = Firebase.firestore
        val datos= hashMapOf("name" to usuario)
        db.collection("Perfiles").document(user).set(datos).addOnSuccessListener {
            Log.i("Firebase", "Datos insertados correctamente")
        }.addOnFailureListener { error ->
            Log.e("FirebaseError",error.message.toString())
        }
    }
    private fun calculateInSampleSize(options: BitmapFactory.Options, maxWidth: Int, maxHeight: Int): Int {
        var inSampleSize = 1
        if (options.outHeight > maxHeight || options.outWidth > maxWidth) {
            val halfHeight = options.outHeight / 2
            val halfWidth = options.outWidth / 2
            while ((halfHeight / inSampleSize) >= maxHeight && (halfWidth / inSampleSize) >= maxWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }


    //long click
    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View?,
        menuInfo: ContextMenu.ContextMenuInfo
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater = menuInflater
        val info = menuInfo as AdapterView.AdapterContextMenuInfo

        inflater.inflate(R.menu.context_menu, menu)
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val userRef = db.collection("users").document(user)

        userRef.collection("chats")
            .get()
            .addOnSuccessListener { chats ->
                val listChats = chats.toObjects(Chat::class.java)
                ListViews.adapter =
                    ChatAdapter(user,
                        this,
                        R.layout.item_chat, listChats
                    )
            }


        val listView1 = findViewById<ListView>(R.id.ListViews)
        listView1.cacheColorHint = 0
        listView1.adapter =  ListViews.adapter
        val info = item.menuInfo as AdapterView.AdapterContextMenuInfo
        return when (item.itemId) {

            //borrar chat si intento borrarlo me salte un mensaje de advertencia
            R.id.delete_item -> {
                System.out.println((ListViews.adapter as ChatAdapter).getData()[info.position].users[0])
                var Seleccion = (ListViews.adapter as ChatAdapter).getData()
                val builder = AlertDialog.Builder(this)
                val title = ImageView(this)
                title.setImageResource(R.drawable.warning)
                title.setBackgroundColor(Color.BLACK);
                title.setPadding(10, 10, 10, 10)
                builder.setCustomTitle(title)
                builder.setMessage("Estas a punto de borrar tus mensajes con " + (ListViews.adapter as ChatAdapter).getData()[info.position].users[1])
                val inflater = layoutInflater
                builder.setView(inflater.inflate(R.layout.layout_dialogo, null))
                builder.setPositiveButton("Borrar chat") { dialogo, _ ->
                    val db = FirebaseFirestore.getInstance()
                    val collectionRef = db.collection("users")
                    collectionRef.document((ListViews.adapter as ChatAdapter).getData()[info.position].users[1]).collection("chats").document((Seleccion.get(info.position).id)).delete()
                    collectionRef.document((ListViews.adapter as ChatAdapter).getData()[info.position].users[0]).collection("chats").document((Seleccion.get(info.position).id)).delete()
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener { exception ->
                            println("Error al eliminar el documento: $exception")
                        }
                    val collectionRefe = db.collection("chats")
                    collectionRefe.document((Seleccion.get(info.position).id)).delete()

                        .addOnSuccessListener {
                            initViews()
                        }
                        .addOnFailureListener { exception ->
                            println("Error al eliminar el documento: $exception")
                        }
                }
                builder.setNegativeButton("Cancelar") { dialog, which ->
                    dialog.dismiss()
                }
                builder.show()
                initViews()
                true
            }
            //entrar al chat
            R.id.Entrar -> {
                var Seleccion = (ListViews.adapter as ChatAdapter).getData()
                chatSelected( Seleccion.get(info.position))
                initViews()
                true}
            else-> { initViews()
                true
            }
        }
    }

    private var isButtonClickable = true
    private val clickDelayMillis = 3000L // 3 segundos
    private val clickHandler = Handler()
    private val clickRunnable = Runnable {
        isButtonClickable = true
    }
    private fun initViews(){
        // creo un nuevo chat y evito que al darle a buscar chats se me creen mas de un chat
        newChatButton.setOnClickListener {
            if (isButtonClickable) {
                newChat()
                isButtonClickable = false
                clickHandler.postDelayed(clickRunnable, clickDelayMillis)
            }
        }
        val userRef = db.collection("users").document(user)
        //recoge los numeros de listviews que habra
        userRef.collection("chats")
            .get()
            .addOnSuccessListener { chats ->
                val listChats = chats.toObjects(Chat::class.java)
                 NumeroDeChats = listChats.size
                ListViews.adapter =
                    ChatAdapter(user,
                        this,
                        R.layout.item_chat, listChats
                    )
            }
        //para que se vean los litsviews
        userRef.collection("chats")
            .get()
            .addOnSuccessListener { chats ->
                val listChats = chats.toObjects(Chat::class.java)
                (ListViews.adapter as ChatAdapter).setData(listChats)
            }

        userRef.collection("chats")
            .addSnapshotListener { chats, error ->
                if (error == null) {
                    chats?.let {
                        val listChats = it.toObjects(Chat::class.java)
                    }
                }
            }
    }
    private fun chatSelected(chat: Chat) {
        //Para ir al chat debo saber cual de los dos usuarios es el que intenta acceder al chat
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chatId", chat.id)
        intent.putExtra("user", user)

        if (user==chat.users[0]){
            intent.putExtra("otherUser", chat.users[1])
        }else{
            intent.putExtra("otherUser", chat.users[0])
        }

        newChatText.setText("")
        startActivity(intent)

    }
    //creacion de un nuevo chat
    private fun newChat() {
        val otherUser = newChatText.text.toString()
        val chatName = "$user y $otherUser"
        val chatName2 = "$otherUser y $user"
        val users = listOf(user, otherUser)
        val chatRef = db.collection("chats").whereEqualTo("name", chatName)
        val chatRef2 = db.collection("chats").whereEqualTo("name", chatName2)
        chatRef.get().addOnSuccessListener { messages ->
            val listMessages = messages.toObjects(Chat::class.java)
            chatRef2.get().addOnSuccessListener { messages ->
                val listMessages2 = messages.toObjects(Chat::class.java)

                if (listMessages.isEmpty() && listMessages2.isEmpty()) {
                    db.collection("Perfiles").document(otherUser).get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val chatId = UUID.randomUUID().toString()
                                val chat = Chat(
                                    id = chatId,
                                    name = chatName,
                                    users = users
                                )

                                db.collection("chats").document(chatId).set(chat)

                                db.collection("users").document(user).collection("chats")
                                    .document(chatId).set(chat)
                                db.collection("users").document(otherUser).collection("chats")
                                    .document(chatId).set(chat)

                                val intent = Intent(this, ChatActivity::class.java)
                                intent.putExtra("chatId", chatId)
                                intent.putExtra("user", user)
                                intent.putExtra("otherUser", otherUser)
                                newChatText.setText("")
                                startActivity(intent)
                            } else {
                                //si intento crear un chat con un usuario inexistente
                                newChatText.setText("")
                                Toast.makeText(this, "Usuario no encontrado", Toast.LENGTH_SHORT)
                                    .show()
                            }
                        }
                } else {
                    if (listMessages.isEmpty()) {
                        val chat = listMessages2.first()
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("chatId", chat.id)
                        intent.putExtra("user", chat.users[0])
                        intent.putExtra("otherUser", otherUser)
                        newChatText.setText("")
                        startActivity(intent)
                    }else {
                        val chat = listMessages.first()
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra("chatId", chat.id)
                        intent.putExtra("user", chat.users[0])
                        intent.putExtra("otherUser", otherUser)
                        newChatText.setText("")
                        startActivity(intent)
                    }
                }
            }
        }
    }
}





