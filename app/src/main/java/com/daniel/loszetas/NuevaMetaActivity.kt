package com.daniel.loszetas

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.data.entities.Meta
import com.daniel.loszetas.databinding.ActivityNuevaMetaBinding
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class NuevaMetaActivity : AppCompatActivity() {

    private lateinit var binding: ActivityNuevaMetaBinding
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private var fechaObjetivoMillis: Long = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityNuevaMetaBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarBotones()
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

        val montoObjetivo = montoObjetivoStr.toDoubleOrNull() ?: 0.0
        val montoInicial = montoInicialStr.toDoubleOrNull() ?: 0.0

        val nuevaMeta = Meta(
            nombre = nombre,
            montoObjetivo = montoObjetivo,
            montoActual = montoInicial,
            fechaObjetivo = fechaObjetivoMillis
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