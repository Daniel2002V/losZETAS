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
import com.daniel.loszetas.utils.ConfiguracionApp
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class PantallaPrincipalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPantallaPrincipalBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var transaccionAdapter: TransaccionAdapter
    private val database by lazy { AppDatabase.getDatabase(this) }
    private var cargarDatosJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPantallaPrincipalBinding.inflate(layoutInflater)
        setContentView(binding.root)

        auth = Firebase.auth


        configurarRecyclerView()
        configurarBotones()
        configurarNavegacion()
        cargarDatos()
        actualizarMonedaDisplay()
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
                // Abrir ajustes para cambiar moneda
                startActivity(Intent(this@PantallaPrincipalActivity, AjustesActivity::class.java))
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

    private fun actualizarMonedaDisplay() {
        val moneda = ConfiguracionApp.obtenerMoneda(this)
        binding.btnCLP.text = moneda.codigo
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
        cargarDatosJob?.cancel()

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
                // Recargar datos después de guardar
                cargarDatos()
            } catch (e: CancellationException) {
                // No hacer nada, es normal cuando se cancela
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
        cargarDatosJob?.cancel()

        cargarDatosJob = lifecycleScope.launch {
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
                        // Usar ConfiguracionApp para formatear moneda
                        tvSaldoActual.text = ConfiguracionApp.formatearMoneda(
                            this@PantallaPrincipalActivity,
                            saldoActual
                        )
                        tvIngresos.text = ConfiguracionApp.formatearMoneda(
                            this@PantallaPrincipalActivity,
                            totalIngresos
                        )
                        tvGastos.text = ConfiguracionApp.formatearMoneda(
                            this@PantallaPrincipalActivity,
                            totalGastos
                        )
                        tvNeto.text = ConfiguracionApp.formatearMoneda(
                            this@PantallaPrincipalActivity,
                            neto
                        )
                    }
                }
            } catch (e: CancellationException) {

            } catch (e: Exception) {
                Toast.makeText(
                    this@PantallaPrincipalActivity,
                    "Error al cargar datos: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        actualizarMonedaDisplay()
        cargarDatos()
    }

    override fun onDestroy() {
        super.onDestroy()
        cargarDatosJob?.cancel()
    }
}