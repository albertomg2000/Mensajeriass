package com.example.mensajeria.activities
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.os.Bundle
import android.util.Patterns
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.mensajeria.R
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_login.*
import java.io.ByteArrayOutputStream

//inicio de sesion o creacion de usuario
class LoginActivity : AppCompatActivity() {

    private val auth = Firebase.auth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        }
        loginButton.setOnClickListener { loginUser() }
        createButton.setOnClickListener { createUser() }
        //para cuando no te acuerdas de la contrasena
        forgotPasswordText.setOnClickListener {
                val intent = Intent(this, Restablecimiento::class.java)
                startActivity(intent)
                true
        }

        checkUser()
    }
    //vemos si existe el usuario
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
            if(task.isSuccessful) {

                val bitmap = BitmapFactory.decodeResource(resources, R.drawable.fotosinperfil)
                val baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
                val data = baos.toByteArray()

                // Subir imagen a Storage
                //subo una imagen por defecto a su perfil, luego sera modificable
                val storageRef = Firebase.storage.reference.child("images/users/$email/profile.png")
                val uploadTask = storageRef.putBytes(data)

                uploadTask.addOnSuccessListener {
                    storageRef.downloadUrl.addOnSuccessListener { uri ->
                        checkUser()
                    }.addOnFailureListener { exception ->
                    }
                }.addOnFailureListener { exception ->
                }

                val db = FirebaseFirestore.getInstance()
                //creamos un estado predeterminado
                val estadosRef = db.collection("Estados")
                val docRef = estadosRef.document(email)
                val estadoMap = hashMapOf("estado" to "Hey there im using Whatsapp!!")

                docRef.set(estadoMap)
                    .addOnSuccessListener {
                        // La información se guardó exitosamente en Firestore
                    }
                    .addOnFailureListener { exception ->
                        // Ocurrió un error al guardar la información en Firestore
                    }
            }
        }
    }


    private fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        return password.length >= 6
    }
    //check de si el usuario y contrasena son correctas
    private fun loginUser() {
        val email = emailText.text.toString()
        val password = passwordText.text.toString()
        if (email.length > 1) {
            if (email.isNotBlank() && isValidEmail(email)) {
                if (password.isNotBlank() && isPasswordValid(password)) {
                    auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            checkUser()
                        } else {
                            val exception = task.exception
                            if (exception is FirebaseAuthInvalidUserException) {
                                Toast.makeText(baseContext, "No existe un usuario con ese correo electrónico", Toast.LENGTH_LONG).show()
                            } else {
                                Toast.makeText(baseContext, "Contraseña incorrecta", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                } else {
                    Toast.makeText(baseContext, "Contraseña inválida (debe tener al menos 6 caracteres)", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(baseContext, "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
            }
        } else {
            Toast.makeText(baseContext, "Correo electrónico demasiado corto", Toast.LENGTH_SHORT).show()
        }
    }

}