package com.daniel.loszetas

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.daniel.loszetas.databinding.ActivityAjustesBinding
import com.google.firebase.auth.FirebaseAuth

class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding
    private lateinit var preferences: SharedPreferences
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAjustesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        preferences = getSharedPreferences("app_preferences", MODE_PRIVATE)

        configurarOpciones()
        configurarNavegacion()
        cargarPreferencias()
    }

    private fun configurarOpciones() {
        // Tema
        binding.btnClaro.setOnClickListener {
            cambiarTema(AppCompatDelegate.MODE_NIGHT_NO, "Claro")
        }

        binding.btnOscuro.setOnClickListener {
            cambiarTema(AppCompatDelegate.MODE_NIGHT_YES, "Oscuro")
        }

        // Moneda (placeholder - ya está configurado como CLP)
        binding.tvMoneda.text = "CLP"

        // Idioma (placeholder - ya está en español)
        binding.tvIdioma.text = "Español"

        // Gestionar cuenta
        binding.btnGestionarCuenta.setOnClickListener {
            mostrarOpcionesCuenta()
        }
    }

    private fun cambiarTema(modo: Int, nombre: String) {
        // Guardar preferencia
        preferences.edit().putInt("tema", modo).apply()
        preferences.edit().putString("tema_nombre", nombre).apply()

        // Aplicar tema
        AppCompatDelegate.setDefaultNightMode(modo)

        // Actualizar UI
        actualizarSeleccionTema(nombre)

        Toast.makeText(this, "Tema cambiado a: $nombre", Toast.LENGTH_SHORT).show()
    }

    private fun actualizarSeleccionTema(temaNombre: String) {
        // Resetear todos los botones
        binding.btnClaro.setBackgroundResource(R.drawable.bg_button_outline)
        binding.btnClaro.setTextColor(getColor(R.color.purple_500))
        binding.btnOscuro.setBackgroundResource(R.drawable.bg_button_outline)
        binding.btnOscuro.setTextColor(getColor(R.color.purple_500))

        // Marcar el seleccionado
        when (temaNombre) {
            "Claro" -> {
                binding.btnClaro.setBackgroundResource(R.drawable.bg_button_primary)
                binding.btnClaro.setTextColor(getColor(android.R.color.white))
            }
            "Oscuro" -> {
                binding.btnOscuro.setBackgroundResource(R.drawable.bg_button_primary)
                binding.btnOscuro.setTextColor(getColor(android.R.color.white))
            }
        }
    }

    private fun cargarPreferencias() {
        // Cargar tema guardado - por defecto será Claro si no hay preferencia guardada
        val temaNombre = preferences.getString("tema_nombre", "Claro") ?: "Claro"
        actualizarSeleccionTema(temaNombre)
    }

    private fun mostrarOpcionesCuenta() {
        val usuario = auth.currentUser

        val opciones = arrayOf(
            "Ver información de cuenta",
            "Cambiar contraseña",
            "Cerrar sesión"
        )

        AlertDialog.Builder(this)
            .setTitle("Gestionar cuenta")
            .setItems(opciones) { _, which ->
                when (which) {
                    0 -> mostrarInfoCuenta(usuario?.email)
                    1 -> cambiarContrasena()
                    2 -> cerrarSesion()
                }
            }
            .show()
    }

    private fun mostrarInfoCuenta(email: String?) {
        AlertDialog.Builder(this)
            .setTitle("Información de cuenta")
            .setMessage("Email: ${email ?: "No disponible"}")
            .setPositiveButton("Aceptar", null)
            .show()
    }

    private fun cambiarContrasena() {
        val usuario = auth.currentUser

        if (usuario?.email != null) {
            auth.sendPasswordResetEmail(usuario.email!!)
                .addOnSuccessListener {
                    AlertDialog.Builder(this)
                        .setTitle("Email enviado")
                        .setMessage("Se ha enviado un correo a ${usuario.email} para restablecer tu contraseña.")
                        .setPositiveButton("Aceptar", null)
                        .show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Error al enviar email: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        } else {
            Toast.makeText(this, "No se pudo obtener el email del usuario", Toast.LENGTH_SHORT).show()
        }
    }

    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                auth.signOut()
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.selectedItemId = R.id.nav_ajustes

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    navegarA(PantallaPrincipalActivity::class.java)
                    true
                }
                R.id.nav_historial -> {
                    navegarA(HistorialActivity::class.java)
                    true
                }
                R.id.nav_estadisticas -> {
                    navegarA(EstadisticasActivity::class.java)
                    true
                }
                R.id.nav_presupuesto -> {
                    navegarA(PresupuestosMetasActivity::class.java)
                    true
                }
                R.id.nav_ajustes -> true
                else -> false
            }
        }
    }

    private fun navegarA(destino: Class<*>) {
        startActivity(Intent(this, destino))
        overridePendingTransition(0, 0)
        finish()
    }
}