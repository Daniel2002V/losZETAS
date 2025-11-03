package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.daniel.loszetas.databinding.ActivityLoginBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth
        Log.d(TAG, "LoginActivity iniciada")

        binding.btnLogin.setOnClickListener {
            val correo = binding.editCorreo.text.toString().trim()
            val pass = binding.editPass.text.toString().trim()

            Log.d(TAG, "Botón login presionado")

            if (correo.isEmpty() || pass.isEmpty()) {
                Log.w(TAG, "Campos vacíos detectados")
                Toast.makeText(this, "Debe ingresar correo y contraseña", Toast.LENGTH_SHORT).show()
            } else {
                Log.d(TAG, "Iniciando validación de login para: $correo")
                loginValidation(correo, pass)
            }
        }
    }

    private fun loginValidation(correo: String, pass: String) {
        Log.d(TAG, "Intentando login con Firebase Auth...")

        auth.signInWithEmailAndPassword(correo, pass)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "✅ Login exitoso")
                    val user = auth.currentUser
                    Log.d(TAG, "Usuario UID: ${user?.uid}")
                    Log.d(TAG, "Email verificado: ${user?.isEmailVerified}")

                    Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                    irAPantallaPrincipal()
                } else {
                    val errorMessage = task.exception?.message ?: "Error desconocido"
                    val errorCode = task.exception?.javaClass?.simpleName

                    Log.e(TAG, "❌ Error en login")
                    Log.e(TAG, "Código de error: $errorCode")
                    Log.e(TAG, "Mensaje: $errorMessage")

                    // Mensajes más específicos para el usuario
                    val userMessage = when {
                        errorMessage.contains("no user record", ignoreCase = true) ->
                            "Este usuario no existe. ¿Necesitas registrarte?"
                        errorMessage.contains("password is invalid", ignoreCase = true) ->
                            "Contraseña incorrecta"
                        errorMessage.contains("email address is badly formatted", ignoreCase = true) ->
                            "El formato del correo es inválido"
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

    private fun irAPantallaPrincipal() {
        Log.d(TAG, "Navegando a PantallaPrincipalActivity")
        startActivity(Intent(this, PantallaPrincipalActivity::class.java))
        finish()
    }

    override fun onStart() {
        super.onStart()
        // Verificar si ya hay un usuario logueado
        val currentUser = auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "Usuario ya autenticado: ${currentUser.email}")
            // Opcional: ir directamente a la pantalla principal
            // irAPantallaPrincipal()
        }
    }
}