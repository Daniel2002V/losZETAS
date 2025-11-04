package com.daniel.loszetas

import android.app.DatePickerDialog
import android.content.Context
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import com.daniel.loszetas.data.database.AppDatabase
import com.daniel.loszetas.data.entities.Meta
import com.daniel.loszetas.data.entities.Presupuesto
import com.daniel.loszetas.data.entities.Transaccion
import com.daniel.loszetas.databinding.DialogAgregarTransaccionBinding
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AgregarTransaccionDialog(
    private val context: Context,
    private val onTransaccionCreada: (Transaccion) -> Unit
) {

    private val dialog = BottomSheetDialog(context)
    private lateinit var binding: DialogAgregarTransaccionBinding
    private val database by lazy { AppDatabase.getDatabase(context) }

    private var esGasto = false
    private var fechaSeleccionada = Calendar.getInstance()
    private val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    private val metodosPago = arrayOf("Tarjeta", "Efectivo", "Transferencia", "Otro")
    private val cuentasDestino = arrayOf("Cuenta principal", "Cuenta de ahorros", "Efectivo")

    // Para presupuestos (gastos)
    private var presupuestosDisponibles = listOf<Presupuesto>()
    private var presupuestoSeleccionado: Presupuesto? = null

    // Para metas (ingresos)
    private var metasDisponibles = listOf<Meta>()
    private var metaSeleccionada: Meta? = null

    fun mostrar(esGasto: Boolean = false) {
        this.esGasto = esGasto

        binding = DialogAgregarTransaccionBinding.inflate(android.view.LayoutInflater.from(context))
        dialog.setContentView(binding.root)

        inicializarVistas()
        configurarSpinners()
        configurarListeners()
        actualizarUI()
        cargarCategorias()

        // Cargar presupuestos si es un gasto, o metas si es un ingreso
        if (esGasto) {
            cargarPresupuestos()
        } else {
            cargarMetas()
        }

        dialog.show()
    }

    private fun inicializarVistas() {
        binding.btnFecha.text = dateFormat.format(fechaSeleccionada.time)
    }

    private fun configurarSpinners() {
        val adapterMetodos = ArrayAdapter(context, android.R.layout.simple_spinner_item, metodosPago)
        adapterMetodos.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerMetodoPago.adapter = adapterMetodos

        val adapterCuentas = ArrayAdapter(context, android.R.layout.simple_spinner_item, cuentasDestino)
        adapterCuentas.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCuentaDestino.adapter = adapterCuentas
    }

    private fun cargarCategorias() {
        if (context !is LifecycleOwner) return

        context.lifecycleScope.launch {
            val flow = if (esGasto) {
                database.categoriaDao().obtenerCategoriasGasto()
            } else {
                database.categoriaDao().obtenerCategoriasIngreso()
            }

            flow.collect { categorias ->
                val nombresCategorias = categorias.map { it.nombre }.toTypedArray()
                val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, nombresCategorias)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                binding.spinnerCategoria.adapter = adapter
            }
        }
    }

    private fun cargarPresupuestos() {
        if (context !is LifecycleOwner) return

        context.lifecycleScope.launch {
            try {
                presupuestosDisponibles = database.presupuestoDao().obtenerTodos().first()

                if (presupuestosDisponibles.isNotEmpty()) {
                    // Mostrar el selector de presupuestos
                    binding.layoutPresupuesto.visibility = View.VISIBLE
                    binding.tvTituloSecundario.text = "Aplicar a presupuesto"

                    val nombresPresupuestos = presupuestosDisponibles.map { presupuesto ->
                        "${presupuesto.categoria} - $${String.format("%.2f", presupuesto.limiteTotal - presupuesto.gastoActual)} disponible"
                    }.toTypedArray()

                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, nombresPresupuestos)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerSecundario.adapter = adapter

                    // Listener para guardar el presupuesto seleccionado
                    binding.spinnerSecundario.setOnItemSelectedListener(
                        object : android.widget.AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: android.widget.AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                presupuestoSeleccionado = presupuestosDisponibles[position]
                            }

                            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                                presupuestoSeleccionado = null
                            }
                        }
                    )

                    // Checkbox para aplicar al presupuesto
                    binding.checkboxAplicar.setOnCheckedChangeListener { _, isChecked ->
                        binding.spinnerSecundario.isEnabled = isChecked
                        if (!isChecked) {
                            presupuestoSeleccionado = null
                        } else if (presupuestosDisponibles.isNotEmpty()) {
                            presupuestoSeleccionado = presupuestosDisponibles[binding.spinnerSecundario.selectedItemPosition]
                        }
                    }
                } else {
                    binding.layoutPresupuesto.visibility = View.GONE
                }
            } catch (e: Exception) {
                binding.layoutPresupuesto.visibility = View.GONE
            }
        }
    }

    private fun cargarMetas() {
        if (context !is LifecycleOwner) return

        context.lifecycleScope.launch {
            try {
                metasDisponibles = database.metaDao().obtenerActivas().first()

                if (metasDisponibles.isNotEmpty()) {
                    // Mostrar el selector de metas
                    binding.layoutPresupuesto.visibility = View.VISIBLE
                    binding.tvTituloSecundario.text = "Aplicar a meta de ahorro"
                    binding.checkboxAplicar.text = "Aplicar a meta de ahorro"

                    val nombresMetas = metasDisponibles.map { meta ->
                        val progreso = (meta.montoActual / meta.montoObjetivo * 100).toInt()
                        "${meta.nombre} - $${String.format("%.2f", meta.montoActual)} de $${String.format("%.2f", meta.montoObjetivo)} ($progreso%)"
                    }.toTypedArray()

                    val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, nombresMetas)
                    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                    binding.spinnerSecundario.adapter = adapter

                    // Listener para guardar la meta seleccionada
                    binding.spinnerSecundario.setOnItemSelectedListener(
                        object : android.widget.AdapterView.OnItemSelectedListener {
                            override fun onItemSelected(
                                parent: android.widget.AdapterView<*>?,
                                view: View?,
                                position: Int,
                                id: Long
                            ) {
                                metaSeleccionada = metasDisponibles[position]
                            }

                            override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {
                                metaSeleccionada = null
                            }
                        }
                    )

                    // Checkbox para aplicar a la meta
                    binding.checkboxAplicar.setOnCheckedChangeListener { _, isChecked ->
                        binding.spinnerSecundario.isEnabled = isChecked
                        if (!isChecked) {
                            metaSeleccionada = null
                        } else if (metasDisponibles.isNotEmpty()) {
                            metaSeleccionada = metasDisponibles[binding.spinnerSecundario.selectedItemPosition]
                        }
                    }
                } else {
                    binding.layoutPresupuesto.visibility = View.GONE
                }
            } catch (e: Exception) {
                binding.layoutPresupuesto.visibility = View.GONE
            }
        }
    }

    private fun configurarListeners() {
        with(binding) {
            btnTabGasto.setOnClickListener {
                esGasto = true
                actualizarUI()
                cargarCategorias()
                cargarPresupuestos()
            }

            btnTabIngreso.setOnClickListener {
                esGasto = false
                actualizarUI()
                cargarCategorias()
                cargarMetas()
            }

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
            tvTitulo.text = if (esGasto) "Agregar gasto" else "Agregar ingreso"

            if (esGasto) {
                btnTabGasto.setBackgroundColor(context.getColor(android.R.color.holo_red_light))
                btnTabIngreso.setBackgroundColor(context.getColor(android.R.color.darker_gray))
            } else {
                btnTabGasto.setBackgroundColor(context.getColor(android.R.color.darker_gray))
                btnTabIngreso.setBackgroundColor(context.getColor(android.R.color.holo_blue_light))
            }

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

            btnGuardar.text = if (esGasto) "Guardar gasto" else "Guardar ingreso"
        }
    }

    private fun guardarTransaccion() {
        if (context !is LifecycleOwner) return

        with(binding) {
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

            val categoria = spinnerCategoria.selectedItem?.toString() ?: "Otros"
            val descripcion = etDescripcion.text.toString().ifEmpty { "Sin descripción" }
            val metodoPago = if (esGasto) spinnerMetodoPago.selectedItem.toString() else null
            val cuentaDestino = if (!esGasto) spinnerCuentaDestino.selectedItem.toString() else null

            // Verificar si debe aplicarse al presupuesto (para gastos)
            val aplicarPresupuesto = esGasto && checkboxAplicar.isChecked && presupuestoSeleccionado != null

            // Verificar si debe aplicarse a la meta (para ingresos)
            val aplicarMeta = !esGasto && checkboxAplicar.isChecked && metaSeleccionada != null

            if (aplicarPresupuesto && presupuestoSeleccionado != null) {
                val presupuesto = presupuestoSeleccionado!!
                val nuevoGasto = presupuesto.gastoActual + monto

                if (nuevoGasto > presupuesto.limiteTotal) {
                    Toast.makeText(
                        context,
                        "¡Advertencia! Este gasto excederá el presupuesto de ${presupuesto.categoria}",
                        Toast.LENGTH_LONG
                    ).show()
                }

                // Actualizar presupuesto
                context.lifecycleScope.launch {
                    try {
                        database.presupuestoDao().actualizarGasto(presupuesto.id, nuevoGasto)

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

                        Toast.makeText(
                            context,
                            "Gasto guardado y presupuesto actualizado",
                            Toast.LENGTH_SHORT
                        ).show()
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error al actualizar presupuesto: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else if (aplicarMeta && metaSeleccionada != null) {
                val meta = metaSeleccionada!!
                val nuevoMonto = meta.montoActual + monto

                // Verificar si se completó la meta
                val metaCompletada = nuevoMonto >= meta.montoObjetivo

                // Actualizar meta
                context.lifecycleScope.launch {
                    try {
                        database.metaDao().actualizarMonto(meta.id, nuevoMonto.coerceAtMost(meta.montoObjetivo))

                        if (metaCompletada) {
                            database.metaDao().marcarCompletada(meta.id)
                        }

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

                        if (metaCompletada) {
                            Toast.makeText(
                                context,
                                "¡Felicidades! Meta \"${meta.nombre}\" completada 🎉",
                                Toast.LENGTH_LONG
                            ).show()
                        } else {
                            val porcentaje = (nuevoMonto / meta.montoObjetivo * 100).toInt()
                            Toast.makeText(
                                context,
                                "Ingreso guardado - Meta al $porcentaje%",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } catch (e: Exception) {
                        Toast.makeText(
                            context,
                            "Error al actualizar meta: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } else {
                // Guardar solo la transacción sin afectar presupuesto o meta
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
}