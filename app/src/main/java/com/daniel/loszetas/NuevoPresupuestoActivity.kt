package com.daniel.loszetas

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.data.entities.Presupuesto
import com.daniel.loszetas.databinding.ActivityNuevoPresupuestoBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NuevoPresupuestoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoPresupuestoBinding
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private var fechaInicioMillis: Long = System.currentTimeMillis()

    private val categorias = listOf("Alimentación", "Transporte", "Entretenimiento", "Servicios")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNuevoPresupuestoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarSpinners()
        configurarBotones()
        actualizarMontoInicial()
    }

    private fun configurarSpinners() {
        // Configurar categorías
        val categoriaChips = listOf(
            binding.chipAlimentacion,
            binding.chipTransporte,
            binding.chipEntretenimiento,
            binding.chipServicios
        )

        var categoriaSeleccionada = ""

        categoriaChips.forEachIndexed { index, chip ->
            chip.setOnClickListener {
                categoriaChips.forEach { it.isChecked = false }
                chip.isChecked = true
                categoriaSeleccionada = categorias[index]
                calcularDistribucion()
            }
        }

        // Periodo
        val periodos = listOf("Mensual", "Semanal", "Anual")
        val periodoAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodos)
        periodoAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
    }

    private fun configurarBotones() {
        binding.btnCerrar.setOnClickListener {
            finish()
        }

        binding.tvMesInicio.text = dateFormat.format(Date(fechaInicioMillis))

        binding.btnCrearPresupuesto.setOnClickListener {
            guardarPresupuesto()
        }

        binding.btnAtras.setOnClickListener {
            finish()
        }
    }

    private fun actualizarMontoInicial() {
        binding.etLimiteTotal.setText("$1,200.00")    }

    private fun calcularDistribucion() {
        // Lógica para calcular distribución basada en categorías seleccionadas
        binding.tvDistribucion.text = "$1,000 / $1,200"
        binding.tvRestante.text = "$200"
    }

    private fun guardarPresupuesto() {
        val limiteStr = binding.etLimiteTotal.text.toString().trim()

        // Obtener categoría seleccionada
        val categoriaSeleccionada = when {
            binding.chipAlimentacion.isChecked -> "Alimentación"
            binding.chipTransporte.isChecked -> "Transporte"
            binding.chipEntretenimiento.isChecked -> "Entretenimiento"
            binding.chipServicios.isChecked -> "Servicios"
            else -> ""
        }

        if (categoriaSeleccionada.isEmpty()) {
            Toast.makeText(this, "Selecciona una categoría", Toast.LENGTH_SHORT).show()
            return
        }

        if (limiteStr.isEmpty()) {
            Toast.makeText(this, "Ingresa un límite total", Toast.LENGTH_SHORT).show()
            return
        }

        val limiteTotal = limiteStr.toDoubleOrNull() ?: 0.0

        val nuevoPresupuesto = Presupuesto(
            categoria = categoriaSeleccionada,
            limiteTotal = limiteTotal,
            periodo = "MENSUAL",
            fechaInicio = fechaInicioMillis
        )

        lifecycleScope.launch {
            try {
                database.presupuestoDao().insertar(nuevoPresupuesto)
                Toast.makeText(this@NuevoPresupuestoActivity, "Presupuesto creado exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@NuevoPresupuestoActivity, "Error al crear presupuesto: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}