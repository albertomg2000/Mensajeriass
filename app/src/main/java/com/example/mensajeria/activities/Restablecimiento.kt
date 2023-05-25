package com.example.mensajeria.activities
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import com.example.mensajeria.R
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth

class Restablecimiento : AppCompatActivity() {

    private lateinit var emailEditText: EditText
    private lateinit var resetButton: Button
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.restablecimiento)

        emailEditText = findViewById(R.id.reset_password_email)
        resetButton = findViewById(R.id.reset_password_button)
        auth = FirebaseAuth.getInstance()
        resetButton.setOnClickListener {

            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                emailEditText.error = "Por favor, ingresa tu correo electrónico."
                emailEditText.requestFocus()
                return@setOnClickListener
            }
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Se ha enviado un correo para restablecer tu contraseña.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    } else {
                        Snackbar.make(
                            findViewById(android.R.id.content),
                            "Lo sentimos, no hemos encontrado una cuenta asociada a este correo electrónico.",
                            Snackbar.LENGTH_LONG
                        ).show()
                    }
                }
        }
    }
}