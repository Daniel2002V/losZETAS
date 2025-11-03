package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class RegistrarseActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "RegistrarseActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_registrarse)

        auth = Firebase.auth
        Log.d(TAG, "RegistrarseActivity iniciada")

        val editNombre = findViewById<EditText>(R.id.edit_nombre)
        val editCorreo = findViewById<EditText>(R.id.edit_correo)
        val editPass = findViewById<EditText>(R.id.edit_pass)
        val editMoneda = findViewById<EditText>(R.id.edit_moneda)
        val buttonRegistrarse = findViewById<Button>(R.id.btn_registrarse)

        buttonRegistrarse.setOnClickListener {
            Log.d(TAG, "Botón registrarse presionado")

            val nombre = editNombre.text.toString().trim()
            val correo = editCorreo.text.toString().trim()
            val pass = editPass.text.toString().trim()
            val moneda = editMoneda.text.toString().trim()

            Log.d(TAG, "Datos ingresados - Nombre: $nombre, Email: $correo, Moneda: $moneda")

            when {
                nombre.isEmpty() -> {
                    Log.w(TAG, "Nombre vacío")
                    Toast.makeText(this, "Debe ingresar su nombre", Toast.LENGTH_SHORT).show()
                }
                correo.isEmpty() -> {
                    Log.w(TAG, "Correo vacío")
                    Toast.makeText(this, "Debe ingresar un correo", Toast.LENGTH_SHORT).show()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(correo).matches() -> {
                    Log.w(TAG, "Formato de correo inválido: $correo")
                    Toast.makeText(this, "El formato del correo es inválido", Toast.LENGTH_SHORT).show()
                }
                pass.length < 8 -> {
                    Log.w(TAG, "Contraseña muy corta: ${pass.length} caracteres")
                    Toast.makeText(
                        this,
                        "La contraseña debe tener al menos 8 caracteres",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                moneda.isEmpty() -> {
                    Log.w(TAG, "Moneda vacía")
                    Toast.makeText(this, "Debe ingresar una moneda", Toast.LENGTH_SHORT).show()
                }
                else -> {
                    Log.d(TAG, "Validación exitosa, procediendo a crear usuario")
                    crearUsuario(correo, pass, nombre, moneda)
                }
            }
        }
    }

    private fun crearUsuario(correo: String, pass: String, nombre: String, moneda: String) {
        Log.d(TAG, "Iniciando creación de usuario en Firebase Auth...")
        Log.d(TAG, "Email: $correo")

        auth.createUserWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Usuario creado exitosamente en Firebase Auth")
                    val user = auth.currentUser
                    Log.d(TAG, "UID del nuevo usuario: ${user?.uid}")
                    Log.d(TAG, "Email: ${user?.email}")

                    Toast.makeText(
                        this,
                        "Cuenta creada exitosamente",
                        Toast.LENGTH_SHORT
                    ).show()

                    // TODO: Aquí puedes guardar nombre y moneda en Firestore
                    // Por ejemplo:
                    // guardarDatosEnFirestore(user?.uid, nombre, moneda)

                    Log.d(TAG, "Navegando a LoginActivity")
                    irALogin()
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    val errorCode = task.exception?.javaClass?.simpleName

                    Log.e(TAG, "❌ Error al crear usuario")
                    Log.e(TAG, "Código de error: $errorCode")
                    Log.e(TAG, "Mensaje: $errorMessage")

                    // Mensajes más específicos para el usuario
                    val userMessage = when {
                        errorMessage.contains("email address is already in use", ignoreCase = true) ->
                            "Este correo ya está registrado. Intenta iniciar sesión"
                        errorMessage.contains("email address is badly formatted", ignoreCase = true) ->
                            "El formato del correo es inválido"
                        errorMessage.contains("password is invalid", ignoreCase = true) ||
                                errorMessage.contains("password should be at least", ignoreCase = true) ->
                            "La contraseña debe tener al menos 6 caracteres"
                        errorMessage.contains("network error", ignoreCase = true) ->
                            "Error de conexión. Verifica tu internet"
                        else ->
                            "Error: $errorMessage"
                    }

                    Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { exception ->
                Log.e(TAG, "❌ Fallo completo en la operación", exception)
                Toast.makeText(
                    this,
                    "Fallo crítico: ${exception.localizedMessage}",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    private fun irALogin() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }

    // Función opcional para guardar datos adicionales en Firestore
    /*
    private fun guardarDatosEnFirestore(uid: String?, nombre: String, moneda: String) {
        if (uid == null) return

        val db = Firebase.firestore
        val userData = hashMapOf(
            "nombre" to nombre,
            "moneda" to moneda,
            "fechaCreacion" to FieldValue.serverTimestamp()
        )

        db.collection("usuarios").document(uid)
            .set(userData)
            .addOnSuccessListener {
                Log.d(TAG, "✅ Datos guardados en Firestore")
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "❌ Error al guardar en Firestore", e)
            }
    }
    */
}