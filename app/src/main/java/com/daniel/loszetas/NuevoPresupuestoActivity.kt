package com.daniel.loszetas

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.data.entities.Presupuesto
import com.daniel.loszetas.databinding.ActivityNuevoPresupuestoBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NuevoPresupuestoActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevoPresupuestoBinding
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val dateFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
    private var fechaInicioMillis: Long = System.currentTimeMillis()

    private var periodoSeleccionado = "MENSUAL"
    private val categoriasSeleccionadas = mutableSetOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNuevoPresupuestoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarVistas()
        cargarCategoriasDesdeDB()
        configurarBotones()
    }

    private fun inicializarVistas() {
        // Inicializar campos vacíos
        binding.etLimiteTotal.setText("")
        binding.etCuentaSeguimiento.setText("")

        // Configurar fecha inicial
        binding.tvMesInicio.text = dateFormat.format(Date(fechaInicioMillis))

        // Configurar selector de fecha
        binding.tvMesInicio.setOnClickListener {
            mostrarSelectorFecha()
        }

        // Inicializar barra de progreso en 0
        binding.tvDistribucion.text = "$0.00 / $0.00"
        binding.tvRestante.text = "Restante $0.00"
    }

    private fun mostrarSelectorFecha() {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = fechaInicioMillis

        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, _ ->
                calendar.set(year, month, 1)
                fechaInicioMillis = calendar.timeInMillis
                binding.tvMesInicio.text = dateFormat.format(Date(fechaInicioMillis))
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun cargarCategoriasDesdeDB() {
        lifecycleScope.launch {
            database.categoriaDao().obtenerCategoriasGasto().collect { categorias ->
                binding.chipGroupCategorias.removeAllViews()

                categorias.forEach { categoria ->
                    val chip = Chip(this@NuevoPresupuestoActivity).apply {
                        text = categoria.nombre
                        isCheckable = true
                        setChipBackgroundColorResource(android.R.color.white)
                        setTextColor(getColor(R.color.purple_500))
                        chipStrokeWidth = 2f
                        chipStrokeColor = getColorStateList(R.color.purple_500)

                        setOnCheckedChangeListener { _, isChecked ->
                            if (isChecked) {
                                categoriasSeleccionadas.add(categoria.nombre)
                                setChipBackgroundColorResource(R.color.purple_100)
                            } else {
                                categoriasSeleccionadas.remove(categoria.nombre)
                                setChipBackgroundColorResource(android.R.color.white)
                            }
                            actualizarDistribucion()
                        }
                    }
                    binding.chipGroupCategorias.addView(chip)
                }
            }
        }
    }

    private fun configurarBotones() {
        // Botón cerrar
        binding.btnCerrar.setOnClickListener {
            finish()
        }

        // Botones de período
        binding.btnMensualPeriodo.setOnClickListener {
            seleccionarPeriodo("MENSUAL")
        }

        binding.btnSemanal.setOnClickListener {
            seleccionarPeriodo("SEMANAL")
        }

        binding.btnAnual.setOnClickListener {
            seleccionarPeriodo("ANUAL")
        }

        // Botón atrás
        binding.btnAtras.setOnClickListener {
            finish()
        }

        // Botón crear presupuesto
        binding.btnCrearPresupuesto.setOnClickListener {
            guardarPresupuesto()
        }

        // Listener para actualizar distribución cuando cambie el límite
        binding.etLimiteTotal.setOnFocusChangeListener { _, hasFocus ->
            if (!hasFocus) {
                actualizarDistribucion()
            }
        }
    }

    private fun seleccionarPeriodo(periodo: String) {
        periodoSeleccionado = periodo

        // Actualizar UI de los botones
        val buttonPrimary = getColor(R.color.purple_500)
        val buttonTransparent = android.graphics.Color.TRANSPARENT
        val textWhite = android.graphics.Color.WHITE
        val textPurple = getColor(R.color.purple_500)

        when (periodo) {
            "MENSUAL" -> {
                binding.btnMensualPeriodo.setBackgroundColor(buttonPrimary)
                binding.btnMensualPeriodo.setTextColor(textWhite)
                binding.btnSemanal.setBackgroundColor(buttonTransparent)
                binding.btnSemanal.setTextColor(textPurple)
                binding.btnAnual.setBackgroundColor(buttonTransparent)
                binding.btnAnual.setTextColor(textPurple)
            }
            "SEMANAL" -> {
                binding.btnMensualPeriodo.setBackgroundColor(buttonTransparent)
                binding.btnMensualPeriodo.setTextColor(textPurple)
                binding.btnSemanal.setBackgroundColor(buttonPrimary)
                binding.btnSemanal.setTextColor(textWhite)
                binding.btnAnual.setBackgroundColor(buttonTransparent)
                binding.btnAnual.setTextColor(textPurple)
            }
            "ANUAL" -> {
                binding.btnMensualPeriodo.setBackgroundColor(buttonTransparent)
                binding.btnMensualPeriodo.setTextColor(textPurple)
                binding.btnSemanal.setBackgroundColor(buttonTransparent)
                binding.btnSemanal.setTextColor(textPurple)
                binding.btnAnual.setBackgroundColor(buttonPrimary)
                binding.btnAnual.setTextColor(textWhite)
            }
        }
    }

    private fun actualizarDistribucion() {
        val limiteStr = binding.etLimiteTotal.text.toString().trim()
        val limiteTotal = limiteStr.replace(",", "").replace("$", "").toDoubleOrNull() ?: 0.0

        if (categoriasSeleccionadas.isEmpty() || limiteTotal == 0.0) {
            binding.tvDistribucion.text = "$0.00 / $0.00"
            binding.tvRestante.text = "Restante $0.00"
            return
        }

        // Por ahora, simplemente mostramos el total
        // Puedes implementar lógica de distribución por categoría aquí
        val distribuido = 0.0
        val restante = limiteTotal - distribuido

        binding.tvDistribucion.text = String.format("$%.2f / $%.2f", distribuido, limiteTotal)
        binding.tvRestante.text = String.format("Restante $%.2f", restante)
    }

    private fun guardarPresupuesto() {
        val limiteStr = binding.etLimiteTotal.text.toString().trim()

        // Validar categorías seleccionadas
        if (categoriasSeleccionadas.isEmpty()) {
            Toast.makeText(this, "Selecciona al menos una categoría", Toast.LENGTH_SHORT).show()
            return
        }

        // Validar límite
        if (limiteStr.isEmpty()) {
            Toast.makeText(this, "Ingresa un límite total", Toast.LENGTH_SHORT).show()
            return
        }

        val limiteTotal = limiteStr.replace(",", "").replace("$", "").toDoubleOrNull()
        if (limiteTotal == null || limiteTotal <= 0) {
            Toast.makeText(this, "Ingresa un límite válido mayor a 0", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear un presupuesto por cada categoría seleccionada
        lifecycleScope.launch {
            try {
                categoriasSeleccionadas.forEach { categoria ->
                    val nuevoPresupuesto = Presupuesto(
                        categoria = categoria,
                        limiteTotal = limiteTotal,
                        gastoActual = 0.0,
                        periodo = periodoSeleccionado,
                        fechaInicio = fechaInicioMillis
                    )
                    database.presupuestoDao().insertar(nuevoPresupuesto)
                }

                Toast.makeText(
                    this@NuevoPresupuestoActivity,
                    "Presupuesto(s) creado(s) exitosamente",
                    Toast.LENGTH_SHORT
                ).show()

                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@NuevoPresupuestoActivity,
                    "Error al crear presupuesto: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}