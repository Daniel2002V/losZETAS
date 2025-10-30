package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.daniel.loszetas.databinding.ActivityMainBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        // Usar binding en lugar de findViewById
        binding.btnRegistrarse.setOnClickListener {
            startActivity(Intent(this, RegistrarseActivity::class.java))
        }

        binding.btnIniciarSesion.setOnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
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
        startActivity(Intent(this, PantallaPrincipalActivity::class.java))
        finish()
    }
}