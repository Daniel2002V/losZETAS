package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth


class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = Firebase.auth

        val editCorreo = findViewById<EditText>(R.id.edit_correo)
        val editPass = findViewById<EditText>(R.id.edit_pass)
        val buttonLogin = findViewById<Button>(R.id.btn_login)

        buttonLogin.setOnClickListener {
            val correo = editCorreo.text.toString().trim()
            val pass = editPass.text.toString().trim()

            if (correo.isEmpty() || pass.isEmpty()) {
                Toast.makeText(
                    this,
                    "Debe ingresar correo y contraseña",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                loginValidation(correo, pass)
            }
        }
    }

    private fun loginValidation(correo: String, pass: String) {
        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Inicio de sesión exitoso",
                        Toast.LENGTH_SHORT
                    ).show()
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
        val intent = Intent(this, PantallaPrincipalActivity::class.java)
        startActivity(intent)
        finish()
    }
}