package com.example.mensajeria.activities
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mensajeria.R
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream

class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        loginButton.setOnClickListener { loginUser() }
        createButton.setOnClickListener { createUser() }
        forgotPasswordText.setOnClickListener {
                val intent = Intent(this, Restablecimiento::class.java)
                startActivity(intent)
                true
        }

        checkUser()
    }

    private fun checkUser(){
        val currentUser = auth.currentUser

        if(currentUser != null){
            val intent = Intent(this, ListOfChatsActivity::class.java)
            intent.putExtra("user", currentUser.email)
            startActivity(intent)

            finish()
        }
    }

    private fun createUser() {
        val email = emailText.text.toString()
        val password = passwordText.text.toString()
        if(!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(baseContext, "Correo electrónico inválido", Toast.LENGTH_LONG).show()
            return
        }

        if (email.length > 40) {
            Toast.makeText(baseContext, "Correo electrónico demasiado extenso", Toast.LENGTH_LONG).show()
            return
        }

        if (password.length < 6) {
            Toast.makeText(baseContext, "Contraseña demasiado corta", Toast.LENGTH_LONG).show()
            return
        }

        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                print(email.length)
                val currentUser = auth.currentUser
                val userId = currentUser?.uid

                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.fotosinoperfilredim)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val data = baos.toByteArray()

                // Subir imagen a Storage
                val storageRef = Firebase.storage.reference.child("images/users/$email/profile.png")
                val uploadTask = storageRef.putBytes(data)

                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        checkUser()
                    }.addOnFailureListener { exception ->
                    }
                }.addOnFailureListener { exception ->
                }

            } else {
                task.exception?.let {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_LONG).show()
                }
            }
        }

    }







    private fun loginUser(){
        val email = emailText.text.toString()
        val password = passwordText.text.toString()

        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if(task.isSuccessful){
                checkUser()
            } else {
                task.exception?.let {
                    Toast.makeText(baseContext, it.message, Toast.LENGTH_LONG).show()
                }
            }

        }
    }
}