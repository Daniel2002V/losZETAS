package com.daniel.loszetas

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.data.entities.Meta
import com.daniel.loszetas.databinding.ActivityNuevaMetaBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class NuevaMetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevaMetaBinding
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var fechaObjetivoMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNuevaMetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        inicializarCampos()
        configurarBotones()
        configurarListeners()
    }

    private fun inicializarCampos() {
        // Limpiar todos los campos
        binding.etNombreMeta.setText("")
        binding.etMontoObjetivo.setText("")
        binding.etMontoInicial.setText("")
        binding.etFechaObjetivo.setText("")
        binding.etFrecuencia.setText("")
        binding.etCuentaAsociada.setText("")

        // Inicializar progreso en 0
        binding.progressBarMeta.progress = 0
        binding.tvProgresoEstimado.text = "$0.00 de $0.00 ~ 0 meses"
    }

    private fun configurarListeners() {
        // Listener para actualizar el progreso cuando cambien los valores
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                calcularProgreso()
            }
        }

        binding.etMontoObjetivo.addTextChangedListener(textWatcher)
        binding.etMontoInicial.addTextChangedListener(textWatcher)
    }

    private fun calcularProgreso() {
        val montoObjetivoStr = binding.etMontoObjetivo.text.toString().trim()
        val montoInicialStr = binding.etMontoInicial.text.toString().trim()

        val montoObjetivo = montoObjetivoStr.replace(",", "").replace("$", "").toDoubleOrNull() ?: 0.0
        val montoInicial = montoInicialStr.replace(",", "").replace("$", "").toDoubleOrNull() ?: 0.0

        if (montoObjetivo > 0) {
            val porcentaje = ((montoInicial / montoObjetivo) * 100).toInt().coerceIn(0, 100)
            binding.progressBarMeta.progress = porcentaje

            // Calcular meses estimados
            val mesesEstimados = if (fechaObjetivoMillis > 0) {
                val diff = fechaObjetivoMillis - System.currentTimeMillis()
                TimeUnit.MILLISECONDS.toDays(diff) / 30
            } else {
                0L
            }

            binding.tvProgresoEstimado.text = String.format(
                "$%.2f de $%.2f ~ %d meses",
                montoInicial,
                montoObjetivo,
                mesesEstimados
            )
        } else {
            binding.progressBarMeta.progress = 0
            binding.tvProgresoEstimado.text = "$0.00 de $0.00 ~ 0 meses"
        }
    }

    private fun configurarBotones() {
        binding.btnCerrar.setOnClickListener {
            finish()
        }

        binding.etFechaObjetivo.setOnClickListener {
            mostrarDatePicker()
        }

        binding.btnCrearMeta.setOnClickListener {
            guardarMeta()
        }

        binding.btnAtras.setOnClickListener {
            finish()
        }
    }

    private fun mostrarDatePicker() {
        val calendar = Calendar.getInstance()

        val datePicker = DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                fechaObjetivoMillis = calendar.timeInMillis
                binding.etFechaObjetivo.setText(dateFormat.format(Date(fechaObjetivoMillis)))
                calcularProgreso() // Actualizar el cálculo de meses
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePicker.datePicker.minDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun guardarMeta() {
        val nombre = binding.etNombreMeta.text.toString().trim()
        val montoObjetivoStr = binding.etMontoObjetivo.text.toString().trim()
        val montoInicialStr = binding.etMontoInicial.text.toString().trim()

        if (nombre.isEmpty()) {
            Toast.makeText(this, "Ingresa un nombre para la meta", Toast.LENGTH_SHORT).show()
            return
        }

        if (montoObjetivoStr.isEmpty()) {
            Toast.makeText(this, "Ingresa el monto objetivo", Toast.LENGTH_SHORT).show()
            return
        }

        if (fechaObjetivoMillis == 0L) {
            Toast.makeText(this, "Selecciona una fecha objetivo", Toast.LENGTH_SHORT).show()
            return
        }

        val montoObjetivo = montoObjetivoStr.replace(",", "").replace("$", "").toDoubleOrNull()
        if (montoObjetivo == null || montoObjetivo <= 0) {
            Toast.makeText(this, "Ingresa un monto objetivo válido", Toast.LENGTH_SHORT).show()
            return
        }

        val montoInicial = montoInicialStr.replace(",", "").replace("$", "").toDoubleOrNull() ?: 0.0

        if (montoInicial > montoObjetivo) {
            Toast.makeText(this, "El monto inicial no puede ser mayor al objetivo", Toast.LENGTH_SHORT).show()
            return
        }

        val nuevaMeta = Meta(
            nombre = nombre,
            montoObjetivo = montoObjetivo,
            montoActual = montoInicial,
            fechaObjetivo = fechaObjetivoMillis,
            completada = false
        )

        lifecycleScope.launch {
            try {
                database.metaDao().insertar(nuevaMeta)
                Toast.makeText(this@NuevaMetaActivity, "Meta creada exitosamente", Toast.LENGTH_SHORT).show()
                finish()
            } catch (e: Exception) {
                Toast.makeText(this@NuevaMetaActivity, "Error al crear meta: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}