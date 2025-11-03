package com.daniel.loszetas

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.data.entities.Transaccion
import com.daniel.loszetas.databinding.ActivityPantallaPrincipalBinding
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.*

class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var transaccionAdapter: TransaccionAdapter

    // Usar el singleton de AppDatabase
    private val database by lazy { AppDatabase.getDatabase(this) }

    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Inicializar ViewBinding
        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Inicializar Firebase
        auth = Firebase.auth

        // Mostrar mensaje de bienvenida
        val usuario = auth.currentUser
        Toast.makeText(
            this,
            "Bienvenido ${usuario?.email}",
            Toast.LENGTH_SHORT
        ).show()

        configurarRecyclerView()
        configurarBotones()
        cargarDatos()
    }

    private fun configurarRecyclerView() {
        transaccionAdapter = TransaccionAdapter()
        binding.rvMovimientos.apply {
            layoutManager = LinearLayoutManager(this@PantallaPrincipalActivity)
            adapter = transaccionAdapter
        }
    }

    private fun configurarBotones() {
        with(binding) {
            // Botón CLP
            btnCLP.setOnClickListener {
                Toast.makeText(
                    this@PantallaPrincipalActivity,
                    "Moneda: CLP",
                    Toast.LENGTH_SHORT
                ).show()
            }

            // Botón Ingreso
            btnIngreso.setOnClickListener {
                mostrarDialogoAgregar(esGasto = false)
            }

            // Botón Gasto
            btnGasto.setOnClickListener {
                mostrarDialogoAgregar(esGasto = true)
            }

            // FAB Agregar
            fabAgregar.setOnClickListener {
                mostrarDialogoAgregar()
            }
        }
    }

    private fun mostrarDialogoAgregar(esGasto: Boolean = false) {
        val dialog = AgregarTransaccionDialog(this) { transaccion ->
            guardarTransaccion(transaccion)
        }
        dialog.mostrar(esGasto)
    }

    private fun guardarTransaccion(transaccion: Transaccion) {
        lifecycleScope.launch {
            try {
                database.transaccionDao().insertar(transaccion)
                Toast.makeText(
                    this@PantallaPrincipalActivity,
                    "Transacción guardada",
                    Toast.LENGTH_SHORT
                ).show()
                // No necesitamos llamar cargarDatos() porque Flow actualiza automáticamente
            } catch (e: Exception) {
                Toast.makeText(
                    this@PantallaPrincipalActivity,
                    "Error al guardar: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun cargarDatos() {
        lifecycleScope.launch {
            try {
                // Usar Flow para observar cambios automáticamente
                database.transaccionDao().obtenerTodas().collect { transacciones ->
                    transaccionAdapter.actualizarTransacciones(transacciones)

                    // Calcular totales
                    val totalIngresos = transacciones
                        .filter { !it.esGasto }
                        .sumOf { it.monto }

                    val totalGastos = transacciones
                        .filter { it.esGasto }
                        .sumOf { it.monto }

                    val saldoActual = totalIngresos - totalGastos
                    val neto = totalIngresos - totalGastos

                    // Actualizar UI con ViewBinding
                    with(binding) {
                        tvSaldoActual.text = currencyFormat.format(saldoActual)
                        tvIngresos.text = currencyFormat.format(totalIngresos)
                        tvGastos.text = currencyFormat.format(totalGastos)
                        tvNeto.text = currencyFormat.format(neto)
                    }
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@PantallaPrincipalActivity,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}