package com.example.mensajeria.activities
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//maneja los mensajes que se envian a firebase
class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Guardar el token en la base de datos
        saveTokenToDatabase(token)
    }
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
    }
    fun saveTokenToDatabase(token: String) {
        val user = FirebaseAuth.getInstance().currentUser
        val userEmail = user?.email
        Log.d("TOKEN", "Token: $token")
        if (userEmail != null) {
            val db = FirebaseFirestore.getInstance()
            val userRef = db.collection("Perfiles").document(userEmail)
            userRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        if (documentSnapshot.contains("token")) {
                            // El campo "token" ya existe, actualizar su valor
                            userRef.update("token", token)
                                .addOnSuccessListener {
                                    Log.d("TOKEN", "Token actualizado exitosamente")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("TOKEN", "Error al actualizar el token", exception)
                                }
                        } else {
                            // El campo "token" no existe, crearlo
                            userRef.set(mapOf("token" to token), SetOptions.merge())
                                .addOnSuccessListener {
                                    Log.d("TOKEN", "Token creado exitosamente")
                                }
                                .addOnFailureListener { exception ->
                                    Log.e("TOKEN", "Error al crear el token", exception)
                                }
                        }
                    } else {
                        Log.e("TOKEN", "Documento no encontrado para el usuario: $userEmail")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.e("TOKEN", "Error al obtener el documento del usuario: $userEmail", exception)
                }
        } else {
            Log.e("TOKEN", "No se pudo obtener el correo del usuario actual")
        }
    }
}
