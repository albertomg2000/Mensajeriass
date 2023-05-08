package com.example.mensajeria.activities

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mensajeria.R
import com.example.mensajeria.adapters.MessageAdapter
import com.example.mensajeria.models.Message
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import de.hdodenhof.circleimageview.CircleImageView
import kotlinx.android.synthetic.main.activity_chat.*
import java.io.File
import java.io.FileOutputStream



class ChatActivity : AppCompatActivity() {
    private var chatId = ""
    private var user = ""
    private var otherUser = ""
    val IMAGE_REQUEST_CODE = 1_000;
    lateinit var storage: FirebaseStorage
    var isTextFieldActive = false
    private lateinit var imageView: ImageView

    var MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0
    private var db = Firebase.firestore

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

        supportActionBar?.setDisplayShowTitleEnabled(false)
        val fullText = otherUser
        val atIndex = fullText.lastIndexOf("@")

        val perfilother = findViewById<CircleImageView>(R.id.perfilother)

        perfilother.setOnClickListener{
            val intent = Intent(this, FotoAmpliadaOtro::class.java)
            intent.putExtra("chatId", chatId)
            intent.putExtra("user", user)
            intent.putExtra("otherUser", otherUser)
            startActivity(intent)
        true
        finish()}

        if (atIndex != -1) {

            val username = fullText.substring(0, atIndex)
            titulo.text = username
        }
        titulo.textSize = 20f // 20sp

        val params = titulo.layoutParams as ConstraintLayout.LayoutParams
        params.marginStart = resources.getDimensionPixelSize(R.dimen.toolbar_title_margin_start)+ 250

        titulo.layoutParams = params

        val foto = findViewById<CircleImageView>(R.id.perfilother)

        val storageRef = FirebaseStorage.getInstance().getReference().child("images/users/$otherUser/profile.png")


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


        val myButton: Button = findViewById(com.example.mensajeria.R.id.imagenEnviar)
        myButton.setOnClickListener {
            //uploadImageToFirebaseStorage()
        }

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
                    imagenDeFuera()
                }
        messageTextField.setOnFocusChangeListener { view, hasFocus ->

        }
        storage = Firebase.storage
    }

    private fun initViews() {
        backButton.setOnClickListener {
            val intent = Intent(this, ListOfChatsActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
            finish()
        }
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
                // No se utiliza en este caso
            }
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // No se utiliza en este caso
            }
        })

        messagesRecylerView.layoutManager = LinearLayoutManager(this)
        messagesRecylerView.adapter = MessageAdapter(user)

        sendMessageButton.setOnClickListener {
            messagesRecylerView.adapter?.let {
                messagesRecylerView.smoothScrollToPosition(it.itemCount)
            }
            sendMessage()

        }

        val chatRef = db.collection("chats").document(chatId)

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
    private fun sendMessage() {
        val message = Message(
            message = messageTextField.text.toString(),
            from = user
        )

        if (message.message.length <= 0) {
        } else {
            db.collection("chats").document(chatId).collection("messages").document().set(message)
            db.collection("users").document(user).collection("chats").document(chatId).collection("messages").document().set(message)
            println(chatId);
            print("CHAT ID *****************---------------------------------------*3234243*")
        }
        messageTextField.setText("")

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_chat, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (R.id.Cambiarfondo == R.id.Cambiarfondo) {

            pickImageFromGallery()

        }
        return true
    }

    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }

    private fun imagenDeFuera() {
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
                    val drawablePath = "/data/data/com.example.mensajeria/files/fondo.png"
                    val inputStream = contentResolver.openInputStream(uri)
                    val outputStream = FileOutputStream(drawablePath)
                    inputStream?.copyTo(outputStream)
                    inputStream?.close()
                    outputStream.close()
                    val bitmap = BitmapFactory.decodeFile(drawablePath)

                    imageView.setImageBitmap(bitmap)
                    ex.background = imageView.drawable
                    ex.background.intrinsicHeight
                    imageView.isInvisible=true

                }
            }
        }
    }

    fun HahechoClick(view: View) {
    }

    private fun uploadImageToFirebaseStorage(uri: Uri) {
        val storageRef = storage.reference
        val imagesRef = storageRef.child("images/${uri.lastPathSegment}")
        val uploadTask = imagesRef.putFile(uri)

        uploadTask.continueWithTask { task ->
            if (!task.isSuccessful) {
                task.exception?.let {
                    throw it
                }
            }
            imagesRef.downloadUrl
        }.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val downloadUri = task.result.toString()
                val message = Message(
                    message = downloadUri,
                    from = user
                )
                db.collection("chats").document(chatId).collection("messages").document().set(message)
            }
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
}


