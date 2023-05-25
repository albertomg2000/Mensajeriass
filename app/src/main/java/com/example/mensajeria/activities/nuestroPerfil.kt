package com.example.mensajeria.activities

import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mensajeria.R
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.nuestroperfil.*
import kotlinx.android.synthetic.main.perfil.*

private var user = ""
private var userName = ""
class nuestroPerfil : AppCompatActivity() {
    private lateinit var LoadingImage: ImageView
    private lateinit var LoadingLetter: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.nuestroperfil)


        // Instanciar los widgets
        val profilePictureImageView = findViewById<ImageView>(R.id.profilePictureImageView2)
        val nameTextView = findViewById<TextView>(R.id.nameTextView2)
        val emailTextView = findViewById<TextView>(R.id.emailTextView2)
        val statusTextView = findViewById<TextView>(R.id.statusEditText2)



        intent.getStringExtra("username")?.let { userName = it }
        intent.getStringExtra("user")?.let { user = it }

        // Ejemplo de cómo usar los widgets
        profilePictureImageView.setImageResource(R.drawable.fotosinperfil)
        nameTextView.text = userName
        emailTextView.text = user

        val dbEstado = FirebaseFirestore.getInstance()
        val estadosRefe = dbEstado.collection("Estados")
        val docRefe = estadosRefe.document(user)

// Obtener referencia al botón
        val guardarButton = findViewById<Button>(R.id.Guardar)

        guardarButton.setOnClickListener {
            // Obtener el estado del TextView
            val estado = statusTextView.text.toString()

            // Crear un mapa con el campo "estado"
            val data = hashMapOf(
                "estado" to estado
            )

            // Actualizar el documento en Firestore
            docRefe.set(data)
                .addOnSuccessListener {
                    // La información se guardó exitosamente en Firestore
                    Toast.makeText(this, "Estado guardado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    // Ocurrió un error al guardar la información en Firestore
                    // Manejar el error apropiadamente
                    Toast.makeText(this, "Error al guardar el estado", Toast.LENGTH_SHORT).show()
                }
        }

        LoadingImage = findViewById(R.id.loading_image_view2)
        LoadingLetter = findViewById(R.id.loading_letter_view2)
        window.statusBarColor = ContextCompat.getColor(this, R.color.negro)
        profilePictureImageView.visibility = View.GONE
        statusTextView.visibility = View.GONE
        emailTextView.visibility=View.GONE
        nameTextView.visibility=View.GONE
        // Obtener la referencia al archivo de imagen en el storage
        val storageRefere = FirebaseStorage.getInstance().getReference()
            .child("images/users/" + user + "/profile.png")

        // Descargar la imagen y mostrarla en el ImageView
        storageRefere.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            profilePictureImageView.setImageBitmap(bmp)
            LoadingLetter.visibility = View.GONE
            LoadingImage.visibility = View.GONE

            profilePictureImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            profilePictureImageView.visibility = View.VISIBLE
            val relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout2)
            relativeLayout.setBackgroundResource(R.color.colorPrimary)
            guardarButton.visibility=View.VISIBLE
            nameTextView.visibility=View.VISIBLE
            statusTextView.visibility = View.VISIBLE
            emailTextView.visibility = View.VISIBLE
            val profileLayout = findViewById<LinearLayout>(R.id.profileLayout2)
            profileLayout.setBackgroundResource(R.color.colorPrimary)
            window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
        }.addOnFailureListener { exception ->
            // Manejar errores
        }


        back_button2.setOnClickListener {
            val intent = Intent(this, ListOfChatsActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
            true
            finish()}

        val storageRef = FirebaseStorage.getInstance().getReference()
            .child("images/users/" + user + "/profile.png")

        // Descargar la imagen y mostrarla en el ImageView
        storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            profilePictureImageView.setImageBitmap(bmp)
            profilePictureImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            profilePictureImageView.visibility = View.VISIBLE
        }.addOnFailureListener { exception ->
            // Manejar errores
        }
        val db = FirebaseFirestore.getInstance()

        val estadosRef = db.collection("Estados")
        val docRef = estadosRef.document(user)

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
            val intent = Intent(this, FotoPerfilAmpliada::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
            true
            finish()}


    }

    override fun onBackPressed() {

        val intent = Intent(this, ListOfChatsActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }
}
