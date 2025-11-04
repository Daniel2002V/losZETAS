package com.daniel.loszetas

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.databinding.ActivityEstadisticasBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

class EstadisticasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityEstadisticasBinding
    private lateinit var transaccionAdapter: TransaccionAdapter
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private var periodoSeleccionado = "MES"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityEstadisticasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarBotonesPeriodo()
        configurarNavegacion()
        cargarDatos()
    }

    private fun configurarRecyclerView() {
        transaccionAdapter = TransaccionAdapter()
        binding.rvEstadisticas.apply {
            layoutManager = LinearLayoutManager(this@EstadisticasActivity)
            adapter = transaccionAdapter
        }
    }

    private fun configurarBotonesPeriodo() {
        binding.btnMes.setOnClickListener {
            seleccionarPeriodo("MES")
        }

        binding.btnTrimestre.setOnClickListener {
            seleccionarPeriodo("TRIMESTRE")
        }

        binding.btnAno.setOnClickListener {
            seleccionarPeriodo("AÑO")
        }
    }

    private fun seleccionarPeriodo(periodo: String) {
        periodoSeleccionado = periodo

        with(binding) {
            btnMes.setBackgroundColor(Color.TRANSPARENT)
            btnTrimestre.setBackgroundColor(Color.TRANSPARENT)
            btnAno.setBackgroundColor(Color.TRANSPARENT)

            when (periodo) {
                "MES" -> btnMes.setBackgroundResource(R.drawable.bg_button_primary)
                "TRIMESTRE" -> btnTrimestre.setBackgroundResource(R.drawable.bg_button_primary)
                "AÑO" -> btnAno.setBackgroundResource(R.drawable.bg_button_primary)
            }
        }

        cargarDatos()
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.selectedItemId = R.id.nav_estadisticas

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
                R.id.nav_estadisticas -> true
                R.id.nav_presupuesto -> {
                    navegarA(PresupuestosMetasActivity::class.java)
                    true
                }
                R.id.nav_ajustes -> {
                    navegarA(AjustesActivity::class.java)
                    true
                }
                else -> false
            }
        }
    }

    private fun navegarA(destino: Class<*>) {
        startActivity(Intent(this, destino))
        overridePendingTransition(0, 0)
        finish()
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                database.transaccionDao().obtenerTodas().collect { transacciones ->
                    val transaccionesFiltradas = filtrarPorPeriodo(transacciones)

                    transaccionAdapter.actualizarTransacciones(transaccionesFiltradas)
                    calcularEstadisticas(transaccionesFiltradas)
                    calcularEstadisticasAvanzadas(transaccionesFiltradas)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(
                    this@EstadisticasActivity,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun filtrarPorPeriodo(transacciones: List<com.daniel.loszetas.data.entities.Transaccion>): List<com.daniel.loszetas.data.entities.Transaccion> {
        val calendar = Calendar.getInstance()

        return when (periodoSeleccionado) {
            "MES" -> {
                calendar.add(Calendar.MONTH, -1)
                transacciones.filter { it.fecha >= calendar.timeInMillis }
            }
            "TRIMESTRE" -> {
                calendar.add(Calendar.MONTH, -3)
                transacciones.filter { it.fecha >= calendar.timeInMillis }
            }
            "AÑO" -> {
                calendar.add(Calendar.YEAR, -1)
                transacciones.filter { it.fecha >= calendar.timeInMillis }
            }
            else -> transacciones
        }
    }

    private fun calcularEstadisticas(transacciones: List<com.daniel.loszetas.data.entities.Transaccion>) {
        val totalIngresos = transacciones
            .filter { !it.esGasto }
            .sumOf { it.monto }

        val totalGastos = transacciones
            .filter { it.esGasto }
            .sumOf { it.monto }

        val neto = totalIngresos - totalGastos

        with(binding) {
            tvIngresosEstadisticas.text = currencyFormat.format(totalIngresos)
            tvGastosEstadisticas.text = currencyFormat.format(totalGastos)
            tvNetoEstadisticas.text = currencyFormat.format(neto)
        }
    }

    private fun calcularEstadisticasAvanzadas(transacciones: List<com.daniel.loszetas.data.entities.Transaccion>) {
        val gastos = transacciones.filter { it.esGasto }

        // Promedio diario de gastos
        val dias = when (periodoSeleccionado) {
            "MES" -> 30
            "TRIMESTRE" -> 90
            "AÑO" -> 365
            else -> 30
        }
        val promedioDiario = if (gastos.isNotEmpty()) {
            gastos.sumOf { it.monto } / dias
        } else 0.0

        binding.tvPromedioDiario.text = "${currencyFormat.format(promedioDiario)}/día"

        // Categoría con más gastos
        val gastosPorCategoria = gastos.groupBy { it.categoria }
            .mapValues { entry -> entry.value.sumOf { it.monto } }
            .toList()
            .sortedByDescending { it.second }

        if (gastosPorCategoria.isNotEmpty()) {
            val topCategoria = gastosPorCategoria.first()
            binding.tvTopCategoria.text = topCategoria.first
            binding.tvTopCategoriaMonto.text = currencyFormat.format(topCategoria.second)
        } else {
            binding.tvTopCategoria.text = "N/A"
            binding.tvTopCategoriaMonto.text = "$0"
        }

        // Distribución por categorías (Top 3)
        mostrarDistribucionCategorias(gastosPorCategoria.take(3))
    }

    private fun mostrarDistribucionCategorias(topCategorias: List<Pair<String, Double>>) {
        if (topCategorias.isEmpty()) {
            binding.tvCategoria1.text = "Sin datos"
            binding.tvCategoria2.text = ""
            binding.tvCategoria3.text = ""
            return
        }

        val total = topCategorias.sumOf { it.second }

        topCategorias.getOrNull(0)?.let {
            val porcentaje = if (total > 0) (it.second / total * 100).toInt() else 0
            binding.tvCategoria1.text = "${it.first}: $porcentaje%"
        }

        topCategorias.getOrNull(1)?.let {
            val porcentaje = if (total > 0) (it.second / total * 100).toInt() else 0
            binding.tvCategoria2.text = "${it.first}: $porcentaje%"
        }

        topCategorias.getOrNull(2)?.let {
            val porcentaje = if (total > 0) (it.second / total * 100).toInt() else 0
            binding.tvCategoria3.text = "${it.first}: $porcentaje%"
        }
    }
}