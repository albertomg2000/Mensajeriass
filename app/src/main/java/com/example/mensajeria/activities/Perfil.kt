package com.example.mensajeria.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mensajeria.R
import com.example.mensajeria.adapters.HorizontalImageAdapter
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.perfil.*

private var otherUser = ""
private var nameOtherUser = ""
private var chatId = ""
private var user = ""
class PerfilActivity : AppCompatActivity() {
    private lateinit var LoadingImage: ImageView
    private lateinit var LoadingLetter: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.perfil)

        // Instanciar los widgets
        val profilePictureImageView = findViewById<ImageView>(R.id.profilePictureImageView)
        val nameTextView = findViewById<TextView>(R.id.nameTextView)
        val emailTextView = findViewById<TextView>(R.id.emailTextView)
        val statusTextView = findViewById<TextView>(R.id.statusTextView)
        val RecyclerVieww = findViewById<RecyclerView>(R.id.horizontalRecyclerView)
        val relativeLayout = findViewById<RelativeLayout>(R.id.RelativeLayout)
        intent.getStringExtra("otherUser")?.let { otherUser = it }
        intent.getStringExtra("nameOtherUser")?.let { nameOtherUser = it }
        intent.getStringExtra("chatId")?.let { chatId = it }
        intent.getStringExtra("user")?.let { user = it }


        LoadingImage = findViewById(R.id.loading_image_view)
        LoadingLetter = findViewById(R.id.loading_letter_view)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.negro)
        }
        profilePictureImageView.visibility = View.GONE
        RecyclerVieww.visibility=View.GONE
        // Obtener la referencia al archivo de imagen en el storage
        val storageRefere = FirebaseStorage.getInstance().getReference()
            .child("images/users/" + otherUser + "/profile.png")

        // Descargar la imagen y mostrarla en el ImageView
        storageRefere.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            profilePictureImageView.setImageBitmap(bmp)
            LoadingLetter.visibility = View.GONE
            LoadingImage.visibility = View.GONE
            profilePictureImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            profilePictureImageView.visibility = View.VISIBLE
            RecyclerVieww.visibility=View.VISIBLE
            relativeLayout.setBackgroundResource(R.color.colorPrimary)
            val profileLayout = findViewById<LinearLayout>(R.id.profileLayout)
            profileLayout.setBackgroundResource(R.color.colorPrimary)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
            }
        }.addOnFailureListener { exception ->
            // Manejar errores
        }


        // Ejemplo de cómo usar los widgets
        profilePictureImageView.setImageResource(R.drawable.fotosinperfil)
        nameTextView.text = nameOtherUser
        emailTextView.text = otherUser

        back_button.setOnClickListener {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("user", user)
            intent.putExtra("otherUser", otherUser)

            startActivity(intent)
            true
            finish()}


        val db = FirebaseFirestore.getInstance()

        val estadosRef = db.collection("Estados")
        val docRef = estadosRef.document(otherUser)

        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val estado = document.getString("estado")

                    statusTextView.text = estado
                } else {

                }
            }
            .addOnFailureListener { exception ->


            }

                profilePictureImageView.setOnClickListener{
            val intent = Intent(this, FotoAmpliadaOtro::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("user", user)
            intent.putExtra("otherUser", otherUser)
            intent.putExtra("chat",1)
            startActivity(intent)
            true
            finish()}

        val storageReference = FirebaseStorage.getInstance().getReference()
        val folderRef = storageReference.child("fotosperfil/" + otherUser)
        folderRef.listAll()
            .addOnSuccessListener { listResult ->
                val imageRefs = listResult.items

                // Recorre todas las referencias de imagen y obtén sus URL
                val downloadUrls: MutableList<String> = mutableListOf()
                val downloadTasks = mutableListOf<Task<Uri>>()

                for (imageRef in imageRefs) {
                    val downloadTask = imageRef.downloadUrl
                    downloadTasks.add(downloadTask)
                }

                Tasks.whenAllSuccess<Uri>(downloadTasks)
                    .addOnSuccessListener { uriList ->
                        // Agrega todas las URL de descarga a la lista
                        for (uri in uriList) {
                            downloadUrls.add(uri.toString())
                        }


                        val adapter = HorizontalImageAdapter(downloadUrls, this, object : OnItemClickListener {
                            override fun OnItemClick(vista: View, position: Int) {
                                val urlCompleta = downloadUrls[position]
                                val intent = Intent(this@PerfilActivity, FotoAmpliadaOtro::class.java)
                                intent.putExtra("url", urlCompleta)
                                intent.putExtra("chatId", chatId)
                                intent.putExtra("user", user)
                                intent.putExtra("otherUser", otherUser)
                                intent.putExtra("chat",1)
                                intent.putExtra("imagenPerfiles",1)
                                startActivity(intent)
                                finish()
                            }
                        })


                        // Crea el LinearLayoutManager y configúralo en el RecyclerView
                        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
                        val horizontalRecyclerView: RecyclerView = findViewById(R.id.horizontalRecyclerView)
                        horizontalRecyclerView.layoutManager = layoutManager

                        // Configura el adaptador en el RecyclerView
                        horizontalRecyclerView.adapter = adapter

                    }
                    .addOnFailureListener { exception ->
                        // Maneja la excepción en caso de error
                    }
            }
            .addOnFailureListener { exception ->
                // Maneja la excepción en caso de error
            }

    }


                private fun obtenerNombreFoto(url: String): String {
                    val nombreArchivo = url.substringAfterLast("/") // Obtener la parte de la URL después de la última barra "/"
                    val nombreFoto = nombreArchivo.substringBeforeLast(".") // Obtener la parte antes del último punto ".", que sería el nombre de la foto sin la extensión
                    return nombreFoto
                }
    override fun onBackPressed() {
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("user", user)
        intent.putExtra("otherUser", otherUser)
        startActivity(intent)
        true
        finish()}
}
