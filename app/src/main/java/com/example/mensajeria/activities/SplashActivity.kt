package com.example.mensajeria.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.example.mensajeria.R

//Pantalla que sale durante unos pocos segundos al iniciar la pagina
class SplashActivity : AppCompatActivity() {
    @RequiresApi(Build.VERSION_CODES.LOLLIPOP)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.green)
        setTheme(R.style.SplashTheme)
        Handler().postDelayed({
            val intentLogin = Intent(this, LoginActivity::class.java)
            intentLogin.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intentLogin)
            finish()
        }, 3000)
    }
}



