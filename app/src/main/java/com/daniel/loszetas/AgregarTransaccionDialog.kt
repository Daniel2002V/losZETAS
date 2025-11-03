package com.daniel.loszetas

import android.app.DatePickerDialog
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import com.daniel.loszetas.data.entities.Transaccion
import com.daniel.loszetas.databinding.DialogAgregarTransaccionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.*

class AgregarTransaccionDialog(
    private val context: Context,
    private val onTransaccionCreada: (Transaccion) -> Unit
) {

    private val dialog = BottomSheetDialog(context)
    private lateinit var binding: DialogAgregarTransaccionBinding

    private var esGasto = false
    private var fechaSeleccionada = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    // Opciones para spinners
    private val categoriasGasto = arrayOf("Hogar", "Transporte", "Comida", "Entretenimiento", "Salud", "Otros")
    private val categoriasIngreso = arrayOf("Sueldo", "Freelance", "Inversiones", "Reembolso", "Otros")
    private val metodosPago = arrayOf("Tarjeta", "Efectivo", "Transferencia", "Otro")
    private val cuentasDestino = arrayOf("Cuenta principal", "Cuenta de ahorros", "Efectivo")

    fun mostrar(esGasto: Boolean = false) {
        this.esGasto = esGasto

        binding = DialogAgregarTransaccionBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        inicializarVistas()
        configurarSpinners()
        configurarListeners()
        actualizarUI()

        dialog.show()
    }

    private fun inicializarVistas() {
        // Mostrar fecha actual
        binding.btnFecha.text = dateFormat.format(fechaSeleccionada.time)
    }

    private fun configurarSpinners() {
        // Categorías
        val categorias = if (esGasto) categoriasGasto else categoriasIngreso
        val adapterCategorias = ArrayAdapter(context, android.R.layout.simple_spinner_item, categorias)
        adapterCategorias.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategoria.adapter = adapterCategorias

        // Métodos de pago
        val adapterMetodos = ArrayAdapter(context, android.R.layout.simple_spinner_item, metodosPago)
        adapterMetodos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMetodoPago.adapter = adapterMetodos

        // Cuentas destino
        val adapterCuentas = ArrayAdapter(context, android.R.layout.simple_spinner_item, cuentasDestino)
        adapterCuentas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCuentaDestino.adapter = adapterCuentas
    }

    private fun configurarListeners() {
        with(binding) {
            // Tabs
            btnTabGasto.setOnClickListener {
                esGasto = true
                actualizarUI()
                configurarSpinners()
            }

            btnTabIngreso.setOnClickListener {
                esGasto = false
                actualizarUI()
                configurarSpinners()
            }

            // Selector de fecha
            btnFecha.setOnClickListener {
                val datePicker = DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        fechaSeleccionada.set(year, month, day)
                        btnFecha.text = dateFormat.format(fechaSeleccionada.time)
                    },
                    fechaSeleccionada.get(Calendar.YEAR),
                    fechaSeleccionada.get(Calendar.MONTH),
                    fechaSeleccionada.get(Calendar.DAY_OF_MONTH)
                )
                datePicker.show()
            }

            // Botones
            btnCancelar.setOnClickListener {
                dialog.dismiss()
            }

            btnGuardar.setOnClickListener {
                guardarTransaccion()
            }
        }
    }

    private fun actualizarUI() {
        with(binding) {
            // Título
            tvTitulo.text = if (esGasto) "Agregar gasto" else "Agregar ingreso"

            // Tabs
            if (esGasto) {
                btnTabGasto.setBackgroundColor(context.getColor(android.R.color.holo_red_light))
                btnTabIngreso.setBackgroundColor(context.getColor(android.R.color.darker_gray))
            } else {
                btnTabGasto.setBackgroundColor(context.getColor(android.R.color.darker_gray))
                btnTabIngreso.setBackgroundColor(context.getColor(android.R.color.holo_blue_light))
            }

            // Campos específicos
            if (esGasto) {
                tvMetodoPago.visibility = View.VISIBLE
                spinnerMetodoPago.visibility = View.VISIBLE
                tvCuentaDestino.visibility = View.GONE
                spinnerCuentaDestino.visibility = View.GONE
            } else {
                tvMetodoPago.visibility = View.GONE
                spinnerMetodoPago.visibility = View.GONE
                tvCuentaDestino.visibility = View.VISIBLE
                spinnerCuentaDestino.visibility = View.VISIBLE
            }

            // Botón guardar
            btnGuardar.text = if (esGasto) "Guardar gasto" else "Guardar ingreso"
        }
    }

    private fun guardarTransaccion() {
        with(binding) {
            // Validar monto
            val montoStr = etMonto.text.toString()
            if (montoStr.isEmpty()) {
                Toast.makeText(context, "Ingresa un monto", Toast.LENGTH_SHORT).show()
                return
            }

            val monto = montoStr.toDoubleOrNull()
            if (monto == null || monto <= 0) {
                Toast.makeText(context, "Ingresa un monto válido", Toast.LENGTH_SHORT).show()
                return
            }

            // Obtener valores
            val categoria = spinnerCategoria.selectedItem.toString()
            val descripcion = etDescripcion.text.toString().ifEmpty { "Sin descripción" }
            val metodoPago = if (esGasto) spinnerMetodoPago.selectedItem.toString() else null
            val cuentaDestino = if (!esGasto) spinnerCuentaDestino.selectedItem.toString() else null

            // Crear transacción
            val transaccion = Transaccion(
                esGasto = esGasto,
                monto = monto,
                categoria = categoria,
                descripcion = descripcion,
                fecha = fechaSeleccionada.timeInMillis,
                metodoPago = metodoPago,
                cuentaDestino = cuentaDestino
            )

            onTransaccionCreada(transaccion)
            dialog.dismiss()
        }
    }
}