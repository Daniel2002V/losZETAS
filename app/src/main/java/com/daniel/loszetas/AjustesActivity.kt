package com.daniel.loszetas

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.daniel.loszetas.databinding.ActivityAjustesBinding
import com.daniel.loszetas.utils.ConfiguracionApp

class AjustesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAjustesBinding
    private lateinit var preferences: SharedPreferences

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
        // ✅ Moneda - Click para cambiar
        binding.cardMoneda.setOnClickListener {
            mostrarSelectorMoneda()
        }

        // ❌ IDIOMA ELIMINADO - Ya no existe

        // Gestionar cuenta - Abrir nueva pantalla
        binding.btnGestionarCuenta.setOnClickListener {
            startActivity(Intent(this, GestionarCuentaActivity::class.java))
        }
    }

    private fun mostrarSelectorMoneda() {
        val monedas = ConfiguracionApp.MONEDAS_DISPONIBLES
        val opciones = monedas.map { "${it.nombre} (${it.codigo})" }.toTypedArray()

        val monedaActual = ConfiguracionApp.obtenerMoneda(this)
        val seleccionada = monedas.indexOfFirst { it.codigo == monedaActual.codigo }

        AlertDialog.Builder(this)
            .setTitle("Seleccionar moneda")
            .setSingleChoiceItems(opciones, seleccionada) { dialog, which ->
                val moneda = monedas[which]
                ConfiguracionApp.guardarMoneda(this, moneda.codigo)
                cargarPreferencias()
                Toast.makeText(
                    this,
                    "Moneda cambiada a ${moneda.nombre}",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun cargarPreferencias() {
        // Cargar moneda actual
        val moneda = ConfiguracionApp.obtenerMoneda(this)
        binding.tvMoneda.text = "${moneda.nombre} (${moneda.codigo})"
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

    override fun onResume() {
        super.onResume()
        cargarPreferencias()
    }
}