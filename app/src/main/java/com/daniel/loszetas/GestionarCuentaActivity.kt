package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daniel.loszetas.databinding.ActivityGestionarCuentaBinding
import com.google.firebase.auth.FirebaseAuth

class GestionarCuentaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityGestionarCuentaBinding
    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityGestionarCuentaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarToolbar()
        cargarInformacion()
        configurarBotones()
    }

    private fun configurarToolbar() {
        binding.btnVolver.setOnClickListener {
            finish()
        }
    }

    private fun cargarInformacion() {
        val usuario = auth.currentUser

        with(binding) {
            // Email
            tvEmailCuenta.text = usuario?.email ?: "No disponible"

            // UID (ID de usuario)
            tvUidCuenta.text = usuario?.uid ?: "No disponible"

            // Fecha de creación de la cuenta
            val fechaCreacion = usuario?.metadata?.creationTimestamp
            if (fechaCreacion != null) {
                val fecha = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
                    .format(java.util.Date(fechaCreacion))
                tvFechaCreacion.text = fecha
            } else {
                tvFechaCreacion.text = "No disponible"
            }
        }
    }

    private fun configurarBotones() {
        with(binding) {
            // Cambiar contraseña - FUNCIONAL con Firebase
            btnCambiarContrasena.setOnClickListener {
                cambiarContrasena()
            }

            // Cerrar sesión - FUNCIONAL con Firebase
            btnCerrarSesion.setOnClickListener {
                cerrarSesion()
            }

            // Eliminar cuenta - FUNCIONAL con Firebase
            btnEliminarCuenta.setOnClickListener {
                confirmarEliminarCuenta()
            }
        }
    }

    /**
     * FUNCIONAL - Envía email de recuperación de contraseña usando Firebase
     */
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

    /**
     * FUNCIONAL - Cierra sesión con Firebase y va a MainActivity (login/registro)
     */
    private fun cerrarSesion() {
        AlertDialog.Builder(this)
            .setTitle("Cerrar sesión")
            .setMessage("¿Estás seguro de que deseas cerrar sesión?")
            .setPositiveButton("Sí") { _, _ ->
                // Cerrar sesión en Firebase
                auth.signOut()

                // Ir a MainActivity (pantalla de login/registro)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setNegativeButton("No", null)
            .show()
    }

    /**
     * Confirma antes de eliminar cuenta
     */
    private fun confirmarEliminarCuenta() {
        AlertDialog.Builder(this)
            .setTitle("Eliminar cuenta")
            .setMessage("⚠️ ADVERTENCIA: Esta acción es irreversible.\n\n¿Estás seguro de que deseas eliminar tu cuenta permanentemente?\n\nSe eliminarán todos tus datos.")
            .setPositiveButton("Eliminar") { _, _ ->
                eliminarCuenta()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    /**
     * FUNCIONAL - Elimina la cuenta de Firebase
     * NOTA: Si el usuario lleva mucho tiempo sin iniciar sesión, Firebase pedirá que vuelva a autenticarse
     */
    private fun eliminarCuenta() {
        val usuario = auth.currentUser

        usuario?.delete()
            ?.addOnSuccessListener {
                Toast.makeText(this, "Cuenta eliminada correctamente", Toast.LENGTH_SHORT).show()

                // Ir a MainActivity (pantalla de login/registro)
                val intent = Intent(this, MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            ?.addOnFailureListener { e ->
                AlertDialog.Builder(this)
                    .setTitle("Error")
                    .setMessage("No se pudo eliminar la cuenta: ${e.message}\n\nPuede que necesites iniciar sesión nuevamente para realizar esta acción.")
                    .setPositiveButton("Aceptar", null)
                    .show()
            }
    }
}