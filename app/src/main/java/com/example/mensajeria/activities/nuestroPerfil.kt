package com.example.mensajeria.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mensajeria.R
import com.example.mensajeria.adapters.ChatAdapter
import com.example.mensajeria.adapters.HorizontalImageAdapter
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_list_of_chats.*
import kotlinx.android.synthetic.main.activity_login.*
import kotlinx.android.synthetic.main.nuestroperfil.*
import kotlinx.android.synthetic.main.perfil.*
import java.io.ByteArrayOutputStream
import java.io.File

private lateinit var imageView: ImageView
private var user = ""
private var userName = ""
class nuestroPerfil : AppCompatActivity() {
    private lateinit var LoadingImage: ImageView
    private lateinit var LoadingLetter: ImageView
    var MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 0
    val IMAGE_REQUEST_CODE = 1_000;
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.nuestroperfil)


        // Instanciar los widgets
        val profilePictureImageView = findViewById<ImageView>(R.id.profilePictureImageView2)
        val nameTextView = findViewById<TextView>(R.id.nameTextView2)
        val emailTextView = findViewById<TextView>(R.id.emailTextView2)
        val statusTextView = findViewById<TextView>(R.id.statusEditText2)
        val RecyclerVieww = findViewById<RecyclerView>(R.id.horizontalRecyclerView2)
        val guardarButton = findViewById<Button>(R.id.Guardar)
        val guardarFotos = findViewById<Button>(R.id.addPhotosButton)
        val relativeLayout = findViewById<RelativeLayout>(R.id.relativeLayout2)
        imageView = findViewById(R.id.imageView24)

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
                    // La información se guardo exitosamente en Firestore
                    Toast.makeText(this, "Estado guardado correctamente", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { exception ->
                    // Ocurrió un error al guardar la informacion en Firestore
                    Toast.makeText(this, "Error al guardar el estado", Toast.LENGTH_SHORT).show()
                }
        }

        LoadingImage = findViewById(R.id.loading_image_view2)
        LoadingLetter = findViewById(R.id.loading_letter_view2)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.statusBarColor = ContextCompat.getColor(this, R.color.negro)
        }
        profilePictureImageView.visibility = View.GONE
        statusTextView.visibility = View.GONE
        emailTextView.visibility=View.GONE
        nameTextView.visibility=View.GONE
        RecyclerVieww.visibility=View.GONE
        guardarButton.visibility=View.GONE
        guardarFotos.visibility=View.GONE
        // Obtener la referencia al archivo de imagen en el storage
        val storageRefere = FirebaseStorage.getInstance().getReference()
            .child("images/users/" + user + "/profile.png")
        // Descargar la imagen y mostrarla en el ImageView mientras no tenga la imagen muestro una imagen de carga
        storageRefere.getBytes(Long.MAX_VALUE).addOnSuccessListener { bytes ->
            val bmp = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
            profilePictureImageView.setImageBitmap(bmp)
            LoadingLetter.visibility = View.GONE
            LoadingImage.visibility = View.GONE
            profilePictureImageView.scaleType = ImageView.ScaleType.CENTER_CROP
            profilePictureImageView.visibility = View.VISIBLE
            relativeLayout.setBackgroundResource(R.color.colorPrimary)
            guardarButton.visibility=View.VISIBLE
            nameTextView.visibility=View.VISIBLE
            statusTextView.visibility = View.VISIBLE
            emailTextView.visibility = View.VISIBLE
            RecyclerVieww.visibility=View.VISIBLE
            guardarButton.visibility=View.VISIBLE
            guardarFotos.visibility=View.VISIBLE
            val profileLayout = findViewById<LinearLayout>(R.id.profileLayout2)
            profileLayout.setBackgroundResource(R.color.colorPrimary)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.statusBarColor = ContextCompat.getColor(this, R.color.colorPrimary)
            }
        }.addOnFailureListener { exception ->
            // Manejar errores
        }

        //volver hacia atras
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


        //ampliar imagen
        profilePictureImageView.setOnClickListener{
            val intent = Intent(this, FotoPerfilAmpliada::class.java)
            intent.putExtra("user", user)
            intent.putExtra("username", userName)
            startActivity(intent)
            true
            finish()}



        val storageReference = FirebaseStorage.getInstance().getReference()
        val folderRef = storageReference.child("fotosperfil/" + user)
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

                        // Crea el adaptador
                        val adapter = HorizontalImageAdapter(downloadUrls, this, object : OnItemClickListener {
                            override fun OnItemClick(vista: View, position: Int) {
                                val urlCompleta = downloadUrls[position]
                                val intent = Intent(this@nuestroPerfil, fotosPerfilesAmpliada::class.java)
                                intent.putExtra("url", urlCompleta)
                                intent.putExtra("user", user)
                                startActivity(intent)
                                finish()
                            }
                        })

                        // Crea el LinearLayoutManager y configúralo en el RecyclerView
                        val layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

                        RecyclerVieww.layoutManager = layoutManager

                        // Configura el adaptador en el RecyclerView
                        RecyclerVieww.adapter = adapter



        addPhotosButton.setOnClickListener {  if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
                )
            ) {
                // Explicar al usuario por qué se necesita el permiso y luego solicitarlo
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

    }
            }
            .addOnFailureListener { exception ->
                // Maneja la excepción en caso de error
            }
    }



    private fun pickImageFromGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        startActivityForResult(intent, IMAGE_REQUEST_CODE)
    }
    private fun uploadImageToFirebaseStorage(bitmap: Bitmap,dato : String) {

        // Convertir el Bitmap a un arreglo de bytes
        val baos = ByteArrayOutputStream()
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
        val data = baos.toByteArray()

        // Subir imagen a Storage
        val storageRefes = Firebase.storage.reference.child("fotosperfil/$user/" + dato)
        val uploadTask = storageRefes.putBytes(data)
        uploadTask.addOnSuccessListener {
            storageRefes.downloadUrl.addOnSuccessListener { uri ->
                val intent = Intent(this, nuestroPerfil::class.java)
                intent.putExtra("user", user)
                intent.putExtra("username", userName)
                startActivity(intent)
                true
                finish()
            }.addOnFailureListener { exception ->
            }
        }.addOnFailureListener { exception ->
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, resultData: Intent?) {
        super.onActivityResult(requestCode, resultCode, resultData)
        if (requestCode == IMAGE_REQUEST_CODE && resultCode == RESULT_OK) {
            resultData?.data?.also { uri ->
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE
                    )
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    // Permiso denegado, muestra un mensaje o realiza alguna acción apropiada
                } else {
                    val options = BitmapFactory.Options()
                    val bitmap = BitmapFactory.decodeStream(
                        contentResolver.openInputStream(uri),
                        null,
                        options
                    )

                    // Establecer la imagen en el ImageView con el ScaleType correspondiente
                    imageView.scaleType = ImageView.ScaleType.CENTER_CROP
                    imageView.setImageBitmap(bitmap)

                    // Obtener el Bitmap de la imagen escalada en el ImageView
                    imageView.isDrawingCacheEnabled = true
                    imageView.buildDrawingCache()
                    val scaledBitmap = imageView.drawingCache
                    val fileName = File(uri.path).name
                    // Subir la imagen a Firebase Storage
                    if (scaledBitmap != null) {
                        uploadImageToFirebaseStorage(scaledBitmap, fileName)
                    }
                }
            }
        }
    }



    override fun onBackPressed() {

        val intent = Intent(this, ListOfChatsActivity::class.java)
        intent.putExtra("user", user)
        startActivity(intent)
        finish()
    }
}
