package com.daniel.loszetas

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.databinding.ActivityPresupuestosMetasBinding
import kotlinx.coroutines.launch

class PresupuestosMetasActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPresupuestosMetasBinding
    private lateinit var presupuestoAdapter: PresupuestoAdapter
    private lateinit var metaAdapter: MetaAdapter
    private val database by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityPresupuestosMetasBinding.inflate(layoutInflater)
        setContentView(binding.root)

        configurarRecyclerViews()
        configurarBotones()
        configurarNavegacion()
        cargarDatos()
    }

    private fun configurarRecyclerViews() {
        presupuestoAdapter = PresupuestoAdapter()
        binding.rvPresupuestos.apply {
            layoutManager = LinearLayoutManager(this@PresupuestosMetasActivity)
            adapter = presupuestoAdapter
        }

        metaAdapter = MetaAdapter()
        binding.rvMetas.apply {
            layoutManager = LinearLayoutManager(this@PresupuestosMetasActivity)
            adapter = metaAdapter
        }
    }

    private fun configurarBotones() {
        binding.btnNuevoPresupuesto.setOnClickListener {
            startActivity(Intent(this, NuevoPresupuestoActivity::class.java))
        }

        binding.btnNuevaMeta.setOnClickListener {
            startActivity(Intent(this, NuevaMetaActivity::class.java))
        }
    }

    private fun configurarNavegacion() {
        binding.bottomNavigation.selectedItemId = R.id.nav_presupuesto

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
                R.id.nav_presupuesto -> true
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
            // Cargar presupuestos
            database.presupuestoDao().obtenerTodos().collect { presupuestos ->
                presupuestoAdapter.actualizarPresupuestos(presupuestos)
            }
        }

        lifecycleScope.launch {
            // Cargar metas
            database.metaDao().obtenerActivas().collect { metas ->
                metaAdapter.actualizarMetas(metas)
            }
        }
    }
}