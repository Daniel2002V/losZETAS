package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daniel.loszetas.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        binding.btnLogin.setOnClickListener {
            val correo = binding.editCorreo.text.toString().trim()
            val pass = binding.editPass.text.toString().trim()

            if (correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(this, "Debe ingresar correo y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                loginValidation(correo, pass)
            }
        }
    }

    private fun loginValidation(correo: String, pass: String) {
        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    irAPantallaPrincipal()
                } else {
                    Toast.makeText(
                        this,
                        "No pudimos iniciar sesión con ese usuario y contraseña",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun irAPantallaPrincipal() {
        startActivity(Intent(this, PantallaPrincipalActivity::class.java))
        finish()
    }
}