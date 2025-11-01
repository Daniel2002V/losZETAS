package com.daniel.loszetas

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.daniel.loszetas.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupButtons()
        setupMockData()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun setupButtons() {
        binding.btnIngreso.setOnClickListener {
            // TODO: Ir a Agregar Ingreso
        }

        binding.btnGasto.setOnClickListener {
            // TODO: Ir a Agregar Gasto
        }

        binding.btnVerHistorial.setOnClickListener {
            // TODO: Ir a Historial
        }
    }

    private fun setupMockData() {
        binding.tvSaldoActual.text = "$1.254.320"
        binding.tvIngresos.text = "$1.550.000"
        binding.tvGastos.text = "$780.200"
        binding.tvNeto.text = "$769.800"
        binding.tvProgresoMeta.text = "Presup.: 75%"
    }
}