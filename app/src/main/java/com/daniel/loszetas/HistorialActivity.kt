package com.daniel.loszetas

import android.app.DatePickerDialog
import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.data.entities.Transaccion
import com.daniel.loszetas.databinding.ActivityHistorialBinding
import com.daniel.loszetas.databinding.DialogSeleccionarCategoriaBinding
import com.daniel.loszetas.databinding.ItemCategoriaBinding
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.cancellation.CancellationException

class HistorialActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHistorialBinding
    private lateinit var transaccionAdapter: TransaccionAdapter
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "CL"))

    private var todasTransacciones: List<Transaccion> = emptyList()
    private var filtroCategoria: String? = null
    private var filtroTipo: String? = null
    private var filtroFechaInicio: Long? = null
    private var filtroFechaFin: Long? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHistorialBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerView()
        configurarBuscador()
        configurarFiltros()
        configurarNavegacion()
        cargarDatos()
    }

    private fun configurarRecyclerView() {
        transaccionAdapter = TransaccionAdapter()
        binding.rvHistorial.apply {
            layoutManager = LinearLayoutManager(this@HistorialActivity)
            adapter = transaccionAdapter
        }
    }

    private fun configurarBuscador() {
        binding.etBuscar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                aplicarFiltros()
            }
        })
    }

    private fun configurarFiltros() {
        binding.btnFecha.setOnClickListener {
            mostrarSelectorFecha()
        }

        binding.btnCategoria.setOnClickListener {
            mostrarDialogoCategoria()
        }

        binding.btnTipo.setOnClickListener {
            mostrarSelectorTipo()
        }
    }

    private fun mostrarDialogoCategoria() {
        val dialog = Dialog(this)
        val dialogBinding = DialogSeleccionarCategoriaBinding.inflate(layoutInflater)
        dialog.setContentView(dialogBinding.root)

        dialog.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        var categoriaSeleccionada: String? = filtroCategoria
        var categoriasDisponibles: List<String> = emptyList()

        fun obtenerCategoriasFrecuentes(esGasto: Boolean?): List<String> {
            val transaccionesFiltradas = when (esGasto) {
                true -> todasTransacciones.filter { it.esGasto }
                false -> todasTransacciones.filter { !it.esGasto }
                null -> todasTransacciones
            }

            return transaccionesFiltradas
                .groupBy { it.categoria }
                .mapValues { it.value.size }
                .entries
                .sortedByDescending { it.value }
                .take(3)
                .map { it.key }
        }

        fun cargarCategorias(categorias: List<String>, contenedor: ViewGroup) {
            contenedor.removeAllViews()
            categorias.forEach { categoria ->
                val itemBinding = ItemCategoriaBinding.inflate(layoutInflater, contenedor, false)

                itemBinding.tvNombreCategoria.text = categoria

                if (categoria == categoriaSeleccionada) {
                    itemBinding.layoutCategoria.setBackgroundColor(0xFFDDD6FE.toInt())
                } else {
                    itemBinding.layoutCategoria.setBackgroundColor(0xFFF3F4F6.toInt())
                }

                itemBinding.root.setOnClickListener {
                    categoriaSeleccionada = categoria
                    cargarCategorias(categorias, contenedor)
                }

                contenedor.addView(itemBinding.root)
            }
        }

        fun actualizarCategoriasPorTipo(tipo: String) {
            lifecycleScope.launch {
                val flow = when (tipo) {
                    "gastos" -> database.categoriaDao().obtenerCategoriasGasto()
                    "ingresos" -> database.categoriaDao().obtenerCategoriasIngreso()
                    else -> database.categoriaDao().obtenerTodasCategorias()
                }

                flow.collect { categorias ->
                    categoriasDisponibles = categorias.map { it.nombre }

                    val frecuentes = obtenerCategoriasFrecuentes(
                        when (tipo) {
                            "gastos" -> true
                            "ingresos" -> false
                            else -> null
                        }
                    )

                    if (frecuentes.isNotEmpty()) {
                        cargarCategorias(frecuentes, dialogBinding.listaSugeridas)
                    } else {
                        dialogBinding.listaSugeridas.removeAllViews()
                    }

                    cargarCategorias(categoriasDisponibles, dialogBinding.listaTodasCategorias)
                }
            }
        }

        actualizarCategoriasPorTipo("todas")

        with(dialogBinding) {
            chipTodas.setOnClickListener {
                resetearChips(dialogBinding)
                chipTodas.setBackgroundResource(R.drawable.bg_button_primary)
                chipTodas.setTextColor(getColor(android.R.color.white))
                actualizarCategoriasPorTipo("todas")
            }

            chipGastos.setOnClickListener {
                resetearChips(dialogBinding)
                chipGastos.setBackgroundResource(R.drawable.bg_button_primary)
                chipGastos.setTextColor(getColor(android.R.color.white))
                actualizarCategoriasPorTipo("gastos")
            }

            chipIngresos.setOnClickListener {
                resetearChips(dialogBinding)
                chipIngresos.setBackgroundResource(R.drawable.bg_button_primary)
                chipIngresos.setTextColor(getColor(android.R.color.white))
                actualizarCategoriasPorTipo("ingresos")
            }

            chipFrecuentes.setOnClickListener {
                resetearChips(dialogBinding)
                chipFrecuentes.setBackgroundResource(R.drawable.bg_button_primary)
                chipFrecuentes.setTextColor(getColor(android.R.color.white))

                val frecuentes = obtenerCategoriasFrecuentes(null)
                listaSugeridas.removeAllViews()
                if (frecuentes.isNotEmpty()) {
                    cargarCategorias(frecuentes, listaTodasCategorias)
                } else {
                    Toast.makeText(this@HistorialActivity, "No hay transacciones aún", Toast.LENGTH_SHORT).show()
                }
            }

            etBuscarCategoria.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val busqueda = s.toString().lowercase()
                    filtrarVistasCategorias(listaSugeridas, busqueda)
                    filtrarVistasCategorias(listaTodasCategorias, busqueda)
                }
            })

            btnCerrarDialogo.setOnClickListener { dialog.dismiss() }

            btnLimpiarCategoria.setOnClickListener {
                categoriaSeleccionada = null
                actualizarCategoriasPorTipo("todas")
            }

            btnAplicarCategoria.setOnClickListener {
                filtroCategoria = categoriaSeleccionada
                aplicarFiltros()
                Toast.makeText(
                    this@HistorialActivity,
                    if (categoriaSeleccionada != null) "Filtrado por: $categoriaSeleccionada" else "Mostrando todas",
                    Toast.LENGTH_SHORT
                ).show()
                dialog.dismiss()
            }
        }

        dialog.show()
    }

    private fun resetearChips(dialogBinding: DialogSeleccionarCategoriaBinding) {
        with(dialogBinding) {
            chipTodas.setBackgroundResource(R.drawable.bg_button_outline)
            chipTodas.setTextColor(0xFF7C3AED.toInt())
            chipGastos.setBackgroundResource(R.drawable.bg_button_outline)
            chipGastos.setTextColor(0xFF7C3AED.toInt())
            chipIngresos.setBackgroundResource(R.drawable.bg_button_outline)
            chipIngresos.setTextColor(0xFF7C3AED.toInt())
            chipFrecuentes.setBackgroundResource(R.drawable.bg_button_outline)
            chipFrecuentes.setTextColor(0xFF7C3AED.toInt())
        }
    }

    private fun filtrarVistasCategorias(contenedor: ViewGroup, busqueda: String) {
        for (i in 0 until contenedor.childCount) {
            val itemView = contenedor.getChildAt(i)
            val itemBinding = ItemCategoriaBinding.bind(itemView)
            itemView.visibility = if (itemBinding.tvNombreCategoria.text.toString().lowercase().contains(busqueda)) {
                View.VISIBLE
            } else {
                View.GONE
            }
        }
    }

    private fun mostrarSelectorFecha() {
        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->
                val fechaInicio = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                }.timeInMillis

                val fechaFin = Calendar.getInstance().apply {
                    set(year, month, day, 23, 59, 59)
                }.timeInMillis

                filtroFechaInicio = fechaInicio
                filtroFechaFin = fechaFin
                aplicarFiltros()

                Toast.makeText(this, "Filtrado por fecha: ${dateFormat.format(fechaInicio)}", Toast.LENGTH_SHORT).show()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun mostrarSelectorTipo() {
        val tipos = arrayOf("Todos", "Ingresos", "Gastos")

        AlertDialog.Builder(this)
            .setTitle("Filtrar por tipo")
            .setItems(tipos) { _, which ->
                filtroTipo = when (which) {
                    0 -> null
                    1 -> "ingreso"
                    else -> "gasto"
                }
                aplicarFiltros()
                Toast.makeText(this, "Filtrado por: ${tipos[which]}", Toast.LENGTH_SHORT).show()
            }
            .show()
    }

    private fun aplicarFiltros() {
        val textoBusqueda = binding.etBuscar.text.toString().lowercase()
        var transaccionesFiltradas = todasTransacciones

        if (textoBusqueda.isNotEmpty()) {
            transaccionesFiltradas = transaccionesFiltradas.filter {
                it.descripcion.lowercase().contains(textoBusqueda) ||
                        it.categoria.lowercase().contains(textoBusqueda)
            }
        }

        if (filtroCategoria != null) {
            transaccionesFiltradas = transaccionesFiltradas.filter {
                it.categoria == filtroCategoria
            }
        }

        when (filtroTipo) {
            "ingreso" -> transaccionesFiltradas = transaccionesFiltradas.filter { !it.esGasto }
            "gasto" -> transaccionesFiltradas = transaccionesFiltradas.filter { it.esGasto }
        }

        if (filtroFechaInicio != null && filtroFechaFin != null) {
            transaccionesFiltradas = transaccionesFiltradas.filter {
                it.fecha in filtroFechaInicio!!..filtroFechaFin!!
            }
        }

        transaccionAdapter.actualizarTransacciones(transaccionesFiltradas)
        actualizarResumen(transaccionesFiltradas)
    }

    private fun actualizarResumen(transacciones: List<Transaccion>) {
        val totalIngresos = transacciones.filter { !it.esGasto }.sumOf { it.monto }
        val totalGastos = transacciones.filter { it.esGasto }.sumOf { it.monto }
        val neto = totalIngresos - totalGastos

        with(binding) {
            tvIngresosHistorial.text = currencyFormat.format(totalIngresos)
            tvGastosHistorial.text = currencyFormat.format(totalGastos)
            tvNetoHistorial.text = currencyFormat.format(neto)
        }
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.selectedItemId = R.id.nav_historial

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> {
                    navegarA(PantallaPrincipalActivity::class.java)
                    true
                }
                R.id.nav_historial -> true
                R.id.nav_estadisticas -> {
                    navegarA(EstadisticasActivity::class.java)
                    true
                }
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
                    todasTransacciones = transacciones
                    aplicarFiltros()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Toast.makeText(
                    this@HistorialActivity,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}