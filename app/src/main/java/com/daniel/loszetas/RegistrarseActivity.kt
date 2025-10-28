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


class RegistrarseActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        auth = Firebase.auth

        val editNombre = findViewById<EditText>(R.id.edit_nombre)
        val editCorreo = findViewById<EditText>(R.id.edit_correo)
        val editPass = findViewById<EditText>(R.id.edit_pass)
        val editMoneda = findViewById<EditText>(R.id.edit_moneda)
        val buttonRegistrarse = findViewById<Button>(R.id.btn_registrarse)

        buttonRegistrarse.setOnClickListener {
            val nombre = editNombre.text.toString().trim()
            val correo = editCorreo.text.toString().trim()
            val pass = editPass.text.toString().trim()
            val moneda = editMoneda.text.toString().trim()

            when {
                nombre.isEmpty() -> {
                    Toast.makeText(this, "Debe ingresar su nombre", Toast.LENGTH_SHORT).show()
                }
                correo.isEmpty() -> {
                    Toast.makeText(this, "Debe ingresar un correo", Toast.LENGTH_SHORT).show()
                }
                pass.length < 8 -> {
                    Toast.makeText(
                        this,
                        "La contraseña debe tener al menos 8 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                moneda.isEmpty() -> {
                    Toast.makeText(this, "Debe ingresar una moneda", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    crearUsuario(correo, pass, nombre, moneda)
                }
            }
        }
    }

    private fun crearUsuario(correo: String, pass: String, nombre: String, moneda: String) {
        auth.createUserWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(
                        this,
                        "Cuenta creada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()
                    // Aquí puedes guardar nombre y moneda en Firestore si lo necesitas
                    irALogin()
                } else {
                    Toast.makeText(
                        this,
                        "Error al crear la cuenta: ${task.exception?.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}