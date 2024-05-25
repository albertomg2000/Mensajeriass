package com.example.mensajeria.activities

import android.Manifest
import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mensajeria.R
import com.example.mensajeria.adapters.MessageAdapter
import com.example.mensajeria.models.Message
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.File
import java.io.FileOutputStream


//Activity del chat entre los dos usuarios
class ChatActivity : AppCompatActivity() {
    private var chatId = ""
    private var user = ""
    private var otherUser = ""
    private var username=""
    val IMAGE_REQUEST_CODE = 1_000;
    lateinit var storage: FirebaseStorage
    private lateinit var imageView: ImageView
    var MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0
    private var db = Firebase.firestore

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        val toolbar = findViewById<Toolbar>(R.id.custom_toolbar)
        val titulo = findViewById<TextView>(R.id.textView2)
        setSupportActionBar(toolbar)
        imageView = findViewById(R.id.imageeeView)
        intent.getStringExtra("chatId")?.let { chatId = it }
        intent.getStringExtra("user")?.let { user = it }
        intent.getStringExtra("otherUser")?.let { otherUser = it }

        // Configura la barra de herramientas personalizada para crear mi propio toolbar
        supportActionBar?.setDisplayShowTitleEnabled(false)
        val perfilother = findViewById<ImageButton>(R.id.perfilother)
        perfilother.setOnClickListener{
            val intent = Intent(this, FotoAmpliadaOtro::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("user", user)
            intent.putExtra("otherUser", otherUser)
            intent.putExtra("chat",0)
            startActivity(intent)
            true
            finish()}

        // Obtener los parámetros de diseño del título
        titulo.visibility= View.INVISIBLE
        val storageRef = FirebaseStorage.getInstance().getReference().child("images/users/$otherUser/profile.png")
        val fullText = otherUser
        val atIndex = fullText.lastIndexOf("@")
        if (atIndex != -1) {

            username = fullText.substring(0, atIndex)
            teeeext.text = username
        }

        teeeext.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            intent.putExtra("otherUser", otherUser)
            intent.putExtra("nameOtherUser", username)
            intent.putExtra("chatId", chatId)
            intent.putExtra("user", user)
            startActivity(intent)
            finish()
            true

        }
        //Para poner la foto del perfil en el toolbar
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
            val foto = findViewById<ImageButton>(R.id.perfilother)
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size, options)
            val drawable = RoundedBitmapDrawableFactory.create(resources, bmp)
            //la pongo en un pequeno circulo
            drawable.isCircular = true
            foto.setImageDrawable(drawable)
            foto.background = resources.getDrawable(R.drawable.rounded_image)
        }.addOnFailureListener { exception ->
        }
        //Compruebo que exista el chat y el usuario, tambien compruebo si tengo algun fondo de pantalla en mi aplicacion, sino pongo uno por defecto
        if (chatId.isNotEmpty() && user.isNotEmpty()) {
            val file = File("/data/data/com.example.mensajeria/files/fondo.png")
            val exists = file.exists()
            if (exists==true) {
                val drawablePath = "/data/data/com.example.mensajeria/files/fondo.png"
                val bitmap = BitmapFactory.decodeFile(drawablePath)
                val metrics = DisplayMetrics()
                windowManager.defaultDisplay.getMetrics(metrics)
                var width = metrics.widthPixels // ancho absoluto en pixels
                val height = metrics.heightPixels
                val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)
                val drawable = BitmapDrawable(resources, resizedBitmap)
                drawable.gravity = Gravity.FILL
                ex.background = drawable
            }else{
            }
            initViews()

        }
        messageTextField.setOnFocusChangeListener { view, hasFocus ->
        }
        storage = Firebase.storage
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initViews() {
        //Para volver al activity anterior en caso de que le de al back
        backButton.setOnClickListener {
            val intent = Intent(this, ListOfChatsActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
            finish()
        }
        //Configuracion del TextField donde escribe los mensajes
        var fild = findViewById<EditText>(R.id.messageTextField)
        fild.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Obtener número de líneas de texto
                val lineCount = fild.lineCount
                // Obtener altura de una línea de texto
                val lineHeight = fild.lineHeight
                // Calcular nueva altura del EditText
                val newHeight = lineHeight * lineCount + 60
                fild.maxLines=10
                fild.maxHeight=400
                if (fild.height != newHeight) {
                    if (newHeight<=400){
                        fild.layoutParams.height = newHeight
                        fild.requestLayout()
                    }
                }
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            }
        })

        //Cuando envio el mensaje me dirigirá al último mensaje enviado, es necesario porque sino al ir al chat
        //Vere el primer mensaje de la conversacion, lo ideal seria ver desde el ultimo
        messagesRecylerView.layoutManager = LinearLayoutManager(this)
        messagesRecylerView.adapter = MessageAdapter(user)

        sendMessageButton.setOnClickListener {
            messagesRecylerView.adapter?.let {
                messagesRecylerView.smoothScrollToPosition(it.itemCount)
            }
            sendMessage()
        }
        //recoger mensajes
        val chatRef = db.collection("chats").document(chatId)
        //dob es uncampo de fecha, me lo ordena por la fecha, cojo 25 mensajes para que no haya un exceso de mensajes y tarde en cargar
        chatRef.collection("messages").orderBy("dob", Query.Direction.DESCENDING)
            .limit(25)
            .get()
            .addOnSuccessListener { messages ->
                val listMessages = messages.toObjects(Message::class.java)
                var listaOrdenada= listMessages.reversed()
                (messagesRecylerView.adapter as MessageAdapter).setData(listaOrdenada)
                messagesRecylerView.adapter?.let {
                    messagesRecylerView.smoothScrollToPosition(it.itemCount)
                }
                chatRef.collection("messages").orderBy("dob", Query.Direction.DESCENDING).limit(25)
                    .addSnapshotListener { messages, error ->
                        if (error == null) {
                            messages?.let {
                                val listMessages = it.toObjects(Message::class.java)
                                var listaOrdenada= listMessages.reversed()
                                (messagesRecylerView.adapter as MessageAdapter).setData(listaOrdenada)
                                messagesRecylerView.adapter?.let {
                                    messagesRecylerView.smoothScrollToPosition(it.itemCount)
                                }
                            }
                        }
                    }
            }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    //envio de mensajes
    private fun sendMessage() {
        val messageText = messageTextField.text.toString()
        //comprobacion de mensaje no vacio
        if (messageText.isNotEmpty()) {
            val message = Message(
                message = messageText,
                from = user
            )

            // Guardar el mensaje en la base de datos
            val chatRef = db.collection("chats").document(chatId)
            val newMessageRef = chatRef.collection("messages").document()
            newMessageRef.set(message)
                .addOnSuccessListener {
                    // Envío exitoso del mensaje, ahora obtener y guardar el token de Firebase Messaging
                    getFirebaseMessagingToken { token ->
                        // Envío exitoso del mensaje, ahora invocar la clase MyFirebaseMessagingService
                        val firebaseMessagingService = MyFirebaseMessagingService()
                        firebaseMessagingService.saveTokenToDatabase(token)

                        // Envío exitoso del mensaje, ahora enviar la notificación al destinatario
                        sendNotificationToRecipient(token)

                        // Limpiar el campo de texto al enviar el mensaje
                        messageTextField.setText("")
                    }
                }
                .addOnFailureListener { exception ->
                    // Error al enviar el mensaje
                    Toast.makeText(this, "Error al enviar el mensaje", Toast.LENGTH_SHORT).show()
                }
        } else {
            Toast.makeText(this, "El mensaje está vacío", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    //metodo para enviar la notificacion en forma de mensaje
    private fun sendNotificationToRecipient(recipientToken: String) {
        // Configurar el título y cuerpo de la notificación
        val title = "Nuevo mensaje"
        val body = "Has recibido un nuevo mensaje"

        // Crear el canal de notificación (requerido para versiones de Android >= Oreo)
        val channelId = "chat_channel"
        val channelName = "Chat Channel"
        val importance = NotificationManager.IMPORTANCE_HIGH

        val notificationChannel = NotificationChannel(channelId, channelName, importance)
        notificationChannel.description = "Notificaciones de chat"
        notificationChannel.enableLights(true)
        notificationChannel.lightColor = Color.RED
        notificationChannel.enableVibration(true)

        // Registrar el canal de notificación en el sistema
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(notificationChannel)

        // Crear el intent para abrir la actividad ChatActivity al hacer clic en la notificación
        val intent = Intent(this, ChatActivity::class.java)
        intent.putExtra("chatId", chatId)
        intent.putExtra("user", user)
        intent.putExtra("otherUser", otherUser)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        // Crear y configurar la notificación
        val notificationBuilder = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.what)
            .setContentTitle(title)
            .setContentText(body)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        // Mostrar la notificación al destinatario
        val notificationId = System.currentTimeMillis().toInt()
        notificationManager.notify(notificationId, notificationBuilder.build())
    }

    private fun getFirebaseMessagingToken(callback: (String) -> Unit) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                if (token != null) {
                    callback.invoke(token)
                } else {
                    Log.e("TOKEN", "El token obtenido es nulo")
                }
            } else {
                Log.e("TOKEN", "Error al obtener el token: ${task.exception}")
            }
        }
    }

    //en el toolbar hay un menu con una opcion para cambiar el fondo de nuestros chats como wasap
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.Cambiarfondo == R.id.Cambiarfondo) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
                != PackageManager.PERMISSION_GRANTED
            ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                ) {
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                        MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                    )
                }
            } else {
                pickImageFromGallery()
            }

        }
        return true
    }
    //elijo la imagen que quiero poner de fondo del chat
    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            resultData?.data?.also { uri ->
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(
                            this,
                            Manifest.permission.WRITE_EXTERNAL_STORAGE
                        )
                    ) {
                    } else {
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE
                        )
                    }
                } else {
                    //esta es la ruta donde se va a guardar el fondo elegido para mi chat, cuando elija otro
                    //el nuevo ira a esta ruta y reemplazara al existente
                    val drawablePath = "/data/data/com.example.mensajeria/files/fondo.png"
                    val inputStream = contentResolver.openInputStream(uri)
                    val outputStream = FileOutputStream(drawablePath)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    val bitmap = BitmapFactory.decodeFile(drawablePath)

                    val metrics = DisplayMetrics()
                    windowManager.defaultDisplay.getMetrics(metrics)
                    var width = metrics.widthPixels // ancho absoluto en pixels
                    val height = metrics.heightPixels
                    val resizedBitmap = Bitmap.createScaledBitmap(bitmap, width, height, false)

                    val drawable = BitmapDrawable(resources, resizedBitmap)
                    drawable.gravity = Gravity.FILL
                    ex.background = drawable

                }
            }
        }
    }
    //le doy un tamaño adecuado
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
    //Para volver hacia atras al activity de lista de chats
    override fun onBackPressed() {
        val intent = Intent(this, ListOfChatsActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }

}









