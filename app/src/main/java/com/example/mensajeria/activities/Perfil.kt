package com.example.mensajeria.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mensajeria.R
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
        intent.getStringExtra("otherUser")?.let { otherUser = it }
        intent.getStringExtra("nameOtherUser")?.let { nameOtherUser = it }
        intent.getStringExtra("chatId")?.let { chatId = it }
        intent.getStringExtra("user")?.let { user = it }



        LoadingImage = findViewById(R.id.loading_image_view)
        LoadingLetter = findViewById(R.id.loading_letter_view)
        window.statusBarColor = ContextCompat.getColor(this, R.color.negro)
        profilePictureImageView.visibility = View.GONE
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
            val relativeLayout = findViewById<RelativeLayout>(R.id.RelativeLayout)
            relativeLayout.setBackgroundResource(R.color.colorPrimary)
            val profileLayout = findViewById<LinearLayout>(R.id.profileLayout)
            profileLayout.setBackgroundResource(R.color.colorPrimary)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
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
            startActivity(intent)
            true
            finish()}


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
