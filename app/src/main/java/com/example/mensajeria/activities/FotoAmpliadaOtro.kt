package com.example.mensajeria.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isInvisible
import com.example.mensajeria.R
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import java.io.ByteArrayOutputStream

class FotoAmpliadaOtro : AppCompatActivity() {
    private var chatId = ""
    private var otherUser = ""
    private var url = ""
    private var chat = 100
    private var imagenPerfiles = 100
    private lateinit var imageView: ImageView
    private var user = ""


    private lateinit var LoadingImage: ImageView
    private lateinit var LoadingLetter: ImageView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.foto_ampliada)
        intent.getStringExtra("user")?.let { user = it }
        intent.getStringExtra("chatId")?.let { chatId = it }
        intent.getStringExtra("otherUser")?.let { otherUser = it }
        intent.getIntExtra("chat",100).let { chat = it }
        intent.getIntExtra("imagenPerfiles",100).let { imagenPerfiles = it }
        intent.getStringExtra("url")?.let { url = it }

        // Configurar el toolbar
        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        val botonPerfil = findViewById<Button>(R.id.buttonPerfil)
        val titulo = findViewById<TextView>(R.id.textView2)
        setSupportActionBar(toolbar)
        toolbar.setBackgroundColor(ContextCompat.getColor(this, android.R.color.black))
        actionBar?.setBackgroundDrawable(ColorDrawable(resources.getColor(R.color.negro)))
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.negro)
        }

        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.title = ""
        val backButton = findViewById<ImageButton>(R.id.back_button)

        botonPerfil.isInvisible=true
        System.out.println(chat)
       backButton.setOnClickListener {

           if (chat == 0) {
               val intent = Intent(this, ChatActivity::class.java)
               intent.putExtra("chatId", chatId)
               intent.putExtra("user", user)
               intent.putExtra("otherUser", otherUser)
               startActivity(intent)

               true
               finish()
           }else if (chat==1){
               val intent = Intent(this, PerfilActivity::class.java)
               intent.putExtra("chatId", chatId)
               intent.putExtra("user", user)
               intent.putExtra("otherUser", otherUser)
               startActivity(intent)

           }
       }

        val fullText = otherUser
        val atIndex = fullText.lastIndexOf("@")
        if (atIndex != -1) {
            val username = fullText.substring(0, atIndex)
            titulo.text = username
        }
        titulo.textSize = 20f // 20sp
        val params = titulo.layoutParams as ConstraintLayout.LayoutParams
        params.marginStart = resources.getDimensionPixelSize(R.dimen.toolbar_title_margin_start)
        titulo.layoutParams = params


        // Obtener la referencia al ImageView
        imageView = findViewById(R.id.image_view)
        LoadingImage = findViewById(R.id.loading_image_view)
        LoadingLetter = findViewById(R.id.loading_letter_view)
        imageView.visibility = View.GONE
        // Obtener la referencia al archivo de imagen en el storage
        if(imagenPerfiles==100) {val storageRef = FirebaseStorage.getInstance().getReference()
            .child("images/users/" + otherUser + "/profile.png")
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bmp)
                LoadingLetter.visibility = View.GONE
                LoadingImage.visibility = View.GONE
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.visibility = View.VISIBLE
            }.addOnFailureListener { exception ->
                // Manejar errores
            }

        }else if (imagenPerfiles==1){
            val imageUrl = url
            val storageRef = Firebase.storage.getReferenceFromUrl(imageUrl)
            storageRef.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
                val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                imageView.setImageBitmap(bmp)
                LoadingLetter.visibility = View.GONE
                LoadingImage.visibility = View.GONE
                imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                imageView.visibility = View.VISIBLE
            }.addOnFailureListener { exception ->
                // Manejar errores
            }
        }
            }

        //depende desde donde haya ampliado la imagen
    override fun onBackPressed() {
        if (chat == 0) {
            val intent = Intent(this, ChatActivity::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("user", user)
            intent.putExtra("otherUser", otherUser)
            startActivity(intent)

            true
            finish()
        }else if (chat==1){
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("user", user)
            intent.putExtra("otherUser", otherUser)
            startActivity(intent)

        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Manejar eventos del toolbar
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }



}


