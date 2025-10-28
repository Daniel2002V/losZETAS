package com.daniel.loszetas

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pantalla_principal)

        auth = Firebase.auth

        val usuario = auth.currentUser
        Toast.makeText(
            this,
            "Bienvenido ${usuario?.email}",
            Toast.LENGTH_SHORT
        ).show()

        // Botón de cerrar sesión (opcional)
        val btnCerrarSesion = findViewById<Button>(R.id.btn_cerrar_sesion)
        btnCerrarSesion?.setOnClickListener {
            auth.signOut()
            finish()
        }
    }
}