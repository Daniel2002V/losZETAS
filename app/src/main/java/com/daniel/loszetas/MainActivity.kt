package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        auth = Firebase.auth

        val buttonRegistrarse = findViewById<Button>(R.id.btn_registrarse)
        val buttonIniciarSesion = findViewById<Button>(R.id.btn_iniciar_sesion)

        buttonRegistrarse.setOnClickListener {
            val intent = Intent(this, RegistrarseActivity::class.java)
            startActivity(intent)
        }

        buttonIniciarSesion.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        // Verificar si ya hay un usuario logueado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            irAPantallaPrincipal()
        }
    }

    private fun irAPantallaPrincipal() {
        val intent = Intent(this, PantallaPrincipalActivity::class.java)
        startActivity(intent)
        finish()
    }
}