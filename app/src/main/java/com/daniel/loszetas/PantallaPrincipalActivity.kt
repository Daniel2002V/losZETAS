package com.daniel.loszetas

import android.content.Intent
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
    private val database by lazy { AppDatabase.getDatabase(this) }
    private val currencyFormat = NumberFormat.getCurrencyInstance(Locale("es", "CL"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth

        val usuario = auth.currentUser
        Toast.makeText(this, "Bienvenido ${usuario?.email}", Toast.LENGTH_SHORT).show()

        configurarRecyclerView()
        configurarBotones()
        configurarNavegacion()
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
            btnCLP.setOnClickListener {
                Toast.makeText(this@PantallaPrincipalActivity, "Moneda: CLP", Toast.LENGTH_SHORT).show()
            }

            btnAgregar.setOnClickListener {
                mostrarDialogoAgregar()
            }

            btnIngreso.setOnClickListener {
                mostrarDialogoAgregar(esGasto = false)
            }

            btnGasto.setOnClickListener {
                mostrarDialogoAgregar(esGasto = true)
            }

            tvVerHistorial.setOnClickListener {
                navegarA(HistorialActivity::class.java)
            }
        }
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.selectedItemId = R.id.nav_inicio

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_inicio -> true
                R.id.nav_historial -> {
                    navegarA(HistorialActivity::class.java)
                    true
                }
                R.id.nav_estadisticas -> {
                    navegarA(EstadisticasActivity::class.java)
                    true
                }
                R.id.nav_presupuesto -> {
                    Toast.makeText(this, "Presupuestos/Metas próximamente", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_ajustes -> {
                    Toast.makeText(this, "Ajustes próximamente", Toast.LENGTH_SHORT).show()
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
                database.transaccionDao().obtenerTodas().collect { transacciones ->
                    val transaccionesRecientes = transacciones.takeLast(5)
                    transaccionAdapter.actualizarTransacciones(transaccionesRecientes)

                    val totalIngresos = transacciones
                        .filter { !it.esGasto }
                        .sumOf { it.monto }

                    val totalGastos = transacciones
                        .filter { it.esGasto }
                        .sumOf { it.monto }

                    val saldoActual = totalIngresos - totalGastos
                    val neto = totalIngresos - totalGastos

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