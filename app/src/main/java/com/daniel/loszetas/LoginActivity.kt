package com.daniel.loszetas
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import android.os.PersistableBundle
import com.daniel.loszetas.databinding.ActivityLoginBinding


/**
 * Activity para el Login y Registro de usuarios
 * Usa Firebase Authentication para manejar la autenticación
 */
class LoginActivity : AppCompatActivity() {

    // ViewBinding: forma moderna y segura de acceder a las vistas
    // Evita el uso de findViewById()
    private lateinit var binding: ActivityLoginBinding

    // Instancia de Firebase Authentication
    // lateinit = se inicializará después, pero antes de usarse
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener la instancia de Firebase Authentication
        // Firebase.auth es un singleton (única instancia en toda la app)
        auth = Firebase.auth

        // Configurar listeners de los botones
        setupListeners()
    }

    /**
     * Configura los listeners de los botones
     */
    private fun setupListeners() {
        // Botón de Login
        binding.btnLogin.setOnClickListener {
            // Obtener texto de los campos
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            // Validar campos antes de intentar login
            if (validateInputs(email, password)) {
                loginUser(email, password)
            }
        }

        // Botón de Registro
        binding.btnRegister.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInputs(email, password)) {
                registerUser(email, password)
            }
        }
    }

    /**
     * Valida los campos de entrada
     * @return true si los campos son válidos, false en caso contrario
     */
    private fun validateInputs(email: String, password: String): Boolean {
        // Limpiar errores previos
        binding.tilEmail.error = null
        binding.tilPassword.error = null

        // Validar email vacío
        if (email.isEmpty()) {
            binding.tilEmail.error = "Ingresa tu correo electrónico"
            binding.etEmail.requestFocus()
            return false
        }

        // Validar formato de email usando Patterns de Android
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Ingresa un correo válido"
            binding.etEmail.requestFocus()
            return false
        }

        // Validar contraseña vacía
        if (password.isEmpty()) {
            binding.tilPassword.error = "Ingresa tu contraseña"
            binding.etPassword.requestFocus()
            return false
        }

        // Firebase requiere mínimo 6 caracteres
        if (password.length < 6) {
            binding.tilPassword.error = "La contraseña debe tener al menos 6 caracteres"
            binding.etPassword.requestFocus()
            return false
        }

        return true
    }

    /**
     * Inicia sesión con email y contraseña
     */
    private fun loginUser(email: String, password: String) {
        // Mostrar loading
        showLoading(true)

        // signInWithEmailAndPassword es una función asíncrona de Firebase
        // addOnCompleteListener se ejecuta cuando la operación termina
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                // Ocultar loading
                showLoading(false)

                // task.isSuccessful indica si el login fue exitoso
                if (task.isSuccessful) {
                    // Login exitoso
                    val user = auth.currentUser // Usuario autenticado
                    Toast.makeText(
                        this,
                        "¡Bienvenido ${user?.email}!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Ir a MainActivity
                    goToMainActivity()
                } else {
                    // Login falló
                    // task.exception contiene el error específico
                    val errorMessage = when {
                        task.exception?.message?.contains("no user record") == true ->
                            "No existe una cuenta con este correo"
                        task.exception?.message?.contains("password is invalid") == true ->
                            "Contraseña incorrecta"
                        task.exception?.message?.contains("network error") == true ->
                            "Error de conexión. Verifica tu internet"
                        else -> "Error: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Registra un nuevo usuario con email y contraseña
     */
    private fun registerUser(email: String, password: String) {
        showLoading(true)

        // createUserWithEmailAndPassword crea una nueva cuenta en Firebase
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                showLoading(false)

                if (task.isSuccessful) {
                    // Registro exitoso
                    Toast.makeText(
                        this,
                        "¡Cuenta creada exitosamente!",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Ir a MainActivity (ya está autenticado)
                    goToMainActivity()
                } else {
                    // Registro falló
                    val errorMessage = when {
                        task.exception?.message?.contains("already in use") == true ->
                            "Este correo ya está registrado"
                        task.exception?.message?.contains("weak password") == true ->
                            "La contraseña es muy débil"
                        else -> "Error al crear cuenta: ${task.exception?.message}"
                    }
                    Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Navega a la MainActivity y finaliza LoginActivity
     */
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // FLAG_ACTIVITY_NEW_TASK | FLAG_ACTIVITY_CLEAR_TASK
        // Limpia el back stack para que al presionar atrás no regrese al login
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    /**
     * Muestra/oculta el ProgressBar y deshabilita/habilita botones
     */
    private fun showLoading(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.btnRegister.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading
    }
}